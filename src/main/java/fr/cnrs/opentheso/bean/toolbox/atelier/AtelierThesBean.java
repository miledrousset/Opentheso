package fr.cnrs.opentheso.bean.toolbox.atelier;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import javax.inject.Named;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;


@Named("atelierThesBean")
@ViewScoped
public class AtelierThesBean implements Serializable {
    
    @Inject
    private AtelierThesService atelierThesService;

    private List<String> titles;
    private List<List<String>> values;
    
    private NodeIdValue thesoSelected;
    private ArrayList<NodeIdValue> nodeListTheso;
    private ArrayList<ConceptResultNode> result;
    
    private int spanTable;
    private String delimiterCsv = ";";
    private String selectedColumn;


    public void init() {
        titles = new ArrayList<>();
        values = new ArrayList<>();
        result = new ArrayList<>();
        nodeListTheso = atelierThesService.searchAllThesaurus();
    }
    
    public void comparer() {
        int position = titles.indexOf(selectedColumn);
        result = atelierThesService.comparer(values, position, thesoSelected);
    }

    public void loadFileCsv(FileUploadEvent event) {
        List<List<String>> temp = atelierThesService.loadCsvFile(event, delimiterCsv);
        if (!CollectionUtils.isEmpty(temp)) {
            titles = temp.get(0);
            for (int i = 1; i < temp.size(); i++) {
                values.add(temp.get(i));
            }
            spanTable = 12 / titles.size();
        }
    }

    public List<List<String>> getValues() {
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

    public ArrayList<ConceptResultNode> getResult() {
        return result;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }
    
    public int getSpanTable() {
        return spanTable;
    }

    public String getSelectedColumn() {
        return selectedColumn;
    }

    public void setSelectedColumn(String selectedColumn) {
        this.selectedColumn = selectedColumn;
    }
    
}
