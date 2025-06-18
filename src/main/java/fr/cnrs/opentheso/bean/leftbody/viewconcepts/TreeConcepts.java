package fr.cnrs.opentheso.bean.leftbody.viewconcepts;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
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
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.RelationService;
import fr.cnrs.opentheso.services.ResourceService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "treeConcepts")
public class TreeConcepts implements Serializable {

    private final GroupView groupView;
    private final CurrentUser currentUser;
    private final ConceptView conceptView;
    private final GroupService groupService;
    private final SelectedTheso selectedTheso;
    private final PropositionBean propositionBean;
    private final ConceptService conceptService;
    private final RelationService relationService;
    private final ResourceService resourceService;
    private final RightBodySetting rightBodySetting;

    private DataService dataService;
    private TreeNode root, selectedNode;
    private String idTheso, idLang;


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
        var listeConceptsOfGroup = conceptService.getListTopConceptsOfGroup(idTheso, idLang,
                ((TreeNodeData) parent.getData()).getNodeId(), selectedTheso.isSortByNotation());
        
        if (listeConceptsOfGroup == null || listeConceptsOfGroup.isEmpty()) {
            parent.setType("group");
            return true;
        }

        for (NodeIdValue nodeGroup : listeConceptsOfGroup) {

            TreeNodeData data = new TreeNodeData(nodeGroup.getId(), nodeGroup.getValue(), "", false, false,
                    true, false, "concept");

            var childs = resourceService.getListNT(nodeGroup.getId(), idTheso, idLang, -1, -1);
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

        var ConceptsId = relationService.getListNT(((TreeNodeData) parent.getData()).getNodeId(), idTheso, idLang, -1, -1);
        if (ConceptsId == null || ConceptsId.isEmpty()) {
            parent.setType("file");
            return true;
        }

        for (NodeNT nodeNT : ConceptsId) {
            TreeNodeData data = new TreeNodeData(nodeNT.getIdConcept(), nodeNT.getTitle(),"", false,
                    false, true, true,"concept" );
            
            var childs = resourceService.getListNT(nodeNT.getIdConcept(), idTheso, idLang, -1, -1);

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
