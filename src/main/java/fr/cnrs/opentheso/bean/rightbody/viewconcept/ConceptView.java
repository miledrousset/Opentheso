package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import com.jsf2leaf.model.Map;
import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.CorpusHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
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
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import fr.cnrs.opentheso.repositories.GpsRepository;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.ResponsiveOption;


/**
 *
 * @author miledrousset
 */
@Named(value = "conceptView")
@SessionScoped
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

    private Map mapModel;
    private NodeConcept nodeConcept;
    private String selectedLang, gpsModeSelected;
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
        nodeCustomRelationReciprocals = null;

        if (mapModel == null) {
            mapModel = new Map();
        }

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

    public String getDrapeauImg(String codePays) {
        if (StringUtils.isEmpty(codePays)) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/nu.svg";
        }

        return "https://countryflagsapi.com/png/" + codePays;
    }

    public String getDrapeauImgLocal(String codePays) {
        if (StringUtils.isEmpty(codePays)) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/noflag.png";
        }
        return FacesContext.getCurrentInstance().getExternalContext()
                .getRequestContextPath() + "/resources/img/flag/" + codePays + ".png";
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
        gpsModeSelected = "LINE";
        nodeConcept = new ConceptHelper().getConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang, step + 1, offset);
        if (nodeConcept == null) {
            return;
        }
        // permet de récupérer les qualificatifs
        if (roleOnThesoBean.getNodePreference().isUseCustomRelation()) {
            String interfaceLang = getIdLangOfInterface();

            nodeConcept.setNodeCustomRelations(new RelationsHelper().getAllNodeCustomRelation(
                    connect.getPoolConnexion(), idConcept, idTheso, idLang, interfaceLang));
            setNodeCustomRelationWithReciprocal(nodeConcept.getNodeCustomRelations());
        }

        setOffset();

        createMap(idConcept, idTheso);

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
        List<NodeCorpus> nodeCorpusesTmp = new CorpusHelper().getAllActiveCorpus(connect.getPoolConnexion(), idTheso);
        if (CollectionUtils.isNotEmpty(nodeCorpusesTmp)) {
            searchCorpus(nodeCorpusesTmp);
        }
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

    private void searchCorpus(List<NodeCorpus> nodeCorpusesTmp) {
        nodeCorpuses = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(nodeCorpusesTmp.size());
        List<Callable<NodeCorpus>> callables = new ArrayList<>();

        for (NodeCorpus nodeCorpus : nodeCorpusesTmp) {
            callables.add(new SearchCorpus(nodeCorpus, nodeConcept));
        }

        try {
            List<Future<NodeCorpus>> futures = executor.invokeAll(callables);
            for (Future<NodeCorpus> future : futures) {
                haveCorpus = true;
                nodeCorpuses.add(future.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    public void createMap(String idConcept, String idTheso) {
        nodeConcept.setNodeGps(gpsRepository.getGpsByConceptAndThesorus(idConcept, idTheso));
        if (CollectionUtils.isNotEmpty(nodeConcept.getNodeGps())) {
            createMapWithMode();
        }
    }

    public void createMapWithMode() {
        GpsMode gpsMode;
        switch (gpsModeSelected) {
            case "POLYGONE":
                gpsMode = GpsMode.POLYGONE;
                break;
            case "POLYLINE":
                gpsMode = GpsMode.POLYLINE;
                break;
            default:
                gpsMode = GpsMode.POINT;
        }
        mapModel = new MapUtils().createMap(nodeConcept.getNodeGps(), gpsMode,
                ObjectUtils.isEmpty(nodeConcept.getTerm()) ? null : nodeConcept.getTerm().getLexical_value());
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
        haveCorpus = false;
        List<NodeCorpus> nodeCorpusesTmp = new CorpusHelper().getAllActiveCorpus(connect.getPoolConnexion(), idTheso);
        if (CollectionUtils.isNotEmpty(nodeCorpusesTmp)) {
            searchCorpus(nodeCorpusesTmp);
        }
        setRoles();

        nodeConcept.setNodeGps(gpsRepository.getGpsByConceptAndThesorus(idConcept, idTheso));
        if (CollectionUtils.isNotEmpty(nodeConcept.getNodeGps())) {
            mapModel = new MapUtils().createMap(nodeConcept.getNodeGps(), GpsMode.POINT, "");
        }

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
                    nodeConcept.getTerm().getId_term(),
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

    public void countTheTotalOfBranch() {
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> listIdsOfBranch = conceptHelper.getIdsOfBranch(
                connect.getPoolConnexion(),
                nodeConcept.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        this.countOfBranch = listIdsOfBranch.size();
    }

    private int getCountFromJson(String jsonText) {
        if (jsonText == null) {
            return -1;
        }
        JsonObject jsonObject;
        try {
            JsonReader reader = Json.createReader(new StringReader(jsonText));
            jsonObject = reader.readObject();
            //         System.err.println(jsonText + " #### " + nodeConcept.getConcept().getIdConcept());
            int count = jsonObject.getInt("count");
            if (count > 0) {
                haveCorpus = true;
            }
            return count;
        } catch (Exception e) {
            System.err.println(e + " " + jsonText + " " + nodeConcept.getConcept().getIdConcept());
            return -1;
        }
    }

    public String getMetaData() {
        if (nodeConcept == null || nodeConcept.getConcept() == null || nodeConcept.getConcept().getIdConcept().isEmpty()) {
            return "";
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(connect.getPoolConnexion(),
                nodeConcept.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                "application/ld+json");
        if (datas == null) {
            return "";
        }
        return datas;
    }

    public int getCountOfBranch() {
        return countOfBranch;
    }

    public void setCountOfBranch(int countOfBranch) {
        this.countOfBranch = countOfBranch;
    }

    public int getOffset() {
        return offset;
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
    /*    if(tree != null 
                && CollectionUtils.isNotEmpty(tree.getClickselectedNodes()) 
                && tree.getClickselectedNodes().get(0) != null 
                && tree.getClickselectedNodes().get(0).getData() != null) {*/
        if (tree != null && tree.getSelectedNode() != null && tree.getSelectedNode().getData() != null) {
            RelationsHelper relationsHelper = new RelationsHelper();
          /*  ArrayList<NodeNT> nodeNTs = relationsHelper.getListNT(connect.getPoolConnexion(),
                    ((TreeNodeData) tree.getClickselectedNodes().get(0).getData()).getNodeId(),
                    idTheso,
                    idLang, step+1, offset);*/
            ArrayList<NodeNT> nodeNTs = relationsHelper.getListNT(connect.getPoolConnexion(),
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

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    private void pathOfConcept(String idTheso, String idConcept, String idLang) {
        PathHelper pathHelper = new PathHelper();
        List<Path> paths = pathHelper.getPathOfConcept2(
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
        //pathOfConcept = getPathFromArray(paths);
        pathLabel = pathHelper.getPathWithLabel(connect.getPoolConnexion(), paths, idTheso, idLang, idConcept);
    }

    private void setRoles() {
        if (nodeConcept == null || nodeConcept.getDcElements() == null) {
            creator = null;
            contributors = null;
        }
        contributors = null;
        creator = null;
        boolean firstElement = true;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getContributors() {
        return contributors;
    }

    public void setContributors(String contributors) {
        this.contributors = contributors;
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

    public void changeStateAltLabelOtherLang() {

    }

    public NodeConcept getNodeConcept() {
        return nodeConcept;
    }

    public void setNodeConcept(NodeConcept nodeConcept) {
        this.nodeConcept = nodeConcept;
    }

    public ArrayList<NodePath> getPathLabel() {
        return pathLabel;
    }

    public void setPathLabel(ArrayList<NodePath> pathLabel) {
        this.pathLabel = pathLabel;
    }

    public void actionAfaire(String id) {
        String i = id;
        FacesContext.getCurrentInstance().getExternalContext().getInitParameterMap().get("version");
    }

    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    /////// notes
    public ArrayList<NodeNote> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<NodeNote> notes) {
        this.notes = notes;
    }

    public ArrayList<NodeNote> getScopeNotes() {
        return scopeNotes;
    }

    public void setScopeNotes(ArrayList<NodeNote> scopeNotes) {
        this.scopeNotes = scopeNotes;
    }

    public ArrayList<NodeNote> getChangeNotes() {
        return changeNotes;
    }

    public void setChangeNotes(ArrayList<NodeNote> changeNotes) {
        this.changeNotes = changeNotes;
    }

    public ArrayList<NodeNote> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(ArrayList<NodeNote> definitions) {
        this.definitions = definitions;
    }

    public ArrayList<NodeNote> getEditorialNotes() {
        return editorialNotes;
    }

    public void setEditorialNotes(ArrayList<NodeNote> editorialNotes) {
        this.editorialNotes = editorialNotes;
    }

    public ArrayList<NodeNote> getExamples() {
        return examples;
    }

    public void setExamples(ArrayList<NodeNote> examples) {
        this.examples = examples;
    }

    public ArrayList<NodeNote> getHistoryNotes() {
        return historyNotes;
    }

    public void setHistoryNotes(ArrayList<NodeNote> historyNotes) {
        this.historyNotes = historyNotes;
    }

    public ArrayList<NodeCorpus> getNodeCorpuses() {
        return nodeCorpuses;
    }

    public void setNodeCorpuses(ArrayList<NodeCorpus> nodeCorpuses) {
        this.nodeCorpuses = nodeCorpuses;
    }

    public Map getMapModel() {
        return mapModel;
    }

    public boolean isHaveCorpus() {
        return haveCorpus;
    }

    public void setHaveCorpus(boolean haveCorpus) {
        this.haveCorpus = haveCorpus;
    }

    public boolean isHaveNext() {
        return haveNext;
    }

    public void setHaveNext(boolean haveNext) {
        this.haveNext = haveNext;
    }

    public List<ResponsiveOption> getResponsiveOptions() {
        return responsiveOptions;
    }

    public ArrayList<NodeIdValue> getNodeFacets() {
        return nodeFacets;
    }

    public void setNodeFacets(ArrayList<NodeIdValue> nodeFacets) {
        this.nodeFacets = nodeFacets;
    }

    public boolean isToggleSwitchAltLabelLang() {
        return toggleSwitchAltLabelLang;
    }

    public void setToggleSwitchAltLabelLang(boolean toggleSwitchAltLabelLang) {
        this.toggleSwitchAltLabelLang = toggleSwitchAltLabelLang;
    }

    public boolean isToggleSwitchNotesLang() {
        return toggleSwitchNotesLang;
    }

    public void setToggleSwitchNotesLang(boolean toggleSwitchNotesLang) {
        this.toggleSwitchNotesLang = toggleSwitchNotesLang;
    }

    public ArrayList<NodeCustomRelation> getNodeCustomRelationReciprocals() {
        return nodeCustomRelationReciprocals;
    }

    public void setNodeCustomRelationReciprocals(ArrayList<NodeCustomRelation> nodeCustomRelationReciprocals) {
        this.nodeCustomRelationReciprocals = nodeCustomRelationReciprocals;
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

    public String getGpsModeSelected() {
        return gpsModeSelected;
    }

    public void setGpsModeSelected(String gpsModeSelected) {
        this.gpsModeSelected = gpsModeSelected;
    }

    public Boolean isMapVisible() {
        return ObjectUtils.isNotEmpty(nodeConcept) && CollectionUtils.isNotEmpty(nodeConcept.getNodeGps());
    }
}
