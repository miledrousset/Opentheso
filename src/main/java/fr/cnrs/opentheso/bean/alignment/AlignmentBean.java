/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.alignment;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.ExternalImagesHelper;
import fr.cnrs.opentheso.bdd.helper.GpsHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentSmall;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.core.alignment.AlignementSource;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import fr.cnrs.opentheso.core.alignment.helper.AgrovocHelper;
import fr.cnrs.opentheso.core.alignment.helper.GemetHelper;
import fr.cnrs.opentheso.core.alignment.helper.GeoNamesHelper;
import fr.cnrs.opentheso.core.alignment.helper.GettyAATHelper;
import fr.cnrs.opentheso.core.alignment.helper.IdRefHelper;
import fr.cnrs.opentheso.core.alignment.helper.OntomeHelper;
import fr.cnrs.opentheso.core.alignment.helper.OpenthesoHelper;
import fr.cnrs.opentheso.core.alignment.helper.WikidataHelper;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "alignmentBean")
@SessionScoped
public class AlignmentBean implements Serializable {

    @Inject private Connect connect;
    @Inject private ConceptView conceptView;
    @Inject private SelectedTheso selectedTheso;
    @Inject private ConceptView conceptBean;
    @Inject private AlignmentManualBean alignmentManualBean;
    @Inject private LanguageBean languageBean;
    @Inject private CurrentUser currentUser;     

    private boolean withLang;
    private boolean withNote;
    private boolean withImage;

    private boolean isViewResult = true;
    private boolean isViewSelection = false;

    private boolean allAlignementVisible, propositionAlignementVisible;
    private List<NodeAlignment> allAlignementFound, selectAlignementForAdd;

    private boolean viewSetting = false;
    private boolean viewAddNewSource = false;

    private ArrayList<AlignementSource> alignementSources;
    private String selectedAlignement;
    private AlignementSource selectedAlignementSource;
    private List<NodeAlignment> listAlignValues;
    private ArrayList<AlignementElement> allignementsList, filteredAlignement;
    private AlignementElement selectedLastPositionReportDtos;

    private NodeAlignment selectedNodeAlignment;
    private ArrayList<Map.Entry<String, String>> alignmentTypes;
    private int selectedAlignementType;

    private String nom;
    private String prenom;
    private boolean isNameAlignment = false; // pour afficher les nom et prénom
    private AlignementElement alignementElementSelected = new AlignementElement();

    private List<String> thesaurusUsedLanguageWithoutCurrentLang;
    private List<String> thesaurusUsedLanguage;

    // permet de gérer le flux des concepts 10 par 10
    private ArrayList<String> allIdsOfBranch;

    private ArrayList<NodeIdValue> idsAndValues;

    private ArrayList<String> idsToGet;
    private String idConceptSelectedForAlignment;
    private String conceptValueForAlignment;
    private int counter = 0; // initialisation du compteur

    private ArrayList<NodeTermTraduction> nodeTermTraductions;
    private ArrayList<NodeNote> nodeNotes;
    private ArrayList<NodeImage> nodeImages;

    // résultat des alignements
    private ArrayList<SelectedResource> traductionsOfAlignment;
    private ArrayList<SelectedResource> descriptionsOfAlignment;
    private ArrayList<SelectedResource> imagesOfAlignment;
    private ArrayList<NodeAlignmentSmall> nodeAlignmentSmall;

    private boolean isSelectedAllLang = true;
    private boolean isSelectedAllDef = true;
    private boolean isSelectedAllImages = true;

    private boolean alignmentInProgress = false;

    // resultat de l'alignement
    private String alignementResult = null;
    private boolean error;

    private String alertWikidata;

    //les alignements existants
    private ArrayList<NodeAlignment> existingAlignments;


    /// pour l'alignement manuel en cas de non réponse
    private String manualAlignmentUri;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void deleteAlignemen() {
        new AlignmentHelper().deleteAlignment(connect.getPoolConnexion(),
                alignementElementSelected.getIdAlignment(),
                selectedTheso.getCurrentIdTheso());

        getIdsAndValues(selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());

        initAlignementByStep(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        getIdsAndValues2(conceptBean.getSelectedLang(), selectedTheso.getCurrentIdTheso());

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Erreur !", "Alignement supprimé avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }
    }

    public void setAlignementElementSelected(AlignementElement alignementElementSelected) {
        this.alignementElementSelected = alignementElementSelected;
    }

    public AlignementElement getAlignementElementSelected() {
        return alignementElementSelected;
    }

    public void clear() {

        allAlignementVisible = true;
        propositionAlignementVisible = false;

        if (alignementSources != null) {
            alignementSources.clear();
            alignementSources = null;
        }
        selectedAlignement = null;
        selectedAlignementSource = null;
        if (listAlignValues != null) {
            listAlignValues.clear();
            listAlignValues = null;
        }
        selectedNodeAlignment = null;
        if (alignmentTypes != null) {
            alignmentTypes.clear();
            alignmentTypes = null;
        }
        nom = null;
        prenom = null;
        if (thesaurusUsedLanguageWithoutCurrentLang != null) {
            thesaurusUsedLanguageWithoutCurrentLang.clear();
            thesaurusUsedLanguageWithoutCurrentLang = null;
        }
        if (thesaurusUsedLanguage != null) {
            thesaurusUsedLanguage.clear();
            thesaurusUsedLanguage = null;
        }
        if (allIdsOfBranch != null) {
            allIdsOfBranch.clear();
            allIdsOfBranch = null;
        }
        if (idsAndValues != null) {
            idsAndValues.clear();
            idsAndValues = null;
        }
        if (idsToGet != null) {
            idsToGet.clear();
            idsToGet = null;
        }
        idConceptSelectedForAlignment = null;
        conceptValueForAlignment = null;
        if (nodeTermTraductions != null) {
            nodeTermTraductions.clear();
            nodeTermTraductions = null;
        }
        if (nodeNotes != null) {
            nodeNotes.clear();
            nodeNotes = null;
        }
        if (nodeImages != null) {
            nodeImages.clear();
            nodeImages = null;
        }
        if (traductionsOfAlignment != null) {
            traductionsOfAlignment.clear();
            traductionsOfAlignment = null;
        }
        if (descriptionsOfAlignment != null) {
            descriptionsOfAlignment.clear();
            descriptionsOfAlignment = null;
        }
        if (imagesOfAlignment != null) {
            imagesOfAlignment.clear();
            imagesOfAlignment = null;
        }
        if (nodeAlignmentSmall != null) {
            nodeAlignmentSmall.clear();
            nodeAlignmentSmall = null;
        }
        manualAlignmentUri = null;
    }

    /////// gestion de la pagination pour le traitement par lot ///////
    /**
     * retourne les dix valeurs suivantes
     *
     * @param idLang
     * @param idTheso
     */
    public void nextTen(String idLang, String idTheso) {

        if (allIdsOfBranch == null) {
            idsAndValues = null;
            return;
        }
        if (counter >= allIdsOfBranch.size()) {
            return;
        }
        idsToGet.clear();
        int counterTemp = counter;

        for (int i = counterTemp; i < counterTemp + 10; i++) {
            if (counter >= allIdsOfBranch.size()) {
                counter = allIdsOfBranch.size();
                break;
            }
            idsToGet.add(allIdsOfBranch.get(i));
            counter++;
        }
        getIdsAndValues(idLang, idTheso);
    }

    public void nextTenRecresive(String idLang, String idTheso) {

        if (allIdsOfBranch == null) {
            idsAndValues = null;
            return;
        }
        if (counter >= allIdsOfBranch.size()) {
            return;
        }
        idsToGet.clear();
        int counterTemp = counter;

        for (int i = counterTemp; i < counterTemp + 10; i++) {
            if (counter >= allIdsOfBranch.size()) {
                counter = allIdsOfBranch.size();
                break;
            }
            idsToGet.add(allIdsOfBranch.get(i));
            counter++;
        }
        getIdsAndValues(idLang, idTheso);
        nextTenRecresive(idLang, idTheso);
    }

    /**
     * retourne les dix valeurs précédantes
     *
     * @param idLang
     * @param idTheso
     */
    public void previousTen(String idLang, String idTheso) {
        if (allIdsOfBranch == null) {
            idsAndValues = null;
            return;
        }
        if (counter - 10 <= 0) {
            return;
        }
        idsToGet.clear();
        int counterTempFirst = counter - 20;
        int counterTempLast = counter - 10;
        if (counterTempFirst < 0) {
            counterTempFirst = 0;
        }

        for (int i = counterTempFirst; i < counterTempLast; i++) {
            if (counter < 0) {
                counter = 0;
                break;
            }
            idsToGet.add(allIdsOfBranch.get(i));
            counter--;
        }
        getIdsAndValues(idLang, idTheso);
    }

    /**
     * retourne à la première position
     *
     * @param idLang
     * @param idTheso
     */
    public void restart(String idLang, String idTheso) {
        if (allIdsOfBranch == null) {
            idsAndValues = null;
            return;
        }
        counter = 0;
        if (counter >= allIdsOfBranch.size()) {
            return;
        }
        idsToGet.clear();
        int counterTemp = counter;

        for (int i = counterTemp; i < counterTemp + 10; i++) {
            if (counter >= allIdsOfBranch.size()) {
                counter = allIdsOfBranch.size();
                break;
            }
            idsToGet.add(allIdsOfBranch.get(i));
            counter++;
        }
        getIdsAndValues(idLang, idTheso);
    }

    /**
     * remettre le compteur à zéro
     */
    public void resetCounter() {
        if (allIdsOfBranch != null) {
            idsAndValues = null;
        }
        counter = 0;
    }

    public void getIdsAndValues(String idLang, String idTheso) {
        if (idsToGet == null) {
            return;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        idsAndValues = conceptHelper.getIdsAndValuesOfConcepts2(
                connect.getPoolConnexion(),
                idsToGet,
                idLang,
                idTheso);
        selectConceptForAlignment(idConceptSelectedForAlignment);

        allignementsList = new ArrayList<>();

        for (NodeIdValue idsAndValue : idsAndValues) {

            ArrayList<NodeAlignment> alignements = new AlignmentHelper()
                    .getAllAlignmentOfConcept(connect.getPoolConnexion(), idsAndValue.getId(), idTheso);

            if (!CollectionUtils.isEmpty(alignements)) {
                for (NodeAlignment alignement : alignements) {
                    AlignementElement element = new AlignementElement();
                    element.setIdConceptOrig(idsAndValue.getId());
                    element.setLabelConceptOrig(idsAndValue.getValue());

                    element.setIdAlignment(alignement.getId_alignement());
                    element.setTypeAlignement(alignement.getAlignmentLabelType());
                    element.setLabelConceptCible(alignement.getConcept_target());
                    element.setTargetUri(alignement.getUri_target());
                    element.setThesaurus_target(alignement.getThesaurus_target());
                    element.setAlignement_id_type(alignement.getAlignement_id_type());
                    element.setIdSource(alignement.getId_source());
                    element.setConceptTarget(alignement.getConcept_target());
                    allignementsList.add(element);
                }
            }

            AlignementElement element = new AlignementElement();
            element.setIdConceptOrig(idsAndValue.getId());
            element.setLabelConceptOrig(idsAndValue.getValue());
            allignementsList.add(element);
        }

        sortDatatableAlignementByColor();
    }

    /**
     * Permet de charger les données des concepts à aligner ###### remplace la
     * fonction ci-dessus après vérification #####
     */
    public void getIdsAndValues2(String idLang, String idTheso) {

        idsAndValues = new ConceptHelper().getIdsAndValuesOfConcepts2(connect.getPoolConnexion(), allIdsOfBranch, idLang, idTheso);
        selectConceptForAlignment(idConceptSelectedForAlignment);

        allignementsList = new ArrayList<>();

        for (NodeIdValue idsAndValue : idsAndValues) {

            ArrayList<NodeAlignment> alignements = new AlignmentHelper()
                    .getAllAlignmentOfConcept(connect.getPoolConnexion(), idsAndValue.getId(), idTheso);

            if (!CollectionUtils.isEmpty(alignements)) {
                for (NodeAlignment alignement : alignements) {
                    AlignementElement element = new AlignementElement();
                    element.setIdConceptOrig(idsAndValue.getId());
                    element.setLabelConceptOrig(idsAndValue.getValue());

                    element.setIdAlignment(alignement.getId_alignement());
                    element.setTypeAlignement(alignement.getAlignmentLabelType());
                    element.setLabelConceptCible(alignement.getConcept_target());
                    element.setTargetUri(alignement.getUri_target());
                    element.setThesaurus_target(alignement.getThesaurus_target());
                    element.setAlignement_id_type(alignement.getAlignement_id_type());
                    element.setIdSource(alignement.getId_source());
                    element.setConceptTarget(alignement.getConcept_target());
                    allignementsList.add(element);
                }
            }

            AlignementElement element = new AlignementElement();
            element.setIdConceptOrig(idsAndValue.getId());
            element.setLabelConceptOrig(idsAndValue.getValue());
            allignementsList.add(element);
        }

        sortDatatableAlignementByColor();
    }

    public void sortDatatableAlignementByColor() {

        if (allignementsList.size() > 1) {
            allignementsList.get(0).setCodeColor(0);
            int pos = 1;
            while (pos < allignementsList.size()) {
                if (allignementsList.get(pos - 1).getIdConceptOrig()
                        .equals(allignementsList.get(pos).getIdConceptOrig())) {
                    allignementsList.get(pos)
                            .setCodeColor(allignementsList.get(pos - 1).getCodeColor());
                } else {
                    if (allignementsList.get(pos - 1).getCodeColor() == 0) {
                        allignementsList.get(pos).setCodeColor(1);
                    } else {
                        allignementsList.get(pos).setCodeColor(0);
                    }
                }
                pos++;
            }

        }

    }

    public ArrayList<AlignementElement> getAllignementsList() {
        return allignementsList;
    }

    // quand on sélectionne un concept, on récupére sa valeur du vecteur
    public void selectConceptForAlignment(String idConcept) {
        idConceptSelectedForAlignment = idConcept;
        for (NodeIdValue idsAndValue : idsAndValues) {
            if (idsAndValue.getId().equalsIgnoreCase(idConceptSelectedForAlignment)) {
                conceptValueForAlignment = idsAndValue.getValue();
                setExistingAlignment(idConcept, selectedTheso.getCurrentIdTheso());
                cancelAlignment();
                resetValuesAlignement();
                prepareValuesForIdRef();
                listAlignValues = null;
                return;
            }
        }
        listAlignValues = null;
    }


    public void searchAlignementsForAllConcepts() {

        selectAlignementForAdd = new ArrayList<>();

        allAlignementFound = new AlignementAutomatique().searchAlignementsAutomatique(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), conceptView.getSelectedLang(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), allignementsList, alignementSources, nom, prenom);

        if (CollectionUtils.isNotEmpty(allAlignementFound)) {
            allAlignementVisible = false;
            propositionAlignementVisible = true;
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "", "Aucun alignement trouvé !"));
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }

    public void annulerAlignementAutomatique() {
        selectAlignementForAdd = new ArrayList<>();
        allAlignementFound = new ArrayList<>();
        allAlignementVisible = true;
        propositionAlignementVisible = false;
    }

    public void saveAlignements() {

        if (CollectionUtils.isNotEmpty(selectAlignementForAdd)) {
            for (NodeAlignment alignment : selectAlignementForAdd) {
                addSingleAlignment(alignment, selectedTheso.getCurrentIdTheso(), alignment.getInternal_id_concept(),
                        selectedTheso.getCurrentUser().getNodeUser().getIdUser());
            }

            allAlignementVisible = true;
            propositionAlignementVisible = false;

            initAlignementByStep(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());

            getIdsAndValues2(conceptBean.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "", "Aucun alignement selectionné !"));
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }

    public void addSingleAlignment(NodeAlignment alignment, String idTheso, String idConcept, int idUser) {

        new AlignmentHelper().addNewAlignment(connect.getPoolConnexion(), idUser, alignment.getConcept_target(),
                alignment.getThesaurus_target(), alignment.getUri_target(), alignment.getAlignement_id_type(),
                alignment.getInternal_id_concept(), idTheso, alignment.getId_source());

        //addTraductions__(idTheso, idConcept, idUser);
        TermHelper termHelper = new TermHelper();
        String idTerm = termHelper.getIdTermOfConcept(connect.getPoolConnexion(), idConcept, idTheso);
        if (StringUtils.isNotEmpty(idTerm)) {
            if (CollectionUtils.isNotEmpty(alignment.getSelectedTraductionsList())) {
                for (SelectedResource selectedResource : alignment.getSelectedTraductionsList()) {
                    Term term = new Term();
                    term.setId_thesaurus(idTheso);
                    term.setLang(selectedResource.getIdLang());
                    term.setLexical_value(selectedResource.getGettedValue());
                    term.setId_term(idTerm);
                    term.setContributor(idUser);
                    term.setCreator(idUser);
                    term.setSource("");
                    term.setStatus("");
                    if (termHelper.isTraductionExistOfConcept(connect.getPoolConnexion(), idConcept, idTheso, selectedResource.getIdLang())) {
                        termHelper.updateTermTraduction(connect.getPoolConnexion(), term, idUser);
                    } else {
                        // insert
                        termHelper.addTraduction(connect.getPoolConnexion(), selectedResource.getGettedValue(), idTerm,
                                selectedResource.getIdLang(), "", "", idTheso, idUser);
                    }
                }
            }

            //addDefinitions__(idTheso, idConcept, idUser);
            if (CollectionUtils.isNotEmpty(alignment.getSelectedDefinitionsList())) {
                NoteHelper noteHelper = new NoteHelper();
                // ajout de la note avec prefix de la source (wikidata)
                for (SelectedResource selectedResource : alignment.getSelectedDefinitionsList()) {
                    if(!noteHelper.isNoteExistOfTerm(connect.getPoolConnexion(),
                            idTerm, idTheso, selectedResource.getIdLang(), selectedResource.getGettedValue(), "definition")) {

                        noteHelper.addTermNote(connect.getPoolConnexion(), idTerm, selectedResource.getIdLang(), idTheso,
                                selectedResource.getGettedValue(), "definition", selectedAlignement, idUser);
                    }
                }
            }
        }

        //addImages__(idTheso, idConcept, idUser);
        if (CollectionUtils.isNotEmpty(alignment.getSelectedImagesList())) {
            for (SelectedResource selectedResource : alignment.getSelectedImagesList()) {
                new ExternalImagesHelper().addExternalImage(connect.getPoolConnexion(), idConcept, idTheso, "",
                        selectedAlignement, selectedResource.getGettedValue(), idUser);
            }
        }

        if (alignment.getThesaurus_target().equalsIgnoreCase("GeoNames")) {
            new GpsHelper().insertCoordonees(connect.getPoolConnexion(), idConcept, idTheso, alignment.getLat(), alignment.getLng());
        }
    }

    public void openExternalLink(String url) {
        PrimeFaces.current().executeScript("window.open('" + url + "', '_blank');");
    }

    public void openEditAlignementWindow(AlignementElement alignement) {
        /*if (setAlignmentSourceBean.getSelectedAlignments() == null
                || setAlignmentSourceBean.getSelectedAlignments().isEmpty()) {
            PrimeFaces pf = PrimeFaces.current();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Vous devez choisir au moins une source !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
        } else {
            selectConceptForAlignment(alignement.getIdConceptOrig());
            PrimeFaces.current().executeScript("PF('saerchAlignement').show();");
        }*/
        selectConceptForAlignment(alignement.getIdConceptOrig());
        PrimeFaces.current().executeScript("PF('searchAlignement').show();");
    }

    private void setExistingAlignment(String idConcept, String idTheso) {
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        existingAlignments = alignmentHelper.getAllAlignmentOfConcept(
                connect.getPoolConnexion(),
                idConcept, idTheso);
    }

    private void prepareValuesForIdRef() {
        if (isNameAlignment) { // alignement de type Autorités
            /// récupération du nom et prénom
            if (conceptValueForAlignment == null || conceptValueForAlignment.isEmpty()) {
                return;
            }
            String valuesTemp[] = conceptValueForAlignment.split(",");
            if (valuesTemp.length == 1) {
                nom = valuesTemp[0];
            }
            if (valuesTemp.length > 1) {
                nom = valuesTemp[0];
                prenom = valuesTemp[1];
            }
        }
    }

    private void resetValuesAlignement() {
        if (listAlignValues != null) {
            listAlignValues.clear();
        }
    }

    /////// fin  gestion de la pagination pour le traitement par lot ///////
    public void selectDeselectTrad() {
        if (isSelectedAllLang) {
            for (SelectedResource selectedResource : traductionsOfAlignment) {
                selectedResource.setSelected(true);
            }
            isSelectedAllLang = true;
        } else {
            for (SelectedResource selectedResource : traductionsOfAlignment) {
                selectedResource.setSelected(false);
            }
            isSelectedAllLang = false;
        }
    }

    public void selectDeselectDef() {
        if (isSelectedAllDef) {
            for (SelectedResource selectedResource : descriptionsOfAlignment) {
                selectedResource.setSelected(true);
            }
            isSelectedAllDef = true;
        } else {
            for (SelectedResource selectedResource : descriptionsOfAlignment) {
                selectedResource.setSelected(false);
            }
            isSelectedAllDef = false;
        }
    }

    public void selectDeselectImages() {
        if (isSelectedAllImages) {
            for (SelectedResource selectedResource : imagesOfAlignment) {
                selectedResource.setSelected(true);
            }
            isSelectedAllImages = true;
        } else {
            for (SelectedResource selectedResource : imagesOfAlignment) {
                selectedResource.setSelected(false);
            }
            isSelectedAllImages = false;
        }
    }

    /// au lancement du module d'alignement, on initialise les variables.
    /**
     * permet d'initialiser le tableau des concepts à aligner
     *
     * @param idTheso
     * @param idConcept
     * @param currentLang
     */
    public void initAlignementByStep(
            String idTheso,
            String idConcept,
            String currentLang) {
        // liste des NT de la branche pour l'alignement par lot
        allIdsOfBranch = new ConceptHelper().getIdsOfBranchLimited(connect.getPoolConnexion(), idConcept, idTheso, 2000);
        idConceptSelectedForAlignment = idConcept;
        idsToGet = new ArrayList<>();
        listAlignValues = null;

        counter = 0;
        initAlignmentSources(idTheso, currentLang);
        reset();
    }

    public void initAlignmentSources(String idTheso, String currentLang) {
        alignmentInProgress = false;
        viewSetting = false;
        viewAddNewSource = false;
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        alignementSources = alignmentHelper.getAlignementSource(connect.getPoolConnexion(), idTheso);

        alignmentTypes = new ArrayList<>();
        HashMap<String, String> map = new AlignmentHelper().getAlignmentType(connect.getPoolConnexion());
        alignmentTypes.addAll(map.entrySet());
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        thesaurusUsedLanguage = thesaurusHelper.getIsoLanguagesOfThesaurus(connect.getPoolConnexion(), idTheso);

        thesaurusUsedLanguageWithoutCurrentLang = thesaurusHelper.getIsoLanguagesOfThesaurus(connect.getPoolConnexion(), idTheso);
        thesaurusUsedLanguageWithoutCurrentLang.remove(currentLang);

        withLang = true;
        withNote = true;
        withImage = true;

        traductionsOfAlignment = new ArrayList<>();
        descriptionsOfAlignment = new ArrayList<>();
        imagesOfAlignment = new ArrayList<>();
        nodeAlignmentSmall = new ArrayList<>();

        isSelectedAllLang = true;
        reset();
        resetAlignmentResult();
        manualAlignmentUri = null;
    }

    private void reset() {
        traductionsOfAlignment = new ArrayList<>();
        descriptionsOfAlignment = new ArrayList<>();
        imagesOfAlignment = new ArrayList<>();
        listAlignValues = new ArrayList<>();
        nodeTermTraductions = new ArrayList<>();
        nodeAlignmentSmall = new ArrayList<>();
        nodeNotes = new ArrayList<>();
        nodeImages = new ArrayList<>();
        selectedNodeAlignment = null;
        isSelectedAllLang = true;
        isViewResult = true;
        isViewSelection = false;
        manualAlignmentUri = null;
    }

    private void resetVariables() {
        if (traductionsOfAlignment != null) {
            traductionsOfAlignment.clear();
        }
        if (descriptionsOfAlignment != null) {
            descriptionsOfAlignment.clear();
        }
        if (imagesOfAlignment != null) {
            imagesOfAlignment.clear();
        }
        if (nodeAlignmentSmall != null) {
            nodeAlignmentSmall.clear();
        }
        isSelectedAllLang = true;
        nom = "";
        prenom = "";
        manualAlignmentUri = null;
    }

    private void resetAlignmentResult() {
        alignementResult = null;
        error = false;
        listAlignValues = null;
        manualAlignmentUri = null;
    }

    /// récupération des infos sur le concept local qui est en cours d'alignement
    /**
     * permet de récupérer les traductions d'un concept en local
     *
     * @param idConcept
     * @param idTheso
     */
    private void getTraductionsOfConcept(String idTheso, String idConcept) {
        nodeTermTraductions = new TermHelper().getAllTraductionsOfConcept(connect.getPoolConnexion(), idConcept, idTheso);
    }

    /**
     * permet de récupérer les définitions existantes pour permettre de
     * structurer l'objet pour comparer les définitions locales et distantes
     *
     * @param idConcept
     * @param idTheso
     */
    private void getDefinitionsOfConcept(String idTheso, String idConcept) {

        String idTerm = new TermHelper().getIdTermOfConcept(connect.getPoolConnexion(), idConcept, idTheso);

        nodeNotes = new NoteHelper().getListNotesTermAllLang(connect.getPoolConnexion(),
                idTerm, idTheso);
    }

    /**
     * permet de récuprer les images du concepts (URI des images) pour pouvoir
     * structurer l'objet pour comparer les images locales et distantes
     */
    private void getExternalImagesOfConcept(String idTheso, String idConcept) {
        nodeImages = new ExternalImagesHelper().getExternalImages(connect.getPoolConnexion(), idConcept, idTheso);
    }

    /**
     * permet de récuprer les alignements du concept pour permettre de vérifier les alignements existants
     */
    private void getAlignmentOfConcept(String idTheso, String idConcept) {
        nodeAlignmentSmall = new AlignmentHelper().getAllAlignmentOfConceptNew(connect.getPoolConnexion(), idConcept, idTheso);
    }

    /**
     * lance la recherche des alignements pour le concept sélectionné avec la source sélectionnée
     */
    public void searchAlignments(String idTheso, String idConcept, String lexicalValue, String idLang) {

        reset();
        for (AlignementSource alignementSource : alignementSources) {
            if (alignementSource.getSource().equalsIgnoreCase(selectedAlignement)) {
                selectedAlignementSource = alignementSource;
                break;
            }
        }

        if (!ObjectUtils.isEmpty(selectedAlignementSource)) {
            // si l'alignement est de type Wikidata, on récupère la liste des concepts pour préparer le choix de l'utilisateur
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("wikidata_sparql")) {
                getAlignmentWikidata_sparql(selectedAlignementSource, idTheso, idConcept, lexicalValue, idLang);
            }
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("wikidata_rest")) {
                getAlignmentWikidata_rest(selectedAlignementSource, idTheso, idConcept, lexicalValue, idLang);
            }

            // ici IdRef pour les sujets
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("idRefSujets")) {
                getAlignmentIdRefSubject(selectedAlignementSource, idTheso, idConcept, lexicalValue);
            }

            // ici IdRef pour les noms de personnes
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("idRefPersonnes")) {
                getAlignmentIdRefPerson(selectedAlignementSource, idTheso, idConcept, lexicalValue, idLang);
            }

            // ici IdRef pour les auteurs
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("idRefAuteurs")) {
                getAlignmentIdRefNames(selectedAlignementSource, idTheso, idConcept);
            }

            // ici IdRef pour les Lieux
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("idRefLieux")) {
                getAlignmentIdRefLieux(selectedAlignementSource, idTheso, idConcept, lexicalValue);
            }

            // ici IdRef pour les Titres Uniformes
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("IdRefTitreUniforme")) {
                getAlignmentIdRefUniformtitle(selectedAlignementSource, idTheso, idConcept, lexicalValue);
            }

            // ici AAT du Getty
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("Getty_AAT")) {
                getAlignmentGettyAAT(selectedAlignementSource, idTheso, idConcept, lexicalValue, idLang);
            }

            // ici pour un alignement de type Opentheso
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("Opentheso")) {
                getAlignmentOpentheso(selectedAlignementSource, idTheso, idConcept, lexicalValue, idLang);
            }

            // ici pour un alignement de type Gemet
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("Gemet")) {
                getAlignmentGemet(selectedAlignementSource, idTheso, idConcept, lexicalValue, idLang);
            }

            // ici pour un alignement de type Agrovoc
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("Agrovoc")) {
                getAlignmentAgrovoc(selectedAlignementSource, idTheso, idConcept, lexicalValue, idLang);
            }

            // ici pour un alignement de type GeoNames
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("GeoNames")) {
                getAlignmentGeoNames(selectedAlignementSource, idTheso, idConcept, lexicalValue, idLang);
            }
            // ici pour un alignement de type Ontome
            if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("Ontome")) {
                getAlignmentOntome(selectedAlignementSource, idTheso, idConcept, lexicalValue);
            }
        }

        if (listAlignValues != null) {
            if (listAlignValues.isEmpty()) {
                alignmentManualBean.reset();
                alignmentManualBean.setManualAlignmentSource(selectedAlignementSource.getSource());
            }
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     *
     * @param alignementSource
     * @param idTheso
     * @param idConcept
     * @param lexicalValue
     * @param idLang
     */
    private void getAlignmentWikidata_rest(
            AlignementSource alignementSource,
            String idTheso,
            String idConcept,
            String lexicalValue,
            String idLang) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        WikidataHelper wikidataHelper = new WikidataHelper();

        // action JSON (HashMap (Wikidata)
        //ici il faut appeler le filtre de Wikidata
        listAlignValues = wikidataHelper.queryWikidata_rest(idConcept, idTheso, lexicalValue.trim(),
                idLang, alignementSource.getRequete(),
                alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), wikidataHelper.getMessages().toString()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     *
     * @param alignementSource
     * @param idTheso
     * @param idConcept
     * @param lexicalValue
     * @param idLang
     */
    private void getAlignmentWikidata_sparql(
            AlignementSource alignementSource,
            String idTheso,
            String idConcept,
            String lexicalValue,
            String idLang) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        WikidataHelper wikidataHelper = new WikidataHelper();

        // action JSON (HashMap (Wikidata)
        //ici il faut appeler le filtre de Wikidata
        listAlignValues = wikidataHelper.queryWikidata_sparql(idConcept, idTheso, lexicalValue.trim(),
                idLang, alignementSource.getRequete(),
                alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), wikidataHelper.getMessages().toString()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     *
     * @param alignementSource
     * @param idTheso
     * @param idConcept
     * @param lexicalValue
     */
    private void getAlignmentIdRefSubject(AlignementSource alignementSource, String idTheso, String idConcept,
            String lexicalValue) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }

        IdRefHelper idRefHelper = new IdRefHelper();

        // action JSON (HashMap (Wikidata)
        //ici il faut appeler le filtre de Wikidata
        listAlignValues = idRefHelper.queryIdRefSubject(idConcept, idTheso, lexicalValue.trim(),
                alignementSource.getRequete(), alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), idRefHelper.getMessages()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     *
     * @param alignementSource
     * @param idTheso
     * @param idConcept
     * @param lexicalValue
     * @param idLang
     */
    private void getAlignmentIdRefPerson(
            AlignementSource alignementSource,
            String idTheso,
            String idConcept,
            String lexicalValue,
            String idLang
    ) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        IdRefHelper idRefHelper = new IdRefHelper();

        // action JSON (HashMap (Wikidata)
        //ici il faut appeler le filtre de Wikidata
        listAlignValues = idRefHelper.queryIdRefPerson(idConcept, idTheso, lexicalValue.trim(),
                alignementSource.getRequete(), alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), idRefHelper.getMessages()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     *
     */
    private void getAlignmentIdRefNames(AlignementSource alignementSource, String idTheso, String idConcept) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        IdRefHelper idRefHelper = new IdRefHelper();

        // action JSON (HashMap (Wikidata)
        //ici il faut appeler le filtre de Wikidata
        listAlignValues = idRefHelper.queryIdRefNames(idConcept, idTheso, nom, prenom, alignementSource.getRequete(),
                alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), idRefHelper.getMessages()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     */
    private void getAlignmentIdRefUniformtitle(AlignementSource alignementSource, String idTheso, String idConcept,
            String lexicalValue) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Source :", "Pas de source sélectionnée"));
            return;
        }
        IdRefHelper idRefHelper = new IdRefHelper();
        listAlignValues = idRefHelper.queryIdRefUniformtitle(idConcept, idTheso, lexicalValue.trim(),
                alignementSource.getRequete(), alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), idRefHelper.getMessages()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     */
    private void getAlignmentIdRefLieux(AlignementSource alignementSource, String idTheso, String idConcept, String lexicalValue) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        IdRefHelper idRefHelper = new IdRefHelper();
        listAlignValues = idRefHelper.queryIdRefLieux(idConcept, idTheso, lexicalValue.trim(),
                alignementSource.getRequete(), alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), idRefHelper.getMessages()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     */
    private void getAlignmentGettyAAT(AlignementSource alignementSource, String idTheso, String idConcept,
                                      String lexicalValue, String idLang) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        GettyAATHelper gettyAATHelper = new GettyAATHelper();

        // action XML
        //ici il faut appeler le filtre du Getty AAT
        listAlignValues = gettyAATHelper.queryAAT(idConcept, idTheso, lexicalValue.trim(),
                idLang, alignementSource.getRequete(),
                alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), gettyAATHelper.getMessages()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     *
     * @param alignementSource
     * @param idTheso
     * @param idConcept
     * @param lexicalValue
     * @param idLang
     */
    private void getAlignmentGemet(
            AlignementSource alignementSource,
            String idTheso,
            String idConcept,
            String lexicalValue,
            String idLang
    ) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        GemetHelper gemetHelper = new GemetHelper();

        // action XML
        //ici il faut appeler le filtre du Getty AAT
        listAlignValues = gemetHelper.queryGemet(idConcept, idTheso, lexicalValue.trim(),
                idLang, alignementSource.getRequete(),
                alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), gemetHelper.getMessages().toString()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     *
     * @param alignementSource
     * @param idTheso
     * @param idConcept
     * @param lexicalValue
     * @param idLang
     */
    private void getAlignmentAgrovoc(
            AlignementSource alignementSource,
            String idTheso,
            String idConcept,
            String lexicalValue,
            String idLang
    ) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        AgrovocHelper agrovocHelper = new AgrovocHelper();

        // action REST Json
        //ici il faut appeler le filtre du Agrovoc
        listAlignValues = agrovocHelper.queryAgrovoc(idConcept, idTheso, lexicalValue.trim(),
                idLang, alignementSource.getRequete(),
                alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), agrovocHelper.getMessages()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     *
     * @param alignementSource
     * @param idTheso
     * @param idConcept
     * @param lexicalValue
     * @param idLang
     */
    private void getAlignmentGeoNames(
            AlignementSource alignementSource,
            String idTheso,
            String idConcept,
            String lexicalValue,
            String idLang) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        GeoNamesHelper geoNamesHelper = new GeoNamesHelper();

        // action XML
        //ici il faut appeler le filtre du Getty AAT
        listAlignValues = geoNamesHelper.queryGeoNames(idConcept, idTheso, lexicalValue.trim(),
                idLang, alignementSource.getRequete(),
                alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), geoNamesHelper.getMessages()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     *
     * @param alignementSource
     * @param idTheso
     * @param idConcept
     * @param lexicalValue
     */
    private void getAlignmentOntome(
            AlignementSource alignementSource,
            String idTheso,
            String idConcept,
            String lexicalValue) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        OntomeHelper ontomeHelper = new OntomeHelper();

        // action JSON (HashMap (Wikidata)
        //ici il faut appeler le filtre de Wikidata
        listAlignValues = ontomeHelper.queryOntomeHelper(idConcept, idTheso, lexicalValue.trim(),
                alignementSource.getRequete(),
                alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), ontomeHelper.getMessages()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     *
     * @param alignementSource
     * @param idTheso
     * @param idConcept
     * @param lexicalValue
     * @param idLang
     */
    private void getAlignmentOpentheso(
            AlignementSource alignementSource,
            String idTheso,
            String idConcept,
            String lexicalValue,
            String idLang
    ) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        OpenthesoHelper openthesoHelper = new OpenthesoHelper();

        // action XML
        //ici il faut appeler le filtre du Getty AAT
        listAlignValues = openthesoHelper.queryOpentheso(idConcept, idTheso, lexicalValue.trim(),
                idLang, alignementSource.getRequete(),
                alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), openthesoHelper.getMessages()));
        }
    }

    /**
     * initialisation des valeurs du concept local pour comparaison avec le
     * concept à aligner
     *
     * @param idTheso
     * @param idConcept
     */
    private void getValuesOfLocalConcept(String idTheso, String idConcept) {
        getTraductionsOfConcept(idTheso, idConcept);
        getDefinitionsOfConcept(idTheso, idConcept);
        getExternalImagesOfConcept(idTheso, idConcept);
        getAlignmentOfConcept(idTheso, idConcept);
    }

    /**
     * L'utilisateur a cliqué sur un concept à aligner, ici on récupère les
     * détails du concept de la source et les détails des options (images,
     * définitions, traductions en plus de l'URL d'alignement récupération des
     * options
     *
     * @param selectedNodeAlignment
     * @param idTheso
     */
    public void getUriAndOptions(NodeAlignment selectedNodeAlignment,
            String idTheso) {
        alignmentInProgress = true;

        if (idConceptSelectedForAlignment == null) {
            return;
        }
        isViewResult = false;
        isViewSelection = true;

        resetAlignmentResult();
        List<String> selectedOptions = new ArrayList<>();
        if (withLang) {
            selectedOptions.add("langues");
        }
        if (withNote) {
            selectedOptions.add("notes");
        }
        if (withImage) {
            selectedOptions.add("images");
        }

        this.selectedNodeAlignment = selectedNodeAlignment;
        // initialisation des valeurs du concept local pour comparaison avec le concept à aligner
        getValuesOfLocalConcept(idTheso, idConceptSelectedForAlignment);

        /**
         * ici on filtre les données par rapport à la source d'alignement on
         * prépare les objets pour recevoir les informations suivant les options
         * sélectionnées : traductions, notes, images
         */

        // si l'alignement est de type Wikidata
        if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("wikidata_sparql") || selectedAlignementSource.getSource_filter().equalsIgnoreCase("wikidata_rest")) {
            WikidataHelper wikidataHelper = new WikidataHelper();
            resetVariables();

            wikidataHelper.setOptionsFromWikidata(selectedNodeAlignment,
                    selectedOptions,
                    thesaurusUsedLanguageWithoutCurrentLang,
                    thesaurusUsedLanguage);
            setObjectTraductions(wikidataHelper.getResourceWikidataTraductions());
            setObjectDefinitions(wikidataHelper.getResourceWikidataDefinitions());
            setObjectImages(wikidataHelper.getResourceWikidataImages());
        }

        // si l'alignement est de type IdRef
        // si l'alignement est de type Getty_AAT
        // si l'alignement est de type Opentheso
        // si l'alignement est de type Gemet
        if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("gemet")) {
            GemetHelper gemetHelper = new GemetHelper();
            resetVariables();

            gemetHelper.setOptions(selectedNodeAlignment,
                    selectedOptions,
                    thesaurusUsedLanguageWithoutCurrentLang,
                    thesaurusUsedLanguage);
            setObjectTraductions(gemetHelper.getResourceTraductions());
            setObjectDefinitions(gemetHelper.getResourceDefinitions());
            setObjectImages(gemetHelper.getResourceImages());
        }

        // si l'alignement est de type Agrovoc
        if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("agrovoc")) {
            AgrovocHelper agrovocHelper = new AgrovocHelper();
            resetVariables();

            agrovocHelper.setOptions(selectedNodeAlignment, selectedOptions, thesaurusUsedLanguageWithoutCurrentLang,
                    selectedTheso.getCurrentLang());
            setObjectTraductions(agrovocHelper.getResourceTraductions());
            setObjectDefinitions(agrovocHelper.getResourceDefinitions());
            setObjectImages(agrovocHelper.getResourceImages());
        }

        // si l'alignement est de type GeoNames
        if (selectedAlignementSource.getSource_filter().equalsIgnoreCase("GeoNames")) {
            GeoNamesHelper geoNamesHelper = new GeoNamesHelper();
            resetVariables();

            geoNamesHelper.setOptions(selectedNodeAlignment, selectedOptions, thesaurusUsedLanguageWithoutCurrentLang);
            setObjectTraductions(geoNamesHelper.getResourceTraductions());
            setObjectDefinitions(geoNamesHelper.getResourceDefinitions());
            setObjectImages(geoNamesHelper.getResourceImages());
        }

    }

    /**
     * permet de charger dans l'objet 'traductionsOfAlignment' toutes les
     * traductions qui n'existent pas en local si la traduction en local est
     * identique à celle récupérée, on l'ignore si la traduction en local est
     * différente, on l'ajoute à l'objet pour correction
     */
    private void setObjectTraductions(List<SelectedResource> traductionsoOfAlignmentTemp) {
        boolean added;

        if (traductionsoOfAlignmentTemp == null) {
            return;
        }
        // la liste des traductions de Wikidata
        for (SelectedResource selectedResource : traductionsoOfAlignmentTemp) {
            added = false;
            // la liste des traductions existantes
            for (NodeTermTraduction nodeTermTraduction : nodeTermTraductions) {
                // cas où la langue récupérée existe déjà en local
                if (selectedResource.getIdLang().equalsIgnoreCase(nodeTermTraduction.getLang())) {
                    // on compare le texte si équivalent, on l'ignore
                    if (!selectedResource.getGettedValue().trim().equalsIgnoreCase(nodeTermTraduction.getLexicalValue().trim())) {
                        selectedResource.setLocalValue(nodeTermTraduction.getLexicalValue());
                        traductionsOfAlignment.add(selectedResource);
                        added = true;
                        break;
                    } else {
                        added = true;
                        break;
                    }
                }
            }
            // si on a déjà ajouté la traduction, on l'ignore, sinon, on l'ajoute
            if (!added) {
                traductionsOfAlignment.add(selectedResource);
            }
        }
    }

    /**
     * permet de charger dans l'objet 'descriptionsWikidata' toutes les
     * définitions qui n'existent pas en local si la définition en local est
     * identique à celle récupérée, on l'ignore si la définition en local est
     * différente, on l'ajoute à l'objet pour correction
     */
    private void setObjectDefinitions(List<SelectedResource> descriptionsOfAlignmentTemp) {
        boolean added;

        if (descriptionsOfAlignmentTemp == null) {
            return;
        }
        // la liste des traductions de Wikidata
        for (SelectedResource selectedResource : descriptionsOfAlignmentTemp) {
            added = false;
            // la liste des traductions existantes
            for (NodeNote nodeNote : nodeNotes) {
                // on compare le texte si équivalent, on l'ignore
                if (!selectedResource.getGettedValue().trim().equalsIgnoreCase(nodeNote.getLexicalvalue().trim())) {
                    selectedResource.setLocalValue(nodeNote.getLexicalvalue());
                    descriptionsOfAlignment.add(selectedResource);
                    added = true;
                    break;
                } else {
                    added = true;
                    break;
                }
            }
            // si on a déjà ajouté la traduction, on l'ignore, sinon, on l'ajoute
            if (!added) {
                descriptionsOfAlignment.add(selectedResource);
            }
        }
    }

    /**
     * permet de charger dans l'objet 'imagesWikidata' toutes les images qui
     * n'existent pas en local si l'image en local est identique à celle
     * récupérée, on l'ignore si l'image en local est différente, on l'ajoute à
     * l'objet pour correction
     */
    private void setObjectImages(List<SelectedResource> imagesOfAlignmentTemp) {
        boolean added;

        if (imagesOfAlignmentTemp == null) {
            return;
        }
        // la liste des traductions de Wikidata
        for (SelectedResource selectedResource : imagesOfAlignmentTemp) {
            added = false;
            // la liste des traductions existantes
            for (NodeImage nodeImage : nodeImages) {
                // on compare l'URI est équivalente, on l'ignore
                if (!selectedResource.getGettedValue().trim().equalsIgnoreCase(nodeImage.getUri().trim())) {
                    selectedResource.setLocalValue(nodeImage.getUri());
                    imagesOfAlignment.add(selectedResource);
                    added = true;
                    break;
                } else {
                    added = true;
                    break;
                }
            }
            // si on a déjà ajouté la traduction, on l'ignore, sinon, on l'ajoute
            if (!added) {
                imagesOfAlignment.add(selectedResource);
            }
        }
    }

    /**
     * permet d'ajouter l'alignement et les options choisis (traductions,
     * définitions et images) la focntion gère les erreurs en cas de problème
     *
     * @param idTheso
     * @param idConcept
     * @param idUser
     */
    public void addAlignment(String idTheso, String idConcept, int idUser) {
        if (selectedNodeAlignment == null) {
            return;
        }

        // ajout de l'alignement séléctionné
        if (!addAlignment__(idTheso, idConcept, idUser)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'ajout de l'alignement a achoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        // ajout des traductions
        if (!addTraductions__(idTheso, idConcept, idUser)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'ajout des tradutcions a achoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        // ajout des définitions
        if (!addDefinitions__(idTheso, idConcept, idUser)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'ajout des notes a achoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        // ajout des images
        if (!addImages__(idTheso, idConcept, idUser)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'ajout des images a achoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        // ajout des coordonnées GPS
        if (selectedNodeAlignment.getThesaurus_target().equalsIgnoreCase("GeoNames")) {
            if (!addGps__(idTheso, idConcept)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'ajout des coordonnées GPS a achoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }

        alignementResult = alignementResult + new AlignmentHelper().getMessage();
        selectedNodeAlignment = null;
        alignmentInProgress = false;

        updateDateOfConcept(idTheso, idConcept, idUser);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        isViewResult = true;
        isViewSelection = false;
        setExistingAlignment(idConcept, idTheso);

        getIdsAndValues2(conceptView.getSelectedLang(), selectedTheso.getCurrentIdTheso());

        resetVariables();
    }

    /**
     * permet d'ajouter l'alignement et les options choisis (traductions,
     * définitions et images) la focntion gère les erreurs en cas de problème
     *
     * @param idTheso
     * @param idConcept
     * @param idUser
     */
    public void addManualAlignement(String idTheso, String idConcept, int idUser) {
        FacesMessage msg;
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        // ajout de l'alignement séléctionné
        if (!alignmentHelper.addNewAlignment(connect.getPoolConnexion(),
                idUser,
                "",
                selectedAlignement,
                manualAlignmentUri,
                selectedAlignementType,
                idConcept, idTheso,
                -1)) {
            alignementResult = "Erreur pendant l'ajout de l'alignement: " + alignmentHelper.getMessage();
            alignmentInProgress = false;
            selectedNodeAlignment = null;
            resetVariables();
            error = true;
            return;
        }

        alignementResult = alignementResult + alignmentHelper.getMessage();
        selectedNodeAlignment = null;
        alignmentInProgress = false;
        //conceptView.getConcept(idTheso, idConcept, conceptView.getSelectedLang());

        updateDateOfConcept(idTheso, idConcept, idUser);

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        isViewResult = true;
        isViewSelection = false;
        setExistingAlignment(idConcept, idTheso);

        getIdsAndValues2(conceptView.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        //getIdsAndValues(selectedTheso.getCurrentLang(), idTheso);

        resetVariables();
    }

    /**
     *
     * @param idTheso
     * @param idConcept
     * @param idUser
     */
    private void updateDateOfConcept(String idTheso, String idConcept, int idUser) {
        ConceptHelper conceptHelper = new ConceptHelper();

        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                idTheso,
                idConcept, idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null),
                idConcept, idTheso);
        ///////////////        
    }

    /**
     * Permet d'ajouter les coordonnées GPS pour les lieux
     *
     * @param idTheso
     * @param idConcept
     * @return
     */
    private boolean addGps__(String idTheso, String idConcept) {

        // ajout de l'alignement séléctionné
        if (!new GpsHelper().insertCoordonees(
                connect.getPoolConnexion(),
                idConcept,
                idTheso,
                selectedNodeAlignment.getLat(),
                selectedNodeAlignment.getLng())) {

            alignementResult = "Erreur pendant l'ajout des coordonnées GPS : ";
            alignmentInProgress = false;
            selectedNodeAlignment = null;
            resetVariables();
            return false;
        }
        alignementResult = "Alignement ajouté ##";
        return true;
    }

    /**
     * Permet d'ajouter l'alignement choisi dans la base de données
     *
     * @param idTheso
     * @param idConcept
     * @param idUser
     * @return
     */
    private boolean addAlignment__(String idTheso, String idConcept, int idUser) {
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        // ajout de l'alignement séléctionné
        if (!alignmentHelper.addNewAlignment(connect.getPoolConnexion(),
                idUser,
                selectedNodeAlignment.getConcept_target(),
                selectedNodeAlignment.getThesaurus_target(),
                selectedNodeAlignment.getUri_target(),
                selectedAlignementType,
                idConcept, idTheso, selectedAlignementSource.getId())) {
            alignementResult = "Erreur pendant l'ajout de l'alignement: "
                    + alignmentHelper.getMessage();
            alignmentInProgress = false;
            selectedNodeAlignment = null;
            resetVariables();
            error = true;
            return false;
        }
        alignementResult = "Alignement ajouté ##";
        return true;
    }

    private boolean addTraductions__(String idTheso, String idConcept, int idUser) {
        TermHelper termHelper = new TermHelper();
        Term term = new Term();
        String idTerm = termHelper.getIdTermOfConcept(connect.getPoolConnexion(), idConcept, idTheso);
        if (idTerm == null) {
            return false;
        }

        for (SelectedResource selectedResource : traductionsOfAlignment) {
            if (selectedResource.isSelected()) {
                term.setId_thesaurus(idTheso);
                term.setLang(selectedResource.getIdLang());
                term.setLexical_value(selectedResource.getGettedValue());
                term.setId_term(idTerm);
                term.setContributor(idUser);
                term.setCreator(idUser);
                term.setSource("");
                term.setStatus("");
                if (termHelper.isTraductionExistOfConcept(connect.getPoolConnexion(),
                        idConcept, idTheso, selectedResource.getIdLang())) {
                    // update
                    if (!termHelper.updateTermTraduction(connect.getPoolConnexion(), term, idUser)) {
                        error = true;
                        alignementResult = alignementResult + ": Erreur pendant la modification des traductions";
                    }
                } else {
                    // insert
                    if (!termHelper.addTraduction(connect.getPoolConnexion(),
                            selectedResource.getGettedValue(),
                            idTerm,
                            selectedResource.getIdLang(),
                            "",
                            "",
                            idTheso,
                            idUser)) {
                        error = true;
                        alignementResult = alignementResult + ": Erreur dans l'ajout des traductions";
                    }
                }
            }
        }
        alignementResult = alignementResult + " Traductions ajoutées ##";
        return true;
    }

    private boolean addDefinitions__(String idTheso, String idConcept, int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        TermHelper termHelper = new TermHelper();
        String idTerm = termHelper.getIdTermOfConcept(connect.getPoolConnexion(), idConcept, idTheso);
        if (idTerm == null) {
            return false;
        }


        // ajout de la note avec prefix de la source (wikidata)
        for (SelectedResource selectedResource : descriptionsOfAlignment) {
            if (selectedResource.isSelected()) {
                if(noteHelper.isNoteExistOfTerm(connect.getPoolConnexion(),
                        idTerm, idTheso,
                        selectedResource.getIdLang(), selectedResource.getGettedValue(), "definition")) {
                    continue;
                }

                if (!noteHelper.addTermNote(connect.getPoolConnexion(),
                        idTerm, selectedResource.getIdLang(),
                        idTheso,
                        selectedResource.getGettedValue(),
                        "definition", selectedAlignement,
                        idUser)) {
                    error = true;
                    alignementResult = alignementResult + ": Erreur dans l'ajout des définitions";
                }
            }
        }
        alignementResult = alignementResult + " Définitions ajoutées ##";
        return true;
    }

    private boolean addImages__(String idTheso, String idConcept, int idUser) {
        ExternalImagesHelper imagesHelper = new ExternalImagesHelper();
        for (SelectedResource selectedResource : imagesOfAlignment) {
            if (selectedResource.isSelected()) {
                if (!imagesHelper.addExternalImage(connect.getPoolConnexion(),
                        idConcept, idTheso,
                        "",
                        selectedAlignement,
                        selectedResource.getGettedValue(),
                        idUser)) {
                    error = true;
                    alignementResult = alignementResult + ": Erreur dans l'ajout des images";
                }
            }
        }
        alignementResult = alignementResult + " Images ajoutées";
        return true;
    }

    public void cancelAlignment() {
        isViewResult = true;
        isViewSelection = false;

        selectedNodeAlignment = null;
        alignmentInProgress = false;
        resetVariables();
    }

    public void cancelManualAlignment() {
        isViewResult = false;
        isViewSelection = false;

        selectedNodeAlignment = null;
        alignmentInProgress = false;
        listAlignValues = null;
        resetVariables();
    }
    
    public long getTotalCount(String internalIdConcept) {
        return allAlignementFound.stream().filter(alignement -> internalIdConcept.equals(alignement.getInternal_id_concept())).count();
    }

    public long getTotalAlignementParConcept(String idConceptOrig) {
        return allignementsList.stream().filter(alignement -> idConceptOrig.equals(alignement.getIdConceptOrig())).count() - 1;
    }

    public long getTotalAlignements() {
        return allignementsList.size() - new HashSet<>(allignementsList).size();
    }

    public void validManualAlignment(String idTheso, String idConcept, int idUser) {
        isViewResult = false;
        isViewSelection = false;
        setExistingAlignment(
                idConceptSelectedForAlignment,
                selectedTheso.getCurrentIdTheso());
        selectedNodeAlignment = null;
        alignmentInProgress = false;
        listAlignValues = null;

        updateDateOfConcept(idTheso, idConcept, idUser);
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        isViewResult = true;
        isViewSelection = false;
        setExistingAlignment(idConcept, idTheso);
        getIdsAndValues2(conceptView.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        resetVariables();
    }

    public ArrayList<Map.Entry<String, String>> getAlignmentTypes() {
        return alignmentTypes;
    }

    public void setAlignmentTypes(ArrayList<Map.Entry<String, String>> alignmentTypes) {
        this.alignmentTypes = alignmentTypes;
    }

    public ArrayList<AlignementSource> getAlignementSources() {
        return alignementSources;
    }

    public void setAlignementSources(ArrayList<AlignementSource> alignementSources) {
        this.alignementSources = alignementSources;
    }

    public boolean isWithLang() {
        return withLang;
    }

    public void setWithLang(boolean withLang) {
        this.withLang = withLang;
    }

    public boolean isWithNote() {
        return withNote;
    }

    public void setWithNote(boolean withNote) {
        this.withNote = withNote;
    }

    public boolean isWithImage() {
        return withImage;
    }

    public void setWithImage(boolean withImage) {
        this.withImage = withImage;
    }

    public String getSelectedAlignement() {
        return selectedAlignement;
    }

    public void actionChoix() {
        if (selectedAlignement == null) {
            return;
        }
        resetAlignmentResult();
        if (selectedAlignement.equalsIgnoreCase("idRefAuteurs")) {
            isNameAlignment = true;
            prepareValuesForIdRef();
            //  setIsNameAlignment(true);

        } else {
            isNameAlignment = false;
        }
        if (selectedAlignement.equalsIgnoreCase("wikidata")) {
            alertWikidata = "!!! Attention à la casse !!!!";
            PrimeFaces pf = PrimeFaces.current();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "!!! Attention respectez la casse !!!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            alertWikidata = "";
        }
    }

    public void setSelectedAlignement(String selectedAlignement) {
        this.selectedAlignement = selectedAlignement;
    }

    public AlignementSource getSelectedAlignementSource() {
        return selectedAlignementSource;
    }

    public void setSelectedAlignementSource(AlignementSource selectedAlignementSource) {
        this.selectedAlignementSource = selectedAlignementSource;
    }

    public List<NodeAlignment> getListAlignValues() {
        return listAlignValues;
    }

    public void setListAlignValues(ArrayList<NodeAlignment> listAlignValues) {
        this.listAlignValues = listAlignValues;
    }

    public int getSelectedAlignementType() {
        return selectedAlignementType;
    }

    public void setSelectedAlignementType(int selectedAlignementType) {
        this.selectedAlignementType = selectedAlignementType;
    }

    public ArrayList<SelectedResource> getTraductionsOfAlignment() {
        return traductionsOfAlignment;
    }

    public void setTraductionsOfAlignment(ArrayList<SelectedResource> traductionsOfAlignment) {
        this.traductionsOfAlignment = traductionsOfAlignment;
    }

    public ArrayList<SelectedResource> getDescriptionsOfAlignment() {
        return descriptionsOfAlignment;
    }

    public void setDescriptionsOfAlignment(ArrayList<SelectedResource> descriptionsOfAlignment) {
        this.descriptionsOfAlignment = descriptionsOfAlignment;
    }

    public ArrayList<NodeAlignmentSmall> getNodeAlignmentSmall() {
        return nodeAlignmentSmall;
    }

    public void setNodeAlignmentSmall(ArrayList<NodeAlignmentSmall> nodeAlignmentSmall) {
        this.nodeAlignmentSmall = nodeAlignmentSmall;
    }

    public boolean isIsSelectedAllLang() {
        return isSelectedAllLang;
    }

    public void setIsSelectedAllLang(boolean isSelectedAllLang) {
        this.isSelectedAllLang = isSelectedAllLang;
    }

    public boolean isIsSelectedAllDef() {
        return isSelectedAllDef;
    }

    public void setIsSelectedAllDef(boolean isSelectedAllDef) {
        this.isSelectedAllDef = isSelectedAllDef;
    }

    public ArrayList<SelectedResource> getImagesOfAlignment() {
        return imagesOfAlignment;
    }

    public void setImagesOfAlignment(ArrayList<SelectedResource> imagesOfAlignment) {
        this.imagesOfAlignment = imagesOfAlignment;
    }

    public boolean isIsSelectedAllImages() {
        return isSelectedAllImages;
    }

    public void setIsSelectedAllImages(boolean isSelectedAllImages) {
        this.isSelectedAllImages = isSelectedAllImages;
    }

    public NodeAlignment getSelectedNodeAlignment() {
        return selectedNodeAlignment;
    }

    public void setSelectedNodeAlignment(NodeAlignment selectedNodeAlignment) {
        this.selectedNodeAlignment = selectedNodeAlignment;
    }

    public boolean isAlignmentInProgress() {
        return alignmentInProgress;
    }

    public void setAlignmentInProgress(boolean alignmentInProgress) {
        this.alignmentInProgress = alignmentInProgress;
    }

    public String getAlignementResult() {
        return alignementResult;
    }

    public void setAlignementResult(String alignementResult) {
        this.alignementResult = alignementResult;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public ArrayList<NodeIdValue> getIdsAndValues() {
        return idsAndValues;
    }

    public void setIdsAndValues(ArrayList<NodeIdValue> idsAndValues) {
        this.idsAndValues = idsAndValues;
    }

    public String getIdConceptSelectedForAlignment() {
        return idConceptSelectedForAlignment;
    }

    public void setIdConceptSelectedForAlignment(String idConceptSelectedForAlignment) {
        this.idConceptSelectedForAlignment = idConceptSelectedForAlignment;
    }

    public String getConceptValueForAlignment() {
        return conceptValueForAlignment;
    }

    public void setConceptValueForAlignment(String conceptValueForAlignment) {
        this.conceptValueForAlignment = conceptValueForAlignment;
    }

    public boolean isIsNameAlignment() {
        return isNameAlignment;
    }

    public void setIsNameAlignment(boolean isNameAlignment) {
        this.isNameAlignment = isNameAlignment;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public ArrayList<NodeAlignment> getExistingAlignments() {
        return existingAlignments;
    }

    public void setExistingAlignments(ArrayList<NodeAlignment> existingAlignments) {
        this.existingAlignments = existingAlignments;
    }

    public boolean isIsViewResult() {
        return isViewResult;
    }

    public void setIsViewResult(boolean isViewResult) {
        this.isViewResult = isViewResult;
    }

    public boolean isIsViewSelection() {
        return isViewSelection;
    }

    public void setIsViewSelection(boolean isViewSelection) {
        this.isViewSelection = isViewSelection;
    }

    public boolean isViewSetting() {
        return viewSetting;
    }

    public void setViewSetting(boolean viewSetting) {
        this.viewSetting = viewSetting;
    }

    public boolean isViewAddNewSource() {
        return viewAddNewSource;
    }

    public void setViewAddNewSource(boolean viewAddNewSource) {
        this.viewAddNewSource = viewAddNewSource;
    }

    public String getAlertWikidata() {
        return alertWikidata;
    }

    public void setAlertWikidata(String alertWikidata) {
        this.alertWikidata = alertWikidata;
    }

    public ArrayList<AlignementElement> getFilteredAlignement() {
        return filteredAlignement;
    }

    public void setFilteredAlignement(ArrayList<AlignementElement> filteredAlignement) {
        this.filteredAlignement = filteredAlignement;
    }

    public AlignementElement getSelectedLastPositionReportDtos() {
        return selectedLastPositionReportDtos;
    }

    public void setSelectedLastPositionReportDtos(AlignementElement selectedLastPositionReportDtos) {
        this.selectedLastPositionReportDtos = selectedLastPositionReportDtos;
    }

    public String getManualAlignmentUri() {
        return manualAlignmentUri;
    }

    public void setManualAlignmentUri(String manualAlignmentUri) {
        this.manualAlignmentUri = manualAlignmentUri;
    }

    public Connect getConnect() {
        return connect;
    }

    public void setConnect(Connect connect) {
        this.connect = connect;
    }

    public ConceptView getConceptView() {
        return conceptView;
    }

    public void setConceptView(ConceptView conceptView) {
        this.conceptView = conceptView;
    }

    public SelectedTheso getSelectedTheso() {
        return selectedTheso;
    }

    public void setSelectedTheso(SelectedTheso selectedTheso) {
        this.selectedTheso = selectedTheso;
    }

    public ConceptView getConceptBean() {
        return conceptBean;
    }

    public void setConceptBean(ConceptView conceptBean) {
        this.conceptBean = conceptBean;
    }

    public AlignmentManualBean getAlignmentManualBean() {
        return alignmentManualBean;
    }

    public void setAlignmentManualBean(AlignmentManualBean alignmentManualBean) {
        this.alignmentManualBean = alignmentManualBean;
    }

    public LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    public boolean isViewResult() {
        return isViewResult;
    }

    public void setViewResult(boolean viewResult) {
        isViewResult = viewResult;
    }

    public boolean isViewSelection() {
        return isViewSelection;
    }

    public void setViewSelection(boolean viewSelection) {
        isViewSelection = viewSelection;
    }

    public boolean isAllAlignementVisible() {
        return allAlignementVisible;
    }

    public void setAllAlignementVisible(boolean allAlignementVisible) {
        this.allAlignementVisible = allAlignementVisible;
    }

    public boolean isPropositionAlignementVisible() {
        return propositionAlignementVisible;
    }

    public void setPropositionAlignementVisible(boolean propositionAlignementVisible) {
        this.propositionAlignementVisible = propositionAlignementVisible;
    }

    public List<NodeAlignment> getAllAlignementFound() {
        return allAlignementFound;
    }

    public void setAllAlignementFound(List<NodeAlignment> allAlignementFound) {
        this.allAlignementFound = allAlignementFound;
    }

    public void setAllignementsList(ArrayList<AlignementElement> allignementsList) {
        this.allignementsList = allignementsList;
    }

    public boolean isNameAlignment() {
        return isNameAlignment;
    }

    public void setNameAlignment(boolean nameAlignment) {
        isNameAlignment = nameAlignment;
    }

    public List<String> getThesaurusUsedLanguageWithoutCurrentLang() {
        return thesaurusUsedLanguageWithoutCurrentLang;
    }

    public void setThesaurusUsedLanguageWithoutCurrentLang(ArrayList<String> thesaurusUsedLanguageWithoutCurrentLang) {
        this.thesaurusUsedLanguageWithoutCurrentLang = thesaurusUsedLanguageWithoutCurrentLang;
    }

    public List<String> getThesaurusUsedLanguage() {
        return thesaurusUsedLanguage;
    }

    public void setThesaurusUsedLanguage(ArrayList<String> thesaurusUsedLanguage) {
        this.thesaurusUsedLanguage = thesaurusUsedLanguage;
    }

    public ArrayList<String> getAllIdsOfBranch() {
        return allIdsOfBranch;
    }

    public void setAllIdsOfBranch(ArrayList<String> allIdsOfBranch) {
        this.allIdsOfBranch = allIdsOfBranch;
    }

    public ArrayList<String> getIdsToGet() {
        return idsToGet;
    }

    public void setIdsToGet(ArrayList<String> idsToGet) {
        this.idsToGet = idsToGet;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public ArrayList<NodeTermTraduction> getNodeTermTraductions() {
        return nodeTermTraductions;
    }

    public void setNodeTermTraductions(ArrayList<NodeTermTraduction> nodeTermTraductions) {
        this.nodeTermTraductions = nodeTermTraductions;
    }

    public ArrayList<NodeNote> getNodeNotes() {
        return nodeNotes;
    }

    public void setNodeNotes(ArrayList<NodeNote> nodeNotes) {
        this.nodeNotes = nodeNotes;
    }

    public ArrayList<NodeImage> getNodeImages() {
        return nodeImages;
    }

    public void setNodeImages(ArrayList<NodeImage> nodeImages) {
        this.nodeImages = nodeImages;
    }

    public boolean isSelectedAllLang() {
        return isSelectedAllLang;
    }

    public void setSelectedAllLang(boolean selectedAllLang) {
        isSelectedAllLang = selectedAllLang;
    }

    public boolean isSelectedAllDef() {
        return isSelectedAllDef;
    }

    public void setSelectedAllDef(boolean selectedAllDef) {
        isSelectedAllDef = selectedAllDef;
    }

    public boolean isSelectedAllImages() {
        return isSelectedAllImages;
    }

    public void setSelectedAllImages(boolean selectedAllImages) {
        isSelectedAllImages = selectedAllImages;
    }

    public List<NodeAlignment> getSelectAlignementForAdd() {
        return selectAlignementForAdd;
    }

    public void setSelectAlignementForAdd(List<NodeAlignment> selectAlignementForAdd) {
        this.selectAlignementForAdd = selectAlignementForAdd;
    }
}
