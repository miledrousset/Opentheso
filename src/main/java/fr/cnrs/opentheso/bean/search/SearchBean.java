/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.search;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptSearch;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "searchBean")
@SessionScoped
public class SearchBean implements Serializable {

    @Inject private Connect connect;
    @Inject private SelectedTheso selectedTheso;
    @Inject private RightBodySetting rightBodySetting;
    @Inject private LeftBodySetting leftBodySetting;
    @Inject private ConceptView conceptBean;    
    
 
    
    NodeSearchMini searchSelected;
    
    ArrayList<NodeSearchMini> listResultAutoComplete;    
    ArrayList<NodeConceptSearch> nodeConceptSearchs;
    
    private String searchValue;
    
    // sert à afficher ou non le total de résultat
    private boolean isSelectedItem;
    
    
    
    // filter search
    private boolean exactMatch;
    private boolean withNote;
    private boolean withId;
    
    /**
     * Creates a new instance of SearchBean
     */
    public SearchBean() {
//        nodeConceptSearchs = new ArrayList<>();
    }
    
    public void activateExactMatch(){
        withId = false;
        withNote = false;
    }
    public void activateWithNote(){
        withId = false;
        exactMatch = false;
    }
    public void activateWithId(){
        withNote = false;
        exactMatch = false;
    }     
    
    public void reset(){
        searchSelected = null;
        nodeConceptSearchs = null;//new ArrayList<>();
    }
    
    public List<NodeSearchMini> completTermFullText(String value) {

        isSelectedItem = false;
        searchSelected = new NodeSearchMini();
        SearchHelper searchHelper = new SearchHelper();
        listResultAutoComplete = new ArrayList<>();
        if(selectedTheso == null) return listResultAutoComplete;
        
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            if(exactMatch) {
                listResultAutoComplete = searchHelper.searchExactMatch(connect.getPoolConnexion(),
                            value,
                            selectedTheso.getCurrentLang(),
                            selectedTheso.getCurrentIdTheso());
            }
            else {
                if(!withId && !withNote) {
                    listResultAutoComplete = searchHelper.searchFullTextElastic(connect.getPoolConnexion(),
                                value,
                                selectedTheso.getCurrentLang(),
                                selectedTheso.getCurrentIdTheso());
                }
            }
        }
        searchValue = value;
        if(listResultAutoComplete == null) 
            listResultAutoComplete = new ArrayList<>();
        return listResultAutoComplete;
    }    

    public void onSelect(){
        nodeConceptSearchs = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        nodeConceptSearchs.add(
                    conceptHelper.getConceptForSearch(
                    connect.getPoolConnexion(),
                    searchSelected.getIdConcept(),
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang())
            );
        setViewsSerach();
        if(nodeConceptSearchs.size() == 1) {
            onSelectConcept(nodeConceptSearchs.get(0).getIdConcept());
        }
        isSelectedItem = true;
    }
    
    
    /**
     * recherche de la valeur saisie en respectant les filtres 
     */
    public void applySearch(){
   //     if(listResultAutoComplete == null) return;
        nodeConceptSearchs = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        SearchHelper searchHelper = new SearchHelper();
        ArrayList<String> nodeSearchsId;
        
        if(withId) {
            nodeSearchsId = searchHelper.searchForIds(connect.getPoolConnexion(),
                    searchValue, selectedTheso.getCurrentIdTheso());
            for (String idConcept : nodeSearchsId) {
                nodeConceptSearchs.add(0,
                    conceptHelper.getConceptForSearch(
                    connect.getPoolConnexion(),
                    idConcept,
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang()));
            }
            if(nodeConceptSearchs.size() > 0) {
                onSelectConcept(nodeConceptSearchs.get(0).getIdConcept());
            }
        }

        if(withNote) {
            nodeSearchsId = searchHelper.searchIdConceptFromNotes(connect.getPoolConnexion(),
                    searchValue, selectedTheso.getCurrentLang(),
                    selectedTheso.getCurrentIdTheso()
                    );
            
            for (String idConcept : nodeSearchsId) {
                nodeConceptSearchs.add(
                    conceptHelper.getConceptForSearch(
                    connect.getPoolConnexion(),
                    idConcept,
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang()));
            }
            if(nodeConceptSearchs.size() > 0) {
                onSelectConcept(nodeConceptSearchs.get(0).getIdConcept());
            }            
         
        }
        
        if(exactMatch) {
            ArrayList<NodeSearchMini> nodeSearchMini = searchHelper.searchExactMatch(connect.getPoolConnexion(),
                    searchValue, selectedTheso.getCurrentLang(),
                    selectedTheso.getCurrentIdTheso()
                    );
            for (NodeSearchMini nodeSearchMini1 : nodeSearchMini) {
                nodeConceptSearchs.add(
                    conceptHelper.getConceptForSearch(
                    connect.getPoolConnexion(),
                    nodeSearchMini1.getIdConcept(),
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang()));
            }
            if(nodeConceptSearchs.size() > 0) {
                onSelectConcept(nodeConceptSearchs.get(0).getIdConcept());
            }
        }
        
        if(!withId && !withNote && !exactMatch) {
            ArrayList<String> nodeSearchMinis = searchHelper.searchFullTextId(
                    connect.getPoolConnexion(),
                    searchValue, selectedTheso.getCurrentLang(),
                    selectedTheso.getCurrentIdTheso()
                    );            
            for (String nodeSearchMini : nodeSearchMinis) {
                nodeConceptSearchs.add(
                        conceptHelper.getConceptForSearch(
                        connect.getPoolConnexion(),
                        nodeSearchMini,
                        selectedTheso.getCurrentIdTheso(),
                        selectedTheso.getCurrentLang())
                );
            }
            if(nodeConceptSearchs.size() > 0) {
                onSelectConcept(nodeConceptSearchs.get(0).getIdConcept());
            }
        }
        
        setViewsSerach();
        isSelectedItem = false;
    }    
    
/*    public void applySearch(){
        if(listResultAutoComplete == null) return;
        nodeConceptSearchs = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        SearchHelper searchHelper = new SearchHelper();
        ArrayList<String> nodeSearchsId;
        
        if(withId) {
            nodeSearchsId = searchHelper.searchForIds(connect.getPoolConnexion(),
                    searchValue, selectedTheso.getCurrentIdTheso());
            for (String idConcept : nodeSearchsId) {
                nodeConceptSearchs.add(0,
                    conceptHelper.getConceptForSearch(
                    connect.getPoolConnexion(),
                    idConcept,
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang()));
            }
        }

        if(withNote) {
            nodeSearchsId = searchHelper.searchIdConceptFromNotes(connect.getPoolConnexion(),
                    searchValue, selectedTheso.getCurrentLang(),
                    selectedTheso.getCurrentIdTheso()
                    );
            
            for (String idConcept : nodeSearchsId) {
                nodeConceptSearchs.add(
                    conceptHelper.getConceptForSearch(
                    connect.getPoolConnexion(),
                    idConcept,
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang()));
            }
        }
        
        if(exactMatch) {
            ArrayList<NodeSearchMini> nodeSearchMini = searchHelper.searchExactMatch(connect.getPoolConnexion(),
                    searchValue, selectedTheso.getCurrentLang(),
                    selectedTheso.getCurrentIdTheso()
                    );
            for (NodeSearchMini nodeSearchMini1 : nodeSearchMini) {
                nodeConceptSearchs.add(
                    conceptHelper.getConceptForSearch(
                    connect.getPoolConnexion(),
                    nodeSearchMini1.getIdConcept(),
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang()));
            }
        }
        
        if(!withId && !withNote && !exactMatch) {
            for (NodeSearchMini nodeSearchMini : listResultAutoComplete) {
                nodeConceptSearchs.add(
                        conceptHelper.getConceptForSearch(
                        connect.getPoolConnexion(),
                        nodeSearchMini.getIdConcept(),
                        selectedTheso.getCurrentIdTheso(),
                        selectedTheso.getCurrentLang())
                );
            }      
        }
        
        setViewsSerach();
        isSelectedItem = false;
    }*/
    
    private void setViewsSerach(){
        rightBodySetting.setShowResultSearchToOn();
        leftBodySetting.setIndex("0"); // vue Filtre de recherche
        rightBodySetting.setIndex("2");
    }
    private void setViewsConcept(){
        rightBodySetting.setShowResultSearchToOn();
        leftBodySetting.setIndex("0"); // vue Filtre de recherche
        rightBodySetting.setIndex("0");
    }    
    
    public void onSelectConcept(String id){
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                id, selectedTheso.getCurrentLang());
        rightBodySetting.setShowConceptToOn();
        rightBodySetting.setIndex("0");
    }

    public NodeSearchMini getSearchSelected() {
        return searchSelected;
    }

    public void setSearchSelected(NodeSearchMini searchSelected) {
        this.searchSelected = searchSelected;
    }

    public ArrayList<NodeConceptSearch> getNodeConceptSearchs() {
        return nodeConceptSearchs;
    }

    public void setNodeConceptSearchs(ArrayList<NodeConceptSearch> nodeConceptSearchs) {
        this.nodeConceptSearchs = nodeConceptSearchs;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public boolean isIsSelectedItem() {
        return isSelectedItem;
    }

    public void setIsSelectedItem(boolean isSelectedItem) {
        this.isSelectedItem = isSelectedItem;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public boolean isWithNote() {
        return withNote;
    }

    public void setWithNote(boolean withNote) {
        this.withNote = withNote;
    }

    public boolean isWithId() {
        return withId;
    }

    public void setWithId(boolean withId) {
        this.withId = withId;
    }

    

}
