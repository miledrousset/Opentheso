package fr.cnrs.opentheso.bean.menu.theso;

import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.viewconcepts.TreeConcepts;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.leftbody.viewliste.ListIndex;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import fr.cnrs.opentheso.bean.search.SearchBean;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.PrimeFaces;

@Named(value = "selectedTheso")
@SessionScoped
public class SelectedTheso implements Serializable {
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

    private static final long serialVersionUID = 1L;

    private String selectedIdTheso;
    private String currentIdTheso;

    private ArrayList<NodeLangTheso> nodeLangs;

    private String selectedLang; // la langue qu'on vient de séléctionner
    private String currentLang; // la langue en cours dans la session
    private boolean isActionFromConcept;

    private String idConceptFromUri;
    private String idThesoFromUri;

    private boolean isUriRequest = false;

    private String thesoName;
    private boolean sortByNotation;
    
    private String localUri;

    @PreDestroy
    public void destroy(){
        /// c'est le premier composant qui se détruit
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
        System.gc();
        System.runFinalization();        
    }      
    
    @PostConstruct
    public void initializing() {
        if (!connect.isConnected()) {
            System.err.println("Erreur de connexion BDD");
            return;
        }
        
        ///////
        ////// ne pas modifier, elle permet de détecter si le timeOut est déclenché pour vider la mémoire
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest req = (HttpServletRequest) externalContext.getRequest();
        if(!req.getSession().isNew()){
            System.gc();
            System.runFinalization();
        }        
        ///////
        ////// ne pas modifier, elle permet de détecter si le timeOut est déclenché pour vider la mémoire        
        
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

    /**
     * Permet de charger le thésaurus sélectionné C'est le point d'entrée de
     * l'application
     */
//    public void setSelectedTheso() throws IOException {
    public void setSelectedTheso() {
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        localUri = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()+"/";  
        connect.setLocalUri(localUri);
        
        searchBean.reset();
        viewEditorThesoHomeBean.reset();
        viewEditorHomeBean.reset();
        if (isUriRequest) {
            isUriRequest = false;
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
            
            PrimeFaces pf = PrimeFaces.current();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex");
            }

            return;
        }
        
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
        indexSetting.setIsValueSelected(false);
        indexSetting.setIsHomeSelected(true);
        indexSetting.setIsThesoActive(true);
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex");
        }
    }
    
    /**
     * Permet de Re-charger le thésaurus sélectionné, pour activer des mises à jour non prises en compte
     */
    public void reloadSelectedTheso() {
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
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        nodeLangs = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(connect.getPoolConnexion(), selectedIdTheso);

        if (idLang == null) {
            idLang = getIdLang();
            if (idLang == null || idLang.isEmpty()) {
                return;
            }
        }
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
        searchBean.reset();

        // initialisation de l'arbre des groupes
        treeGroups.initialise(selectedIdTheso, selectedLang);
        treeConcepts.initialise(selectedIdTheso, selectedLang);
        tree.initialise(selectedIdTheso, selectedLang);
        if (!isActionFromConcept) {
            conceptBean.init();
        }
        isActionFromConcept = false;
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
     * @return 
     */
    public void preRenderView() throws IOException {
        if (idThesoFromUri == null) {
            return;
        }
        if (idThesoFromUri.equalsIgnoreCase(selectedIdTheso)) {
            if (idConceptFromUri == null || idConceptFromUri.isEmpty()) {
                // accès au même thésaurus, on l'ignore 
                initIdsFromUri();
                return;
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
        //    tree.expandTreeToPath(idConceptFromUri, idThesoFromUri, currentLang);
            initIdsFromUri();
            return;
        }

        // gestion de l'accès par thésaurus d'un identifiant différent 
        if (!idThesoFromUri.equalsIgnoreCase(selectedIdTheso)) {
            if (isValidTheso(idThesoFromUri)) {
                selectedIdTheso = idThesoFromUri;
                startNewTheso(currentLang);
                if (idConceptFromUri != null && !idConceptFromUri.isEmpty()) {
                    conceptBean.getConcept(currentIdTheso, idConceptFromUri, currentLang);
                    actionFromConceptToOn();
                /*    if (conceptBean.getNodeConcept() != null) {
                        tree.expandTreeToPath(idConceptFromUri, idThesoFromUri, currentLang);
                    }*/
                }
            } else {
                return;
            }
        }

        indexSetting.setIsSelectedTheso(true);
        indexSetting.setIsThesoActive(true);
  //      rightBodySetting.setShowConceptToOn();
        rightBodySetting.setIndex("0");
  //      indexSetting.setIsValueSelected(true);

        // ne marche pas
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formMenu");
            pf.ajax().update("formLeftTab");
            pf.ajax().update("formSearch:languageSelect");
            pf.ajax().update("formSearch");
            pf.ajax().update("formRightTab");
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

}
