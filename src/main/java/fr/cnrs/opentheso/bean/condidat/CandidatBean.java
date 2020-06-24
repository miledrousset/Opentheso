package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bean.condidat.dao.CondidatDao;
import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
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
    private ConceptView conceptBean;

    private CandidatService candidatService;

    private boolean isListCandidatsActivate;
    private boolean isNewCandidatActivate;
    private boolean myCandidatsSelected;

    private String message;
    private String searchValue;

    private CandidatDto candidatSelected, initialCandidat;
    private List<CandidatDto> candidatList;

    @PostConstruct
    public void init() {
        candidatService = new CandidatService();

        isListCandidatsActivate = true;
        isNewCandidatActivate = false;

        selectedTheso.getSelectedIdTheso();
        candidatList = candidatService.getAllCandidats(connect);
    }

    public void selectMyCandidats() {
        if (myCandidatsSelected) {
            candidatList = candidatList.stream()
                    .filter(candidat -> candidat.getNomPref().equalsIgnoreCase("Firas GABSI"))
                    .collect(Collectors.toList());
        } else {
            candidatList = candidatService.getAllCandidats(connect);
        }
    }

    public void searchByTermeAndAuteur() {
        if (!StringUtils.isEmpty(searchValue)) {
            candidatList = candidatList.stream()
                    .filter(candidat -> candidat.getNomPref().contains(searchValue))
                    .collect(Collectors.toList());
        } else {
            candidatList = candidatService.getAllCandidats(connect);
        }
    }

    public void showCandidatSelected(CandidatDto candidatDto) {
        candidatSelected = candidatDto;
        candidatService.getCandidatDetails(connect, candidatSelected, currentUser.getUsername());
        initialCandidat = candidatSelected;
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

        ConceptHelper conceptHelper = new ConceptHelper();
        if (roleOnThesoBean.getNodePreference() == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        TermHelper termHelper = new TermHelper();

        if (initialCandidat == null) {
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
            concept.setIdThesaurus(selectedTheso.getCurrentIdTheso());
            concept.setUserName(currentUser.getUsername());
            
            //String idNewConcept = conceptHelper.addConcept(connect.getPoolConnexion(), null, null, concept, terme, currentUser.getNodeUser().getIdUser());conn = ds.getConnection();
            String idNewConcept = conceptHelper.addConceptInTable(
                    connect.getPoolConnexion().getConnection(), 
                    concept, currentUser.getNodeUser().getIdUser());
            
            if (idNewConcept == null) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", conceptHelper.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            
            new CondidatDao().setStatutForCandidat(connect, 
                    connect.getPoolConnexion().getConnection().createStatement(), 0, idNewConcept, 
                    selectedTheso.getCurrentIdTheso(), currentUser.getNodeUser().getIdUser()+"");

            conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), idNewConcept, selectedTheso.getSelectedLang());
            
            candidatService.saveIntitule(connect, candidatSelected.getNomPref(), selectedTheso.getCurrentIdTheso(), 
                    selectedTheso.getSelectedLang(), idNewConcept);
        } else {
            if (!initialCandidat.getNomPref().equals(candidatSelected.getNomPref())) {
                candidatService.updateIntitule(connect, candidatSelected.getNomPref(), selectedTheso.getCurrentIdTheso(),
                        selectedTheso.getSelectedLang(), candidatSelected.getIdConcepte()+"");
            }
        }
        
        //update domaine
        //update terme générique
        //update terme associés
        //update employé pour 
        
        //update défénition
        candidatService.saveNote(connect, initialCandidat == null ? "" : initialCandidat.getDefenition(), 
                candidatSelected.getDefenition(), candidatSelected, 
                selectedTheso.getSelectedLang(), "definition");
        
        //update note
        candidatService.saveNote(connect, initialCandidat == null ? "" : initialCandidat.getNoteApplication(), 
                candidatSelected.getNoteApplication(), candidatSelected, 
                selectedTheso.getSelectedLang(), "note");
        //update traduction
        //corpus lié
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
        setIsNewCandidatActivate(true);
        candidatSelected = new CandidatDto();
        initialCandidat = null;
    }

}
