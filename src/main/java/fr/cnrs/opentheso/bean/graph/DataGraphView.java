
package fr.cnrs.opentheso.bean.graph;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.jena.base.Sys;
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

    public void init() {
        graphObjects = new ArrayList<>(graphService.getViews().values());
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
        System.out.println("visualisation "+viewId);
        GraphObject view = graphService.getView(viewId);

        if(view == null) return;
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();

        boolean useOT = true;

        String opethesoUrl = context.getRequestScheme() + "://" + context.getRequestServerName() + (Objects.equals(context.getRequestServerName(), "localhost") ? ":" + context.getRequestServerPort() : "");

        final String baseDataURL = useOT ? opethesoUrl +"/opentheso2/openapi/v1/graph/getData ": "http://localhost:3334/getJson";
        UriBuilder url = UriBuilder.fromUri(baseDataURL);
        url.queryParam("lang", "fr");
        if (!view.getExportedData().isEmpty()) {
            view.getExportedData().forEach(data -> {
                url.queryParam("idThesoConcept", data.getRight() == null ? data.getLeft() : data.getLeft() + "," + data.getRight());
            });
        }

        String urlString = url.build().toString();
        context.redirect(UriBuilder.fromUri(context.getRequestContextPath()  + "/d3js/index.html").queryParam("dataUrl", urlString).queryParam("format", "opentheso").build().toString());
    }

    public void exportToNeo4J(String viewId){
        System.out.println("export "+viewId);
        GraphObject view = graphService.getView(viewId);
    }

    private void showMessage(String clientId) {
        FacesContext.getCurrentInstance()
                .addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, clientId + " multiview state has been cleared out", null));
    }
}    

