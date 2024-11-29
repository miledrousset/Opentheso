package fr.cnrs.opentheso.bean.rightbody.viewgroup;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.models.group.NodeGroupType;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.group.NodeGroupTraductions;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.bean.index.IndexSetting;

import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Data
/**
 *
 * @author miledrousset
 */
@Named(value = "groupView")
@SessionScoped
public class GroupView implements Serializable {

    
    @Autowired @Lazy private IndexSetting indexSetting;     
    @Autowired @Lazy private ViewEditorThesoHomeBean viewEditorThesoHomeBean;
    @Autowired @Lazy private ViewEditorHomeBean viewEditorHomeBean;

    @Autowired
    private GroupHelper groupHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private NoteHelper noteHelper;

    private NodeGroup nodeGroup;
    private ArrayList<NodeGroupTraductions> nodeGroupTraductions;
    private NodeGroupType nodeGroupType;
    
    private NodeNote note;
    private NodeNote scopeNote;
    private NodeNote changeNote;
    private NodeNote definition;
    private NodeNote editorialNote;
    private NodeNote example;
    private NodeNote historyNote;

    /// Notes du concept pour l'affichage du multilingue
    private ArrayList<NodeNote> noteAllLang;
    private ArrayList<NodeNote> scopeNoteAllLang;
    private ArrayList<NodeNote> changeNoteAllLang;
    private ArrayList<NodeNote> definitionAllLang;
    private ArrayList<NodeNote> editorialNoteAllLang;
    private ArrayList<NodeNote> exampleAllLang;
    private ArrayList<NodeNote> historyNoteAllLang;
    
    private int count;

    private boolean toggleSwitchNotesLang;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeGroupTraductions!= null){
            nodeGroupTraductions.clear();
            nodeGroupTraductions = null;
        }
        nodeGroup = null;
        nodeGroupType = null;
    }      
    
    /**
     * Creates a new instance of ConceptBean
     */
    public GroupView() {
        toggleSwitchNotesLang = true;
    }

    public void init() {
        /*  if(isUriRequest) {
            isUriRequest = false;
            return;
        }*/
        count = 0;
        nodeGroup = null;
        nodeGroupType = null;
        nodeGroupTraductions = null;
    }

    /**
     * récuparation des informations pour le concept sélectionné
     *
     * @param idTheso
     * @param idGroup
     * @param idLang
     */
    public void getGroup(String idTheso, String idGroup, String idLang) {

        nodeGroup = groupHelper.getThisConceptGroup(idGroup, idTheso, idLang);
        
        nodeGroupTraductions = groupHelper.getGroupTraduction(idGroup, idTheso, idLang);
        nodeGroupType = groupHelper.getGroupType(nodeGroup.getConceptGroup().getIdtypecode());

        setNotes(idTheso, idGroup, idLang);

        count = conceptHelper.getCountOfConceptsOfGroup(idTheso, idGroup);
        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();
    }

    public void setNotes(String idTheso, String idGroup, String idLang) {
        ArrayList<NodeNote> nodeNotes;
        if (toggleSwitchNotesLang) {
            nodeNotes = noteHelper.getListNotesAllLang(idGroup, idTheso);
            setNotesForAllLang(nodeNotes);
        } else {
            nodeNotes = noteHelper.getListNotes(idGroup, idTheso, idLang);
            setAllNotes(nodeNotes);
        }
    };
    
    /////////////////////////////////
    /////////////////////////////////
    // fonctions pour les notes /////    
    /////////////////////////////////
    /////////////////////////////////
    private void setAllNotes(ArrayList<NodeNote> nodeNotes) {
        clearNotes();
        for (NodeNote nodeNote : nodeNotes) {
            switch (nodeNote.getNoteTypeCode()) {
                case "note":
                    note  = nodeNote;
                    break;
                case "scopeNote":
                    scopeNote = nodeNote;
                    break;
                case "changeNote":
                    changeNote = nodeNote;
                    break;
                case "definition":
                    definition = nodeNote;
                    break;
                case "editorialNote":
                    editorialNote = nodeNote;
                    break;
                case "example":
                    example = nodeNote;
                    break;
                case "historyNote":
                    historyNote = nodeNote;
                    break;
            }
        }
    }

    private void setNotesForAllLang(ArrayList<NodeNote> nodeNotes) {
        clearNotesAllLang();
        clearNotes();

        if (CollectionUtils.isNotEmpty(nodeNotes)) {
            // note
            nodeNotes.stream()
                    .filter(note1 -> "note".equals(note1.getNoteTypeCode()))
                    .map(note1 -> {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setIdNote(note1.getIdNote());
                        nodeNote.setIdConcept(note1.getIdentifier());
                        nodeNote.setNoteTypeCode(note1.getNoteTypeCode());
                        nodeNote.setLexicalValue(note1.getLexicalValue());
                        nodeNote.setLang(note1.getLang());
                        nodeNote.setNoteSource(note1.getNoteSource());
                        return nodeNote;
                    })
                    .forEach(noteAllLang::add);

            // scopeNote
            nodeNotes.stream()
                    .filter(note1 -> "scopeNote".equals(note1.getNoteTypeCode()))
                    .map(note1 -> {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setIdNote(note1.getIdNote());
                        nodeNote.setIdConcept(note1.getIdentifier());
                        nodeNote.setNoteTypeCode(note1.getNoteTypeCode());
                        nodeNote.setLexicalValue(note1.getLexicalValue());
                        nodeNote.setLang(note1.getLang());
                        nodeNote.setNoteSource(note1.getNoteSource());
                        return nodeNote;
                    })
                    .forEach(scopeNoteAllLang::add);

            // changeNote
            nodeNotes.stream()
                    .filter(note1 -> "changeNote".equals(note1.getNoteTypeCode()))
                    .map(note1 -> {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setIdNote(note1.getIdNote());
                        nodeNote.setIdConcept(note1.getIdentifier());
                        nodeNote.setNoteTypeCode(note1.getNoteTypeCode());
                        nodeNote.setLexicalValue(note1.getLexicalValue());
                        nodeNote.setLang(note1.getLang());
                        nodeNote.setNoteSource(note1.getNoteSource());
                        return nodeNote;
                    })
                    .forEach(changeNoteAllLang::add);

            // definition
            nodeNotes.stream()
                    .filter(note1 -> "definition".equals(note1.getNoteTypeCode()))
                    .map(note1 -> {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setIdNote(note1.getIdNote());
                        nodeNote.setIdConcept(note1.getIdentifier());
                        nodeNote.setNoteTypeCode(note1.getNoteTypeCode());
                        nodeNote.setLexicalValue(note1.getLexicalValue());
                        nodeNote.setLang(note1.getLang());
                        nodeNote.setNoteSource(note1.getNoteSource());
                        return nodeNote;
                    })
                    .forEach(definitionAllLang::add);

            // editorialNote
            nodeNotes.stream()
                    .filter(note1 -> "editorialNote".equals(note1.getNoteTypeCode()))
                    .map(note1 -> {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setIdNote(note1.getIdNote());
                        nodeNote.setIdConcept(note1.getIdentifier());
                        nodeNote.setNoteTypeCode(note1.getNoteTypeCode());
                        nodeNote.setLexicalValue(note1.getLexicalValue());
                        nodeNote.setLang(note1.getLang());
                        nodeNote.setNoteSource(note1.getNoteSource());
                        return nodeNote;
                    })
                    .forEach(editorialNoteAllLang::add);

            // example
            nodeNotes.stream()
                    .filter(note1 -> "example".equals(note1.getNoteTypeCode()))
                    .map(note1 -> {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setIdNote(note1.getIdNote());
                        nodeNote.setIdConcept(note1.getIdentifier());
                        nodeNote.setNoteTypeCode(note1.getNoteTypeCode());
                        nodeNote.setLexicalValue(note1.getLexicalValue());
                        nodeNote.setLang(note1.getLang());
                        nodeNote.setNoteSource(note1.getNoteSource());
                        return nodeNote;
                    })
                    .forEach(exampleAllLang::add);

            // historyNote
            nodeNotes.stream()
                    .filter(note1 -> "historyNote".equals(note1.getNoteTypeCode()))
                    .map(note1 -> {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setIdNote(note1.getIdNote());
                        nodeNote.setIdConcept(note1.getIdentifier());
                        nodeNote.setNoteTypeCode(note1.getNoteTypeCode());
                        nodeNote.setLexicalValue(note1.getLexicalValue());
                        nodeNote.setLang(note1.getLang());
                        nodeNote.setNoteSource(note1.getNoteSource());
                        return nodeNote;
                    })
                    .forEach(historyNoteAllLang::add);
        }

    }
    private void clearNotesAllLang() {
        noteAllLang = new ArrayList<>();
        scopeNoteAllLang = new ArrayList<>();
        changeNoteAllLang = new ArrayList<>();
        definitionAllLang = new ArrayList<>();
        editorialNoteAllLang = new ArrayList<>();
        exampleAllLang = new ArrayList<>();
        historyNoteAllLang = new ArrayList<>();
    }

    private void clearNotes() {
        note = null;
        scopeNote = null;
        changeNote = null;
        definition = null;
        editorialNote = null;
        example = null;
        historyNote = null;
    }
}
