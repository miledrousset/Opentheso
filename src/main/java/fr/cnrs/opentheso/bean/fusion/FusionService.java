package fr.cnrs.opentheso.bean.fusion;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.HierarchicalRelationship;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.*;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTerm;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.core.imports.rdf4j.ReadRdf4j;
import fr.cnrs.opentheso.core.imports.rdf4j.helper.AddConceptsStruct;
import fr.cnrs.opentheso.core.imports.rdf4j.helper.ImportRdf4jHelper;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
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
    private boolean loadDone, fusionDone;
    private String uri;
    private double total;
    private List<String> conceptsAjoutes, conceptsModifies;

    public void lancerFussion(NodeIdValue thesoSelected) {

        fusionDone = false;
        conceptsModifies = new ArrayList<>();
        conceptsAjoutes = new ArrayList<>();

        ConceptHelper conceptHelper = new ConceptHelper();

        for (SKOSResource conceptSource : sourceSkos.getConceptList()) {
            if (!StringUtils.isEmpty(conceptSource.getIdentifier())) {

                ImportRdf4jHelper importRdf4jHelper = new ImportRdf4jHelper();
                importRdf4jHelper.setRdf4jThesaurus(sourceSkos);
                AddConceptsStruct acs = new AddConceptsStruct();
                importRdf4jHelper.initAddConceptsStruct(acs, conceptSource, thesoSelected.getId(), false);
                importRdf4jHelper.addRelation(acs, thesoSelected.getId());

                NodeConcept conceptFound = conceptHelper.getConcept(connect.getPoolConnexion(),
                        conceptSource.getIdentifier(),
                        thesoSelected.getId(),
                        selectedTheso.getCurrentLang());

                if (conceptFound == null) {
                    addConceptToBdd(acs, thesoSelected.getId());
                    conceptsAjoutes.add(acs.concept.getIdConcept() + " - " + acs.term.getLexical_value());
                } else {

                    //alignment : existe -> on ne fait rien; ,n'existe pas -> on l'ajout
                    if (!CollectionUtils.isEmpty(acs.nodeAlignments)) {
                        for (NodeAlignment nodeAlignment : acs.nodeAlignments) {
                            if (isAlignementExist(nodeAlignment, conceptFound)) {
                                new AlignmentHelper().addNewAlignment(connect.getPoolConnexion(),
                                        nodeAlignment.getId_author(),
                                        nodeAlignment.getConcept_target(),
                                        nodeAlignment.getThesaurus_target(),
                                        nodeAlignment.getUri_target(),// URI
                                        nodeAlignment.getAlignement_id_type(),
                                        conceptSource.getIdentifier(),
                                        conceptFound.getConcept().getIdThesaurus(),
                                        nodeAlignment.getId_alignement());
                            }
                        }
                    }

                    // Synonymes
                    if (!CollectionUtils.isEmpty(acs.nodeEMList)) {
                        for (NodeEM nodeEM : acs.nodeEMList) {
                            if (!isSynonymeExist(nodeEM, conceptFound)) {
                                Term term = new Term();
                                term.setId_concept(conceptSource.getIdentifier());
                                term.setLexical_value(nodeEM.getLexical_value());
                                term.setLang(nodeEM.getLang());
                                term.setId_thesaurus(conceptFound.getConcept().getIdThesaurus());
                                term.setSource(nodeEM.getSource());
                                term.setStatus(nodeEM.getStatus());
                                term.setHidden(nodeEM.isHiden());
                                new TermHelper().addNonPreferredTerm(connect.getPoolConnexion(), term,
                                        currentUser.getNodeUser().getIdUser());
                            }
                        }
                    }

                    //Definition
                    if (!CollectionUtils.isEmpty(acs.nodeNotes)) {
                        for (NodeNote nodeNote : acs.nodeNotes) {
                            if (!isDefinitionExist(nodeNote, conceptFound)) {
                                new NoteHelper().addConceptNote(connect.getPoolConnexion(),
                                        conceptFound.getConcept().getIdThesaurus(),
                                        nodeNote.getLang(),
                                        conceptFound.getConcept().getIdThesaurus(),
                                        nodeNote.getLexicalvalue(),
                                        nodeNote.getNotetypecode(),
                                        nodeNote.getIdUser());
                            }
                        }
                    }

                    conceptsModifies.add(acs.concept.getIdConcept() + " - " + acs.term.getLexical_value());
                }

                fusionDone = true;
                PrimeFaces.current().executeScript("PF('bui').hide();");
                PrimeFaces.current().ajax().update("statistiques");
            }
        }

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

    private void addConceptToBdd(AddConceptsStruct acs, String idTheso) {

        if (!acs.conceptHelper.insertConceptInTable(connect.getPoolConnexion(), acs.concept, currentUser.getNodeUser().getIdUser())) {
            System.out.println("Erreur sur le Concept = " + acs.concept.getIdConcept());
        }

        acs.termHelper.insertTerm(connect.getPoolConnexion(), acs.nodeTerm, currentUser.getNodeUser().getIdUser());

        RelationsHelper relationsHelper = new RelationsHelper();

        for (HierarchicalRelationship hierarchicalRelationship : acs.hierarchicalRelationships) {
            switch (hierarchicalRelationship.getRole()) {
                case "NT":
                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2());

                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "BT",
                            hierarchicalRelationship.getIdConcept1());
                    break;
                case "BT":
                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2());

                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "NT",
                            hierarchicalRelationship.getIdConcept1());
                    break;
                case "RT":
                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2());

                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "RT",
                            hierarchicalRelationship.getIdConcept1());
                    break;
                case "NTP":
                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2());

                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "BTP",
                            hierarchicalRelationship.getIdConcept1());
                    break;
                case "NTG":
                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2());

                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "BTG",
                            hierarchicalRelationship.getIdConcept1());
                    break;
                case "NTI":
                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept1(),
                            hierarchicalRelationship.getIdThesaurus(),
                            hierarchicalRelationship.getRole(),
                            hierarchicalRelationship.getIdConcept2());

                    relationsHelper.insertHierarchicalRelation(connect.getPoolConnexion(),
                            hierarchicalRelationship.getIdConcept2(),
                            hierarchicalRelationship.getIdThesaurus(),
                            "BTI",
                            hierarchicalRelationship.getIdConcept1());
                    break;
            }
        }

        for (NodeNote nodeNoteList1 : acs.nodeNotes) {

            if (nodeNoteList1.getNotetypecode().equals("customnote") || nodeNoteList1.getNotetypecode().equals("scopeNote")
                    || nodeNoteList1.getNotetypecode().equals("note")) {
                acs.noteHelper.addConceptNote(connect.getPoolConnexion(), acs.concept.getIdConcept(), nodeNoteList1.getLang(),
                        idTheso, nodeNoteList1.getLexicalvalue(), nodeNoteList1.getNotetypecode(), currentUser.getNodeUser().getIdUser());
            }

            if (nodeNoteList1.getNotetypecode().equals("definition")
                    || nodeNoteList1.getNotetypecode().equals("historyNote")
                    || nodeNoteList1.getNotetypecode().equals("editorialNote")
                    || nodeNoteList1.getNotetypecode().equals("changeNote")
                    || nodeNoteList1.getNotetypecode().equals("example")) {
                acs.noteHelper.addTermNote(connect.getPoolConnexion(), acs.nodeTerm.getIdTerm(), nodeNoteList1.getLang(),
                        idTheso, nodeNoteList1.getLexicalvalue(), nodeNoteList1.getNotetypecode(), currentUser.getNodeUser().getIdUser());
            }
        }

        for (NodeAlignment nodeAlignment : acs.nodeAlignments) {
            acs.alignmentHelper.addNewAlignment(connect.getPoolConnexion(), nodeAlignment);
        }

        for (NodeEM nodeEMList1 : acs.nodeEMList) {
            acs.term.setId_concept(acs.concept.getIdConcept());
            acs.term.setId_term(acs.nodeTerm.getIdTerm());
            acs.term.setLexical_value(nodeEMList1.getLexical_value());
            acs.term.setLang(nodeEMList1.getLang());
            acs.term.setId_thesaurus(idTheso);//thesaurus.getId_thesaurus());
            acs.term.setSource(nodeEMList1.getSource());
            acs.term.setStatus(nodeEMList1.getStatus());
            acs.term.setHidden(nodeEMList1.isHiden());
            acs.termHelper.addNonPreferredTerm(connect.getPoolConnexion(), acs.term, currentUser.getNodeUser().getIdUser());
        }

        if (acs.nodeGps.getLatitude() != null && acs.nodeGps.getLongitude() != null) {
            acs.gpsHelper.insertCoordonees(connect.getPoolConnexion(), acs.concept.getIdConcept(), idTheso,
                    acs.nodeGps.getLatitude(), acs.nodeGps.getLongitude());
        }

        if(acs.isTopConcept) {
            acs.conceptHelper.setTopConcept(connect.getPoolConnexion(), acs.concept.getIdConcept(), idTheso);
        }

        // ajout des images externes URI
        for (String imageUri : acs.nodeImages) {
            acs.imagesHelper.addExternalImage(connect.getPoolConnexion(), acs.concept.getIdConcept(), idTheso, "", "", imageUri, currentUser.getNodeUser().getIdUser());
        }

        DeprecateHelper deprecateHelper = new DeprecateHelper();
        if(acs.conceptStatus.equalsIgnoreCase("dep")) {
            deprecateHelper.deprecateConcept(connect.getPoolConnexion(), acs.concept.getIdConcept(), idTheso, currentUser.getNodeUser().getIdUser());
        }
        for (NodeIdValue nodeIdValue : acs.replacedBy) {
            deprecateHelper.addReplacedBy(connect.getPoolConnexion(), acs.concept.getIdConcept(), idTheso, nodeIdValue.getId(), currentUser.getNodeUser().getIdUser());
        }

        // initialisation des variables
        acs.conceptStatus = "";
        acs.isTopConcept = false;
        acs.nodeGps = new NodeGps();
        acs.concept = new Concept();
        acs.nodeTerm = new NodeTerm();

        if(acs.nodeTermTraductionList != null) acs.nodeTermTraductionList.clear();
        if(acs.nodeEMList != null) acs.nodeEMList.clear();
        if(acs.nodeNotes != null) acs.nodeNotes.clear();
        if(acs.nodeAlignments != null) acs.nodeAlignments.clear();
        if(acs.hierarchicalRelationships != null) acs.hierarchicalRelationships.clear();
        if(acs.idGrps != null) acs.idGrps.clear();
        if(acs.nodeImages != null) acs.nodeImages.clear();
        if(acs.replacedBy != null) acs.replacedBy.clear();
        if(acs.replaces != null) acs.replaces.clear();
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
}
