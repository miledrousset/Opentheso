/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
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
    private LanguageBean languageBean;
    @Inject
    private ConceptView conceptBean;
    @Inject
    private SelectedTheso selectedTheso;

    private String selectedLang;
    private ArrayList<NoteHelper.NoteType> noteTypes;
    private ArrayList<NodeLangTheso> nodeLangs;
    private String selectedTypeNote;
    private String noteValue;
    private NodeNote selectedNodeNote;

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
    }

    public void reset() {
        noteTypes = new NoteHelper().getNotesType(connect.getPoolConnexion());
        nodeLangs = selectedTheso.getNodeLangs();
        selectedLang = selectedTheso.getSelectedLang();
        noteValue = "";
        selectedTypeNote = null;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private String clearValue(String rawNote) {
        rawNote = rawNote.replaceAll("<p>", "");
        rawNote = rawNote.replaceAll("</p>", "\n");

        // enlève les code ascii non visibles
        return rawNote.replace((char) 27, ' ');
    }

    /**
     * permet d'ajouter un nouveau concept si le groupe = null, on ajoute un
     * concept sans groupe si l'id du concept est fourni, il faut controler s'il
     * est unique
     *
     * @param idUser
     */
    public void addNewNote(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        if (noteValue == null || noteValue.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        noteValue = clearValue(noteValue);
        noteValue = StringEscapeUtils.unescapeXml(noteValue);
        switch (selectedTypeNote) {
            case "note":
                if (!addConceptNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "definition":
                if (!addtermNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "scopeNote":
                if (!addConceptNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "example":
                if (!addtermNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "historyNote":
                if (!addtermNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "editorialNote":
                if (!addtermNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "changeNote":
                if (!addtermNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            default:
                break;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept());

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note ajoutée avec succès");

        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('addNote').hide();");

        reset();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public void updateNote(NodeNote nodeNote, int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        FacesMessage msg;
        if (selectedTypeNote.equalsIgnoreCase("note") || selectedTypeNote.equalsIgnoreCase("scopeNote") || selectedTypeNote.equalsIgnoreCase("historyNote")) {
            if (!noteHelper.updateConceptNote(connect.getPoolConnexion(),
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
        } else {
            if (!noteHelper.updateTermNote(connect.getPoolConnexion(),
                    nodeNote.getId_note(),
                    nodeNote.getId_term(),
                    nodeNote.getLang(),
                    selectedTheso.getCurrentIdTheso(),
                    nodeNote.getLexicalvalue(),
                    nodeNote.getNotetypecode(),
                    idUser)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('addNote').hide();");
        //      reset();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("conceptForm:idEditNote");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public ArrayList<NodeNote> nodeToEdit() {
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

    public void deleteNote(NodeNote nodeNote, int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        FacesMessage msg;

        if (selectedTypeNote.equalsIgnoreCase("note") || selectedTypeNote.equalsIgnoreCase("scopeNote") || selectedTypeNote.equalsIgnoreCase("historyNote")) {
            if (!noteHelper.deletethisNoteOfConcept(connect.getPoolConnexion(),
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
        } else {
            if (!noteHelper.deleteThisNoteOfTerm(connect.getPoolConnexion(),
                    nodeNote.getId_note(),
                    nodeNote.getId_term(),
                    nodeNote.getLang(),
                    selectedTheso.getCurrentIdTheso(),
                    nodeNote.getNotetypecode(),
                    nodeNote.getLexicalvalue(), idUser)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        //    PrimeFaces.current().executeScript("PF('addNote').hide();");
        //      reset();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("conceptForm:idDeleteNote");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public ArrayList<NodeNote> nodeToDelete() {
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

    private boolean addConceptNote(int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        return noteHelper.addConceptNote(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedLang,
                selectedTheso.getCurrentIdTheso(),
                noteValue,
                selectedTypeNote, idUser);
    }

    private boolean addtermNote(int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        return noteHelper.addTermNote(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getId_term(),
                selectedLang,
                selectedTheso.getCurrentIdTheso(),
                noteValue,
                selectedTypeNote, idUser);
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

}