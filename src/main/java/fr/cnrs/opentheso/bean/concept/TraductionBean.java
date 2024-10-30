package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.proposition.TraductionPropBean;
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


@Data
@Named(value = "traductionBean")
@SessionScoped
public class TraductionBean implements Serializable {

    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private PropositionBean propositionBean;
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private CurrentUser currentUser;   
    @Autowired @Lazy private LanguageBean languageBean;

    @Autowired
    private TermHelper termHelper;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    private String selectedLang;
    private List<NodeLangTheso> nodeLangs;
    private List<NodeLangTheso> nodeLangsFiltered; // uniquement les langues non traduits
    private List<NodeTermTraduction> nodeTermTraductions;
    private List<NodeTermTraduction> nodeTermTraductionsForEdit;

    private String traductionValue;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeLangs != null) {
            nodeLangs.clear();
            nodeLangs = null;
        }
        if (nodeLangsFiltered != null) {
            nodeLangsFiltered.clear();
            nodeLangsFiltered = null;
        }
        if (nodeTermTraductions != null) {
            nodeTermTraductions.clear();
            nodeTermTraductions = null;
        }
        selectedLang = null;
        traductionValue = null;
    }

    public TraductionBean() {
    }

    public void reset() {
        nodeLangs = selectedTheso.getNodeLangs();
        if (nodeLangsFiltered == null) {
            nodeLangsFiltered = new ArrayList<>();
        } else {
            nodeLangsFiltered.clear();
        }
        nodeTermTraductions = conceptBean.getNodeConcept().getNodeTermTraductions();

        selectedLang = null;
        traductionValue = "";
    }

    public void setTraductionsForEdit() {
        
        reset();
        
        if (nodeTermTraductionsForEdit == null) {
            nodeTermTraductionsForEdit = new ArrayList<>();
        } else {
            nodeTermTraductionsForEdit.clear();
        }

        for (NodeTermTraduction nodeTermTraduction : nodeTermTraductions) {
            NodeTermTraduction nodeTermTraduction1 = new NodeTermTraduction();
            nodeTermTraduction1.setLexicalValue(nodeTermTraduction.getLexicalValue());
            nodeTermTraduction1.setLang(nodeTermTraduction.getLang());
            nodeTermTraductionsForEdit.add(nodeTermTraduction1);
        }
    }

    public void setLangWithNoTraduction() {
        nodeLangs.forEach((nodeLang) -> {
            nodeLangsFiltered.add(nodeLang);
        });

        // les langues à ignorer
        ArrayList<String> langsToRemove = new ArrayList<>();
        langsToRemove.add(conceptBean.getSelectedLang());
        for (NodeTermTraduction nodeTermTraduction : conceptBean.getNodeConcept().getNodeTermTraductions()) {
            langsToRemove.add(nodeTermTraduction.getLang());
        }
        for (NodeLangTheso nodeLang : nodeLangs) {
            if (langsToRemove.contains(nodeLang.getCode())) {
                nodeLangsFiltered.remove(nodeLang);
            }
        }
        if (nodeLangsFiltered.isEmpty()) {
            infoNoTraductionToAdd();
        }
    }

    public void setLangWithNoTraductionProp() {
        
        reset();
        
        nodeLangs.forEach((nodeLang) -> {
            nodeLangsFiltered.add(nodeLang);
        });

        // les langues à ignorer
        ArrayList<String> langsToRemove = new ArrayList<>();
        langsToRemove.add(conceptBean.getSelectedLang());
        for (TraductionPropBean nodeTermTraduction : propositionBean.getProposition().getTraductionsProp()) {
            langsToRemove.add(nodeTermTraduction.getLang());
        }
        for (NodeLangTheso nodeLang : nodeLangs) {
            if (langsToRemove.contains(nodeLang.getCode())) {
                nodeLangsFiltered.remove(nodeLang);
            }
        }
        
        PrimeFaces pf = PrimeFaces.current();
        if (nodeLangsFiltered.isEmpty()) {
            infoNoTraductionToAdd();
            pf.ajax().update("containerIndex:rightTab:idAddTraduction");
        } else {
            pf.executeScript("PF('addTraductionProp').show();");
        }
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void infoNoTraductionToAdd() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", languageBean.getMsg("concept.translate.isTranslatedIntoAllLang"));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter une nouvelle traduction au concept
     *
     * @param idUser
     */
    public void addNewTraduction(int idUser) {
        FacesMessage msg;
        if (traductionValue == null || traductionValue.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Une valeur est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (selectedLang == null || selectedLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Pas de langue choisie !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (selectedTheso.getCurrentIdTheso() == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Aucun thésaurus sélectionné !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (termHelper.isTermExistIgnoreCase(
                connect.getPoolConnexion(),
                traductionValue,
                selectedTheso.getCurrentIdTheso(),
                selectedLang)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " un label identique existe dans cette langue !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (!termHelper.addTraduction(connect.getPoolConnexion(),
                traductionValue,
                conceptBean.getNodeFullConcept().getPrefLabel().getIdTerm(),
                selectedLang, "", "", selectedTheso.getCurrentIdTheso(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur d'ajout de traduction !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
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
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", languageBean.getMsg("concept.translate.success"));
        FacesContext.getCurrentInstance().addMessage(null, msg);

        reset();
        setLangWithNoTraduction();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex:rightTab:idAddTraduction");
            pf.executeScript("PF('addTraduction').show();");
        }
    }

    public void addNewTraductionProposition() {
        FacesMessage msg;
        if (traductionValue == null || traductionValue.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Une valeur est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (selectedLang == null || selectedLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Pas de langue choisie !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (termHelper.isTermExistIgnoreCase(
                connect.getPoolConnexion(),
                traductionValue,
                selectedTheso.getCurrentIdTheso(),
                selectedLang)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Un label identique existe dans cette langue !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        for (TraductionPropBean traductionPropBean : propositionBean.getProposition().getTraductionsProp()) {
            if (selectedLang.equalsIgnoreCase(traductionPropBean.getLang())
                    && traductionValue.equalsIgnoreCase(traductionPropBean.getLexicalValue())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Un label identique existe dans cette langue !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }

        TraductionPropBean traductionProp = new TraductionPropBean();
        traductionProp.setLang(selectedLang);
        traductionProp.setLexicalValue(traductionValue);
        traductionProp.setIdTerm(conceptBean.getNodeConcept().getTerm().getIdTerm());
        traductionProp.setToAdd(true);
        propositionBean.getProposition().getTraductionsProp().add(traductionProp);
        propositionBean.setTraductionAccepted(true);
        
    }

    /**
     * permet de modifier une traduction au concept
     *
     * @param nodeTermTraduction
     * @param idUser
     */
    public void updateTraduction(NodeTermTraduction nodeTermTraduction, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (nodeTermTraduction == null || nodeTermTraduction.getLexicalValue().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " veuillez saisir une valeur !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (termHelper.isTermExistIgnoreCase(
                connect.getPoolConnexion(),
                nodeTermTraduction.getLexicalValue(),
                selectedTheso.getCurrentIdTheso(),
                nodeTermTraduction.getLang())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " un label identique existe dans cette langue !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (!termHelper.updateTraduction(connect.getPoolConnexion(),
                nodeTermTraduction.getLexicalValue(), conceptBean.getNodeConcept().getTerm().getIdTerm(),
                nodeTermTraduction.getLang(),
                selectedTheso.getCurrentIdTheso(), idUser)) {
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
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "traduction modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:rightTab:idRenameTraduction");
            pf.executeScript("PF('renameTraduction').show();");
        }
    }

    public void updateTraductionProp(TraductionPropBean traductionPropBean) {

        if (traductionPropBean.getLexicalValue().isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Veuillez saisir une valeur !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        if (traductionPropBean.isToUpdate() && 
                traductionPropBean.getLexicalValue().equalsIgnoreCase(traductionPropBean.getOldValue())){
            traductionPropBean.setToUpdate(false);
            return;
        }

        // Rechercher dans la base s'il existe un label identique
        if (termHelper.isTermExistIgnoreCase(
                connect.getPoolConnexion(),
                traductionPropBean.getLexicalValue(),
                selectedTheso.getCurrentIdTheso(),
                traductionPropBean.getLang())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Un label identique existe dans cette langue !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        for (int i = 0; i < propositionBean.getProposition().getTraductionsProp().size(); i++) {
            if (propositionBean.getProposition().getTraductionsProp().get(i).getLang()
                    .equals(traductionPropBean.getLang())) {
                if (propositionBean.getProposition().getTraductionsProp().get(i).isToRemove()) {
                    propositionBean.getProposition().getTraductionsProp().get(i).setToRemove(false);
                    propositionBean.getProposition().getTraductionsProp().get(i).setToUpdate(true);
                    propositionBean.getProposition().getTraductionsProp().get(i).setLexicalValue(traductionPropBean.getLexicalValue());
                } else if (propositionBean.getProposition().getTraductionsProp().get(i).isToAdd()) {
                    propositionBean.getProposition().getTraductionsProp().get(i).setLexicalValue(traductionPropBean.getLexicalValue());
                } else {
                    propositionBean.getProposition().getTraductionsProp().get(i).setToUpdate(true);
                    propositionBean.getProposition().getTraductionsProp().get(i).setLexicalValue(traductionPropBean.getLexicalValue());
                }
                propositionBean.setTraductionAccepted(propositionBean.getProposition().getTraductionsProp().get(i).isToUpdate());
            }
        }
        
        propositionBean.checkTraductionPropositionStatus();
    }

    /**
     * permet de modifier toutes les traductions du concept multiple corrections
     *
     * @param idUser
     */
    public void updateAllTraduction(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (nodeTermTraductionsForEdit == null || nodeTermTraductionsForEdit.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " veuillez saisir une valeur !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        boolean toModify;
        boolean isModified = false;

        for (NodeTermTraduction nodeTermTraduction : nodeTermTraductionsForEdit) {
            toModify = false;
            isModified = false;
            /// pour vérifier si le terme a changé
            for (NodeTermTraduction nodeTermTraductionOld : nodeTermTraductions) {
                if (nodeTermTraduction.getLang().equalsIgnoreCase(nodeTermTraductionOld.getLang())) {
                    toModify = !nodeTermTraduction.getLexicalValue().equalsIgnoreCase(nodeTermTraductionOld.getLexicalValue());
                    break;
                }
            }
            if (toModify) {
                if (termHelper.isTermExistIgnoreCase(
                        connect.getPoolConnexion(),
                        nodeTermTraduction.getLexicalValue(),
                        selectedTheso.getCurrentIdTheso(),
                        nodeTermTraduction.getLang())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " un label identique existe dans cette langue : " + nodeTermTraduction.getLang());
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    continue;
                }

                if (!termHelper.updateTraduction(connect.getPoolConnexion(),
                        nodeTermTraduction.getLexicalValue(), conceptBean.getNodeConcept().getTerm().getIdTerm(),
                        nodeTermTraduction.getLang(),
                        selectedTheso.getCurrentIdTheso(), idUser)) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La modification a échoué !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    if (pf.isAjaxRequest()) {
                        pf.ajax().update("messageIndex");
                    }
                    return;
                }
                isModified = true;
            }
        }
        if (isModified) {
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
            
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "traduction modifiée avec succès");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            reset();

            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
                pf.ajax().update("containerIndex:rightTab:idRenameTraduction");
                pf.executeScript("PF('renameTraduction').show();");
            }
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Aucune modification à faire");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    /**
     * permet de supprimer une traduction au concept
     *
     * @param nodeTermTraduction
     * @param idUser
     */
    public void deleteTraduction(NodeTermTraduction nodeTermTraduction, int idUser) {
        FacesMessage msg;
        if (nodeTermTraduction == null || nodeTermTraduction.getLang().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de sélection de tradcution !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (!termHelper.deleteTraductionOfTerm(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getIdTerm(),
                nodeTermTraduction.getLexicalValue(),
                nodeTermTraduction.getLang(),
                selectedTheso.getCurrentIdTheso(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
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
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "traduction supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        //    PrimeFaces.current().executeScript("PF('addNote').hide();");
        reset();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:rightTab:idDeleteTraduction");
            pf.executeScript("PF('deleteTraduction').show();");
        }
    }

    public void deleteTraductionProp(TraductionPropBean traductionPropBean) {

        for (int i = 0; i < propositionBean.getProposition().getTraductionsProp().size(); i++) {
            if (propositionBean.getProposition().getTraductionsProp().get(i).getLexicalValue()
                    .equals(traductionPropBean.getLexicalValue())) {
                if (propositionBean.getProposition().getTraductionsProp().get(i).isToUpdate()) {
                    propositionBean.getProposition().getTraductionsProp().get(i).setToRemove(true);
                    propositionBean.getProposition().getTraductionsProp().get(i).setToUpdate(false);
                    propositionBean.getProposition().getTraductionsProp().get(i).setLexicalValue(
                        propositionBean.getProposition().getTraductionsProp().get(i).getOldValue());
                } else if (propositionBean.getProposition().getTraductionsProp().get(i).isToAdd()) {
                    propositionBean.getProposition().getTraductionsProp().remove(i);
                } else {
                    propositionBean.getProposition().getTraductionsProp().get(i).setToRemove(
                            !propositionBean.getProposition().getTraductionsProp().get(i).isToRemove());
                }
            }
        }
        
        propositionBean.checkTraductionPropositionStatus();
    }

}
