/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;

/**
 *
 * @author miledrousset
 */
@Named(value = "editConcept")
//@javax.enterprise.context.RequestScoped
//// on ne peut pas relancer plusieurs actions avec cette déclaration

@javax.enterprise.context.SessionScoped
public class EditConcept implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private ConceptView conceptView;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private Tree tree;

    private String prefLabel;
    private String notation;
    private String source;

    private boolean isCreated;
    private boolean duplicate;
    private boolean forDelete;

    public EditConcept() {
    }

    public void reset(String label) {
        isCreated = false;
        duplicate = false;
        prefLabel = label;
        notation = "";
        forDelete = false;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour modifier Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter un nouveau top concept si le groupe = null, on ajoute un
     * TopConcept sans groupe si l'id du concept est fourni, il faut controler
     * s'il est unique
     *
     * @param idLang
     * @param idTheso
     * @param idUser
     */
    public void updateLabel(
            String idTheso,
            String idLang,
            int idUser) {

        duplicate = false;

        if (prefLabel == null || prefLabel.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "le label est obligatoire !");
            FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:renameForm:newPrefLabel", msg);
            //     msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error", "Invalid credentials");
            //        FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        TermHelper termHelper = new TermHelper();

        // vérification si le term à ajouter existe déjà, s oui, on a l'Id, sinon, on a Null
        String idTerm = termHelper.isTermEqualTo(connect.getPoolConnexion(),
                prefLabel.trim(),
                idTheso,
                idLang);

        if (idTerm != null) {
            String label = termHelper.getLexicalValue(connect.getPoolConnexion(), idTerm, idTheso, idLang);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", label + " : existe déjà ! voulez-vous continuer ?");
            FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:renameForm:newPrefLabel", msg);
            duplicate = true;
            return;
        }
        if(termHelper.isAltLabelExist(connect.getPoolConnexion(), idTerm, idTheso, idLang)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " un synonyme existe déjà ! voulez-vous continuer ?");
            FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:renameForm:newPrefLabel", msg);
            duplicate = true;
            return;
        }
        
        
        updateForced(idTheso, idLang, idUser);

        //   if(tree.getSelectedNode() == null) return;
        // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
        //    if( !((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(idConceptParent)){
        //   }

        /*    tree.addNewChild(tree.getRoot(), idNewConcept, idTheso, idLang);

        tree.expandTreeToPath(idNewConcept, idTheso, idLang);
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formLeftTab");
        }*/
        // cas où l'arbre est déjà déplié ou c'est un concept sans fils
        /*    if (tree.getSelectedNode().isExpanded() || tree.getSelectedNode().getChildCount() == 0) {
            tree.addNewChild(tree.getSelectedNode(), idNewConcept, idTheso, idLang);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("formLeftTab");
            }
        }*/
        // sinon, on ne fait rien, l'arbre sera déplié automatiquement
        //    PrimeFaces.current().executeScript("$('.addNT1').modal('hide');"); 
    }

    public void updateForced(
            String idTheso,
            String idLang,
            int idUser) {

        TermHelper termHelper = new TermHelper();
        String idTerm = termHelper.getIdTermOfConcept(connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(), idTheso);
        if (idTerm == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de cohérence de BDD !!");
            FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:renameForm:newPrefLabel", msg);
            return;
        }

        // on vérifie si la tradcution existe, on la met à jour, sinon, on en ajoute une
        if (termHelper.isTermExistInThisLang(connect.getPoolConnexion(), idTerm, idLang, idTheso)) {
            if (!termHelper.updateTraduction(connect.getPoolConnexion(),
                    prefLabel, idTerm, idLang, idTheso, idUser)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de cohérence de BDD !!");
                FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:renameForm:newPrefLabel", msg);
                return;
            }            
        } else {
            if (!termHelper.addTraduction(connect.getPoolConnexion(),
                    prefLabel, idTerm, idLang, "", "", idTheso, idUser)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de cohérence de BDD !!");
                FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:renameForm:newPrefLabel", msg);
                return;
            }
        }

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(), idTheso,
                conceptView.getNodeConcept().getConcept().getIdConcept());

        conceptView.getConcept(idTheso, conceptView.getNodeConcept().getConcept().getIdConcept(), idLang);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a bien été modifié");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('renameConcept').hide();");

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            // attention, cet update est très important, il faut le faire par composant, sinon, les autres dialogues ne marchent plus
            pf.ajax().update("formRightTab:viewTabConcept:idPrefLabelRow");
        }

        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptView.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptView.getNodeConcept().getConcept().getIdConcept(), idTheso, idLang);
            }
            ((TreeNodeData) tree.getSelectedNode().getData()).setName(prefLabel);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("formLeftTab:tabTree:tree");
            }
        }

        reset("");
    }

    public void cancel() {
        duplicate = false;
    }

    public void infosDelete() {
        String message;
        if (conceptView.getNodeConcept().getNodeNT().isEmpty()) { // pas d'enfants
            message = "La suppression du concept est définitive !!";
        } else {
            message = "La suppression de la branche est définitive !!";
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention !", message);
        FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:deleteConceptForm:currentPrefLabelToDelete", msg);
        PrimeFaces pf = PrimeFaces.current();
        forDelete = true;
    }

    /**
     * permet de supprimer un concept
     *
     * @param idTheso
     * @param idUser
     */
    public void deleteConcept(
            String idTheso,
            int idUser) {

        ConceptHelper conceptHelper = new ConceptHelper();
        if (roleOnThesoBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:deleteConceptForm:currentPrefLabelToDelete", msg);
            return;
        }

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        if (conceptView.getNodeConcept().getNodeNT().isEmpty()) {
            // suppression du concept
            if (!conceptHelper.deleteConcept(connect.getPoolConnexion(),
                    conceptView.getNodeConcept().getConcept().getIdConcept(),
                    idTheso,
                    idUser)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La suppression a échoué !!");
                FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:deleteConceptForm:currentPrefLabelToDelete", msg);
                return;
            }
        } else {
            /// suppression d'une branche
            deleteBranch(
                    conceptView.getNodeConcept().getConcept().getIdConcept(),
                    idTheso, idUser);
        }

        // mise à jour
        PrimeFaces pf = PrimeFaces.current();
        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptView.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptView.getNodeConcept().getConcept().getIdConcept(),
                        idTheso,
                        selectedTheso.getCurrentLang());
            }
            TreeNode parent = tree.getSelectedNode().getParent();
            if (parent != null) {
                parent.getChildren().remove(tree.getSelectedNode());

                if (pf.isAjaxRequest()) {
                    pf.ajax().update("formLeftTab:tabTree:tree");
                }
            }
        }
        if (!conceptView.getNodeConcept().getNodeBT().isEmpty()) {
            conceptView.getConcept(idTheso,
                    conceptView.getNodeConcept().getNodeBT().get(0).getIdConcept(),
                    selectedTheso.getCurrentLang());
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a bien été supprimé");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("formRightTab:viewTabConcept:conceptView");
        }
        PrimeFaces.current().executeScript("PF('deleteConcept').hide();");

        reset("");
    }

    /**
     * permet de supprimer toute la branche avec ses concepts
     *
     * @param selectedNode
     */
    private void deleteBranch(String idConceptTop, String idTheso, int idUser) {
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts = conceptHelper.getIdsOfBranch(
                connect.getPoolConnexion(),
                idConceptTop,
                idTheso);

        // supprimer les concepts
        for (String idConcept : idConcepts) {
            conceptHelper.deleteConceptWithoutControl(connect.getPoolConnexion(),
                    idConcept,
                    idTheso,
                    idUser);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    public void infosArk() {
        String message = "Permet de générer un identifiant Ark, si l'identifiant existe, il sera mise à jour !!";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    
    
    /**
     * permet de générer l'identifiant Ark, s'il n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateArk(){
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts = new ArrayList<>();
        idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        FacesMessage msg;
        if(!conceptHelper.generateArkId(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                idConcepts)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La génération de Ark a échoué !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La génération de Ark a réussi !!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }

    
    /**
     * permet de générer les identifiants Ark pour les concepts qui n'en ont pas
     * si un identifiant n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateArkForConceptWithoutArk(){
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts;
        idConcepts = conceptHelper.getAllIdConceptOfThesaurusWithoutArk(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        
        if(roleOnThesoBean.getNodePreference() == null) return;
        //idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        FacesMessage msg;
        if(!conceptHelper.generateArkId(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                idConcepts)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La génération de Ark a échoué !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);             
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La génération de Ark a réussi !!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }       
    
    /**
     * permet de générer les identifiants Ark pour cette branche,
     * si un identifiant n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateArkForThisBranch(){
        if(roleOnThesoBean.getNodePreference() == null) return;        
        if(conceptView.getNodeConcept() == null) return;
        
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts = conceptHelper.getIdsOfBranch(connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        FacesMessage msg;
        if(!conceptHelper.generateArkId(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                idConcepts)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La génération de Ark a échoué !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);             
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La génération de Ark a réussi !!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }      
    
    /**
     * permet de générer la totalité des identifiants Ark,
     * si un identifiant n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateAllArk(){
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts;
        idConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        
        if(roleOnThesoBean.getNodePreference() == null) return;
        //idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        FacesMessage msg;
        if(!conceptHelper.generateArkId(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                idConcepts)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La génération de Ark a échoué !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);             
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La génération de Ark a réussi !!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }    
    
    
    public void cancelDelete() {
        forDelete = false;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isIsCreated() {
        return isCreated;
    }

    public void setIsCreated(boolean isCreated) {
        this.isCreated = isCreated;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public boolean isForDelete() {
        return forDelete;
    }

    public void setForDelete(boolean forDelete) {
        this.forDelete = forDelete;
    }

}
