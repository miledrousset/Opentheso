package fr.cnrs.opentheso.bean.leftbody.viewconcepts;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.terms.NodeNT;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.bean.leftbody.DataService;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;

import fr.cnrs.opentheso.services.GroupService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import jakarta.enterprise.context.SessionScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miledrousset
 */
@Named(value = "treeConcepts")
@SessionScoped
public class TreeConcepts implements Serializable {

    @Autowired @Lazy private RightBodySetting rightBodySetting;
    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private ConceptView conceptView;
    @Autowired @Lazy private GroupView groupView;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private PropositionBean propositionBean;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    private DataService dataService;
    private TreeNode root, selectedNode;
    private String idTheso, idLang;
    @Autowired
    private GroupService groupService;


    public void reset() {
        root = null;
        selectedNode = null;
        rightBodySetting.init();
        dataService = null;
        idTheso = null;
        idLang = null;
    }

    public void initialise(String idTheso, String idLang) {
        this.idTheso = idTheso;
        this.idLang = idLang;
        dataService = new DataService();
        root = dataService.createRoot();
        addFirstNodes();
    }

    private boolean addFirstNodes() {

        // liste des groupes de premier niveau
        List<NodeGroup> racineNode = groupService.getListRootConceptGroup(idTheso, idLang, selectedTheso.isSortByNotation(),
                ObjectUtils.isEmpty(currentUser.getNodeUser()));

        for (NodeGroup nodeGroup : racineNode) {
            TreeNodeData data = new TreeNodeData(nodeGroup.getConceptGroup().getIdGroup(), nodeGroup.getLexicalValue(),
                    nodeGroup.getConceptGroup().getNotation(),true,false,false,false,"group");

            if (nodeGroup.isHaveChildren()) {
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
        if (parent.getChildCount() == 1 && ((TreeNode)parent.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            parent.getChildren().remove(0);
            addGroupsChild(parent);
            addConceptsChild(parent);
        }
        addConceptSpecifique(parent);
    }

    private boolean addGroupsChild(TreeNode parent) {
        var listeSubGroup = groupService.getListChildsOfGroup(((TreeNodeData) parent.getData()).getNodeId(), idTheso, idLang, selectedTheso.isSortByNotation());

        if (listeSubGroup == null) {
            parent.setType("group");
            return true;
        }

        for (NodeGroup nodeGroup : listeSubGroup) {
            TreeNodeData data = new TreeNodeData(nodeGroup.getConceptGroup().getIdGroup(), nodeGroup.getLexicalValue(),
                    nodeGroup.getConceptGroup().getNotation(),false,true,false,false,"subGroup");

            if (nodeGroup.isHaveChildren()) {
                dataService.addNodeWithChild("subGroup", data, parent);
            } else {
                dataService.addNodeWithoutChild("subGroup", data, parent);
            }
        }
        return true;
    }

    private boolean addConceptsChild(TreeNode parent) {
        
        // il faut ici récupérer les Topterms de la collection #MR        
        ArrayList<NodeIdValue> listeConceptsOfGroup = conceptHelper.getListTopConceptsOfGroup(idTheso, idLang,
                ((TreeNodeData) parent.getData()).getNodeId(), selectedTheso.isSortByNotation());
        
        if (listeConceptsOfGroup == null || listeConceptsOfGroup.isEmpty()) {
            parent.setType("group");
            return true;
        }

        for (NodeIdValue nodeGroup : listeConceptsOfGroup) {

            TreeNodeData data = new TreeNodeData(nodeGroup.getId(), nodeGroup.getValue(), "", false, false,
                    true, false, "concept");

            ArrayList<NodeNT> childs = relationsHelper.getListNT(nodeGroup.getId(), idTheso, idLang, -1, -1);
            if (CollectionUtils.isEmpty(childs)) {
                new DefaultTreeNode("file", data, parent);
            } else {
                TreeNode document = new DefaultTreeNode(data, parent);
                new DefaultTreeNode("DUMMY", document);
            }

        }
        return true;
    }

    private boolean addConceptSpecifique(TreeNode parent) {

        ArrayList<NodeNT> ConceptsId = relationsHelper.getListNT(
                ((TreeNodeData) parent.getData()).getNodeId(), idTheso, idLang, -1, -1);

        if (ConceptsId == null || ConceptsId.isEmpty()) {
            parent.setType("file");
            return true;
        }

        for (NodeNT nodeNT : ConceptsId) {
            TreeNodeData data = new TreeNodeData(nodeNT.getIdConcept(), nodeNT.getTitle(),"", false,
                    false, true, true,"concept" );
            
            ArrayList<NodeNT> childs = relationsHelper.getListNT(nodeNT.getIdConcept(),
                    idTheso, idLang, -1, -1);

            if (CollectionUtils.isEmpty(childs)) {
                new DefaultTreeNode("file", data, parent);
            } else {
                TreeNode document = new DefaultTreeNode(data, parent);
                new DefaultTreeNode("DUMMY", document);
            }
        }
        return true;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        
        propositionBean.setRubriqueVisible(false);
        propositionBean.setNewProposition(false);
        rightBodySetting.setIndex("0");
        
        if (((TreeNodeData) selectedNode.getData()).isIsConcept()) {
            rightBodySetting.setShowConceptToOn();
            conceptView.getConceptForTree(idTheso,
                    ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang, currentUser);
            rightBodySetting.setIndex("0");
        }
        if (((TreeNodeData) selectedNode.getData()).isIsGroup() || ((TreeNodeData) selectedNode.getData()).isIsSubGroup()) {
            rightBodySetting.setShowGroupToOn();
            groupView.getGroup(idTheso, ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
            rightBodySetting.setIndex("1");
        }
    }

}
