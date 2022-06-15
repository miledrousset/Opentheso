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
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "searchBean")
@SessionScoped
public class SearchBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private RightBodySetting rightBodySetting;
    @Inject
    private LeftBodySetting leftBodySetting;
    @Inject
    private ConceptView conceptBean;
    @Inject
    private IndexSetting indexSetting;
    @Inject
    private TreeGroups treeGroups;
    @Inject
    private RoleOnThesoBean roleOnThesoBean;

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
    private boolean indexMatch;
    private boolean withNote;
    private boolean withId;

    private boolean searchResultVisible;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (listResultAutoComplete != null) {
            listResultAutoComplete.clear();
            listResultAutoComplete = null;
        }
        if (nodeConceptSearchs != null) {
            nodeConceptSearchs.clear();
            nodeConceptSearchs = null;
        }
        searchSelected = null;
        searchValue = null;
        searchHelper = null;
        conceptHelper = null;

        searchResultVisible = false;
    }

    public void activateIndexMatch() {
        exactMatch = false;
        withId = false;
        withNote = false;
    }

    public void activateExactMatch() {
        withId = false;
        withNote = false;
        indexMatch = false;
    }

    public void activateWithNote() {
        withId = false;
        exactMatch = false;
        indexMatch = false;
    }

    public void activateWithId() {
        withNote = false;
        exactMatch = false;
        indexMatch = false;
    }

    public void reset() {
        if (nodeConceptSearchs != null) {
            for (NodeConceptSearch nodeConceptSearch : nodeConceptSearchs) {
                nodeConceptSearch.clear();
            }
            nodeConceptSearchs = null;
        }
        if (listResultAutoComplete != null) {
            listResultAutoComplete.clear();
        }
        searchSelected = null;
    }

    public List<NodeSearchMini> completTermFullText(String value) {

        isSelectedItem = false;
        //    searchSelected = new NodeSearchMini();
        if (searchHelper == null) {
            searchHelper = new SearchHelper();
        }

        if (listResultAutoComplete == null) {
            listResultAutoComplete = new ArrayList<>();
        } else {
            listResultAutoComplete.clear();
        }

        if (selectedTheso == null) {
            return listResultAutoComplete;
        }

        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getSelectedLang() != null) {
            String idLang;
            if (selectedTheso.getSelectedLang().equalsIgnoreCase("all")) {
                idLang = null;
            } else {
                idLang = selectedTheso.getSelectedLang();
            }

            if (exactMatch) {
                listResultAutoComplete = searchHelper.searchExactMatch(connect.getPoolConnexion(),
                        value,
                        idLang,
                        selectedTheso.getCurrentIdTheso());
            }
            if (indexMatch) {
                listResultAutoComplete = searchHelper.searchStartWith(connect.getPoolConnexion(),
                        value,
                        idLang,
                        selectedTheso.getCurrentIdTheso());
            }
            if (!withId && !withNote && !indexMatch && !exactMatch) {
                listResultAutoComplete = searchHelper.searchFullTextElastic(connect.getPoolConnexion(),
                        value,
                        idLang,
                        selectedTheso.getCurrentIdTheso());
            }
        }
        searchValue = value;
        if (listResultAutoComplete == null) {
            listResultAutoComplete = new ArrayList<>();
        }
        return listResultAutoComplete;
    }

    public void onSelect() {
        if (searchSelected == null) {
            return;
        }
        String[] values = searchSelected.getIdConcept().split("####");
        if (values == null) {
            return;
        }
        String idConcept;
        if (values.length > 1) {
            idConcept = values[0];

            //action group
            if (values[1].equalsIgnoreCase("isGroup")) {
                treeGroups.selectThisGroup(idConcept);
            }

        } else {
            idConcept = searchSelected.getIdConcept();

            if (nodeConceptSearchs == null) {
                nodeConceptSearchs = new ArrayList<>();
            } else {
                nodeConceptSearchs.clear();
            }
            if (conceptHelper == null) {
                conceptHelper = new ConceptHelper();
            }
            nodeConceptSearchs.add(
                    conceptHelper.getConceptForSearch(
                            connect.getPoolConnexion(),
                            idConcept,
                            selectedTheso.getCurrentIdTheso(),
                            selectedTheso.getCurrentLang())
            );
            setViewsSearch();
            if (nodeConceptSearchs.size() == 1) {
                onSelectConcept(selectedTheso.getCurrentIdTheso(), nodeConceptSearchs.get(0).getIdConcept(), selectedTheso.getCurrentLang());
            }
        }
        isSelectedItem = true;

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            /*        pf.ajax().update("containerIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("containerIndex:formLeftTab");*/
        }
    }

    /**
     * recherche de la valeur saisie en respectant les filtres
     */
    public void applySearch() {
        if (nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else {
            nodeConceptSearchs.clear();
        }

        if (conceptHelper == null) {
            conceptHelper = new ConceptHelper();
        }
        if (searchHelper == null) {
            searchHelper = new SearchHelper();
        }

        if (roleOnThesoBean.getSelectedThesoForSearch() == null || roleOnThesoBean.getSelectedThesoForSearch().isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Il faut choisir au moins un thésaurus !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        // cas où la recherche est sur un thésaurus sélectionné, il faut trouver la langue sélectionnée par l'utilisateur, si all, on cherche sur tous les thésaurus 
        if (selectedTheso.getCurrentIdTheso() == null || selectedTheso.getCurrentIdTheso().isEmpty()) {
            for (String idTheso : roleOnThesoBean.getSelectedThesoForSearch()) {
                nodeConceptSearchs.addAll(searchInThesaurus(idTheso, searchLangOfTheso(roleOnThesoBean.getListTheso(), idTheso))); // languageBean.getIdLangue());
            }
        } else {
            String idLang = null;
            if (!selectedTheso.getSelectedLang().equalsIgnoreCase("all")) {
                idLang = selectedTheso.getSelectedLang();
            }
            List<NodeConceptSearch> concepts = searchInThesaurus(selectedTheso.getCurrentIdTheso(), idLang);// languageBean.getIdLangue());
            nodeConceptSearchs.addAll(concepts);
        }

        if (CollectionUtils.isNotEmpty(nodeConceptSearchs)) {
            Collections.sort(nodeConceptSearchs);
            if (nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            } else {
                setViewsSearch();
                isSelectedItem = false;
            }
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Pas de résultat !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }

        rightBodySetting.setIndex("0");
        indexSetting.setIsValueSelected(true);
    }
    
    private String searchLangOfTheso(List<RoleOnThesoBean.ThesoModel> listTheso, String idTheso) {
        for (RoleOnThesoBean.ThesoModel theso : listTheso) {
            if (theso.getId().equals(idTheso)) {
                return theso.getDefaultLang();
            }
        }
        return selectedTheso.getSelectedLang();
    }

    private List<NodeConceptSearch> searchInThesaurus(String idTheso, String idLang) {

        List<String> nodeSearchsId;
        List<NodeConceptSearch> concepts = new ArrayList<>();

        if (withId) {
            nodeSearchsId = searchHelper.searchForIds(connect.getPoolConnexion(), searchValue, idTheso);
            for (String idConcept : nodeSearchsId) {
                concepts.add(0, conceptHelper.getConceptForSearch(connect.getPoolConnexion(), idConcept, idTheso, idLang));
            }
        }

        if (withNote) {
            nodeSearchsId = searchHelper.searchIdConceptFromNotes(connect.getPoolConnexion(), searchValue, idLang, idTheso);

            for (String idConcept : nodeSearchsId) {
                concepts.add(conceptHelper.getConceptForSearch(connect.getPoolConnexion(), idConcept, idTheso, idLang));
            }
        }

        if (exactMatch) {
            ArrayList<NodeSearchMini> nodeSearchMini = searchHelper.searchExactMatch(connect.getPoolConnexion(),
                    searchValue, idLang, idTheso);

            for (NodeSearchMini nodeSearchMini1 : nodeSearchMini) {
                concepts.add(conceptHelper.getConceptForSearch(connect.getPoolConnexion(),
                        nodeSearchMini1.getIdConcept(), idTheso, idLang));
            }
        }

        if (!withId && !withNote && !exactMatch) {
            ArrayList<String> nodeSearchMinis = searchHelper.searchFullTextId(
                    connect.getPoolConnexion(), searchValue, idLang, idTheso);
            for (String nodeSearchMini : nodeSearchMinis) {
                concepts.add(conceptHelper.getConceptForSearch(connect.getPoolConnexion(),
                        nodeSearchMini, idTheso, idLang));
            }
        }
        Collections.sort(concepts);
        return concepts;
    }

    public void afficherResultatRecherche() {
        if(CollectionUtils.isEmpty(nodeConceptSearchs)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Il faut faire une recherche avant !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            PrimeFaces.current().executeScript("PF('resultatRecherche').show();");
        }
    }

    /**
     * permet de retourner la liste des concepts qui ont une poly-hiérarchie
     */
    public void getAllPolyierarchy() {
        if (nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else {
            nodeConceptSearchs.clear();
        }

        if (conceptHelper == null) {
            conceptHelper = new ConceptHelper();
        }
        if (searchHelper == null) {
            searchHelper = new SearchHelper();
        }

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
        if (!nodeConceptSearchs.isEmpty()) {
            Collections.sort(nodeConceptSearchs);
            onSelectConcept(selectedTheso.getCurrentIdTheso(), nodeConceptSearchs.get(0).getIdConcept(), selectedTheso.getCurrentLang());
        }
        if (nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if (nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            } else {
                setViewsSearch();
                isSelectedItem = false;
            }
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Pas de résultat !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        rightBodySetting.setIndex("0");
        indexSetting.setIsValueSelected(true);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }
    
    /**
     * permet de retourner la liste des concepts qui ont une poly-hiérarchie
     */
    public void getAllDeprecatedConcepts() {
        if (nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else {
            nodeConceptSearchs.clear();
        }

        if (conceptHelper == null) {
            conceptHelper = new ConceptHelper();
        }
        if (searchHelper == null) {
            searchHelper = new SearchHelper();
        }

        ArrayList<String> nodeSearchsId = searchHelper.searchAllDeprecatedConcepts(
                connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

        for (String idConcept : nodeSearchsId) {
            nodeConceptSearchs.add(
                    conceptHelper.getConceptForSearch(
                            connect.getPoolConnexion(),
                            idConcept,
                            selectedTheso.getCurrentIdTheso(),
                            selectedTheso.getCurrentLang()));
        }
        if (!nodeConceptSearchs.isEmpty()) {
            onSelectConcept(selectedTheso.getCurrentIdTheso(), nodeConceptSearchs.get(0).getIdConcept(), selectedTheso.getCurrentLang());
        }
        if (nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            Collections.sort(nodeConceptSearchs);
            if (nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            } else {
                setViewsSearch();
                isSelectedItem = false;
            }
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Pas de résultat !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        rightBodySetting.setIndex("0");
        indexSetting.setIsValueSelected(true);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }    

    /**
     * permet de retourner la liste des concepts qui ont plusieurs Groupes
     */
    public void searchConceptWithMultiGroup() {
        if (nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else {
            nodeConceptSearchs.clear();
        }

        if (conceptHelper == null) {
            conceptHelper = new ConceptHelper();
        }
        if (searchHelper == null) {
            searchHelper = new SearchHelper();
        }

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
        if (!nodeConceptSearchs.isEmpty()) {
            Collections.sort(nodeConceptSearchs);
            onSelectConcept(selectedTheso.getCurrentIdTheso(), nodeConceptSearchs.get(0).getIdConcept(), selectedTheso.getCurrentLang());
        }
        if (nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if (nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            } else {
                setViewsSearch();
                isSelectedItem = false;
            }
        }
    }

    /**
     * permet de retourner la liste des concepts qui ont plusieurs Groupes
     */
    public void searchConceptWithoutGroup() {
        if (nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else {
            nodeConceptSearchs.clear();
        }

        if (conceptHelper == null) {
            conceptHelper = new ConceptHelper();
        }
        if (searchHelper == null) {
            searchHelper = new SearchHelper();
        }

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
        if (!nodeConceptSearchs.isEmpty()) {
            Collections.sort(nodeConceptSearchs);
            onSelectConcept(selectedTheso.getCurrentIdTheso(), nodeConceptSearchs.get(0).getIdConcept(), selectedTheso.getCurrentLang());
        }
        if (nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if (nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            } else {
                setViewsSearch();
                isSelectedItem = false;
            }
        }
    }

    /**
     * permet de retourner la liste des concepts qui sont en doublons
     */
    public void searchConceptDuplicated() {
        if (nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else {
            nodeConceptSearchs.clear();
        }

        if (conceptHelper == null) {
            conceptHelper = new ConceptHelper();
        }
        if (searchHelper == null) {
            searchHelper = new SearchHelper();
        }

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
        if (!nodeConceptSearchs.isEmpty()) {
            Collections.sort(nodeConceptSearchs);
            onSelectConcept(selectedTheso.getCurrentIdTheso(), nodeConceptSearchs.get(0).getIdConcept(), selectedTheso.getCurrentLang());
        }
        if (nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if (nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            } else {
                setViewsSearch();
                isSelectedItem = false;
            }
        }
    }

    /**
     * permet de retourner la liste des concepts qui sont une relation RT et NT
     * ou BT ce qui est interdit
     */
    public void searchConceptWithRTandBT() {
        if (nodeConceptSearchs == null) {
            nodeConceptSearchs = new ArrayList<>();
        } else {
            nodeConceptSearchs.clear();
        }

        if (conceptHelper == null) {
            conceptHelper = new ConceptHelper();
        }
        if (searchHelper == null) {
            searchHelper = new SearchHelper();
        }
        ArrayList<String> allIdConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

        ArrayList<String> nodeSearchsId = new ArrayList<>();
        for (String idConcept : allIdConcepts) {
            if (searchHelper.isConceptHaveRTandBT(
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
        if (!nodeConceptSearchs.isEmpty()) {
            Collections.sort(nodeConceptSearchs);
            onSelectConcept(selectedTheso.getCurrentIdTheso(), nodeConceptSearchs.get(0).getIdConcept(), selectedTheso.getCurrentLang());
        }
        if (nodeConceptSearchs != null && !nodeConceptSearchs.isEmpty()) {
            if (nodeConceptSearchs.size() == 1) {
                isSelectedItem = true;
                setViewsConcept();
            } else {
                setViewsSearch();
                isSelectedItem = false;
            }
        }
    }

    private void setViewsSearch() {
        rightBodySetting.setShowResultSearchToOn();
        leftBodySetting.setIndex("0"); // vue Filtre de recherche
        rightBodySetting.setIndex("0");
    }

    private void setViewsConcept() {
        rightBodySetting.setShowResultSearchToOn();
        leftBodySetting.setIndex("0"); // vue Filtre de recherche
        rightBodySetting.setIndex("0");
    }

    public void onSelectConcept(String idTheso, String idConcept, String idLang) {
        roleOnThesoBean.initNodePref(idTheso);
        selectedTheso.setSelectedIdTheso(idTheso);
        selectedTheso.setSelectedLang(idLang);
        try {
            selectedTheso.setSelectedThesoForSearch();
        } catch (IOException ex) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        conceptBean.getConcept(idTheso, idConcept, idLang);
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

    public boolean isIndexMatch() {
        return indexMatch;
    }

    public void setIndexMatch(boolean indexMatch) {
        this.indexMatch = indexMatch;
    }

    public void searchResultVisible() {
        if (searchResultVisible) {
            searchResultVisible = false;
            PrimeFaces.current().executeScript("PF('resultatRecherche').hide();");
        } else {
            searchResultVisible = true;
            PrimeFaces.current().executeScript("PF('resultatRecherche').show();");
        }

        PrimeFaces.current().ajax().update("containerIndex:searchBar");
    }

    public String getResultSearchIcon() {
        return searchResultVisible ? "fas fa-arrow-circle-right" : "fa fa-arrow-circle-left";
    }

    public void setSearchResultVisible(boolean searchResultVisible) {
        this.searchResultVisible = searchResultVisible;
    }

}
