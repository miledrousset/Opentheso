package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.CandidateHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.candidat.dao.DomaineDao;
import fr.cnrs.opentheso.bean.candidat.dao.NoteDao;
import fr.cnrs.opentheso.bean.candidat.dao.TermeDao;
import fr.cnrs.opentheso.bean.candidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.candidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.candidat.enumeration.VoteType;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import javax.annotation.PreDestroy;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;


@SessionScoped
@Named(value = "candidatBean")
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

    private NodeAlignment alignementSelected;

    private String employePour;
    private String message, definition, selectedExportFormat;
    private String searchValue1, searchValue2, searchValue3;

    private CandidatDto candidatSelected, initialCandidat;
    private List<String> exportFormat;
    private List<CandidatDto> candidatList, rejetCadidat, acceptedCadidat, allTermes;
    private List<DomaineDto> domaines;
    private List<NodeLangTheso> selectedLanguages;
    private List<NodeIdValue> allCollections, allTermesGenerique, AllTermesAssocies;
    private ArrayList<NodeLangTheso> languagesOfTheso;
    private List<CandidatDto> selectedCandidates;
    private boolean listSelected;
    private boolean traductionVisible;

    private List<NodeIdValue> collectionTemps, termesGeneriqueTmp, termesAssociesTmp;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        candidatSelected = null;
        initialCandidat = null;
        exportFormat = null;
        candidatList = null;
        rejetCadidat = null;
        acceptedCadidat = null;
        allTermes = null;
        domaines = null;
        selectedLanguages = null;
        languagesOfTheso = null;
        definition = null;
    }

    public void setStateForSelectedCandidate() {
        if (selectedCandidates != null) {
            listSelected = !selectedCandidates.isEmpty();
        }
    }

    public void initCandidatModule() {
        isListCandidatsActivate = true;
        isRejectCandidatsActivate = true;
        isAcceptedCandidatsActivate = true;
        isNewCandidatActivate = false;
        isShowCandidatActivate = false;
        isImportViewActivate = false;
        isExportViewActivate = false;
        listSelected = false;

        collectionTemps = new ArrayList<>();

        candidatList = new ArrayList<>();
        allTermes = new ArrayList<>();
        domaines = new ArrayList<>();
        selectedLanguages = new ArrayList<>();
        rejetCadidat = new ArrayList<>();
        acceptedCadidat = new ArrayList<>();
        selectedCandidates = new ArrayList<>();

        getAllCandidatsByThesoAndLangue();
        getRejectCandidatByThesoAndLangue();
        getAcceptedCandidatByThesoAndLangue();
        tabViewIndexSelected = 0;

        alignementSelected = new NodeAlignment();

        exportFormat = Arrays.asList("skos", "json", "jsonLd", "turtle");
        selectedExportFormat = "skos";

        allCollections = new GroupHelper().searchGroup(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang(), "%");

/*        allTermesGenerique = new SearchHelper().searchAutoCompletionForRelationIdValue(connect.getPoolConnexion(), "%",
                selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
        if (candidatSelected != null && CollectionUtils.isNotEmpty(candidatSelected.getTermesGenerique())) {
            for (NodeIdValue nodeIdValue : candidatSelected.getTermesGenerique()) {
                allTermesGenerique.remove(nodeIdValue);
            }
        }*/

/*        AllTermesAssocies = new SearchHelper().searchAutoCompletionForRelationIdValue(connect.getPoolConnexion(), "%",
                selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
        if (candidatSelected != null && CollectionUtils.isNotEmpty(candidatSelected.getTermesAssocies())) {
            for (NodeIdValue nodeIdValue : candidatSelected.getTermesGenerique()) {
                AllTermesAssocies.remove(nodeIdValue);
            }
        }*/

        try {
            languagesOfTheso = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(
                    connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
            languagesOfTheso.forEach((nodeLang) -> {
                selectedLanguages.add(nodeLang);
            });
        } catch (Exception e) {
        }
    }

    public void addSynonyme() {

        if (StringUtils.isNotEmpty(employePour)) {
            if (candidatSelected.getEmployePourList().contains(employePour)) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Le mot '" + employePour + "' existe déjà !");
            } else {
                candidatSelected.getEmployePourList().add(employePour);
                employePour = "";
                PrimeFaces.current().ajax().update("tabViewCandidat");
            }
        }
    }

    public void removeSynonyme(String synonyme) {
        if (CollectionUtils.isNotEmpty(candidatSelected.getEmployePourList())) {
            candidatSelected.getEmployePourList().remove(synonyme);
            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatSynonym");
        }
    }

    public void removeGenericTerm(NodeIdValue genericTerm) {
        if (CollectionUtils.isNotEmpty(candidatSelected.getTermesGenerique())) {
            candidatSelected.getTermesGenerique().remove(genericTerm);
            allTermesGenerique.add(genericTerm);
            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatBT");
        }
    }

    public void removeAssociesTerm(NodeIdValue associeTerm) {
        if (CollectionUtils.isNotEmpty(candidatSelected.getTermesAssocies())) {
            candidatSelected.getTermesAssocies().remove(associeTerm);
            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatRT");
        }
    }

    public void getAllCandidatsByThesoAndLangue() {
        tabViewIndexSelected = 0;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            candidatList = candidatService.getCandidatsByStatus(connect, selectedTheso.getSelectedIdTheso(),
                    getIdLang(), 1, "CA");
        } else {
            candidatList.clear();
        }
    }

    public void getRejectCandidatByThesoAndLangue() {
        tabViewIndexSelected = 2;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            rejetCadidat = candidatService.getCandidatsByStatus(connect, selectedTheso.getSelectedIdTheso(),
                    getIdLang(), 3, "CA");
        } else {
            rejetCadidat.clear();
        }
    }

    public void getAcceptedCandidatByThesoAndLangue() {
        tabViewIndexSelected = 1;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            acceptedCadidat = candidatService.getCandidatsByStatus(connect, selectedTheso.getSelectedIdTheso(),
                    getIdLang(), 2, null);
        } else {
            acceptedCadidat.clear();
        }
        isAcceptedCandidatsActivate = true;
    }

    /**
     * permet de supprimer les candidats sélectionnés
     * @param idUser 
     */
    public void deleteSelectedCandidate(int idUser){
        if(selectedCandidates == null) return;
        ConceptHelper conceptHelper = new ConceptHelper();
        for (CandidatDto selectedCandidate : selectedCandidates) {
            if(!conceptHelper.deleteConcept(connect.getPoolConnexion(), selectedCandidate.getIdConcepte(), 
                    selectedCandidate.getIdThesaurus(), idUser)){
                showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de suppression");
                return;
            }
        }
        initCandidatModule();
        getAllCandidatsByThesoAndLangue();
        showMessage(FacesMessage.SEVERITY_INFO, "Candidats supprimés");
    }
    
    /**
     * permet de savoir si l'identifiant actuel est propriétaire du candidat 
     * @return 
     */
    public boolean isMyCandidate() {
        return candidatSelected.getCreatedById() == candidatSelected.getUserId();
    }
    
    /**
     * permet de déctercter la langue préférée d'un thésaurus
     *
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
                    .filter(candidat -> candidat.getCreatedById() == currentUser.getNodeUser().getIdUser())
                    .collect(Collectors.toList());
        } else {
            getAllCandidatsByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(candidatList.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }
    
    public String getCountOfCandidats(){
        return "" + candidatList.size();
    }
    
    public String getCountOfAcceptedCandidats(){
        return "" + acceptedCadidat.size();
    }    
    
    public String getCountOfRejectedCandidats(){
        return "" + rejetCadidat.size();
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
        } else {
            getAllCandidatsByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(candidatList.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void deleteAlignment(NodeAlignment nodeAlignment) {

        if(!new AlignmentHelper().deleteAlignment(connect.getPoolConnexion(),
                nodeAlignment.getId_alignement(),
                selectedTheso.getCurrentIdTheso())) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        candidatSelected.setAlignments(new AlignmentHelper().getAllAlignmentOfConcept(connect.getPoolConnexion(),
                candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso()));

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
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
        candidatSelected.setLang(getIdLang());
        candidatSelected.setUserId(currentUser.getNodeUser().getIdUser());
        candidatSelected.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatService.getCandidatDetails(connect, candidatSelected);
        initialCandidat = new CandidatDto(candidatSelected);

        allTermes = candidatList.stream().filter(candidat -> !candidat.getNomPref().equals(candidatDto.getNomPref()))
                .collect(Collectors.toList());

        isShowCandidatActivate = true;
        isNewCandidatActivate = false;
        isListCandidatsActivate = false;
    }
    
    public CandidatDto getAllInfosOfCandidate(CandidatDto candidatDto){
        candidatDto.setLang(getIdLang());
        candidatDto.setUserId(currentUser.getNodeUser().getIdUser());
        candidatDto.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatService.getCandidatDetails(connect, candidatDto);
        return candidatDto;
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
        isImportViewActivate = false;
        isExportViewActivate = false;
    }

    public void setIsNewCandidatRejected(boolean isCandidatRejected) {
        isRejectCandidatsActivate = true;
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

        if(isNewCandidatActivate) {
            if (StringUtils.isEmpty(definition)) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.def"));
                return;
            }
        }        

        if (roleOnThesoBean.getNodePreference() == null) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg2"));
            return;
        }

        var termHelper = new TermHelper();

        if (initialCandidat == null) {

            // en cas d'un nouveau candidat, verification dans les prefLabels
            if (termHelper.isPrefLabelExist(connect.getPoolConnexion(), candidatSelected.getNomPref().trim(),
                    selectedTheso.getCurrentIdTheso(), getIdLang())) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg3"));
                return;
            }
            // verification dans les altLabels
            if (termHelper.isAltLabelExist(connect.getPoolConnexion(), candidatSelected.getNomPref().trim(),
                    selectedTheso.getCurrentIdTheso(), getIdLang())) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg4"));
                return;
            }

            var concept = Concept.builder()
                    .idConcept(candidatSelected.getIdConcepte())
                    .idThesaurus(selectedTheso.getCurrentIdTheso())
                    .topConcept(false)
                    .lang(getIdLang())
                    .idUser(currentUser.getNodeUser().getIdUser())
                    .userName(currentUser.getUsername())
                    .status("CA")
                    .build();

            ConceptHelper conceptHelper = new ConceptHelper();
            conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

            var idNewConcept = candidatService.saveNewCondidat(connect, concept, conceptHelper);
            if (idNewConcept == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("candidat.save.msg5"));
                return;
            }
            candidatSelected.setIdConcepte(idNewConcept);

            var terme = Term.builder()
                    .lang(getIdLang())
                    .id_thesaurus(selectedTheso.getCurrentIdTheso())
                    .contributor(currentUser.getNodeUser().getIdUser())
                    .lexical_value(candidatSelected.getNomPref().trim())
                    .source("candidat")
                    .status("D")
                    .build();

            candidatSelected.setIdTerm(candidatService.saveNewTerm(connect, terme, candidatSelected.getIdConcepte(),
                    candidatSelected.getUserId()));

            new NoteHelper().addNote(connect.getPoolConnexion(), candidatSelected.getIdConcepte(),
                    selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso(),
                    definition, "definition", "", currentUser.getNodeUser().getIdUser());
           
            
            setIsListCandidatsActivate(true);

        } else {
            if (!initialCandidat.getNomPref().equals(candidatSelected.getNomPref())) {
                if (termHelper.isTermExistInThisLang(connect.getPoolConnexion(), candidatSelected.getIdTerm(), getIdLang(), candidatSelected.getIdThesaurus())) {
                    candidatService.updateIntitule(connect, candidatSelected.getNomPref(), candidatSelected.getIdThesaurus(),
                            getIdLang(), candidatSelected.getIdTerm());
                } else {
                    Term term = new Term();
                    term.setId_thesaurus(selectedTheso.getCurrentIdTheso());
                    term.setLang(getIdLang());
                    term.setContributor(currentUser.getNodeUser().getIdUser());
                    term.setLexical_value(candidatSelected.getNomPref().trim());
                    term.setSource("candidat");
                    term.setStatus("D");
                    term.setId_term(candidatSelected.getIdTerm());
                    TermeDao termeDao = new TermeDao();
                    termeDao.addNewTerme(connect.getPoolConnexion(), term);
                }

            }
        }
        /////////////////////////
        ///// insert DcTermsData
        var dcElement = DcElement.builder()
                .name(DCMIResource.CREATOR)
                .value(currentUser.getNodeUser().getName())
                .build();
        new DcElementHelper().addDcElementConcept(connect.getPoolConnexion(), dcElement, candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus());
        ///////////////        
        
        
        candidatService.updateDetailsCondidat(connect, candidatSelected, currentUser.getNodeUser().getIdUser());

        //getAllCandidatsByThesoAndLangue();
        candidatSelected.setNodeNotes(new NoteDao().getNotesCandidat(connect.getPoolConnexion(), candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus()));
        definition = "";
        isNewCandidatActivate = false;
        isListCandidatsActivate = false;
        isShowCandidatActivate = true;
        showMessage(FacesMessage.SEVERITY_INFO, "Candidat enregistré avec succès");
    }

    public List<NodeIdValue> searchCollection(String enteredValue) {

        if (CollectionUtils.isNotEmpty(allCollections)) {
            if ("%".equals(enteredValue)) {
                return createCollectionsFiltred(allCollections, candidatSelected.getCollections());
            } else {
                return createCollectionsFiltred(allCollections.stream()
                        .filter(element -> element.getValue().contains(enteredValue))
                        .collect(Collectors.toList()), candidatSelected.getCollections());
            }
        } else {
            return Collections.emptyList();
        }
    }

    private List<NodeIdValue> createCollectionsFiltred(List<NodeIdValue> collections, List<NodeIdValue> collectionsSelected) {
        List<NodeIdValue> resultat = new ArrayList<>();
        for (NodeIdValue element : collections) {
            if (!isExist(collectionsSelected, element)) {
                resultat.add(element);
            }
        }
        return resultat;
    }

    private boolean isExist(List<NodeIdValue> collections, NodeIdValue nodeIdValue) {
        return collections.stream()
                .filter(element -> element.getValue().equals(nodeIdValue.getValue()))
                .findFirst()
                .isPresent();
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
        PrimeFaces.current().ajax().update("containerIndex:votePanel");
    }

    /**
     * permet d'ajouter un vote pour un note de candidat, c'est 1 seul vote par
     * utilisateur
     *
     * @param nodeNote
     */
    public void addNoteVote(NodeNote nodeNote) {
        try {
            // cas où il y a un vote, on le supprime
            if (candidatService.getVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                    currentUser.getNodeUser().getIdUser(), nodeNote.getId_note() + "", VoteType.NOTE)) {

                candidatService.removeVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), nodeNote.getId_note() + "", VoteType.NOTE);
                nodeNote.setVoted(false);
            } else {
                // cas ou il n'y a pas de vote, alors on vote
                candidatService.addVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), nodeNote.getId_note() + "", VoteType.NOTE);
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

    /**
     * permet de retourner la liste des concepts possibles pour ajouter une
     * relation NT (en ignorant les relations interdites) on ignore les concepts
     * de type TT on ignore les concepts de type RT
     *
     * @param value
     * @return
     */
    public List<NodeIdValue> searchTermeGenerique(String value) {
        return new SearchHelper().searchAutoCompletionForRelationIdValue(connect.getPoolConnexion(), value,
                selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
    }


    public List<NodeIdValue> searchTermeAssocie(String value) {
        return new SearchHelper().searchAutoCompletionForRelationIdValue(connect.getPoolConnexion(), value,
                selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
    }

    public void initialNewCandidat() throws IOException {
        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }
        setIsNewCandidatActivate(true);

        candidatSelected = new CandidatDto();
        candidatSelected.setIdConcepte(null);//candidatService.getCandidatID(connect));
        candidatSelected.setLang(getIdLang());
        candidatSelected.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatSelected.setUserId(currentUser.getNodeUser().getIdUser());

        allTermes = candidatList;

        initialCandidat = null;
        definition = null;
        

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    /**
     * permet de récupérer le nom d'un utilisateur d'après son ID
     *
     * @param idUser
     * @return
     */
    public String getUserName(int idUser) {
        UserHelper userHelper = new UserHelper();
        return userHelper.getNameUser(connect.getPoolConnexion(), idUser);

    }
    
    public void reactivateRejectedCandidat(){
        if(candidatSelected == null || candidatSelected.getIdConcepte() == null || candidatSelected.getIdConcepte().isEmpty()) return;
        
        CandidateHelper candidateHelper = new CandidateHelper();
        if(!candidateHelper.reactivateRejectedCandidat(connect.getPoolConnexion(),
                candidatSelected.getIdThesaurus(),
                candidatSelected.getIdConcepte())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "l'action a échoué");          
        } else {
            try {
                showMessage(FacesMessage.SEVERITY_INFO, "l'action a réussi");
                initCandidatModule();
                getAllCandidatsByThesoAndLangue();                
                setIsListCandidatsActivate(true);
            } catch (IOException ex) {
                Logger.getLogger(CandidatBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addCollection(SelectEvent<NodeIdValue> event) {
        Optional<NodeIdValue> elementAdded = allCollections.stream()
                .filter(element -> event.getObject().getId().equalsIgnoreCase(element.getId()))
                .findFirst();
        if (elementAdded.isPresent()) {
            try {
                new DomaineDao().addNewDomaine(connect, elementAdded.get().getId(),
                        candidatSelected.getIdThesaurus(),
                        candidatSelected.getIdConcepte());

                candidatSelected.getCollections().add(elementAdded.get());
                collectionTemps = Collections.emptyList();
                PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatCollection");

                showMessage(FacesMessage.SEVERITY_INFO, "Collection ajoutée avec succès !");
            } catch(Exception exception) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant l'enregistrement de la nouvelle collection !");
            }
        }
    }

    public void removeCollection(NodeIdValue collection) {
        if (CollectionUtils.isNotEmpty(candidatSelected.getCollections())) {
            candidatSelected.getCollections().remove(collection);
            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatCollection");
        }
    }

    public void onTraductionGenericSelect(SelectEvent<NodeIdValue> event) {
        /*
        Optional<NodeIdValue> elementAdded = allTermesGenerique.stream()
                .filter(element -> event.getObject().getId().equalsIgnoreCase(element.getId()))
                .findFirst();
        if (elementAdded.isPresent()) {
            if (candidatSelected.getTermesGenerique().stream()
                    .filter(element -> element.getValue().equalsIgnoreCase(elementAdded.get().getValue()))
                    .findFirst().isPresent()) {
                showMessage(FacesMessage.SEVERITY_WARN, "Le terme generique existe déjà !");
            } else {*/

                     //   (NodeIdValue)event.getObject());//elementAdded.get());
           //     allTermesGenerique.remove(elementAdded.get());
           // }
         //   termesGeneriqueTmp = Collections.emptyList();
         //   PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatBT");
       // }*/
       //####### Désactivé par Miled, ne prend pas en charge tous les concepts du thésaurus ######

        var elementAdded = event.getObject();
        Optional<NodeIdValue> elOptional = Optional.of(elementAdded);
        
        if (candidatSelected.getTermesGenerique().stream()
                .filter(element -> element.getId().equalsIgnoreCase(elOptional.get().getId()))
                .findFirst().isPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Le terme existe déjà !");
        } else {
            elementAdded.setValue(new ConceptHelper().getLexicalValueOfConcept(
                    connect.getPoolConnexion(), elementAdded.getId(), selectedTheso.getCurrentIdTheso(),selectedTheso.getCurrentLang()));
            candidatSelected.getTermesGenerique().add(elementAdded); 
        }
        termesGeneriqueTmp = Collections.emptyList();
        PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatBT");        
    }

    public void onTraductionAssocieSelect(SelectEvent<NodeIdValue> event) {

    /*    Optional<NodeIdValue> elementAdded = AllTermesAssocies.stream()
                .filter(element -> event.getObject().getId().equalsIgnoreCase(element.getId()))
                .findFirst();
        if (elementAdded.isPresent()) {
            if (candidatSelected.getTermesAssocies().stream()
                    .filter(element -> element.getValue().equalsIgnoreCase(elementAdded.get().getValue()))
                    .findFirst().isPresent()) {
                showMessage(FacesMessage.SEVERITY_WARN, "Le terme associé existe déjà !");
            } else {
                candidatSelected.getTermesAssocies().add(elementAdded.get());
                AllTermesAssocies.remove(elementAdded.get());
            }
            termesAssociesTmp = Collections.emptyList();
            PrimeFaces.current().ajax().update("tabViewCandidat");
        }*/
        NodeIdValue elementAdded = (NodeIdValue)event.getObject();
        Optional<NodeIdValue> elOptional = Optional.of(elementAdded);
        
        if (candidatSelected.getTermesAssocies().stream()
                .filter(element -> element.getId().equalsIgnoreCase(elOptional.get().getId()))
                .findFirst().isPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Le terme existe déjà !");
        } else {        
            elementAdded.setValue(new ConceptHelper().getLexicalValueOfConcept(
                    connect.getPoolConnexion(), elementAdded.getId(), selectedTheso.getCurrentIdTheso(),selectedTheso.getCurrentLang()));
            candidatSelected.getTermesAssocies().add(elementAdded); 
        }
        termesAssociesTmp = Collections.emptyList();
        PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatRT");     
    
    
    
    }

    public void onRelationBTAdded(SelectEvent<NodeIdValue> event) {
        Optional<NodeIdValue> elementAdded = allCollections.stream()
                .filter(element -> event.getObject().getId().equalsIgnoreCase(element.getId()))
                .findFirst();
        if (elementAdded.isPresent()) {
            candidatSelected.getCollections().add(elementAdded.get());
            collectionTemps = new ArrayList<>();
            PrimeFaces.current().ajax().update("tabViewCandidat");
        }
    }

    /**
     * permet de récupérer les anciens candidats saisies dans l'ancien module
     * uniquement les candidats qui étatient en attente
     */
    public void getOldCandidates() {
        String messageInfo = new CandidatService().getOldCandidates(connect, selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser(), roleOnThesoBean.getNodePreference());
        showMessage(FacesMessage.SEVERITY_INFO, messageInfo);
        getAllCandidatsByThesoAndLangue();
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces.current().ajax().update("messageIndex");
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

    public List<CandidatDto> getSelectedCandidates() {
        return selectedCandidates;
    }
    
    public List<String> getSelectedCandidatesAsId() {
        List<String> listIdOfConcept = new ArrayList<>();
        for (CandidatDto selectedCandidate : selectedCandidates) {
            listIdOfConcept.add(selectedCandidate.getIdConcepte());
        }
        return listIdOfConcept;
    }    

    public void setSelectedCandidates(List<CandidatDto> selectedCandidates) {
        this.selectedCandidates = selectedCandidates;
    }

    public boolean isListSelected() {
        return listSelected;
    }

    public void setListSelected(boolean listSelected) {
        this.listSelected = listSelected;
    }

    public boolean isTraductionVisible() {
        return traductionVisible;
    }

    public void setTraductionVisible(boolean traductionVisible) {
        this.traductionVisible = traductionVisible;
    }

    public SelectedTheso getSelectedTheso() {
        return selectedTheso;
    }

    public void setSelectedTheso(SelectedTheso selectedTheso) {
        this.selectedTheso = selectedTheso;
    }

    public RoleOnThesoBean getRoleOnThesoBean() {
        return roleOnThesoBean;
    }

    public void setRoleOnThesoBean(RoleOnThesoBean roleOnThesoBean) {
        this.roleOnThesoBean = roleOnThesoBean;
    }

    public CandidatService getCandidatService() {
        return candidatService;
    }

    public void setNewCandidatActivate(boolean newCandidatActivate) {
        isNewCandidatActivate = newCandidatActivate;
    }

    public void setRejectCandidatsActivate(boolean rejectCandidatsActivate) {
        isRejectCandidatsActivate = rejectCandidatsActivate;
    }

    public void setInitialCandidat(CandidatDto initialCandidat) {
        this.initialCandidat = initialCandidat;
    }

    public void setExportFormat(List<String> exportFormat) {
        this.exportFormat = exportFormat;
    }

    public void setRejetCadidat(List<CandidatDto> rejetCadidat) {
        this.rejetCadidat = rejetCadidat;
    }

    public void setDomaines(List<DomaineDto> domaines) {
        this.domaines = domaines;
    }

    public void setLanguagesOfTheso(ArrayList<NodeLangTheso> languagesOfTheso) {
        this.languagesOfTheso = languagesOfTheso;
    }

    public List<NodeIdValue> getCollectionTemps() {
        return collectionTemps;
    }

    public void setCollectionTemps(List<NodeIdValue> collectionTemps) {
        this.collectionTemps = collectionTemps;
    }

    public List<NodeIdValue> getTermesGeneriqueTmp() {
        return termesGeneriqueTmp;
    }

    public void setTermesGeneriqueTmp(List<NodeIdValue> termesGeneriqueTmp) {
        this.termesGeneriqueTmp = termesGeneriqueTmp;
    }

    public List<NodeIdValue> getTermesAssociesTmp() {
        return termesAssociesTmp;
    }

    public void setTermesAssociesTmp(List<NodeIdValue> termesAssociesTmp) {
        this.termesAssociesTmp = termesAssociesTmp;
    }

    public String getEmployePour() {
        return employePour;
    }

    public void setEmployePour(String employePour) {
        this.employePour = employePour;
    }

    public NodeAlignment getAlignementSelected() {
        return alignementSelected;
    }

    public void setAlignementSelected(NodeAlignment alignementSelected) {
        this.alignementSelected = alignementSelected;
    }

    public void deleteAlignement() {
        new AlignmentHelper().deleteAlignment(connect.getPoolConnexion(),
                alignementSelected.getId_alignement(),
                selectedTheso.getCurrentIdTheso());

        candidatSelected.setAlignments(new AlignmentHelper().getAllAlignmentOfConcept(connect.getPoolConnexion(),
                candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso()));

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement supprimé avec succès !");

        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public void updateAlignement() {
        new AlignmentHelper().updateAlignment(connect.getPoolConnexion(),
                alignementSelected.getId_alignement(),
                alignementSelected.getConcept_target(),
                alignementSelected.getThesaurus_target(),
                alignementSelected.getUri_target(),
                alignementSelected.getAlignement_id_type(),
                candidatSelected.getIdConcepte(),
                selectedTheso.getCurrentIdTheso());

        candidatSelected.setAlignments(new AlignmentHelper().getAllAlignmentOfConcept(connect.getPoolConnexion(),
                candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso()));

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement mise à jour avec succès !");

        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public String getCreatedByBtnTitle() {
        var createdBy = (candidatSelected != null && StringUtils.isNotEmpty(candidatSelected.getCreatedBy())) ?
                " " + languageBean.getMsg("rightbody.concept.createdBy") + candidatSelected.getCreatedBy() : "";
        return languageBean.getMsg("candidat.file") + createdBy;
    }
}
