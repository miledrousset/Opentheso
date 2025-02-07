package fr.cnrs.opentheso.bean.graph;

import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.models.search.NodeSearchMini;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import fr.cnrs.opentheso.models.graphs.GraphObject;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.services.graphs.GraphService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;
import lombok.Data;
import org.primefaces.component.chip.Chip;
import org.primefaces.extensions.event.ClipboardErrorEvent;
import org.primefaces.extensions.event.ClipboardSuccessEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.EagerResult;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.QueryConfig;
import org.primefaces.PrimeFaces;
import org.apache.http.client.utils.URIBuilder;
import java.net.URISyntaxException;

import jakarta.servlet.http.HttpServletRequest;


/**
 *
 * @author miledrousset
 */
@Data
@Named
@SessionScoped
public class DataGraphView implements Serializable {


    private final SelectedTheso selectedTheso;
    @Autowired @Lazy
    private GraphService graphService;

    @Autowired
    private PreferencesHelper preferencesHelper;

    @Autowired
    private SearchHelper searchHelper;

    @Value("${neo4j.serverName}")
    private String serverNameNeo4j;

    @Value("${neo4j.serverPort}")
    private String serverPortNeo4j;

    @Value("${neo4j.user}")
    private String userNeo4j;

    @Value("${neo4j.password}")
    private String passwordNeo4j;

    @Value("${neo4j.databaseName}")
    private String databaseNameNeo4j;

    private List<GraphObject> graphObjects;

    private GraphObject selectedGraph;

    private int selectedViewId;
    private String selectedViewName;

    private String newViewName;
    private String newViewDescription;
    private String newViewDataToAdd;
    private List<ImmutablePair<String, String>> newViewExportedData;
    
    private String selectedIdTheso;
    private NodeSearchMini searchSelected;
    @Autowired
    private ThesaurusHelper thesaurusHelper;
    @Autowired
    private ConceptHelper conceptHelper;
    @Autowired
    private LanguageBean langueBean;

    @jakarta.inject.Inject
    public DataGraphView(@Named("selectedTheso") SelectedTheso selectedTheso) {
        this.selectedTheso = selectedTheso;
    }


    private Properties getPrefOfNeo4j(){

        var props = new Properties();
        props.setProperty("neo4j.serverName", serverNameNeo4j);
        props.setProperty("neo4j.serverPort", serverPortNeo4j);
        props.setProperty("neo4j.user", userNeo4j);
        props.setProperty("neo4j.password", passwordNeo4j);
        props.setProperty("neo4j.databaseName", databaseNameNeo4j);
        return props;
    }

    /**
     * permet de retourner la liste des concepts possibles pour ajouter une
     * relation NT (en ignorant les relations interdites) on ignore les concepts
     * de type TT on ignore les concepts de type RT
     */
    public List<NodeSearchMini> getAutoComplete(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        String idLang = preferencesHelper.getWorkLanguageOfTheso(selectedIdTheso);
        
        if (selectedIdTheso != null && idLang != null) {
            liste = searchHelper.searchAutoCompletionForRelation(value, idLang, selectedIdTheso, true);
        }
        return liste;
    }

    public void init() {
        graphObjects = new ArrayList<>(graphService.getViews().values());
        selectedIdTheso = null;
        searchSelected = null;
    }

    public void initNewViewDialog() {
        selectedViewId = -1;
        selectedViewName = null;
        newViewName = null;
        newViewDescription = null;
        newViewDataToAdd = null;
        newViewExportedData = new ArrayList<>();
    }

    // pour afficher la valeur des identifiants de thésaurus et concept
    public void onSelectTheso(AjaxBehaviorEvent e) {
        String idTheso = ((Chip) e.getSource()).getLabel();
        String idLang = preferencesHelper.getWorkLanguageOfTheso(idTheso);
        String nameOfTheso = thesaurusHelper.getTitleOfThesaurus(idTheso, idLang);
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Thesaurus", nameOfTheso);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    // pour afficher la valeur des identifiants de thésaurus et concept
    public void onSelectThesoConcept(AjaxBehaviorEvent e) {
        String idThesoConcept = ((Chip) e.getSource()).getLabel();
        String[] idValue = idThesoConcept.split(",");

        String idTheso = idValue[0].trim();
        String idConcept = idValue[1].trim();

        String idLang = preferencesHelper.getWorkLanguageOfTheso(idTheso);
        String nameOfTheso = thesaurusHelper.getTitleOfThesaurus(idTheso, idLang);
        String nameOfConcept = conceptHelper.getLexicalValueOfConcept(idConcept,idTheso,idLang);
        FacesMessage message1 = new FacesMessage(FacesMessage.SEVERITY_INFO, "Thesaurus", nameOfTheso);
        FacesMessage message2 = new FacesMessage(FacesMessage.SEVERITY_INFO, "Concept", nameOfConcept);
        FacesContext.getCurrentInstance().addMessage(null, message1);
        FacesContext.getCurrentInstance().addMessage(null, message2);
    }

    public void initEditViewDialog(String id) {
        GraphObject viewToEdit = graphService.getView(id);
        if (viewToEdit == null) {
            return;
        }
        selectedViewId = viewToEdit.getId();
        newViewName = viewToEdit.getName();
        newViewDescription = viewToEdit.getDescription();
        newViewDataToAdd = null;
        newViewExportedData = viewToEdit.getExportedData();
    }

    public String generateGraphVisualizationUrl(String viewId) throws URISyntaxException {
        GraphObject view = graphService.getView(viewId);

        if (view == null) {
            return null;
        }
        var facesContext = FacesContext.getCurrentInstance();
        var request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        var protocol = request.isSecure() ? "https://" : "http://";
        var host = request.getHeader("host");
        var contextPath = request.getContextPath();
        String opethesoUrl = protocol + host + contextPath;
        final String baseDataURL = opethesoUrl + "/openapi/v1/graph/getData";

        // Utilisation de URIBuilder pour construire l'URL
        URIBuilder uriBuilder = new URIBuilder(baseDataURL);
        uriBuilder.addParameter("lang", "fr");

        if (!view.getExportedData().isEmpty()) {
            view.getExportedData().forEach(data -> {
                String idThesoConcept = data.getRight() == null ? data.getLeft() : data.getLeft() + ":" + data.getRight();
                uriBuilder.addParameter("idThesoConcept", idThesoConcept);
            });
        }

        String urlString = uriBuilder.build().toString();

        // Construit l'URL de redirection
        URIBuilder redirectUrlBuilder = new URIBuilder(opethesoUrl + "/d3js/index.xhtml");

        redirectUrlBuilder.addParameter("dataUrl", urlString);

        redirectUrlBuilder.addParameter("format", "opentheso");
        return redirectUrlBuilder.build().toString();
    }

    public void exportToNeo4J(String viewId) {
        
        Properties properties = getPrefOfNeo4j();
        if(properties == null){
            showMessage(FacesMessage.SEVERITY_ERROR, "La base de données Neo4J n'est pas paramétrée, ouvrez hikari.properties et faire le changement !");
            return;
        }
        
        GraphObject view = graphService.getView(viewId);
        if (view == null) {
            return;
        }
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        //       String openthesoUrl = "http://localhost:8080/opentheso2";
        String openthesoUrl = context.getRequestScheme() + "://" + context.getRequestServerName()
                + (Objects.equals(context.getRequestServerName(), "localhost") ? ":" + context.getRequestServerPort() : "")
                + context.getApplicationContextPath();

        final String dbUri = "neo4j://" + properties.getProperty("neo4j.serverName") + ":" + properties.getProperty("neo4j.serverPort"); //"neo4j://localhost:7687";
        final String dbUser = properties.getProperty("neo4j.user");//"neo4j";
        final String dbPassword = properties.getProperty("neo4j.password");//"neo4j1234";
        final String dbName = properties.getProperty("neo4j.databaseName");//"neo4j"; //TODO mettre Neo4j avant de commit

        final String thesaurusImportURIWithPlaceholder = openthesoUrl + "/openapi/v1/thesaurus/%THESO_ID%";
        final String branchImportURIWithPlaceholder = openthesoUrl + "/openapi/v1/concept/%THESO_ID%/%TOP_CONCEPT_ID%/expansion?way=down";

        try (var driver = GraphDatabase.driver(dbUri, AuthTokens.basic(dbUser, dbPassword))) {
            driver.verifyConnectivity();

            StringBuilder builder = new StringBuilder();
            view.getExportedData().forEach((data) -> {

                String importURL;
                if (data.right == null) {
                    importURL = thesaurusImportURIWithPlaceholder.replace("%THESO_ID%", data.left);
                } else {
                    importURL = branchImportURIWithPlaceholder.replace("%THESO_ID%", data.left).replace("%TOP_CONCEPT_ID%", data.right);
                }

                builder.append("CALL n10s.rdf.import.fetch(\""); 
                builder.append(importURL);
                builder.append("\", \"RDF/XML\", {headerParams: { Accept: \"application/rdf+xml;charset=utf-8\"}});\n");
            });

           // System.out.println(builder);

            EagerResult result = driver.executableQuery("CALL apoc.cypher.runMany('" + builder.toString() + "', {}, {statistics:false,timeout:10})")
                    .withConfig(QueryConfig.builder().withDatabase(dbName).build())
                    .execute();

            List<org.neo4j.driver.Record> records = result.records();

            if (!records.isEmpty()) {
                records.forEach(System.out::println);
            }
            showMessage(FacesMessage.SEVERITY_INFO, "Export réussi vers Neo4J !");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de connexion à la base de données Neo4J !");
            e.printStackTrace();
        }
    }

    public void removeView(String viewId) {
        graphService.deleteView(viewId);
        showMessage(FacesMessage.SEVERITY_INFO, "Vue supprimée avec succès");
        init();
    }

    public void addDataToNewViewList() {
        if (selectedViewId == -1) {
            return;
        }

        ImmutablePair<String, String> tuple;
        if(StringUtils.isEmpty(selectedIdTheso))return;

        String idConcept;
        if(searchSelected == null || StringUtils.isEmpty(searchSelected.getIdConcept())) {
            idConcept = null;
        } else {
            idConcept = searchSelected.getIdConcept();
        }
        
        tuple = new ImmutablePair<>(selectedIdTheso, idConcept);
        
        if(graphService.isExistDatas(selectedViewId, selectedIdTheso, idConcept)){
            showMessage(FacesMessage.SEVERITY_WARN, "Cette combinaison existe déjà !");
            init();
            return;
        }
        
        graphService.addDataToView(selectedViewId, tuple);
        newViewExportedData.add(tuple);
        newViewDataToAdd = null;
        init();
    }

    public void applyView() {
        if (newViewName.isEmpty() || newViewDescription.isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Une vue doit possèder un nom et une description");
            return;
        }
        if (selectedViewId == -1) {
            int newViewId = graphService.createView(new GraphObject(newViewName, newViewDescription, new ArrayList<>()));
            if(newViewId == -1){
                showMessage(FacesMessage.SEVERITY_ERROR, "La création de la vue a échoué");
                init();
                return;
            }
            selectedViewId = newViewId;
            showMessage(FacesMessage.SEVERITY_INFO, "Vue créée avec succès");
        } else {
            graphService.saveView(new GraphObject(selectedViewId, newViewName, newViewDescription, newViewExportedData));
            showMessage(FacesMessage.SEVERITY_INFO, "Vue modifiée avec succès");
        }
        init();
    }

    public void removeExportedDataRow(String left, String right) {
        Optional<ImmutablePair<String, String>> optTuple = newViewExportedData.stream().filter(data -> {
            if (data.right == null) {
                return data.left.equals(left);
            } else {
                return data.right.equals(right) && data.left.equals(left);
            }
        }).findFirst();
        optTuple.ifPresent(tuple -> {
            graphService.removeDataFromView(selectedViewId, tuple);
            newViewExportedData.remove(tuple);
        });
        init();
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces.current().ajax().update("messageIndex");
    }


    public void successListener(final ClipboardSuccessEvent successEvent) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "",
                langueBean.getMsg("copied"));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void errorListener(final ClipboardErrorEvent errorEvent) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Component id: " + errorEvent.getComponent().getId() + " Action: " + errorEvent.getAction());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

}
