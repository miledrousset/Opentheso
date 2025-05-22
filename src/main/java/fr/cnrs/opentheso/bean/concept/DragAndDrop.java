package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.FacetService;
import fr.cnrs.opentheso.services.GroupService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.TreeDragDropEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;


@Slf4j
@Named(value = "dragAndDrop")
@SessionScoped
public class DragAndDrop implements Serializable {

    @Autowired @Lazy
    private ConceptView conceptBean;

    @Autowired @Lazy
    private SelectedTheso selectedTheso;

    @Autowired @Lazy
    private CurrentUser currentUser;

    @Autowired @Lazy
    private Tree tree;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private ConceptDcTermRepository conceptDcTermRepository;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private ConceptService conceptService;

    private boolean isCopyOn;
    private boolean isValidPaste;
    private NodeConcept nodeConceptDrag;
    private ArrayList<NodeBT> nodeBTsToCut;

    private ArrayList<NodeGroup> nodeGroupsToCut;
    private ArrayList<NodeGroup> nodeGroupsToAdd;

    private List<TreeNode> nodesToConfirme;
    private List<GroupNode> groupNodeToAdd;
    private List<GroupNode> groupNodeToCut;
    private List<BTNode> groupNodeBtToCut;

    private NodeConcept nodeConceptDrop;

    private boolean isdragAndDrop;
    private boolean isDropToRoot;
    
    private boolean isGroupToCut;

    private TreeNode dragNode;
    private TreeNode dropNode;
    @Autowired
    private FacetService facetService;

    public void clear(){
        if(nodeBTsToCut != null){
            nodeBTsToCut.clear();
            nodeBTsToCut = null;
        }
        if(nodeGroupsToCut != null){
            nodeGroupsToCut.clear();
            nodeGroupsToCut = null;
        }   
        if(nodeGroupsToAdd != null){
            nodeGroupsToAdd.clear();
            nodeGroupsToAdd = null;
        }
        if(nodeConceptDrag != null){
            nodeConceptDrag.clear();
            nodeConceptDrag = null;
        }    
        if(nodeConceptDrop != null){
            nodeConceptDrop.clear();
            nodeConceptDrop = null;
        }             
        
        dragNode = null;
        dropNode = null;        
    }

    public void reset() {
        dragNode = null;
        dropNode = null;
        if(nodeBTsToCut != null)
            nodeBTsToCut.clear();
        if(nodeGroupsToCut != null)
            nodeGroupsToCut.clear();
        if(nodeGroupsToAdd != null)
            nodeGroupsToAdd.clear();          
        isCopyOn = false;
        isValidPaste = false;
        
        if(nodeConceptDrag != null)
            nodeConceptDrag.clear();
        if(nodeConceptDrop != null)
            nodeConceptDrop.clear();
        nodeConceptDrop = null;
        
        isdragAndDrop = false;
        isDropToRoot = false;
        isGroupToCut = false;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Copy and paste !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void setBTsToCut() {
        if (nodeConceptDrag == null) {
            return;
        }
        if (nodeBTsToCut == null) {
            nodeBTsToCut = new ArrayList<>();
        }
        for (NodeBT nodeBT : nodeConceptDrag.getNodeBT()) {
            NodeBT nodeBT1 = new NodeBT();
            nodeBT1.setIdConcept(nodeBT.getIdConcept());
            nodeBT1.setTitle(nodeBT.getTitle());
            nodeBT1.setSelected(true);
            nodeBTsToCut.add(nodeBT1);
        }
    }

    public List<NodeBT> setBTsToCut(NodeConcept concept) {

        List<NodeBT> Bts = new ArrayList<>();

        for (NodeBT nodeBT : concept.getNodeBT()) {
            NodeBT nodeBT1 = new NodeBT();
            nodeBT1.setIdConcept(nodeBT.getIdConcept());
            nodeBT1.setTitle(nodeBT.getTitle());
            nodeBT1.setSelected(true);
            Bts.add(nodeBT1);
        }
        return Bts;
    }

    public void setGroupsToCut() {
        if (nodeConceptDrag == null) {
            return;
        }
        if(nodeGroupsToCut == null)
            nodeGroupsToCut = new ArrayList<>();
        for (NodeGroup nodeGroup : nodeConceptDrag.getNodeConceptGroup()) {
            NodeGroup nodeGroup1 = new NodeGroup();
            nodeGroup1.getConceptGroup().setIdGroup(nodeGroup.getConceptGroup().getIdGroup());
            nodeGroup1.setLexicalValue(nodeGroup.getLexicalValue());
            nodeGroup1.setSelected(true);
            nodeGroupsToCut.add(nodeGroup1);
        }
        if(nodeGroupsToAdd == null)
            nodeGroupsToAdd = new ArrayList<>();
        if(nodeConceptDrop != null) {
            for (NodeGroup nodeGroup : nodeConceptDrop.getNodeConceptGroup()) {
                NodeGroup nodeGroup1 = new NodeGroup();
                nodeGroup1.getConceptGroup().setIdGroup(nodeGroup.getConceptGroup().getIdGroup());
                nodeGroup1.setLexicalValue(nodeGroup.getLexicalValue());
                nodeGroup1.setSelected(true);
                nodeGroupsToAdd.add(nodeGroup1);
            }
        }
    }

    public List<NodeGroup> setGroupsToCut(NodeConcept concept) {

        List<NodeGroup> groupsToCut = new ArrayList<>();

        for (NodeGroup nodeGroup : concept.getNodeConceptGroup()) {
            NodeGroup nodeGroup1 = new NodeGroup();
            nodeGroup1.getConceptGroup().setIdGroup(nodeGroup.getConceptGroup().getIdGroup());
            nodeGroup1.setLexicalValue(nodeGroup.getLexicalValue());
            nodeGroup1.setSelected(true);
            groupsToCut.add(nodeGroup1);
        }

        return groupsToCut;
    }

    public List<NodeGroup> setGroupsToAdd(NodeConcept concept) {

        List<NodeGroup> groupsToAdd = new ArrayList<>();
        for (NodeGroup nodeGroup : concept.getNodeConceptGroup()) {
            NodeGroup nodeGroup1 = new NodeGroup();
            nodeGroup1.getConceptGroup().setIdGroup(nodeGroup.getConceptGroup().getIdGroup());
            nodeGroup1.setLexicalValue(nodeGroup.getLexicalValue());
            nodeGroup1.setSelected(true);
            groupsToAdd.add(nodeGroup1);
        }
        return groupsToAdd;
    }

    /**
     * permet de préparer le concept ou la branche pour le déplacement vers un autre endroit #MR
     *
     * @param nodeConcept
     */
    public void onStartCut(NodeConcept nodeConcept) {

        if (nodeConcept == null) {
            return;
        }

        reset();
        this.dragNode = tree.getSelectedNode();

        nodeConceptDrag = nodeConcept;
        isCopyOn = true;

        if (nodeConceptDrag.getTerm() != null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Couper "
                    + nodeConceptDrag.getTerm().getLexicalValue() + " (" + nodeConceptDrag.getConcept().getIdConcept() + ")");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void test() {
        PrimeFaces.current().ajax().update("containerIndex");
    }

    /**
     * permet de coller la branche copiée précédement sous le concept en cours
     * déplacements valides: - d'un concept à un concept - de la racine à un
     * concept ou TopConcept #MR
     * Ne marche que pour Couper/coller (pas de Drag and drop)
     *
     * @param dropppedConcept
     */
    public void paste(NodeConcept dropppedConcept) {
        if(dropppedConcept != null) {
            this.nodeConceptDrop = dropppedConcept;
            if(dropNode != null && !nodeConceptDrop.getConcept().getIdConcept().equalsIgnoreCase(((TreeNodeData) dropNode.getData()).getNodeId() )) {
                pasteWitoutTreeControl();
                return;            
            }
        } else
            dropNode = tree.getRoot();


        if(isDropToRoot) {
            dropNode = tree.getRoot();
        }

        // controle de cohérence entre les noeuds sélectionnés dans l'arbre et les concepts à déplacer
        if(!nodeConceptDrag.getConcept().getIdConcept().equalsIgnoreCase( ((TreeNodeData) dragNode.getData()).getNodeId() )) {
            pasteWitoutTreeControl();
            return;
        }

       isdragAndDrop = false;        
       pasteWithTreeControl();
    }
    
    
    private void pasteWithTreeControl(){
        if(!isDropToRoot) {
            validatePaste();
            if(!isValidPaste) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
              //  rollBackAfterErrorOrCancelDragDrop();
                updateMessage();
                return;
            }         
        }
        /// préparer le noeud à couper
        setBTsToCut();
        
        /// préparer les collections à couper        
        setGroupsToCut();

        // cas de déplacement vers une facette 
        if(((TreeNodeData) dropNode.getData()).getNodeType().equalsIgnoreCase("facet")) {
            // cas d'une branche, pas permis
            if(!nodeConceptDrag.getNodeNT().isEmpty()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                rollBackAfterErrorOrCancelDragDrop();
                updateMessage();
                return;
            }
            String idFacet = ((TreeNodeData) dropNode.getData()).getNodeId();

            if(idFacet == null) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                rollBackAfterErrorOrCancelDragDrop();
                updateMessage();
                return;                
            }

            facetService.addConceptToFacet(idFacet, selectedTheso.getCurrentIdTheso(), ((TreeNodeData) dragNode.getData()).getNodeId());
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Concept ajouté à la facette");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            // on vérifie si le noeud à déplacer est un membre d'une facette, alors on supprime cette appartenance
            if(((TreeNodeData) dragNode.getData()).getNodeType().equalsIgnoreCase("facetmember")){
                try {
                    String idFacetParent = ((TreeNodeData) dragNode.getData()).getIdFacetParent();
                    facetService.deleteConceptFromFacet(idFacetParent, ((TreeNodeData) dragNode.getData()).getNodeId(),
                            selectedTheso.getCurrentIdTheso());
                } catch (Exception e) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    rollBackAfterErrorOrCancelDragDrop();
                    return;                      
                }
                
            }
            
            // cas de changement de groupe, on propose de garder / supprimer / ajouter le groupe
            if(isDroppedToAnotherGroup()) {
                // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                ///choix de l'option pour deplacer la collection ou non 
                isGroupToCut = true;
                PrimeFaces pf = PrimeFaces.current();
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("containerIndex:formLeftTab:idDragAndDrop");
                 //   pf.ajax().update("dragAndDropForm");
                    
                }
                pf.executeScript("PF('dragAndDrop').show();");
                return;
            }
            
            // on controle s'il y a plusieurs branches, 
            if (nodeConceptDrag.getNodeBT().size() < 2) {
                // sinon, on applique le changement direct 
                drop();
            } else {
                // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                PrimeFaces pf = PrimeFaces.current();
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("containerIndex:formLeftTab:idDragAndDrop");
               //     pf.ajax().update("dragAndDropForm");
                }
                pf.executeScript("PF('dragAndDrop').show();");
            }
        }        
    }
    
    private void pasteWitoutTreeControl(){
        FacesMessage msg;  
        setBTsToCut();        
        validatePaste();
        if(!isValidPaste) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            rollBackAfterErrorOrCancelDragDrop();
            updateMessage();
            return;
        }        
        
        
        if(isDropToRoot) {
            // cas de déplacement d'un concept à la racine   
            if(!moveFromConceptToRoot()) return;
        } else {
            if (nodeConceptDrop == null) {
                return;
            }

            if (nodeConceptDrop.getConcept().getIdConcept().equalsIgnoreCase(nodeConceptDrag.getConcept().getIdConcept())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Impossible de coller au même endroit ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }

            // cas de déplacement d'un concept à concept        
            if ((!nodeConceptDrag.getConcept().isTopConcept())) {
                if(!moveFromConceptToConcept()) return;
            }

            // cas de déplacement de la racine à un concept
            if ((nodeConceptDrag.getConcept().isTopConcept())) {
                if(!moveFromRootToConcept()) return;
            }
        }
        
        reloadConcept();
        reloadTree();
        
        if(isDropToRoot)
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                    nodeConceptDrag.getTerm().getLexicalValue()
                            + " -> "
                            + "Root");
        else
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                nodeConceptDrag.getTerm().getLexicalValue()
                        + " -> "
                        + nodeConceptDrop.getTerm().getLexicalValue());
                
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();        
    }
    
    
    public void validatePaste() {
        FacesMessage msg;
        isValidPaste = false;

        ArrayList<String> descendingConcepts = conceptHelper.getIdsOfBranch(nodeConceptDrag.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        if((descendingConcepts != null) && (!descendingConcepts.isEmpty())) {
            if(descendingConcepts.contains(nodeConceptDrop.getConcept().getIdConcept())){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Opération non autorisée, elle peut provoquer des boucles infinies !!! ");
                FacesContext.getCurrentInstance().addMessage(null, msg);            
                return;
            }
        }

        ArrayList<String> listBT = relationsHelper.getListIdOfBT(
                nodeConceptDrag.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        if((listBT != null) && (!listBT.isEmpty())) {
            if(listBT.contains(nodeConceptDrop.getConcept().getIdConcept())){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Opération non autorisée, elle peut provoquer des boucles infinies !!! ");
                FacesContext.getCurrentInstance().addMessage(null, msg);            
                return;
            }
        }
        if(isMoveConceptToConceptValid(selectedTheso.getCurrentIdTheso(),
                nodeConceptDrag.getConcept().getIdConcept(),
                nodeConceptDrop.getConcept().getIdConcept())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            isValidPaste = false;
            return;
        }
        isValidPaste = true;
    }

    /**
     * Fonction pour récupérer l'évènement drag drop de l'arbre
     *
     * @param event
     */
    public void onDragDrop(TreeDragDropEvent event) {
        reset();
        dragNode = (TreeNode) event.getDragNode();
        dropNode = (TreeNode) event.getDropNode();

        FacesMessage msg;

        // à corriger pour traiter le déplacement des facettes par Drag and Drop
        if("facet".equalsIgnoreCase(dragNode.getType())){
            if("facet".equalsIgnoreCase(dropNode.getType())){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " déplacement non permis !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                isValidPaste = false;
                return;
            }
            facetService.updateFacetParent(((TreeNodeData) dropNode.getData()).getNodeId(),
                    ((TreeNodeData) dragNode.getData()).getNodeId(), selectedTheso.getCurrentIdTheso());

            tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
            tree.expandTreeToPath2(
                    ((TreeNodeData) dropNode.getParent().getData()).getNodeId(),//facetSelected.getIdConceptParent(),
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getSelectedLang(),
                    ((TreeNodeData) dragNode.getData()).getNodeId() + "");
            PrimeFaces pf = PrimeFaces.current();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("formRightTab:facetView");
                pf.ajax().update("formLeftTab:tabTree:tree");
            }

            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Facette déplacée avec succès !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        nodeConceptDrag = conceptHelper.getConcept(((TreeNodeData) dragNode.getData()).getNodeId(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang(), -1, -1);

        isdragAndDrop = true;

        /// préparer le noeud à couper
        setBTsToCut();

        if (dropNode.getParent() == null) {
            // déplacement à la racine
            isDropToRoot = true;
        } else {
            nodeConceptDrop = conceptHelper.getConcept(
                    ((TreeNodeData) dropNode.getData()).getNodeId(),
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(), -1, -1);
            /// Vérifier si le dépalcement est valide (controle des relations interdites)
            if(nodeConceptDrop != null) {
                if(isMoveConceptToConceptValid(selectedTheso.getCurrentIdTheso(),
                        nodeConceptDrag.getConcept().getIdConcept(),
                        nodeConceptDrop.getConcept().getIdConcept())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");

                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    isValidPaste = false;
                    reloadTree();
                    return;
                }
            }
        }


        /// préparer les collections à couper
        setGroupsToCut();

        // cas de déplacement vers une facette
        if(((TreeNodeData) dropNode.getData()).getNodeType().equalsIgnoreCase("facet")) {
            // cas d'une branche, pas permis
            if(!nodeConceptDrag.getNodeNT().isEmpty()) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                rollBackAfterErrorOrCancelDragDrop();
                updateMessage();
                return;
            }
            String idFacet = ((TreeNodeData) dropNode.getData()).getNodeId();

            if(idFacet == null) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                rollBackAfterErrorOrCancelDragDrop();
                updateMessage();
                return;
            }

            facetService.addConceptToFacet(idFacet, selectedTheso.getCurrentIdTheso(), ((TreeNodeData) dragNode.getData()).getNodeId());
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Concept ajouté à la facette");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            // on vérifie si le noeud à déplacer est un membre d'une facette, alors on supprime cette appartenance
            if(((TreeNodeData) dragNode.getData()).getNodeType().equalsIgnoreCase("facetmember")){
                try {
                    String idFacetParent = ((TreeNodeData) dragNode.getData()).getIdFacetParent();
                    facetService.deleteConceptFromFacet(idFacetParent, ((TreeNodeData) dragNode.getData()).getNodeId(),
                            selectedTheso.getCurrentIdTheso());
                } catch (Exception e) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    rollBackAfterErrorOrCancelDragDrop();
                    return;
                }

            }

            // cas de changement de groupe, on propose de garder / supprimer / ajouter le groupe
            if(isDroppedToAnotherGroup()) {
                // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                ///choix de l'option pour deplacer la collection ou non
                isGroupToCut = true;
                PrimeFaces.current().ajax().update("containerIndex:formLeftTab:idDragAndDrop");
                PrimeFaces.current().executeScript("PF('dragAndDrop').show();");
                return;
            }

            // on controle s'il y a plusieurs branches,
            if (nodeConceptDrag.getNodeBT().size() < 2) {
                // sinon, on applique le changement direct
                drop();
            } else {
                // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                PrimeFaces.current().ajax().update("containerIndex:formLeftTab:idDragAndDrop");
                PrimeFaces.current().executeScript("PF('dragAndDrop').show();");
            }
        }
    }

    public void onDragDropMultiple(TreeDragDropEvent event) {
        reset();
        dragNode = (TreeNode) event.getDragNode();
        dropNode = (TreeNode) event.getDropNode();


        nodesToConfirme = new ArrayList<>();

        List<TreeNode> nodes = tree.getClickselectedNodes();
        groupNodeToAdd = new ArrayList<>();
        groupNodeToCut = new ArrayList<>();
        groupNodeBtToCut = new ArrayList<>();

        for (TreeNode node : tree.getClickselectedNodes()) {
            // à corriger pour traiter le déplacement des facettes par Drag and Drop
            if ("facet".equalsIgnoreCase(node.getType())) {
                if ("facet".equalsIgnoreCase(dropNode.getType())) {
                    showMessage(FacesMessage.SEVERITY_ERROR, "Déplacement non permis !");
                    isValidPaste = false;
                    return;
                }
                facetService.updateFacetParent(((TreeNodeData) dropNode.getData()).getNodeId(),
                        ((TreeNodeData) node.getData()).getNodeId(), selectedTheso.getCurrentIdTheso());

                tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
                tree.expandTreeToPath2(((TreeNodeData) dropNode.getParent().getData()).getNodeId(),
                        selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang(),
                        ((TreeNodeData) node.getData()).getNodeId());

                PrimeFaces.current().ajax().update("formRightTab:facetView");
                PrimeFaces.current().ajax().update("formLeftTab:tabTree:tree");
                showMessage(FacesMessage.SEVERITY_WARN, "Facette déplacée avec succès !!!");
                return;
            }

            nodeConceptDrag = conceptHelper.getConcept(
                    ((TreeNodeData) node.getData()).getNodeId(), selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(), -1, -1);

            isdragAndDrop = true;
            nodesToConfirme.add(node);

            /// préparer le noeud à couper
            setBTsToCut();
            //setBTsToCut();
            List<NodeBT> nodeBTs = setBTsToCut(nodeConceptDrag);
            for (NodeBT nodeBT : nodeBTs) {
                groupNodeBtToCut.add(new BTNode(node, nodeBT));
            }

            if (dropNode.getParent() == null) {
                // déplacement à la racine
                isDropToRoot = true;
            } else {
                nodeConceptDrop = conceptHelper.getConcept(
                        ((TreeNodeData) dropNode.getData()).getNodeId(), selectedTheso.getCurrentIdTheso(),
                        selectedTheso.getCurrentLang(), -1, -1);
                /// Vérifier si le dépalcement est valide (controle des relations interdites)
                if (nodeConceptDrop != null) {
                    if (isMoveConceptToConceptValid(selectedTheso.getCurrentIdTheso(),
                            nodeConceptDrag.getConcept().getIdConcept(), nodeConceptDrop.getConcept().getIdConcept())) {
                        isValidPaste = false;
                        reloadTree();
                        return;
                    }
                }
            }

            // préparer les collections à couper
            setGroupsToCut();
            //setGroupsToCut();
            List<NodeGroup> groupsToCut = setGroupsToCut(nodeConceptDrag);
            for (NodeGroup nodeGroup : groupsToCut) {
                groupNodeToCut.add(new GroupNode(node, nodeGroup));
            }
            List<NodeGroup> groupsToAdd = setGroupsToAdd(nodeConceptDrag);
            for (NodeGroup nodeGroup : groupsToAdd) {
                groupNodeToAdd.add(new GroupNode(node, nodeGroup));
            }

            // cas de déplacement vers une facette
            if (((TreeNodeData) dropNode.getData()).getNodeType().equalsIgnoreCase("facet")) {
                // cas d'une branche, pas permis
                if (!nodeConceptDrag.getNodeNT().isEmpty()) {

                    showMessage(FacesMessage.SEVERITY_WARN, "Action non permise !!!");
                    rollBackAfterErrorOrCancelDragDrop();
                    return;
                }
                String idFacet = ((TreeNodeData) dropNode.getData()).getNodeId();

                if (idFacet == null) {

                    showMessage(FacesMessage.SEVERITY_WARN, "Action non permise !!!");
                    rollBackAfterErrorOrCancelDragDrop();
                    return;
                }

                facetService.addConceptToFacet(idFacet, selectedTheso.getCurrentIdTheso(), ((TreeNodeData) node.getData()).getNodeId());
                showMessage(FacesMessage.SEVERITY_INFO, "Concept ajouté à la facette");
            } else {
                // on vérifie si le noeud à déplacer est un membre d'une facette, alors on supprime cette appartenance
                if (((TreeNodeData) node.getData()).getNodeType().equalsIgnoreCase("facetmember")) {
                    try {
                        String idFacetParent = ((TreeNodeData) node.getData()).getIdFacetParent();
                        facetService.deleteConceptFromFacet(idFacetParent, ((TreeNodeData) node.getData()).getNodeId(),
                                selectedTheso.getCurrentIdTheso());
                    } catch (Exception e) {
                        showMessage(FacesMessage.SEVERITY_WARN, "Action non permise !!!");
                        rollBackAfterErrorOrCancelDragDrop();
                        return;
                    }

                }

                // cas de changement de groupe, on propose de garder / supprimer / ajouter le groupe
                if (isDroppedToAnotherGroup()) {
                    // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                    ///choix de l'option pour deplacer la collection ou non
                    isGroupToCut = true;
                    nodesToConfirme.add(node);
                    PrimeFaces.current().ajax().update("containerIndex:formLeftTab:idDragAndDropMultiple");
                    PrimeFaces.current().executeScript("PF('dragAndDropMultiple').show();");
                    continue;
                }

                // on controle s'il y a plusieurs branches,
                if (nodeConceptDrag.getNodeBT().size() < 2) {
                    // sinon, on applique le changement direct
                    drop();
                } else {
                    nodesToConfirme.add(node);
                    // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                    PrimeFaces.current().ajax().update("containerIndex:formLeftTab:idDragAndDropMultiple");
                    PrimeFaces.current().executeScript("PF('dragAndDropMultiple').show();");
                }
            }
        }
    }


    /**
     * permet de déposer la branche copiée précédement par Drag and Drop
     *
     */
    
    public void drop() {
        FacesMessage msg;  
        if(isDropToRoot) {
            // cas de déplacement d'un concept à la racine   
            if(!moveFromConceptToRoot()) return;
        } else {
            if (nodeConceptDrop == null) {
                return;
            }
            
            /// Vérifier si le dépalcement est valide (controle des relations interdites)
            if(isMoveConceptToConceptValid(selectedTheso.getCurrentIdTheso(),
                    nodeConceptDrag.getConcept().getIdConcept(), nodeConceptDrop.getConcept().getIdConcept())) {

                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                FacesContext.getCurrentInstance().addMessage(null, msg);
                isValidPaste = false;
                reloadTree();
                return;    
            }            

            // cas de déplacement d'un concept à concept        
            if ((!nodeConceptDrag.getConcept().isTopConcept())) {
                if(!moveFromConceptToConcept()) return;
            }

            // cas de déplacement de la racine à un concept
            if ((nodeConceptDrag.getConcept().isTopConcept())) {
                if(!moveFromRootToConcept()) return;
            }
        }
        if(isGroupToCut) {
           addAndCutGroup();
        }

        reloadConcept();
        reloadTree();
        
        if(isDropToRoot)
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                    nodeConceptDrag.getTerm().getLexicalValue() + " -> " + "Root");
        else
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                nodeConceptDrag.getTerm().getLexicalValue() + " -> " + nodeConceptDrop.getTerm().getLexicalValue());
                
        FacesContext.getCurrentInstance().addMessage(null, msg);        
        PrimeFaces.current().executeScript("PF('dragAndDrop').hide();");
        reset();
    }

    public void dropMultiple() {

        for (TreeNode node : nodesToConfirme) {

            nodeConceptDrag = nodeConceptDrop = conceptHelper.getConcept(
                    ((TreeNodeData) node.getData()).getNodeId(), selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(), -1, -1);

            if (nodeConceptDrop == null) {
                continue;
            }

            if (dropNode.getParent() == null) {
                // cas de déplacement d'un concept à la racine
                if (!moveFromConceptToRootMultiple()) {
                    return;
                }
            } else {
                /// Vérifier si le dépalcement est valide (controle des relations interdites)
                // cas de déplacement d'un concept à concept
                if ((!nodeConceptDrag.getConcept().isTopConcept())) {
                    if (!moveFromConceptToConcept()) {
                        continue;
                    }
                }

                // cas de déplacement de la racine à un concept
                if ((nodeConceptDrag.getConcept().isTopConcept())) {
                    if (!moveFromRootToConcept()) {
                        continue;
                    }
                }
            }

            addAndCutGroupMultiple();

            if (dropNode.getParent() == null) {
                showMessage(FacesMessage.SEVERITY_INFO, nodeConceptDrag.getTerm().getLexicalValue() + " -> Root");
            } else {
                showMessage(FacesMessage.SEVERITY_INFO, nodeConceptDrag.getTerm().getLexicalValue()
                        + " -> " + nodeConceptDrop.getTerm().getLexicalValue());
            }
        }

        reloadConcept();
        reloadTree();
        PrimeFaces.current().executeScript("PF('dragAndDrop').hide();");
    }
    
    private boolean isDroppedToAnotherGroup(){
        if(nodeConceptDrag == null || nodeConceptDrop == null) return false;
        if(nodeConceptDrag.getNodeConceptGroup() == null || nodeConceptDrop.getNodeConceptGroup() == null) return false;
        
        //cas où les deux concepts n'ont pas de collections, donc pas de changement de collection
        if(nodeConceptDrag.getNodeConceptGroup().isEmpty() && nodeConceptDrop.getNodeConceptGroup().isEmpty())
            return false;
        
        //cas où l'un des deux concepts n'a pas de collection, alors il y a changement de collection
        if(nodeConceptDrag.getNodeConceptGroup().isEmpty() || nodeConceptDrop.getNodeConceptGroup().isEmpty())
            return true;
        
        //cas où il y a plusieurs collections, alors il peut y avoir changement de collection        
        if(nodeConceptDrag.getNodeConceptGroup().size() > 1 || nodeConceptDrop.getNodeConceptGroup().size() >1)
            return true;
        
        //cas où les deux collections sont identiques ou non 
        return !nodeConceptDrag.getNodeConceptGroup().get(0).getConceptGroup().getIdGroup().equalsIgnoreCase(
                nodeConceptDrop.getNodeConceptGroup().get(0).getConceptGroup().getIdGroup());
    }
    
    private void updateMessage(){
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }        
    }

    /**
     * permet de retourner les noms des collections/groupes 
     * @return 
     */
    public String getLabelOfGroupes() {
        if (nodeConceptDrag == null) {
            return null;
        }
        String labels = "";
        boolean first = true;
        for (NodeGroup nodeGroup : nodeConceptDrag.getNodeConceptGroup()) {
            if (!first) {
                labels = labels + ", " + nodeGroup.getLexicalValue();
            } else {
                labels = nodeGroup.getLexicalValue();
                first = false;
            }
        }
        return labels;
    }

    private boolean moveFromConceptToConceptMultiple(){
        // cas de déplacement d'un concept à concept
        ArrayList<String> oldBtToDelete = new ArrayList<>();
        for (BTNode nodeBT : groupNodeBtToCut) {
            if (((TreeNodeData) nodeBT.getNode().getData()).getNodeId().equals(nodeConceptDrag.getConcept().getIdConcept())
                    && nodeBT.nodeBT.isSelected()) {
                // on prépare les BT sélectionné pour la suppression
                oldBtToDelete.add(nodeBT.nodeBT.getIdConcept());
            }
        }
        if (oldBtToDelete.isEmpty()) {
            showMessage(FacesMessage.SEVERITY_WARN, nodeConceptDrag.getTerm().getLexicalValue()
                    + " - Aucun parent n'est sélectionné pour déplacement");
            return false;
        }
        if (!conceptHelper.moveBranchFromConceptToConcept(
                nodeConceptDrag.getConcept().getIdConcept(),
                oldBtToDelete,
                nodeConceptDrop.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser())) {

            showMessage(FacesMessage.SEVERITY_ERROR, nodeConceptDrag.getTerm().getLexicalValue()
                    + " - Erreur pendant la suppression des branches !!");
        }
        return true;
    }

    private boolean moveFromConceptToRootMultiple(){
        FacesMessage msg;
        ArrayList<String> oldBtToDelete = new ArrayList<>();

        for (BTNode nodeBT : groupNodeBtToCut) {
            if (((TreeNodeData) nodeBT.getNode().getData()).getNodeId()
                    .equals(nodeConceptDrag.getConcept().getIdConcept())) {
                oldBtToDelete.add(nodeConceptDrag.getConcept().getIdConcept());
            }
        }
        // cas incohérent mais à corriger, c'est un concept qui est topTorm mais qui n'a pas l'info
        if (oldBtToDelete.isEmpty()) {
            if (!conceptHelper.setTopConcept(
                    nodeConceptDrag.getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
            return true;
        }

        for (String oldIdBT : oldBtToDelete) {
            if (!conceptHelper.moveBranchFromConceptToRoot(
                    nodeConceptDrag.getConcept().getIdConcept(),
                    oldIdBT,
                    selectedTheso.getCurrentIdTheso(),
                    currentUser.getNodeUser().getIdUser())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
        }
        return true;
    }

    private void addAndCutGroupMultiple() {
        ArrayList<String> allId = conceptHelper.getIdsOfBranch(nodeConceptDrop.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        if( (allId == null) || (allId.isEmpty())) return;

        for (GroupNode nodeGroup : groupNodeToCut) {
            if (((TreeNodeData) nodeGroup.getNode().getData()).getNodeId()
                    .equals(nodeConceptDrag.getConcept().getIdConcept())
                    && nodeGroup.getNodeGroup().isSelected()) {
                for (String idConcept : allId) {
                    groupService.deleteRelationConceptGroupConcept(nodeGroup.getNodeGroup().getConceptGroup().getIdGroup(),
                            idConcept, selectedTheso.getCurrentIdTheso());
                }
            }
            if (((TreeNodeData) nodeGroup.getNode().getData()).getNodeId().equals(nodeConceptDrag.getConcept().getIdConcept())
                    && !nodeGroup.getNodeGroup().isSelected()) {
                for (String idConcept : allId) {
                    groupService.addConceptGroupConcept(nodeGroup.getNodeGroup().getConceptGroup().getIdGroup(),
                            idConcept, selectedTheso.getCurrentIdTheso());
                }
            }
        }
        for (GroupNode nodeGroup : groupNodeToAdd) {
            if (((TreeNodeData) nodeGroup.getNode().getData()).getNodeId().equals(nodeConceptDrag.getConcept().getIdConcept())
                    && !nodeGroup.getNodeGroup().isSelected()) {
                for (String idConcept : allId) {
                    groupService.deleteRelationConceptGroupConcept(nodeGroup.getNodeGroup().getConceptGroup().getIdGroup(),
                            idConcept, selectedTheso.getCurrentIdTheso());
                }
            }
            if (((TreeNodeData) nodeGroup.getNode().getData()).getNodeId().equals(nodeConceptDrag.getConcept().getIdConcept())
                    && nodeGroup.getNodeGroup().isSelected()) {
                for (String idConcept : allId) {
                    groupService.addConceptGroupConcept(nodeGroup.getNodeGroup().getConceptGroup().getIdGroup(),
                            idConcept, selectedTheso.getCurrentIdTheso());
                }
            }
        }
    }

    private void showMessage(FacesMessage.Severity severity, String message) {
        FacesMessage msg = new FacesMessage(severity, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    /**
     * deplacement entre concepts
     * @return 
     */
    private boolean moveFromConceptToConcept(){
        // cas de déplacement d'un concept à concept
        FacesMessage msg;
        ArrayList<String> oldBtToDelete = new ArrayList<>();
        for (NodeBT nodeBT : nodeBTsToCut) {
            if (nodeBT.isSelected()) {
                // on prépare les BT sélectionné pour la suppression
                oldBtToDelete.add(nodeBT.getIdConcept());
            }
        }
        if (oldBtToDelete.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "aucun parent n'est sélectionné pour déplacement ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        if (!conceptHelper.moveBranchFromConceptToConcept(nodeConceptDrag.getConcept().getIdConcept(),
                oldBtToDelete, nodeConceptDrop.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), currentUser.getNodeUser().getIdUser())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur pendant la suppression des branches !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        return true;
    }
    
    private boolean moveFromRootToConcept() {
        FacesMessage msg;
        if (!conceptHelper.moveBranchFromRootToConcept(
                nodeConceptDrag.getConcept().getIdConcept(),
                nodeConceptDrop.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur pendant le déplacement dans la base de données ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        return true;
    }
    
    private boolean moveFromConceptToRoot(){
        FacesMessage msg;
        ArrayList<String> oldBtToDelete = new ArrayList<>();
        
        for (NodeBT nodeBT : nodeBTsToCut) {
            oldBtToDelete.add(nodeBT.getIdConcept());
        }
        // cas incohérent mais à corriger, c'est un concept qui est topTorm mais qui n'a pas l'info
        if (oldBtToDelete.isEmpty()) {
            if (!conceptHelper.setTopConcept(
                    nodeConceptDrag.getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
            return true;
        } 
        
        for (String oldIdBT : oldBtToDelete) {
            if (!conceptHelper.moveBranchFromConceptToRoot(
                    nodeConceptDrag.getConcept().getIdConcept(),
                    oldIdBT,
                    selectedTheso.getCurrentIdTheso(),
                    currentUser.getNodeUser().getIdUser())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
        }
        return true;
    }

    private void reloadConcept(){
        PrimeFaces pf = PrimeFaces.current();

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), nodeConceptDrag.getConcept().getIdConcept(), currentUser.getNodeUser().getIdUser());

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(nodeConceptDrag.getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());
        
        // si le concept n'est pas déployé à doite, alors on ne fait rien
        if(conceptBean.getNodeConcept() != null){
            conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    conceptBean.getSelectedLang(), currentUser);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:rightTab:conceptView");
            }
        }      
    }

    private void reloadTree(){

        String lang = selectedTheso.getCurrentLang();
        if (!StringUtils.isEmpty(conceptBean.getSelectedLang())) {
            lang = conceptBean.getSelectedLang();
        }

        tree.initAndExpandTreeToPath(nodeConceptDrag.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), lang);

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("formLeftTab:tabTree:tree");
        PrimeFaces.current().executeScript("srollToSelected();");
    }

    private void addAndCutGroup() {
        ArrayList<String> allId = conceptHelper.getIdsOfBranch(nodeConceptDrag.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        if( (allId == null) || (allId.isEmpty())) return;         
        
        for (NodeGroup nodeGroup : nodeGroupsToCut) {
            if(nodeGroup.isSelected()) {
                for (String idConcept : allId) {
                    groupService.deleteRelationConceptGroupConcept(nodeGroup.getConceptGroup().getIdGroup(),
                        idConcept, selectedTheso.getCurrentIdTheso());
                }
            }
            if(!nodeGroup.isSelected()) {
                for (String idConcept : allId) {
                    groupService.addConceptGroupConcept(nodeGroup.getConceptGroup().getIdGroup(), idConcept, selectedTheso.getCurrentIdTheso());
                }
            }            
        }
        for (NodeGroup nodeGroup : nodeGroupsToAdd) {
            if(!nodeGroup.isSelected()) {
                for (String idConcept : allId) {
                    groupService.deleteRelationConceptGroupConcept(nodeGroup.getConceptGroup().getIdGroup(), idConcept, selectedTheso.getCurrentIdTheso());
                }
            }
            if(nodeGroup.isSelected()) {
                for (String idConcept : allId) {
                    groupService.addConceptGroupConcept(nodeGroup.getConceptGroup().getIdGroup(),
                        idConcept, selectedTheso.getCurrentIdTheso());
                }
            }            
        }
    }
  
    public void rollBackAfterErrorOrCancelDragDrop() {

        reloadTree();
        reset();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", "Déplacement annulé ");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('dragAndDrop').hide();");
    }

    public boolean isIsCopyOn() {
        return isCopyOn;
    }

    public void setIsCopyOn(boolean isCopyOn) {
        this.isCopyOn = isCopyOn;
    }

    public NodeConcept getCuttedConcept() {
        return nodeConceptDrag;
    }

    public void setCuttedConcept(NodeConcept cuttedConcept) {
        this.nodeConceptDrag = cuttedConcept;
    }

    public ArrayList<NodeBT> getNodeBTsToCut() {
        return nodeBTsToCut;
    }

    public void setNodeBTsToCut(ArrayList<NodeBT> nodeBTsToCut) {
        this.nodeBTsToCut = nodeBTsToCut;
    }


    public NodeConcept getDropppedConcept() {
        return nodeConceptDrop;
    }

    public void setDropppedConcept(NodeConcept dropppedConcept) {
        this.nodeConceptDrop = dropppedConcept;
    }

    public boolean isIsdragAndDrop() {
        return isdragAndDrop;
    }

    public void setIsdragAndDrop(boolean isdragAndDrop) {
        this.isdragAndDrop = isdragAndDrop;
    }

    public NodeConcept getNodeConceptDrag() {
        return nodeConceptDrag;
    }

    public void setNodeConceptDrag(NodeConcept nodeConceptDrag) {
        this.nodeConceptDrag = nodeConceptDrag;
    }

    public NodeConcept getNodeConceptDrop() {
        return nodeConceptDrop;
    }

    public void setNodeConceptDrop(NodeConcept nodeConceptDrop) {
        this.nodeConceptDrop = nodeConceptDrop;
    }

    public boolean isIsDropToRoot() {
        return isDropToRoot;
    }

    public void setIsDropToRoot(boolean isDropToRoot) {
        this.isDropToRoot = isDropToRoot;
    }

    public boolean isIsValidPaste() {
        return isValidPaste;
    }

    public void setIsValidPaste(boolean isValidPaste) {
        this.isValidPaste = isValidPaste;
    }

    public ArrayList<NodeGroup> getNodeGroupsToCut() {
        return nodeGroupsToCut;
    }

    public void setNodeGroupsToCut(ArrayList<NodeGroup> nodeGroupsToCut) {
        this.nodeGroupsToCut = nodeGroupsToCut;
    }

    public ArrayList<NodeGroup> getNodeGroupsToAdd() {
        return nodeGroupsToAdd;
    }

    public void setNodeGroupsToAdd(ArrayList<NodeGroup> nodeGroupsToAdd) {
        this.nodeGroupsToAdd = nodeGroupsToAdd;
    }

    public TreeNode getDragNode() {
        return dragNode;
    }

    public void setDragNode(TreeNode dragNode) {
        this.dragNode = dragNode;
    }

    public TreeNode getDropNode() {
        return dropNode;
    }

    public void setDropNode(TreeNode dropNode) {
        this.dropNode = dropNode;
    }

    public List<TreeNode> getNodesToConfirme() {
        return nodesToConfirme;
    }

    public void setNodesToConfirme(List<TreeNode> nodesToConfirme) {
        this.nodesToConfirme = nodesToConfirme;
    }

    public List<GroupNode> getGroupNodeToAdd() {
        return groupNodeToAdd;
    }

    public void setGroupNodeToAdd(List<GroupNode> groupNodeToAdd) {
        this.groupNodeToAdd = groupNodeToAdd;
    }

    public List<GroupNode> getGroupNodeToCut() {
        return groupNodeToCut;
    }

    public void setGroupNodeToCut(List<GroupNode> groupNodeToCut) {
        this.groupNodeToCut = groupNodeToCut;
    }

    public List<BTNode> getGroupNodeBtToCut() {
        return groupNodeBtToCut;
    }

    public void setGroupNodeBtToCut(List<BTNode> groupNodeBtToCut) {
        this.groupNodeBtToCut = groupNodeBtToCut;
    }

    public class GroupNode {

        private TreeNode node;
        private NodeGroup nodeGroup;

        public GroupNode(TreeNode node, NodeGroup nodeGroup) {
            this.node = node;
            this.nodeGroup = nodeGroup;
        }

        public TreeNode getNode() {
            return node;
        }

        public void setNode(TreeNode node) {
            this.node = node;
        }

        public NodeGroup getNodeGroup() {
            return nodeGroup;
        }

        public void setNodeGroup(NodeGroup nodeGroup) {
            this.nodeGroup = nodeGroup;
        }
    }

    public class BTNode {

        private TreeNode node;
        private NodeBT nodeBT;

        public BTNode(TreeNode node, NodeBT nodeBT) {
            this.node = node;
            this.nodeBT = nodeBT;
        }

        public TreeNode getNode() {
            return node;
        }

        public void setNode(TreeNode node) {
            this.node = node;
        }

        public NodeBT getNodeBT() {
            return nodeBT;
        }

        public void setNodeBT(NodeBT nodeBT) {
            this.nodeBT = nodeBT;
        }
    }

    public boolean isMoveConceptToConceptValid(String idTheso, String idConcept, String idConceptToAdd) {

        return idConcept.equalsIgnoreCase(idConceptToAdd)
                || relationsHelper.isConceptHaveRelationRT(idConcept, idConceptToAdd, idTheso)
                || relationsHelper.isConceptHaveRelationNTorBT(idConcept, idConceptToAdd, idTheso);
    }
}
