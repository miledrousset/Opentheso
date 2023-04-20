package fr.cnrs.opentheso.bdd.helper.nodes;


public class NodePreference {
    private String sourceLang;
    private int identifierType;
    private String preferredName;
    private boolean auto_expand_tree;
    private boolean sort_by_notation;
    private boolean tree_cache;    
    
    
    // paramètres Ark
    private boolean useArk;
    private String serveurArk;
    private String uriArk;
    private String idNaan;
    private String prefixArk;
    private String userArk;
    private String passArk;
    private boolean generateHandle;
    
    // pour acitver l'onglet ConceptTree(le dernnier)
    private boolean useConceptTree;
    
    // paramètres Handle
    private boolean useHandle;
    private String userHandle;
    private String passHandle;
    private String pathKeyHandle;
    private String pathCertHandle;
    private String urlApiHandle;
    private String prefixIdHandle;
    private String privatePrefixHandle;    
    
    
    
    private String pathImage;
    private String dossierResize;
    
    private boolean bddActive;
    private boolean bddUseId;
    private String urlBdd;
    private String urlCounterBdd;    
    
    private boolean z3950actif;
    private String collectionAdresse;
    private String noticeUrl;
    private String urlEncode;
    private String pathNotice1;
    private String pathNotice2;
    private String cheminSite;
    private boolean webservices;
    
    private String originalUri;
    private boolean originalUriIsArk;
    private boolean originalUriIsHandle;
    private boolean originalUriIsDoi;    

    /// pour la génération des identifants Ark en local
    private boolean useArkLocal;
    private String naanArkLocal;
    private String prefixArkLocal;
    private int sizeIdArkLocal;
    
    private boolean breadcrumb;
    
    private boolean suggestion;
    
    private boolean useCustomRelation;
    
    // pour affichager le nom des personnes qui ont modifié les concepts
    private boolean displayUserName;

            
    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public int getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(int identifierType) {
        this.identifierType = identifierType;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public boolean isUseArk() {
        return useArk;
    }

    public void setUseArk(boolean useArk) {
        this.useArk = useArk;
    }

    public String getServeurArk() {
        return serveurArk;
    }

    public void setServeurArk(String serveurArk) {
        this.serveurArk = serveurArk;
    }

    public String getUriArk() {
        return uriArk;
    }

    public void setUriArk(String uriArk) {
        this.uriArk = uriArk;
    }

    public String getIdNaan() {
        return idNaan;
    }

    public void setIdNaan(String idNaan) {
        this.idNaan = idNaan;
    }

    public String getPrefixArk() {
        return prefixArk;
    }

    public void setPrefixArk(String prefixArk) {
        this.prefixArk = prefixArk;
    }

    public String getUserArk() {
        return userArk;
    }

    public void setUserArk(String userArk) {
        this.userArk = userArk;
    }

    public String getPassArk() {
        return passArk;
    }

    public void setPassArk(String passArk) {
        this.passArk = passArk;
    }

    public boolean isUseHandle() {
        return useHandle;
    }

    public void setUseHandle(boolean useHandle) {
        this.useHandle = useHandle;
    }

    public String getUserHandle() {
        return userHandle;
    }

    public void setUserHandle(String userHandle) {
        this.userHandle = userHandle;
    }

    public String getPassHandle() {
        return passHandle;
    }

    public void setPassHandle(String passHandle) {
        this.passHandle = passHandle;
    }

    public String getPathKeyHandle() {
        return pathKeyHandle;
    }

    public void setPathKeyHandle(String pathKeyHandle) {
        this.pathKeyHandle = pathKeyHandle;
    }

    public String getPathCertHandle() {
        return pathCertHandle;
    }

    public void setPathCertHandle(String pathCertHandle) {
        this.pathCertHandle = pathCertHandle;
    }

    public String getUrlApiHandle() {
        return urlApiHandle;
    }

    public void setUrlApiHandle(String urlApiHandle) {
        this.urlApiHandle = urlApiHandle;
    }

    public String getPrefixIdHandle() {
        return prefixIdHandle;
    }

    public void setPrefixIdHandle(String prefixIdHandle) {
        this.prefixIdHandle = prefixIdHandle;
    }

    public String getPrivatePrefixHandle() {
        return privatePrefixHandle;
    }

    public void setPrivatePrefixHandle(String privatePrefixHandle) {
        this.privatePrefixHandle = privatePrefixHandle;
    }

    public String getPathImage() {
        return pathImage;
    }

    public void setPathImage(String pathImage) {
        this.pathImage = pathImage;
    }

    public String getDossierResize() {
        return dossierResize;
    }

    public void setDossierResize(String dossierResize) {
        this.dossierResize = dossierResize;
    }

    public boolean isBddActive() {
        return bddActive;
    }

    public void setBddActive(boolean bddActive) {
        this.bddActive = bddActive;
    }

    public boolean isBddUseId() {
        return bddUseId;
    }

    public void setBddUseId(boolean bddUseId) {
        this.bddUseId = bddUseId;
    }

    public String getUrlBdd() {
        return urlBdd;
    }

    public void setUrlBdd(String urlBdd) {
        this.urlBdd = urlBdd;
    }

    public String getUrlCounterBdd() {
        return urlCounterBdd;
    }

    public void setUrlCounterBdd(String urlCounterBdd) {
        this.urlCounterBdd = urlCounterBdd;
    }

    public boolean isZ3950actif() {
        return z3950actif;
    }

    public void setZ3950actif(boolean z3950actif) {
        this.z3950actif = z3950actif;
    }

    public String getCollectionAdresse() {
        return collectionAdresse;
    }

    public void setCollectionAdresse(String collectionAdresse) {
        this.collectionAdresse = collectionAdresse;
    }

    public String getNoticeUrl() {
        return noticeUrl;
    }

    public void setNoticeUrl(String noticeUrl) {
        this.noticeUrl = noticeUrl;
    }

    public String getUrlEncode() {
        return urlEncode;
    }

    public void setUrlEncode(String urlEncode) {
        this.urlEncode = urlEncode;
    }

    public String getPathNotice1() {
        return pathNotice1;
    }

    public void setPathNotice1(String pathNotice1) {
        this.pathNotice1 = pathNotice1;
    }

    public String getPathNotice2() {
        return pathNotice2;
    }

    public void setPathNotice2(String pathNotice2) {
        this.pathNotice2 = pathNotice2;
    }

    public String getCheminSite() {
        return cheminSite;
    }

    public void setCheminSite(String cheminSite) {
        this.cheminSite = cheminSite;
    }

    public boolean isWebservices() {
        return webservices;
    }

    public void setWebservices(boolean webservices) {
        this.webservices = webservices;
    }

    public String getOriginalUri() {
        return originalUri;
    }

    public void setOriginalUri(String originalUri) {
        this.originalUri = originalUri;
    }

    public boolean isOriginalUriIsArk() {
        return originalUriIsArk;
    }

    public void setOriginalUriIsArk(boolean originalUriIsArk) {
        this.originalUriIsArk = originalUriIsArk;
    }

    public boolean isOriginalUriIsHandle() {
        return originalUriIsHandle;
    }

    public void setOriginalUriIsHandle(boolean originalUriIsHandle) {
        this.originalUriIsHandle = originalUriIsHandle;
    }

    public boolean isGenerateHandle() {
        return generateHandle;
    }

    public void setGenerateHandle(boolean generateHandle) {
        this.generateHandle = generateHandle;
    }

    public boolean isAuto_expand_tree() {
        return auto_expand_tree;
    }

    public void setAuto_expand_tree(boolean auto_expand_tree) {
        this.auto_expand_tree = auto_expand_tree;
    }

    public boolean isSort_by_notation() {
        return sort_by_notation;
    }

    public void setSort_by_notation(boolean sort_by_notation) {
        this.sort_by_notation = sort_by_notation;
    }

    public boolean isTree_cache() {
        return tree_cache;
    }

    public void setTree_cache(boolean tree_cache) {
        this.tree_cache = tree_cache;
    }

    public boolean isOriginalUriIsDoi() {
        return originalUriIsDoi;
    }

    public void setOriginalUriIsDoi(boolean originalUriIsDoi) {
        this.originalUriIsDoi = originalUriIsDoi;
    }

    public boolean isUseArkLocal() {
        return useArkLocal;
    }

    public void setUseArkLocal(boolean useArkLocal) {
        this.useArkLocal = useArkLocal;
    }

    public String getNaanArkLocal() {
        return naanArkLocal;
    }

    public void setNaanArkLocal(String naanArkLocal) {
        this.naanArkLocal = naanArkLocal;
    }

    public String getPrefixArkLocal() {
        return prefixArkLocal;
    }

    public void setPrefixArkLocal(String prefixArkLocal) {
        this.prefixArkLocal = prefixArkLocal;
    }

    public int getSizeIdArkLocal() {
        return sizeIdArkLocal;
    }

    public void setSizeIdArkLocal(int sizeIdArkLocal) {
        this.sizeIdArkLocal = sizeIdArkLocal;
    }

    public boolean isBreadcrumb() {
        return breadcrumb;
    }

    public void setBreadcrumb(boolean breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    public boolean isUseConceptTree() {
        return useConceptTree;
    }

    public void setUseConceptTree(boolean useConceptTree) {
        this.useConceptTree = useConceptTree;
    }

    public boolean isDisplayUserName() {
        return displayUserName;
    }

    public void setDisplayUserName(boolean displayUserName) {
        this.displayUserName = displayUserName;
    }

    public boolean isSuggestion() {
        return suggestion;
    }

    public void setSuggestion(boolean suggestion) {
        this.suggestion = suggestion;
    }

    public boolean isUseCustomRelation() {
        return useCustomRelation;
    }

    public void setUseCustomRelation(boolean useCustomRelation) {
        this.useCustomRelation = useCustomRelation;
    }

    
}