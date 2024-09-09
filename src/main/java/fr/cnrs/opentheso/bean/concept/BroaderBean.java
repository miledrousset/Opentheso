package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.ValidateActionHelper;
import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
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

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "broaderBean")
@SessionScoped
public class BroaderBean implements Serializable {

    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private Tree tree;
    @Autowired @Lazy private CurrentUser currentUser;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private ValidateActionHelper validateActionHelper;

    @Autowired
    private SearchHelper searchHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    private NodeSearchMini searchSelected;
    private List<NodeBT> nodeBTs;

    public void clear() {
        searchSelected = null;
        if (nodeBTs != null) {
            nodeBTs.clear();
            nodeBTs = null;
        }
    }

    public void reset() {
        nodeBTs = conceptBean.getNodeConcept().getNodeBT();
        searchSelected = null;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Related !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de retourner la liste des concepts possibles pour ajouter une
     * relation NT (en ignorant les relations interdites) on ignore les concepts
     * de type TT on ignore les concepts de type RT
     *
     * @param value
     * @return
     */
    public List<NodeSearchMini> getAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && conceptBean.getSelectedLang() != null) {
            liste = searchHelper.searchAutoCompletionForRelation(
                    connect.getPoolConnexion(),
                    value,
                    conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso(), true);
        }
        return liste;
    }

    /**
     * permet d'ajouter un
     *
     * @param idUser
     */
    public void addNewBroaderLink(int idUser) {

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (searchSelected == null || searchSelected.getIdConcept() == null || searchSelected.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        /// vérifier la cohérence de la relation
        if (!validateActionHelper.isAddRelationBTValid(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                searchSelected.getIdConcept())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        try {
            Connection conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            if (!relationsHelper.addRelationBT(
                    conn,
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso(), searchSelected.getIdConcept(), idUser)) {
                conn.rollback();
                conn.close();
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La création a échoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            } else {
                conn.commit();
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SynonymBean.class.getName()).log(Level.SEVERE, null, ex);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur SQL !", ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        // on vérifie si le concept qui a été ajouté était TopTerme, alors on le rend plus TopTerm pour éviter les boucles à l'infini
        if (conceptHelper.isTopConcept(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso())) {
            if (!conceptHelper.setNotTopConcept(connect.getPoolConnexion(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !",
                        " erreur en enlevant le concept du TopConcept, veuillez utiliser les outils de coorection de cohérence !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }
        }
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor

        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////         
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Relation ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        tree.initAndExpandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getSelectedLang());

        PrimeFaces.current().executeScript("srollToSelected();");
        PrimeFaces.current().executeScript("PF('addBroaderLink').hide();");

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }

    }

    /**
     * permet de supprimer une relation BT au concept
     *
     * @param nodeBT
     * @param idUser
     */
    public void deleteBroaderLink(NodeBT nodeBT, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (nodeBT == null || nodeBT.getIdConcept() == null || nodeBT.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (!relationsHelper.deleteRelationBT(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeBT.getIdConcept(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        // on vérifie si le concept en cours n'a plus de BT, on le rend TopTerme
        if (!relationsHelper.isConceptHaveRelationBT(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso())) {
            if (!conceptHelper.setTopConcept(
                    connect.getPoolConnexion(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !",
                        " erreur en passant le concept et TopConcept, veuillez utiliser les outils de coorection de cohérence !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }
        }

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor

        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////          
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", " Relation supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("formRightTab:viewTabConcept:idConceptBroader");
            pf.ajax().update("formRightTab:viewTabConcept:deleteBroaderLinkForm");
        }

        tree.initAndExpandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getSelectedLang());
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }

        PrimeFaces.current().executeScript("srollToSelected();");
        PrimeFaces.current().executeScript("PF('deleteBroaderLink').hide();");
    }

}
