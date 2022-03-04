package fr.cnrs.opentheso.bean.leftbody.viewtree;

import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bean.facet.EditFacet;

import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.DataService;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PathHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bean.alignment.AlignmentBean;
import fr.cnrs.opentheso.bean.alignment.AlignmentManualBean;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author miledrousset
 */
@Named(value = "tree")
@SessionScoped
public class Tree implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private RightBodySetting rightBodySetting;

    @Inject
    private LeftBodySetting leftBodySetting;

    @Inject
    private ConceptView conceptBean;

    @Inject
    private SelectedTheso selectedTheso;

    @Inject
    private RoleOnThesoBean roleOnThesoBean;

    @Inject
    private IndexSetting indexSetting;

    @Inject
    private EditFacet editFacet;

    @Inject
    private AlignmentBean alignmentBean;

    @Inject
    private AlignmentManualBean alignmentManualBean;

    private DataService dataService;
    private TreeNode selectedNode; // le neoud sélectionné par clique
    private TreeNode root;
    private String idTheso, idConceptParent, idLang, idConceptSelected;
    private TreeNodeData treeNodeDataSelect;
    private ArrayList<TreeNode> selectedNodes; // enregistre les noeuds séléctionnés apres une recherche

    private boolean manySiblings = false;

    @PreDestroy
    public void destroy() {
        reset();
    }

    public void reset() {
        if (selectedNodes != null) {
            selectedNodes.clear();
            selectedNodes = null;
        }
        root = null;
        selectedNode = null;
        rightBodySetting.init();
        dataService = null;
        treeNodeDataSelect = null;
        idTheso = null;
        idConceptParent = null;
        idLang = null;
        manySiblings = false;
    }

    public void initialise(String idTheso, String idLang) {
        manySiblings = false;
        this.idTheso = idTheso;
        this.idLang = idLang;
        selectedTheso.setSelectedLang(idLang);
        selectedTheso.setCurrentLang(idLang);

        dataService = new DataService();
        root = dataService.createRoot();

        addFirstNodes();

        selectedNodes = new ArrayList<>();
        leftBodySetting.setIndex("0");
    }

    public boolean isDragAndDrop(NodeUser nodeUser) {
        if (nodeUser == null) {
            return false;
        }
        if (roleOnThesoBean == null) {
            return false;
        }
        if (roleOnThesoBean.isIsSuperAdmin() || roleOnThesoBean.isIsAdminOnThisTheso() || roleOnThesoBean.isIsManagerOnThisTheso()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean addFirstNodes() {
        ConceptHelper conceptHelper = new ConceptHelper();
        TreeNodeData data;

        // la liste est triée par alphabétique ou notation
        ArrayList<NodeConceptTree> nodeConceptTrees
                = conceptHelper.getListOfTopConcepts(connect.getPoolConnexion(),
                        idTheso, idLang, selectedTheso.isSortByNotation());

        if (nodeConceptTrees.size() >= 2000) {
            manySiblings = true;
        }

        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            data = new TreeNodeData(
                    nodeConceptTree.getIdConcept(),
                    nodeConceptTree.getTitle(),
                    nodeConceptTree.getNotation(),
                    false,//isgroup
                    false,//isSubGroup
                    false,//isConcept
                    true,//isTopConcept
                    "topTerm"
            );
            if (nodeConceptTree.isHaveChildren()) {
                dataService.addNodeWithChild("concept", data, root);
            } else {
                dataService.addNodeWithoutChild("file", data, root);
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
        leftBodySetting.setIndex("0");
        manySiblings = false;

        DefaultTreeNode parent = (DefaultTreeNode) event.getTreeNode();

        if (parent.getChildCount() == 1 && ((TreeNode) parent.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            parent.getChildren().remove(0);
            idConceptParent = ((TreeNodeData) parent.getData()).getNodeId();
            FacetHelper facetHelper = new FacetHelper();
            if ("facet".equals(parent.getType())) {
                addMembersOfFacet(parent);
            } else {
                if (facetHelper.isConceptHaveFacet(connect.getPoolConnexion(), idConceptParent, idTheso)) {
                    addConceptsChildWithFacets(parent);
                } else {
                    addConceptsChild(parent);
                }
            }
        }
    }

    public SelectedTheso getSelectedTheso() {
        return selectedTheso;
    }

    /// l'évennement ne focntionne pas avec tree dynamic="true"
    public void onNodeCollapse(NodeCollapseEvent event) {
        leftBodySetting.setIndex("0");
        event.getTreeNode().setExpanded(false);
    }

    private void addMembersOfFacet(TreeNode parent) {
        List<String> list = new FacetHelper().getAllMembersOfFacetSorted(connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                selectedTheso.getCurrentLang(),
                idTheso);

        TermHelper termHelper = new TermHelper();
        ConceptHelper conceptHelper = new ConceptHelper();
        list.stream().forEach(idConcept1 -> {

            Term term = termHelper.getThisTerm(connect.getPoolConnexion(), idConcept1, idTheso,
                    selectedTheso.getCurrentLang());

            TreeNodeData data = new TreeNodeData(
                    idConcept1,
                    term.getLexical_value().isEmpty() ? "(" + idConcept1 + ")" : term.getLexical_value(),
                    null,
                    false,
                    false,
                    true,
                    false,
                    "facetMember");
            data.setIdFacetParent(((TreeNodeData) parent.getData()).getNodeId());
            boolean haveConceptChild = conceptHelper.haveChildren(connect.getPoolConnexion(), idTheso,
                    idConcept1);
            if (haveConceptChild) {
                dataService.addNodeWithChild("concept", data, parent);
            } else {
                dataService.addNodeWithoutChild("file", data, parent);
            }
        });
    }

    private boolean addConceptsChild(TreeNode parent) {
        ConceptHelper conceptHelper = new ConceptHelper();
        FacetHelper facetHelper = new FacetHelper();

        ArrayList<NodeConceptTree> nodeConceptTrees = conceptHelper.getListConcepts(
                connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                idTheso,
                selectedTheso.getCurrentLang(),
                selectedTheso.isSortByNotation());

        if (nodeConceptTrees.size() >= 2000) {
            manySiblings = true;
        }

//        boolean haveConceptChild;
//        boolean haveFacet;
        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            if (nodeConceptTree.getIdConcept() == null) {
                continue;
            }

            TreeNodeData data = new TreeNodeData(
                    nodeConceptTree.getIdConcept(),
                    nodeConceptTree.getTitle().isEmpty() ? "(" + nodeConceptTree.getIdConcept() + ")" : nodeConceptTree.getTitle(),
                    nodeConceptTree.getNotation(),
                    false,//isgroup
                    false,//isSubGroup
                    true,//isConcept
                    false,//isTopConcept
                    "term"
            );

            if (conceptHelper.haveChildren(connect.getPoolConnexion(), idTheso,
                    nodeConceptTree.getIdConcept())) {
                dataService.addNodeWithChild("concept", data, parent);
                continue;
            }
            if (facetHelper.isConceptHaveFacet(connect.getPoolConnexion(), nodeConceptTree.getIdConcept(), idTheso)) {
                dataService.addNodeWithChild("concept", data, parent);
                continue;
            }
            dataService.addNodeWithoutChild("file", data, parent);

//            haveConceptChild = conceptHelper.haveChildren(connect.getPoolConnexion(), idTheso,
//                    nodeConceptTree.getIdConcept());
//
//            haveFacet = facetHelper.isConceptHaveFacet(connect.getPoolConnexion(), nodeConceptTree.getIdConcept(), idTheso);
//
//            if (haveConceptChild || haveFacet) {
//                dataService.addNodeWithChild("concept", data, parent);
//            } else {
//                dataService.addNodeWithoutChild("file", data, parent);
//            }
        }
        return true;
    }

    private boolean addConceptsChildWithFacets(TreeNode parent) {
        ConceptHelper conceptHelper = new ConceptHelper();

        ArrayList<NodeConceptTree> nodeConceptTrees = conceptHelper.getListConceptsIgnoreConceptsInFacets(
                connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                idTheso,
                selectedTheso.getCurrentLang(),
                selectedTheso.isSortByNotation());

        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            if (nodeConceptTree.getIdConcept() == null) {
                continue;
            }

            TreeNodeData data = new TreeNodeData(
                    nodeConceptTree.getIdConcept(),
                    nodeConceptTree.getTitle().isEmpty() ? "(" + nodeConceptTree.getIdConcept() + ")" : nodeConceptTree.getTitle(),
                    nodeConceptTree.getNotation(),
                    false,//isgroup
                    false,//isSubGroup
                    true,//isConcept
                    false,//isTopConcept
                    "term"
            );

            boolean haveConceptChild = conceptHelper.haveChildren(connect.getPoolConnexion(), idTheso,
                    nodeConceptTree.getIdConcept());
            if (haveConceptChild) {
                dataService.addNodeWithChild("concept", data, parent);
            } else {
                dataService.addNodeWithoutChild("file", data, parent);
            }
        }

        addFacettes(parent);

        return true;
    }

    /////// pour l'ajout d'un fils supplementaire après un ajout de concept
    public void addNewChild(TreeNode parent, String idConcept, String idTheso, String idLang) {
        ConceptHelper conceptHelper = new ConceptHelper();
        TreeNodeData data;
        String label = conceptHelper.getLexicalValueOfConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang);
        if (label == null || label.isEmpty()) {
            label = "(" + idConcept + ")";
        }
        data = new TreeNodeData(
                idConcept,
                label,
                "",
                false,//isgroup
                false,//isSubGroup
                true,//isConcept
                false,//isTopConcept
                "term"
        );
        if (conceptHelper.haveChildren(connect.getPoolConnexion(), idTheso, idConcept)) {
            dataService.addNodeWithChild("concept", data, parent);
        } else {
            dataService.addNodeWithoutChild("file", data, parent);
        }

    }

    public void addNewFacet(TreeNode parent, String facetName, String idFacet) {
        TreeNodeData data = new TreeNodeData(
                idFacet,
                facetName,
                "",
                false,//isgroup
                false,//isSubGroup
                true,//isConcept
                false,//isTopConcept
                "facet");

        dataService.addNodeWithoutChild("facet", data, parent);
    }

    private void addFacettes(TreeNode parent) {
        FacetHelper facetHelper = new FacetHelper();
        List<NodeIdValue> nodeIdValues = facetHelper.getAllIdValueFacetsOfConcept(
                connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                idTheso,
                selectedTheso.getCurrentLang());
        nodeIdValues.stream().forEach(facette -> {
            TreeNodeData data = new TreeNodeData(
                    facette.getId() + "",
                    facette.getValue().isEmpty() ? "(" + facette.getId() + ")" : facette.getValue(),
                    null,
                    false,
                    false,
                    true,
                    false,
                    "facet"
            );

            if (facetHelper.isFacetHaveMembers(connect.getPoolConnexion(), facette.getId(), idTheso)) {
                dataService.addNodeWithChild("facet", data, parent);
            } else {
                dataService.addNodeWithoutChild("facet", data, parent);
            }
        });
    }

    public void onNodeSelect(NodeSelectEvent event) {
        
        alignmentManualBean.reset();
        
        leftBodySetting.setIndex("0");
        DefaultTreeNode parent = (DefaultTreeNode) event.getTreeNode();
        treeNodeDataSelect = (TreeNodeData) selectedNode.getData();

        if (!"facet".equals(parent.getType())) {
            indexSetting.setIsFacetSelected(false);
            idConceptParent = ((TreeNodeData) selectedNode.getData()).getNodeId();

            /*    if (((TreeNodeData) selectedNode.getData()).isIsConcept()) {
                rightBodySetting.setShowConceptToOn();
                conceptBean.getConceptForTree(idTheso,
                        ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
            }
            if (((TreeNodeData) selectedNode.getData()).isIsTopConcept()) {*/
            rightBodySetting.setShowConceptToOn();
            conceptBean.getConceptForTree(idTheso,
                    ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
            //     }

            idConceptSelected = ((TreeNodeData) selectedNode.getData()).getNodeId();
            rightBodySetting.setIndex("0");
        } else {
            indexSetting.setIsFacetSelected(true);
            editFacet.initEditFacet(((TreeNodeData) parent.getData()).getNodeId(), idTheso, idLang);
        }

        /*
        Il ne faut pas charger le tableau d'alignement ici, mais plutôt au moment où l'utilisateur clique sur l'onglet alignement (voir après avoir sélectionné la source d'alignement)
        
        Désactivé pat MR, ca prend trop de temps quand on a 40.000 concepts dans la branche
         */
        ////alignmentBean.initAlignementByStep(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), conceptBean.getSelectedLang());
        ////alignmentBean.nextTenRecresive(conceptBean.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        /////// 
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        PrimeFaces.current().ajax().update("indexTitle");
        PrimeFaces.current().ajax().update("containerIndex:formLeftTab:tabTree:graph");
    }

    public void onTabConceptChange(TabChangeEvent event) {
        if (event.getTab().getId().equals("viewTabAlignement")) {
            rightBodySetting.setIndex("3");
            indexSetting.setIsValueSelected(true);            
            
            alignmentBean.initAlignementByStep(selectedTheso.getCurrentIdTheso(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());
            
            alignmentBean.nextTen(conceptBean.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        }
        if (event.getTab().getId().equals("viewTabSearch")) {
            rightBodySetting.setIndex("2");
            indexSetting.setIsValueSelected(true);            
        }    
        if (event.getTab().getId().equals("viewTabGroup")) {
            rightBodySetting.setIndex("1");
            indexSetting.setIsValueSelected(true);            
        }         
        if (event.getTab().getId().equals("viewTabConcept")) {
            rightBodySetting.setIndex("0");
            indexSetting.setIsValueSelected(true);            
        }          
    }
    public void onTabTreeChange(TabChangeEvent event) {
        if (event.getTab().getId().equals("viewTabTree")) {
            leftBodySetting.setIndex("0");
        }
        if (event.getTab().getId().equals("viewTabList")) {
            leftBodySetting.setIndex("1");
        }
        if (event.getTab().getId().equals("viewTabGroups")) {
            leftBodySetting.setIndex("2");
        }       
        if (event.getTab().getId().equals("viewTabConceptTree")) {
            leftBodySetting.setIndex("3");
        }        
    }
    
    
    
    public String getIdConceptSelected() {
        return idConceptSelected;
    }

    public boolean isGrapheLinkVisible() {
        return !StringUtils.isEmpty(idConceptSelected);
    }

    /**
     * permet de déplier l'arbre suivant le Path ou les paths en paramètre
     *
     * @param idConcept
     * @param idTheso
     * @param idLang #MR
     */
    public void expandTreeToPath(String idConcept, String idTheso, String idLang) {

        ArrayList<Path> paths = new PathHelper().getPathOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);

        if (root == null) {
            initialise(idTheso, idLang);
        }

        // deselectionner et fermer toutes les noeds de l'arbres
//        initialiserEtatNoeuds(root);
        // cas de changement de langue pendant la navigation dans les concepts
        // il faut reconstruire l'arbre dès le début
        if (idLang != null && !idLang.equalsIgnoreCase(this.idLang)) {
            initialise(idTheso, idLang);
        }

        if (!paths.isEmpty()) {
            // pour déselectionner les noeuds avant de séléctionner le neoud trouvé
            selectedNodes.forEach((selectedNode1) -> {
                selectedNode1.setSelected(false);
            });
            if (selectedNode != null) {
                selectedNode.setSelected(false);
            }
            selectedNodes.clear();
        }

        TreeNode treeNodeParent = root;
        treeNodeParent.setExpanded(true);
        for (Path thisPath : paths) {
            for (String idC : thisPath.getPath()) {
                treeNodeParent = selectChildNode(treeNodeParent, idC);
                if (treeNodeParent == null) {
                    // erreur de cohérence
                    return;
                }
                // compare le dernier élément au concept en cours, si oui, on expand pas, sinon, erreur ...
                if (!((TreeNodeData) treeNodeParent.getData()).getNodeId().equalsIgnoreCase(thisPath.getPath().get(thisPath.getPath().size() - 1))) {
                    treeNodeParent.setExpanded(true);
                }
            }
            treeNodeParent.setSelected(true);
            selectedNodes.add(treeNodeParent);
            selectedNode = treeNodeParent;
            treeNodeParent = root;
        }
    //    leftBodySetting.setIndex("0");
        PrimeFaces.current().executeScript("srollToSelected();");
    }

    public void expandTreeToPath2(String idConcept, String idTheso, String idLang, String idFacette) {

        ArrayList<Path> paths = new PathHelper().getPathOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);
        paths.get(0).getPath().add(idFacette);

        if (root == null) {
            initialise(idTheso, idLang);
        }

        if (idLang != null && !idLang.equalsIgnoreCase(this.idLang)) {
            initialise(idTheso, idLang);
        }

        if (!paths.isEmpty()) {
            // pour déselectionner les noeuds avant de séléctionner le neoud trouvé
            selectedNodes.forEach((selectedNode1) -> {
                selectedNode1.setSelected(false);
            });
            if (selectedNode != null) {
                selectedNode.setSelected(false);
            }
            selectedNodes.clear();
        }

        TreeNode treeNodeParent = root;
        treeNodeParent.setExpanded(true);
        for (Path thisPath : paths) {
            for (String idC : thisPath.getPath()) {
                treeNodeParent = selectChildNode(treeNodeParent, idC);
                if (treeNodeParent == null) {
                    // erreur de cohérence
                    return;
                }
                // compare le dernier élément au concept en cours, si oui, on expand pas, sinon, erreur ...
                if (!((TreeNodeData) treeNodeParent.getData()).getNodeId().equalsIgnoreCase(thisPath.getPath().get(thisPath.getPath().size() - 1))) {
                    treeNodeParent.setExpanded(true);
                }
            }
            treeNodeParent.setSelected(true);
            selectedNodes.add(treeNodeParent);
            selectedNode = treeNodeParent;
            treeNodeParent = root;
        }
        leftBodySetting.setIndex("0");
        PrimeFaces.current().executeScript("srollToSelected();");
    }

    // deselectionner et fermer toutes les noeds de l'arbres
 /*   private void initialiserEtatNoeuds(TreeNode nodeRoot) {
        for (TreeNode node :  List<TreeNode<TreeNode>> nodeRoot.getChildren()) {
            try {
                TreeNodeData treeNodeData = (TreeNodeData) node.getData();
                node.setExpanded(false);
                node.setSelected(false);

                if (!treeNodeData.isIsConcept()) {
                    initialiserEtatNoeuds(node);
                }
            } catch (Exception ex) {

            }
        }
    }*/

    /**
     * permet de déplier l'arbre suivant le Path ou les paths en paramètre On
     * reconstruit l'arbre dès le début suite à des modifications
     *
     * @param idConcept
     * @param idTheso
     * @param idLang #MR
     */
    public void initAndExpandTreeToPath(
            String idConcept,
            String idTheso,
            String idLang) {

        ArrayList<Path> paths = new PathHelper().getPathOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);

        initialise(idTheso, idLang);

        if (!paths.isEmpty()) {
            // pour déselectionner les noeuds avant de séléctionner le neoud trouvé
            selectedNodes.forEach((selectedNode1) -> {
                selectedNode1.setSelected(false);
            });
            if (selectedNode != null) {
                selectedNode.setSelected(false);
            }
            selectedNodes.clear();
        }

        TreeNode treeNodeParent = root;
        treeNodeParent.setExpanded(true);
        for (Path thisPath : paths) {
            for (String idC : thisPath.getPath()) {
                treeNodeParent = selectChildNode(treeNodeParent, idC);
                if (treeNodeParent == null) {
                    // erreur de cohérence
                    return;
                }
                // compare le dernier élément au concept en cours, si oui, on expand pas, sinon, erreur ...
                if (!((TreeNodeData) treeNodeParent.getData()).getNodeId()
                        .equalsIgnoreCase(thisPath.getPath().get(thisPath.getPath().size() - 1))) {
                    treeNodeParent.setExpanded(true);
                }
            }
            treeNodeParent.setSelected(true);
            selectedNodes.add(treeNodeParent);
            selectedNode = treeNodeParent;
            treeNodeParent = root;
        }
        leftBodySetting.setIndex("0");
    }

    /**
     * permet de controler si la branche est chargée, on se positionne sur le
     * concept, sinon, on récupère les fils et on se positionne sur le concept
     * Cas d'un noeud Facette : on zappe le neoud puisque le concept est sous
     * cette facette, ensuite, on se positionne sur le concept
     *
     * @param treeNodeParent
     * @param idConceptChildToFind
     * @return
     */
    private TreeNode selectChildNode(TreeNode treeNodeParent, String idConceptChildToFind) {
        // test si les fils ne sont pas construits
        FacetHelper facetHelper = new FacetHelper();
        if (treeNodeParent.getChildCount() == 1 && ((TreeNode) treeNodeParent.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            treeNodeParent.getChildren().remove(0);

            if ("facet".equals(treeNodeParent.getType())) {
                addMembersOfFacet(treeNodeParent);
            } else {
                if (facetHelper.isConceptHaveFacet(connect.getPoolConnexion(),
                        ((TreeNodeData) treeNodeParent.getData()).getNodeId(), idTheso)) {
                    addConceptsChildWithFacets(treeNodeParent);
                } else {
                    addConceptsChild(treeNodeParent);
                }
            }
        }
        List<TreeNode> treeNodes = treeNodeParent.getChildren();

        for (TreeNode treeNode : treeNodes) {
            //// permet de sauter le noeud de type Facette et passer au concept membre
            if (((TreeNodeData) treeNode.getData()).getNodeType().equalsIgnoreCase("facet")) {
                try {
                    //String idFacet = ((TreeNodeData) treeNode.getData()).getNodeId();
                    if (facetHelper.isFacetHaveThisMember(connect.getPoolConnexion(),
                            ((TreeNodeData) treeNode.getData()).getNodeId(),
                            idConceptChildToFind, idTheso)) {
                        if (treeNode.getChildCount() == 1 && ((TreeNode) treeNode.getChildren().get(0)).getData().toString().equals("DUMMY")) {
                            treeNode.getChildren().remove(0);
                            addMembersOfFacet(treeNode);
                        }
                        return selectChildNodeFromFacet(treeNode, idConceptChildToFind);
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            //// fin 

            if (((TreeNodeData) treeNode.getData()).getNodeId().equalsIgnoreCase(idConceptChildToFind)) {
                return treeNode;
            }
        }
        // pas de noeud trouvé dans les fils
        return null;
    }

    private TreeNode selectChildNodeFromFacet(TreeNode treeNodeFacet, String idConceptChildToFind) {
        List<TreeNode> treeNodes = treeNodeFacet.getChildren();

        for (TreeNode treeNode : treeNodes) {
            if (((TreeNodeData) treeNode.getData()).getNodeId().equalsIgnoreCase(idConceptChildToFind)) {
                treeNodeFacet.setExpanded(true);
                return treeNode;
            }
        }
        // pas de noeud trouvé dans les fils
        return null;
    }

    public DataService getDataService() {
        return dataService;
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public String getIdConcept() {
        return idConceptParent;
    }

    public void setIdConcept(String idConcept) {
        this.idConceptParent = idConcept;
    }

    /*    public void showDiagram(boolean status) throws IOException {
        if (treeNodeDataSelect == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "", "Vous devez selectioner un élement de l'arbre"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
            return;
        }

        diagramVisisble = status;
        if(status)
            conceptsDiagramBean.init(treeNodeDataSelect.getNodeId(), idTheso, idLang);
        else
            conceptsDiagramBean.clear();

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }*/
    public TreeNodeData getTreeNodeDataSelect() {
        return treeNodeDataSelect;
    }

    public void setTreeNodeDataSelect(TreeNodeData treeNodeDataSelect) {
        this.treeNodeDataSelect = treeNodeDataSelect;
    }

    public boolean isManySiblings() {
        return manySiblings;
    }

    public void setManySiblings(boolean manySiblings) {
        this.manySiblings = manySiblings;
    }

//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
////// pour tester la mémoire occupée par l'arbre ////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
    /**
     * permet de déplier tout l'arbre
     *
     * #MR
     */
    public void expandAllNode() {
        dataService = null;
        dataService = new DataService();
        root = dataService.createRoot();
        addFirstNodes();
        List<TreeNode> treeNodes = root.getChildren();

        for (TreeNode treeNode : treeNodes) {
            expandedAllRecursively(treeNode, true);
        }
    }

    private void expandedAllRecursively(TreeNode node, boolean expanded) {
        if (node.getChildCount() == 1 && ((TreeNode) node.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            node.getChildren().remove(0);
            addConceptsChild(node);
        }
/*        for (TreeNode child : node.getChildren()) {

//            addConceptsChild(node);
            expandedAllRecursively(child, expanded);
        }
        node.setExpanded(expanded);*/
    }

//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
////// pour tester la mémoire occupée par l'arbre ////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////      
}
