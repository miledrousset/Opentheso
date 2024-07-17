package fr.cnrs.opentheso.bean.graph;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.base.Sys;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.EagerResult;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.QueryConfig;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named
@SessionScoped
public class DataGraphView implements Serializable {

    @Inject
    private Connect connect;
    private List<GraphObject> graphObjects;

    private GraphObject selectedGraph;

    @Inject
    private GraphService graphService;

    private int selectedViewId;
    private String newViewName;
    private String newViewDescription;
    private String newViewDataToAdd;
    private List<ImmutablePair<String, String>> newViewExportedData;

    public int getSelectedViewId() {
        return selectedViewId;
    }

    public void setSelectedViewId(int selectedViewId) {
        this.selectedViewId = selectedViewId;
    }

    public String getNewViewDataToAdd() {
        return newViewDataToAdd;
    }

    public void setNewViewDataToAdd(String newViewDataToAdd) {
        this.newViewDataToAdd = newViewDataToAdd;
    }

    public String getNewViewName() {
        return newViewName;
    }

    public void setNewViewName(String newViewName) {
        this.newViewName = newViewName;
    }

    public String getNewViewDescription() {
        return newViewDescription;
    }

    public void setNewViewDescription(String newViewDescription) {
        this.newViewDescription = newViewDescription;
    }

    public List<ImmutablePair<String, String>> getNewViewExportedData() {
        return newViewExportedData;
    }

    public void setNewViewExportedData(List<ImmutablePair<String, String>> newViewExportedData) {
        this.newViewExportedData = newViewExportedData;
    }

    public void init() {
        graphObjects = new ArrayList<>(graphService.getViews().values());
    }

    public void initNewViewDialog() {
        selectedViewId = -1;
        newViewName = null;
        newViewDescription = null;
        newViewDataToAdd = null;
        newViewExportedData = new ArrayList<>();
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

    public List<GraphObject> getGraphObjects() {
        return graphObjects;
    }

    public void setService(GraphService graphService) {
        this.graphService = graphService;
    }

    public GraphObject getSelectedGraphObject() {
        return selectedGraph;
    }

    public void setSelectedProduct(GraphObject selectedGraph) {
        this.selectedGraph = selectedGraph;
    }

    public void clearMultiViewState() {
        FacesContext context = FacesContext.getCurrentInstance();
        String viewId = context.getViewRoot().getViewId();
        PrimeFaces.current().multiViewState().clearAll(viewId, true, this::showMessage);
    }

    public void redirectToGraphVisualization(String viewId) throws IOException {
        System.out.println("visualisation " + viewId);
        GraphObject view = graphService.getView(viewId);

        if (view == null) {
            return;
        }
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();

        boolean useOT = true;

        String opethesoUrl = context.getRequestScheme() + "://" + context.getRequestServerName()
                + (Objects.equals(context.getRequestServerName(), "localhost") ? ":" + context.getRequestServerPort() : "")
                + context.getApplicationContextPath();

        final String baseDataURL = useOT ? opethesoUrl + "/openapi/v1/graph/getData " : "http://localhost:3334/getJson";
        UriBuilder url = UriBuilder.fromUri(baseDataURL);
        url.queryParam("lang", "fr");
        if (!view.getExportedData().isEmpty()) {
            view.getExportedData().forEach(data -> {
                url.queryParam("idThesoConcept", data.getRight() == null ? data.getLeft() : data.getLeft() + "," + data.getRight());
            });
        }

        String urlString = url.build().toString();
        context.redirect(UriBuilder.fromUri(context.getRequestContextPath() + "/d3js/index.html").queryParam("dataUrl", urlString).queryParam("format", "opentheso").build().toString());
    }

    public void exportToNeo4J(String viewId) {
        System.out.println("export " + viewId);
        GraphObject view = graphService.getView(viewId);
        if (view == null) {
            return;
        }
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        //       String openthesoUrl = "http://localhost:8080/opentheso2";
        String openthesoUrl = context.getRequestScheme() + "://" + context.getRequestServerName()
                + (Objects.equals(context.getRequestServerName(), "localhost") ? ":" + context.getRequestServerPort() : "")
                + context.getApplicationContextPath();

        final String dbUri = "neo4j://localhost:7687";
        final String dbUser = "neo4j";
        final String dbPassword = "neo4j1234";
        final String dbName = "neo4j"; //TODO mettre Neo4j avant de commit

        final String thesaurusImportURIWithPlaceholder = openthesoUrl + "/openapi/v1/thesaurus/%THESO_ID%";
        final String branchImportURIWithPlaceholder = openthesoUrl + "/openapi/v1/concept/%THESO_ID%/%TOP_CONCEPT_ID%/expansion?way=down";

        try (var driver = GraphDatabase.driver(dbUri, AuthTokens.basic(dbUser, dbPassword))) {
            driver.verifyConnectivity();
            System.out.println("Connection estabilished.");

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

            System.out.println(builder);

            EagerResult result = driver.executableQuery("CALL apoc.cypher.runMany('" + builder.toString() + "', {}, {statistics:false,timeout:10})")
                    .withConfig(QueryConfig.builder().withDatabase(dbName).build())
                    .execute();

            List<org.neo4j.driver.Record> records = result.records();

            if (!records.isEmpty()) {
                records.forEach(System.out::println);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeView(String viewId) {
        System.out.println("suppression " + viewId);
        graphService.deleteView(viewId);
        showMessage(FacesMessage.SEVERITY_INFO, "Vue supprimée avec succès");
        init();
    }

    public void addDataToNewViewList() {
        if (selectedViewId == -1) {
            return;
        }
        String[] splittedData = newViewDataToAdd.split(",");
        ImmutablePair<String, String> tuple;
        if (splittedData.length == 0) {
            return;
        }
        if (splittedData.length == 1 || splittedData.length == 2) {
            tuple = new ImmutablePair<>(splittedData[0], splittedData.length == 2 ? splittedData[1] : null);
            graphService.addDataToView(selectedViewId, tuple);
            newViewExportedData.add(tuple);
        }
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

    private void showMessage(String clientId) {
        FacesContext.getCurrentInstance()
                .addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, clientId + " multiview state has been cleared out", null));
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces.current().ajax().update("messageIndex");
    }

}
