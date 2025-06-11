package fr.cnrs.opentheso.bean.proposition;

import fr.cnrs.opentheso.models.propositions.PropositionStatusEnum;
import fr.cnrs.opentheso.models.propositions.Proposition;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesaurusBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.models.propositions.PropositionDao;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.PropositionService;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import fr.cnrs.opentheso.services.ThesaurusService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;


@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "propositionBean")
public class PropositionBean implements Serializable {

    private final IndexSetting indexSetting;
    private final RoleOnThesaurusBean roleOnThesoBean;
    private final SelectedTheso selectedTheso;
    private final RightBodySetting rightBodySetting;
    private final PropositionService propositionService;
    private final LanguageBean languageBean;
    private final PreferenceService preferenceService;
    private final ThesaurusService thesaurusService;

    private Proposition proposition;
    private PropositionDao propositionSelected;
    private List<PropositionDao> propositions;
    private boolean isRubriqueVisible, isNewProposition, isConsultation, prefTermeAccepted, varianteAccepted,
            traductionAccepted, noteAccepted, definitionAccepted, changeNoteAccepted, scopeAccepted,
            editorialNotesAccepted, examplesAccepted, historyAccepted;
    private String nom, email, commentaire, commentaireAdmin, message, actionNom;
    private String filter2 = "3";
    private String showAllPropositions = "1";
    private int nbrNewPropositions;


    public void init() {
        nom = "";
        email = "";
        commentaire = "";
        commentaireAdmin = "";
        message = "";
        propositionSelected = null;
        propositions = null;
    }

    public void onSelectConcept(PropositionDao propositionDao, ConceptView conceptView, CurrentUser currentUser) throws IOException {

        this.propositionSelected = propositionDao;

        var preference = preferenceService.getThesaurusPreferences(propositionDao.getIdTheso());
        if (!preference.isSuggestion()) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("rightbody.proposal.avertissement"));
            return;
        }

        roleOnThesoBean.initNodePref(propositionDao.getIdTheso());
        selectedTheso.setSelectedIdTheso(propositionDao.getIdTheso());
        selectedTheso.setSelectedLang(propositionDao.getLang());
        selectedTheso.setSelectedThesaurusForSearch();
        rightBodySetting.setIndex("3");
        indexSetting.setIsSelectedTheso(true);
        isRubriqueVisible = true;

        conceptView.getConcept(propositionDao.getIdTheso(), propositionDao.getIdConcept(), propositionDao.getLang(), currentUser);

        proposition = new Proposition();
        propositionService.preparerPropositionSelect(proposition, propositionDao);

        nom = propositionDao.getNom();
        email = propositionDao.getEmail();
        commentaire = propositionDao.getCommentaire();
        commentaireAdmin = propositionDao.getAdminComment();

        chercherProposition();
        nbrNewPropositions = propositionService.searchNbrNewProposition();
        PrimeFaces.current().ajax().update("containerIndex:notificationProp");

        varianteAccepted = false;
        traductionAccepted = false;
        noteAccepted = false;
        definitionAccepted = false;
        changeNoteAccepted = false;
        scopeAccepted = false;
        editorialNotesAccepted = false;
        examplesAccepted = false;
        historyAccepted = false;
        isConsultation = true;

        prefTermeAccepted = proposition.isUpdateNomConcept();
        checkSynonymPropositionStatus();
        checkTraductionPropositionStatus();
        checkNotePropositionStatus();
    }

    public void checkSynonymPropositionStatus() {
        for (SynonymPropBean synonymPropBean : proposition.getSynonymsProp()) {
            if (synonymPropBean.isToAdd() || synonymPropBean.isToRemove() || synonymPropBean.isToUpdate()) {
                varianteAccepted = true;
            }
        }
    }

    public void checkTraductionPropositionStatus() {
        for (TraductionPropBean traductionPropBean : proposition.getTraductionsProp()) {
            if (traductionPropBean.isToAdd() || traductionPropBean.isToRemove() || traductionPropBean.isToUpdate()) {
                traductionAccepted = true;
            }
        }
    }

    public void checkNotePropositionStatus() {
        if (proposition.getNote() != null) {
            if (proposition.getNote().isToAdd() || proposition.getNote().isToRemove() || proposition.getNote().isToUpdate()) {
                noteAccepted = true;
            }
        }

        if (proposition.getDefinition() != null) {
            if (proposition.getDefinition().isToAdd() || proposition.getDefinition().isToRemove() || proposition.getDefinition().isToUpdate()) {
                definitionAccepted = true;
            }
        }

        if (proposition.getChangeNote() != null) {
            if (proposition.getChangeNote().isToAdd() || proposition.getChangeNote().isToRemove() || proposition.getChangeNote().isToUpdate()) {
                changeNoteAccepted = true;
            }
        }

        if (proposition.getScopeNote() != null) {
            if (proposition.getScopeNote().isToAdd() || proposition.getScopeNote().isToRemove() || proposition.getScopeNote().isToUpdate()) {
                scopeAccepted = true;
            }
        }

        if (proposition.getEditorialNote() != null) {
            if (proposition.getEditorialNote().isToAdd() || proposition.getEditorialNote().isToRemove() || proposition.getEditorialNote().isToUpdate()) {
                editorialNotesAccepted = true;
            }
        }

        if (proposition.getExample() != null) {
            if (proposition.getExample().isToAdd() || proposition.getExample().isToRemove() || proposition.getExample().isToUpdate()) {
                examplesAccepted = true;
            }
        }

        if (proposition.getHistoryNote() != null) {
            if (proposition.getHistoryNote().isToAdd() || proposition.getHistoryNote().isToRemove() || proposition.getHistoryNote().isToUpdate()) {
                historyAccepted = true;
            }
        }
    }

    public void annuler() {
        rightBodySetting.setIndex("0");
    }

    public void afficherPropositionsNotification() {
        if(StringUtils.isEmpty(selectedTheso.getSelectedIdTheso() )) return;
        chercherProposition();
        PrimeFaces.current().ajax().update("listPropositionsPanel");
        PrimeFaces.current().executeScript("showListPropositionsBar();");
    }

    public void chercherProposition() {
        String idTheso = selectedTheso.getSelectedIdTheso();//"2".equals(filter2) ? selectedTheso.getSelectedIdTheso() : "%";
        if(StringUtils.isEmpty(idTheso)) return;

        if ("1".equalsIgnoreCase(showAllPropositions)) {
            propositions = propositionService.searchPropositionsNonTraitee(idTheso);
        } else {
            propositions = propositionService.searchAllPropositions(idTheso);
        }
    }

    public void searchNewPropositions() {
        nbrNewPropositions = propositionService.searchNbrNewProposition();
        PrimeFaces.current().ajax().update("containerIndex:notificationProp");
    }

    public void preparerConfirmationDialog(String action) {
        actionNom = action;
        switch (actionNom) {
            case "envoyerProposition":
                message = languageBean.getMsg("rightbody.proposal.confirmSendProposal");
                break;
            case "approuverProposition":
                message = languageBean.getMsg("rightbody.proposal.confirmValidateProposal");
                break;
            case "refuserProposition":
                message = languageBean.getMsg("rightbody.proposal.confirmRejectProposal");
                break;
            case "supprimerProposition":
                message = languageBean.getMsg("rightbody.proposal.confirmDeleteProposal");
                break;
            default:
                message = languageBean.getMsg("rightbody.proposal.confirmCancelProposal");
        }
        PrimeFaces.current().executeScript("PF('confirmDialog').show();");
    }

    public void executionAction() throws IOException {
        if (null != actionNom) {
            if (propositionSelected != null) {
                propositionSelected.setAdminComment(commentaireAdmin);
                propositionSelected.setThesoName(selectedTheso.getThesoName());
            }
            switch (actionNom) {
                case "envoyerProposition":
                    envoyerProposition();
                    break;
                case "approuverProposition":
                    propositionService.insertProposition(proposition, propositionSelected, commentaireAdmin,
                            prefTermeAccepted, varianteAccepted, traductionAccepted,
                            noteAccepted, definitionAccepted, changeNoteAccepted, scopeAccepted,
                            editorialNotesAccepted, examplesAccepted, historyAccepted);
                    switchToConceptOnglet();
                    showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("rightbody.proposal.confirmIntegratedProposal") + " '"
                            + propositionSelected.getNomConcept() + "' (" + propositionSelected.getIdTheso() + ") !");
                    break;
                case "refuserProposition":
                    propositionService.refuserProposition(propositionSelected, commentaireAdmin);
                    switchToConceptOnglet();
                    showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("rightbody.proposal.confirmProposalForConcept") + " '"
                            + propositionSelected.getNomConcept() + "' (" + propositionSelected.getIdTheso() + ") a été refusée avec succès !");
                    break;
                case "supprimerProposition":
                    propositionService.supprimerPropostion(propositionSelected);
                    switchToConceptOnglet();
                    showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("rightbody.proposal.confirmProposalForConcept") + " '" + propositionSelected.getNomConcept()
                            + "' (" + propositionSelected.getIdTheso() + ") " + languageBean.getMsg("rightbody.proposal.confirmProposalWasDeleted") + "!");
                    break;
                case "annulerProposition":
                    annulerPropostion();
                    break;
            }
        }

        chercherProposition();
        nbrNewPropositions = propositionService.searchNbrNewProposition();
        PrimeFaces.current().ajax().update("containerIndex:notificationProp");


        PrimeFaces.current().executeScript("PF('confirmDialog').hide();");
    }

    public void switchToNouvelleProposition(NodeConcept nodeConcept, CurrentUser currentUser) {
        init();

        isNewProposition = true;
        isRubriqueVisible = true;
        if (currentUser.getNodeUser() == null) {
            rightBodySetting.setIndex("2");
        } else {
            if (roleOnThesoBean.getNodeUserRoleGroup().isContributor()) {
                rightBodySetting.setIndex("2");
            } else {
                rightBodySetting.setIndex("3");
            }
        }

        proposition = propositionService.selectProposition(nodeConcept);
        isConsultation = false;

        if (!ObjectUtils.isEmpty(currentUser.getNodeUser())) {
            nom = currentUser.getNodeUser().getName();
            email = currentUser.getNodeUser().getMail();
        } else {
            nom = "";
            email = "";
        }
    }

    public void updateNomConcept() {

        if (StringUtils.isEmpty(proposition.getNomConceptProp())) {
            proposition.setNomConceptProp("");
            proposition.setUpdateNomConcept(false);

            showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("rightbody.proposal.alertEmptyLabel"));
        } else {
            if (propositionService.updateNomConcept(proposition.getNomConceptProp())) {
                proposition.setUpdateNomConcept(true);
            } else {
                proposition.setNomConceptProp("");
                proposition.setUpdateNomConcept(false);
            }
        }

        prefTermeAccepted = proposition.isUpdateNomConcept();
        PrimeFaces.current().executeScript("PF('nouveauNomConcept').hide();");
    }

    private void switchToConceptOnglet() {
        rightBodySetting.setIndex("0");
        isRubriqueVisible = false;
    }

    public void annulerPropostion() {
        switchToConceptOnglet();
        proposition = null;
        nom = "";
        email = "";
    }

    private void envoyerProposition() {

        if (StringUtils.isEmpty(nom)) {
            showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("rightbody.proposal.alertEmptyName"));
            return;
        }

        if (StringUtils.isEmpty(email)) {
            showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("rightbody.proposal.alertEmptyMail"));
            return;
        }
        if (StringUtils.isEmpty(commentaire)) {
            showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("candidat.send_message.msg1"));
            return;
        }

        if (StringUtils.isEmpty(proposition.getNomConceptProp()) && !isSynchroProPresent() && !isTraductionProPresent()
                && !isNoteProPresent() && !isChangeNoteProPresent() && !isDefinitionProPresent()
                && !isEditorialNoteProPresent() && !isExempleNoteProPresent()
                && !isHistoryNoteProPresent() && !isScopeNoteProPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("rightbody.proposal.alertEmptyProposal"));
            return;
        }

        var thesaurusName = thesaurusService.getTitleOfThesaurus(selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        if (propositionService.envoyerProposition(proposition, nom, email, commentaire, thesaurusName)) {
            showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("rightbody.proposal.confirmProposalSent"));
        }

        switchToConceptOnglet();
    }

    private boolean isSynchroProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getSynonymsProp())) {
            for (SynonymPropBean synonymProp : proposition.getSynonymsProp()) {
                if (synonymProp.isToAdd() || synonymProp.isToRemove() || synonymProp.isToUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isHistoryNoteProPresent() {
        if (proposition.getHistoryNote() != null) {
            if (proposition.getHistoryNote().isToAdd() || proposition.getHistoryNote().isToRemove() || proposition.getHistoryNote().isToUpdate()) {
                return true;
            }
        }
        return false;
    }

    private boolean isScopeNoteProPresent() {
        if (proposition.getScopeNote() != null) {
            if (proposition.getScopeNote().isToAdd() || proposition.getScopeNote().isToRemove() || proposition.getScopeNote().isToUpdate()) {
                return true;
            }
        }

        return false;
    }

    private boolean isExempleNoteProPresent() {
        if (proposition.getExample() != null) {
            if (proposition.getExample().isToAdd() || proposition.getExample().isToRemove() || proposition.getExample().isToUpdate()) {
                return true;
            }
        }
        return false;
    }

    private boolean isEditorialNoteProPresent() {
        if (proposition.getEditorialNote() != null) {
            if (proposition.getEditorialNote().isToAdd() || proposition.getEditorialNote().isToRemove() || proposition.getEditorialNote().isToUpdate()) {
                return true;
            }
        }
        return false;
    }

    private boolean isNoteProPresent() {
        if (proposition.getNote() != null) {
            if (proposition.getNote().isToAdd() || proposition.getNote().isToRemove() || proposition.getNote().isToUpdate()) {
                return true;
            }
        }
        return false;
    }

    private boolean isChangeNoteProPresent() {
        if (proposition.getChangeNote() != null) {
            if (proposition.getChangeNote().isToAdd() || proposition.getChangeNote().isToRemove() || proposition.getChangeNote().isToUpdate()) {
                return true;
            }
        }

        return false;
    }

    private boolean isDefinitionProPresent() {
        if (proposition.getDefinition() != null) {
            if (proposition.getDefinition().isToAdd() || proposition.getDefinition().isToRemove() || proposition.getDefinition().isToUpdate()) {
                return true;
            }
        }
        return false;
    }

    private boolean isTraductionProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getTraductionsProp())) {
            for (TraductionPropBean traductionProp : proposition.getTraductionsProp()) {
                if (traductionProp.isToAdd() || traductionProp.isToRemove() || traductionProp.isToUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean showButtonDecision() {
        return propositionSelected != null && (PropositionStatusEnum.LU.name().equals(propositionSelected.getStatus())
                || PropositionStatusEnum.ENVOYER.name().equals(propositionSelected.getStatus()));
    }

    public boolean showButtonAction() {
        return (proposition != null && propositionSelected == null) || showButtonDecision();
    }

    private void showMessage(FacesMessage.Severity type, String message) {
        FacesMessage msg = new FacesMessage(type, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public Boolean isCanMakeAction(CurrentUser currentUser) {
        return (currentUser.getNodeUser() != null && !currentUser.getNodeUser().getMail().equalsIgnoreCase(email))
                && (currentUser.getNodeUser().isSuperAdmin() || roleOnThesoBean.isAdminOnThisThesaurus());
    }

    public boolean isSameUser(CurrentUser currentUser) {
        if (currentUser.getNodeUser() != null) {
            return currentUser.getNodeUser().getMail().equalsIgnoreCase(email);
        }
        return false;
    }

    public boolean isIsConsultation() {
        return isConsultation;
    }

}
