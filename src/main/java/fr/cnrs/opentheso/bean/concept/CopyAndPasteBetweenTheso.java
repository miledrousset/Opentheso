package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.CopyAndPasteBetweenThesoService;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;

import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "copyAndPasteBetweenTheso")
@SessionScoped
public class CopyAndPasteBetweenTheso implements Serializable {

    
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private Tree tree;
    @Autowired @Lazy private RoleOnThesoBean roleOnThesoBean;

    @Autowired
    private CopyAndPasteBetweenThesoService copyAndPasteBetweenThesoHelper;

    @Autowired
    private ConceptHelper conceptHelper;


    private boolean isCopyOn;
    private boolean isValidPaste;
    private NodeConcept nodeConceptDrag;
    private NodeConcept nodeConceptDrop;
    
    private List<String> conceptsToCopy;
    
    private String idThesoOrigin;
    
    private boolean isDropToRoot;
    
    // pour savoir si on récupère les identifiants pérennes ou non
    private String identifierType = "sans";
    @Autowired
    private ConceptService conceptService;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        idThesoOrigin = null;
        nodeConceptDrop = null;        
        if(conceptsToCopy!= null){
            conceptsToCopy.clear();
            conceptsToCopy = null;
        }    
    }      
    
    public void reset() {
        isCopyOn = false;
        isValidPaste = false;
        nodeConceptDrag = null;
        nodeConceptDrop = null;
        isDropToRoot = false;
        idThesoOrigin = null;
        conceptsToCopy = null;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Copy and paste !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de préparer le concept ou la branche pour le déplacement vers un autre endroit #MR
     *
     */
    public void onStartCopy() {
        
        // controler les déplacements non autorisés
        FacesMessage msg;

        // aprsè l'initialisation du conceptBean, cette variable est aussi initialisée
        // pour éviter cela, on construi un nouvel objet
        nodeConceptDrag = conceptHelper.getConcept(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getTerm().getLang(), -1, -1);
        isCopyOn = true;
        idThesoOrigin = selectedTheso.getCurrentIdTheso();
        if (nodeConceptDrag == null) {
            return;
        }

        conceptsToCopy = conceptService.getIdsOfBranch(nodeConceptDrag.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Copier "
                + nodeConceptDrag.getTerm().getLexicalValue() + " (" + nodeConceptDrag.getConcept().getIdConcept() + ") Total = " + conceptsToCopy.size());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    /**
     * permet de vérifier si le déplacement est valide 
     */
    public void validatePaste() {
        if(selectedTheso.getCurrentIdTheso().equalsIgnoreCase(idThesoOrigin)) {
            return;
        }
        
        FacesMessage msg;
        isValidPaste = false;

        // on vérifie si les ids des concepts à copier n'existent pas déjà dans le thésaurus cible.
        for (String idConcept : conceptsToCopy) {
            if(conceptService.isIdExiste(idConcept, selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "L'identifiant " + idConcept + " existe déjà dans le thésaurus cible, c'est interdit !!! ");
                FacesContext.getCurrentInstance().addMessage(null, msg);            
                return;                
            }
        }
        isValidPaste = true;
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
            if(!copyToRoot()) return;
        } else {
            if (conceptBean.getNodeConcept() == null) {
                return;
            }
            // cas de déplacement d'un concept à concept        
            if(!copyToConcept()) return;
        }
        

        
        if(isDropToRoot)
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                    nodeConceptDrag.getTerm().getLexicalValue()
                            + " -> "
                            + "Root");
        else
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                nodeConceptDrag.getTerm().getLexicalValue()
                        + " -> "
                        + conceptBean.getNodeConcept().getTerm().getLexicalValue());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();
    }    
    
    /**
     * deplacement entre concepts
     * @return 
     */
    private boolean copyToConcept(){
        // cas de déplacement d'un concept/branche d'un autre thésaurus vers un concept
        
        if(nodeConceptDrag.getConcept() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "", "Erreur de copie"));
            return false;
        }

        if(!copyAndPasteBetweenThesoHelper.pasteBranchLikeNT(
                    selectedTheso.getCurrentIdTheso(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    idThesoOrigin,
                    nodeConceptDrag.getConcept().getIdConcept(),
                    identifierType, 
                    currentUser.getNodeUser().getIdUser(),
                    roleOnThesoBean.getNodePreference())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de copie"));
        }
        return true;
    }
    
    private boolean copyToRoot(){
        // cas de déplacement d'un concept/branche d'un autre thésaurus à la racine
        
            if(!copyAndPasteBetweenThesoHelper.pasteBranchToRoot(
                    selectedTheso.getCurrentIdTheso(),

                    idThesoOrigin,
                    nodeConceptDrag.getConcept().getIdConcept(),
                    identifierType, 
                    currentUser.getNodeUser().getIdUser(),
                    roleOnThesoBean.getNodePreference())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "", "Erreur de copie"));
            } 
        return true; 
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
  
    public void rollBackAfterErrorOrCancelDragDrop() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", "Déplacement annulé ");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('cutAndPaste').hide();");
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

    public NodeConcept getDropppedConcept() {
        return nodeConceptDrop;
    }

    public void setDropppedConcept(NodeConcept dropppedConcept) {
        this.nodeConceptDrop = dropppedConcept;
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

    public String getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }

    public String getIdThesoOrigin() {
        return idThesoOrigin;
    }

    public void setIdThesoOrigin(String idThesoOrigin) {
        this.idThesoOrigin = idThesoOrigin;
    }

}
