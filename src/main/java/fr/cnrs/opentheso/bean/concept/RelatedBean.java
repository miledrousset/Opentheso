/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ValidateActionHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeRT;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "relatedBean")
@SessionScoped
public class RelatedBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private Tree tree;
   
    private NodeSearchMini searchSelected;
    private ArrayList<NodeRT> nodeRTs;
    private boolean tagPrefLabel = false;
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeRTs != null){
            nodeRTs.clear();
            nodeRTs = null;
        }
        searchSelected = null;
    }     
    
    public RelatedBean() {
    }

    public void reset() {
        nodeRTs = conceptBean.getNodeConcept().getNodeRT();
        searchSelected = null;
        tagPrefLabel = false;
    }
   
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Related !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
    /**
     * permet de retourner la liste des concepts possibles 
     * pour ajouter une relation NT
     * (en ignorant les relations interdites) 
     * on ignore les concepts de type TT
     * on ignore les concepts de type RT
     * @param value
     * @return 
     */
    public List<NodeSearchMini> getAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        SearchHelper searchHelper = new SearchHelper();
        if (selectedTheso.getCurrentIdTheso() != null && conceptBean.getSelectedLang() != null){
            liste = searchHelper.searchAutoCompletionForRelation(
                    connect.getPoolConnexion(),
                    value,
                    conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso());
        }
        return liste;
    }    
    
    /**
     * permet d'ajouter un 
     * @param idUser 
     */
    public void addNewRelatedLink(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(searchSelected == null || searchSelected.getIdConcept() == null || searchSelected.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        /// vérifier la cohérence de la relation
        ValidateActionHelper validateActionHelper = new ValidateActionHelper();
        if(!validateActionHelper.isAddRelationRTValid(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                searchSelected.getIdConcept())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;    
        }        
        
        RelationsHelper relationsHelper = new RelationsHelper();
        try {
            Connection conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            if(!relationsHelper.addRelationRT(
                    conn,
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso(), searchSelected.getIdConcept(), idUser)){
                conn.rollback();
                conn.close();
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La création a échoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;                  
            } else {
                conn.commit();
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SynonymBean.class.getName()).log(Level.SEVERE, null, ex);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur SQL !", ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;                
        }
        
        // mettre à jour le label du concept si l'option TAG est activée
        if(tagPrefLabel) {
            TermHelper termHelper = new TermHelper();
            ConceptHelper conceptHelper = new ConceptHelper();
            String taggedValue = conceptHelper.getLexicalValueOfConcept(connect.getPoolConnexion(),
                    searchSelected.getIdConcept(),
                    selectedTheso.getCurrentIdTheso(),
                    conceptBean.getSelectedLang());
            termHelper.updateTraduction(connect.getPoolConnexion(),
                    conceptBean.getNodeConcept().getTerm().getLexical_value() + " (" + taggedValue + ")",
                    conceptBean.getNodeConcept().getTerm().getId_term(),
                    conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso(),
                    idUser);
        }
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Relation ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if(tagPrefLabel) {
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formRightTab:viewTabConcept:idPrefLabelRow");
            }

            if (tree.getSelectedNode() != null) {
                // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
                if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                        conceptBean.getNodeConcept().getConcept().getIdConcept())) {
                    tree.expandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(), 
                            selectedTheso.getCurrentIdTheso(),
                            conceptBean.getSelectedLang());
                }
                ((TreeNodeData) tree.getSelectedNode().getData()).setName(conceptBean.getNodeConcept().getTerm().getLexical_value());
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("formLeftTab:tabTree:tree");
                }
            }            
        }
        reset();
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex:formLeftTab");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }
    
    /**
     * permet de supprimer une relation RT au concept
     * @param nodeRT
     * @param idUser 
     */
    public void deleteRelatedLink(NodeRT nodeRT, int idUser){
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeRT == null || nodeRT.getIdConcept() == null || nodeRT.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        RelationsHelper relationsHelper = new RelationsHelper();
        if(!relationsHelper.deleteRelationRT(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeRT.getIdConcept(),
                idUser)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }            
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", " Relation supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:listConceptSpecAssocies");
        }  
    }
    public void addReplacedBy() {
        FacesMessage msg;
        
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "");
            FacesContext.getCurrentInstance().addMessage(null, msg);        
        NodeSearchMini nodeSearchMini = searchSelected;

    }

    public NodeSearchMini getSearchSelected() {
        return searchSelected;
    }

    public void setSearchSelected(NodeSearchMini searchSelected) {
        this.searchSelected = searchSelected;
    }

    public ArrayList<NodeRT> getNodeRTs() {
        return nodeRTs;
    }

    public void setNodeRTs(ArrayList<NodeRT> nodeRTs) {
        this.nodeRTs = nodeRTs;
    }

    public boolean isTagPrefLabel() {
        return tagPrefLabel;
    }

    public void setTagPrefLabel(boolean tagPrefLabel) {
        this.tagPrefLabel = tagPrefLabel;
    }


}