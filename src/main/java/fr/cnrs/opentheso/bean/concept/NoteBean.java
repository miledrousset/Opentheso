package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.FacetHelper;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.bean.facet.EditFacet;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.facets.NodeFacet;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;

import java.io.Serializable;
import java.util.ArrayList;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import org.apache.commons.text.StringEscapeUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "noteBean")
@SessionScoped
public class NoteBean implements Serializable {

    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private PropositionBean propositionBean;
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private EditFacet editFacet;
    @Autowired @Lazy private GroupView groupView;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private GroupHelper groupHelper;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private FacetHelper facetHelper;

    @Autowired
    private NoteHelper noteHelper;
    
    private String selectedLang;
    
    private ArrayList<NoteHelper.NoteType> noteTypes;
    private ArrayList<NodeLangTheso> nodeLangs;
    private String selectedTypeNote;
    private String noteValue;
    private NodeNote selectedNodeNote;
    boolean isFacetNote;
    boolean isConceptNote;
    boolean isGroupNote;
    private NodeFacet nodeFacet;
    private NodeGroup nodeGroup;
    private NodeNote noteToEdit;

    ArrayList<NodeNote> nodeNotesByLanguage;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (noteTypes != null) {
            noteTypes.clear();
            noteTypes = null;
        }
        if (nodeLangs != null) {
            nodeLangs.clear();
            nodeLangs = null;
        }
        selectedLang = null;
        selectedTypeNote = null;
        noteValue = null;
        selectedNodeNote = null;
        nodeGroup = null;
        nodeNotesByLanguage = null;
    }
    
    /**
     * permet d'initialiser l'édition des notes pour les facettes
     * @param nodeFacet 
     */
    public void resetForFacet(NodeFacet nodeFacet){
        resetGroup();
        setIsFacetNote();
        this.nodeFacet = nodeFacet;
    }
    
    /**
     * permet d'initialiser l'édition des notes pour les facettes
     * @param nodeGroup 
     */
    public void resetForGroup(NodeGroup nodeGroup){
        resetGroup();
        setIsGroupNote();
        this.nodeGroup = nodeGroup;
    }    
    private void resetGroup() {
        noteTypes = noteHelper.getNotesType(connect.getPoolConnexion());
        nodeLangs = selectedTheso.getNodeLangs();
        selectedLang = selectedTheso.getSelectedLang();
        noteValue = "";
        selectedTypeNote = null;
        isFacetNote = false;
        isConceptNote = false;
        isGroupNote = false;    
    }

    public void resetConcept(String idLang) {
        ArrayList<NoteHelper.NoteType> noteTypes1 = findNoteTypes();
        nodeNotesByLanguage = new ArrayList<>();
        setNotesByLang(noteTypes1, conceptBean.getNodeFullConcept().getIdentifier(), selectedTheso.getCurrentIdTheso(), idLang);
        nodeLangs = selectedTheso.getNodeLangs();
        selectedLang = idLang;
        noteValue = "";
        selectedTypeNote = null;
        isFacetNote = false;
        isConceptNote = true;
        isGroupNote = false;
    }

    private void setNotesByLang( ArrayList<NoteHelper.NoteType> noteType, String idConcept, String idTheso, String idLang) {
        boolean first = true;

        for(NoteHelper.NoteType type : noteType){
            NodeNote nodeNote = noteHelper.getNodeNote(connect.getPoolConnexion(), idConcept, idTheso, idLang, type.getCodeNote());
            if(nodeNote == null){
                nodeNote = new NodeNote();
                nodeNote.setIdConcept(idConcept);
                nodeNote.setLang(idLang);
                nodeNote.setNoteTypeCode(type.getCodeNote());
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
        resetConcept(idLang);
    }

    public void reset() {
        ArrayList<NoteHelper.NoteType> noteTypes1 = findNoteTypes();
        filterNotesByUsage(noteTypes1);
        nodeLangs = selectedTheso.getNodeLangs();
        selectedLang = selectedTheso.getSelectedLang();
        noteValue = "";
        selectedTypeNote = null;
        isFacetNote = false;
        isConceptNote = false;
        isGroupNote = false;    
    }
    
    private ArrayList<NoteHelper.NoteType> findNoteTypes(){
        ArrayList<NoteHelper.NoteType> noteTypes1 = noteHelper.getNotesType(connect.getPoolConnexion());
        return noteTypes1;
    }

    private void filterNotesByUsage(ArrayList<NoteHelper.NoteType> noteTypes1){
        noteTypes = new ArrayList<>();
        for (NoteHelper.NoteType noteType : noteTypes1) {
            switch (noteType.getCodeNote()) {
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
        noteToEdit = noteHelper.getNodeNote(connect.getPoolConnexion(),
                identifier,
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang(),
                selectedTypeNote);
    }

    /**
     * permet d'ajouter une note
     */
    public void addAndUpdateNote(int idUser) {

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
        if (!addNote(conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser)) {
            printErreur();
            return;
        }
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor

        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        noteValue = "";
        refreshNoteType();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
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
        if (!addNote(conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser)) {
            printErreur();
            return;
        }
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor

        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        
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
        ArrayList<NoteHelper.NoteType> noteTypes1 = findNoteTypes();
        filterNotesByUsage(noteTypes1);
    }
    
    
    
    //// notes pour les facettes 
    private void addFacetNote(int idUser){
        if (!addNote(nodeFacet.getIdFacet(), idUser)) {
            printErreur();
            return;
        }

        facetHelper.updateDateOfFacet(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeFacet.getIdFacet(), idUser);
        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        editFacet.initEditFacet(nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        noteValue = "";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
    }
    
    /// notes pour les collections
    private void addGroupNote(int idUser){
        if (!addNote(nodeGroup.getConceptGroup().getIdgroup(), idUser)) {
            printErreur();
            return;
        }
        groupHelper.updateModifiedDate(connect.getPoolConnexion(), nodeGroup.getConceptGroup().getIdgroup(),
                selectedTheso.getCurrentIdTheso());
        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentLang());
        noteValue = "";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);           
    }
    
    private boolean addNote(String identifier, int idUser) {
        if (noteHelper.isNoteExist(
                connect.getPoolConnexion(),
                identifier,
                selectedTheso.getCurrentIdTheso(),
                selectedLang,
                noteValue,
                selectedTypeNote)) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }

        return noteHelper.addNote(
                connect.getPoolConnexion(),
                identifier,
                selectedLang,
                selectedTheso.getCurrentIdTheso(),
                noteValue,
                selectedTypeNote,
                "",
                idUser);
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

        if (noteHelper.isNoteExist(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                selectedLang,
                noteValue,
                selectedTypeNote)) {

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
        
        if (!noteHelper.updateNote(connect.getPoolConnexion(),
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalValue(),
                nodeNote.getNoteTypeCode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        
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
        if (!noteHelper.updateNote(connect.getPoolConnexion(),
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeFacet.getIdFacet(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalValue(),
                nodeNote.getNoteTypeCode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        facetHelper.updateDateOfFacet(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeFacet.getIdFacet(), idUser);
        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        editFacet.initEditFacet(nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);    
        
    }
    
    // mise à jour des notes pour les facettes
    private void updateGroupNote(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        if (!noteHelper.updateNote(connect.getPoolConnexion(),
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeGroup.getConceptGroup().getIdgroup(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalValue(),
                nodeNote.getNoteTypeCode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        groupHelper.updateModifiedDate(connect.getPoolConnexion(),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        
        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);           
    }    

    public void deleteNote(NodeNote nodeNote, int idUser) {
        FacesMessage msg;

        if(isFacetNote){
            deleteThisNoteFacet(nodeNote, idUser);
            return;
        }
        if(isGroupNote){
            deleteThisNoteGroup(nodeNote, idUser);
            return;
        }

        if (!noteHelper.deleteThisNote(connect.getPoolConnexion(),
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique
                nodeNote.getIdConcept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNoteTypeCode(),
                nodeNote.getLexicalValue(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
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
        if (!noteHelper.deleteThisNote(connect.getPoolConnexion(),
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique
                nodeNote.getIdConcept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNoteTypeCode(),
                nodeNote.getLexicalValue(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        editFacet.initEditFacet(nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    private void deleteThisNoteGroup(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        if (!noteHelper.deleteThisNote(connect.getPoolConnexion(),
                nodeNote.getIdNote(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique
                nodeGroup.getConceptGroup().getIdgroup(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNoteTypeCode(),
                nodeNote.getLexicalValue(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentLang());

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
        switch (selectedTypeNote) {
            case "note":
                return propositionBean.getProposition().getNote();
            case "definition":
                return propositionBean.getProposition().getDefinition();
            case "scopeNote":
                return propositionBean.getProposition().getScopeNote();
            case "example":
                return propositionBean.getProposition().getExample();
            case "historyNote":
                return propositionBean.getProposition().getHistoryNote();
            case "editorialNote":
                return propositionBean.getProposition().getEditorialNote();
            default:
                return propositionBean.getProposition().getChangeNote();
        }
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
