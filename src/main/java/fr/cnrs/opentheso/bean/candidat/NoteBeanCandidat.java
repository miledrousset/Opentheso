package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.entites.NoteType;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.services.CandidatService;
import fr.cnrs.opentheso.services.NoteService;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;



@Data
@Named(value = "noteBeanCandidat")
@SessionScoped
@RequiredArgsConstructor
public class NoteBeanCandidat implements Serializable {

    private final SelectedTheso selectedTheso;
    private final CandidatBean candidatBean;
    private final NoteService noteService;
    private final CandidatService candidatService;

    private List<NoteType> noteTypes;
    private List<NodeLangTheso> nodeLangs;
    private String selectedLang, selectedTypeNote, noteValue, noteValueToChange;
    private NodeNote selectedNodeNote;
    private boolean isEditMode, visible;


    public void reset() {
        visible = true;
        noteTypes = noteService.getNotesType();
        nodeLangs = selectedTheso.getNodeLangs();
        selectedLang = candidatBean.getCandidatSelected().getLang();
        noteValue = "";
        selectedTypeNote = null;
        isEditMode = false;
    }
   
    public void resetEditNode(NodeNote selectedNodeNote) {
        reset();
        noteValue = selectedNodeNote.getLexicalValue();
        selectedTypeNote = selectedNodeNote.getLang();
        this.selectedNodeNote = selectedNodeNote;
        isEditMode = true;
    }
    
    public void infos() {
        MessageUtils.showInformationMessage("Rédiger une aide ici pour Add Concept !");
    }

    /**
     * permet d'ajouter un nouveau concept si le groupe = null, on ajoute un
     * concept sans groupe si l'id du concept est fourni, il faut controler s'il
     * est unique
     *
     * @param idUser
     */
    public void addNewNote(int idUser) {

        if(isEditMode) {
            updateNote(idUser);
            return;
        }

        if (StringUtils.isEmpty(noteValue)) {
            MessageUtils.showErrorMessage("La note ne doit pas être vide !");
            return;
        }

        noteService.addNote(candidatBean.getCandidatSelected().getIdConcepte(), selectedLang, selectedTheso.getCurrentIdTheso(),
                noteValue, selectedTypeNote, "", idUser);

        refreshInterface();
        MessageUtils.showInformationMessage("Note ajoutée avec succès");
        PrimeFaces.current().ajax().update("candidatForm:listTraductionForm");
    }
    
    public void updateNote(int idUser) {

        if (!noteService.updateNote(selectedNodeNote.getIdNote(), selectedNodeNote.getIdConcept(), selectedNodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(), selectedNodeNote.getLexicalValue(), selectedNodeNote.getNoteSource(),
                selectedNodeNote.getNoteTypeCode(), idUser)) {

            MessageUtils.showErrorMessage("Erreur pendant la modification de la note !");
            return;
        }

        refreshInterface();
        MessageUtils.showInformationMessage("Note modifiée avec succès");
    }
    
    public void deleteNote(int idUser) {

        noteService.deleteThisNote(selectedNodeNote.getIdNote(), selectedNodeNote.getIdConcept(), selectedNodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(), selectedNodeNote.getNoteTypeCode(), noteValueToChange, idUser);

        candidatService.deleteVoteByNoteId(selectedNodeNote.getIdNote(), selectedTheso.getCurrentIdTheso(),
                selectedNodeNote.getIdConcept());

        refreshInterface();
        MessageUtils.showInformationMessage("Note supprimée avec succès");
        PrimeFaces.current().ajax().update("candidatForm");
    }

    private void refreshInterface() {
        reset();
        visible = false;
        candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
    }
}
