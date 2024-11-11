package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;


@Data
@Named(value = "cutAndPaste")
@SessionScoped
public class CutAndPaste implements Serializable {

    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private Tree tree;

    @Autowired
    private ConceptHelper conceptHelper;

    private boolean isCopyOn;
    private boolean isValidPaste;
    private NodeConcept nodeConceptDrag;
    private ArrayList<NodeBT> nodeBTsToCut;

    private NodeConcept nodeConceptDrop;

    private boolean isDropToRoot;

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
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Copy and paste !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
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
        if (!conceptHelper.moveBranchFromConceptToConcept(connect.getPoolConnexion(),
                nodeConceptDrag.getConcept().getIdConcept(),
                oldBtToDelete,
                nodeConceptDrop.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur pendant la suppression des branches !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        return true;
    }
    
    private boolean moveFromRootToConcept() {
        FacesMessage msg;
        if (!conceptHelper.moveBranchFromRootToConcept(connect.getPoolConnexion(),
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
            if (!conceptHelper.setTopConcept(connect.getPoolConnexion(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
            return true;
        } 
        
        for (String oldIdBT : oldBtToDelete) {
            if (!conceptHelper.moveBranchFromConceptToRoot(connect.getPoolConnexion(),
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
        FacesMessage msg;  
     
        
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
        PrimeFaces.current().executeScript("PF('cutAndPaste').hide();");
        reset();
    }
  
    public void rollBackAfterErrorOrCancelDragDrop() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", "Déplacement annulé ");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('cutAndPaste').hide();");
    }

}
