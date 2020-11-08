package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.condidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.condidat.enumeration.VoteType;
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
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
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

    private final CandidatService candidatService = new CandidatService();

    private boolean isListCandidatsActivate, isNewCandidatActivate, isShowCandidatActivate;
    private boolean isRejectCandidatsActivate, isAcceptedCandidatsActivate, isExportViewActivate, isImportViewActivate;
    private boolean myCandidatsSelected1, myCandidatsSelected2, myCandidatsSelected3;

    private int tabViewIndexSelected, progressBarStep, progressBarValue;

    private String message, definition, selectedExportFormat;
    private String searchValue1, searchValue2, searchValue3;

    private CandidatDto candidatSelected, initialCandidat;
    private List<String> exportFormat;
    private List<CandidatDto> candidatList, rejetCadidat, acceptedCadidat, allTermes;
    private List<DomaineDto> domaines;
    private List<NodeLangTheso> selectedLanguages;
    private ArrayList<NodeLangTheso> languagesOfTheso;


    private String test;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    @PostConstruct
    public void initCandidatModule() {
        isListCandidatsActivate = true;
        isRejectCandidatsActivate = true;
        isAcceptedCandidatsActivate = true;
        isNewCandidatActivate = false;
        isShowCandidatActivate = false;
        isImportViewActivate = false;
        isExportViewActivate = false;

        candidatList = new ArrayList<>();
        allTermes = new ArrayList<>();
        domaines = new ArrayList<>();
        getAllCandidatsByThesoAndLangue();
        getRejectCandidatByThesoAndLangue();
        getAcceptedCandidatByThesoAndLangue();
        tabViewIndexSelected = 0;

        exportFormat = Arrays.asList("skos", "json", "jsonLd", "turtle");
        selectedExportFormat = "skos";

        languagesOfTheso = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        selectedLanguages = new ArrayList<>();
        languagesOfTheso.forEach((nodeLang) -> {
            selectedLanguages.add(nodeLang);
        });

    }

    public void getAllCandidatsByThesoAndLangue() {
        tabViewIndexSelected = 0;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            candidatList = candidatService.getCandidatsByStatus(connect, selectedTheso.getSelectedIdTheso(),
                    getIdLang(), 1, "CA");
        } else {
            candidatList = new ArrayList<>();
        }
    }

    public void getRejectCandidatByThesoAndLangue() {
        tabViewIndexSelected = 2;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            rejetCadidat = candidatService.getCandidatsByStatus(connect, selectedTheso.getSelectedIdTheso(),
                    getIdLang(), 3, "CA");
        } else {
            rejetCadidat = new ArrayList<>();
        }
    }

    public void getAcceptedCandidatByThesoAndLangue() {
        tabViewIndexSelected = 1;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            acceptedCadidat = candidatService.getCandidatsByStatus(connect, selectedTheso.getSelectedIdTheso(),
                    getIdLang(), 2, null);
        } else {
            acceptedCadidat = new ArrayList<>();
        }
    }

    /**
     * permet de déctercter la langue préférée d'un thésaurus
     * @return
     */
    private String getIdLang() {
        String idLang = connect.getWorkLanguage();
        if (roleOnThesoBean.getNodePreference() != null) {
            idLang = roleOnThesoBean.getNodePreference().getSourceLang();
        }
        return idLang;
    }

    public void selectMyCandidats() {
        if (myCandidatsSelected1) {
            candidatList = candidatList.stream()
                    .filter(candidat -> candidat.getUserId() == currentUser.getNodeUser().getIdUser())
                    .collect(Collectors.toList());
        } else {
            getAllCandidatsByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(candidatList.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void selectMyRejectCandidats() {
        if (myCandidatsSelected3) {
            rejetCadidat = rejetCadidat.stream()
                    .filter(candidat -> candidat.getUserId() == currentUser.getNodeUser().getIdUser())
                    .collect(Collectors.toList());
        } else {
            getRejectCandidatByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(rejetCadidat.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void searchRejectCandByTermeAndAuteur() {
        if (!StringUtils.isEmpty(searchValue3)) {
            rejetCadidat = rejetCadidat.stream()
                    .filter(candidat -> candidat.getNomPref().contains(searchValue3) || candidat.getUser().contains(searchValue3))
                    .collect(Collectors.toList());
        } else {
            getRejectCandidatByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(rejetCadidat.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void selectMyAcceptedCandidats() {
        if (myCandidatsSelected2) {
            acceptedCadidat = acceptedCadidat.stream()
                    .filter(candidat -> candidat.getUserId() == currentUser.getNodeUser().getIdUser())
                    .collect(Collectors.toList());
        } else {
            getAcceptedCandidatByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(acceptedCadidat.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void searchAcceptedCandByTermeAndAuteur() {
        if (!StringUtils.isEmpty(searchValue2)) {
            acceptedCadidat = acceptedCadidat.stream()
                    .filter(candidat -> candidat.getNomPref().contains(searchValue2) || candidat.getUser().contains(searchValue2))
                    .collect(Collectors.toList());
        } else {
            getAcceptedCandidatByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(acceptedCadidat.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void searchByTermeAndAuteur() {
        if (!StringUtils.isEmpty(searchValue1)) {
            
            candidatList = candidatService.searchCandidats(connect, searchValue1,
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(),
                    1, "CA");
            /// désactivé par Miled pour permettre de rechercher un candidat sur le serveur et non pas dans le vecteur
            /*
            candidatList = candidatList.stream()
                    .filter(candidat -> candidat.getNomPref().contains(searchValue1) || candidat.getUser().contains(searchValue1))
                    .collect(Collectors.toList());*/
        } else {
            getAllCandidatsByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(candidatList.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void showRejectCandidatSelected(CandidatDto candidatDto) throws IOException {

        tabViewIndexSelected = 2;

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }

        isRejectCandidatsActivate = false;
        getCandidatInformations(candidatDto);

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());

    }

    public void showAcceptedCandidatSelected(CandidatDto candidatDto) throws IOException {

        tabViewIndexSelected = 1;

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }

        isAcceptedCandidatsActivate = false;
        getCandidatInformations(candidatDto);

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());

    }
    
    public void getCandidatInformations(CandidatDto candidatDto) {
        candidatSelected = candidatDto;
        candidatSelected.setLang(languageBean.getIdLangue());
        candidatSelected.setUserId(currentUser.getNodeUser().getIdUser());
        candidatSelected.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatService.getCandidatDetails(connect, candidatSelected);
    }

    public void showCandidatSelected(CandidatDto candidatDto) throws IOException {

        tabViewIndexSelected = 0;

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

        setShowCandidatActivate(true);

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    public void setIsListCandidatsActivate(boolean isListCandidatsActivate) throws IOException {

        tabViewIndexSelected = 0;

        this.isListCandidatsActivate = true;
        isRejectCandidatsActivate = true;
        isAcceptedCandidatsActivate = true;

        isNewCandidatActivate = false;
        isShowCandidatActivate = false;

        isExportViewActivate = false;
        isImportViewActivate = false;

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    public boolean isNewCandidatActivate() {
        return isNewCandidatActivate;
    }

    public void setIsNewCandidatActivate(boolean isNewCandidatActivate) {
        this.isNewCandidatActivate = isNewCandidatActivate;
        isListCandidatsActivate = false;
        isShowCandidatActivate = false;
        isImportViewActivate = false;
        isExportViewActivate = false;
    }

    public boolean isShowCandidatActivate() {
        return isShowCandidatActivate;
    }

    public void setShowCandidatActivate(boolean isShowCandidatActivate) {
        this.isShowCandidatActivate = isShowCandidatActivate;
        isListCandidatsActivate = false;
        isNewCandidatActivate = false;
        isImportViewActivate = false;
        isExportViewActivate = false;
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

            candidatService.saveNewTerm(connect, terme,
                    candidatSelected.getIdConcepte(), candidatSelected.getUserId());


            /**
             * à déplacer au moment d'insérer le candidat dans le thésaurus
             *
             */
   /*         if (conceptHelper.getNodePreference() != null) {
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
            }*/
            setIsListCandidatsActivate(true);

        } else {
            if (!initialCandidat.getNomPref().equals(candidatSelected.getNomPref())) {
                candidatService.updateIntitule(connect, candidatSelected.getNomPref(), selectedTheso.getCurrentIdTheso(),
                        selectedTheso.getSelectedLang(), candidatSelected.getIdTerm());
            }
        }

        candidatService.updateDetailsCondidat(connect, candidatSelected, initialCandidat, allTermes, domaines, currentUser.getNodeUser().getIdUser());

        getAllCandidatsByThesoAndLangue();

        showMessage(FacesMessage.SEVERITY_INFO, "Candidat enregistré avec succès");

    //    setIsListCandidatsActivate(true);
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
  //          PrimeFaces.current().ajax().update("messageIndex");
  //          PrimeFaces.current().ajax().update("candidatForm");
        }

    }




    public void showAllignementDialog(int pos) {

/*        if (initialCandidat == null) {
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
        PrimeFaces.current().ajax().update("addAlignmentForm");*/

    }

    public ArrayList<NodeIdValue> searchCollection(String enteredValue) {
        
        ArrayList<NodeIdValue> nodeIdValues = null;
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            nodeIdValues = new GroupHelper().searchGroup(
                    connect.getPoolConnexion(),
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(),
                    enteredValue);
        }
        return nodeIdValues;        
        /*
        
        GroupHelper groupHelper = new GroupHelper();
        groupHelper.get
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
        }*/
    }

    //// ajouté par Miled
    /**
     * permet d'ajouter un vote pour ce candidat, c'est 1 seul vote par
     * utilisateur
     */
    public void addVote() {
        try {
            // cas où il y a un vote, on le supprime
            if (candidatService.getVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), null, VoteType.CANDIDAT)) {
                candidatService.removeVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), null, VoteType.CANDIDAT);
                candidatSelected.setVoted(false);
            } else {
                // cas ou il n'y a pas de vote, alors on vote
            candidatService.addVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                    currentUser.getNodeUser().getIdUser(), null, VoteType.CANDIDAT);
                candidatSelected.setVoted(true);
            }

        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                Logger.getLogger(CandidatBean.class.getName()).log(Level.SEVERE, null, sqle.toString());
                showMessage(FacesMessage.SEVERITY_ERROR, "Le vote a échoué");
                return;
            }
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Vote enregistré");

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("candidatForm:vote");
    }

    /**
     * permet d'ajouter un vote pour un note de candidat, c'est 1 seul vote par utilisateur
     * @param nodeNote
     */
    public void addNoteVote(NodeNote nodeNote) {
        try {
            // cas où il y a un vote, on le supprime
            if (candidatService.getVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                    currentUser.getNodeUser().getIdUser(), nodeNote.getId_note()+"", VoteType.NOTE)) {

                candidatService.removeVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), nodeNote.getId_note()+"", VoteType.NOTE);
                nodeNote.setVoted(false);
            } else {
                // cas ou il n'y a pas de vote, alors on vote
            candidatService.addVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                    currentUser.getNodeUser().getIdUser(), nodeNote.getId_note()+"", VoteType.NOTE);
                nodeNote.setVoted(true);
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                Logger.getLogger(CandidatBean.class.getName()).log(Level.SEVERE, null, sqle.toString());
                showMessage(FacesMessage.SEVERITY_ERROR, "Le vote a échoué");
                return;
            }
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Vote du note enregistré");

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

    public ArrayList<NodeIdValue> searchTerme2(String value) {
        ArrayList<NodeIdValue> liste = new ArrayList<>();
        SearchHelper searchHelper = new SearchHelper();
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            liste = searchHelper.searchAutoCompletionForRelationIdValue(connect.getPoolConnexion(),value,
                    selectedTheso.getCurrentLang(),selectedTheso.getCurrentIdTheso());
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

        allTermes = candidatList;

        initialCandidat = null;

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    /**
     * permet de récupérer le nom d'un utilisateur d'après son ID
     * @param idUser
     * @return
     */
    public String getUserName(int idUser){
        UserHelper userHelper = new UserHelper();
        return userHelper.getNameUser(connect.getPoolConnexion(), idUser);

    }

    /**
     * permet de récupérer les anciens candidats saisies dans l'ancien module
     * uniquement les candidats qui étatient en attente
     */
    public void getOldCandidates(){
        String messageInfo = new CandidatService().getOldCandidates(connect,selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser(),roleOnThesoBean.getNodePreference());
        showMessage(FacesMessage.SEVERITY_INFO, messageInfo);
        getAllCandidatsByThesoAndLangue();
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

    public boolean isMyCandidatsSelected1() {
        return myCandidatsSelected1;
    }

    public void setMyCandidatsSelected1(boolean myCandidatsSelected1) {
        this.myCandidatsSelected1 = myCandidatsSelected1;
    }

    public boolean isMyCandidatsSelected2() {
        return myCandidatsSelected2;
    }

    public void setMyCandidatsSelected2(boolean myCandidatsSelected2) {
        this.myCandidatsSelected2 = myCandidatsSelected2;
    }

    public boolean isMyCandidatsSelected3() {
        return myCandidatsSelected3;
    }

    public void setMyCandidatsSelected3(boolean myCandidatsSelected3) {
        this.myCandidatsSelected3 = myCandidatsSelected3;
    }

    public String getSearchValue1() {
        return searchValue1;
    }

    public void setSearchValue1(String searchValue1) {
        this.searchValue1 = searchValue1;
    }

    public String getSearchValue2() {
        return searchValue2;
    }

    public void setSearchValue2(String searchValue2) {
        this.searchValue2 = searchValue2;
    }

    public String getSearchValue3() {
        return searchValue3;
    }

    public void setSearchValue3(String searchValue3) {
        this.searchValue3 = searchValue3;
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

    public boolean isListCandidatsActivate() {
        return isListCandidatsActivate;
    }

    public void setListCandidatsActivate(boolean isListCandidatsActivate) {
        getAllCandidatsByThesoAndLangue();
        this.isListCandidatsActivate = isListCandidatsActivate;
        isImportViewActivate = false;
        isExportViewActivate = false;
    }


    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<CandidatDto> getRejetCadidat() {
        return rejetCadidat;
    }

    public List<CandidatDto> getAcceptedCadidat() {
        return acceptedCadidat;
    }

    public void setAcceptedCadidat(List<CandidatDto> acceptedCadidat) {
        this.acceptedCadidat = acceptedCadidat;
    }

    public boolean isRejectCandidatsActivate() {
        return isRejectCandidatsActivate;
    }

    public int getTabViewIndexSelected() {
        return tabViewIndexSelected;
    }

    public void setTabViewIndexSelected(int tabViewIndexSelected) {
        this.tabViewIndexSelected = tabViewIndexSelected;
    }

    public boolean isExportViewActivate() {
        return isExportViewActivate;
    }

    public void setExportViewActivate(boolean isExportViewActivate) {
        this.isExportViewActivate = isExportViewActivate;
        isImportViewActivate = false;
        isListCandidatsActivate = false;
        isShowCandidatActivate = false;
        
        setProgressBarStep(0);
        setProgressBarValue(0);
    }

    public ArrayList<NodeLangTheso> getLanguagesOfTheso() {
        return languagesOfTheso;
    }

    public List<NodeLangTheso> getSelectedLanguages() {
        return selectedLanguages;
    }

    public void setSelectedLanguages(List<NodeLangTheso> selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
    }

    public String getSelectedExportFormat() {
        return selectedExportFormat;
    }

    public void setSelectedExportFormat(String selectedExportFormat) {
        this.selectedExportFormat = selectedExportFormat;
    }

    public List<String> getExportFormat() {
        return exportFormat;
    }

    public boolean isImportViewActivate() {
        return isImportViewActivate;
    }

    public void setImportViewActivate(boolean isImportViewActivate) {
        this.isImportViewActivate = isImportViewActivate;
        isExportViewActivate = false;
        isListCandidatsActivate = false;
        isShowCandidatActivate = false;
        
        setProgressBarStep(0);
        setProgressBarValue(0);
    }

    public int getProgressBarStep() {
        return progressBarStep;
    }

    public void setProgressBarStep(int progressBarStep) {
        this.progressBarStep = progressBarStep;
    }

    public int getProgressBarValue() {
        return progressBarValue;
    }

    public void setProgressBarValue(int progressBarValue) {
        this.progressBarValue = progressBarValue;
    }

    public boolean isAcceptedCandidatsActivate() {
        return isAcceptedCandidatsActivate;
    }

    public void setAcceptedCandidatsActivate(boolean isAcceptedCandidatsActivate) {
        this.isAcceptedCandidatsActivate = isAcceptedCandidatsActivate;
    }
}
