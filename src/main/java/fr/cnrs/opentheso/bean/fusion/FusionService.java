package fr.cnrs.opentheso.bean.fusion;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.models.imports.AddConceptsStruct;
import fr.cnrs.opentheso.services.AlignmentService;
import fr.cnrs.opentheso.services.NonPreferredTermService;
import fr.cnrs.opentheso.services.NoteService;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.TermService;
import fr.cnrs.opentheso.services.imports.rdf4j.ImportRdf4jHelper;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@Named
@Service
public class FusionService implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    @Autowired @Lazy
    private CurrentUser currentUser;

    @Autowired
    private ImportRdf4jHelper importRdf4jHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private NoteHelper noteHelper;

    @Autowired
    private PreferenceService preferenceService;

    @Autowired
    private TermService termService;

    @Autowired
    private AlignmentService alignmentService;

    @Autowired
    private NonPreferredTermService nonPreferredTermService;


    private SKOSXmlDocument sourceSkos;
    private boolean loadDone, fusionDone, fusionBtnEnable;
    private String uri;
    private double total;
    private List<String> conceptsAjoutes, conceptsModifies, conceptsExists;
    @Autowired
    private NoteService noteService;

    public void lancerFussion(NodeIdValue thesoSelected) {

        fusionDone = false;
        fusionBtnEnable = true;
        if (conceptsModifies == null) {
            conceptsModifies = new ArrayList<>();
        } else {
            conceptsModifies.clear();
        }
        if (conceptsAjoutes == null) {
            conceptsAjoutes = new ArrayList<>();
        } else {
            conceptsAjoutes.clear();
        }
        if (conceptsExists == null) {
            conceptsExists = new ArrayList<>();
        } else {
            conceptsExists.clear();
        }

        List<NodeEM> nodeEMsLocal;

        String workLang = preferenceService.getWorkLanguageOfThesaurus(thesoSelected.getId());

        for (SKOSResource conceptSource : sourceSkos.getConceptList()) {
            if (!StringUtils.isEmpty(conceptSource.getIdentifier())) {

                /// prépration d'un objet au format Concept  
                importRdf4jHelper.setRdf4jThesaurus(sourceSkos);
                AddConceptsStruct acs = new AddConceptsStruct();
                importRdf4jHelper.initAddConceptsStruct(acs, conceptSource, thesoSelected.getId(), false);
                importRdf4jHelper.addRelation(acs, thesoSelected.getId());

                // récupération du concept Local
                NodeConcept conceptFound = conceptHelper.getConcept(
                        conceptSource.getIdentifier(),
                        thesoSelected.getId(),
                        workLang, -1, -1);

                if (conceptFound == null && !conceptSource.getLabelsList().isEmpty()) {
                    importRdf4jHelper.addConceptToBdd(acs, thesoSelected.getId(), false);
                    conceptsAjoutes.add(acs.concept.getIdConcept() + " - " + conceptSource.getLabelsList().get(0).getLabel());
                } else {
                    if (conceptFound == null) {
                        return;
                    }
                    boolean isUpdated = false;

                    // Traduction OK validée #MR
                    //alignment
                    if (!CollectionUtils.isEmpty(acs.nodeAlignments)) {
                        for (NodeAlignment nodeAlignment : acs.nodeAlignments) {
                            if (!isAlignementExist(nodeAlignment, conceptFound.getNodeAlignments())) {
                                alignmentService.addNewAlignment(
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

                    // Traduction OK validée #MR
                    // Synonymes : NonPreferredTerms
                    if (!CollectionUtils.isEmpty(acs.nodeEMList)) {
                        nodeEMsLocal = nonPreferredTermService.getAllNonPreferredTerms(conceptSource.getIdentifier(), conceptFound.getConcept().getIdThesaurus());
                        for (NodeEM nodeEM : acs.nodeEMList) {
                            if (!isSynonymeExist(nodeEM, nodeEMsLocal)) {
                                Term term = new Term();
                                term.setIdTerm(conceptFound.getTerm().getIdTerm());
                                term.setIdConcept(conceptFound.getConcept().getIdConcept());
                                term.setLexicalValue(nodeEM.getLexicalValue());
                                term.setLang(nodeEM.getLang());
                                term.setIdThesaurus(conceptFound.getConcept().getIdThesaurus());
                                term.setSource(nodeEM.getSource());
                                term.setStatus(nodeEM.getStatus());
                                term.setHidden(nodeEM.isHiden());
                                nonPreferredTermService.addNonPreferredTerm(term, currentUser.getNodeUser().getIdUser());
                                isUpdated = true;
                            }
                        }
                    }

                    // Traduction OK validée #MR
                    if (!CollectionUtils.isEmpty(acs.nodeTermTraductionList)) {
                        for (NodeTermTraduction nodeTermTraduction : acs.nodeTermTraductionList) {
                            if (!isTraductionExist(nodeTermTraduction, conceptFound)) {
                                Term term = new Term();
                                term.setIdTerm(conceptFound.getTerm().getIdTerm());
                                term.setIdConcept(conceptFound.getConcept().getIdConcept());
                                term.setLexicalValue(nodeTermTraduction.getLexicalValue());
                                term.setLang(nodeTermTraduction.getLang());
                                term.setIdThesaurus(conceptFound.getConcept().getIdThesaurus());
                                if (termService.isTermExistInLangAndThesaurus(term.getIdTerm(), term.getIdThesaurus(), nodeTermTraduction.getLang())) {
                                    termService.updateTermTraduction(term, currentUser.getNodeUser().getIdUser());
                                } else {
                                    var termToSave = Term.builder()
                                            .lexicalValue(nodeTermTraduction.getLexicalValue())
                                            .idTerm(acs.nodeTerm.getIdTerm())
                                            .lang(nodeTermTraduction.getLang())
                                            .idThesaurus(conceptFound.getConcept().getIdThesaurus())
                                            .source("fusion")
                                            .status("")
                                            .build();
                                    termService.addTermTraduction(termToSave, currentUser.getNodeUser().getIdUser());
                                }
                                isUpdated = true;
                            }
                        }
                    }

                    // OK validé par #MR
                    //Definition
                    if (!CollectionUtils.isEmpty(acs.nodeNotes)) {
                        for (NodeNote nodeNote : acs.nodeNotes) {
                            /// détecter le type de note avant 
                            if (nodeNote.getNoteTypeCode().equalsIgnoreCase("definition")) {
                                if (!noteHelper.isNoteExist(acs.concept.getIdConcept(), conceptFound.getConcept().getIdThesaurus(),
                                        nodeNote.getLang(), nodeNote.getLexicalValue(), "definition")) {

                                    noteService.addNote(acs.concept.getIdConcept(), nodeNote.getLang(), conceptFound.getConcept().getIdThesaurus(),
                                            nodeNote.getLexicalValue(), nodeNote.getNoteTypeCode(), "", nodeNote.getIdUser());
                                    isUpdated = true;
                                }
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
        importRdf4jHelper.addLangsToThesaurus(thesoSelected.getId());

    }

    
    /**
     * pour comparer la traduction locale à la traduction importée
     */
    private boolean isTraductionExist(NodeTermTraduction nodeTermTraductionImport, NodeConcept nodeConceptLocal) {
        if (nodeConceptLocal == null)
            return false;
        
        // comparaison avec le prefLabel 
        if(nodeConceptLocal.getTerm() != null) {
            if (nodeTermTraductionImport.getLexicalValue().equalsIgnoreCase(nodeConceptLocal.getTerm().getLexicalValue())
                && (nodeTermTraductionImport.getLang().equalsIgnoreCase(nodeConceptLocal.getTerm().getLang()))) {
                return true;
            }
        }
        
        // comparaison aux traductions
        for(NodeTermTraduction existingTranslation  : nodeConceptLocal.getNodeTermTraductions()){
            if (nodeTermTraductionImport.getLexicalValue().equalsIgnoreCase(existingTranslation.getLexicalValue())
                && (nodeTermTraductionImport.getLang().equalsIgnoreCase(existingTranslation.getLang()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * permet de vérifier si l'alignement existe ou non
     */
    private boolean isAlignementExist(NodeAlignment nodeAlignementImport, List<NodeAlignment> nodeAlignmentsLocal) {
        if (nodeAlignmentsLocal == null || nodeAlignmentsLocal.isEmpty())
            return false;        

        for(NodeAlignment nodeAlignmentsLocal1  : nodeAlignmentsLocal){
            if (nodeAlignementImport.getAlignement_id_type() == nodeAlignmentsLocal1.getAlignement_id_type()
                && (nodeAlignementImport.getUri_target().equalsIgnoreCase(nodeAlignmentsLocal1.getUri_target())) ) {
                return true;
            }
        }         
        return false;
    }

    private boolean isSynonymeExist(NodeEM nodeEMimport, List<NodeEM> nodeEMLocal) {
        if (nodeEMLocal == null || nodeEMLocal.isEmpty())
            return false;

        for(NodeEM existingNodeEM  : nodeEMLocal){
            if (nodeEMimport.getLexicalValue().equalsIgnoreCase(existingNodeEM.getLexicalValue())
                && (nodeEMimport.getLang().equalsIgnoreCase(existingNodeEM.getLang()))) {
                return true;
            }
        }
        return false;
    }

    public void initFusionResult() {
        fusionBtnEnable = false;
        fusionDone = false;
    }
}
