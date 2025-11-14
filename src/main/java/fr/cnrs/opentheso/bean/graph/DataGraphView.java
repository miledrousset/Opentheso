package fr.cnrs.opentheso.bean.graph;

import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.models.graphs.GraphObject;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.SearchService;
import fr.cnrs.opentheso.services.TermService;
import fr.cnrs.opentheso.services.ThesaurusService;
import fr.cnrs.opentheso.services.graphs.GraphService;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.primefaces.component.chip.Chip;
import org.primefaces.extensions.event.ClipboardErrorEvent;
import org.primefaces.extensions.event.ClipboardSuccessEvent;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.client.utils.URIBuilder;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.QueryConfig;


@Getter
@Setter
@Service
@RequiredArgsConstructor
public class DataGraphView {

    @Value("${app.neo4j.enabled:false}")
    private boolean neo4jEnabled;

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

    private final LanguageBean langueBean;
    private final SelectedTheso selectedTheso;
    private final CurrentUser currentUser;
    private final GraphService graphService;
    private final ThesaurusService thesaurusService;
    private final PreferenceService preferenceService;
    private final TermService termService;
    private final SearchService searchService;

    private int selectedViewId;
    private String selectedViewName, newViewName, newViewDescription, newViewDataToAdd, selectedIdTheso;
    private List<ImmutablePair<String, String>> newViewExportedData;
    private NodeSearchMini searchSelected;
    private GraphObject selectedGraph;
    private List<GraphObject> graphObjects;

    /**
     * permet de retourner la liste des concepts possibles pour ajouter une
     * relation NT (en ignorant les relations interdites) on ignore les concepts
     * de type TT on ignore les concepts de type RT
     */
    public List<NodeSearchMini> getAutoComplete(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        String idLang = preferenceService.getWorkLanguageOfThesaurus(selectedIdTheso);
        
        if (selectedIdTheso != null && idLang != null) {
            liste = searchService.searchAutoCompletionForRelation(value, idLang, selectedIdTheso, true);
        }
        return liste;
    }

    public void init() {

        int idUser = currentUser.getNodeUser().getIdUser();
        graphObjects = new ArrayList<>(graphService.getViews(idUser).values());
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

    public void onSelectThesaurus(AjaxBehaviorEvent e) {
        String idTheso = ((Chip) e.getSource()).getLabel();
        String idLang = preferenceService.getWorkLanguageOfThesaurus(idTheso);
        String nameOfTheso = thesaurusService.getTitleOfThesaurus(idTheso, idLang);
        MessageUtils.showInformationMessage("Thesaurus : " + nameOfTheso);
    }

    // pour afficher la valeur des identifiants de thésaurus et concept
    public void onSelectThesaurusConcept(AjaxBehaviorEvent e) {

        var idValue = ((Chip) e.getSource()).getLabel().split(",");
        var idThesaurus = idValue[0].trim();
        var idConcept = idValue[1].trim();

        var idLang = preferenceService.getWorkLanguageOfThesaurus(idThesaurus);
        var nameOfThesaurus = thesaurusService.getTitleOfThesaurus(idThesaurus, idLang);
        var nameOfConcept = termService.getLexicalValueOfConcept(idConcept, idThesaurus, idLang);

        MessageUtils.showInformationMessage("Thesaurus : " + nameOfThesaurus);
        MessageUtils.showInformationMessage("Concept : " + nameOfConcept);
    }

    public void initEditViewDialog(String id) {

        var viewToEdit = graphService.getView(id);
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

        var view = graphService.getView(viewId);
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
        var uriBuilder = new URIBuilder(baseDataURL);
        uriBuilder.addParameter("lang", "fr");

        if (!view.getExportedData().isEmpty()) {
            view.getExportedData().forEach(data -> {
                String idThesoConcept = data.getRight() == null ? data.getLeft() : data.getLeft() + ":" + data.getRight();
                uriBuilder.addParameter("idThesoConcept", idThesoConcept);
            });
        }

        // Construit l'URL de redirection
        var redirectUrlBuilder = new URIBuilder(opethesoUrl + "/d3js/index.xhtml");
        redirectUrlBuilder.addParameter("dataUrl", uriBuilder.build().toString());
        redirectUrlBuilder.addParameter("format", "opentheso");
        return redirectUrlBuilder.build().toString();
    }

    public void exportToNeo4J(String viewId) {
        if (!neo4jEnabled) {
            MessageUtils.showWarnMessage("Export Neo4J désactivé (app.neo4j.enabled=false)");
            return;
        }

        var properties = getPrefOfNeo4j();
        var view = graphService.getView(viewId);
        if (view == null) {
            return;
        }

        var context = FacesContext.getCurrentInstance().getExternalContext();
        var openthesoUrl = context.getRequestScheme() + "://" + context.getRequestServerName()
                + (Objects.equals(context.getRequestServerName(), "localhost") ? ":" + context.getRequestServerPort() : "")
                + context.getApplicationContextPath();

        var dbUri = "neo4j://" + properties.getProperty("neo4j.serverName") + ":" + properties.getProperty("neo4j.serverPort"); //"neo4j://localhost:7687";
        var dbUser = properties.getProperty("neo4j.user");
        var dbPassword = properties.getProperty("neo4j.password");
        var dbName = properties.getProperty("neo4j.databaseName");

        var thesaurusImportURIWithPlaceholder = openthesoUrl + "/openapi/v1/thesaurus/%THESO_ID%";
        var branchImportURIWithPlaceholder = openthesoUrl + "/openapi/v1/concept/%THESO_ID%/%TOP_CONCEPT_ID%/expansion?way=down";

        try (var driver = GraphDatabase.driver(dbUri, AuthTokens.basic(dbUser, dbPassword))) {
            driver.verifyConnectivity();

            var builder = new StringBuilder();
            view.getExportedData().forEach((data) -> {
                builder.append("CALL n10s.rdf.import.fetch(\""); 
                builder.append(data.right == null
                        ? thesaurusImportURIWithPlaceholder.replace("%THESO_ID%", data.left)
                        : branchImportURIWithPlaceholder.replace("%THESO_ID%", data.left).replace("%TOP_CONCEPT_ID%", data.right));
                builder.append("\", \"RDF/XML\", {headerParams: { Accept: \"application/rdf+xml;charset=utf-8\"}});\n");
            });

            var result = driver.executableQuery("CALL apoc.cypher.runMany('" + builder.toString() + "', {}, {statistics:false,timeout:10})")
                    .withConfig(QueryConfig.builder().withDatabase(dbName).build())
                    .execute();

            List<org.neo4j.driver.Record> records = result.records();
            if (!records.isEmpty()) {
                records.forEach(System.out::println);
            }
            MessageUtils.showInformationMessage("Export réussi vers Neo4J !");
        } catch (Exception e) {
            MessageUtils.showErrorMessage("Erreur de connexion à la base de données Neo4J !");
        }
    }

    public void removeView(String viewId) {
        graphService.deleteView(viewId);
        MessageUtils.showInformationMessage("Vue supprimée avec succès");
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
            MessageUtils.showWarnMessage("Cette combinaison existe déjà !");
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
            MessageUtils.showErrorMessage("Une vue doit possèder un nom et une description");
            return;
        }
        if (selectedViewId == -1) {
            int idUser = currentUser.getNodeUser().getIdUser();
            int newViewId = graphService.createView(new GraphObject(newViewName, newViewDescription, new ArrayList<>()), idUser);
            if(newViewId == -1){
                MessageUtils.showErrorMessage("La création de la vue a échoué");
                init();
                return;
            }
            selectedViewId = newViewId;
            MessageUtils.showInformationMessage("Vue créée avec succès");
        } else {
            graphService.saveView(new GraphObject(selectedViewId, newViewName, newViewDescription, newViewExportedData));
            MessageUtils.showInformationMessage("Vue modifiée avec succès");
        }
        init();
    }

    public void removeExportedDataRow(String left, String right) {
        var optTuple = newViewExportedData.stream().filter(data -> {
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

    public void successListener(final ClipboardSuccessEvent successEvent) {
        MessageUtils.showInformationMessage(langueBean.getMsg("copied"));
    }

    public void errorListener(final ClipboardErrorEvent errorEvent) {
        MessageUtils.showErrorMessage("Component id: " + errorEvent.getComponent().getId() + " Action: " + errorEvent.getAction());
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

}
