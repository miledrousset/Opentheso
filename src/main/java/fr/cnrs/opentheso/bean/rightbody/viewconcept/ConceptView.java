/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import com.jsf2leaf.model.LatLong;
import com.jsf2leaf.model.Layer;
import com.jsf2leaf.model.Map;
import com.jsf2leaf.model.Marker;
import com.jsf2leaf.model.Pulse;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PathHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePath;
import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

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

    private Map mapModel;

    private NodeConcept nodeConcept;
    private String selectedLang;
    
    
    private ArrayList<String> pathOfConcept;
    private ArrayList<NodePath> pathLabel;
    
    private ArrayList<Path> paths; // les hiérarchies du concept
    
    /// pagination
    private int sizeToShowNT;
    
    
    /// Notes concept
    private ArrayList<NodeNote> notes;
    private ArrayList<NodeNote> scopeNotes;  
    
    //// Notes term    
    private ArrayList<NodeNote> changeNotes;
    private ArrayList<NodeNote> definitions;    
    private ArrayList<NodeNote> editorialNotes; 
    private ArrayList<NodeNote> examples;
    private ArrayList<NodeNote> historyNotes;

    /**
     * Creates a new instance of ConceptBean
     */
    public ConceptView() {
    }

    public void init() {
        /*  if(isUriRequest) {
            isUriRequest = false;
            return;
        }*/
        nodeConcept = null;
        selectedLang = null;
        notes = new ArrayList<>();
        scopeNotes = new ArrayList<>();  
        changeNotes = new ArrayList<>();
        definitions = new ArrayList<>();
        editorialNotes = new ArrayList<>();
        examples = new ArrayList<>();
        historyNotes = new ArrayList<>();
        sizeToShowNT = 0;
    }

    /**
     * récuparation des informations pour le concept sélectionné
     *
     * @param idTheso
     * @param idConcept
     * @param idLang
     */
    public void getConcept(String idTheso, String idConcept, String idLang) {
        ConceptHelper conceptHelper = new ConceptHelper();
        nodeConcept = conceptHelper.getConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang);

        if (nodeConcept.getNodeGps() != null) {
            initMap();
        }

        pathOfConcept(idTheso, idConcept, idLang);
        selectedLang = idLang;
        setNotes();
        setSizeToShowNT();
        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();
    }
    
    private void initMap()  {
        
        LatLong place = new LatLong(nodeConcept.getNodeGps().getLatitude()+"", 
            nodeConcept.getNodeGps().getLongitude()+"");
        
        String titre = nodeConcept.getTerm() != null ? nodeConcept.getTerm().getLexical_value() : "";
        
        //Configure Map
        mapModel = new Map();
        mapModel.setWidth("350px");
        mapModel.setHeight("250px");
        mapModel.setCenter(place);
        mapModel.setZoom(13);
        mapModel.setAttribution("OpenTheso");
        mapModel.setMiniMap(false);
        mapModel.setLayerControl(false);
        mapModel.setDraggingEnabled(true);
        mapModel.setZoomEnabled(true);

        //addMarker
        mapModel.addLayer(new Layer().addMarker(new Marker(place, titre, new Pulse(true, 10, "#42A068"))));
    }
    
    private void setSizeToShowNT() {
        // Max 20
        if(nodeConcept.getNodeNT().size() > 20)
            sizeToShowNT = 20;
        else
            sizeToShowNT = nodeConcept.getNodeNT().size();
    }

    public void incrementSizeToShowNT(){
        sizeToShowNT = sizeToShowNT + 20;
        if(sizeToShowNT > nodeConcept.getNodeNT().size())
            sizeToShowNT = nodeConcept.getNodeNT().size();
    }

    public int getSizeToShowNT() {
        return sizeToShowNT;
    }

    public void setSizeToShowNT(int sizeToShowNT) {
        this.sizeToShowNT = sizeToShowNT;
    }
    
    
    private void pathOfConcept(String idTheso, String idConcept, String idLang) {
        paths = new PathHelper().getPathOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);
        //pathOfConcept = getPathFromArray(paths);
        pathLabel = getPathWithLabel(paths, idTheso, idLang, idConcept);
    }

    private ArrayList<NodePath> getPathWithLabel(ArrayList<Path> paths,
                String idTheso, String idLang, String selectedIdConcept) {
        ArrayList<NodePath> pathLabel1 = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        boolean isStartNewPath = true;
        String label;
        
        for (Path path1 : paths) {
            for (String idConcept : path1.getPath()) {
                if(!idConcept.equalsIgnoreCase(selectedIdConcept)) {
                    NodePath nodePath = new NodePath();
                    nodePath.setIdConcept(idConcept);
                    label = conceptHelper.getLexicalValueOfConcept(
                            connect.getPoolConnexion(),
                            idConcept,
                            idTheso, idLang);
                    if(label.isEmpty())
                        label = "("+ idConcept+")";
                    nodePath.setTitle(label);
                    if(isStartNewPath)
                        nodePath.setIsStartOfPath(isStartNewPath);
                    else
                        nodePath.setIsStartOfPath(isStartNewPath);
                    pathLabel1.add(nodePath);
                    isStartNewPath = false;
                }
            }
            isStartNewPath = true;
        }
        return pathLabel1;
    }
    
    
    
/////////////////////////////////
/////////////////////////////////
// fonctions pour les notes /////    
/////////////////////////////////
/////////////////////////////////

    private void setNotes(){
        notes.clear();
        scopeNotes.clear();  
        changeNotes.clear();
        definitions.clear();
        editorialNotes.clear();
        examples.clear();
        historyNotes.clear();
        
        for (NodeNote nodeNote : nodeConcept.getNodeNotesConcept()) {
            switch (nodeNote.getNotetypecode()) {
                case "note" :
                    notes.add(nodeNote);
                    break;
                case "scopeNote" :
                    scopeNotes.add(nodeNote);
                    break;
                case "historyNote" :
                    historyNotes.add(nodeNote);
                    break;                     
            }
        }
        for (NodeNote nodeNote : nodeConcept.getNodeNotesTerm()) {
            switch (nodeNote.getNotetypecode()) {
                case "changeNote" :
                    changeNotes.add(nodeNote);
                    break;
                case "definition" :
                    definitions.add(nodeNote);
                    break;
                case "editorialNote" :
                    editorialNotes.add(nodeNote);
                    break;
                case "example" :
                    examples.add(nodeNote);
                    break; 
            }
        }        
    }
    
    

    public NodeConcept getNodeConcept() {
        return nodeConcept;
    }

    public void setNodeConcept(NodeConcept nodeConcept) {
        this.nodeConcept = nodeConcept;
    }

    public ArrayList<String> getPathOfConcept() {
        return pathOfConcept;
    }

    public void setPathOfConcept(ArrayList<String> pathOfConcept) {
        this.pathOfConcept = pathOfConcept;
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

    public Map getMapModel() {
        return mapModel;
    }
}
