package fr.cnrs.opentheso.models.nodes;

import lombok.Data;


@Data
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
    private String adminHandle;
    private int indexHandle;
    private boolean useHandleWithCertificat;
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
    private boolean uppercase_for_ark;
    
    // pour affichager le nom des personnes qui ont modifié les concepts
    private boolean displayUserName;
    
    /// pour masquer les notes à l'interface publique
    private boolean showHistoryNote;
    private boolean showEditorialNote;
    
    // pour gérer le module de traduction via Deepl
    private boolean use_deepl_translation;
    private String deepl_api_key;

}
