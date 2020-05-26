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
import javax.faces.application.FacesMessage;

import javax.faces.context.FacesContext;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
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

    private DataService dataService;

    private TreeNode selectedNode;
    private TreeNode root;

    private String idTheso;
    private String idLang;

    @PostConstruct
    public void init() {
//      initialise("th44", "fr");
    }

    public void reset() {
        root = null;
        selectedNode = null;
        rightBodySetting.init();
    }

    public void initialise(String idTheso, String idLang) {
        this.idTheso = idTheso;
        this.idLang = idLang;
        dataService = new DataService();
        root = dataService.createRoot();
        addFirstNodes();
    }

    private boolean addFirstNodes() {
        GroupHelper groupHelper = new GroupHelper();
        TreeNodeData data;

        // liste des groupes de premier niveau
        List<NodeGroup> racineNode = groupHelper.getListRootConceptGroup(
                connect.getPoolConnexion(),
                idTheso,
                idLang);
        for (NodeGroup nodeGroup : racineNode) {
            data = new TreeNodeData(
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
        DefaultTreeNode parent = (DefaultTreeNode) event.getTreeNode();
        if (parent.getChildCount() == 1 && parent.getChildren().get(0).getData().toString().equals("DUMMY")) {
            parent.getChildren().remove(0);
            addGroupsChild(parent);
            addConceptsChild(parent);
        }
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Expanded", event.getTreeNode().toString());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    private boolean addGroupsChild(TreeNode parent) {
        GroupHelper groupHelper = new GroupHelper();
        TreeNodeData data;
        ArrayList<NodeGroup> listeSubGroup = groupHelper.getListChildsOfGroup(
                connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                idTheso,
                idLang);
        if (listeSubGroup == null) {
            parent.setType("group");
            return true;
        }
        for (NodeGroup nodeGroup : listeSubGroup) {
            data = new TreeNodeData(
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
                false);
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
                    "",
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

    /////// pour l'ajout d'un nouveau Group après le chargement de l'arbre
    public void addNewGroupChild(String idGroup, String idTheso, String idLang) {
        GroupHelper groupHelper = new GroupHelper();
        TreeNodeData data;
        
        NodeGroup nodeGroup = groupHelper.getThisConceptGroup(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        if(nodeGroup == null) return;
        
        String label;
        if (nodeGroup.getLexicalValue().isEmpty()) {
            label = "(" + idGroup + ")";
        } else
            label = nodeGroup.getLexicalValue();
        
        data = new TreeNodeData(
                idGroup,
                label,
                nodeGroup.getConceptGroup().getNotation(),
                false,//isgroup
                false,//isSubGroup
                true,//isConcept
                false,//isTopConcept
                "group"
        );
        dataService.addNodeWithoutChild("group", data, root);
    }

    public void onNodeCollapse(NodeCollapseEvent event) {
        /*      FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Collapsed", event.getTreeNode().toString());
        FacesContext.getCurrentInstance().addMessage(null, message);
         */
    }

    public void onNodeSelect(NodeSelectEvent event) {
        if (((TreeNodeData) selectedNode.getData()).isIsConcept()) {
            rightBodySetting.setShowConceptToOn();
            conceptView.getConcept(idTheso,
                    ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
            rightBodySetting.setIndex("0");
        }
        if (((TreeNodeData) selectedNode.getData()).isIsGroup() || ((TreeNodeData) selectedNode.getData()).isIsSubGroup()) {
            rightBodySetting.setShowGroupToOn();
            groupView.getGroup(idTheso, ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
            rightBodySetting.setIndex("1");            
        }


        /// test pour modifier le label du node, il suffit de renommer le node et ca marche automatiquement
        /// ((TreeNodeData)selectedNode.getData()).setName("name2");
        ///

        /*    externalResources.loadImages(idTheso, ((TreeNodeData)selectedNode.getData()).getNodeId());
        MyTreeNode myTreeNode = new MyTreeNode(3, ((TreeNodeData)selectedNode.getData()).getNodeId(),
                idTheso,
                idLang,
                "", "", "", null, null, null);
        selectedTerme.majTerme(myTreeNode);
        newTreeBean.setSelectedNode(myTreeNode);*/
    }

    public void onNodeUnselect(NodeUnselectEvent event) {
        /*    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Unselected", event.getTreeNode().toString());
        FacesContext.getCurrentInstance().addMessage(null, message);*/
    }

}
