package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
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
@Named(value = "synonymBean")
@SessionScoped
public class SynonymBean implements Serializable {

    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private PropositionBean propositionBean;
    @Autowired @Lazy private CurrentUser currentUser;

    @Autowired
    private TermHelper termHelper;

    private String selectedLang;
    private String selectedValue;
    private List<NodeLangTheso> nodeLangs;
    private List<NodeEM> nodeEMs;
    private List<NodeEM> nodeEMsForEdit;
    private boolean hidden;
    private boolean duplicate;
    private String value;
    // pour garder la valeur sélectionnée pour forcer une modfication en doublon (
    private NodeEM nodeEM;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeLangs != null) {
            nodeLangs.clear();
            nodeLangs = null;
        }
        if (nodeEMs != null) {
            nodeEMs.clear();
            nodeEMs = null;
        }
        if (nodeEMsForEdit != null) {
            nodeEMsForEdit.clear();
            nodeEMsForEdit = null;
        }
        selectedLang = null;
        selectedValue = null;
        value = null;
        nodeEM = null;
    }

    public void reset() {
        hidden = false;
        selectedLang = conceptBean.getSelectedLang();
        nodeLangs = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(connect.getPoolConnexion(), 
                selectedTheso.getCurrentIdTheso(), selectedLang);        

        nodeEMs = conceptBean.getNodeConcept().getNodeEM();
        
        prepareNodeEMForEdit();
        
        value = "";
        duplicate = false;
        this.nodeEM = null;
        
        System.out.println(">> " + nodeEMsForEdit.size());
    }

    private void init() {
        value = "";
        duplicate = false;
        this.nodeEM = null;
    }
   
    public void prepareNodeEMForEdit() {
        nodeEMsForEdit = new ArrayList<>();
        for (NodeEM nodeEM1 : conceptBean.getNodeConcept().getNodeEM()) {
            nodeEM1.setOldValue(nodeEM1.getLexicalValue());
            nodeEM1.setOldHiden(nodeEM1.isHiden());
            nodeEMsForEdit.add(nodeEM1);
        }
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter un synonyme sans controler le nom en doublon
     *
     * @param idUser
     */
    public void addForced(int idUser) {
        // pour la compatibilité avec les anciennes versions
        String use = "USE";
        if (hidden) {
            use = "Hidden";
        }

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        String idTerm = termHelper.getIdTermOfConcept(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        if (!termHelper.addNonPreferredTerm(connect.getPoolConnexion(),
                idTerm,
                value,
                selectedLang,
                selectedTheso.getCurrentIdTheso(),
                "",
                use,
                hidden,
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La création a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            init();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formRightTab:viewTabConcept:addSynonymForm");
            }
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
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Synonyme ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("containerIndex:formLeftTab");
        }
    }

    /**
     * permet de modifier un synonyme sans controle avec doublon
     *
     * @param idUser
     */
    public void updateSynonymForced(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        duplicate = false;
        if (!termHelper.updateTermSynonyme(connect.getPoolConnexion(),
                nodeEM.getOldValue(), nodeEM.getLexicalValue(),
                conceptBean.getNodeConcept().getTerm().getIdTerm(),
                nodeEM.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeEM.isHiden(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La modification a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
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
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Synonyme modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        //    PrimeFaces.current().executeScript("PF('addNote').hide();");

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");;
        }
    }

    /**
     * permet de modifier un synonyme sans controle avec doublon
     *
     * @param nodeEM
     * @param idUser
     */
    public void updateStatus(NodeEM nodeEM, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (!termHelper.updateStatus(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getIdTerm(),
                nodeEM.getLexicalValue(),
                nodeEM.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeEM.isHiden(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La modification a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
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
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Synonyme modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public void cancel() {
        duplicate = false;
        this.nodeEM = null;
    }
}
