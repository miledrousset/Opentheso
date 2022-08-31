package fr.cnrs.opentheso.bean.proposition;

import fr.cnrs.opentheso.bean.proposition.model.PropositionStatusEnum;
import fr.cnrs.opentheso.bean.proposition.model.Proposition;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.dao.PropositionDao;
import fr.cnrs.opentheso.bean.proposition.service.PropositionService;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.search.SearchBean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.PrimeFaces;


@Named(value = "propositionBean")
@SessionScoped
public class PropositionBean implements Serializable {

    @Inject
    private CurrentUser currentUser;

    @Inject
    private ConceptView conceptView;

    @Inject
    private IndexSetting indexSetting;

    @Inject
    private RoleOnThesoBean roleOnThesoBean;

    @Inject
    private SelectedTheso selectedTheso;

    @Inject
    private RightBodySetting rightBodySetting;

    @Inject
    private PropositionService propositionService;

    @Inject
    private SearchBean searchBean;

    private boolean isRubriqueVisible;
    private Proposition proposition;
    private String nom, email, commentaire, commentaireAdmin;
    private String message;
    private String actionNom;
    private String showAllPropositions = "1";
    private int nbrNewPropositions, filter2 = 1;

    private PropositionDao propositionSelected;
    private List<PropositionDao> propositions;
    
    private boolean propositionVisibleControle;
    private boolean prefTermeAccepted, varianteAccepted, traductionAccepted,
            noteAccepted, definitionAccepted, changeNoteAccepted, scopeAccepted,
            editorialNotesAccepted, examplesAccepted, historyAccepted;
    

    public void onSelectConcept(PropositionDao propositionDao) throws IOException {

        this.propositionSelected = propositionDao;

        roleOnThesoBean.initNodePref(propositionDao.getIdTheso());
        selectedTheso.setSelectedIdTheso(propositionDao.getIdTheso());
        selectedTheso.setSelectedLang(propositionDao.getLang());
        selectedTheso.setSelectedThesoForSearch();
        rightBodySetting.setIndex("3");
        indexSetting.setIsSelectedTheso(true);
        isRubriqueVisible = true;

        conceptView.getConcept(propositionDao.getIdTheso(), propositionDao.getIdConcept(), propositionDao.getLang());

        proposition = new Proposition();
        propositionService.preparerPropositionSelect(proposition, propositionDao);

        nom = propositionDao.getNom();
        email = propositionDao.getEmail();

        propositions = propositionService.searchAllPropositions(null);
        nbrNewPropositions = propositionService.searchNbrNewProposition();
        
        afficherListPropositions();
        
        varianteAccepted = false;
        traductionAccepted = false;
        noteAccepted = false;
        definitionAccepted = false;
        changeNoteAccepted = false;
        scopeAccepted = false;
        editorialNotesAccepted = false;
        examplesAccepted = false;
        historyAccepted = false;
        
        prefTermeAccepted = proposition.isUpdateNomConcept();
        checkSynonymPropositionStatus();
        checkTraductionPropositionStatus();
        checkNotePropositionStatus();
    }
    
    public void checkSynonymPropositionStatus() {
        for (SynonymPropBean synonymPropBean : proposition.getSynonymsProp()) {
            if (synonymPropBean.isToAdd() || synonymPropBean.isToRemove() 
                    || synonymPropBean.isToUpdate()) {
                varianteAccepted = true;
            }
        }
    }
    
    public void checkTraductionPropositionStatus() {
        for (TraductionPropBean traductionPropBean : proposition.getTraductionsProp()) {
            if (traductionPropBean.isToAdd() || traductionPropBean.isToRemove() 
                    || traductionPropBean.isToUpdate()) {
                traductionAccepted = true;
            }
        }
    }
    
    public void checkNotePropositionStatus() {
        
        for (NotePropBean note : proposition.getNotes()) {
            if (note.isToAdd() || note.isToRemove() || note.isToUpdate()) {
                noteAccepted = true;
            }
        }
        
        for (NotePropBean note : proposition.getDefinitions()) {
            if (note.isToAdd() || note.isToRemove() || note.isToUpdate()) {
                definitionAccepted = true;
            }
        }
        
        for (NotePropBean note : proposition.getChangeNotes()) {
            if (note.isToAdd() || note.isToRemove() || note.isToUpdate()) {
                changeNoteAccepted = true;
            }
        }
        
        for (NotePropBean note : proposition.getScopeNotes()) {
            if (note.isToAdd() || note.isToRemove() || note.isToUpdate()) {
                scopeAccepted = true;
            }
        }
        
        for (NotePropBean note : proposition.getEditorialNotes()) {
            if (note.isToAdd() || note.isToRemove() || note.isToUpdate()) {
                editorialNotesAccepted = true;
            }
        }
        
        for (NotePropBean note : proposition.getExamples()) {
            if (note.isToAdd() || note.isToRemove() || note.isToUpdate()) {
                examplesAccepted = true;
            }
        }
        
        for (NotePropBean note : proposition.getHistoryNotes()) {
            if (note.isToAdd() || note.isToRemove() || note.isToUpdate()) {
                historyAccepted = true;
            }
        }
    }
    
    public void afficherPropositionsNotification() {
        afficherListPropositions();
        if (searchBean.isSearchVisibleControle()) {
            PrimeFaces.current().executeScript("disparaitre();");
            PrimeFaces.current().executeScript("afficher();");
            searchBean.setBarVisisble(true);
            searchBean.setSearchResultVisible(false);
            searchBean.setSearchVisibleControle(false);
            propositionVisibleControle = true;
        } else {
            if (searchBean.isBarVisisble()) {           
                PrimeFaces.current().executeScript("disparaitre();");
                searchBean.setBarVisisble(false);
                propositionVisibleControle = false;
            } else {
                PrimeFaces.current().executeScript("afficher();");
                searchBean.setBarVisisble(true);
                propositionVisibleControle = true;
            }
        }
    }

    public void afficherListPropositions() {
        chercherProposition();
        searchBean.setSearchResultVisible(false);
    }
    
    public void chercherProposition() {
        propositions = new ArrayList<>();
        String idTheso = filter2 == 2 ? selectedTheso.getSelectedIdTheso() : "%";
        switch (showAllPropositions) {
            case "1" :
                propositions = propositionService.searchPropositionsNonTraitter(idTheso);
                break;
            case "2" :
                propositions = propositionService.searchOldPropositions(idTheso);
                break;
            default:
                propositions = propositionService.searchAllPropositions(idTheso);
        }
    }

    public void searchNewPropositions() {
        nbrNewPropositions = propositionService.searchNbrNewProposition();
    }

    public void preparerConfirmationDialog(String action) {
        actionNom = action;
        switch (actionNom) {
            case "envoyerProposition":
                message = "Est ce que vous êtes sur de vouloir ENVOYER votre proposition ?";
                break;
            case "approuverProposition":
                message = "Est ce que vous êtes sur de vouloir VALIDER la proposition ?";
                break;
            case "refuserProposition":
                message = "Est ce que vous êtes sur de vouloir REFUSER la proposition ?";
                break;
            case "supprimerProposition":
                message = "Est ce que vous êtes sur de vouloir SUPPRIMER la proposition ?";
                break;
            default:
                message = "Est ce que vous êtes sur de vouloir ANNULER la proposition ?";
                ;
        }
        PrimeFaces.current().executeScript("PF('confirmDialog').show();");
    }

    public void executionAction() throws IOException {
        if (null != actionNom) {
            switch (actionNom) {
                case "envoyerProposition":
                    envoyerProposition();
                    break;
                case "approuverProposition":
                    propositionService.insertProposition(proposition, propositionSelected, commentaireAdmin,
                            prefTermeAccepted, varianteAccepted, traductionAccepted,
                            noteAccepted, definitionAccepted, changeNoteAccepted, scopeAccepted,
                            editorialNotesAccepted, examplesAccepted, historyAccepted);
                    switchToConceptInglet();
                    showMessage(FacesMessage.SEVERITY_INFO, "Proposition integrée avec sucée dans le concept '"
                            + propositionSelected.getNomConcept() + "' (" + propositionSelected.getIdTheso() + ") !");
                    break;
                case "refuserProposition":
                    propositionService.refuserProposition(propositionSelected, commentaireAdmin);
                    switchToConceptInglet();
                    showMessage(FacesMessage.SEVERITY_INFO, "Proposition pour le concept '"
                            + propositionSelected.getNomConcept() + "' (" + propositionSelected.getIdTheso() + ") refusée avec sucée !");
                    break;
                case "supprimerProposition":
                    propositionService.supprimerPropostion(propositionSelected);
                    switchToConceptInglet();
                    showMessage(FacesMessage.SEVERITY_INFO, "Proposition pour le concept  '" + propositionSelected.getNomConcept()
                            + "' (" + propositionSelected.getIdTheso() + ") suppprimée avec sucée !");
                    break;
                case "annulerProposition":
                    annulerPropostion();
                    break;
            }
        }
        PrimeFaces.current().executeScript("PF('confirmDialog').hide();");
    }

    public void switchToNouvelleProposition(NodeConcept nodeConcept) {

        isRubriqueVisible = true;
        rightBodySetting.setIndex(currentUser.getNodeUser() == null ? "2" : "3");

        proposition = propositionService.selectProposition(nodeConcept);

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
            showMessage(FacesMessage.SEVERITY_ERROR, "Le label est oubligatoire !");
        } else {
            if (propositionService.updateNomConcept(proposition.getNomConceptProp())) {
                proposition.setUpdateNomConcept(true);
            } else {
                proposition.setNomConceptProp("");
                proposition.setUpdateNomConcept(false);
            }
        }

        prefTermeAccepted = proposition.isUpdateNomConcept();
        PrimeFaces.current().executeScript("PF('nouveauNomConcept').hiden();");
    }

    private void switchToConceptInglet() {
        rightBodySetting.setIndex("0");
        isRubriqueVisible = false;
    }

    public void annulerPropostion() {
        switchToConceptInglet();
        proposition = null;
        nom = "";
        email = "";
    }

    private void envoyerProposition() {

        if (StringUtils.isEmpty(nom)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le champs nom est oubligatoire !");
            return;
        }

        if (StringUtils.isEmpty(email)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le champs email est oubligatoire !");
            return;
        }

        if (StringUtils.isEmpty(proposition.getNomConceptProp()) && !isSynchroProPresent() && !isTraductionProPresent()
                && !isNoteProPresent() && !isChangeNoteProPresent() && !isDefinitionProPresent()
                && !isEditorialNoteProPresent() && !isExempleNoteProPresent()
                && !isHistoryNoteProPresent() && !isScopeNoteProPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous devez proposer au moins une modification !");
            return;
        }

        if (propositionService.envoyerProposition(proposition, nom, email, commentaire)) {
            showMessage(FacesMessage.SEVERITY_INFO, "Proposition envoyée !");
        }

        switchToConceptInglet();
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
        if (CollectionUtils.isNotEmpty(proposition.getHistoryNotes())) {
            for (NotePropBean history : proposition.getHistoryNotes()) {
                if (history.isToAdd() || history.isToRemove() || history.isToUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isScopeNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getScopeNotes())) {
            for (NotePropBean scope : proposition.getScopeNotes()) {
                if (scope.isToAdd() || scope.isToRemove() || scope.isToUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isExempleNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getExamples())) {
            for (NotePropBean exemple : proposition.getExamples()) {
                if (exemple.isToAdd() || exemple.isToRemove() || exemple.isToUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEditorialNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getEditorialNotes())) {
            for (NotePropBean editorialNote : proposition.getEditorialNotes()) {
                if (editorialNote.isToAdd() || editorialNote.isToRemove() || editorialNote.isToUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getNotes())) {
            for (NotePropBean notePropBean : proposition.getNotes()) {
                if (notePropBean.isToAdd() || notePropBean.isToRemove() || notePropBean.isToUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isChangeNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getChangeNotes())) {
            for (NotePropBean notePropBean : proposition.getChangeNotes()) {
                if (notePropBean.isToAdd() || notePropBean.isToRemove() || notePropBean.isToUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDefinitionProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getDefinitions())) {
            for (NotePropBean notePropBean : proposition.getDefinitions()) {
                if (notePropBean.isToAdd() || notePropBean.isToRemove() || notePropBean.isToUpdate()) {
                    return true;
                }
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
        return propositionSelected == null || (!PropositionStatusEnum.APPROUVER.name().equals(propositionSelected.getStatus())
                && !PropositionStatusEnum.REFUSER.name().equals(propositionSelected.getStatus()));
    }

    private void showMessage(FacesMessage.Severity type, String message) {
        FacesMessage msg = new FacesMessage(type, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public boolean isIsRubriqueVisible() {
        return isRubriqueVisible;
    }

    public void setIsRubriqueVisible(boolean isRubriqueVisible) {
        this.isRubriqueVisible = isRubriqueVisible;
    }

    public Proposition getProposition() {
        return proposition;
    }

    public void setProposition(Proposition proposition) {
        this.proposition = proposition;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public List<PropositionDao> getPropositions() {
        return propositions;
    }

    public void setPropositions(List<PropositionDao> propositions) {
        this.propositions = propositions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActionNom() {
        return actionNom;
    }

    public void setActionNom(String actionNom) {
        this.actionNom = actionNom;
    }

    public int getNbrNewPropositions() {
        return nbrNewPropositions;
    }

    public void setNbrNewPropositions(int nbrNewPropositions) {
        this.nbrNewPropositions = nbrNewPropositions;
    }

    public String getShowAllPropositions() {
        return showAllPropositions;
    }

    public void setShowAllPropositions(String showAllPropositions) {
        this.showAllPropositions = showAllPropositions;
    }

    public int getFilter2() {
        return filter2;
    }

    public void setFilter2(int filter2) {
        this.filter2 = filter2;
    }

    public String getCommentaireAdmin() {
        return commentaireAdmin;
    }

    public void setCommentaireAdmin(String commentaireAdmin) {
        this.commentaireAdmin = commentaireAdmin;
    }

    public boolean isPrefTermeAccepted() {
        return prefTermeAccepted;
    }

    public void setPrefTermeAccepted(boolean prefTermeAccepted) {
        this.prefTermeAccepted = prefTermeAccepted;
    }

    public boolean isVarianteAccepted() {
        return varianteAccepted;
    }

    public void setVarianteAccepted(boolean varianteAccepted) {
        this.varianteAccepted = varianteAccepted;
    }

    public boolean isTraductionAccepted() {
        return traductionAccepted;
    }

    public void setTraductionAccepted(boolean traductionAccepted) {
        this.traductionAccepted = traductionAccepted;
    }

    public boolean isNoteAccepted() {
        return noteAccepted;
    }

    public void setNoteAccepted(boolean noteAccepted) {
        this.noteAccepted = noteAccepted;
    }

    public boolean isDefinitionAccepted() {
        return definitionAccepted;
    }

    public void setDefinitionAccepted(boolean definitionAccepted) {
        this.definitionAccepted = definitionAccepted;
    }

    public boolean isChangeNoteAccepted() {
        return changeNoteAccepted;
    }

    public void setChangeNoteAccepted(boolean changeNoteAccepted) {
        this.changeNoteAccepted = changeNoteAccepted;
    }

    public boolean isScopeAccepted() {
        return scopeAccepted;
    }

    public void setScopeAccepted(boolean scopeAccepted) {
        this.scopeAccepted = scopeAccepted;
    }

    public boolean isEditorialNotesAccepted() {
        return editorialNotesAccepted;
    }

    public void setEditorialNotesAccepted(boolean editorialNotesAccepted) {
        this.editorialNotesAccepted = editorialNotesAccepted;
    }

    public boolean isExamplesAccepted() {
        return examplesAccepted;
    }

    public void setExamplesAccepted(boolean examplesAccepted) {
        this.examplesAccepted = examplesAccepted;
    }

    public boolean isHistoryAccepted() {
        return historyAccepted;
    }

    public void setHistoryAccepted(boolean historyAccepted) {
        this.historyAccepted = historyAccepted;
    }

    public boolean isPropositionVisibleControle() {
        return propositionVisibleControle;
    }

    public void setPropositionVisibleControle(boolean propositionVisibleControle) {
        this.propositionVisibleControle = propositionVisibleControle;
    }
    
}
