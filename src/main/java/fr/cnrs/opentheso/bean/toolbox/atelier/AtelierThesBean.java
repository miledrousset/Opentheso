package fr.cnrs.opentheso.bean.toolbox.atelier;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import javax.inject.Named;
import javax.inject.Inject;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.RowEditEvent;


@Named("atelierThesBean")
@ViewScoped
public class AtelierThesBean implements Serializable {
    
    @Inject
    private AtelierThesService atelierThesService;

    private List<Data> values;
    
    private NodeIdValue thesoSelected;
    private ArrayList<NodeIdValue> nodeListTheso;
    private ArrayList<ConceptResultNode> result;
    
    private boolean skip;
    private Integer progressValue, progressStep;
    private String delimiterCsv = ";";


    public void init() {
        values = new ArrayList<>();
        result = new ArrayList<>();
        nodeListTheso = atelierThesService.searchAllThesaurus();
    }
    
    public void comparer() {
        //progressValue = 0;
        //progressStep = (int) 100/values.size();
        result = atelierThesService.comparer(values, thesoSelected);
        System.out.println(">> Final : " + result.size());
    }

    public String onFlowProcess(FlowEvent event) {
        if (skip) {
            skip = false;   //reset in case user goes back
            return "confirm";
        } else {
            return event.getNewStep();
        }
    }

    public void loadFileCsv(FileUploadEvent event) {
        values = atelierThesService.loadCsvFile(event, delimiterCsv);
    }

    public List<Data> getValues() {
        return values;
    }

    public void onRowEdit(RowEditEvent<Data> event) {
        FacesMessage msg = new FacesMessage("Car Edited", event.getObject().getId());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowCancel(RowEditEvent<Data> event) {
        FacesMessage msg = new FacesMessage("Edit Cancelled", event.getObject().getId());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onAddNew() {
        Data newLigne = new Data();
        values.add(newLigne);
    }

    public String getDelimiterCsv() {
        return delimiterCsv;
    }

    public void setDelimiterCsv(String delimiterCsv) {
        this.delimiterCsv = delimiterCsv;
    }

    public ArrayList<NodeIdValue> getNodeListTheso() {
        return nodeListTheso;
    }

    public void setNodeListTheso(ArrayList<NodeIdValue> nodeListTheso) {
        this.nodeListTheso = nodeListTheso;
    }

    public NodeIdValue getThesoSelected() {
        return thesoSelected;
    }

    public void setThesoSelected(NodeIdValue thesoSelected) {
        this.thesoSelected = thesoSelected;
    }

    public Integer getProgressValue() {
        return progressValue;
    }

    public void setProgressValue(Integer progressValue) {
        this.progressValue = progressValue;
    }

    public Integer getProgressStep() {
        return progressStep;
    }

    public void setProgressStep(Integer progressStep) {
        this.progressStep = progressStep;
    }

    public ArrayList<ConceptResultNode> getResult() {
        return result;
    }
    
}
