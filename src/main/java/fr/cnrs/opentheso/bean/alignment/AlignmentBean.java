package fr.cnrs.opentheso.bean.alignment;

import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.repositories.AlignmentHelper;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.ExternalImagesHelper;
import fr.cnrs.opentheso.repositories.GpsHelper;
import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.alignment.NodeAlignmentSmall;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.bean.language.LanguageBean;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.models.alignment.AlignementElement;
import fr.cnrs.opentheso.models.alignment.AlignementSource;
import fr.cnrs.opentheso.models.alignment.SelectedResource;
import fr.cnrs.opentheso.client.alignement.AgrovocHelper;
import fr.cnrs.opentheso.client.alignement.GemetHelper;
import fr.cnrs.opentheso.client.alignement.GeoNamesHelper;
import fr.cnrs.opentheso.client.alignement.GettyAATHelper;
import fr.cnrs.opentheso.client.alignement.IdRefHelper;
import fr.cnrs.opentheso.client.alignement.OntomeHelper;
import fr.cnrs.opentheso.client.alignement.OpenthesoHelper;
import fr.cnrs.opentheso.client.alignement.WikidataHelper;
import fr.cnrs.opentheso.services.alignements.AlignementAutomatique;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Slf4j
@Data
@Named(value = "alignmentBean")
@SessionScoped
public class AlignmentBean implements Serializable {

    @Autowired
    private ConceptView conceptView;

    @Autowired
    private SelectedTheso selectedTheso;

    @Autowired
    private ConceptView conceptBean;

    @Autowired
    private AlignmentManualBean alignmentManualBean;

    @Autowired
    private LanguageBean languageBean;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private ExternalImagesHelper externalImagesHelper;

    @Autowired
    private TermHelper termHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private AlignementAutomatique alignementAutomatique;

    @Autowired
    private AlignmentHelper alignmentHelper;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private NoteHelper noteHelper;

    @Autowired
    private GpsHelper gpsHelper;

    private boolean withLang;
    private boolean withNote;
    private boolean withImage;

    private boolean isViewResult = true;
    private boolean isViewSelection = false;

    private NodeAlignment alignementSelect;
    private NodeLangTheso nodeLangTheso;

    private boolean allAlignementVisible, propositionAlignementVisible, manageAlignmentVisible, comparaisonVisible;
    private List<NodeAlignment> allAlignementFound, selectAlignementForAdd;
    private NodeAlignment selectOneAlignementForAdd;

    private boolean viewSetting = false;
    private boolean viewAddNewSource = false;

    private ArrayList<AlignementSource> alignementSources;
    private String selectedAlignement;
    private AlignementSource alignementSource, selectedAlignementSource;
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

    private String mode;

    private int alignementResultSize = 3;

    //les alignements existants
    private ArrayList<NodeAlignment> existingAlignments;


    /// pour l'alignement manuel en cas de non réponse
    private String manualAlignmentUri;

    public void deleteAlignment(AlignementElement alignement) {
        alignmentHelper.deleteAlignment(
                alignement.getIdAlignment(),
                selectedTheso.getCurrentIdTheso());

        getIdsAndValues(selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());

        initAlignementByStep(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        getIdsAndValues2(conceptBean.getSelectedLang(), selectedTheso.getCurrentIdTheso());

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement supprimé avec succès !");
    }

    public void initForManageAlignment(){
        manageAlignmentVisible = true;
        allAlignementVisible = false;
        propositionAlignementVisible = false;
        comparaisonVisible = false;
    }

    public void cancelForManageAlignment(){
        allAlignementVisible = true;
        propositionAlignementVisible = false;
        manageAlignmentVisible = false;
        comparaisonVisible = false;
    }

    public void supprimerPropositionAlignement(NodeAlignment alignment) {
        allAlignementFound = allAlignementFound.stream()
                .filter(element -> !StringUtils.isEmpty(element.getUri_target()) && !element.getUri_target().equalsIgnoreCase(alignment.getUri_target()))
                .collect(Collectors.toList());
    }

    public void removeAlignmentFromTab(NodeAlignment alignment) {
        allAlignementFound = allAlignementFound.stream()
                .filter(element -> !StringUtils.isEmpty(element.getInternal_id_concept()) && !element.getInternal_id_concept().equalsIgnoreCase(alignment.getInternal_id_concept()))
                .collect(Collectors.toList());
    }

    public void getIdsAndValues(String idLang, String idTheso) {
        if (idsToGet == null) {
            return;
        }

        idsAndValues = conceptHelper.getIdsAndValuesOfConcepts2(idsToGet, idLang, idTheso);
        selectConceptForAlignment(idConceptSelectedForAlignment);

        allignementsList = new ArrayList<>();

        for (NodeIdValue idsAndValue : idsAndValues) {

            ArrayList<NodeAlignment> alignements = alignmentHelper.getAllAlignmentOfConcept(idsAndValue.getId(), idTheso);

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

        idsAndValues = conceptHelper.getIdsAndValuesOfConcepts2(allIdsOfBranch, idLang, idTheso);
        selectConceptForAlignment(idConceptSelectedForAlignment);

        allignementsList = new ArrayList<>();

        for (NodeIdValue concept : idsAndValues) {

            ArrayList<NodeAlignment> alignements = alignmentHelper.getAllAlignmentOfConcept(concept.getId(), idTheso);

            if (!CollectionUtils.isEmpty(alignements)) {
                for (NodeAlignment alignement : alignements) {
                    AlignementElement element = new AlignementElement();
                    element.setIdConceptOrig(concept.getId());
                    element.setLabelConceptOrig(concept.getValue());
                    element.setIdAlignment(alignement.getId_alignement());
                    element.setTypeAlignement(alignement.getAlignmentLabelType());
                    element.setTargetUri(alignement.getUri_target());
                    element.setThesaurus_target(alignement.getThesaurus_target());
                    element.setAlignement_id_type(alignement.getAlignement_id_type());
                    element.setIdSource(alignement.getId_source());
                    element.setConceptTarget(alignement.getConcept_target());
                    element.setValide(alignement.isAlignementLocalValide());

                    var labelConceptCible = StringUtils.isNotEmpty(alignement.getConcept_target()) ?
                            alignement.getConcept_target() : concept.getValue();
                    element.setLabelConceptCible(labelConceptCible);

                    allignementsList.add(element);
                }
            }

            var element = new AlignementElement();
            element.setIdConceptOrig(concept.getId());
            element.setLabelConceptOrig(concept.getValue());
            allignementsList.add(element);
        }

        sortDatatableAlignementByColor();
    }

    public void checkAlignement(String idConceptOrig) {

        allignementsList.stream()
                .filter(element -> element.getIdConceptOrig().equalsIgnoreCase(idConceptOrig))
                .forEach(alignement -> {
                    var isValide = isReachable(alignement.getTargetUri());
                    if (isValide != alignement.isValide()) {
                        alignmentHelper.updateAlignmentUrlStatut(alignement.getIdAlignment(),
                                isValide, idConceptOrig, selectedTheso.getCurrentIdTheso());
                        alignement.setValide(isValide);
                    }
                });

        if (allignementsList.stream()
                .filter(element -> element.getIdConceptOrig().equalsIgnoreCase(idConceptOrig))
                .filter(alignement -> !alignement.isValide())
                .filter(alignement -> alignement.getTargetUri() != null)
                .findFirst().isPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Il existe au moins un alignement qui n'est plus disponible !");
        } else {
            showMessage(FacesMessage.SEVERITY_INFO, "Tous les alignements sont opérationnelles !");
        }
    }

    private boolean isReachable(String urlString) {
        try {
            URL url = new URL(urlString.replace("http://", "https://"));

            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);  // Set a timeout for the connection
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);  // Consider 2xx and 3xx responses as reachable
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    // quand on sélectionne un concept, on récupére sa valeur du vecteur
    public void selectConceptForAlignment(String idConcept) {
        idConceptSelectedForAlignment = idConcept;
        for (NodeIdValue idsAndValue : idsAndValues) {
            if (idsAndValue.getId().equalsIgnoreCase(idConceptSelectedForAlignment)) {
                conceptValueForAlignment = idsAndValue.getValue();
                setExistingAlignment(idConcept, selectedTheso.getCurrentIdTheso());
                cancelAlignment();
                if (listAlignValues != null) {
                    listAlignValues.clear();
                }
                prepareValuesForIdRef();
                listAlignValues = null;
                return;
            }
        }
        listAlignValues = null;
    }

    public void searchAlignementsForAllConcepts(AlignementSource alignementSource) {

        this.alignementSource = alignementSource;
        selectAlignementForAdd = new ArrayList<>();

        allAlignementFound = alignementAutomatique.searchAlignementsAutomatique(selectedTheso.getCurrentIdTheso(),
                selectedTheso.getSelectedLang(), allignementsList, alignementSource, nom, prenom, mode, idsAndValues);

        if (CollectionUtils.isNotEmpty(allAlignementFound)) {
            allAlignementVisible = false;
            manageAlignmentVisible = false;
            if ("alignement-auto".equalsIgnoreCase(mode)) {
                propositionAlignementVisible = true;
                comparaisonVisible = false;
            } else {
                propositionAlignementVisible = false;
                comparaisonVisible = true;
            }
            PrimeFaces.current().ajax().update("containerIndex:formRightTab");
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
        manageAlignmentVisible = false;
        comparaisonVisible = false;

        initAlignementByStep(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        getIdsAndValues2(conceptBean.getSelectedLang(), selectedTheso.getCurrentIdTheso());
    }

    public void saveAlignements() {

        if (CollectionUtils.isNotEmpty(selectAlignementForAdd)) {
            for (NodeAlignment alignment : selectAlignementForAdd) {
                addSingleAlignment(alignment, selectedTheso.getCurrentIdTheso(), alignment.getInternal_id_concept(),
                        selectedTheso.getCurrentUser().getNodeUser().getIdUser());
            }

            allAlignementVisible = true;
            propositionAlignementVisible = false;
            manageAlignmentVisible = false;
            comparaisonVisible = false;

            initAlignementByStep(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());

            getIdsAndValues2(conceptBean.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "", "Aucun alignement selectionné !"));
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }

    public void openEditAlignementWindow(AlignementElement alignement) {
        alignementSources = alignmentHelper.getAlignementSource(selectedTheso.getCurrentIdTheso());
        selectConceptForAlignment(alignement.getIdConceptOrig());
        PrimeFaces.current().executeScript("PF('searchAlignement').show();");
    }

    public void addSingleAlignment(NodeAlignment alignment, String idTheso, String idConcept, int idUser) {

        alignmentHelper.addNewAlignment(idUser, alignment.getConcept_target(),
                alignment.getThesaurus_target(), alignment.getUri_target(), alignment.getAlignement_id_type(),
                alignment.getInternal_id_concept(), idTheso, alignment.getId_source());

        String idTerm = termHelper.getIdTermOfConcept(idConcept, idTheso);
        if (StringUtils.isNotEmpty(idTerm)) {
            if (CollectionUtils.isNotEmpty(alignment.getSelectedTraductionsList())) {
                for (SelectedResource selectedResource : alignment.getSelectedTraductionsList()) {
                    Term term = new Term();
                    term.setIdThesaurus(idTheso);
                    term.setLang(selectedResource.getIdLang());
                    term.setLexicalValue(selectedResource.getGettedValue());
                    term.setIdTerm(idTerm);
                    term.setContributor(idUser);
                    term.setCreator(idUser);
                    term.setSource("");
                    term.setStatus("");
                    if (termHelper.isTraductionExistOfConcept(idConcept, idTheso, selectedResource.getIdLang())) {
                        termHelper.updateTermTraduction(term, idUser);
                    } else {
                        // insert
                        termHelper.addTraduction(selectedResource.getGettedValue(), idTerm,
                                selectedResource.getIdLang(), "", "", idTheso, idUser);
                    }
                }
            }

            //addDefinitions__(idTheso, idConcept, idUser);
            if (CollectionUtils.isNotEmpty(alignment.getSelectedDefinitionsList())) {
                // ajout de la note avec prefix de la source (wikidata)
                for (SelectedResource selectedResource : alignment.getSelectedDefinitionsList()) {
                    if(!noteHelper.isNoteExist(
                            idConcept, idTheso, selectedResource.getIdLang(), selectedResource.getGettedValue(), "definition")) {

                        noteHelper.addNote(idConcept, selectedResource.getIdLang(), idTheso,
                                selectedResource.getGettedValue(), "definition", alignementSource.getSource(), idUser);
                    }
                }
            }
        }

        //addImages__(idTheso, idConcept, idUser);
        if (CollectionUtils.isNotEmpty(alignment.getSelectedImagesList())) {
            for (SelectedResource selectedResource : alignment.getSelectedImagesList()) {
                externalImagesHelper.addExternalImage(idConcept, idTheso, selectedResource.getLocalValue(),
                        alignementSource.getSource(), selectedResource.getGettedValue(), "");
            }
        }

        if (alignment.getThesaurus_target().equalsIgnoreCase("GeoNames")) {
            gpsHelper.insertCoordonees(idConcept, idTheso, alignment.getLat(), alignment.getLng());
        }
    }

    public void remplacerAlignementSelected(String idThesaurus) throws SQLException {

        supprimerAlignementLocal(alignementSelect, idThesaurus);

        addSingleAlignment(alignementSelect,
                selectedTheso.getCurrentIdTheso(),
                alignementSelect.getInternal_id_concept(),
                selectedTheso.getCurrentUser().getNodeUser().getIdUser());

        // Supprimer le nouvel alignement de la liste des propositions des alignements
        allAlignementFound = allAlignementFound.stream()
                .filter(element -> !element.getUri_target().equals(alignementSelect.getUri_target()))
                .collect(Collectors.toList());

        // Mettre à jour la valeur de local libelle et local definition par rapport au nouveau alignement
        allAlignementFound.stream().forEach(element -> {
            if (isEquals(element, alignementSelect)) {
                element.setLabelLocal(alignementSelect.getConcept_target());
                var definitionFound = alignementSelect.getSelectedDefinitionsList().stream()
                        .filter(definition -> definition.getIdLang().equalsIgnoreCase(selectedTheso.getSelectedLang()))
                        .findFirst();
                definitionFound.ifPresent(selectedResource -> element.setDefinitionLocal(selectedResource.getGettedValue()));
            }
        });

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement remplacé avec succès");
        PrimeFaces.current().executeScript("PF('remplacerAlignement').hide();");

    }

    private boolean isEquals(NodeAlignment element, NodeAlignment alignementSelect) {
        return (ObjectUtils.isNotEmpty(element.getConceptOrigin()) && element.getConceptOrigin().equals(alignementSelect.getConceptOrigin()))
                || element.getUri_target().equals(alignementSelect.getUri_target());
    }

    public void supprimerAlignementLocal(NodeAlignment selectedAlignement, String idThesaurus) throws SQLException {

        //Supprimer définition
        if (ObjectUtils.isNotEmpty(conceptView.getDefinition())) {
            noteHelper.deleteNotes(conceptView.getDefinition().getIdNote() + "", idThesaurus);
        }

        //Supprimer l'alignement
        alignmentHelper.deleteAlignment(selectedAlignement.getId_alignement(), idThesaurus);

        alignmentHelper.deleteAlignment(selectedAlignement.getInternal_id_concept(),
                idThesaurus, selectedAlignement.getUri_target());
    }

    public String getDefinitionFromAlignement(NodeAlignment alignement) {
        if (CollectionUtils.isNotEmpty(alignement.getSelectedDefinitionsList())) {
            var definition = alignement.getSelectedDefinitionsList().stream()
                    .filter(element -> element.getIdLang().equalsIgnoreCase(selectedTheso.getSelectedLang()))
                    .findFirst();
            if (definition.isPresent()) {
                return definition.get().getGettedValue();
            } else {
                return "";
            }
        }
        return "--";
    }

    public void addAlignementSelected() {

        addSingleAlignment(alignementSelect,
                selectedTheso.getCurrentIdTheso(),
                alignementSelect.getInternal_id_concept(),
                selectedTheso.getCurrentUser().getNodeUser().getIdUser());

        allAlignementFound = allAlignementFound.stream()
                .filter(element -> !element.getUri_target().equals(alignementSelect.getUri_target()))
                .collect(Collectors.toList());

        selectAlignementForAdd = List.of();

        if (CollectionUtils.isEmpty(allAlignementFound)) {
            allAlignementVisible = true;
            propositionAlignementVisible = false;
            manageAlignmentVisible = false;
            comparaisonVisible = false;

            initAlignementByStep(selectedTheso.getCurrentIdTheso(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());

            getIdsAndValues2(conceptBean.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement ajouté avec succès");
        PrimeFaces.current().executeScript("PF('addAlignement').hide();");

    }

    private String alignementTypeSelected;
    public void addAlignementByConcept() {

        if (selectOneAlignementForAdd != null) {
            var alignementToSave = allAlignementFound.stream()
                    .filter(element -> element.getUri_target().equals(selectOneAlignementForAdd.getUri_target()))
                    .findFirst();

            if (alignementToSave.isPresent()) {
            //    selectedAlignementSource = alignementSource;
            //    getUriAndOptions(selectOneAlignementForAdd, selectedTheso.getCurrentIdTheso());
                
                this.alignementSelect = alignementToSave.get();
                PrimeFaces.current().executeScript("PF('remplacerAlignement').show();");
            } else {
                showMessage(FacesMessage.SEVERITY_ERROR, "Vous devez choisir un seul alignement pour le conception"
                        + selectOneAlignementForAdd.getConcept_target());
            }
        }
    }

    public void setAlignementToRemplace(NodeAlignment alignment) {

        if (alignment != null) {
            var alignementToSave = allAlignementFound.stream()
                    .filter(element -> element.getUri_target().equals(alignment.getUri_target()))
                    .findFirst();

            if (alignementToSave.isPresent()) {
                this.alignementSelect = alignementToSave.get();
                PrimeFaces.current().executeScript("PF('remplacerAlignement').show();");
            } else {
                showMessage(FacesMessage.SEVERITY_ERROR, "Vous devez choisir un seul alignement pour le conception");
            }
        }
    }

    private void showMessage(FacesMessage.Severity severity, String message) {
        FacesMessage msg = new FacesMessage(severity, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public void setExistingAlignment(String idConcept, String idTheso) {
        existingAlignments = alignmentHelper.getAllAlignmentOfConcept(idConcept, idTheso);
    }

    public void prepareValuesForIdRef() {
        if (isNameAlignment) {
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

    /// au lancement du module d'alignement, on initialise les variables.
    /**
     * permet d'initialiser le tableau des concepts à aligner
     */
    public void initAlignementByStep(String idTheso, String idConcept, String currentLang) {
        allIdsOfBranch = conceptHelper.getIdsOfBranchLimited(idConcept, idTheso, 2000);
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

        alignementSources = alignmentHelper.getAlignementSource(idTheso);

        initAlignmentType();

        thesaurusUsedLanguage = thesaurusHelper.getIsoLanguagesOfThesaurus(idTheso);

        thesaurusUsedLanguageWithoutCurrentLang = thesaurusHelper.getIsoLanguagesOfThesaurus(idTheso);
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
    
    public void initAlignmentType(){
        alignmentTypes = new ArrayList<>();
        HashMap<String, String> map = alignmentHelper.getAlignmentType();
        alignmentTypes.addAll(map.entrySet());           
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
                getAlignmentIdRefPerson(selectedAlignementSource, idTheso, idConcept, lexicalValue);
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
     */
    private void getAlignmentWikidata_rest(AlignementSource alignementSource, String idTheso, String idConcept, String lexicalValue, String idLang) {

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
     */
    private void getAlignmentWikidata_sparql(AlignementSource alignementSource, String idTheso, String idConcept, String lexicalValue, String idLang) {

        if (alignementSource == null) {
            listAlignValues = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Source :", "Pas de source sélectionnée"));
            return;
        }
        WikidataHelper wikidataHelper = new WikidataHelper();

        // action JSON (HashMap (Wikidata)
        //ici il faut appeler le filtre de Wikidata
        listAlignValues = wikidataHelper.queryWikidata_sparql(idConcept, idTheso, alignementSource.getRequete(), alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), wikidataHelper.getMessages().toString()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
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
     */
    private void getAlignmentIdRefPerson(AlignementSource alignementSource, String idTheso, String idConcept, String lexicalValue) {

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
    private void getAlignmentIdRefUniformtitle(AlignementSource alignementSource, String idTheso, String idConcept, String lexicalValue) {

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
                alignementSource.getRequete(), alignementSource.getSource());
        if (listAlignValues == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    languageBean.getMsg("search.noResult"), gettyAATHelper.getMessages()));
        }
    }

    /**
     * Cette fonction permet de récupérer les concepts à aligner de la source
     * juste la liste des concepts avec une note pour distinguer les concepts/
     */
    private void getAlignmentGemet(AlignementSource alignementSource, String idTheso, String idConcept, String lexicalValue, String idLang) {

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
     */
    private void getAlignmentAgrovoc(AlignementSource alignementSource, String idTheso, String idConcept, String lexicalValue, String idLang) {

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
     */
    private void getAlignmentGeoNames(AlignementSource alignementSource, String idTheso, String idConcept, String lexicalValue, String idLang) {

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
     */
    private void getAlignmentOntome(AlignementSource alignementSource, String idTheso, String idConcept, String lexicalValue) {

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
     */
    private void getAlignmentOpentheso(AlignementSource alignementSource, String idTheso, String idConcept, String lexicalValue, String idLang) {
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
     * initialisation des valeurs du concept local pour comparaison avec leconcept à aligner
     */
    private void getValuesOfLocalConcept(String idTheso, String idConcept) {
        nodeTermTraductions = termHelper.getAllTraductionsOfConcept(idConcept, idTheso);
        nodeNotes = noteHelper.getListNotesAllLang(idConcept, idTheso);
        nodeImages = externalImagesHelper.getExternalImages(idConcept, idTheso);
        nodeAlignmentSmall = alignmentHelper.getAllAlignmentOfConceptNew(idConcept, idTheso);
    }

    /**
     * L'utilisateur a cliqué sur un concept à aligner, ici on récupère les
     * détails du concept de la source et les détails des options (images,
     * définitions, traductions en plus de l'URL d'alignement récupération des
     * options
     */
    public void getUriAndOptions(NodeAlignment selectedNodeAlignment, String idTheso) {
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
        boolean toIgnore;
        if (descriptionsOfAlignmentTemp == null) {
            return;
        }
        // la liste des définitions de Wikidata
        for (SelectedResource selectedResource : descriptionsOfAlignmentTemp) {
            toIgnore = false;
            // on compare le texte si équivalent, on l'ignore
            for (NodeNote nodeNote : nodeNotes) {
                switch (nodeNote.getNoteTypeCode()) {
                    case "definition":
                        if(selectedResource.getIdLang().equalsIgnoreCase(nodeNote.getLang())){
                            // la def existe dans cette langue
                            
                            if (!selectedResource.getGettedValue().trim().equalsIgnoreCase(nodeNote.getLexicalValue().trim())) {
                                // la def est diférente, il faut le signaler pour l'accepter ou non 
                                selectedResource.setLocalValue(nodeNote.getLexicalValue());
                            } else 
                                // la def est identique, il faut l'ignorer
                                toIgnore = true;
                        }
                        break;
                    default:
                        break;
                }
            }
            if(!toIgnore){
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
     */
    public void addAlignment(String idTheso, String idConcept, int idUser, boolean fromAlignmentInterface) {
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
        if (!addImages__(idTheso, idConcept)) {
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

        alignementResult = alignementResult + alignmentHelper.getMessage();
        selectedNodeAlignment = null;
        alignmentInProgress = false;

        if (fromAlignmentInterface) {
            updateDateOfConcept(idTheso, idConcept, idUser);

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement ajouté avec succès");
            FacesContext.getCurrentInstance().addMessage(null, msg);

            isViewResult = true;
            isViewSelection = false;
            setExistingAlignment(idConcept, idTheso);

            getIdsAndValues2(conceptView.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        }

        resetVariables();
    }

    /**
     * permet d'ajouter l'alignement et les options choisis (traductions,
     * définitions et images) la focntion gère les erreurs en cas de problème
     */
    public void addManualAlignement(String idTheso, String idConcept, int idUser) {

        // ajout de l'alignement séléctionné
        if (!alignmentHelper.addNewAlignment(idUser, "", selectedAlignement,
                manualAlignmentUri, selectedAlignementType, idConcept, idTheso, -1)) {
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

        updateDateOfConcept(idTheso, idConcept, idUser);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        isViewResult = true;
        isViewSelection = false;
        setExistingAlignment(idConcept, idTheso);

        getIdsAndValues2(conceptView.getSelectedLang(), selectedTheso.getCurrentIdTheso());

        resetVariables();
    }

    private void updateDateOfConcept(String idTheso, String idConcept, int idUser) {

        conceptHelper.updateDateOfConcept(idTheso, idConcept, idUser);
        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                idConcept, idTheso);
    }

    /**
     * Permet d'ajouter les coordonnées GPS pour les lieux
     */
    private boolean addGps__(String idTheso, String idConcept) {

        // ajout de l'alignement séléctionné
        if (!gpsHelper.insertCoordonees(idConcept, idTheso, selectedNodeAlignment.getLat(),
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
     */
    private boolean addAlignment__(String idTheso, String idConcept, int idUser) {

        // ajout de l'alignement séléctionné
        if (!alignmentHelper.addNewAlignment(
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

        Term term = new Term();
        String idTerm = termHelper.getIdTermOfConcept(idConcept, idTheso);
        if (idTerm == null) {
            return false;
        }

        for (SelectedResource selectedResource : traductionsOfAlignment) {
            if (selectedResource.isSelected()) {
                term.setIdThesaurus(idTheso);
                term.setLang(selectedResource.getIdLang());
                term.setLexicalValue(selectedResource.getGettedValue());
                term.setIdTerm(idTerm);
                term.setContributor(idUser);
                term.setCreator(idUser);
                term.setSource("");
                term.setStatus("");
                if (termHelper.isTraductionExistOfConcept(
                        idConcept, idTheso, selectedResource.getIdLang())) {
                    // update
                    if (!termHelper.updateTermTraduction(term, idUser)) {
                        error = true;
                        alignementResult = alignementResult + ": Erreur pendant la modification des traductions";
                    }
                } else {
                    // insert
                    if (!termHelper.addTraduction(
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

        // ajout de la note avec prefix de la source (wikidata)
        for (SelectedResource selectedResource : descriptionsOfAlignment) {
            if (selectedResource.isSelected()) {
                if(noteHelper.isNoteExist(
                        idConcept, idTheso,
                        selectedResource.getIdLang(), selectedResource.getGettedValue(), "definition")) {
                    continue;
                }

                if (!noteHelper.addNote(
                        idConcept, selectedResource.getIdLang(),
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

    private boolean addImages__(String idTheso, String idConcept) {

        for (SelectedResource selectedResource : imagesOfAlignment) {
            if (selectedResource.isSelected()) {
                if (!externalImagesHelper.addExternalImage(
                        idConcept, idTheso,
                        conceptValueForAlignment,
                        selectedAlignement,
                        selectedResource.getGettedValue(),
                        "")) {
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
            showMessage(FacesMessage.SEVERITY_WARN, "!!! Attention respectez la casse !!!!");
        } else {
            alertWikidata = "";
        }
    }

    public String getSourceAlignement(AlignementElement alignment, SetAlignmentSourceBean setAlignmentSourceBean) {
        if (StringUtils.isEmpty(alignment.getThesaurus_target())) {
            if (CollectionUtils.isEmpty(setAlignmentSourceBean.getAllAlignementSources())) {
                setAlignmentSourceBean.initSourcesList();
            }
            var sourceFound = setAlignmentSourceBean.getAllAlignementSources().stream()
                    .filter(source -> getBaseUrl(source.getRequete()).equalsIgnoreCase(getBaseUrl(alignment.getTargetUri())))
                    .findFirst();
            return sourceFound.isPresent() ? sourceFound.get().getSource() : getBaseUrl(alignment.getTargetUri());
        } else {
            return alignment.getThesaurus_target();
        }
    }

    private String getBaseUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }
    
}
