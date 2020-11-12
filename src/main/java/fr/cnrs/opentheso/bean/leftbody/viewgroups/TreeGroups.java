/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.leftbody.viewgroups;

import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.DataService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author miledrousset
 */
@Named(value = "treeGroups")
@SessionScoped

public class TreeGroups implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private RightBodySetting rightBodySetting;
    @Inject
    ConceptView conceptView;
    @Inject
    GroupView groupView;
    @Inject
    private LeftBodySetting leftBodySetting;
    @Inject
    private SelectedTheso selectedTheso;


    private DataService dataService;

    private TreeNode root, selectedNode;

    private String idTheso, idLang;

    private boolean noedSelected;


    public void reset() {
        root = null;
        selectedNode = null;
        rightBodySetting.init();
        noedSelected = false;
    }

    public void initialise(String idTheso, String idLang) {
        this.idTheso = idTheso;
        this.idLang = idLang;
        dataService = new DataService();
        root = dataService.createRoot();
        addFirstNodes();
        noedSelected = false;
    }

    private boolean addFirstNodes() {

        // liste des groupes de premier niveau
        List<NodeGroup> racineNode = new GroupHelper().getListRootConceptGroup(connect.getPoolConnexion(), idTheso, idLang);

        for (NodeGroup nodeGroup : racineNode) {
            TreeNodeData data = new TreeNodeData(
                    nodeGroup.getConceptGroup().getIdgroup(),
                    nodeGroup.getLexicalValue(),
                    nodeGroup.getConceptGroup().getNotation(),
                    true,//isgroup
                    false,//isSubGroup
                    false,//isConcept
                    false,//isTopConcept
                    "group"
            );
            if (nodeGroup.isIsHaveChildren()) {
                dataService.addNodeWithChild("group", data, root);
            } else {
                dataService.addNodeWithoutChild("group", data, root);
            }
        }
        return true;
    }

    public TreeNode getRoot() {
        return root;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public void onNodeExpand(NodeExpandEvent event) {

        if (noedSelected) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "",
                    "Un noeud est en cours de chargement !"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
        } else {
            noedSelected = true;
            DefaultTreeNode parent = (DefaultTreeNode) event.getTreeNode();
            if (parent.getChildCount() == 1 && parent.getChildren().get(0).getData().toString().equals("DUMMY")) {
                parent.getChildren().remove(0);
                addGroupsChild(parent);
                addConceptsChild(parent);
            }
            noedSelected = false;
        }
    }

    private boolean addGroupsChild(TreeNode parent) {

        ArrayList<NodeGroup> listeSubGroup = new GroupHelper().getListChildsOfGroup(
                connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                idTheso,
                idLang);
        if (listeSubGroup == null) {
            parent.setType("group");
            return true;
        }

        for (NodeGroup nodeGroup : listeSubGroup) {
            TreeNodeData data = new TreeNodeData(
                    nodeGroup.getConceptGroup().getIdgroup(),
                    nodeGroup.getLexicalValue(),
                    nodeGroup.getConceptGroup().getNotation(),
                    false,//isgroup
                    true,//isSubGroup
                    false,//isConcept
                    false,//isTopConcept                    
                    "subGroup"
            );
            if (nodeGroup.isIsHaveChildren()) {
                dataService.addNodeWithChild("subGroup", data, parent);
            } else {
                dataService.addNodeWithoutChild("subGroup", data, parent);
            }
        }
        return true;
    }

    private boolean addConceptsChild(TreeNode parent) {
        ConceptHelper conceptHelper = new ConceptHelper();
        TreeNodeData data;

        ArrayList<NodeIdValue> listeConceptsOfGroup = conceptHelper.getListConceptsOfGroup(
                connect.getPoolConnexion(),
                idTheso,
                idLang,
                ((TreeNodeData) parent.getData()).getNodeId(),
                selectedTheso.isSortByNotation());
        if (listeConceptsOfGroup == null || listeConceptsOfGroup.isEmpty()) {
            parent.setType("group");
            return true;
        }
        int i = 0;
        for (NodeIdValue nodeGroup : listeConceptsOfGroup) {
            if (i == 2000) { // pour limiter l'affichage dans l'arbre de plus de 2000 concepts à la suite
                data = new TreeNodeData(
                        "....",
                        "....",
                        "",
                        false,//isgroup
                        false,//isSubGroup
                        true,//isConcept
                        false,//isTopConcept
                        "concept"
                );
                dataService.addNodeWithoutChild("file", data, parent);
                return true;
            }
            data = new TreeNodeData(
                    nodeGroup.getId(),
                    nodeGroup.getValue(),
                    nodeGroup.getNotation(),
                    false,//isgroup
                    false,//isSubGroup
                    true,//isConcept
                    false,//isTopConcept
                    "concept"
            );
            dataService.addNodeWithoutChild("file", data, parent);
            i++;
        }
        return true;
    }

    /**
     * pour l'ajout d'un nouveau Group après le chargement de l'arbre
     * @param idGroup
     * @param idTheso
     * @param idLang 
     */
    public void addNewGroupToTree(String idGroup, String idTheso, String idLang) {

        NodeGroup nodeGroup = new GroupHelper().getThisConceptGroup(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        if (nodeGroup == null) {
            return;
        }

        String label;
        if (nodeGroup.getLexicalValue().isEmpty()) {
            label = "(" + idGroup + ")";
        } else
            label = nodeGroup.getLexicalValue();

        TreeNodeData data = new TreeNodeData(
                idGroup,
                label,
                nodeGroup.getConceptGroup().getNotation(),
                true,//isgroup
                false,//isSubGroup
                false,//isConcept
                false,//isTopConcept
                "group"
        );
        dataService.addNodeWithoutChild("group", data, root);
    }
    
    /**
     * pour l'ajout d'un nouveau Group après le chargement de l'arbre
     * @param parent
     * @param idGroup
     * @param idTheso
     * @param idLang 
     */
    public void addNewSubGroupToTree(TreeNode parent, String idGroup, String idTheso, String idLang) {

        NodeGroup nodeGroup = new GroupHelper().getThisConceptGroup(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        if (nodeGroup == null) {
            return;
        }

        String label;
        if (nodeGroup.getLexicalValue().isEmpty()) {
            label = "(" + idGroup + ")";
        } else
            label = nodeGroup.getLexicalValue();

        TreeNodeData data = new TreeNodeData(
                idGroup,
                label,
                nodeGroup.getConceptGroup().getNotation(),
                false,//isgroup
                true,//isSubGroup
                false,//isConcept
                false,//isTopConcept
                "group"
        );
        dataService.addNodeWithoutChild("group", data, parent);
    }    

    public void selectThisGroup(String idGroup) {
        rightBodySetting.setShowGroupToOn();
        groupView.getGroup(idTheso, idGroup, idLang);
        rightBodySetting.setIndex("1");
        leftBodySetting.setIndex("2");
        if (selectedNode != null) {
            selectedNode.setSelected(false);
        }
    }

    public void onNodeSelect(NodeSelectEvent event) {
        if (noedSelected) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "",
                    "Un noeud est en cours de chargement !"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
        } else {
            noedSelected = true;
            if (((TreeNodeData) selectedNode.getData()).isIsConcept()) {
                rightBodySetting.setShowConceptToOn();
                conceptView.getConceptForTree(idTheso,
                        ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
                rightBodySetting.setIndex("0");
            }
            if (((TreeNodeData) selectedNode.getData()).isIsGroup() || ((TreeNodeData) selectedNode.getData()).isIsSubGroup()) {
                rightBodySetting.setShowGroupToOn();
                groupView.getGroup(idTheso, ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
                rightBodySetting.setIndex("1");
            }
            noedSelected = false;
        }
    }
    
    public String getNodeLabel(){
        if(selectedNode == null) return null;
        return ((TreeNodeData) selectedNode.getData()).getName();
    }
    
    public String getSelectedNodeId(){
        if(selectedNode == null) return null;
        return ((TreeNodeData) selectedNode.getData()).getNodeId();
    }    
    
    public boolean isIsGroupNode(){
        if(selectedNode == null) return false;
        return ((TreeNodeData) selectedNode.getData()).isIsGroup() || ((TreeNodeData) selectedNode.getData()).isIsSubGroup();
    }    
    
    /**
     * permet de savoir si le groupe/collection a un sous group/collection
     * @return 
     */
    public boolean isIsThisGroupHaveSubGroup(){
        if(selectedNode == null) return false;
        GroupHelper groupHelper = new GroupHelper();
        return groupHelper.isHaveSubGroup(connect.getPoolConnexion(),
                idTheso,
                ((TreeNodeData) selectedNode.getData()).getNodeId());
    }       
    
    
    

    public boolean isNoedSelected() {
        return noedSelected;
    }

    public void setNoedSelected(boolean noedSelected) {
        this.noedSelected = noedSelected;
    }
}
