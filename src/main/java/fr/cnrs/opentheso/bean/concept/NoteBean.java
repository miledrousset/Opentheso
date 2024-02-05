package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeFacet;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.facet.EditFacet;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.text.StringEscapeUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "noteBean")
@SessionScoped

public class NoteBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private PropositionBean propositionBean;
    @Inject
    private ConceptView conceptBean;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject private CurrentUser currentUser;    

    @Inject private EditFacet editFacet;
    @Inject private GroupView groupView;
    
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
    }
    
    /**
     * permet d'initialiser l'édition des notes pour les facettes
     * @param nodeFacet 
     */
    public void resetForFacet(NodeFacet nodeFacet){
        reset();
        setIsFacetNote();
        this.nodeFacet = nodeFacet;
    }
    
    /**
     * permet d'initialiser l'édition des notes pour les facettes
     * @param nodeGroup 
     */
    public void resetForGroup(NodeGroup nodeGroup){
        reset();
        setIsGroupNote();
        this.nodeGroup = nodeGroup;
    }    
    
    
    public void reset() {
        noteTypes = new NoteHelper().getNotesType(connect.getPoolConnexion());
        nodeLangs = selectedTheso.getNodeLangs();
        selectedLang = selectedTheso.getSelectedLang();
        noteValue = "";
        selectedTypeNote = null;
        isFacetNote = false;
        isConceptNote = false;
        isGroupNote = false;        
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
     * permet d'ajouter une note 
     *
     * @param idUser
     */
    public void addNewNote(int idUser) {

        if (noteValue == null || noteValue.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }


        noteValue = new StringPlus().clearValue(noteValue);
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
        new ConceptHelper().updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        noteValue = "";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    
    
    //// notes pour les facettes 
    private void addFacetNote(int idUser){
        if (!addNote(nodeFacet.getIdFacet(), idUser)) {
            printErreur();
            return;
        }
        new FacetHelper().updateDateOfFacet(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeFacet.getIdFacet(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
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
        new GroupHelper().updateModifiedDate(connect.getPoolConnexion(),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentLang());
        noteValue = "";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);           
    }
    
    
    
    
    private boolean addNote(String identifier, int idUser) {
        if (new NoteHelper().isNoteExist(
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

        return new NoteHelper().addNote(
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

        noteValue = new StringPlus().clearValue(noteValue);
        noteValue = StringEscapeUtils.unescapeXml(noteValue);

        if (new NoteHelper().isNoteExist(
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
                if (!checkExisting(propositionBean.getProposition().getNotes())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "definition":
                if (!checkExisting(propositionBean.getProposition().getDefinitions())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "scopeNote":
                if (!checkExisting(propositionBean.getProposition().getScopeNotes())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "example":
                if (!checkExisting(propositionBean.getProposition().getExamples())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "historyNote":
                if (!checkExisting(propositionBean.getProposition().getHistoryNotes())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "editorialNote":
                if (!checkExisting(propositionBean.getProposition().getEditorialNotes())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "changeNote":
                if (!checkExisting(propositionBean.getProposition().getChangeNotes())) {
                    showErrorMessageNote();
                    return;
                }
                break;
        }

        NotePropBean notePropBean = new NotePropBean();
        notePropBean.setToAdd(true);
        notePropBean.setLang(selectedLang);
        notePropBean.setLexicalvalue(noteValue);
        notePropBean.setId_concept(conceptBean.getNodeConcept().getConcept().getIdConcept());
        notePropBean.setNotetypecode(selectedTypeNote);

        switch (selectedTypeNote) {
            case "note":
                propositionBean.getProposition().getNotes().add(notePropBean);
                break;
            case "definition":
                propositionBean.getProposition().getDefinitions().add(notePropBean);
                break;
            case "scopeNote":
                propositionBean.getProposition().getScopeNotes().add(notePropBean);
                break;
            case "example":
                propositionBean.getProposition().getExamples().add(notePropBean);
                break;
            case "historyNote":
                propositionBean.getProposition().getHistoryNotes().add(notePropBean);
                break;
            case "editorialNote":
                propositionBean.getProposition().getEditorialNotes().add(notePropBean);
                break;
            case "changeNote":
                propositionBean.getProposition().getChangeNotes().add(notePropBean);
                break;
        }
        
        propositionBean.checkNotePropositionStatus();
    }

    private void showErrorMessageNote() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note "
                + noteValue + " ( " + selectedLang + ") note existe déjà !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private boolean checkExisting(List<NotePropBean> notes) {
        for (NotePropBean notePropBean : notes) {
            if (selectedLang.equalsIgnoreCase(notePropBean.getLang())
                    && noteValue.equalsIgnoreCase(notePropBean.getLexicalvalue())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
        }
        return true;
    }

    
    
    
    
    public void updateNote(NodeNote nodeNote, int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        FacesMessage msg;
        
        if(nodeNote.getLexicalvalue().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne peut pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            conceptBean.getConcept(
                    selectedTheso.getCurrentIdTheso(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());            
            return;            
        }
        nodeNote.setLexicalvalue(new StringPlus().clearValue(nodeNote.getLexicalvalue()));
        nodeNote.setLexicalvalue(StringEscapeUtils.unescapeXml(nodeNote.getLexicalvalue()));        
        
        if(isFacetNote){
            updateFacetNote(nodeNote, idUser);
            return;
        }
        if(isGroupNote){
            updateGroupNote(nodeNote, idUser);
            return;
        }            
        
        if (!noteHelper.updateNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalvalue(),
                nodeNote.getNotetypecode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    // mise à jour des notes pour les facettes
    private void updateFacetNote(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        NoteHelper noteHelper = new NoteHelper();
        if (!noteHelper.updateNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeFacet.getIdFacet(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalvalue(),
                nodeNote.getNotetypecode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        new FacetHelper().updateDateOfFacet(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeFacet.getIdFacet(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
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
        NoteHelper noteHelper = new NoteHelper();
        if (!noteHelper.updateNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeGroup.getConceptGroup().getIdgroup(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalvalue(),
                nodeNote.getNotetypecode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        new GroupHelper().updateModifiedDate(connect.getPoolConnexion(),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);           
    }    
    
    public void updateNoteProp(NotePropBean notePropBean) {

        switch (selectedTypeNote) {
            case "note":
                for (int i = 0; i < propositionBean.getProposition().getNotes().size(); i++) {
                    if (propositionBean.getProposition().getNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getNotes().get(i).getLexicalvalue())) {
                            propositionBean.getProposition().getNotes().get(i).setToRemove(false);
                            propositionBean.getProposition().getNotes().get(i).setToUpdate(false);
                            propositionBean.getProposition().getNotes().get(i).setToAdd(false);
                            propositionBean.getProposition().getNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getNotes().get(i).isToRemove()) {
                            propositionBean.getProposition().getNotes().get(i).setToUpdate(true);
                            propositionBean.getProposition().getNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getNotes().get(i).isToRemove()) {
                            propositionBean.getProposition().getNotes().get(i).setToRemove(false);
                            propositionBean.getProposition().getNotes().get(i).setToUpdate(true);
                            propositionBean.getProposition().getNotes().get(i).setToAdd(false);
                            propositionBean.getProposition().getNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else {
                            propositionBean.getProposition().getNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "definition":
                for (int i = 0; i < propositionBean.getProposition().getDefinitions().size(); i++) {
                    if (propositionBean.getProposition().getDefinitions().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getDefinitions().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getDefinitions().get(i).getLexicalvalue())) {
                            propositionBean.getProposition().getDefinitions().get(i).setToRemove(false);
                            propositionBean.getProposition().getDefinitions().get(i).setToUpdate(false);
                            propositionBean.getProposition().getDefinitions().get(i).setToAdd(false);
                            propositionBean.getProposition().getDefinitions().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getDefinitions().get(i).isToAdd()) {
                            propositionBean.getProposition().getDefinitions().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getDefinitions().get(i).isToRemove()) {
                            propositionBean.getProposition().getDefinitions().get(i).setToUpdate(true);
                            propositionBean.getProposition().getDefinitions().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "scopeNote":
                for (int i = 0; i < propositionBean.getProposition().getScopeNotes().size(); i++) {
                    if (propositionBean.getProposition().getScopeNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getScopeNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getScopeNotes().get(i).getLexicalvalue())) {
                            propositionBean.getProposition().getScopeNotes().get(i).setToRemove(false);
                            propositionBean.getProposition().getScopeNotes().get(i).setToUpdate(false);
                            propositionBean.getProposition().getScopeNotes().get(i).setToAdd(false);
                            propositionBean.getProposition().getScopeNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getScopeNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getScopeNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getScopeNotes().get(i).isToRemove()) {
                            propositionBean.getProposition().getScopeNotes().get(i).setToUpdate(true);
                            propositionBean.getProposition().getScopeNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "example":
                for (int i = 0; i < propositionBean.getProposition().getExamples().size(); i++) {
                    if (propositionBean.getProposition().getExamples().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getExamples().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getExamples().get(i).getLexicalvalue())) {
                            propositionBean.getProposition().getExamples().get(i).setToRemove(false);
                            propositionBean.getProposition().getExamples().get(i).setToUpdate(false);
                            propositionBean.getProposition().getExamples().get(i).setToAdd(false);
                            propositionBean.getProposition().getExamples().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getExamples().get(i).isToAdd()) {
                            propositionBean.getProposition().getExamples().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getExamples().get(i).isToRemove()) {
                            propositionBean.getProposition().getExamples().get(i).setToUpdate(true);
                            propositionBean.getProposition().getExamples().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "historyNote":
                for (int i = 0; i < propositionBean.getProposition().getHistoryNotes().size(); i++) {
                    if (propositionBean.getProposition().getHistoryNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getHistoryNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getHistoryNotes().get(i).getLexicalvalue())) {
                            propositionBean.getProposition().getHistoryNotes().get(i).setToRemove(false);
                            propositionBean.getProposition().getHistoryNotes().get(i).setToUpdate(false);
                            propositionBean.getProposition().getHistoryNotes().get(i).setToAdd(false);
                            propositionBean.getProposition().getHistoryNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getHistoryNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getHistoryNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getHistoryNotes().get(i).isToRemove()) {
                            propositionBean.getProposition().getHistoryNotes().get(i).setToUpdate(true);
                            propositionBean.getProposition().getHistoryNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "editorialNote":
                for (int i = 0; i < propositionBean.getProposition().getEditorialNotes().size(); i++) {
                    if (propositionBean.getProposition().getEditorialNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getEditorialNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getEditorialNotes().get(i).getLexicalvalue())) {
                            propositionBean.getProposition().getEditorialNotes().get(i).setToRemove(false);
                            propositionBean.getProposition().getEditorialNotes().get(i).setToUpdate(false);
                            propositionBean.getProposition().getEditorialNotes().get(i).setToAdd(false);
                            propositionBean.getProposition().getEditorialNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getEditorialNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getEditorialNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getEditorialNotes().get(i).isToRemove()) {
                            propositionBean.getProposition().getEditorialNotes().get(i).setToUpdate(true);
                            propositionBean.getProposition().getEditorialNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "changeNote":
                for (int i = 0; i < propositionBean.getProposition().getChangeNotes().size(); i++) {
                    if (propositionBean.getProposition().getChangeNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getChangeNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getChangeNotes().get(i).getLexicalvalue())) {
                            propositionBean.getProposition().getChangeNotes().get(i).setToRemove(false);
                            propositionBean.getProposition().getChangeNotes().get(i).setToUpdate(false);
                            propositionBean.getProposition().getChangeNotes().get(i).setToAdd(false);
                            propositionBean.getProposition().getChangeNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getChangeNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getChangeNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getChangeNotes().get(i).isToRemove()) {
                            propositionBean.getProposition().getChangeNotes().get(i).setToUpdate(true);
                            propositionBean.getProposition().getChangeNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
        }
        
        propositionBean.checkNotePropositionStatus();
    }

    public ArrayList<NodeNote> nodeToEdit() {
        if(isFacetNote) {
            return nodeToEditFacet();
        }
        if(isGroupNote) {
            return nodeToEditGroup();
        }        
        return nodeToEditConcept();
    }
    
    private ArrayList<NodeNote> nodeToEditConcept() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return conceptBean.getNotes();
            case "definition":
                return conceptBean.getDefinitions();
            case "scopeNote":
                return conceptBean.getScopeNotes();
            case "example":
                return conceptBean.getExamples();
            case "historyNote":
                return conceptBean.getHistoryNotes();
            case "editorialNote":
                return conceptBean.getEditorialNotes();
            case "changeNote":
                return conceptBean.getChangeNotes();
            default:
                return null;
        }
    }
    
    private ArrayList<NodeNote> nodeToEditFacet() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return editFacet.getNotes();
            case "definition":
                return editFacet.getDefinitions();
            case "scopeNote":
                return editFacet.getScopeNotes();
            case "example":
                return editFacet.getExamples();
            case "historyNote":
                return editFacet.getHistoryNotes();
            case "editorialNote":
                return editFacet.getEditorialNotes();
            case "changeNote":
                return editFacet.getChangeNotes();
            default:
                return null;
        }
    }    
    
    private ArrayList<NodeNote> nodeToEditGroup() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return groupView.getNotes();
            case "definition":
                return groupView.getDefinitions();
            case "scopeNote":
                return groupView.getScopeNotes();
            case "example":
                return groupView.getExamples();
            case "historyNote":
                return groupView.getHistoryNotes();
            case "editorialNote":
                return groupView.getEditorialNotes();
            case "changeNote":
                return groupView.getChangeNotes();
            default:
                return null;
        }
    }       

    public List<NotePropBean> nodePropToEdit() {
        if (selectedTypeNote == null) {
            return new ArrayList<>();
        }
        if(propositionBean.getProposition() == null) return new ArrayList<>();
        switch (selectedTypeNote) {
            case "note":
                return propositionBean.getProposition().getNotes();
            case "definition":
                return propositionBean.getProposition().getDefinitions();
            case "scopeNote":
                return propositionBean.getProposition().getScopeNotes();
            case "example":
                return propositionBean.getProposition().getExamples();
            case "historyNote":
                return propositionBean.getProposition().getHistoryNotes();
            case "editorialNote":
                return propositionBean.getProposition().getEditorialNotes();
            default:
                return propositionBean.getProposition().getChangeNotes();
        }
    }

    public void deleteNote(NodeNote nodeNote, int idUser) {
        NoteHelper noteHelper = new NoteHelper();
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
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeNote.getId_concept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNotetypecode(),
                nodeNote.getLexicalvalue(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

    }
    
    private void deleteThisNoteFacet(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        NoteHelper noteHelper = new NoteHelper();        
        if (!noteHelper.deleteThisNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeNote.getId_concept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNotetypecode(),
                nodeNote.getLexicalvalue(), idUser)) {
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
        NoteHelper noteHelper = new NoteHelper();        
        if (!noteHelper.deleteThisNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeGroup.getConceptGroup().getIdgroup(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNotetypecode(),
                nodeNote.getLexicalvalue(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);             
    }    
    
    public ArrayList<NodeNote> nodeToDelete() {
        if(isFacetNote) {
            return nodeToDeleteFacet();
        }
        if(isGroupNote) {
            return nodeToDeleteGroup();
        }        
        return nodeToDeleteConcept();
    }    

    private ArrayList<NodeNote> nodeToDeleteConcept() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return conceptBean.getNotes();
            case "definition":
                return conceptBean.getDefinitions();
            case "scopeNote":
                return conceptBean.getScopeNotes();
            case "example":
                return conceptBean.getExamples();
            case "historyNote":
                return conceptBean.getHistoryNotes();
            case "editorialNote":
                return conceptBean.getEditorialNotes();
            case "changeNote":
                return conceptBean.getChangeNotes();
            default:
                return null;
        }
    }
    
    private ArrayList<NodeNote> nodeToDeleteFacet() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return editFacet.getNotes();
            case "definition":
                return editFacet.getDefinitions();
            case "scopeNote":
                return editFacet.getScopeNotes();
            case "example":
                return editFacet.getExamples();
            case "historyNote":
                return editFacet.getHistoryNotes();
            case "editorialNote":
                return editFacet.getEditorialNotes();
            case "changeNote":
                return editFacet.getChangeNotes();
            default:
                return null;
        }
    }    
    
    private ArrayList<NodeNote> nodeToDeleteGroup() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return groupView.getNotes();
            case "definition":
                return groupView.getDefinitions();
            case "scopeNote":
                return groupView.getScopeNotes();
            case "example":
                return groupView.getExamples();
            case "historyNote":
                return groupView.getHistoryNotes();
            case "editorialNote":
                return groupView.getEditorialNotes();
            case "changeNote":
                return groupView.getChangeNotes();
            default:
                return null;
        }
    }      

    public void deleteNoteProp(NotePropBean notePropBean) {

        switch (selectedTypeNote) {
            case "note":
                for (int i = 0; i < propositionBean.getProposition().getNotes().size(); i++) {
                    if (propositionBean.getProposition().getNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getNotes().remove(i);
                        } else if (propositionBean.getProposition().getNotes().get(i).isToUpdate()) {
                            propositionBean.getProposition().getNotes().get(i).setToUpdate(false);
                            propositionBean.getProposition().getNotes().get(i).setToRemove(true);
                            propositionBean.getProposition().getNotes().get(i)
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getNotes().get(i).setToRemove(
                                    !propositionBean.getProposition().getNotes().get(i).isToRemove());
                        }
                    }
                }
                break;
            case "definition":
                for (int i = 0; i < propositionBean.getProposition().getDefinitions().size(); i++) {
                    if (propositionBean.getProposition().getDefinitions().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getDefinitions().get(i).isToAdd()) {
                            propositionBean.getProposition().getDefinitions().remove(i);
                        } else if (propositionBean.getProposition().getDefinitions().get(i).isToUpdate()) {
                            propositionBean.getProposition().getDefinitions().get(i).setToUpdate(false);
                            propositionBean.getProposition().getDefinitions().get(i).setToRemove(true);
                            propositionBean.getProposition().getDefinitions().get(i)
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getDefinitions().get(i).setToRemove(
                                    !propositionBean.getProposition().getDefinitions().get(i).isToRemove());
                        }
                    }
                }
                break;
            case "scopeNote":
                for (int i = 0; i < propositionBean.getProposition().getScopeNotes().size(); i++) {
                    if (propositionBean.getProposition().getScopeNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getScopeNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getScopeNotes().remove(i);
                        } else if (propositionBean.getProposition().getScopeNotes().get(i).isToUpdate()) {
                            propositionBean.getProposition().getScopeNotes().get(i).setToUpdate(false);
                            propositionBean.getProposition().getScopeNotes().get(i).setToRemove(true);
                            propositionBean.getProposition().getScopeNotes().get(i)
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getScopeNotes().get(i).setToRemove(
                                    !propositionBean.getProposition().getScopeNotes().get(i).isToRemove());
                        }
                    }
                }
                break;
            case "example":
                for (int i = 0; i < propositionBean.getProposition().getExamples().size(); i++) {
                    if (propositionBean.getProposition().getExamples().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getExamples().get(i).isToAdd()) {
                            propositionBean.getProposition().getExamples().remove(i);
                        } else if (propositionBean.getProposition().getExamples().get(i).isToUpdate()) {
                            propositionBean.getProposition().getExamples().get(i).setToUpdate(false);
                            propositionBean.getProposition().getExamples().get(i).setToRemove(true);
                            propositionBean.getProposition().getExamples().get(i)
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getExamples().get(i).setToRemove(
                                    !propositionBean.getProposition().getExamples().get(i).isToRemove());
                        }
                    }
                }
                break;
            case "historyNote":
                for (int i = 0; i < propositionBean.getProposition().getHistoryNotes().size(); i++) {
                    if (propositionBean.getProposition().getHistoryNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getHistoryNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getHistoryNotes().remove(i);
                        } else if (propositionBean.getProposition().getHistoryNotes().get(i).isToUpdate()) {
                            propositionBean.getProposition().getHistoryNotes().get(i).setToUpdate(false);
                            propositionBean.getProposition().getHistoryNotes().get(i).setToRemove(true);
                            propositionBean.getProposition().getHistoryNotes().get(i)
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getHistoryNotes().get(i).setToRemove(
                                    !propositionBean.getProposition().getHistoryNotes().get(i).isToRemove());
                        }
                    }
                }
                break;
            case "editorialNote":
                for (int i = 0; i < propositionBean.getProposition().getEditorialNotes().size(); i++) {
                    if (propositionBean.getProposition().getEditorialNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getEditorialNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getEditorialNotes().remove(i);
                        } else if (propositionBean.getProposition().getEditorialNotes().get(i).isToUpdate()) {
                            propositionBean.getProposition().getEditorialNotes().get(i).setToUpdate(false);
                            propositionBean.getProposition().getEditorialNotes().get(i).setToRemove(true);
                            propositionBean.getProposition().getEditorialNotes().get(i)
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getEditorialNotes().get(i).setToRemove(
                                    !propositionBean.getProposition().getEditorialNotes().get(i).isToRemove());
                        }
                    }
                }
                break;
            case "changeNote":
                for (int i = 0; i < propositionBean.getProposition().getChangeNotes().size(); i++) {
                    if (propositionBean.getProposition().getChangeNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getChangeNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getChangeNotes().remove(i);
                        } else if (propositionBean.getProposition().getChangeNotes().get(i).isToUpdate()) {
                            propositionBean.getProposition().getChangeNotes().get(i).setToUpdate(false);
                            propositionBean.getProposition().getChangeNotes().get(i).setToRemove(true);
                            propositionBean.getProposition().getChangeNotes().get(i)
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getChangeNotes().get(i).setToRemove(
                                    !propositionBean.getProposition().getChangeNotes().get(i).isToRemove());
                        }
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

    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public ArrayList<NoteHelper.NoteType> getNoteTypes() {
        return noteTypes;
    }

    public void setNoteTypes(ArrayList<NoteHelper.NoteType> noteTypes) {
        this.noteTypes = noteTypes;
    }

    public String getSelectedTypeNote() {
        return selectedTypeNote;
    }

    public void setSelectedTypeNote(String selectedTypeNote) {
        this.selectedTypeNote = selectedTypeNote;
    }

    public String getNoteValue() {
        return noteValue;
    }

    public void setNoteValue(String noteValue) {
        this.noteValue = noteValue;
    }

    public ArrayList<NodeLangTheso> getNodeLangs() {
        return nodeLangs;
    }

    public void setNodeLangs(ArrayList<NodeLangTheso> nodeLangs) {
        this.nodeLangs = nodeLangs;
    }

    public NodeNote getSelectedNodeNote() {
        return selectedNodeNote;
    }

    public void setSelectedNodeNote(NodeNote selectedNodeNote) {
        this.selectedNodeNote = selectedNodeNote;
    }

    public boolean isIsConceptNote() {
        return isConceptNote;
    }

    public void setIsConceptNote(boolean isConceptNote) {
        this.isConceptNote = isConceptNote;
    }

    public boolean isIsGroupNote() {
        return isGroupNote;
    }

    public void setIsGroupNote(boolean isGroupNote) {
        this.isGroupNote = isGroupNote;
    }

    public boolean isIsFacetNote() {
        return isFacetNote;
    }

    public void setIsFacetNote(boolean isFacetNote) {
        this.isFacetNote = isFacetNote;
    }

}
