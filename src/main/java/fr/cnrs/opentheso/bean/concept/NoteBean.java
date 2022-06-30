package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
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

        if (noteValue == null || noteValue.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
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

        new ConceptHelper().updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        noteValue = "";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void addNewNoteProp() {

        if (noteValue == null || noteValue.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        noteValue = clearValue(noteValue);
        noteValue = StringEscapeUtils.unescapeXml(noteValue);

        if (new NoteHelper().isNoteExistOfConcept(
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
    }

    public void updateNoteProp(NotePropBean notePropBean) {

        switch (selectedTypeNote) {
            case "note":
                for (int i = 0; i < propositionBean.getProposition().getNotes().size(); i++) {
                    if (propositionBean.getProposition().getNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (!propositionBean.getProposition().getNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getNotes().get(i).setToUpdate(true);
                        }
                        propositionBean.getProposition().getNotes().get(i).setLang(notePropBean.getLang());
                        propositionBean.getProposition().getNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                    }
                }
                break;
            case "definition":
                for (int i = 0; i < propositionBean.getProposition().getDefinitions().size(); i++) {
                    if (propositionBean.getProposition().getDefinitions().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (!propositionBean.getProposition().getDefinitions().get(i).isToAdd()) {
                            propositionBean.getProposition().getDefinitions().get(i).setToUpdate(true);
                        }
                        propositionBean.getProposition().getDefinitions().get(i).setLang(notePropBean.getLang());
                        propositionBean.getProposition().getDefinitions().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                    }
                }
                break;
            case "scopeNote":
                for (int i = 0; i < propositionBean.getProposition().getScopeNotes().size(); i++) {
                    if (propositionBean.getProposition().getScopeNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (!propositionBean.getProposition().getScopeNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getScopeNotes().get(i).setToUpdate(true);
                        }
                        propositionBean.getProposition().getScopeNotes().get(i).setLang(notePropBean.getLang());
                        propositionBean.getProposition().getScopeNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                    }
                }
                break;
            case "example":
                for (int i = 0; i < propositionBean.getProposition().getExamples().size(); i++) {
                    if (propositionBean.getProposition().getExamples().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (!propositionBean.getProposition().getExamples().get(i).isToAdd()) {
                            propositionBean.getProposition().getExamples().get(i).setToUpdate(true);
                        }
                        propositionBean.getProposition().getExamples().get(i).setLang(notePropBean.getLang());
                        propositionBean.getProposition().getExamples().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                    }
                }
                break;
            case "historyNote":
                for (int i = 0; i < propositionBean.getProposition().getHistoryNotes().size(); i++) {
                    if (propositionBean.getProposition().getHistoryNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (!propositionBean.getProposition().getHistoryNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getHistoryNotes().get(i).setToUpdate(true);
                        }
                        propositionBean.getProposition().getHistoryNotes().get(i).setLang(notePropBean.getLang());
                        propositionBean.getProposition().getHistoryNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                    }
                }
                break;
            case "editorialNote":
                for (int i = 0; i < propositionBean.getProposition().getEditorialNotes().size(); i++) {
                    if (propositionBean.getProposition().getEditorialNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (!propositionBean.getProposition().getEditorialNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getEditorialNotes().get(i).setToUpdate(true);
                        }
                        propositionBean.getProposition().getEditorialNotes().get(i).setLang(notePropBean.getLang());
                        propositionBean.getProposition().getEditorialNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                    }
                }
                break;
            case "changeNote":
                for (int i = 0; i < propositionBean.getProposition().getChangeNotes().size(); i++) {
                    if (propositionBean.getProposition().getChangeNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (!propositionBean.getProposition().getChangeNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getChangeNotes().get(i).setToUpdate(true);
                        }
                        propositionBean.getProposition().getChangeNotes().get(i).setLang(notePropBean.getLang());
                        propositionBean.getProposition().getChangeNotes().get(i).setLexicalvalue(notePropBean.getLexicalvalue());
                    }
                }
                break;
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

    public List<NotePropBean> nodePropToEdit() {
        if (selectedTypeNote == null) {
            return null;
        }
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
            case "changeNote":
                return propositionBean.getProposition().getChangeNotes();
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

    public void deleteNoteProp(NotePropBean notePropBean) {

        switch (selectedTypeNote) {
            case "note":
                for (int i = 0; i < propositionBean.getProposition().getNotes().size(); i++) {
                    if (propositionBean.getProposition().getNotes().get(i).getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getNotes().get(i).isToAdd()) {
                            propositionBean.getProposition().getNotes().remove(i);
                        } else {
                            propositionBean.getProposition().getNotes().get(i).setToRemove(true);
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
                        } else {
                            propositionBean.getProposition().getDefinitions().get(i).setToRemove(true);
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
                        } else {
                            propositionBean.getProposition().getScopeNotes().get(i).setToRemove(true);
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
                        } else {
                            propositionBean.getProposition().getExamples().get(i).setToRemove(true);
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
                        } else {
                            propositionBean.getProposition().getHistoryNotes().get(i).setToRemove(true);
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
                        } else {
                            propositionBean.getProposition().getEditorialNotes().get(i).setToRemove(true);
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
                        } else {
                            propositionBean.getProposition().getChangeNotes().get(i).setToRemove(true);
                        }
                    }
                }
                break;
        }
    }

    private boolean addConceptNote(int idUser) {

        if (new NoteHelper().isNoteExistOfConcept(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                selectedLang,
                noteValue,
                selectedTypeNote)) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }

        return new NoteHelper().addConceptNote(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedLang,
                selectedTheso.getCurrentIdTheso(),
                noteValue,
                selectedTypeNote, idUser);
    }

    private boolean addtermNote(int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        if (noteHelper.isNoteExistOfTerm(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getId_term(),
                selectedTheso.getCurrentIdTheso(),
                selectedLang,
                noteValue,
                selectedTypeNote)) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }

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
