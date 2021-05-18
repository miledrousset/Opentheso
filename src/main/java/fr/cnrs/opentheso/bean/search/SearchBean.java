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
import javax.annotation.PreDestroy;
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
    
    private NodeSearchMini searchSelected;
    private ArrayList<NodeSearchMini> listResultAutoComplete;    
    private ArrayList<NodeConceptSearch> nodeConceptSearchs;
    private String searchValue;
    
    // sert à afficher ou non le total de résultat
    private boolean isSelectedItem;
    private SearchHelper searchHelper;
    private ConceptHelper conceptHelper;
    
    // filter search
    private boolean exactMatch;
    private boolean withNote;
    private boolean withId;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(listResultAutoComplete!= null){
            listResultAutoComplete.clear();
            listResultAutoComplete = null;
        }
        if(nodeConceptSearchs!= null){
            nodeConceptSearchs.clear();
            nodeConceptSearchs = null;
        }        
        searchSelected = null;
        searchValue = null;
        searchHelper = null;
        conceptHelper = null;
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
        if(nodeConceptSearchs != null) {
            for (NodeConceptSearch nodeConceptSearch : nodeConceptSearchs) {
                nodeConceptSearch.clear();
            }
            nodeConceptSearchs = null;
        }
        if(listResultAutoComplete != null)
            listResultAutoComplete.clear();
        searchSelected = null;
    }

    public List<NodeSearchMini> completTermFullText(String value) {

        isSelectedItem = false;
    //    searchSelected = new NodeSearchMini();
        if(searchHelper == null) 
            searchHelper = new SearchHelper();
        
        if(listResultAutoComplete == null) {
            listResultAutoComplete = new ArrayList<>();
        } else 
            listResultAutoComplete.clear();

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
        if(nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else 
            nodeConceptSearchs.clear();        
        if(conceptHelper == null) 
            conceptHelper = new ConceptHelper();
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
        if(nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else 
            nodeConceptSearchs.clear();
        
        if(conceptHelper == null) 
            conceptHelper = new ConceptHelper();
        if(searchHelper == null) 
            searchHelper = new SearchHelper();
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
        

        if(nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if(nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            }
            else {
                setViewsSerach();
                isSelectedItem = false;
            }
        }
    }
    
    /**
     * permet de retourner la liste des concepts qui ont une poly-hiérarchie
     */
    public void getAllPolyierarchy(){
        if(nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else 
            nodeConceptSearchs.clear();
        
        if(conceptHelper == null) 
            conceptHelper = new ConceptHelper();
        if(searchHelper == null) 
            searchHelper = new SearchHelper();        
        
        ArrayList<String> nodeSearchsId = searchHelper.searchAllPolyierarchy(
                connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        
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
        if(nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if(nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            }
            else {
                setViewsSerach();
                isSelectedItem = false;
            }
        }        
    }
    
    /**
     * permet de retourner la liste des concepts qui ont plusieurs Groupes
     */
    public void searchConceptWithMultiGroup(){
        if(nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else 
            nodeConceptSearchs.clear();
        
        if(conceptHelper == null) 
            conceptHelper = new ConceptHelper();
        if(searchHelper == null) 
            searchHelper = new SearchHelper();        
        
        ArrayList<String> nodeSearchsId = searchHelper.searchConceptWithMultiGroup(
                connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        
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
        if(nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if(nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            }
            else {
                setViewsSerach();
                isSelectedItem = false;
            }
        }        
    }    
    
    /**
     * permet de retourner la liste des concepts qui ont plusieurs Groupes
     */
    public void searchConceptWithoutGroup(){
        if(nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else 
            nodeConceptSearchs.clear();
        
        if(conceptHelper == null) 
            conceptHelper = new ConceptHelper();
        if(searchHelper == null) 
            searchHelper = new SearchHelper();        
        
        ArrayList<String> nodeSearchsId = searchHelper.searchConceptWithoutGroup(
                connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        
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
        if(nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if(nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            }
            else {
                setViewsSerach();
                isSelectedItem = false;
            }
        }        
    } 

    /**
     * permet de retourner la liste des concepts qui sont en doublons
     */
    public void searchConceptDuplicated(){
        if(nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else 
            nodeConceptSearchs.clear();
        
        if(conceptHelper == null) 
            conceptHelper = new ConceptHelper();
        if(searchHelper == null) 
            searchHelper = new SearchHelper();        
        
        ArrayList<String> nodeSearchLabels = searchHelper.searchConceptDuplicated(
                connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), 
                selectedTheso.getCurrentLang());
        
        for (String label : nodeSearchLabels) {
            nodeConceptSearchs.addAll(
                conceptHelper.getConceptForSearchFromLabel(
                connect.getPoolConnexion(),
                label,
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang()));
        }
        if(nodeConceptSearchs.size() > 0) {
            onSelectConcept(nodeConceptSearchs.get(0).getIdConcept());
        }
        if(nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if(nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            }
            else {
                setViewsSerach();
                isSelectedItem = false;
            }
        }        
    }    
    
    /**
     * permet de retourner la liste des concepts qui sont une relation RT et NT ou BT
     * ce qui est interdit
     */
    public void searchConceptWithRTandBT(){
        if(nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else 
            nodeConceptSearchs.clear();
        
        if(conceptHelper == null) 
            conceptHelper = new ConceptHelper();
        if(searchHelper == null) 
            searchHelper = new SearchHelper();        
        ArrayList<String> allIdConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        
        ArrayList<String> nodeSearchsId = new ArrayList<>();
        for (String idConcept : allIdConcepts) {
            if(searchHelper.isConceptHaveRTandBT(
                    connect.getPoolConnexion(),
                    idConcept,
                    selectedTheso.getCurrentIdTheso())) {
                nodeSearchsId.add(idConcept);
            }
        }
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
        if(nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if(nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            }
            else {
                setViewsSerach();
                isSelectedItem = false;
            }
        }        
    }    
    
    
    
    
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
