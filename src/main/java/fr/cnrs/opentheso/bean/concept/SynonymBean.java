package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private DcElementHelper dcElementHelper;

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
        nodeLangs = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), selectedLang);

        nodeEMs = conceptBean.getNodeConcept().getNodeEM();
        
        prepareNodeEMForEdit();
        
        value = "";
        duplicate = false;
        this.nodeEM = null;
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
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor

        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
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
     * permet de modifier un synonyme
     *
     * @param nodeEMLocal
     * @param idUser
     */
    public void updateSynonym(NodeEM nodeEMLocal, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        TermHelper termHelper = new TermHelper();

        if (nodeEMLocal == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        // save de la valeur pour une modification forcée
        this.nodeEM = nodeEMLocal;

        if (!nodeEMLocal.getOldValue().equals(nodeEMLocal.getLexicalValue())) {
            if (termHelper.isTermExist(connect.getPoolConnexion(),
                    nodeEMLocal.getLexicalValue(),
                    selectedTheso.getCurrentIdTheso(),
                    nodeEMLocal.getLang())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                duplicate = true;
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }
            if (termHelper.isAltLabelExist(connect.getPoolConnexion(),
                    nodeEMLocal.getLexicalValue(),
                    selectedTheso.getCurrentIdTheso(),
                    nodeEMLocal.getLang())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                duplicate = true;
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }
            updateSynonymForced(idUser);
        } else {
            if (nodeEMLocal.isOldHiden() != nodeEMLocal.isHiden()) {
                updateStatus(nodeEMLocal, idUser);
            }
        }
        reset();
        prepareNodeEMForEdit();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
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
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
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
     * permet d'ajouter une nouvelle traduction au concept
     *
     * @param idUser
     */
    public void addNewSynonym(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        duplicate = false;
        if (value == null || value.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Une valeur est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (selectedLang == null || selectedLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Pas de langue choisie !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (termHelper.isTermExist(connect.getPoolConnexion(),
                value,
                selectedTheso.getCurrentIdTheso(),
                selectedLang)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            duplicate = true;
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        if (termHelper.isAltLabelExist(connect.getPoolConnexion(),
                value,
                selectedTheso.getCurrentIdTheso(),
                selectedLang)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            duplicate = true;
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        addForced(idUser);
    }

    /**
     * permet d'ajouter une nouvelle traduction au concept
     */
    public void addPropSynonym() {
        FacesMessage msg;
        if (value == null || value.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Une valeur est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (selectedLang == null || selectedLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Pas de langue choisie !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (termHelper.isTermExist(connect.getPoolConnexion(), value,
                selectedTheso.getCurrentIdTheso(), conceptBean.getSelectedLang())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (termHelper.isAltLabelExist(connect.getPoolConnexion(), value,
                selectedTheso.getCurrentIdTheso(), conceptBean.getSelectedLang())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (CollectionUtils.isEmpty(propositionBean.getProposition().getSynonymsProp())) {
            propositionBean.getProposition().setSynonymsProp(new ArrayList<>());
        }

        for (SynonymPropBean synonym : propositionBean.getProposition().getSynonymsProp()) {
            if (synonym.getLexicalValue().equalsIgnoreCase(value)
                    && synonym.getLang().equalsIgnoreCase(selectedLang)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().ajax().update("messageIndex");
                return;
            }
        }

        SynonymPropBean synonymPropBean = new SynonymPropBean();
        synonymPropBean.setToAdd(true);
        synonymPropBean.setHiden(hidden);
        synonymPropBean.setLang(selectedLang);
        synonymPropBean.setLexicalValue(value);
        synonymPropBean.setOldValue(value);
        propositionBean.getProposition().getSynonymsProp().add(synonymPropBean);
        propositionBean.setVarianteAccepted(true);
    }

    public boolean isVarianteMenuDisable() {
        return CollectionUtils.isEmpty(propositionBean.getProposition().getSynonymsProp());
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
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor

        dcElementHelper.addDcElementConcept(connect.getPoolConnexion(),
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

    /**
     * permet de modifier tous les synonymes
     *
     * @param idUser
     */
    public void updateAllSynonyms(int idUser) {

        if (nodeEMsForEdit == null) {
            var msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        for (NodeEM nodeEM1 : nodeEMsForEdit) {
            // save de la valeur pour une modification forcée
            this.nodeEM = nodeEM1;

            if (!nodeEM1.getOldValue().equals(nodeEM1.getLexicalValue())) {
                if (termHelper.isTermExist(connect.getPoolConnexion(),
                        nodeEM1.getLexicalValue(),
                        selectedTheso.getCurrentIdTheso(),
                        nodeEM1.getLang())) {
                    var msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    duplicate = true;
                    PrimeFaces.current().ajax().update("messageIndex");
                    return;
                }
                if (termHelper.isAltLabelExist(connect.getPoolConnexion(),
                        nodeEM1.getLexicalValue(),
                        selectedTheso.getCurrentIdTheso(),
                        nodeEM1.getLang())) {
                    var msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    duplicate = true;
                    PrimeFaces.current().ajax().update("messageIndex");
                    return;
                }
                updateSynonymForced(idUser);
            } else {
                if (nodeEM1.isOldHiden() != nodeEM1.isHiden()) {
                    updateStatus(nodeEM1, idUser);
                }
            }
        }
        reset();
        prepareNodeEMForEdit();
    }

    public void updateAllSynonymsProp() {

        if (CollectionUtils.isEmpty(propositionBean.getProposition().getSynonymsProp())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        for (int i = 0; i < propositionBean.getProposition().getSynonymsProp().size(); i++) {
            if (!propositionBean.getProposition().getSynonymsProp().get(i).getOldValue()
                    .equals(propositionBean.getProposition().getSynonymsProp().get(i).getLexicalValue())) {
                if (termHelper.isTermExist(connect.getPoolConnexion(),
                        propositionBean.getProposition().getSynonymsProp().get(i).getLexicalValue(),
                        selectedTheso.getCurrentIdTheso(),
                        propositionBean.getProposition().getSynonymsProp().get(i).getLang())) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    PrimeFaces.current().ajax().update("messageIndex");
                    return;
                }
                if (termHelper.isAltLabelExist(connect.getPoolConnexion(),
                        propositionBean.getProposition().getSynonymsProp().get(i).getLexicalValue(),
                        selectedTheso.getCurrentIdTheso(),
                        propositionBean.getProposition().getSynonymsProp().get(i).getLang())) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    PrimeFaces.current().ajax().update("messageIndex");
                    return;
                }
                if (!propositionBean.getProposition().getSynonymsProp().get(i).isToAdd()) {
                    propositionBean.getProposition().getSynonymsProp().get(i).setToUpdate(true);
                }
            } else {
                if (propositionBean.getProposition().getSynonymsProp().get(i).isOldHiden()
                        != propositionBean.getProposition().getSynonymsProp().get(i).isHiden()) {
                    if (!propositionBean.getProposition().getSynonymsProp().get(i).isToAdd()) {
                        propositionBean.getProposition().getSynonymsProp().get(i).setToUpdate(true);
                    }
                }
            }
        }
    }

    public void updateSynonymProp(SynonymPropBean synonymPropBean) {

        if (synonymPropBean == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", "pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (StringUtils.isEmpty(synonymPropBean.getLexicalValue())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", "Le champs valeur est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (synonymPropBean.isToUpdate()
                && synonymPropBean.getLexicalValue().equals(synonymPropBean.getOldValue())) {
            synonymPropBean.setToUpdate(false);
            return;
        }

        if (!synonymPropBean.getOldValue().equals(synonymPropBean.getLexicalValue())) {
            if (termHelper.isTermExist(connect.getPoolConnexion(),
                    synonymPropBean.getLexicalValue(),
                    selectedTheso.getCurrentIdTheso(),
                    synonymPropBean.getLang())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "Un label identique existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().ajax().update("messageIndex");
                return;
            }

            if (termHelper.isAltLabelExist(connect.getPoolConnexion(),
                    synonymPropBean.getLexicalValue(),
                    selectedTheso.getCurrentIdTheso(),
                    synonymPropBean.getLang())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "Un label identique existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().ajax().update("messageIndex");
                return;
            }

            if (synonymPropBean.isToRemove()) {
                synonymPropBean.setLexicalValue(synonymPropBean.getOldValue());
            } else if (!synonymPropBean.isToAdd()) {
                synonymPropBean.setToUpdate(true);
            }
        } else {
            if (synonymPropBean.isOldHiden() != synonymPropBean.isHiden()) {
                if (synonymPropBean.isToRemove()) {
                    synonymPropBean.setLexicalValue(synonymPropBean.getOldValue());
                } else if (!synonymPropBean.isToAdd()) {
                    synonymPropBean.setToUpdate(true);
                }
            }
        }

        propositionBean.checkSynonymPropositionStatus();
    }


    /**
     * permet de supprimer un synonyme
     */
    public void deleteSynonym(NodeEM nodeEM, int idUser) {

        if (nodeEM == null) {
            var msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }
        if (!termHelper.deleteNonPreferedTerm(connect.getPoolConnexion(), conceptBean.getNodeConcept().getTerm().getIdTerm(),
                nodeEM.getLang(), nodeEM.getLexicalValue(), selectedTheso.getCurrentIdTheso(), idUser)) {

            var msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
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

        var msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Synonyme supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        PrimeFaces.current().ajax().update("conceptForm:listSynonimesToDelete");
    }

    public void deleteSynonymPropo(SynonymPropBean spb) {

        if (spb == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        for (SynonymPropBean synonym : propositionBean.getProposition().getSynonymsProp()) {
            if (spb.getLexicalValue().equals(synonym.getLexicalValue())) {
                if (synonym.isToAdd()) {
                    propositionBean.getProposition().getSynonymsProp().remove(synonym);
                } else if (synonym.isToUpdate()) {
                    synonym.setToRemove(true);
                    synonym.setToUpdate(false);
                    synonym.setLexicalValue(synonym.getOldValue());
                } else {
                    synonym.setToRemove(!synonym.isToRemove());
                }
            }
        }

        propositionBean.checkSynonymPropositionStatus();
    }

    public void cancel() {
        duplicate = false;
        this.nodeEM = null;
    }
}
