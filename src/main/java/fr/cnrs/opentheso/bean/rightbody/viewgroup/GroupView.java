/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.rightbody.viewgroup;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGroupType;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupTraductions;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "groupView")
@SessionScoped
public class GroupView implements Serializable {
    @Inject private Connect connect;
    @Inject private IndexSetting indexSetting;     
    @Inject private ViewEditorThesoHomeBean viewEditorThesoHomeBean;
    @Inject private ViewEditorHomeBean viewEditorHomeBean;       

    private NodeGroup nodeGroup;
    private ArrayList<NodeGroupTraductions> nodeGroupTraductions;
    private NodeGroupType nodeGroupType;
    
    private ArrayList<NodeNote> notes;
    private ArrayList<NodeNote> scopeNotes;
    private ArrayList<NodeNote> changeNotes;
    private ArrayList<NodeNote> definitions;
    private ArrayList<NodeNote> editorialNotes;
    private ArrayList<NodeNote> examples;
    private ArrayList<NodeNote> historyNotes;        
    
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
        GroupHelper groupHelper = new GroupHelper();
        nodeGroup = groupHelper.getThisConceptGroup(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        
        nodeGroupTraductions = groupHelper.getGroupTraduction(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        nodeGroupType = groupHelper.getGroupType(
                connect.getPoolConnexion(), nodeGroup.getConceptGroup().getIdtypecode());
        
        
        NoteHelper noteHelper = new NoteHelper();
        ArrayList<NodeNote> nodeNotes = noteHelper.getListNotes(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        setAllNotes(nodeNotes);        
        
        ConceptHelper conceptHelper = new ConceptHelper();
        count = conceptHelper.getCountOfConceptsOfGroup(connect.getPoolConnexion(), idTheso, idGroup);
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
            switch (nodeNote.getNotetypecode()) {
                case "note":
                    notes.add(nodeNote);
                    break;
                case "scopeNote":
                    scopeNotes.add(nodeNote);
                    break;
                case "changeNote":
                    changeNotes.add(nodeNote);
                    break;
                case "definition":
                    definitions.add(nodeNote);
                    break;
                case "editorialNote":
                    editorialNotes.add(nodeNote);
                    break;
                case "example":
                    examples.add(nodeNote);
                    break;
                case "historyNote":
                    historyNotes.add(nodeNote);
                    break;
            }
        }
    }
    private void clearNotes() {
        notes = new ArrayList<>();
        scopeNotes = new ArrayList<>();
        changeNotes = new ArrayList<>();
        definitions = new ArrayList<>();
        editorialNotes = new ArrayList<>();
        examples = new ArrayList<>();
        historyNotes = new ArrayList<>();
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

    public ArrayList<NodeNote> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<NodeNote> notes) {
        this.notes = notes;
    }

    public ArrayList<NodeNote> getScopeNotes() {
        return scopeNotes;
    }

    public void setScopeNotes(ArrayList<NodeNote> scopeNotes) {
        this.scopeNotes = scopeNotes;
    }

    public ArrayList<NodeNote> getChangeNotes() {
        return changeNotes;
    }

    public void setChangeNotes(ArrayList<NodeNote> changeNotes) {
        this.changeNotes = changeNotes;
    }

    public ArrayList<NodeNote> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(ArrayList<NodeNote> definitions) {
        this.definitions = definitions;
    }

    public ArrayList<NodeNote> getEditorialNotes() {
        return editorialNotes;
    }

    public void setEditorialNotes(ArrayList<NodeNote> editorialNotes) {
        this.editorialNotes = editorialNotes;
    }

    public ArrayList<NodeNote> getExamples() {
        return examples;
    }

    public void setExamples(ArrayList<NodeNote> examples) {
        this.examples = examples;
    }

    public ArrayList<NodeNote> getHistoryNotes() {
        return historyNotes;
    }

    public void setHistoryNotes(ArrayList<NodeNote> historyNotes) {
        this.historyNotes = historyNotes;
    }
   
}
