package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "cutAndPaste")
public class CutAndPaste implements Serializable {

    private final Tree tree;
    private final ConceptView conceptBean;
    private final CurrentUser currentUser;
    private final SelectedTheso selectedTheso;
    private final ConceptService conceptService;

    private boolean isCopyOn, isValidPaste, isDropToRoot;
    private NodeConcept nodeConceptDrag;
    private List<NodeBT> nodeBTsToCut;
    private NodeConcept nodeConceptDrop;


    public void clear(){
        nodeConceptDrag = null;
        nodeConceptDrop = null;        
        if(nodeBTsToCut!= null){
            nodeBTsToCut.clear();
            nodeBTsToCut = null;
        }    
    }

    public void reset() {
        if(nodeBTsToCut != null)
            nodeBTsToCut.clear();
        isCopyOn = false;
        isValidPaste = false;
        nodeConceptDrag = null;
        nodeConceptDrop = null;

        isDropToRoot = false;
    }

    public void infos() {
        MessageUtils.showInformationMessage("Rédiger une aide ici pour Copy and paste !");
    }
 
    /**
     * permet de retourner les noms des collections/groupes
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
    
    /**
     * deplacement entre concepts
     */
    private boolean moveFromConceptToConcept(){
        // cas de déplacement d'un concept à concept
        List<String> oldBtToDelete = new ArrayList<>();
        for (NodeBT nodeBT : nodeBTsToCut) {
            if (nodeBT.isSelected()) {
                // on prépare les BT sélectionné pour la suppression
                oldBtToDelete.add(nodeBT.getIdConcept());
            }
        }
        if (oldBtToDelete.isEmpty()) {
            MessageUtils.showErrorMessage("Aucun parent n'est sélectionné pour déplacement ");
            return false;
        }
        if (!conceptService.moveBranchFromConceptToConcept(nodeConceptDrag.getConcept().getIdConcept(), oldBtToDelete,
                nodeConceptDrop.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), currentUser.getNodeUser().getIdUser())) {
            MessageUtils.showErrorMessage("Erreur pendant la suppression des branches !!");
            return false;
        }
        return true;
    }
    
    private boolean moveFromRootToConcept() {
        if (!conceptService.moveBranchFromRootToConcept(nodeConceptDrag.getConcept().getIdConcept(),
                nodeConceptDrop.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), currentUser.getNodeUser().getIdUser())) {
            MessageUtils.showErrorMessage("Erreur pendant le déplacement dans la base de données ");
            return false;
        }
        return true;
    }
    
    private boolean moveFromConceptToRoot(){

        List<String> oldBtToDelete = new ArrayList<>();
        
        for (NodeBT nodeBT : nodeBTsToCut) {
            oldBtToDelete.add(nodeBT.getIdConcept());
        }
        // cas incohérent mais à corriger, c'est un concept qui est topTorm mais qui n'a pas l'info
        if (oldBtToDelete.isEmpty()) {
            if (!conceptService.setTopConcept(nodeConceptDrag.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), true)) {
                MessageUtils.showErrorMessage("Erreur pendant le déplacement dans la base de données ");
                return false;
            }
            return true;
        } 
        
        for (String oldIdBT : oldBtToDelete) {
            if (!conceptService.moveBranchFromConceptToRoot(nodeConceptDrag.getConcept().getIdConcept(), oldIdBT,
                    selectedTheso.getCurrentIdTheso(), currentUser.getNodeUser().getIdUser())) {
                MessageUtils.showErrorMessage("Erreur pendant le déplacement dans la base de données ");
                return false;
            }
        }
        return true;
    }

    private void reloadConcept(){
        PrimeFaces pf = PrimeFaces.current();

        // si le concept n'est pas déployé à droite, alors on ne fait rien
        if(conceptBean.getNodeConcept() != null){
            conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    conceptBean.getSelectedLang(), currentUser);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formRightTab");
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
    
    /**
     * permet de coller la branche copiée précédement sous le concept en cours
     * déplacements valides: - d'un concept à un concept - de la racine à un
     * concept ou TopConcept #MR
     * Ne marche que pour Couper/coller (pas de Drag and drop)
     *
     */
    public void paste() {

        if(isDropToRoot) {
            // cas de déplacement d'un concept à la racine   
            if(!moveFromConceptToRoot()) return;
        } else {
            if (nodeConceptDrop == null) {
                return;
            }

            if (nodeConceptDrop.getConcept().getIdConcept().equalsIgnoreCase(nodeConceptDrag.getConcept().getIdConcept())) {
                MessageUtils.showWarnMessage("Impossible de coller au même endroit ");
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
           MessageUtils.showInformationMessage(nodeConceptDrag.getTerm().getLexicalValue() + " -> " + "Root");
        else
            MessageUtils.showInformationMessage(nodeConceptDrag.getTerm().getLexicalValue() + " -> " + nodeConceptDrop.getTerm().getLexicalValue());

        PrimeFaces.current().executeScript("PF('cutAndPaste').hide();");
        reset();
    }
  
    public void rollBackAfterErrorOrCancelDragDrop() {
        MessageUtils.showInformationMessage("Déplacement annulé ");
        PrimeFaces.current().executeScript("PF('cutAndPaste').hide();");
    }

}
