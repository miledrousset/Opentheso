package fr.cnrs.opentheso.bean.search;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.models.concept.NodeConceptSearch;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.IOException;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.collections4.CollectionUtils;
import org.primefaces.PrimeFaces;


@Named(value = "searchBean")
@SessionScoped
public class SearchBean implements Serializable {

    @Autowired
    private SelectedTheso selectedTheso;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RightBodySetting rightBodySetting;

    @Autowired
    private LeftBodySetting leftBodySetting;

    @Autowired
    private ConceptView conceptBean;

    @Autowired
    private IndexSetting indexSetting;

    @Autowired
    private TreeGroups treeGroups;

    @Autowired
    private Tree tree;

    @Autowired
    private RoleOnThesoBean roleOnThesoBean;

    @Autowired
    private PropositionBean propositionBean;
    
    @Autowired
    private LanguageBean languageBean;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private SearchHelper searchHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    private NodeSearchMini searchSelected;

    private ArrayList<NodeSearchMini> listResultAutoComplete;
    private ArrayList<NodeConceptSearch> nodeConceptSearchs;
    private String searchValue;

    // sert à afficher ou non le total de résultat
    private boolean isSelectedItem;

    // filter search
    private boolean exactMatch;
    private boolean indexMatch;
    private boolean withNote;
    private boolean withId;
    private boolean isSearchInSpecificTheso;


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
                listResultAutoComplete = searchHelper.searchExactMatch(
                        value,
                        idLang,
                        selectedTheso.getCurrentIdTheso());
            }
            if (indexMatch) {
                listResultAutoComplete = searchHelper.searchStartWith(
                        value,
                        idLang,
                        selectedTheso.getCurrentIdTheso());
            }

            if (withId) {
                listResultAutoComplete = searchHelper.searchByAllId(value,
                        idLang, selectedTheso.getCurrentIdTheso());
            }                
                         
            if (!withId && !withNote && !indexMatch && !exactMatch) {
                listResultAutoComplete = searchHelper.searchFullTextElastic(
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
            //action facet
            if (values[1].equalsIgnoreCase("isFacet")) {
                tree.selectThisFacet(idConcept);
            }

        } else {
            idConcept = searchSelected.getIdConcept();

            if (nodeConceptSearchs == null) {
                nodeConceptSearchs = new ArrayList<>();
            } else {
                nodeConceptSearchs.clear();
            }

            nodeConceptSearchs.add(conceptHelper.getConceptForSearch(idConcept, selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang())
            );
            setViewsSearch();
            if (nodeConceptSearchs.size() == 1) {
                onSelectConcept(selectedTheso.getCurrentIdTheso(), nodeConceptSearchs.get(0).getIdConcept(), selectedTheso.getCurrentLang());
            }
        }
        isSelectedItem = true;

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
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

        if (roleOnThesoBean.getSelectedThesoForSearch() == null || roleOnThesoBean.getSelectedThesoForSearch().isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Il faut choisir au moins un thésaurus !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        isSearchInSpecificTheso = true;
        if ((selectedTheso.getCurrentIdTheso() == null || selectedTheso.getCurrentIdTheso().isEmpty()) && roleOnThesoBean.getSelectedThesoForSearch().size() > 1) {
            isSearchInSpecificTheso = false;
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
            if (nodeConceptSearchs.size() == 1) {
                conceptBean.getConcept(nodeConceptSearchs.get(0).getIdTheso(), nodeConceptSearchs.get(0).getIdConcept(),
                        nodeConceptSearchs.get(0).getCurrentLang(), currentUser);
                isSelectedItem = true;
                setViewsConcept();

            } else {
                setViewsSearch();
                isSelectedItem = false;
            }

            PrimeFaces.current().executeScript("showResultSearchBar();");
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Recherche de '" + searchValue + "' : Aucun resultat trouvée !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }

        rightBodySetting.setIndex("0");
        indexSetting.setIsValueSelected(true);

        PrimeFaces.current().ajax().update("resultSearchBar");
    }

    private String searchLangOfTheso(List<RoleOnThesoBean.ThesoModel> listTheso, String idTheso) {
        for (RoleOnThesoBean.ThesoModel theso : listTheso) {
            if (theso.getId().equals(idTheso)) {
                return theso.getDefaultLang();
            }
        }
        return selectedTheso.getSelectedLang();
    }

    /**
     * permet de retourner le nom du thesaurus
     *
     * @param idTheso
     * @param idLang
     * @return
     */
    public String getThesoName(String idTheso, String idLang) {

        return thesaurusHelper.getTitleOfThesaurus(idTheso, idLang);
    }

    private List<NodeConceptSearch> searchInThesaurus(String idTheso, String idLang) {

        List<String> nodeSearchsId;
        List<NodeConceptSearch> concepts = new ArrayList<>();
        String thesaurusLabel = thesaurusHelper.getTitleOfThesaurus(idTheso, idLang);
        NodeConceptSearch nodeConceptSearch;
        if(searchValue == null)
            searchValue = "";

        if (withId) {
            nodeSearchsId = searchHelper.searchForIds(searchValue, idTheso);
            for (String idConcept : nodeSearchsId) {
                nodeConceptSearch = conceptHelper.getConceptForSearch(
                        idConcept, idTheso, idLang);
                if (nodeConceptSearch != null) {
                    nodeConceptSearch.setThesoName(thesaurusLabel);
                }
                concepts.add(0,nodeConceptSearch);
            }            
        }

        if (withNote) {
            nodeSearchsId = searchHelper.searchIdConceptFromNotes(searchValue, idLang, idTheso);

            for (String idConcept : nodeSearchsId) {
                nodeConceptSearch = conceptHelper.getConceptForSearch(
                        idConcept, idTheso, idLang);
                if (nodeConceptSearch != null) {
                    nodeConceptSearch.setThesoName(thesaurusLabel);
                }
                concepts.add(nodeConceptSearch);
            }
        }

        if (exactMatch) {
            ArrayList<NodeSearchMini> nodeSearchMinis = searchHelper.searchExactMatch(
                    searchValue, idLang, idTheso);

            for (NodeSearchMini nodeSearchMini : nodeSearchMinis) {
                nodeConceptSearch = conceptHelper.getConceptForSearch(
                        nodeSearchMini.getIdConcept(), idTheso, idLang);
                if (nodeConceptSearch != null) {
                    nodeConceptSearch.setThesoName(thesaurusLabel);
                }
                concepts.add(nodeConceptSearch);
            }
        }

        if (indexMatch || searchValue.isEmpty()) {
            ArrayList<NodeSearchMini> nodeSearchMinis = searchHelper.searchStartWith(
                    searchValue,
                    idLang,
                    idTheso);
            for (NodeSearchMini nodeSearchMini : nodeSearchMinis) {
                nodeConceptSearch = conceptHelper.getConceptForSearch(
                        nodeSearchMini.getIdConcept(), idTheso, idLang);
                if (nodeConceptSearch != null) {
                    nodeConceptSearch.setThesoName(thesaurusLabel);
                }
                concepts.add(nodeConceptSearch);
            }

        }

        if (!withId && !withNote && !exactMatch && !indexMatch) {
            
            ArrayList<String> listIds = searchHelper.searchFullTextElasticId(
                        searchValue,
                        idLang,
                        idTheso);                    
            
            
            for (String idConcept : listIds) {
                nodeConceptSearch = conceptHelper.getConceptForSearch(
                        idConcept, idTheso, idLang);
                if (nodeConceptSearch != null) {
                    nodeConceptSearch.setThesoName(thesaurusLabel);
                }
                concepts.add(nodeConceptSearch);
            }
        }
    //    Collections.sort(concepts);
        return concepts;
    }

    public void afficherResultatRecherche() {
        if (CollectionUtils.isEmpty(nodeConceptSearchs)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", languageBean.getMsg("search.doSearchBefore") + " !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
        } else {
            PrimeFaces.current().executeScript("showResultSearchBar();");
        }
    }

    public void setBarSearchStatus() {
        PrimeFaces.current().executeScript("showResultSearchBar();");
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

        ArrayList<String> nodeSearchsId = searchHelper.searchAllPolyierarchy(selectedTheso.getCurrentIdTheso());

        for (String idConcept : nodeSearchsId) {
            nodeConceptSearchs.add(conceptHelper.getConceptForSearch(
                    idConcept, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang()));
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
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Recherche Poly-hiéarchie : Aucun résultat trouvée !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        rightBodySetting.setIndex("0");
        indexSetting.setIsValueSelected(true);
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

        ArrayList<String> nodeSearchsId = searchHelper.searchAllDeprecatedConcepts(selectedTheso.getCurrentIdTheso());

        for (String idConcept : nodeSearchsId) {
            nodeConceptSearchs.add(conceptHelper.getConceptForSearch(idConcept,
                    selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang()));
        }

        if (!nodeConceptSearchs.isEmpty()) {
            onSelectConcept(selectedTheso.getCurrentIdTheso(),
                    nodeConceptSearchs.get(0).getIdConcept(), selectedTheso.getCurrentLang());
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
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Recherche de concepts dépréciés : Pas de résultat !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        rightBodySetting.setIndex("0");
        indexSetting.setIsValueSelected(true);
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

        ArrayList<String> nodeSearchsId = searchHelper.searchConceptWithMultiGroup(selectedTheso.getCurrentIdTheso());

        for (String idConcept : nodeSearchsId) {
            nodeConceptSearchs.add(conceptHelper.getConceptForSearch(idConcept, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang()));
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
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Recherche multi-groupes : Aucun résultat trouvée !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        rightBodySetting.setIndex("0");
        indexSetting.setIsValueSelected(true);
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

        ArrayList<String> nodeSearchsId = searchHelper.searchConceptWithoutGroup(selectedTheso.getCurrentIdTheso());

        for (String idConcept : nodeSearchsId) {
            nodeConceptSearchs.add(conceptHelper.getConceptForSearch(idConcept, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang()));
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
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Recherche sans-groupes : Aucun résultat trouvée !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        rightBodySetting.setIndex("0");
        indexSetting.setIsValueSelected(true);
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

        ArrayList<String> nodeSearchLabels = searchHelper.searchConceptDuplicated( selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());

        for (String label : nodeSearchLabels) {
            nodeConceptSearchs.add(conceptHelper.getConceptForSearchFromLabel(label, selectedTheso.getCurrentIdTheso(),
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
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Recherche doublons : Aucun résultat trouvée !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        rightBodySetting.setIndex("0");
        indexSetting.setIsValueSelected(true);
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

        ArrayList<String> allIdConcepts = conceptHelper.getAllIdConceptOfThesaurus(selectedTheso.getCurrentIdTheso());

        ArrayList<String> nodeSearchsId = new ArrayList<>();
        for (String idConcept : allIdConcepts) {
            if (searchHelper.isConceptHaveRTandBT(idConcept, selectedTheso.getCurrentIdTheso())) {
                nodeSearchsId.add(idConcept);
            }
        }
        for (String idConcept : nodeSearchsId) {
            nodeConceptSearchs.add(conceptHelper.getConceptForSearch(idConcept, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang()));
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
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Recherche relations interdites : Aucun résultat trouvée !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        rightBodySetting.setIndex("0");
        indexSetting.setIsValueSelected(true);
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

        propositionBean.setNewProposition(false);
        roleOnThesoBean.initNodePref(idTheso);
        selectedTheso.setSelectedIdTheso(idTheso);
        selectedTheso.setSelectedLang(idLang);
        try {
            selectedTheso.setSelectedThesoForSearch();
       //      selectedTheso.setSelectedTheso();
        } catch (IOException ex) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        roleOnThesoBean.setSelectedThesoForSearch(roleOnThesoBean.getSelectedThesoForSearch().stream()
                .filter(theso -> theso.contains(idTheso))
                .collect(Collectors.toList()));
        conceptBean.getConcept(idTheso, idConcept, idLang, currentUser);
        rightBodySetting.setIndex("0");
        
        PrimeFaces.current().ajax().update("containerIndex:contentConcept");
        PrimeFaces.current().ajax().update("containerIndex:thesoSelect");

    //    PrimeFaces.current().executeScript("afficheSearchBar()");
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

    public boolean isIsSearchInSpecificTheso() {
        return isSearchInSpecificTheso;
    }

    public void setIsSearchInSpecificTheso(boolean isSearchInSpecificTheso) {
        this.isSearchInSpecificTheso = isSearchInSpecificTheso;
    }

}
