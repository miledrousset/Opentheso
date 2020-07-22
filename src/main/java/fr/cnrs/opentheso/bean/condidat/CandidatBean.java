package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.alignment.AlignmentBean;
import fr.cnrs.opentheso.bean.alignment.AlignmentManualBean;
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
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
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

    @Inject
    private AlignmentBean alignmentBean;

    @Inject
    private AlignmentManualBean alignmentManualBean;

    private final CandidatService candidatService = new CandidatService();

    private boolean isListCandidatsActivate;
    private boolean isNewCandidatActivate;
    private boolean myCandidatsSelected;

    private String message;
    private String definition;
    private String searchValue;

    private CandidatDto candidatSelected, initialCandidat;
    private List<CandidatDto> candidatList, allTermes;
    private List<DomaineDto> domaines;

    @PostConstruct
    public void initCandidatModule() {
        isListCandidatsActivate = true;
        isNewCandidatActivate = false;
        candidatList = new ArrayList<>();
        allTermes = new ArrayList<>();
        domaines = new ArrayList<>();
        getAllCandidatsByThesoAndLangue();
    }

    public void getAllCandidatsByThesoAndLangue() {
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            candidatList = candidatService.getAllCandidats(connect, selectedTheso.getSelectedIdTheso(),
                    languageBean.getIdLangue());
        } else {
            candidatList = new ArrayList<>();
        }
    }

    public void selectMyCandidats() {
        if (myCandidatsSelected) {
            candidatList = candidatList.stream()
                    .filter(candidat -> candidat.getUserId() == currentUser.getNodeUser().getIdUser())
                    .collect(Collectors.toList());
        } else {
            getAllCandidatsByThesoAndLangue();
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
            getAllCandidatsByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(candidatList.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void showCandidatSelected(CandidatDto candidatDto) throws IOException {

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }

        candidatSelected = candidatDto;
        candidatSelected.setLang(languageBean.getIdLangue());
        candidatSelected.setUserId(currentUser.getNodeUser().getIdUser());
        candidatSelected.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatService.getCandidatDetails(connect, candidatSelected);
        initialCandidat = new CandidatDto(candidatSelected);

        allTermes = candidatList.stream().filter(candidat -> !candidat.getNomPref().equals(candidatDto.getNomPref()))
                .collect(Collectors.toList());

        domaines = candidatService.getDomainesList(connect, selectedTheso.getCurrentIdTheso(), languageBean.getIdLangue());

        conceptView.setNodeConcept(new ConceptHelper().getConcept(connect.getPoolConnexion(), candidatDto.getIdConcepte(),
                selectedTheso.getSelectedIdTheso(), languageBean.getIdLangue()));

        alignmentBean.initAlignmentSources(selectedTheso.getCurrentIdTheso(), candidatDto.getIdConcepte(), languageBean.getIdLangue());
        alignmentBean.setIdConceptSelectedForAlignment(candidatDto.getIdConcepte());

        setIsNewCandidatActivate(true);

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    public void setIsListCandidatsActivate(boolean isListCandidatsActivate) throws IOException {
        this.isListCandidatsActivate = isListCandidatsActivate;
        isNewCandidatActivate = false;

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    public boolean isIsNewCandidatActivate() {
        return isNewCandidatActivate;
    }

    public void setIsNewCandidatActivate(boolean isNewCandidatActivate) {
        this.isNewCandidatActivate = isNewCandidatActivate;
        isListCandidatsActivate = false;
    }

    public void saveConcept() throws SQLException, IOException {

        if (StringUtils.isEmpty(candidatSelected.getNomPref())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg1"));
            return;
        }

        if (roleOnThesoBean.getNodePreference() == null) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg2"));
            return;
        }

        if (initialCandidat == null) {

            ConceptHelper conceptHelper = new ConceptHelper();
            TermHelper termHelper = new TermHelper();

            // en cas d'un nouveau candidat, verification dans les prefLabels
            if (termHelper.isPrefLabelExist(connect.getPoolConnexion(), candidatSelected.getNomPref().trim(),
                    selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang())) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg3"));
                return;
            }
            // verification dans les altLabels
            if (termHelper.isAltLabelExist(connect.getPoolConnexion(), candidatSelected.getNomPref().trim(),
                    selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang())) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg4"));
                return;
            }

            Concept concept = new Concept();
            concept.setIdConcept(candidatSelected.getIdConcepte());
            concept.setIdThesaurus(selectedTheso.getCurrentIdTheso());
            concept.setTopConcept(false);
            concept.setLang(connect.getWorkLanguage());
            concept.setIdUser(currentUser.getNodeUser().getIdUser());
            concept.setUserName(currentUser.getUsername());
            concept.setStatus("CA");

            conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

            String idNewConcept = candidatService.saveNewCondidat(connect, concept, conceptHelper);
            if (idNewConcept == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("candidat.save.msg5"));
                return;
            }
            candidatSelected.setIdConcepte(idNewConcept);
            Term terme = new Term();
            terme.setId_thesaurus(selectedTheso.getCurrentIdTheso());
            terme.setLang(languageBean.getIdLangue());
            terme.setContributor(currentUser.getNodeUser().getIdUser());
            terme.setLexical_value(candidatSelected.getNomPref().trim());
            terme.setSource("candidat");
            terme.setStatus("D");

            candidatService.saveNewTerm(connect, terme, candidatSelected);

            if (conceptHelper.getNodePreference() != null) {
                ArrayList<String> idConcepts = new ArrayList<>();
                // création de l'identifiant Handle
                if (conceptHelper.getNodePreference().isUseHandle()) {
                    if (!conceptHelper.addIdHandle(connect.getPoolConnexion().getConnection(), candidatSelected.getIdConcepte(),
                            candidatSelected.getIdThesaurus())) {
                        showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("candidat.save.msg6"));
                        return;
                    }
                }

                if (conceptHelper.getNodePreference().isUseArk()) {
                    idConcepts.add(candidatSelected.getIdConcepte());
                    if (!conceptHelper.generateArkId(connect.getPoolConnexion(), candidatSelected.getIdThesaurus(), idConcepts)) {
                        showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("candidat.save.msg7"));
                        return;
                    }
                }
            }

        } else {
            if (!initialCandidat.getNomPref().equals(candidatSelected.getNomPref())) {
                candidatService.updateIntitule(connect, candidatSelected.getNomPref(), selectedTheso.getCurrentIdTheso(),
                        selectedTheso.getSelectedLang(), candidatSelected.getIdConcepte() + "", candidatSelected.getIdTerm());
            }
        }

        candidatService.updateDetailsCondidat(connect, candidatSelected, initialCandidat, allTermes, domaines, currentUser.getNodeUser().getIdUser());

        getAllCandidatsByThesoAndLangue();

        showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.save.msg8"));

        setIsListCandidatsActivate(true);

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("candidatForm");

    }

    public void showAllignementDialog(int pos) {

        if (initialCandidat == null) {
            showMessage(FacesMessage.SEVERITY_INFO, "Vous devez enregistrer votre candidat avant de gérer les alignements.");
            return;
        }

        switch (pos) {
            case 1:
                alignmentBean.initAlignementByStep(
                        selectedTheso.getCurrentIdTheso(),
                        candidatSelected.getIdConcepte(),
                        languageBean.getIdLangue());
                alignmentBean.nextTen(languageBean.getIdLangue(), selectedTheso.getCurrentIdTheso());
                PrimeFaces.current().executeScript("PF('addAlignment').show();");
                return;
            case 2:
                alignmentManualBean.reset();
                PrimeFaces.current().executeScript("PF('addManualAlignment').show();");
                return;
            case 3:
                alignmentManualBean.reset();
                PrimeFaces.current().executeScript("PF('updateAlignment').show();");
                return;
            default:
                alignmentManualBean.reset();
                alignmentBean.initAlignementByStep(selectedTheso.getCurrentIdTheso(),
                        candidatSelected.getIdConcepte(), languageBean.getIdLangue());
                PrimeFaces.current().executeScript("PF('deleteAlignment').show();");
        }
        PrimeFaces.current().ajax().update("addAlignmentForm");

    }

    public List<String> searchDomaineName(String enteredValue) {
        if ("%".equals(enteredValue)) {
            return domaines.stream().map(domaineDto -> domaineDto.getName()).collect(Collectors.toList());
        } else {
            List<String> matches = new ArrayList<>();
            for (DomaineDto s : domaines) {
                if (s.getName() != null && s.getName().toLowerCase().startsWith(enteredValue.toLowerCase())) {
                    matches.add(s.getName());
                }
            }
            return matches;
        }
    }

    //// ajouté par Miled
    /**
     * permet d'ajouter un vote pour ce candidat, c'est 1 seul vote par
     * utilisateur
     */
    public void addVote() {
        try {
            // cas où il y a un vote, on le supprime
            if (candidatService.getVote(connect,
                        candidatSelected.getIdThesaurus(),
                        candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser())) {
                candidatService.removeVote(connect,
                        candidatSelected.getIdThesaurus(),
                        candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser());
                candidatSelected.setVoted(false);
            } else {
                // cas ou il n'y a pas de vote, alors on vote
            candidatService.addVote(connect,
                    candidatSelected.getIdThesaurus(),
                    candidatSelected.getIdConcepte(),
                    currentUser.getNodeUser().getIdUser());
                candidatSelected.setVoted(true);            
            }

        //    getAllCandidatsByThesoAndLangue();

    //        setIsListCandidatsActivate(true);
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                Logger.getLogger(CandidatBean.class.getName()).log(Level.SEVERE, null, sqle.toString());
                showMessage(FacesMessage.SEVERITY_ERROR, "Le vote a échoué");
                return;
            }
        }
        /*catch (IOException ex) {
            Logger.getLogger(CandidatBean.class.getName()).log(Level.SEVERE, null, ex);
            showMessage(FacesMessage.SEVERITY_ERROR, "Le vote a échoué");
            return;
        }*/

        showMessage(FacesMessage.SEVERITY_INFO, "Vote enregistré");

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("candidatForm:vote");
    }

//// déprécié par Miled    
    /*
    public List<String> searchTerme(String enteredValue) {
        List<String> matches = new ArrayList<>();
        //using data factory for getting suggestions
        for (CandidatDto s : allTermes) {
            if (s.getNomPref().toLowerCase().startsWith(enteredValue.toLowerCase())) {
                matches.add(s.getNomPref());
            }
        }
        return matches;
    }*/
    /**
     * permet de retourner la liste des concepts possibles pour ajouter une
     * relation NT (en ignorant les relations interdites) on ignore les concepts
     * de type TT on ignore les concepts de type RT
     *
     * @param value
     * @return
     */
    public List<NodeSearchMini> searchTerme(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        SearchHelper searchHelper = new SearchHelper();
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            liste = searchHelper.searchAutoCompletionForRelation(
                    connect.getPoolConnexion(),
                    value,
                    selectedTheso.getCurrentLang(),
                    selectedTheso.getCurrentIdTheso());
        }
        return liste;
    }
    
    public ArrayList<NodeIdValue> searchTerme2(String value) {
        ArrayList<NodeIdValue> liste = new ArrayList<>();
        SearchHelper searchHelper = new SearchHelper();
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            liste = searchHelper.searchAutoCompletionForRelationIdValue(
                    connect.getPoolConnexion(),
                    value,
                    selectedTheso.getCurrentLang(),
                    selectedTheso.getCurrentIdTheso());
        }
        return liste;
    }    

    public void initialNewCandidat() throws IOException {
        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }

        setIsNewCandidatActivate(true);

        candidatSelected = new CandidatDto();
        candidatSelected.setIdConcepte(null);//candidatService.getCandidatID(connect));
        candidatSelected.setLang(languageBean.getIdLangue());
        candidatSelected.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatSelected.setUserId(currentUser.getNodeUser().getIdUser());

        domaines = candidatService.getDomainesList(connect, selectedTheso.getCurrentIdTheso(), languageBean.getIdLangue());

        alignmentBean.initAlignmentSources(selectedTheso.getCurrentIdTheso(), candidatSelected.getIdConcepte(), languageBean.getIdLangue());
        alignmentBean.setIdConceptSelectedForAlignment(candidatSelected.getIdConcepte());

        if (conceptView.getNodeConcept() != null) {
            conceptView.getNodeConcept().setNodeAlignments(new ArrayList<>());
        }

        allTermes = candidatList;

        initialCandidat = null;

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
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

    public CandidatDto getInitialCandidat() {
        return initialCandidat;
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

    public boolean isIsListCandidatsActivate() {
        return isListCandidatsActivate;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

}
