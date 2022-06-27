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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
    

    private boolean isRubriqueVisible;
    private Proposition proposition;
    private String nom, email, commentaire;

    private PropositionDao propositionSelected;
    private List<PropositionDao> propositions;

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

        nom = currentUser.getNodeUser().getName();
        email = currentUser.getNodeUser().getMail();

        propositionService.preparerPropositionSelect(proposition, propositionDao);
    }

    public void afficherListPropositions() {
        propositions = propositionService.searchAllPropositions();
        PrimeFaces.current().executeScript("PF('listNotification').show();");
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
        proposition.setUpdateNomConcept(true);
        PrimeFaces.current().executeScript("PF('nouveauNomConcept').hiden();");
    }

    public void supprimerPropostion() {
        
        propositionService.supprimerPropostion(propositionSelected);
        switchToConceptInglet();
        showMessage(FacesMessage.SEVERITY_INFO, "Proposition pour le concept  '" + propositionSelected.getNomConcept() 
                + "' (" + propositionSelected.getIdTheso() + ") suppprimée avec sucée !");
    }

    public void refuserProposition() {
        
        propositionService.refuserProposition(propositionSelected);
        
        switchToConceptInglet();
        showMessage(FacesMessage.SEVERITY_INFO, "Proposition pour le concept '"
                + propositionSelected.getNomConcept() + "' (" + propositionSelected.getIdTheso() + ") suppprimé avec sucée !");
    }

    public void approuverProposition() throws IOException {

        propositionService.insertProposition(proposition, propositionSelected);
        
        switchToConceptInglet();
        showMessage(FacesMessage.SEVERITY_INFO, "Proposition integrée avec sucée dans le concept '"
                + propositionSelected.getNomConcept() + "' (" + propositionSelected.getIdTheso() + ") !");
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

    public void envoyerProposition() {

        if (StringUtils.isEmpty(nom)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le champs nom est oubligatoire !");
            return;
        }

        if (StringUtils.isEmpty(email)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le champs email est oubligatoire !");
            return;
        }

        if (StringUtils.isEmpty(proposition.getNomConceptProp()) && !isSynchroProPresent() && !isTraductionProPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous devez proposer au moins une modification !");
            return;
        }

        propositionService.envoyerProposition(proposition, nom, email, commentaire);

        switchToConceptInglet();
        showMessage(FacesMessage.SEVERITY_INFO, "Proposition envoyée !");
    }

    private boolean isSynchroProPresent() {
        for (SynonymPropBean synonymProp : proposition.getSynonymsProp()) {
            if (synonymProp.isToAdd() || synonymProp.isToRemove() || synonymProp.isToUpdate()) {
                return true;
            }
        }
        return false;
    }

    private boolean isTraductionProPresent() {
        for (TraductionPropBean traductionProp : proposition.getTraductionsProp()) {
            if (traductionProp.isToAdd() || traductionProp.isToRemove() || traductionProp.isToUpdate()) {
                return true;
            }
        }
        return false;
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

    public boolean showButtonDecision() {
        return propositionSelected != null && (PropositionStatusEnum.LU.name().equals(propositionSelected.getStatus())
                || PropositionStatusEnum.ENVOYER.name().equals(propositionSelected.getStatus()));
    }
}
