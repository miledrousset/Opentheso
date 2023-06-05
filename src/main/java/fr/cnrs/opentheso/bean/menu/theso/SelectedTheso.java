package fr.cnrs.opentheso.bean.menu.theso;

import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bean.alignment.AlignementElement;
import fr.cnrs.opentheso.bean.alignment.ResultatAlignement;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.viewconcepts.TreeConcepts;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.leftbody.viewliste.ListIndex;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.MenuBean;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import fr.cnrs.opentheso.bean.search.SearchBean;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Arrays;
import javax.faces.application.FacesMessage;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import liquibase.exception.LiquibaseException;

import org.primefaces.PrimeFaces;
import org.primefaces.event.UnselectEvent;


@Named(value = "selectedTheso")
@SessionScoped
public class SelectedTheso implements Serializable {

    @Inject private LanguageBean LanguageBean;
    @Inject private Connect connect;
    @Inject private IndexSetting indexSetting;
    @Inject private TreeGroups treeGroups;
    @Inject private TreeConcepts treeConcepts;
    @Inject private Tree tree;
    @Inject private ListIndex listIndex;
    @Inject private ConceptView conceptBean;
    @Inject private SearchBean searchBean;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private ViewEditorThesoHomeBean viewEditorThesoHomeBean;
    @Inject private ViewEditorHomeBean viewEditorHomeBean;
    @Inject private RightBodySetting rightBodySetting;
    @Inject private MenuBean menuBean;
    @Inject private PropositionBean propositionBean;

    private static final long serialVersionUID = 1L;

    private String selectedIdTheso;
    private String currentIdTheso;
    private String optionThesoSelected;

    private ArrayList<NodeLangTheso> nodeLangs;

    private String selectedLang; // la langue qu'on vient de séléctionner
    private String currentLang; // la langue en cours dans la session
    private boolean isActionFromConcept;

    private String idConceptFromUri;
    private String idThesoFromUri;
    private String idGroupFromUri;

    private boolean isUriRequest = false;

    private String thesoName;
    private boolean sortByNotation;
    
    private boolean isNetworkAvailable;
    
    private String localUri;

    private List<ResultatAlignement> resultAlignementList;

    @PreDestroy
    public void destroy(){
        clear();
    }

    public void clear(){
        if(nodeLangs!= null){
            nodeLangs.clear();
            nodeLangs = null;
        }
        selectedIdTheso = null;
        currentIdTheso = null;
        selectedLang = null;
        currentLang = null;
        idThesoFromUri = null;      
        thesoName = null;   
        localUri = null;
    }      
    
    @PostConstruct
    public void initializing() {

        if (!connect.isConnected()) {
            System.err.println("Erreur de connexion BDD");
            return;
        }
        
        isNetworkAvailable = true;
        roleOnThesoBean.showListTheso();
        sortByNotation = false;
    }

    public void init() {
        selectedLang = null;
        currentLang = null;
        nodeLangs = null;
        selectedIdTheso = null;
        currentIdTheso = null;
        thesoName = null;
        localUri = null;
    }

    private void initIdsFromUri() {
        idConceptFromUri = null;
        idThesoFromUri = null;
    }

    /**
     * capte les actions qui proviennent de la vue concept pour éviter
     * d'initialiser la vue ! sert pour replacer l'arbre
     */
    public void actionFromConceptToOn() {
        isActionFromConcept = true;
    }

    public void setSelectedOptionTheso() {
        switch(optionThesoSelected) {
            case "Option1":
                viewEditorHomeBean.reset();
                break;
            case "Option2":
                viewEditorHomeBean.initText();
                break;
            case "Option3":
                viewEditorHomeBean.initGoogleAnalytics();
                break;
        }
    }

    public String getUriOfTheso(NodePreference nodePreference){
        String contextPath = FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
        String serverAdress = FacesContext.getCurrentInstance().getExternalContext().getRequestServerName();
        String protocole = FacesContext.getCurrentInstance().getExternalContext().getRequestScheme();
    //    HttpServletRequest request = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
        String baseUrl = protocole + "://" + serverAdress + contextPath;


   /*     String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        String uri = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()+"/";  */
        if(nodePreference == null) {
            return baseUrl + "/?idt=" + currentIdTheso;
        }
        else {
            String idArk = new ThesaurusHelper().getIdArkOfThesaurus(connect.getPoolConnexion(), currentIdTheso);
            if(StringUtils.isEmpty(idArk)){
                return baseUrl + "/?idt=" + currentIdTheso;
            } else
            return baseUrl + "/api/ark:/" + idArk;
        }
        /*
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        thesaurusHelper.get*/
    }

    /**
     * Permet de charger le thésaurus sélectionné C'est le point d'entrée de
     * l'application
     * @throws java.io.IOException
     */
    public void setSelectedTheso() throws IOException {
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        localUri = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()+"/";  
        connect.setLocalUri(localUri);
        
        viewEditorThesoHomeBean.reset();
        viewEditorHomeBean.reset();
        if (isUriRequest) {
            isUriRequest = false;
            
            if (searchBean.isBarVisisble()) {
                searchBean.setNodeConceptSearchs(new ArrayList<>());
                searchBean.setBarVisisble(false);
                PrimeFaces.current().executeScript("disparaitre();");
            }
            
            menuBean.redirectToThesaurus();
            return;
        }

        if (selectedIdTheso == null || selectedIdTheso.isEmpty()) {
            
            roleOnThesoBean.showListTheso();
            treeGroups.reset();
            tree.reset();
            treeConcepts.reset();
            listIndex.reset();
            conceptBean.init();
            init();
            
            indexSetting.setIsSelectedTheso(false); 
            
            roleOnThesoBean.setSelectedThesoForSearch(new ArrayList());
            for (RoleOnThesoBean.ThesoModel thesoModel : roleOnThesoBean.getListTheso()) {
                roleOnThesoBean.getSelectedThesoForSearch().add(thesoModel.getId());
            }
            
            if (searchBean.isBarVisisble()) {
                searchBean.setNodeConceptSearchs(new ArrayList<>());
                searchBean.setBarVisisble(false);
                PrimeFaces.current().executeScript("disparaitre();");
            }
            
            menuBean.redirectToThesaurus();
            return;
        }

        // après un raffraichissement F5
        if (selectedIdTheso.equalsIgnoreCase(currentIdTheso)) {
            if (!selectedLang.equalsIgnoreCase(currentLang)) {
                startNewLang();
            }
            
            if (searchBean.isBarVisisble()) {
                searchBean.setNodeConceptSearchs(new ArrayList<>());
                searchBean.setBarVisisble(false);
                PrimeFaces.current().executeScript("disparaitre();");
            }
            
            menuBean.redirectToThesaurus();
            return;
        }

        sortByNotation = false;
        startNewTheso(null);
        indexSetting.setIsSelectedTheso(true);
        indexSetting.setIsValueSelected(false);
        indexSetting.setIsHomeSelected(true);
        indexSetting.setIsThesoActive(true);
        
        propositionBean.searchNewPropositions();
        
        for (RoleOnThesoBean.ThesoModel thesoModel : roleOnThesoBean.getListTheso()) {
            if (selectedIdTheso.equals(thesoModel.getId())) {
                roleOnThesoBean.setSelectedThesoForSearch(Arrays.asList(selectedIdTheso));
            }
        }
        
        if (searchBean.isBarVisisble()) {
            searchBean.setNodeConceptSearchs(new ArrayList<>());
            searchBean.setBarVisisble(false);
            PrimeFaces.current().executeScript("disparaitre();");
        }
        
        menuBean.redirectToThesaurus();
    }
    
    
    public void setSelectedThesoForSearch() throws IOException {
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        localUri = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()+"/";  
        connect.setLocalUri(localUri);

        viewEditorThesoHomeBean.reset();
        viewEditorHomeBean.reset();

        // après un raffraichissement F5
        if (selectedIdTheso.equalsIgnoreCase(currentIdTheso)) {
            if (!selectedLang.equalsIgnoreCase(currentLang)) {
                startNewLang();
            }
            return;
        }

        sortByNotation = false;
        startNewTheso(null);
        indexSetting.setIsSelectedTheso(true);
        indexSetting.setIsValueSelected(true);
        indexSetting.setIsHomeSelected(false);
        indexSetting.setIsThesoActive(true);
    }

    public List<ResultatAlignement> getResultAlignementList() {
        return resultAlignementList;
    }

    public void setResultAlignementList(List<ResultatAlignement> resultAlignementList) {
        this.resultAlignementList = resultAlignementList;
    }

    public void onUnselect(UnselectEvent<ResultatAlignement> event) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Unselected", event.getObject().getTitle()));
    }

    /**
     * permet de recharger l'arbre des collections 
     */
    public void reloadGroups(){
        treeGroups.reset();
        treeGroups.initialise(selectedIdTheso, selectedLang);        
    }
    
    /**
     * permet de recharger l'arbre des collections 
     */
    public void reloadConceptTree(){
        treeConcepts.reset();
        treeConcepts.initialise(selectedIdTheso, selectedLang);        
    }    
    
    /**
     * Permet de Re-charger le thésaurus sélectionné, pour activer des mises à jour non prises en compte
     * @throws java.io.IOException
     */
    public void reloadSelectedTheso() throws IOException {
        roleOnThesoBean.showListTheso();

        searchBean.reset();
        viewEditorThesoHomeBean.reset();
        viewEditorHomeBean.reset();
        treeGroups.reset();

        if (selectedIdTheso == null || selectedIdTheso.isEmpty()) {
            return;
        }
        startNewTheso(null);
        indexSetting.setIsSelectedTheso(true);
        indexSetting.setIsValueSelected(false);
        indexSetting.setIsHomeSelected(true);

        menuBean.redirectToThesaurus();
    }

    /**
     * permet de changer la langue du thésaurus et recharger les données
     */
    public void changeLang() {
        if (isUriRequest) {
            isUriRequest = false;
            return;
        }
        if (selectedLang == null || selectedIdTheso == null) {
            return;
        }

        if (selectedLang.equalsIgnoreCase(currentLang)) {
            isActionFromConcept = false;
            return;
        }
        if (selectedLang.equalsIgnoreCase("all")) {
            isActionFromConcept = false;
            return;
        }        
        startNewLang();
    }

    /**
     * initialise le nouveau thésaurus avec l'identifiant de thésaurus
     * sélectionné si la langue est fournie, on initialise dans cette langue,
     * sinon, on prend la langue source du thésaurus
     *
     * @param idLang
     */
    private void startNewTheso(String idLang) {
        currentIdTheso = selectedIdTheso;
        // setting des préférences du thésaurus sélectionné
        roleOnThesoBean.initNodePref();
        roleOnThesoBean.showListTheso();
        if (idLang == null) {
            idLang = getIdLang();
            if (idLang == null || idLang.isEmpty()) {
                return;
            }
        }
        nodeLangs = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(),
                selectedIdTheso,
                LanguageBean.getIdLangue());

        currentLang = idLang;
        selectedLang = idLang;
        setThesoName();

        // initialisation de l'arbre des groupes
        treeGroups.reset();
        treeGroups.initialise(selectedIdTheso, selectedLang);

        treeConcepts.reset();
        treeConcepts.initialise(selectedIdTheso, selectedLang);

        tree.reset();
        tree.initialise(selectedIdTheso, selectedLang);

        listIndex.reset();
        conceptBean.clear();
        conceptBean.init();
    }

    private void setThesoName() {
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        thesoName = thesaurusHelper.getTitleOfThesaurus(connect.getPoolConnexion(),
                selectedIdTheso, selectedLang);
    }

    private void startNewLang() {
        currentLang = selectedLang;
        
        treeGroups.reset();
        tree.reset();
        listIndex.reset();

        // initialisation de l'arbre des groupes
        treeGroups.initialise(selectedIdTheso, selectedLang);
        treeConcepts.initialise(selectedIdTheso, selectedLang);
        tree.initialise(selectedIdTheso, selectedLang);
        
        if (!isActionFromConcept) {
            conceptBean.init();
        }
        isActionFromConcept = false;

        searchBean.onSelectConcept(selectedIdTheso, tree.getIdConceptSelected(), selectedLang);
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.executeScript("srollToSelected()");
        }
        
        if (searchBean.isBarVisisble()) {
            searchBean.setNodeConceptSearchs(new ArrayList<>());
            searchBean.setBarVisisble(false);
            PrimeFaces.current().executeScript("disparaitre();");
        }
    }

    private String getIdLang() {
        String idLang = connect.getWorkLanguage();
        if (roleOnThesoBean.getNodePreference() != null) {
            idLang = roleOnThesoBean.getNodePreference().getSourceLang();
        }
        return idLang;
    }

    /**
     * Pour sélectionner un thésaurus ou un concept en passant par l'URL 
     * @throws java.io.IOException 
     */
    public void preRenderView() throws IOException {
        if (idThesoFromUri == null) {
            return;
        }
        if (idThesoFromUri.equalsIgnoreCase(selectedIdTheso)) {
            if (idConceptFromUri == null || idConceptFromUri.isEmpty()) {
                //test si c'est une collection 
                if (idGroupFromUri == null || idGroupFromUri.isEmpty()) {
                    initIdsFromUri();
                    return;
                } else {
                    // sélectionner le groupe ou collection
                    treeGroups.selectThisGroup(idGroupFromUri.trim());
                    initIdsFromUri();
                    return;
                }

            }
            if (currentLang == null) {
                String idLang = getIdLang();
                if (idLang == null || idLang.isEmpty()) {
                    return;
                }
                currentLang = idLang;
                selectedLang = idLang;
            }
            conceptBean.getConcept(selectedIdTheso, idConceptFromUri, currentLang);
            actionFromConceptToOn();
            initIdsFromUri();
            return;
        }

        // gestion de l'accès par thésaurus d'un identifiant différent 
        if (!idThesoFromUri.equalsIgnoreCase(selectedIdTheso)) {
            if (isValidTheso(idThesoFromUri)) {
                /// chargement du thésaurus
                selectedIdTheso = idThesoFromUri;
                roleOnThesoBean.initNodePref(selectedIdTheso);
                startNewTheso(roleOnThesoBean.getNodePreference().getSourceLang());//currentLang);
                indexSetting.setIsSelectedTheso(true);
                indexSetting.setIsThesoActive(true);
                rightBodySetting.setIndex("0");  
                
                if (idConceptFromUri != null && !idConceptFromUri.isEmpty()) {
                    // chargement du concept puisqu'il est renseigné
                    conceptBean.getConcept(currentIdTheso, idConceptFromUri, currentLang);
                    actionFromConceptToOn();
                    
                } else {
                    //cas d'appel pour une collection
                    if(idGroupFromUri != null && !idGroupFromUri.isEmpty()) {
                        treeGroups.selectThisGroup(idGroupFromUri.trim());
                        rightBodySetting.setIndex("1");                        
                        initIdsFromUri();
                     //   return;
                    } else 
                        indexSetting.setIsHomeSelected(true);
                }
            } else {
                return;
            }
        }
        initIdsFromUri();
    }

    private boolean isValidTheso(String idTheso) {
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        return !thesaurusHelper.isThesoPrivate(connect.getPoolConnexion(), idTheso);
    }

    public String getIdConceptFromUri() {
        return idConceptFromUri;
    }

    public void setIdConceptFromUri(String idConceptFromUri) {
        this.idConceptFromUri = idConceptFromUri;
    }

    public String getIdGroupFromUri() {
        return idGroupFromUri;
    }

    public void setIdGroupFromUri(String idGroupFromUri) {
        this.idGroupFromUri = idGroupFromUri;
    }

    public String getIdThesoFromUri() {
        return idThesoFromUri;
    }

    public void setIdThesoFromUri(String idThesoFromUri) {
        this.idThesoFromUri = idThesoFromUri;
    }

    public ArrayList<NodeLangTheso> getNodeLangs() {
        return nodeLangs;
    }

    public void setNodeLangs(ArrayList<NodeLangTheso> nodeLangs) {
        this.nodeLangs = nodeLangs;
    }

    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public String getCurrentLang() {
        return currentLang;
    }

    public void setCurrentLang(String currentLang) {
        this.currentLang = currentLang;
    }

    public String getCurrentIdTheso() {
        return currentIdTheso;
    }

    public void setCurrentIdTheso(String currentIdTheso) {
        this.currentIdTheso = currentIdTheso;
    }

    public String getThesoName() {
        return thesoName;
    }

    public void setThesoName(String thesoName) {
        this.thesoName = thesoName;
    }

    public String getSelectedIdTheso() {
        return selectedIdTheso;
    }

    public void setSelectedIdTheso(String selectedIdTheso) {
        this.selectedIdTheso = selectedIdTheso;
    }

    public boolean isSortByNotation() {
        return sortByNotation;
    }

    public void setSortByNotation(boolean sortByNotation) {
        this.sortByNotation = sortByNotation;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public String getOptionThesoSelected() {
        return optionThesoSelected;
    }

    public void setOptionThesoSelected(String optionThesoSelected) {
        this.optionThesoSelected = optionThesoSelected;
    }

    public boolean isIsNetworkAvailable() {
        return isNetworkAvailable;
    }

}
