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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

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
    
    private int count;

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

        ArrayList<NodeNote> nodeNotes = noteHelper.getListNotes(idGroup, idTheso, idLang);
        setAllNotes(nodeNotes);

        count = conceptHelper.getCountOfConceptsOfGroup(idTheso, idGroup);
        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();
    }
    
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
    private void clearNotes() {
        note = null;
        scopeNote = null;
        changeNote = null;
        definition = null;
        editorialNote = null;
        example = null;
        historyNote = null;
    }        

    public NodeGroup getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(NodeGroup nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<NodeGroupTraductions> getNodeGroupTraductions() {
        return nodeGroupTraductions;
    }

    public void setNodeGroupTraductions(ArrayList<NodeGroupTraductions> nodeGroupTraductions) {
        this.nodeGroupTraductions = nodeGroupTraductions;
    }

    public NodeGroupType getNodeGroupType() {
        return nodeGroupType;
    }

    public void setNodeGroupType(NodeGroupType nodeGroupType) {
        this.nodeGroupType = nodeGroupType;
    }

    public NodeNote getNote() {
        return note;
    }

    public void setNote(NodeNote note) {
        this.note = note;
    }

    public NodeNote getScopeNote() {
        return scopeNote;
    }

    public void setScopeNote(NodeNote scopeNote) {
        this.scopeNote = scopeNote;
    }

    public NodeNote getChangeNote() {
        return changeNote;
    }

    public void setChangeNote(NodeNote changeNote) {
        this.changeNote = changeNote;
    }

    public NodeNote getDefinition() {
        return definition;
    }

    public void setDefinition(NodeNote definition) {
        this.definition = definition;
    }

    public NodeNote getEditorialNote() {
        return editorialNote;
    }

    public void setEditorialNote(NodeNote editorialNote) {
        this.editorialNote = editorialNote;
    }

    public NodeNote getExample() {
        return example;
    }

    public void setExample(NodeNote example) {
        this.example = example;
    }

    public NodeNote getHistoryNote() {
        return historyNote;
    }

    public void setHistoryNote(NodeNote historyNote) {
        this.historyNote = historyNote;
    }

 
   
}
