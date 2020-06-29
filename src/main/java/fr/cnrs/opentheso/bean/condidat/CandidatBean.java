package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.condidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Named(value = "candidatBean")
@SessionScoped
public class CandidatBean implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private CurrentUser currentUser;

    @Inject
    private SelectedTheso selectedTheso;

    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    
    @Inject
    private LanguageBean languageBean;

    @Inject
    private ConceptView conceptView;

    private CandidatService candidatService;

    private boolean isListCandidatsActivate;
    private boolean isNewCandidatActivate;
    private boolean myCandidatsSelected;

    private String message;
    private String searchValue;

    private CandidatDto candidatSelected, initialCandidat;
    private List<CandidatDto> candidatList, allTermes;
    private List<DomaineDto> domaines;

    @PostConstruct
    public void init() {
        candidatService = new CandidatService();

        isListCandidatsActivate = true;
        isNewCandidatActivate = false;

        selectedTheso.getSelectedIdTheso();
        candidatList = candidatService.getAllCandidats(connect, selectedTheso.getCurrentIdTheso(), languageBean.getIdLangue());
    }

    public void selectMyCandidats() {
        if (myCandidatsSelected) {
            candidatList = candidatList.stream()
                    .filter(candidat -> candidat.getUserId() == currentUser.getNodeUser().getIdUser())
                    .collect(Collectors.toList());
        } else {
            candidatList = candidatService.getAllCandidats(connect, selectedTheso.getCurrentIdTheso(), languageBean.getIdLangue());
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(candidatList.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void searchByTermeAndAuteur() {
        if (!StringUtils.isEmpty(searchValue)) {
            candidatList = candidatList.stream()
                    .filter(candidat -> candidat.getNomPref().contains(searchValue) || candidat.getUser().contains(searchValue))
                    .collect(Collectors.toList());
        } else {
            candidatList = candidatService.getAllCandidats(connect, selectedTheso.getCurrentIdTheso(), languageBean.getIdLangue());
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(candidatList.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void showCandidatSelected(CandidatDto candidatDto) {
        candidatSelected = candidatDto;
        candidatService.getCandidatDetails(connect, candidatSelected, currentUser.getUsername());
        initialCandidat = new CandidatDto(candidatSelected);
        allTermes = candidatList.stream().filter(candidat -> !candidat.getNomPref().equals(candidatDto.getNomPref()))
                .collect(Collectors.toList());
        getDomainesListe();

        conceptView.setNodeConcept(new ConceptHelper().getConcept(connect.getPoolConnexion(), candidatDto.getIdConcepte(),
                selectedTheso.getSelectedIdTheso(), languageBean.getIdLangue()));

        setIsNewCandidatActivate(true);
    }


    public boolean isIsListCandidatsActivate() {
        return isListCandidatsActivate;
    }

    public void setIsListCandidatsActivate(boolean isListCandidatsActivate) {
        this.isListCandidatsActivate = isListCandidatsActivate;
        isNewCandidatActivate = false;
    }

    public boolean isIsNewCandidatActivate() {
        return isNewCandidatActivate;
    }

    public void setIsNewCandidatActivate(boolean isNewCandidatActivate) {
        this.isNewCandidatActivate = isNewCandidatActivate;
        isListCandidatsActivate = false;
    }

    public void saveConcept() throws SQLException {

        if (StringUtils.isEmpty(candidatSelected.getNomPref())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "le label est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (roleOnThesoBean.getNodePreference()
                == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (initialCandidat == null) {

            ConceptHelper conceptHelper = new ConceptHelper();
            TermHelper termHelper = new TermHelper();

            conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

            // en cas d'un nouveau candidat
            // verification dans les prefLabels
            if (termHelper.isPrefLabelExist(connect.getPoolConnexion(), candidatSelected.getNomPref().trim(),
                    selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "un TopTerme existe déjà avec ce nom !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            // verification dans les altLabels
            if (termHelper.isAltLabelExist(connect.getPoolConnexion(), candidatSelected.getNomPref().trim(),
                    selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "un synonyme existe déjà avec ce nom !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }

            Concept concept = new Concept();
            concept.setIdThesaurus(selectedTheso.getCurrentIdTheso());
            concept.setTopConcept(true);
            concept.setLang(selectedTheso.getSelectedLang());
            concept.setIdUser(currentUser.getNodeUser().getIdUser());
            concept.setUserName(currentUser.getUsername());
            concept.setStatus("D");

            Term terme = new Term();
            terme.setId_thesaurus(selectedTheso.getCurrentIdTheso());
            terme.setLang(selectedTheso.getSelectedLang());
            terme.setLexical_value(candidatSelected.getNomPref().trim());
            terme.setStatus("D");

            String idNewConcept = candidatService.saveNewCondidat(connect, concept,
                    terme, candidatSelected, conceptHelper);

            if (idNewConcept == null) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", conceptHelper.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }

            if (conceptHelper.getNodePreference() != null) {
                ArrayList<String> idConcepts = new ArrayList<>();
                // création de l'identifiant Handle
                if (conceptHelper.getNodePreference().isUseHandle()) {
                    if (!conceptHelper.addIdHandle(connect.getPoolConnexion().getConnection(), idNewConcept, concept.getIdThesaurus())) {
                        connect.getPoolConnexion().getConnection().rollback();
                        connect.getPoolConnexion().close();
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Handle a échouée");
                        return;
                    }
                }

                if (conceptHelper.getNodePreference().isUseArk()) {
                    idConcepts.add(idNewConcept);
                    if (!conceptHelper.generateArkId(connect.getPoolConnexion(), concept.getIdThesaurus(), idConcepts)) {
                        connect.getPoolConnexion().getConnection().rollback();
                        connect.getPoolConnexion().close();
                        message = message + "La création Ark a échouée";
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Ark a échouée");
                    }
                }
            }

        } else {
            if (!initialCandidat.getNomPref().equals(candidatSelected.getNomPref())) {
                candidatService.updateIntitule(connect, candidatSelected.getNomPref(), selectedTheso.getCurrentIdTheso(),
                        selectedTheso.getSelectedLang(), candidatSelected.getIdConcepte() + "", candidatSelected.getIdTerm());
            }
        }

        candidatService.updateDetailsCondidat(connect, candidatSelected, initialCandidat);

        candidatList = candidatService.getAllCandidats(connect, selectedTheso.getCurrentIdTheso(), languageBean.getIdLangue());

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info",
                "Le candidat a bien été ajouté");

        FacesContext.getCurrentInstance().addMessage(null, msg);
        setIsListCandidatsActivate(true);
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("candidatForm");

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<CandidatDto> getCandidatList() {
        return candidatList;
    }

    public void setCandidatList(List<CandidatDto> candidatList) {
        this.candidatList = candidatList;
    }

    public Connect getConnect() {
        return connect;
    }

    public void setConnect(Connect connect) {
        this.connect = connect;
    }

    public boolean isMyCandidatsSelected() {
        return myCandidatsSelected;
    }

    public void setMyCandidatsSelected(boolean myCandidatsSelected) {
        this.myCandidatsSelected = myCandidatsSelected;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public CandidatDto getCandidatSelected() {
        return candidatSelected;
    }

    public void setCandidatSelected(CandidatDto candidatSelected) {
        this.candidatSelected = candidatSelected;
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public void initialNewCandidat() {
        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous devez choisir avant un thesaurus !");
            return;
        }
        setIsNewCandidatActivate(true);
        candidatSelected = new CandidatDto();
        candidatSelected.setLang(languageBean.getIdLangue());
        candidatSelected.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        getDomainesListe();
        allTermes = candidatList;
        initialCandidat = null;
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }
    }

    public List<DomaineDto> getDomaines() {
        return domaines;
    }

    public List<CandidatDto> getAllTermes() {
        return allTermes;
    }

    public void setAllTermes(List<CandidatDto> allTermes) {
        this.allTermes = allTermes;
    }

    public LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    public ConceptView getConceptView() {
        return conceptView;
    }

    public void setConceptView(ConceptView conceptView) {
        this.conceptView = conceptView;
    }

    private void getDomainesListe() {
        domaines = new ArrayList<>();
        domaines.add(new DomaineDto(0, "Selectionnez un domaine"));
        domaines.addAll(candidatService.getDomainesList(connect, selectedTheso.getCurrentIdTheso()));
    }
 
}
