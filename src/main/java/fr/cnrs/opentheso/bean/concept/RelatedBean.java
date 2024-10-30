package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.ValidateActionHelper;
import fr.cnrs.opentheso.models.concept.NodeConceptType;
import fr.cnrs.opentheso.models.relations.NodeCustomRelation;
import fr.cnrs.opentheso.models.terms.NodeRT;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "relatedBean")
@SessionScoped
public class RelatedBean implements Serializable {

    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private Tree tree;
    @Autowired @Lazy private CurrentUser currentUser;

    @Autowired
    private ValidateActionHelper validateActionHelper;

    @Autowired
    private SearchHelper searchHelper;

    @Autowired
    private TermHelper termHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private DcElementHelper dcEelmentHelper;

    private NodeSearchMini searchSelected;
    private List<NodeRT> nodeRTs;
    private boolean tagPrefLabel = false;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeRTs != null) {
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
     * permet de retourner la liste des concepts possibles pour ajouter une
     * relation NT (en ignorant les relations interdites) on ignore les concepts
     * de type TT on ignore les concepts de type RT
     */
    public List<NodeSearchMini> getAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && conceptBean.getSelectedLang() != null) {
            liste = searchHelper.searchAutoCompletionForRelation(
                    connect.openConnexionPool(),
                    value,
                    conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso(), true);
        }
        return liste;
    }

    /**
     * permet de retourner la liste des concepts possibles pour ajouter une
     * relation NT (en ignorant les relations interdites) on ignore les concepts
     * de type TT on ignore les concepts de type RT
     *
     * @param value
     * @return
     */
    public List<NodeSearchMini> getAutoCompletCustomRelation(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && conceptBean.getSelectedLang() != null) {
            liste = searchHelper.searchAutoCompletionForCustomRelation(
                    connect.openConnexionPool(),
                    value,
                    conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso());
        }
        return liste;
    }    

    /**
     * permet d'ajouter un lien Qualifier
     *
     * @param idUser
     */
    public void addNewCustomRelationship(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (searchSelected == null || searchSelected.getIdConcept() == null || searchSelected.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        String conceptType = conceptHelper.getTypeOfConcept(
                connect.openConnexionPool(),
                searchSelected.getIdConcept(),
                 selectedTheso.getCurrentIdTheso());
        
        if(StringUtils.isEmpty(conceptType)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Le type de concept n'est pas reconnu !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        
        NodeConceptType nodeConceptType = relationsHelper.getNodeTypeConcept(
                connect.openConnexionPool(), conceptType, selectedTheso.getCurrentIdTheso());
        
        if (!relationsHelper.addCustomRelationship(
                connect.openConnexionPool(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), searchSelected.getIdConcept(), idUser,
                conceptType,
                nodeConceptType.isReciprocal()
            )) {
            
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La création a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);


        conceptHelper.updateDateOfConcept(connect.openConnexionPool(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
                ///// insert DcTermsData to add contributor

        dcEelmentHelper.addDcElementConcept(connect.openConnexionPool(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Relation ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        reset();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex:formRightTab");
        }
    }
    
    /**
     * permet de supprimer une relation Qualificatif au concept
     *
     * @param nodeCustomRelation
     * @param idUser
     */
    public void deleteCustomRelationship(NodeCustomRelation nodeCustomRelation, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (nodeCustomRelation == null || nodeCustomRelation.getTargetConcept() == null || nodeCustomRelation.getTargetConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        if (!relationsHelper.deleteCustomRelationship(connect.openConnexionPool(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeCustomRelation.getTargetConcept(),
                idUser, nodeCustomRelation.getRelation(), nodeCustomRelation.isReciprocal())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(connect.openConnexionPool(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor

        dcEelmentHelper.addDcElementConcept(connect.openConnexionPool(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////         
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", " Relation supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:listConceptSpecAssocies");
            pf.executeScript("PF('deleteQualifierLink').show();");
        }
    }      

    /**
     * permet d'ajouter un
     *
     * @param idUser
     */
    public void addNewRelatedLink(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (searchSelected == null || searchSelected.getIdConcept() == null || searchSelected.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        /// vérifier la cohérence de la relation
        if (!validateActionHelper.isAddRelationRTValid(
                connect.openConnexionPool(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                searchSelected.getIdConcept())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        try {
            Connection conn = connect.openConnexionPool().getConnection();
            conn.setAutoCommit(false);
            if (!relationsHelper.addRelationRT(
                    conn,
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso(), searchSelected.getIdConcept(), idUser)) {
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
        if (tagPrefLabel) {
            String taggedValue = conceptHelper.getLexicalValueOfConcept(connect.openConnexionPool(),
                    searchSelected.getIdConcept(),
                    selectedTheso.getCurrentIdTheso(),
                    conceptBean.getSelectedLang());
            termHelper.updateTraduction(connect.openConnexionPool(),
                    conceptBean.getNodeConcept().getTerm().getLexicalValue() + " (" + taggedValue + ")",
                    conceptBean.getNodeConcept().getTerm().getIdTerm(),
                    conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso(),
                    idUser);
        }

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(connect.openConnexionPool(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor

        dcEelmentHelper.addDcElementConcept(connect.openConnexionPool(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////       
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Relation ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (tagPrefLabel) {
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formRightTab:viewTabConcept:idPrefLabelRow");
            }

            if (CollectionUtils.isNotEmpty(tree.getClickselectedNodes())) {
                // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
                if (!((TreeNodeData) tree.getClickselectedNodes().get(0).getData()).getNodeId().equalsIgnoreCase(
                        conceptBean.getNodeConcept().getConcept().getIdConcept())) {
                    tree.expandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                            selectedTheso.getCurrentIdTheso(),
                            conceptBean.getSelectedLang());
                }
                ((TreeNodeData) tree.getClickselectedNodes().get(0).getData()).setName(conceptBean.getNodeConcept().getTerm().getLexicalValue());
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("formLeftTab:tabTree:tree");
                }
            }
        }
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex:formLeftTab");
            pf.ajax().update("containerIndex:formRightTab");
            pf.executeScript("PF('addRelatedLink').show();");
        }
    }

    /**
     * permet de supprimer une relation RT au concept
     *
     * @param nodeRT
     * @param idUser
     */
    public void deleteRelatedLink(NodeRT nodeRT, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (nodeRT == null || nodeRT.getIdConcept() == null || nodeRT.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (!relationsHelper.deleteRelationRT(connect.openConnexionPool(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeRT.getIdConcept(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(connect.openConnexionPool(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor

        dcEelmentHelper.addDcElementConcept(connect.openConnexionPool(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////          
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", " Relation supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:listConceptSpecAssocies");
            pf.executeScript("PF('deleteRelatedLink').show();");
        }
    }
}
