package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.entites.NoteType;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.bean.facet.EditFacet;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.facets.NodeFacet;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.FacetService;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.NoteService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "noteBean")
public class NoteBean implements Serializable {

    private final PropositionBean propositionBean;
    private final ConceptView conceptBean;
    private final SelectedTheso selectedTheso;
    private final CurrentUser currentUser;
    private final EditFacet editFacet;
    private final GroupView groupView;
    private final GroupService groupService;
    private final ConceptDcTermRepository conceptDcTermRepository;
    private final ConceptService conceptService;
    private final NoteService noteService;
    private final FacetService facetService;

    private String selectedTypeNote, noteValue, selectedLang;
    boolean isFacetNote, isConceptNote, isGroupNote;
    private NodeFacet nodeFacet;
    private NodeGroup nodeGroup;
    private NodeNote selectedNodeNote, noteToEdit;
    private List<NoteType> noteTypes;
    private List<NodeLangTheso> nodeLangues;
    private List<NodeNote> nodeNotesByLanguage;

    /**
     * permet d'initialiser l'édition des notes pour les facettes
     * @param nodeFacet 
     */
    public void resetForFacet(NodeFacet nodeFacet){
        resetNotes(nodeFacet.getIdFacet(), nodeFacet.getLang());
        setIsFacetNote();
        this.nodeFacet = nodeFacet;
    }
    
    /**
     * permet d'initialiser l'édition des notes pour les facettes
     */
    public void resetForGroup(NodeGroup nodeGroup){
        resetNotes(nodeGroup.getConceptGroup().getIdGroup(), nodeGroup.getIdLang());
        setIsGroupNote();
    }

    /**
     * permet d'initialiser l'édition des notes pour les concepts
     */
    public void resetForConcept(String identifier, String idLang){
        resetNotes(identifier, idLang);
        setIsConceptNote();
    }

    private void resetGroup() {
        noteTypes = noteService.getNotesType();
        nodeLangues = selectedTheso.getNodeLangs();
        selectedLang = selectedTheso.getSelectedLang();
        noteValue = "";
        selectedTypeNote = null;
        isFacetNote = false;
        isConceptNote = false;
        isGroupNote = false;    
    }

    private void resetNotes(String identifier, String idLang) {
        List<NoteType> noteTypes1 = noteService.getNotesType();
        nodeNotesByLanguage = new ArrayList<>();
        setNotesByLang(noteTypes1, identifier, selectedTheso.getCurrentIdTheso(), idLang);
        nodeLangues = selectedTheso.getNodeLangs();
        selectedLang = idLang;
        noteValue = "";
        selectedTypeNote = null;
    }

    private void setNotesByLang(List<NoteType> noteType, String identifier, String idTheso, String idLang) {
        boolean first = true;

        for(NoteType type : noteType){
            NodeNote nodeNote = noteService.getNodeNote(identifier, idTheso, idLang, type.getCode());
            if(nodeNote == null){
                nodeNote = new NodeNote();
                nodeNote.setLang(idLang);
                nodeNote.setNoteTypeCode(type.getCode());
                nodeNote.setIdentifier(identifier);
            }
            if(first){
                selectedNodeNote = nodeNote;
                selectedTypeNote = nodeNote.getNoteTypeCode();
                first = false;
            }
            nodeNotesByLanguage.add(nodeNote);
        }
        actionChangeType();
    }

    public void  actionChangeType(){
        for(NodeNote nodeNote : nodeNotesByLanguage){
            if(selectedTypeNote.equals(nodeNote.getNoteTypeCode())){
                selectedNodeNote = nodeNote;
            }
        }
    }

    public void  actionChangeLang(String idLang){
        resetNotes(selectedNodeNote.getIdentifier(), idLang);
    }

    /**
     * permet d'ajouter une note
     */
    public void addAndUpdateNote(int idUser) {
        noteValue = "";
        if (selectedNodeNote == null || selectedNodeNote.getLexicalValue().isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }
        noteValue = StringEscapeUtils.unescapeXml(selectedNodeNote.getLexicalValue());
        if(noteService.isNoteExistInThatLang(selectedNodeNote.getIdentifier(), selectedTheso.getCurrentIdTheso(),
                selectedNodeNote.getLang(), selectedNodeNote.getNoteTypeCode())){
            updateNoteNewVersion(selectedNodeNote, idUser);
        } else {
            if (!addNoteNewVersion(selectedNodeNote, idUser)) {
                printErreur();
                return;
            }
            selectedNodeNote = noteService.getNodeNote(selectedNodeNote.getIdentifier(),selectedTheso.getCurrentIdTheso(), selectedNodeNote.getLang(), selectedNodeNote.getNoteTypeCode());
        }
        if(isConceptNote) {
            conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    conceptBean.getSelectedLang(), currentUser);

            conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), selectedNodeNote.getIdentifier(), idUser);

            conceptDcTermRepository.save(ConceptDcTerm.builder()
                    .name(DCMIResource.CONTRIBUTOR)
                    .value(currentUser.getNodeUser().getName())
                    .idConcept(selectedNodeNote.getIdentifier())
                    .idThesaurus(selectedTheso.getCurrentIdTheso())
                    .build());
        }
        if(isGroupNote) {
            groupView.getGroup(
                    selectedTheso.getCurrentIdTheso(),
                    groupView.getNodeGroup().getConceptGroup().getIdGroup(),
                    selectedTheso.getCurrentLang());
        }
        if(isFacetNote) {
            editFacet.initEditFacet(nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        }
    }

    /**
     * Nouvelle version pour permettre de gérer le multilingue
     */
    private boolean addNoteNewVersion(NodeNote nodeNote, int idUser) {
        if (noteService.isNoteExist(nodeNote.getIdentifier(), selectedTheso.getCurrentIdTheso(), nodeNote.getLang(),
                nodeNote.getLexicalValue(), nodeNote.getNoteTypeCode())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        noteService.addNote(nodeNote.getIdentifier(), nodeNote.getLang(), selectedTheso.getCurrentIdTheso(), nodeNote.getLexicalValue(),
                nodeNote.getNoteTypeCode(), nodeNote.getNoteSource(), idUser);
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        return true;
    }

    /**
     * Nouvelle méthode pour modifier la note pour gérer le multilingue
     */
    private void updateNoteNewVersion(NodeNote nodeNote, int idUser) {
        FacesMessage msg;
        if(nodeNote.getLexicalValue().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne peut pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }
        nodeNote.setLexicalValue(StringEscapeUtils.unescapeXml(nodeNote.getLexicalValue()));
        nodeNote.setNoteSource(fr.cnrs.opentheso.utils.StringUtils.clearValue(nodeNote.getNoteSource()));
        nodeNote.setLexicalValue(fr.cnrs.opentheso.utils.StringUtils.clearNoteFromP(nodeNote.getLexicalValue()));

        if (!noteService.updateNote(nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique
                nodeNote.getIdentifier(), nodeNote.getLang(), selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalValue(), nodeNote.getNoteSource(), nodeNote.getNoteTypeCode(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if(isGroupNote) {
            conceptDcTermRepository.save(ConceptDcTerm.builder()
                    .name(DCMIResource.CONTRIBUTOR)
                    .value(currentUser.getNodeUser().getName())
                    .idConcept(groupView.getNodeGroup().getConceptGroup().getIdGroup())
                    .idThesaurus(selectedTheso.getCurrentIdTheso())
                    .build());
        }
        if (isConceptNote){
            conceptDcTermRepository.save(ConceptDcTerm.builder()
                    .name(DCMIResource.CONTRIBUTOR)
                    .value(currentUser.getNodeUser().getName())
                    .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                    .idThesaurus(selectedTheso.getCurrentIdTheso())
                    .build());
        }
        if(isFacetNote){
            conceptDcTermRepository.save(ConceptDcTerm.builder()
                    .name(DCMIResource.CONTRIBUTOR)
                    .value(currentUser.getNodeUser().getName())
                    .idConcept(editFacet.getFacetSelected().getIdFacet())
                    .idThesaurus(selectedTheso.getCurrentIdTheso())
                    .build());
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }


    private void setIsFacetNote(){
        isFacetNote = true;
        isConceptNote = false;
        isGroupNote = false;
    }
    private void setIsConceptNote(){
        isFacetNote = false;
        isConceptNote = true;
        isGroupNote = false;
    }
    private void setIsGroupNote(){
        isFacetNote = false;
        isConceptNote = false;
        isGroupNote = true;
    }

    public void reset() {
        List<NoteType> noteTypes1 = noteService.getNotesType();
        filterNotesByUsage(noteTypes1);
        nodeLangues = selectedTheso.getNodeLangs();
        selectedLang = selectedTheso.getSelectedLang();
        noteValue = "";
        selectedTypeNote = null;
        isFacetNote = false;
        isConceptNote = false;
        isGroupNote = false;
    }

    private void filterNotesByUsage(List<NoteType> noteTypes1){
        noteTypes = new ArrayList<>();
        for (NoteType noteType : noteTypes1) {
            switch (noteType.getCode()) {
                case "note":
                    if(conceptBean.getNote() == null || conceptBean.getNote().getLexicalValue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;
                case "definition":
                    if(conceptBean.getDefinition() == null || conceptBean.getDefinition().getLexicalValue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;     
                case "scopeNote":
                    if(conceptBean.getScopeNote()== null || conceptBean.getScopeNote().getLexicalValue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;  
                case "example":          
                    if(conceptBean.getExample() == null || conceptBean.getExample().getLexicalValue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;                     
                case "historyNote":
                    if(conceptBean.getHistoryNote()== null || conceptBean.getHistoryNote().getLexicalValue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break; 
                case "editorialNote":
                    if(conceptBean.getEditorialNote()== null || conceptBean.getEditorialNote().getLexicalValue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;                      
                    
                case "changeNote":
                    if(conceptBean.getChangeNote() == null || conceptBean.getChangeNote().getLexicalValue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;                      
                default:
                    break;
            }
        }
    }

    public void initNoteProp(String noteType) {
        reset();
        setSelectedTypeNote(noteType);
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * A chaque sélection de note pour modification ou suppression, on initialise cette note
     */
    public void initNodeToEdit(String identifier,String selectedTypeNote1) {
        if (selectedTypeNote1 == null) {
            return;
        }
        this.selectedTypeNote = selectedTypeNote1;
        noteToEdit = noteService.getNodeNote(identifier, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang(), selectedTypeNote);
    }


    /**
     * permet d'ajouter une note 
     */
    public void addNewNote(int idUser) {

        if (noteValue == null || noteValue.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }


        noteValue = fr.cnrs.opentheso.utils.StringUtils.clearValue(noteValue);
        noteValue = StringEscapeUtils.unescapeXml(noteValue);

        if(isFacetNote){
            addFacetNote(idUser);
            return;
        }
        if(isGroupNote){
            addGroupNote(idUser);
            return;
        }        
            
            
        ///// notes pour les concepts    
        addNote(conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        noteValue = "";
        refreshNoteType();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    // permet de filtrer les notes manquantes par langue pour ajouter des nouvelles notes
    public void refreshNoteType(){
        filterNotesByUsage(noteService.getNotesType());
    }
    
    
    
    //// notes pour les facettes 
    private void addFacetNote(int idUser){

        addNote(nodeFacet.getIdFacet(), idUser);

        facetService.updateDateOfFacet(selectedTheso.getCurrentIdTheso(), nodeFacet.getIdFacet(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(nodeFacet.getIdFacet())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        editFacet.initEditFacet(nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        noteValue = "";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
    }
    
    /// notes pour les collections
    private void addGroupNote(int idUser){

        addNote(nodeGroup.getConceptGroup().getIdGroup(), idUser);
        groupService.updateModifiedDate(nodeGroup.getConceptGroup().getIdGroup(),
                selectedTheso.getCurrentIdTheso());

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(nodeGroup.getConceptGroup().getIdGroup())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeGroup.getConceptGroup().getIdGroup(), selectedTheso.getCurrentLang());
        noteValue = "";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);           
    }
    
    private void addNote(String identifier, int idUser) {

        noteService.isNoteExist(identifier, selectedTheso.getCurrentIdTheso(), selectedLang, noteValue, selectedTypeNote);
        noteService.addNote(identifier, selectedLang, selectedTheso.getCurrentIdTheso(), noteValue, selectedTypeNote, "", idUser);
    }


    public void addNewNoteProp() {

        if (noteValue == null || noteValue.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        noteValue = fr.cnrs.opentheso.utils.StringUtils.clearValue(noteValue);
        noteValue = StringEscapeUtils.unescapeXml(noteValue);

        if (noteService.isNoteExist(conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(),
                selectedLang, noteValue, selectedTypeNote)) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        switch (selectedTypeNote) {
            case "note":
                if (!checkExisting(propositionBean.getProposition().getNote())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "definition":
                if (!checkExisting(propositionBean.getProposition().getDefinition())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "scopeNote":
                if (!checkExisting(propositionBean.getProposition().getScopeNote())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "example":
                if (!checkExisting(propositionBean.getProposition().getExample())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "historyNote":
                if (!checkExisting(propositionBean.getProposition().getHistoryNote())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "editorialNote":
                if (!checkExisting(propositionBean.getProposition().getEditorialNote())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "changeNote":
                if (!checkExisting(propositionBean.getProposition().getChangeNote())) {
                    showErrorMessageNote();
                    return;
                }
                break;
        }

        NotePropBean notePropBean = new NotePropBean();
        notePropBean.setToAdd(true);
        notePropBean.setLang(selectedLang);
        notePropBean.setLexicalValue(noteValue);
        notePropBean.setIdConcept(conceptBean.getNodeConcept().getConcept().getIdConcept());
        notePropBean.setNoteTypeCode(selectedTypeNote);

        switch (selectedTypeNote) {
            case "note":
                propositionBean.getProposition().setNote(notePropBean);
                break;
            case "definition":
                propositionBean.getProposition().setDefinition(notePropBean);
                break;
            case "scopeNote":
                propositionBean.getProposition().setScopeNote(notePropBean);
                break;
            case "example":
                propositionBean.getProposition().setExample(notePropBean);
                break;
            case "historyNote":
                propositionBean.getProposition().setHistoryNote(notePropBean);
                break;
            case "editorialNote":
                propositionBean.getProposition().setEditorialNote(notePropBean);
                break;
            case "changeNote":
                propositionBean.getProposition().setChangeNote(notePropBean);
                break;
        }
        
        propositionBean.checkNotePropositionStatus();
    }

    private void showErrorMessageNote() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note "
                + noteValue + " ( " + selectedLang + ") note existe déjà !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private boolean checkExisting(NotePropBean note) {
        if(note == null) return true;
        if (selectedLang.equalsIgnoreCase(note.getLang())
                && noteValue.equalsIgnoreCase(note.getLexicalValue())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        return true;
    }
    
    public void updateNote(NodeNote nodeNote, int idUser) {
        FacesMessage msg;
        
        if(nodeNote.getLexicalValue().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne peut pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            conceptBean.getConcept(
                    selectedTheso.getCurrentIdTheso(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    conceptBean.getSelectedLang(), currentUser);
            return;            
        }
        nodeNote.setLexicalValue(fr.cnrs.opentheso.utils.StringUtils.clearValue(nodeNote.getLexicalValue()));
        nodeNote.setLexicalValue(StringEscapeUtils.unescapeXml(nodeNote.getLexicalValue()));        
        
        if(isFacetNote){
            updateFacetNote(nodeNote, idUser);
            return;
        }
        if(isGroupNote){
            updateGroupNote(nodeNote, idUser);
            return;
        }            
        
        if (!noteService.updateNote(
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalValue(),
                nodeNote.getNoteSource(),
                nodeNote.getNoteTypeCode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    // mise à jour des notes pour les facettes
    private void updateFacetNote(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        if (!noteService.updateNote(
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeNote.getIdentifier(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalValue(),
                nodeNote.getNoteSource(),
                nodeNote.getNoteTypeCode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        facetService.updateDateOfFacet(selectedTheso.getCurrentIdTheso(), nodeNote.getIdentifier(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(nodeNote.getIdentifier())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        editFacet.initEditFacet(nodeNote.getIdentifier(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);    
        
    }
    
    // mise à jour des notes pour les collections
    private void updateGroupNote(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        if (!noteService.updateNote(
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeNote.getIdentifier(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalValue(),
                nodeNote.getNoteSource(),
                nodeNote.getNoteTypeCode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        groupService.updateModifiedDate(nodeNote.getIdentifier(), selectedTheso.getCurrentIdTheso());

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(nodeNote.getIdentifier())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeNote.getIdentifier(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);           
    }    

    public void deleteNote(NodeNote nodeNote, int idUser) {
        FacesMessage msg;

        if(isFacetNote){
            deleteThisNoteFacet(nodeNote, idUser);
            resetNotes(nodeNote.getIdentifier(), nodeNote.getLang());
            return;
        }
        if(isGroupNote){
            deleteThisNoteGroup(nodeNote, idUser);
            resetNotes(nodeNote.getIdentifier(), nodeNote.getLang());
            return;
        }

        noteService.deleteThisNote(
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique
                nodeNote.getIdConcept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNoteTypeCode(),
                nodeNote.getLexicalValue(), idUser);
        resetNotes(nodeNote.getIdentifier(), nodeNote.getLang());

        if(isFacetNote){
            updateFacetNote(nodeNote, idUser);
            return;
        }
        if(isGroupNote){
            updateGroupNote(nodeNote, idUser);
            return;
        }

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

    }

    private void deleteThisNoteFacet(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        noteService.deleteThisNote(nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique
                nodeNote.getIdConcept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNoteTypeCode(),
                nodeNote.getLexicalValue(), idUser);
        editFacet.initEditFacet(nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    private void deleteThisNoteGroup(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        noteService.deleteThisNote(
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique
                nodeNote.getIdentifier(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNoteTypeCode(),
                nodeNote.getLexicalValue(), idUser);
        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeNote.getIdentifier(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }














    /******************************************/
    /********* Partie pour Firas **************/
    /************ à revoir ********************/
    /******************************************/

    public void updateNoteProp(NotePropBean notePropBean) {

        switch (selectedTypeNote) {
            case "note":
                if (propositionBean.getProposition().getNote().getLexicalValue()
                        .equals(notePropBean.getLexicalValue())) {

                    if (notePropBean.isToAdd()) {
                        propositionBean.getProposition().getNote().setLexicalValue(notePropBean.getLexicalValue());
                        break;
                    }

                    if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                            .getNote().getLexicalValue())) {
                        propositionBean.getProposition().getNote().setToRemove(false);
                        propositionBean.getProposition().getNote().setToUpdate(false);
                        propositionBean.getProposition().getNote().setToAdd(false);
                        propositionBean.getProposition().getNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (!propositionBean.getProposition().getNote().isToRemove()) {
                        propositionBean.getProposition().getNote().setToUpdate(true);
                        propositionBean.getProposition().getNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (propositionBean.getProposition().getNote().isToRemove()) {
                        propositionBean.getProposition().getNote().setToRemove(false);
                        propositionBean.getProposition().getNote().setToUpdate(true);
                        propositionBean.getProposition().getNote().setToAdd(false);
                        propositionBean.getProposition().getNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else {
                        propositionBean.getProposition().getNote().setLexicalValue(notePropBean.getLexicalValue());
                    }
                }
                break;
            case "definition":
                if (propositionBean.getProposition().getDefinition().getLexicalValue()
                        .equals(notePropBean.getLexicalValue())) {

                    if (notePropBean.isToAdd()) {
                        propositionBean.getProposition().getDefinition().setLexicalValue(notePropBean.getLexicalValue());
                        break;
                    }

                    if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                            .getDefinition().getLexicalValue())) {
                        propositionBean.getProposition().getDefinition().setToRemove(false);
                        propositionBean.getProposition().getDefinition().setToUpdate(false);
                        propositionBean.getProposition().getDefinition().setToAdd(false);
                        propositionBean.getProposition().getDefinition().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (propositionBean.getProposition().getDefinition().isToAdd()) {
                        propositionBean.getProposition().getDefinition().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (!propositionBean.getProposition().getDefinition().isToRemove()) {
                        propositionBean.getProposition().getDefinition().setToUpdate(true);
                        propositionBean.getProposition().getDefinition().setLexicalValue(notePropBean.getLexicalValue());
                    }
                }
                break;
            case "scopeNote":
                if (propositionBean.getProposition().getScopeNote().getLexicalValue()
                        .equals(notePropBean.getLexicalValue())) {

                    if (notePropBean.isToAdd()) {
                        propositionBean.getProposition().getScopeNote().setLexicalValue(notePropBean.getLexicalValue());
                        break;
                    }

                    if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                            .getScopeNote().getLexicalValue())) {
                        propositionBean.getProposition().getScopeNote().setToRemove(false);
                        propositionBean.getProposition().getScopeNote().setToUpdate(false);
                        propositionBean.getProposition().getScopeNote().setToAdd(false);
                        propositionBean.getProposition().getScopeNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (propositionBean.getProposition().getScopeNote().isToAdd()) {
                        propositionBean.getProposition().getScopeNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (!propositionBean.getProposition().getScopeNote().isToRemove()) {
                        propositionBean.getProposition().getScopeNote().setToUpdate(true);
                        propositionBean.getProposition().getScopeNote().setLexicalValue(notePropBean.getLexicalValue());
                    }
                }
                break;
            case "example":
                if (propositionBean.getProposition().getExample().getLexicalValue()
                        .equals(notePropBean.getLexicalValue())) {

                    if (notePropBean.isToAdd()) {
                        propositionBean.getProposition().getExample().setLexicalValue(notePropBean.getLexicalValue());
                        break;
                    }

                    if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                            .getExample().getLexicalValue())) {
                        propositionBean.getProposition().getExample().setToRemove(false);
                        propositionBean.getProposition().getExample().setToUpdate(false);
                        propositionBean.getProposition().getExample().setToAdd(false);
                        propositionBean.getProposition().getExample().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (propositionBean.getProposition().getExample().isToAdd()) {
                        propositionBean.getProposition().getExample().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (!propositionBean.getProposition().getExample().isToRemove()) {
                        propositionBean.getProposition().getExample().setToUpdate(true);
                        propositionBean.getProposition().getExample().setLexicalValue(notePropBean.getLexicalValue());
                    }
                }
                break;
            case "historyNote":
                if (propositionBean.getProposition().getHistoryNote().getLexicalValue()
                        .equals(notePropBean.getLexicalValue())) {

                    if (notePropBean.isToAdd()) {
                        propositionBean.getProposition().getHistoryNote().setLexicalValue(notePropBean.getLexicalValue());
                        break;
                    }

                    if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                            .getHistoryNote().getLexicalValue())) {
                        propositionBean.getProposition().getHistoryNote().setToRemove(false);
                        propositionBean.getProposition().getHistoryNote().setToUpdate(false);
                        propositionBean.getProposition().getHistoryNote().setToAdd(false);
                        propositionBean.getProposition().getHistoryNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (propositionBean.getProposition().getHistoryNote().isToAdd()) {
                        propositionBean.getProposition().getHistoryNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (!propositionBean.getProposition().getHistoryNote().isToRemove()) {
                        propositionBean.getProposition().getHistoryNote().setToUpdate(true);
                        propositionBean.getProposition().getHistoryNote().setLexicalValue(notePropBean.getLexicalValue());
                    }
                }
                break;
            case "editorialNote":
                if (propositionBean.getProposition().getEditorialNote().getLexicalValue()
                        .equals(notePropBean.getLexicalValue())) {

                    if (notePropBean.isToAdd()) {
                        propositionBean.getProposition().getEditorialNote().setLexicalValue(notePropBean.getLexicalValue());
                        break;
                    }

                    if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                            .getEditorialNote().getLexicalValue())) {
                        propositionBean.getProposition().getEditorialNote().setToRemove(false);
                        propositionBean.getProposition().getEditorialNote().setToUpdate(false);
                        propositionBean.getProposition().getEditorialNote().setToAdd(false);
                        propositionBean.getProposition().getEditorialNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (propositionBean.getProposition().getEditorialNote().isToAdd()) {
                        propositionBean.getProposition().getEditorialNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (!propositionBean.getProposition().getEditorialNote().isToRemove()) {
                        propositionBean.getProposition().getEditorialNote().setToUpdate(true);
                        propositionBean.getProposition().getEditorialNote().setLexicalValue(notePropBean.getLexicalValue());
                    }
                }
                break;
            case "changeNote":
                if (propositionBean.getProposition().getChangeNote().getLexicalValue()
                        .equals(notePropBean.getLexicalValue())) {

                    if (notePropBean.isToAdd()) {
                        propositionBean.getProposition().getChangeNote().setLexicalValue(notePropBean.getLexicalValue());
                        break;
                    }

                    if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                            .getChangeNote().getLexicalValue())) {
                        propositionBean.getProposition().getChangeNote().setToRemove(false);
                        propositionBean.getProposition().getChangeNote().setToUpdate(false);
                        propositionBean.getProposition().getChangeNote().setToAdd(false);
                        propositionBean.getProposition().getChangeNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (propositionBean.getProposition().getChangeNote().isToAdd()) {
                        propositionBean.getProposition().getChangeNote().setLexicalValue(notePropBean.getLexicalValue());
                    } else if (!propositionBean.getProposition().getChangeNote().isToRemove()) {
                        propositionBean.getProposition().getChangeNote().setToUpdate(true);
                        propositionBean.getProposition().getChangeNote().setLexicalValue(notePropBean.getLexicalValue());
                    }
                }
                break;
        }

        propositionBean.checkNotePropositionStatus();
    }

    public NotePropBean nodePropToEdit() {
        if (selectedTypeNote == null) {
            return new NotePropBean();
        }
        if(propositionBean.getProposition() == null) return new NotePropBean();
        return switch (selectedTypeNote) {
            case "note" -> propositionBean.getProposition().getNote();
            case "definition" -> propositionBean.getProposition().getDefinition();
            case "scopeNote" -> propositionBean.getProposition().getScopeNote();
            case "example" -> propositionBean.getProposition().getExample();
            case "historyNote" -> propositionBean.getProposition().getHistoryNote();
            case "editorialNote" -> propositionBean.getProposition().getEditorialNote();
            default -> propositionBean.getProposition().getChangeNote();
        };
    }

    public void deleteNoteProp(NotePropBean notePropBean) {

        switch (selectedTypeNote) {
            case "note":
                    if (propositionBean.getProposition().getNote().getLexicalValue()
                            .equals(notePropBean.getLexicalValue())) {
                        if (propositionBean.getProposition().getNote().isToAdd()) {
                            propositionBean.getProposition().setNote(null);
                        } else if (propositionBean.getProposition().getNote().isToUpdate()) {
                            propositionBean.getProposition().getNote().setToUpdate(false);
                            propositionBean.getProposition().getNote().setToRemove(true);
                            propositionBean.getProposition().getNote()
                                    .setLexicalValue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getNote().setToRemove(
                                    !propositionBean.getProposition().getNote().isToRemove());
                        }
                    }
                break;
            case "definition":
                    if (propositionBean.getProposition().getDefinition().getLexicalValue()
                            .equals(notePropBean.getLexicalValue())) {
                        if (propositionBean.getProposition().getDefinition().isToAdd()) {
                            propositionBean.getProposition().setDefinition(null);
                        } else if (propositionBean.getProposition().getDefinition().isToUpdate()) {
                            propositionBean.getProposition().getDefinition().setToUpdate(false);
                            propositionBean.getProposition().getDefinition().setToRemove(true);
                            propositionBean.getProposition().getDefinition()
                                    .setLexicalValue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getDefinition().setToRemove(
                                    !propositionBean.getProposition().getDefinition().isToRemove());
                        }
                    }
                
                break;
            case "scopeNote":
                    if (propositionBean.getProposition().getScopeNote().getLexicalValue()
                            .equals(notePropBean.getLexicalValue())) {
                        if (propositionBean.getProposition().getScopeNote().isToAdd()) {
                            propositionBean.getProposition().setScopeNote(null);
                        } else if (propositionBean.getProposition().getScopeNote().isToUpdate()) {
                            propositionBean.getProposition().getScopeNote().setToUpdate(false);
                            propositionBean.getProposition().getScopeNote().setToRemove(true);
                            propositionBean.getProposition().getScopeNote()
                                    .setLexicalValue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getScopeNote().setToRemove(
                                    !propositionBean.getProposition().getScopeNote().isToRemove());
                        }
                    }
                
                break;
            case "example":
                    if (propositionBean.getProposition().getExample().getLexicalValue()
                            .equals(notePropBean.getLexicalValue())) {
                        if (propositionBean.getProposition().getExample().isToAdd()) {
                            propositionBean.getProposition().setExample(null);
                        } else if (propositionBean.getProposition().getExample().isToUpdate()) {
                            propositionBean.getProposition().getExample().setToUpdate(false);
                            propositionBean.getProposition().getExample().setToRemove(true);
                            propositionBean.getProposition().getExample()
                                    .setLexicalValue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getExample().setToRemove(
                                    !propositionBean.getProposition().getExample().isToRemove());
                        }
                    }
                break;
            case "historyNote":
                    if (propositionBean.getProposition().getHistoryNote().getLexicalValue()
                            .equals(notePropBean.getLexicalValue())) {
                        if (propositionBean.getProposition().getHistoryNote().isToAdd()) {
                            propositionBean.getProposition().setHistoryNote(null);
                        } else if (propositionBean.getProposition().getHistoryNote().isToUpdate()) {
                            propositionBean.getProposition().getHistoryNote().setToUpdate(false);
                            propositionBean.getProposition().getHistoryNote().setToRemove(true);
                            propositionBean.getProposition().getHistoryNote()
                                    .setLexicalValue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getHistoryNote().setToRemove(
                                    !propositionBean.getProposition().getHistoryNote().isToRemove());
                        }
                    }
                break;
            case "editorialNote":
                    if (propositionBean.getProposition().getEditorialNote().getLexicalValue()
                            .equals(notePropBean.getLexicalValue())) {
                        if (propositionBean.getProposition().getEditorialNote().isToAdd()) {
                            propositionBean.getProposition().setEditorialNote(null);
                        } else if (propositionBean.getProposition().getEditorialNote().isToUpdate()) {
                            propositionBean.getProposition().getEditorialNote().setToUpdate(false);
                            propositionBean.getProposition().getEditorialNote().setToRemove(true);
                            propositionBean.getProposition().getEditorialNote()
                                    .setLexicalValue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getEditorialNote().setToRemove(
                                    !propositionBean.getProposition().getEditorialNote().isToRemove());
                        }
                    }
                
                break;
            case "changeNote":
                    if (propositionBean.getProposition().getChangeNote().getLexicalValue()
                            .equals(notePropBean.getLexicalValue())) {
                        if (propositionBean.getProposition().getChangeNote().isToAdd()) {
                            propositionBean.getProposition().setChangeNote(null);
                        } else if (propositionBean.getProposition().getChangeNote().isToUpdate()) {
                            propositionBean.getProposition().getChangeNote().setToUpdate(false);
                            propositionBean.getProposition().getChangeNote().setToRemove(true);
                            propositionBean.getProposition().getChangeNote().setLexicalValue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getChangeNote().setToRemove(
                                    !propositionBean.getProposition().getChangeNote().isToRemove());
                        }
                    }
                
                break;
        }
        
        propositionBean.checkNotePropositionStatus();
    }

    private void printErreur() {
        FacesMessage msg;
        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de création de note !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}
