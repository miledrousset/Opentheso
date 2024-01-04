package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import com.jsf2leaf.model.Map;
import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.CorpusHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.LanguageHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.PathHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeConceptType;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCustomRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePath;
import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.repositories.GpsRepository;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.ResponsiveOption;


/**
 *
 * @author miledrousset
 */
@Data
@SessionScoped
@Named(value = "conceptView")
public class ConceptView implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private IndexSetting indexSetting;
    @Inject
    private ViewEditorThesoHomeBean viewEditorThesoHomeBean;
    @Inject
    private ViewEditorHomeBean viewEditorHomeBean;
    @Inject
    private Tree tree;
    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private GpsRepository gpsRepository;
    @Inject
    private CurrentUser currentUser;

    private Map mapModel;
    private NodeConcept nodeConcept;
    private String selectedLang;
    private GpsMode gpsModeSelected;
    private ArrayList<NodeCorpus> nodeCorpuses;
    private ArrayList<NodePath> pathLabel;
    private ArrayList<NodeIdValue> nodeFacets;

    /// pagination
    private int offset;
    private int step;
    private boolean haveNext;

    // total de la branche
    private int countOfBranch;

    // pour savoir si le concept a des relations vers des corpus
    private boolean haveCorpus;
    private boolean searchedForCorpus;

    /// Notes concept
    private ArrayList<NodeNote> notes;
    private ArrayList<NodeNote> scopeNotes;

    //// Notes term    
    private ArrayList<NodeNote> changeNotes;
    private ArrayList<NodeNote> definitions;
    private ArrayList<NodeNote> editorialNotes;
    private ArrayList<NodeNote> examples;
    private ArrayList<NodeNote> historyNotes;

    private ArrayList<NodeCustomRelation> nodeCustomRelationReciprocals;

    private List<ResponsiveOption> responsiveOptions;

    private boolean toggleSwitchAltLabelLang;
    private boolean toggleSwitchNotesLang;

    private String creator;
    private String contributors;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        nodeCorpuses = new ArrayList<>();
        pathLabel = new ArrayList<>();
        notes = new ArrayList<>();
        scopeNotes = new ArrayList<>();
        changeNotes = new ArrayList<>();
        definitions = new ArrayList<>();
        editorialNotes = new ArrayList<>();
        examples = new ArrayList<>();
        historyNotes = new ArrayList<>();
        nodeConcept = new NodeConcept();
        nodeFacets = new ArrayList<>();

        selectedLang = null;
        mapModel = null;
        nodeCustomRelationReciprocals = null;
    }

    /**
     * Creates a new instance of ConceptBean
     */
    public ConceptView() {
    }

    public void init() {
        toggleSwitchAltLabelLang = false;
        toggleSwitchNotesLang = false;
        if (nodeConcept != null) {
            nodeConcept.clear();
        }
        selectedLang = null;

        if (nodeFacets == null) {
            nodeFacets = new ArrayList<>();
        }
        clearNotes();

        offset = 0;
        step = 20;
        haveNext = false;

        nodeCorpuses = null;
        countOfBranch = 0;
        haveCorpus = false;
        searchedForCorpus = false;
        nodeCustomRelationReciprocals = null;

        responsiveOptions = new ArrayList<>();
        responsiveOptions.add(new ResponsiveOption("1024px", 5));
        responsiveOptions.add(new ResponsiveOption("768px", 3));
        responsiveOptions.add(new ResponsiveOption("560px", 1));
    }

    private void clearNotes() {
        notes = new ArrayList<>();
        scopeNotes = new ArrayList<>();
        changeNotes = new ArrayList<>();
        definitions = new ArrayList<>();
        editorialNotes = new ArrayList<>();
        examples = new ArrayList<>();
        historyNotes = new ArrayList<>();
    }

    public String getDrapeauImgLocal(String codePays) {
        if (StringUtils.isEmpty(codePays)) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/noflag.png";
        }
        return FacesContext.getCurrentInstance().getExternalContext()
                .getRequestContextPath() + "/resources/img/flag/" + codePays + ".png";
    }
    
    public String getFlagFromCodeLang(String idLang){
        LanguageHelper languageHelper = new LanguageHelper();
        String flag = languageHelper.getFlagFromIdLang(connect.getPoolConnexion(), idLang);
        return FacesContext.getCurrentInstance().getExternalContext()
                .getRequestContextPath() + "/resources/img/flag/" + flag + ".png";        
    }

    /**
     * permet de retourner le label du type de concept en focntion de la langue de l'interface
     *
     * @param conceptType
     * @param idTheso
     * @return
     */
    public String getLabelOfConceptType(String conceptType, String idTheso) {
        String idLang;
        RelationsHelper relationsHelper = new RelationsHelper();
        idLang = getIdLangOfInterface();

        return relationsHelper.getLabelOfTypeConcept(connect.getPoolConnexion(),
                conceptType,
                idTheso,
                idLang);
    }

    /**
     * récuparation des informations pour le concept sélectionné c'est pour la
     * navigation entre les concepts dans la vue de droite avec deployement de
     * l'arbre
     *
     * @param idTheso
     * @param idConcept
     * @param idLang
     */
    public void getConcept(String idTheso, String idConcept, String idLang) {
        offset = 0;
        gpsModeSelected = GpsMode.POINT;
        nodeConcept = new ConceptHelper().getConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang, step + 1, offset);
        if (nodeConcept == null) {
            return;
        }
        
        searchedForCorpus = false;
        
        
        // permet de récupérer les qualificatifs
        if (roleOnThesoBean.getNodePreference().isUseCustomRelation()) {
            String interfaceLang = getIdLangOfInterface();

            nodeConcept.setNodeCustomRelations(new RelationsHelper().getAllNodeCustomRelation(
                    connect.getPoolConnexion(), idConcept, idTheso, idLang, interfaceLang));
            setNodeCustomRelationWithReciprocal(nodeConcept.getNodeCustomRelations());
        }

        setOffset();

        // récupération des informations sur les corpus liés
        nodeCorpuses = null;
    //    haveCorpus = true;


        createMap(idConcept, idTheso, Boolean.TRUE);

        selectedLang = idLang;
        if (toggleSwitchAltLabelLang) {
            getAltLabelWithAllLanguages();
        }
        if (toggleSwitchNotesLang) {
            getNotesWithAllLanguages();
        } else {
            setNotes();
        }

        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();

        // récupération des informations sur les corpus liés
        haveCorpus = false;
    //    nodeCorpuses = new CorpusHelper().getAllActiveCorpus(connect.getPoolConnexion(), idTheso);

        setRoles();

        setFacetsOfConcept(idConcept, idTheso, idLang);

        // deployement de l'arbre si l'option est true
        if (roleOnThesoBean.getNodePreference() != null) {
            if (roleOnThesoBean.getNodePreference().isBreadcrumb())
                pathOfConcept(idTheso, idConcept, idLang);

            if (roleOnThesoBean.getNodePreference().isAuto_expand_tree()) {
                tree.expandTreeToPath(
                        idConcept,
                        idTheso,
                        idLang);
                if (PrimeFaces.current().isAjaxRequest()) {
                    PrimeFaces.current().ajax().update("containerIndex:formLeftTab:tabTree:tree");
                    PrimeFaces.current().ajax().update("containerIndex:languageSelect");
                }
                selectedTheso.actionFromConceptToOn();
            }
        }

        countOfBranch = 0;
    }


    public void searchCorpus() {
        searchedForCorpus = true;
        SearchCorpus2 searchCorpus2 = new SearchCorpus2();
        nodeCorpuses = new CorpusHelper().getAllActiveCorpus(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());     
        nodeCorpuses = searchCorpus2.SearchCorpus(nodeCorpuses, nodeConcept);
        haveCorpus = searchCorpus2.isHaveCorpus();
        if(!haveCorpus) {
            nodeCorpuses = null;
            nodeCorpuses = null;
        }
    }

    public void createMap(String idConcept, String idTheso, Boolean isFirstTime) {
        nodeConcept.setNodeGps(gpsRepository.getGpsByConceptAndThesorus(idConcept, idTheso));
        if (CollectionUtils.isNotEmpty(nodeConcept.getNodeGps())) {
            gpsModeSelected = getGpsMode(nodeConcept.getNodeGps());
            gpsList = formatCoordonnees(nodeConcept.getNodeGps());
            if (isFirstTime) {
                mapModel = new MapUtils().createMap(nodeConcept.getNodeGps(), gpsModeSelected,
                        ObjectUtils.isEmpty(nodeConcept.getTerm()) ? null : nodeConcept.getTerm().getLexical_value());
            } else {
                mapModel = new MapUtils().updateMap(nodeConcept.getNodeGps(), mapModel, gpsModeSelected,
                        ObjectUtils.isEmpty(nodeConcept.getTerm()) ? null : nodeConcept.getTerm().getLexical_value());
            }
        } else {
            gpsList = "";
        }
    }

    public boolean isGpsDisable() {
        return currentUser.getNodeUser() == null || (currentUser.getNodeUser() != null &&
                !(roleOnThesoBean.isManagerOnThisTheso() || roleOnThesoBean.isAdminOnThisTheso() || roleOnThesoBean.isSuperAdmin()));
    }

    private String formatCoordonnees(List<Gps> listeCoordonnees) {
        if (CollectionUtils.isEmpty(listeCoordonnees)) {
            return "";
        } else {
            StringBuilder resultat = new StringBuilder();
            for (Gps gps : listeCoordonnees) {
                resultat.append(gps.toString()).append(", ");
            }
            return "(" + resultat.substring(0, resultat.length() - 2) + ")";
        }
    }

    /**
     * récuparation des informations pour le concept sélectionné après une
     * sélection dans l'arbre
     *
     * @param idTheso
     * @param idConcept
     * @param idLang
     */
    public void getConceptForTree(String idTheso, String idConcept, String idLang) {
        offset = 0;
        nodeConcept = new ConceptHelper().getConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang, step + 1, offset);
        if (nodeConcept == null) return;
        
        // permet de récupérer les qualificatifs
        if (roleOnThesoBean.getNodePreference().isUseCustomRelation()) {
            nodeConcept.setNodeCustomRelations(new RelationsHelper().getAllNodeCustomRelation(
                    connect.getPoolConnexion(), idConcept, idTheso, idLang, getIdLangOfInterface()));
            setNodeCustomRelationWithReciprocal(nodeConcept.getNodeCustomRelations());
        }

        if (roleOnThesoBean.getNodePreference().isBreadcrumb())
            pathOfConcept(idTheso, idConcept, idLang);

        if (toggleSwitchAltLabelLang) {
            getAltLabelWithAllLanguages();
        }
        if (toggleSwitchNotesLang) {
            getNotesWithAllLanguages();
        } else {
            setNotes();
        }

        setOffset();

        // récupération des informations sur les corpus liés
        nodeCorpuses = null;
        haveCorpus = false;
        searchedForCorpus = false;
      //  nodeCorpuses = new CorpusHelper().getAllActiveCorpus(connect.getPoolConnexion(), idTheso);

        setRoles();

        createMap(idConcept, idTheso, Boolean.TRUE);

        setFacetsOfConcept(idConcept, idTheso, idLang);

        selectedLang = idLang;
        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();
        countOfBranch = 0;
    }

    /**
     * permet de récupérer toutes les notes dans toutes les langues
     */
    public void getNotesWithAllLanguages() {
        NoteHelper noteHelper = new NoteHelper();
        if (toggleSwitchNotesLang) {
            nodeConcept.setNodeNotesTerm(noteHelper.getListNotesTermAllLang(
                    connect.getPoolConnexion(), nodeConcept.getTerm().getId_term(), nodeConcept.getConcept().getIdThesaurus()));
            nodeConcept.setNodeNotesConcept(noteHelper.getListNotesConceptAllLang(
                    connect.getPoolConnexion(), nodeConcept.getConcept().getIdConcept(), nodeConcept.getConcept().getIdThesaurus()));
        } else {
            nodeConcept.setNodeNotesTerm(noteHelper.getListNotesTerm(
                    connect.getPoolConnexion(),
                    nodeConcept.getTerm().getId_term(),
                    nodeConcept.getConcept().getIdThesaurus(),
                    selectedLang));

            nodeConcept.setNodeNotesConcept(noteHelper.getListNotesConcept(
                    connect.getPoolConnexion(), nodeConcept.getConcept().getIdConcept(),
                    nodeConcept.getConcept().getIdThesaurus(),
                    selectedLang));
        }
        setNotes();
        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    public void getAltLabelWithAllLanguages() {
        TermHelper termHelper = new TermHelper();

        if (toggleSwitchAltLabelLang)
            nodeConcept.setNodeEM(termHelper.getAllNonPreferredTerms(
                    connect.getPoolConnexion(), nodeConcept.getConcept().getIdConcept(), nodeConcept.getConcept().getIdThesaurus()));
        else
            nodeConcept.setNodeEM(termHelper.getNonPreferredTerms(connect.getPoolConnexion(),
                    nodeConcept.getConcept().getIdConcept(),
                    nodeConcept.getConcept().getIdThesaurus(),
                    selectedLang));
        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }


    private void setFacetsOfConcept(String idConcept, String idTheso, String idLang) {
        FacetHelper facetHelper = new FacetHelper();
        List<String> facetIds = facetHelper.getAllIdFacetsConceptIsPartOf(connect.getPoolConnexion(), idConcept, idTheso);
        if (nodeFacets == null)
            nodeFacets = new ArrayList<>();
        else
            nodeFacets.clear();
        for (String facetId : facetIds) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(facetId);
            nodeIdValue.setValue(facetHelper.getLabelOfFacet(connect.getPoolConnexion(), facetId, idTheso, idLang));
            nodeFacets.add(nodeIdValue);
        }
    }

    public void setOffset() {
        if (nodeConcept.getNodeNT().size() < step) {
            offset = 0;
            haveNext = false;
        } else {
            offset = offset + step + 1;
            haveNext = true;
        }

    }

    public void countTheTotalOfBranch() {
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> listIdsOfBranch = conceptHelper.getIdsOfBranch(
                connect.getPoolConnexion(),
                nodeConcept.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        this.countOfBranch = listIdsOfBranch.size();
    }

    public void setNodeCustomRelationWithReciprocal(ArrayList<NodeCustomRelation> nodeCustomRelations) {
        nodeCustomRelationReciprocals = new ArrayList<>();
        for (NodeCustomRelation nodeCustomRelation : nodeCustomRelations) {
            if (nodeCustomRelation.isReciprocal())
                nodeCustomRelationReciprocals.add(nodeCustomRelation);
        }
        if (nodeCustomRelationReciprocals.isEmpty())
            nodeCustomRelationReciprocals = null;
    }

    public void getNextNT(String idTheso, String idConcept, String idLang) {
        if (tree != null && tree.getSelectedNode() != null && tree.getSelectedNode().getData() != null) {
            ArrayList<NodeNT> nodeNTs = new RelationsHelper().getListNT(connect.getPoolConnexion(),
                    ((TreeNodeData) tree.getSelectedNode().getData()).getNodeId(),
                    idTheso,
                    idLang, step + 1, offset);
            if (nodeNTs != null && !nodeNTs.isEmpty()) {
                nodeConcept.getNodeNT().addAll(nodeNTs);
                setOffset();
                return;
            }
            haveNext = false;
        }
    }

    private void pathOfConcept(String idTheso, String idConcept, String idLang) {
        PathHelper pathHelper = new PathHelper();
        List<Path> paths = pathHelper.getPathOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);
        if (pathHelper.getMessage() != null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", pathHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        if (paths == null) {
            System.out.println("Erreur de path pour le concept :" + idConcept);
            if (pathLabel != null) {
                pathLabel.clear();
            }
            return;
        }
        pathLabel = pathHelper.getPathWithLabel(connect.getPoolConnexion(), paths, idTheso, idLang, idConcept);
    }

    private void setRoles() {
        contributors = null;
        creator = null;
        boolean firstElement = true;
        if (CollectionUtils.isNotEmpty(nodeConcept.getDcElements())) {
            for (DcElement dcElement : nodeConcept.getDcElements()) {
                switch (dcElement.getName()) {
                    case DCMIResource.CONTRIBUTOR:
                        if (firstElement) {
                            contributors = dcElement.getValue();
                            firstElement = false;
                        } else {
                            contributors = contributors + "; " + dcElement.getValue();
                        }
                        break;
                    case DCMIResource.CREATOR:
                        creator = dcElement.getValue();
                    default:
                        break;
                }
            }
        }
    }

    public String getNoteSource(String noteSource) {
        if (StringUtils.isEmpty(noteSource))
            return "";
        else
            return " (" + noteSource + ")";
    }

    /////////////////////////////////
/////////////////////////////////
// fonctions pour les notes /////    
/////////////////////////////////
/////////////////////////////////
    private void setNotes() {
        clearNotes();
        for (NodeNote nodeNote : nodeConcept.getNodeNotesConcept()) {
            switch (nodeNote.getNotetypecode()) {
                case "note":
                    notes.add(nodeNote);
                    break;
                case "scopeNote":
                    scopeNotes.add(nodeNote);
                    break;
            }
        }
        for (NodeNote nodeNote : nodeConcept.getNodeNotesTerm()) {
            switch (nodeNote.getNotetypecode()) {
                case "changeNote":
                    changeNotes.add(nodeNote);
                    break;
                case "definition":
                    definitions.add(nodeNote);
                    break;
                case "editorialNote":
                    editorialNotes.add(nodeNote);
                    break;
                case "example":
                    examples.add(nodeNote);
                    break;
                case "historyNote":
                    historyNotes.add(nodeNote);
                    break;
            }
        }
    }

    public String getColorOfTypeConcept() {
        if ("concept".equalsIgnoreCase(nodeConcept.getConcept().getConceptType()))
            return "";
        else
            return "#fcd8bf";
    }

    public String geLabelReciprocal(NodeConceptType nodeConceptType) {
        if ("concept".equalsIgnoreCase(nodeConceptType.getCode())) {
            return "";
        }
        String idLang = languageBean.getIdLangue();
        if (nodeConceptType.isReciprocal()) {
            if ("fr".equalsIgnoreCase(idLang)) {
                return " - Relation réciproque";
            }
            if ("en".equalsIgnoreCase(idLang)) {
                return " - Reciprocal relation";
            }
        } else {
            if ("fr".equalsIgnoreCase(idLang)) {
                return " - Relation à sens unique";
            }
            if ("en".equalsIgnoreCase(idLang)) {
                return " - One-way relationship";
            }
        }
        return "";
    }

    /**
     * permet de retouver la langue de l'interface et se limiter au fr et en
     *
     * @return
     */
    private String getIdLangOfInterface() {
        String idLang;
        if ("en".equalsIgnoreCase(languageBean.getIdLangue()) || "fr".equalsIgnoreCase(languageBean.getIdLangue())) {
            idLang = languageBean.getIdLangue();
        } else
            idLang = "en";
        return idLang;
    }

    public Boolean isMapVisible() {
        return ObjectUtils.isNotEmpty(nodeConcept) && CollectionUtils.isNotEmpty(nodeConcept.getNodeGps());
    }

    public void onRowEdit(RowEditEvent<Gps> event) {
        gpsRepository.updateGps(event.getObject());
        createMap(nodeConcept.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage("Coordonnée modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowCancel(RowEditEvent<Gps> event) {
        gpsRepository.removeGps(event.getObject());

        createMap(nodeConcept.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage("Coordonnée supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void addNewGps() {
        Gps gps = new Gps();
        gps.setIdTheso(selectedTheso.getCurrentIdTheso());
        gps.setIdConcept(nodeConcept.getConcept().getIdConcept());
        gps.setPosition(nodeConcept.getNodeGps().size() + 1);

        gpsRepository.saveNewGps(gps);
        createMap(nodeConcept.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage("Nouvelle coordonnée ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowReorder(ReorderEvent event) {
        Integer fromId = 0, toId = 0;

        for (Gps gps : nodeConcept.getNodeGps()) {
            if (gps.getPosition() == (event.getFromIndex() + 1)) {
                fromId = gps.getId();
            }
            if (gps.getPosition() == (event.getToIndex() + 1)) {
                toId = gps.getId();
            }
        }

        gpsRepository.updateGpsPosition(fromId, (event.getToIndex() + 1));
        gpsRepository.updateGpsPosition(toId, (event.getFromIndex() + 1));

        createMap(nodeConcept.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage("Réorganisation des coordonnées effectuée");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private String gpsList;

    public void formatGpsList() {

        if (StringUtils.isEmpty(gpsList)) {
            nodeConcept.setNodeGps(null);
            gpsRepository.removeGpsByConcept(nodeConcept.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        } else {
            var gpsListTmps = readGps(gpsList, selectedTheso.getCurrentIdTheso(), nodeConcept.getConcept().getIdConcept());

            if (ObjectUtils.isNotEmpty(gpsListTmps)) {
                nodeConcept.setNodeGps(gpsListTmps);

                gpsModeSelected = getGpsMode(nodeConcept.getNodeGps());

                gpsRepository.removeGpsByConcept(nodeConcept.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
                for (Gps gps : nodeConcept.getNodeGps()) {
                    gpsRepository.saveNewGps(gps);
                }
            } else {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Aucune coordonnée GPS trouvée !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().ajax().update("messageIndex");
                return;
            }
        }

        createMap(nodeConcept.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Coordonnée GPS modifiés !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:rightTab");
    }

    private GpsMode getGpsMode(List<Gps> nodeGps) {
        if (CollectionUtils.isNotEmpty(nodeGps)) {
            var lastIndex = nodeGps.size() - 1;
            if (nodeGps.size() == 1) {
                return GpsMode.POINT;
            } else if (nodeGps.get(0).getLongitude().equals(nodeGps.get(lastIndex).getLongitude())
                    && nodeGps.get(0).getLatitude().equals(nodeGps.get(lastIndex).getLatitude())) {
                return GpsMode.POLYGONE;
            } else {
                return GpsMode.POLYLINE;
            }
        }
        return null;
    }

    public static List<Gps> readGps(String gpsValue, String idTheso, String idConcept) {

        List<Gps> gpsList = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\(([^)]+)\\)").matcher(gpsValue);
        while (matcher.find()) {

            Matcher matcher2 = Pattern.compile("([0-9]+[.,][0-9]+) ([0-9]+[.,][0-9]+)").matcher(matcher.group(1));

            while (matcher2.find()) {
                Gps gpsTmp = new Gps();
                gpsTmp.setIdTheso(idTheso);
                gpsTmp.setIdConcept(idConcept);
                gpsTmp.setPosition(gpsList.size() + 1);
                gpsTmp.setLatitude(Double.parseDouble(matcher2.group(1).replace(",", ".")));
                gpsTmp.setLongitude(Double.parseDouble(matcher2.group(2).replace(",", ".")));
                gpsList.add(gpsTmp);
            }
        }

        return gpsList;
    }
}
