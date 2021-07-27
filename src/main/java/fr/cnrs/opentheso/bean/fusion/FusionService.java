package fr.cnrs.opentheso.bean.fusion;

import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.*;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.core.imports.rdf4j.ReadRdf4j;
import fr.cnrs.opentheso.core.imports.rdf4j.helper.AddConceptsStruct;
import fr.cnrs.opentheso.core.imports.rdf4j.helper.ImportRdf4jHelper;
import fr.cnrs.opentheso.skosapi.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Named
@ViewScoped
public class FusionService implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private SelectedTheso selectedTheso;

    @Inject
    private CurrentUser currentUser;

    private SKOSXmlDocument sourceSkos;
    private boolean loadDone, fusionDone, fusionBtnEnable;
    private String uri;
    private double total;
    private List<String> conceptsAjoutes, conceptsModifies, conceptsExists;

    public void lancerFussion(NodeIdValue thesoSelected) {

        fusionDone = false;
        fusionBtnEnable = true;
        conceptsModifies = new ArrayList<>();
        conceptsAjoutes = new ArrayList<>();
        conceptsExists = new ArrayList<>();

        ConceptHelper conceptHelper = new ConceptHelper();
        ImportRdf4jHelper importRdf4jHelper = new ImportRdf4jHelper();
        importRdf4jHelper.setDs(connect.getPoolConnexion());

        for (SKOSResource conceptSource : sourceSkos.getConceptList()) {
            if (!StringUtils.isEmpty(conceptSource.getIdentifier())) {

                importRdf4jHelper.setRdf4jThesaurus(sourceSkos);
                AddConceptsStruct acs = new AddConceptsStruct();
                importRdf4jHelper.initAddConceptsStruct(acs, conceptSource, thesoSelected.getId(), false);
                importRdf4jHelper.addRelation(acs, thesoSelected.getId());

                NodeConcept conceptFound = conceptHelper.getConcept(connect.getPoolConnexion(),
                        conceptSource.getIdentifier(),
                        thesoSelected.getId(),
                        selectedTheso.getCurrentLang());

                if (conceptFound == null && !conceptSource.getLabelsList().isEmpty()) {
                    importRdf4jHelper.addConceptToBdd(acs, thesoSelected.getId(), false);
                    conceptsAjoutes.add(acs.concept.getIdConcept() + " - " + conceptSource.getLabelsList().get(0).getLabel());
                } else {
                    boolean isUpdated = false;
                    //alignment
                    if (!CollectionUtils.isEmpty(acs.nodeAlignments)) {
                        for (NodeAlignment nodeAlignment : acs.nodeAlignments) {
                            if (!isAlignementExist(nodeAlignment, conceptFound)) {
                                new AlignmentHelper().addNewAlignment(connect.getPoolConnexion(),
                                        nodeAlignment.getId_author(),
                                        nodeAlignment.getConcept_target(),
                                        nodeAlignment.getThesaurus_target(),
                                        nodeAlignment.getUri_target(),// URI
                                        nodeAlignment.getAlignement_id_type(),
                                        conceptSource.getIdentifier(),
                                        conceptFound.getConcept().getIdThesaurus(),
                                        nodeAlignment.getId_alignement());
                                isUpdated = true;
                            }
                        }
                    }

                    // Synonymes : NonPreferredTerms
                    if (!CollectionUtils.isEmpty(acs.nodeEMList)) {
                        for (NodeEM nodeEM : acs.nodeEMList) {
                            if (!isSynonymeExist(nodeEM, conceptFound)) {
                                Term term = new Term();
                                term.setId_term(acs.nodeTerm.getIdTerm());
                                term.setId_concept(conceptSource.getIdentifier());
                                term.setLexical_value(nodeEM.getLexical_value());
                                term.setLang(nodeEM.getLang());
                                term.setId_thesaurus(conceptFound.getConcept().getIdThesaurus());
                                term.setSource(nodeEM.getSource());
                                term.setStatus(nodeEM.getStatus());
                                term.setHidden(nodeEM.isHiden());
                                new TermHelper().addNonPreferredTerm(connect.getPoolConnexion(), term,
                                        currentUser.getNodeUser().getIdUser());
                                isUpdated = true;
                            }
                        }
                    }

                    // Traduction
                    if (!CollectionUtils.isEmpty(acs.nodeTermTraductionList)) {
                        for (NodeTermTraduction nodeTermTraduction : acs.nodeTermTraductionList) {
                            if (!isTraductionExist(nodeTermTraduction, conceptFound)) {
                                try {
                                    Term term = new Term();
                                    term.setId_term(acs.nodeTerm.getIdTerm());
                                    term.setId_concept(conceptSource.getIdentifier());
                                    term.setLexical_value(nodeTermTraduction.getLexicalValue());
                                    term.setLang(nodeTermTraduction.getLang());
                                    term.setId_thesaurus(conceptFound.getConcept().getIdThesaurus());
                                    new TermHelper().addTermTraduction(connect.getPoolConnexion().getConnection(), term,
                                            currentUser.getNodeUser().getIdUser());
                                    isUpdated = true;
                                } catch (Exception ex) {

                                }
                            }
                        }
                    }

                    //Definition
                    if (!CollectionUtils.isEmpty(acs.nodeNotes)) {
                        NoteHelper noteHelper = new NoteHelper();
                        for (NodeNote nodeNote : acs.nodeNotes) {
                            ArrayList<String> definitions = noteHelper.getDefinition(connect.getPoolConnexion(), nodeNote.getId_concept(),
                                    conceptFound.getConcept().getIdThesaurus(), nodeNote.getLang());
                            if (!definitions.contains(nodeNote.getLexicalvalue())) {
                                noteHelper.addTermNote(connect.getPoolConnexion(),
                                        acs.nodeTerm.getIdTerm(),
                                        nodeNote.getLang(),
                                        conceptFound.getConcept().getIdThesaurus(),
                                        nodeNote.getLexicalvalue(),
                                        nodeNote.getNotetypecode(),
                                        nodeNote.getIdUser());
                                isUpdated = true;
                            }
                        }
                    }

                    if (isUpdated) {
                        conceptsModifies.add(acs.concept.getIdConcept() + " - " + conceptSource.getLabelsList().get(0).getLabel());
                    } else {
                        conceptsExists.add(acs.concept.getIdConcept());
                    }
                }

                fusionDone = true;
                PrimeFaces.current().executeScript("PF('bui').hide();");
                PrimeFaces.current().ajax().update("statistiques");
            }
        }

        importRdf4jHelper.addGroups(sourceSkos.getGroupList(), thesoSelected.getId());
        importRdf4jHelper.addLangsToThesaurus(connect.getPoolConnexion(), thesoSelected.getId());

    }

    private boolean isTraductionExist(NodeTermTraduction nodeTermTraduction, NodeConcept concept) {
        if (CollectionUtils.isEmpty(concept.getNodeTermTraductions()))
            return false;

        for(NodeTermTraduction node : concept.getNodeTermTraductions()){
            if (nodeTermTraduction.getLexicalValue() == node.getLexicalValue()
                    && (nodeTermTraduction.getLang() != null && node.getLang().equals(node.getLang()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isAlignementExist(NodeAlignment nodeAlignement, NodeConcept concept) {
        if (CollectionUtils.isEmpty(concept.getNodeEM()))
            return false;

        for(NodeAlignment node : concept.getNodeAlignments()){
            if (nodeAlignement.getAlignement_id_type() == node.getAlignement_id_type()
                    && (nodeAlignement.getName() != null && nodeAlignement.getName().equals(node.getName()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isDefinitionExist(NodeNote nodeNote, NodeConcept concept) {
        if (CollectionUtils.isEmpty(concept.getNodeEM()))
            return false;

        for(NodeNote node : concept.getNodeNotesTerm()){
            if ("definition".equalsIgnoreCase(node.getNotetypecode())
                    && node.getLexicalvalue().equals(nodeNote.getLexicalvalue())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSynonymeExist(NodeEM nodeEM, NodeConcept concept) {
        if (CollectionUtils.isEmpty(concept.getNodeEM()))
            return false;

        for(NodeEM node : concept.getNodeEM()){
            if (node.getLang().equals(nodeEM.getLang()) && node.getLexical_value().equals(nodeEM.getLexical_value())) {
                return true;
            }
        }
        return false;
    }

    public void importTheso(FileUploadEvent event) {

        try (InputStream is = event.getFile().getInputStream()) {
            ReadRdf4j readRdf4j = new ReadRdf4j(is, 0, false, connect.getWorkLanguage());
            sourceSkos = readRdf4j.getsKOSXmlDocument();
            total = sourceSkos.getConceptList().size();
            uri = sourceSkos.getTitle();
            loadDone = true;
            readRdf4j.clean();
            System.gc();
        } catch (Exception e) {

        }
        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
        PrimeFaces.current().ajax().update("statistiques");
    }

    public boolean isLoadDone() {
        return loadDone;
    }

    public String getUri() {
        return uri;
    }

    public double getTotal() {
        return total;
    }

    public List<String> getConceptsAjoutes() {
        return conceptsAjoutes;
    }

    public List<String> getConceptsModifies() {
        return conceptsModifies;
    }

    public boolean isFusionDone() {
        return fusionDone;
    }

    public List<String> getConceptsExists() {
        return conceptsExists;
    }

    public boolean isFusionBtnEnable() {
        return fusionBtnEnable;
    }

    public void initFusionResult() {
        fusionBtnEnable = false;
        fusionDone = false;
    }
}
