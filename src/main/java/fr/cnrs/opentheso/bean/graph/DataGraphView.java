
package fr.cnrs.opentheso.bean.graph;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */

    
    
@Named
@SessionScoped
public class DataGraphView implements Serializable {

    private List<GraphObject> graphObjects;
    
    private GraphObject selectedGraph;

    @Inject
    private GraphService graphService;

    public void init() {
        graphService.init();
        graphObjects = graphService.getProducts(3);
    }

    public List<GraphObject> getGraphObjetcs() {
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

    private void showMessage(String clientId) {
        FacesContext.getCurrentInstance()
                .addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, clientId + " multiview state has been cleared out", null));
    }
}    

