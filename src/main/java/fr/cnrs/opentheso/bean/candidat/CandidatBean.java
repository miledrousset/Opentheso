package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.repositories.AlignmentHelper;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.bean.alignment.AlignmentBean;
import fr.cnrs.opentheso.bean.alignment.AlignmentManualBean;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.candidats.DomaineDao;
import fr.cnrs.opentheso.repositories.candidats.NoteDao;
import fr.cnrs.opentheso.repositories.candidats.RelationDao;
import fr.cnrs.opentheso.repositories.candidats.TermeDao;
import fr.cnrs.opentheso.models.candidats.CandidatDto;
import fr.cnrs.opentheso.models.candidats.DomaineDto;
import fr.cnrs.opentheso.models.candidats.enumeration.VoteType;
import fr.cnrs.opentheso.bean.concept.ImageBean;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.ImageService;
import fr.cnrs.opentheso.services.candidats.CandidatService;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.factory.annotation.Value;
import jakarta.inject.Named;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.context.ExternalContext;
import jakarta.servlet.http.HttpServletRequest;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;


@Data
@SessionScoped
@Named(value = "candidatBean")
public class CandidatBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private final UserRepository userRepository;
    private final SelectedTheso selectedTheso;
    private final NoteDao noteDao;
    private final RoleOnThesoBean roleOnThesoBean;
    private final CurrentUser currentUser;
    private final LanguageBean languageBean;
    private final ConceptView conceptView;
    private final ImageBean imageBean;
    private final AlignmentBean alignmentBean;
    private final AlignmentManualBean alignmentManualBean;
    private final NoteHelper noteHelper;
    private final RelationsHelper relationsHelper;
    private final RelationDao relationDao;
    private final SearchHelper searchHelper;
    private final ConceptDcTermRepository conceptDcTermRepository;
    private final ImageService imageService;
    private final DomaineDao domaineDao;
    private final TermHelper termHelper;
    private final GroupHelper groupHelper;
    private final ConceptHelper conceptHelper;
    private final CandidatService candidatService;
    private final AlignmentHelper alignmentHelper;
    private final UserHelper userHelper;
    private final TermeDao termeDao;
    private final ThesaurusHelper thesaurusHelper;

    private boolean isListCandidatsActivate, isNewCandidatActivate, isShowCandidatActivate;
    private boolean isRejectCandidatsActivate, isAcceptedCandidatsActivate, isExportViewActivate, isImportViewActivate;
    private boolean myCandidatsSelected1, myCandidatsSelected2, myCandidatsSelected3;
    private boolean listSelected, traductionVisible, isModifiedLabel;
    private int tabViewIndexSelected, progressBarStep, progressBarValue;

    private NodeAlignment alignementSelected;
    private String employePour, message, definition, selectedExportFormat, searchValue1, searchValue2, searchValue3;
    private CandidatDto candidatSelected, initialCandidat;
    private List<String> exportFormat;
    private List<CandidatDto> selectedCandidates, candidatList, rejetCadidat, acceptedCadidat, allTermes;
    private List<DomaineDto> domaines;
    private List<NodeLangTheso> selectedLanguages, languagesOfTheso;
    private List<NodeIdValue> allCollections, allTermesGenerique, AllTermesAssocies, collectionTemps, termesGeneriqueTmp, termesAssociesTmp;


    public CandidatBean(UserRepository userRepository, SelectedTheso selectedTheso, NoteDao noteDao,
                        RoleOnThesoBean roleOnThesoBean, CurrentUser currentUser, LanguageBean languageBean,
                        ConceptView conceptView, ImageBean imageBean, AlignmentBean alignmentBean,
                        AlignmentManualBean alignmentManualBean, NoteHelper noteHelper, RelationsHelper relationsHelper,
                        RelationDao relationDao, SearchHelper searchHelper, ConceptDcTermRepository conceptDcTermRepository,
                        ImageService imageService, DomaineDao domaineDao,
                        TermHelper termHelper, GroupHelper groupHelper, ConceptHelper conceptHelper,
                        CandidatService candidatService, AlignmentHelper alignmentHelper, UserHelper userHelper,
                        TermeDao termeDao, ThesaurusHelper thesaurusHelper) {

        this.userRepository = userRepository;
        this.selectedTheso = selectedTheso;
        this.noteDao = noteDao;
        this.roleOnThesoBean = roleOnThesoBean;
        this.currentUser = currentUser;
        this.languageBean = languageBean;
        this.conceptView = conceptView;
        this.imageBean = imageBean;
        this.alignmentBean = alignmentBean;
        this.alignmentManualBean = alignmentManualBean;
        this.noteHelper = noteHelper;
        this.relationsHelper = relationsHelper;
        this.relationDao = relationDao;
        this.searchHelper = searchHelper;
        this.conceptDcTermRepository = conceptDcTermRepository;
        this.imageService = imageService;
        this.domaineDao = domaineDao;
        this.termHelper = termHelper;
        this.groupHelper = groupHelper;
        this.conceptHelper = conceptHelper;
        this.candidatService = candidatService;
        this.alignmentHelper = alignmentHelper;
        this.userHelper = userHelper;
        this.termeDao = termeDao;
        this.thesaurusHelper = thesaurusHelper;
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
        isModifiedLabel = false;

        collectionTemps = new ArrayList<>();
        termesGeneriqueTmp = new ArrayList<>();
        termesAssociesTmp = new ArrayList<>();

        candidatList = new ArrayList<>();
        allTermes = new ArrayList<>();
        domaines = new ArrayList<>();
        selectedLanguages = new ArrayList<>();
        rejetCadidat = new ArrayList<>();
        acceptedCadidat = new ArrayList<>();
        selectedCandidates = new ArrayList<>();

        getAllCandidatsByThesoAndLangue();
        tabViewIndexSelected = 0;

        alignementSelected = new NodeAlignment();

        exportFormat = Arrays.asList("skos", "json", "jsonLd", "turtle");
        selectedExportFormat = "skos";

        try {
            languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
            languagesOfTheso.forEach((nodeLang) -> {
                selectedLanguages.add(nodeLang);
            });
        } catch (Exception e) {
        }
    }

    public void onTabChange(TabChangeEvent event) {
        switch(event.getTab().getTitle()) {
            case "Concepts insérés":
                getAcceptedCandidatByThesoAndLangue();
                break;
            case "Concepts rejetés":
                getRejectCandidatByThesoAndLangue();
                break;
            default:
                getAllCandidatsByThesoAndLangue();
        }
    }

    public void getAllCandidatsByThesoAndLangue() {
        isModifiedLabel = false;
        tabViewIndexSelected = 0;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            candidatList = candidatService.getCandidatsByStatus(selectedTheso.getSelectedIdTheso(), getIdLang(), 1);
        } else {
            candidatList.clear();
        }
    }

    public void getRejectCandidatByThesoAndLangue() {
        tabViewIndexSelected = 2;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            rejetCadidat = candidatService.getCandidatsByStatus(selectedTheso.getSelectedIdTheso(), getIdLang(), 3);
        } else {
            rejetCadidat.clear();
        }
    }

    public void getAcceptedCandidatByThesoAndLangue() {
        tabViewIndexSelected = 1;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            acceptedCadidat = candidatService.getCandidatsByStatus(selectedTheso.getSelectedIdTheso(), getIdLang(), 2);
        } else {
            acceptedCadidat.clear();
        }
        isAcceptedCandidatsActivate = true;
    }

    /**
     * permet de supprimer les candidats sélectionnés
     *
     * @param idUser
     */
    public void deleteSelectedCandidate(int idUser) {
        if (CollectionUtils.isEmpty(selectedCandidates)) {
            return;
        }

        for (CandidatDto selectedCandidate : selectedCandidates) {
            if (!conceptHelper.deleteConcept(selectedCandidate.getIdConcepte(),
                    selectedCandidate.getIdThesaurus(), idUser)) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de suppression");
                return;
            }
        }
        initCandidatModule();
        getAllCandidatsByThesoAndLangue();
        showMessage(FacesMessage.SEVERITY_INFO, "Candidats supprimés");
    }

    /**
     * permet de supprimer le candidat sélectionné
     */
    public void deleteCandidate(int idUser) {
        if (candidatSelected == null) {
            return;
        }
        if (!conceptHelper.deleteConcept(candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), idUser)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de suppression");
            return;
        }

        initCandidatModule();
        getAllCandidatsByThesoAndLangue();
        showMessage(FacesMessage.SEVERITY_INFO, "Candidat supprimé");
    }

    /**
     * permet de savoir si l'identifiant actuel est propriétaire du candidat
     *
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

        if (roleOnThesoBean.getNodePreference() != null) {
            return roleOnThesoBean.getNodePreference().getSourceLang();
        }
        return workLanguage;
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

    public String getCountOfCandidats() {
        return "" + candidatList.size();
    }

    public String getCountOfAcceptedCandidats() {
        return "" + acceptedCadidat.size();
    }

    public String getCountOfRejectedCandidats() {
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
                    .filter(candidat -> checkCandidat(candidat))
                    .collect(Collectors.toList());
        } else {
            getRejectCandidatByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(rejetCadidat.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    private boolean checkCandidat(CandidatDto candidat) {
        return (StringUtils.isNotEmpty(candidat.getNomPref()) && candidat.getNomPref().contains(searchValue3))
                || (StringUtils.isNotEmpty(candidat.getUser()) && candidat.getUser().contains(searchValue3));
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
                    .filter(candidat -> candidatCheck(candidat))
                    .collect(Collectors.toList());
        } else {
            getAcceptedCandidatByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(acceptedCadidat.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    private boolean candidatCheck(CandidatDto candidat) {
        return (StringUtils.isNotEmpty(candidat.getNomPref()) && candidat.getNomPref().contains(searchValue2))
                || (StringUtils.isNotEmpty(candidat.getUser()) && candidat.getUser().contains(searchValue2));
    }

    public void searchByTermeAndAuteur() {
        if (!StringUtils.isEmpty(searchValue1)) {
            candidatList = candidatService.searchCandidats(searchValue1,
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

        if (!alignmentHelper.deleteAlignment(
                nodeAlignment.getId_alignement(),
                selectedTheso.getCurrentIdTheso())) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        candidatSelected.setAlignments(alignmentHelper.getAllAlignmentOfConcept(
                candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso()));

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("tabViewCandidat");
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
        candidatService.getCandidatDetails(candidatSelected, selectedTheso.getCurrentIdTheso());
    }

    public void showCandidatSelected(CandidatDto candidatDto) {

        tabViewIndexSelected = 0;

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }

        candidatSelected = candidatDto;
        candidatSelected.setLang(getIdLang());
        candidatSelected.setUserId(currentUser.getNodeUser().getIdUser());
        candidatSelected.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatService.getCandidatDetails(candidatSelected, selectedTheso.getCurrentIdTheso());
        initialCandidat = new CandidatDto(candidatSelected);

        allTermes = candidatList.stream().filter(candidat -> !candidat.getNomPref().equals(candidatDto.getNomPref()))
                .collect(Collectors.toList());

        isShowCandidatActivate = true;
        isNewCandidatActivate = false;
        isListCandidatsActivate = false;

        alignmentManualBean.reset();
    }

    public CandidatDto getAllInfosOfCandidate(CandidatDto candidatDto) {
        candidatDto.setLang(getIdLang());
        candidatDto.setUserId(currentUser.getNodeUser().getIdUser());
        candidatDto.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatService.getCandidatDetails(candidatDto, selectedTheso.getCurrentIdTheso());
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

        if (isNewCandidatActivate) {
            if (StringUtils.isEmpty(definition)) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.def"));
                return;
            }
        }

        if (roleOnThesoBean.getNodePreference() == null) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg2"));
            return;
        }

        if (initialCandidat == null) {

            // en cas d'un nouveau candidat, verification dans les prefLabels
            if (termHelper.isPrefLabelExist(candidatSelected.getNomPref().trim(), selectedTheso.getCurrentIdTheso(), getIdLang())) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg3"));
                return;
            }
            // verification dans les altLabels
            if (termHelper.isAltLabelExist(candidatSelected.getNomPref().trim(), selectedTheso.getCurrentIdTheso(), getIdLang())) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg4"));
                return;
            }

            conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

            var idNewConcept = candidatService.saveNewCondidat(Concept.builder()
                    .idConcept(candidatSelected.getIdConcepte())
                    .idThesaurus(selectedTheso.getCurrentIdTheso())
                    .topConcept(false)
                    .lang(getIdLang())
                    .idUser(currentUser.getNodeUser().getIdUser())
                    .userName(currentUser.getUsername())
                    .status("CA")
                    .build());

            if (idNewConcept == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("candidat.save.msg5"));
                return;
            }
            candidatSelected.setIdConcepte(idNewConcept);

            var terme = Term.builder()
                    .lang(getIdLang())
                    .idThesaurus(selectedTheso.getCurrentIdTheso())
                    .contributor(currentUser.getNodeUser().getIdUser())
                    .lexicalValue(candidatSelected.getNomPref().trim())
                    .source("candidat")
                    .status("D")
                    .build();

            candidatSelected.setIdTerm(candidatService.saveNewTerm(terme, candidatSelected.getIdConcepte(),
                    candidatSelected.getUserId()));

            noteHelper.addNote(candidatSelected.getIdConcepte(),
                    selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso(),
                    definition, "definition", "", currentUser.getNodeUser().getIdUser());

            setIsListCandidatsActivate(true);
        } else {
            if (!initialCandidat.getNomPref().equals(candidatSelected.getNomPref())) {
                if (termHelper.isTermExistInThisLang(candidatSelected.getIdTerm(), getIdLang(), candidatSelected.getIdThesaurus())) {
                    candidatService.updateIntitule(candidatSelected.getNomPref(), candidatSelected.getIdThesaurus(),
                            getIdLang(), candidatSelected.getIdTerm());
                } else {
                    Term term = new Term();
                    term.setIdThesaurus(selectedTheso.getCurrentIdTheso());
                    term.setLang(getIdLang());
                    term.setContributor(currentUser.getNodeUser().getIdUser());
                    term.setLexicalValue(candidatSelected.getNomPref().trim());
                    term.setSource("candidat");
                    term.setStatus("D");
                    term.setIdTerm(candidatSelected.getIdTerm());
                    termeDao.addNewTerme(term);
                }

            }
        }

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CREATOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(candidatSelected.getIdConcepte())
                .idThesaurus(candidatSelected.getIdThesaurus())
                .build());

        candidatService.updateDetailsCondidat(candidatSelected);

        candidatSelected.setNodeNotes(noteDao.getNotesCandidat(candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));
        definition = "";
        isNewCandidatActivate = false;
        isListCandidatsActivate = false;
        isShowCandidatActivate = true;

        showMessage(FacesMessage.SEVERITY_INFO, "Candidat enregistré avec succès");
    }

    public List<NodeIdValue> searchCollection(String enteredValue) {

        if (StringUtils.isNotEmpty(enteredValue)) {
            allCollections = groupHelper.searchGroup(selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(), enteredValue);
            return createCollectionsFiltred(allCollections, candidatSelected.getCollections());
        } else {
            return Collections.emptyList();
        }
    }

    private List<NodeIdValue> createCollectionsFiltred(List<NodeIdValue> collections, List<NodeIdValue> collectionsSelected) {
        if (CollectionUtils.isNotEmpty(collections)) {
            return collections.stream()
                    .filter(element -> !isExist(collectionsSelected, element))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
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
            if (candidatService.getVote(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                    currentUser.getNodeUser().getIdUser(), null, VoteType.CANDIDAT)) {
                candidatService.removeVote(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), null, VoteType.CANDIDAT);
                candidatSelected.setVoted(false);
            } else {
                // cas ou il n'y a pas de vote, alors on vote
                candidatService.addVote(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
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
        PrimeFaces.current().ajax().update("tabViewCandidat");
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
            if (candidatService.getVote(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                    currentUser.getNodeUser().getIdUser(), nodeNote.getIdNote() + "", VoteType.NOTE)) {

                candidatService.removeVote(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), nodeNote.getIdNote() + "", VoteType.NOTE);
                nodeNote.setVoted(false);
            } else {
                // cas ou il n'y a pas de vote, alors on vote
                candidatService.addVote(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), nodeNote.getIdNote() + "", VoteType.NOTE);
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

        if (StringUtils.isNotEmpty(value)) {
            allTermesGenerique = searchHelper.searchAutoCompletionForRelationIdValue(value,
                    selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
            return createCollectionsFiltred(allTermesGenerique, candidatSelected.getTermesGenerique());
        } else {
            return Collections.emptyList();
        }
    }

    public List<NodeIdValue> searchTermeAssocie(String value) {

        if (StringUtils.isNotEmpty(value)) {
            AllTermesAssocies = searchHelper.searchAutoCompletionForRelationIdValue(value,
                    selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
            return createCollectionsFiltred(AllTermesAssocies, candidatSelected.getTermesAssocies());
        } else {
            return Collections.emptyList();
        }
    }

    public void initialNewCandidat() throws IOException {
        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }
        setIsNewCandidatActivate(true);

        candidatSelected = new CandidatDto();
        candidatSelected.setIdConcepte(null);
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
        var user = userRepository.findById(idUser);
        return user.isPresent() ? userRepository.findById(idUser).get().getUsername() : "";

    }

    public void reactivateRejectedCandidat() throws IOException {
        if (candidatSelected == null || candidatSelected.getIdConcepte() == null || candidatSelected.getIdConcepte().isEmpty()) {
            return;
        }

        if (!candidatService.updateCandidatStatus(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(), 1)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "l'action a échoué");
        } else {
            showMessage(FacesMessage.SEVERITY_INFO, "l'action a réussi");
            initCandidatModule();
            getAllCandidatsByThesoAndLangue();
            setIsListCandidatsActivate(true);
        }
    }

    public void addCollection(SelectEvent<NodeIdValue> event) {
        Optional<NodeIdValue> elementAdded = allCollections.stream()
                .filter(element -> event.getObject().getId().equalsIgnoreCase(element.getId()))
                .findFirst();
        if (elementAdded.isPresent()) {
            domaineDao.addNewDomaine(elementAdded.get().getId(),
                    candidatSelected.getIdThesaurus(),
                    candidatSelected.getIdConcepte());

            candidatSelected.getCollections().add(elementAdded.get());
            collectionTemps = Collections.emptyList();
            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatCollection");

            showMessage(FacesMessage.SEVERITY_INFO, "Collection ajoutée avec succès !");
        }
    }

    public void removeCollection(NodeIdValue collection) {
        if (CollectionUtils.isNotEmpty(candidatSelected.getCollections())) {
            domaineDao.deleteDomaine(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(), collection.getId());

            candidatSelected.getCollections().remove(collection);
            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatCollection");

            showMessage(FacesMessage.SEVERITY_INFO, "Collection supprimée avec succès !");
        }
    }

    public void addSynonyme() {

        if (StringUtils.isNotEmpty(employePour)) {
            if (candidatSelected.getEmployePourList().contains(employePour)) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Le mot '" + employePour + "' existe déjà !");
            } else {
                try {
                    termeDao.addNewEmployePour(employePour, candidatSelected.getIdThesaurus(),
                            candidatSelected.getLang(), candidatSelected.getIdTerm());

                    candidatSelected.getEmployePourList().add(employePour);
                    employePour = "";
                    PrimeFaces.current().ajax().update("tabViewCandidat");
                    showMessage(FacesMessage.SEVERITY_INFO, "Synonyme ajouté avec succès !");
                } catch (Exception ex) {
                    showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant l'ajout du nouveau synonyme !");
                }
            }
        }
    }

    public void removeSynonyme(String synonyme) {
        if (CollectionUtils.isNotEmpty(candidatSelected.getEmployePourList())) {
            termeDao.deleteEMByIdTermAndLang(candidatSelected.getIdTerm(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getLang());

            candidatSelected.getEmployePourList().remove(synonyme);
            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatSynonym");

            showMessage(FacesMessage.SEVERITY_INFO, "Synonyme supprimé avec succès !");
        }
    }

    public void addTraduction(SelectEvent<NodeIdValue> event) throws SQLException {
        var elementAdded = event.getObject();

        if (candidatSelected.getTermesGenerique().stream()
                .filter(element -> element.getId().equalsIgnoreCase(elementAdded.getId()))
                .findFirst().isPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Le terme existe déjà !");
        } else {

            relationDao.addRelationBT(candidatSelected.getIdConcepte(), elementAdded.getId(), selectedTheso.getCurrentIdTheso());

            candidatSelected.setTermesGenerique(relationDao.getCandidatRelationsBT(
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            showMessage(FacesMessage.SEVERITY_INFO, "Term générique ajoutée avec succès !");

            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatBT");
        }
        termesGeneriqueTmp = Collections.emptyList();
    }

    public void removeGenericTerm(NodeIdValue genericTerm) throws SQLException {
        if (CollectionUtils.isNotEmpty(candidatSelected.getTermesGenerique())) {

            relationsHelper.deleteRelationBT(candidatSelected.getIdConcepte(),
                    selectedTheso.getCurrentIdTheso(), genericTerm.getId(), currentUser.getNodeUser().getIdUser());

            candidatSelected.setTermesGenerique(relationDao.getCandidatRelationsBT(
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatBT");

            showMessage(FacesMessage.SEVERITY_INFO, "Term générique supprimée avec succès !");
        }
    }

    public void addTraductionAssocieSelect(SelectEvent<NodeIdValue> event) throws SQLException {

        if (candidatSelected.getTermesAssocies().stream()
                .filter(element -> element.getId().equalsIgnoreCase(event.getObject().getId()))
                .findFirst().isPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Le terme existe déjà !");
        } else {
            relationDao.addRelationRT(candidatSelected.getIdConcepte(), event.getObject().getId(), selectedTheso.getCurrentIdTheso());

            candidatSelected.setTermesAssocies(relationDao.getCandidatRelationsRT(
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            showMessage(FacesMessage.SEVERITY_INFO, "Term associé ajouté avec succès !");
            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatRT");
        }
        termesAssociesTmp = Collections.emptyList();
    }

    public void removeAssociesTerm(NodeIdValue associeTerm) throws SQLException {

        relationsHelper.deleteRelationRT(candidatSelected.getIdConcepte(),
                selectedTheso.getCurrentIdTheso(), associeTerm.getId(), currentUser.getNodeUser().getIdUser());

        candidatSelected.setTermesAssocies(relationDao.getCandidatRelationsRT(
                candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                candidatSelected.getLang()));

        showMessage(FacesMessage.SEVERITY_INFO, "Term associé supprimé avec succès !");
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
        String messageInfo = candidatService.getOldCandidates(selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser(), roleOnThesoBean.getNodePreference());
        showMessage(FacesMessage.SEVERITY_INFO, messageInfo);
        getAllCandidatsByThesoAndLangue();
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public void setListCandidatsActivate(boolean isListCandidatsActivate) {
        getAllCandidatsByThesoAndLangue();
        this.isListCandidatsActivate = isListCandidatsActivate;
        isImportViewActivate = false;
        isExportViewActivate = false;
    }

    public void setExportViewActivate(boolean isExportViewActivate) {
        this.isExportViewActivate = isExportViewActivate;
        isImportViewActivate = false;
        isListCandidatsActivate = false;
        isShowCandidatActivate = false;

        progressBarStep = 0;
        progressBarValue = 0;
    }

    public void setImportViewActivate(boolean isImportViewActivate) {
        this.isImportViewActivate = isImportViewActivate;
        isExportViewActivate = false;
        isListCandidatsActivate = false;
        isShowCandidatActivate = false;

        progressBarStep = 0;
        progressBarValue = 0;
    }

    public List<String> getSelectedCandidatesAsId() {
        List<String> listIdOfConcept = new ArrayList<>();
        for (CandidatDto selectedCandidate : selectedCandidates) {
            listIdOfConcept.add(selectedCandidate.getIdConcepte());
        }
        return listIdOfConcept;
    }

    public void deleteAlignement() {
        alignmentHelper.deleteAlignment(
                alignementSelected.getId_alignement(),
                selectedTheso.getCurrentIdTheso());

        candidatSelected.setAlignments(alignmentHelper.getAllAlignmentOfConcept(
                candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso()));

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement supprimé avec succès !");

        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public void updateAlignement() {
        alignmentHelper.updateAlignment(
                alignementSelected.getId_alignement(),
                alignementSelected.getConcept_target(),
                alignementSelected.getThesaurus_target(),
                alignementSelected.getUri_target(),
                alignementSelected.getAlignement_id_type(),
                candidatSelected.getIdConcepte(),
                selectedTheso.getCurrentIdTheso());

        candidatSelected.setAlignments(alignmentHelper.getAllAlignmentOfConcept(
                candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso()));

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement mise à jour avec succès !");

        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public String getCreatedByBtnTitle() {
        var createdBy = (candidatSelected != null && StringUtils.isNotEmpty(candidatSelected.getCreatedBy()))
                ? " " + languageBean.getMsg("rightbody.concept.createdBy") + " " + candidatSelected.getCreatedBy() : "";
        return languageBean.getMsg("candidat.file") + createdBy;
    }

    public void addNewImage(int idUser) {

        if (StringUtils.isEmpty(imageBean.getUri())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucune URI insérée !");
            return;
        }

        imageService.addExternalImage(candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso(), imageBean.getName(),
                imageBean.getCopyright(), imageBean.getUri(), imageBean.getCreator(), idUser);

        candidatSelected.setImages(imageService.getAllExternalImages(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte()));

        showMessage(FacesMessage.SEVERITY_INFO, "Image ajoutée avec succès");
        initImageDialog();
        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public void openAddAlignementWindow() {
        alignmentBean.setConceptValueForAlignment(candidatSelected.getNomPref());
        alignmentBean.setExistingAlignment(candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso());
        alignmentBean.prepareValuesForIdRef();
        alignmentBean.setListAlignValues(null);
        alignmentBean.initAlignmentSources(selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        alignmentBean.setIdConceptSelectedForAlignment(candidatSelected.getIdConcepte());

        PrimeFaces.current().executeScript("PF('searchAlignement').show();");
    }

    public void searchAlignementAuto() {
        alignmentBean.addAlignment(selectedTheso.getSelectedIdTheso(),
                alignmentBean.getIdConceptSelectedForAlignment(),
                currentUser.getNodeUser().getIdUser(),
                false);

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement ajouté avec sucée !");

        candidatService.getCandidatDetails(candidatSelected, selectedTheso.getCurrentIdTheso());

        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public String getNoteType(String typeCode) {
        switch (typeCode) {
            case "note":
                return "Note";
            case "historyNote":
                return "Note historique";
            case "scopeNote":
                return "Note d'application";
            case "example":
                return "Exemple";
            case "editorialNote":
                return "Note éditoriale";
            case "definition":
                return "Définition";
            default:
                return "Note de changement";
        }
    }

    public void initImageDialog() {
        imageBean.setUri(null);
        imageBean.setCopyright(null);
        imageBean.setName(null);
        imageBean.setCreator(null);
    }

    public void deleteImage(String imageUri) {

        imageService.deleteImages(candidatSelected.getIdConcepte(), selectedTheso.getSelectedIdTheso(), imageUri);

        candidatSelected.setImages(imageService.getAllExternalImages(candidatSelected.getIdThesaurus(),
                candidatSelected.getIdConcepte()));

        showMessage(FacesMessage.SEVERITY_INFO, "Image supprimée avec succès");
    }

    public boolean isIsModifiedLabel() {
        return isModifiedLabel;
    }

    public void setIsModifiedLabel(boolean isModifiedLabel) {
        this.isModifiedLabel = isModifiedLabel;
    }

    public void changeStateOfLabel() {
        this.isModifiedLabel = true;
    }

    public void updateCandidateLabel() {
        candidatService.updateIntitule(candidatSelected.getNomPref(), candidatSelected.getIdThesaurus(),
                candidatSelected.getLang(), candidatSelected.getIdTerm());
        isModifiedLabel = false;

    }
}
