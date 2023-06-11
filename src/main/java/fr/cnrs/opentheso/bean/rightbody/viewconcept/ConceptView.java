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
import com.jsf2leaf.model.Polyline;
import com.jsf2leaf.model.Pulse;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.CorpusHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.PathHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeConceptType;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCustomRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePath;
import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import fr.cnrs.opentheso.ws.RestRDFHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.ResponsiveOption;

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
    @Inject
    private Tree tree;
    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private LanguageBean languageBean;

    private Map mapModel;
    private NodeConcept nodeConcept;
    private String selectedLang;
    private ArrayList<NodeCorpus> nodeCorpuses;
    private ArrayList<NodePath> pathLabel;
    private ArrayList<NodeIdValue> nodeFacets;

    /// pagination
    private int offset;
    private int step;    
    private boolean haveNext;
    
    // total de la branche
    private int countOfBranch;

    // pour savoir si le concept a des relations vers des corpus
    private boolean haveCorpus;

    /// Notes concept
    private ArrayList<NodeNote> notes;
    private ArrayList<NodeNote> scopeNotes;

    //// Notes term    
    private ArrayList<NodeNote> changeNotes;
    private ArrayList<NodeNote> definitions;
    private ArrayList<NodeNote> editorialNotes;
    private ArrayList<NodeNote> examples;
    private ArrayList<NodeNote> historyNotes;

    private ArrayList<NodeCustomRelation> nodeCustomRelationReciprocals;
    
    private List<ResponsiveOption> responsiveOptions;
    
    private boolean toggleSwitchAltLabelLang;
    private boolean toggleSwitchNotesLang;    

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeCorpuses != null) {
            nodeCorpuses.clear();
            nodeCorpuses = null;
        }
        if (pathLabel != null) {
            pathLabel.clear();
            pathLabel = null;
        }
        if (notes != null) {
            notes.clear();
            notes = null;
        }
        if (scopeNotes != null) {
            scopeNotes.clear();
            scopeNotes = null;
        }
        if (changeNotes != null) {
            changeNotes.clear();
            changeNotes = null;
        }
        if (definitions != null) {
            definitions.clear();
            definitions = null;
        }
        if (editorialNotes != null) {
            editorialNotes.clear();
            editorialNotes = null;
        }
        if (examples != null) {
            examples.clear();
            examples = null;
        }
        if (historyNotes != null) {
            historyNotes.clear();
            historyNotes = null;
        }
        if (nodeConcept != null) {
            nodeConcept.clear();
            nodeConcept = null;
        }
        if (nodeFacets != null) {
            nodeFacets.clear();
            nodeFacets = null;
        }        
        
        selectedLang = null;
        mapModel = null;
        nodeCustomRelationReciprocals = null;
    }

    /**
     * Creates a new instance of ConceptBean
     */
    public ConceptView() {
    }

    public void init() {
        toggleSwitchAltLabelLang = false;
        toggleSwitchNotesLang = false;
        if (nodeConcept != null) {
            nodeConcept.clear();
        }
        selectedLang = null;

        if (nodeFacets == null) {
            nodeFacets = new ArrayList<>();
        }        
        clearNotes();
        
        offset = 0;
        step = 20;
        haveNext = false;
        
        nodeCorpuses = null;
        countOfBranch = 0;
        haveCorpus = false;
        nodeCustomRelationReciprocals = null;

        if (mapModel == null) {
            mapModel = new Map();
        }
        
        responsiveOptions = new ArrayList<>();
        responsiveOptions.add(new ResponsiveOption("1024px", 5));
        responsiveOptions.add(new ResponsiveOption("768px", 3));
        responsiveOptions.add(new ResponsiveOption("560px", 1));
    }

    private void clearNotes() {
        if (notes == null) {
            notes = new ArrayList<>();
        } else 
            notes.clear();
                    
        if (scopeNotes == null) {
            scopeNotes = new ArrayList<>();
        } else 
            scopeNotes.clear();
        
        if (changeNotes == null) {
            changeNotes = new ArrayList<>();
        } else 
            changeNotes.clear();
        
        if (definitions == null) {
            definitions = new ArrayList<>();
        } else 
            definitions.clear();
        
        if (editorialNotes == null) {
            editorialNotes = new ArrayList<>();
        } else 
            editorialNotes.clear();
        
        if (examples == null) {
            examples = new ArrayList<>();
        } else 
            examples.clear();
        
        if (historyNotes == null) {
            historyNotes = new ArrayList<>();
        } else 
            historyNotes.clear();        
    }
    
    public String getDrapeauImg(String codePays) {
        if (StringUtils.isEmpty(codePays)) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/nu.svg";
        }
        
        return "https://countryflagsapi.com/png/" + codePays;      
    }
    public String getDrapeauImgLocal(String codePays) {
        if (StringUtils.isEmpty(codePays)) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/noflag.png";
        }
        return FacesContext.getCurrentInstance().getExternalContext()
                .getRequestContextPath() + "/resources/img/flag/" + codePays + ".png";   
    }    
    
    /**
     * permet de retourner le label du type de concept en focntion de la langue de l'interface
     * @param conceptType
     * @param idTheso
     * @return 
     */
    public String getLabelOfConceptType(String conceptType, String idTheso){
        String idLang;
        RelationsHelper relationsHelper = new RelationsHelper();
        idLang = getIdLangOfInterface();
        
        return relationsHelper.getLabelOfTypeConcept(connect.getPoolConnexion(),
                conceptType,
                idTheso, 
                idLang);
    }
    
    /**
     * récuparation des informations pour le concept sélectionné c'est pour la
     * navigation entre les concepts dans la vue de droite avec deployement de
     * l'arbre
     *
     * @param idTheso
     * @param idConcept
     * @param idLang
     */
    public void getConcept(String idTheso, String idConcept, String idLang) {
        offset = 0;
        nodeConcept = new ConceptHelper().getConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang, step+1, offset);
        if (nodeConcept == null) {
            return;
        }
        // permet de récupérer les qualificatifs
        if(roleOnThesoBean.getNodePreference().isUseCustomRelation()){
            String interfaceLang = getIdLangOfInterface();            

            nodeConcept.setNodeCustomRelations(new RelationsHelper().getAllNodeCustomRelation(
                    connect.getPoolConnexion(), idConcept, idTheso, idLang, interfaceLang));
            setNodeCustomRelationWithReciprocal(nodeConcept.getNodeCustomRelations());
        }        
        
        setOffset();
        if (nodeConcept.getNodeGps() != null) {
            initMap();
        }
        selectedLang = idLang;
        if(toggleSwitchAltLabelLang) {
            getAltLabelWithAllLanguages();
        }
        if(toggleSwitchNotesLang) {
            getNotesWithAllLanguages();
        } else {
            setNotes();
        }


        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();

        // récupération des informations sur les corpus liés
        haveCorpus = false;
        nodeCorpuses = new CorpusHelper().getAllActiveCorpus(connect.getPoolConnexion(), idTheso);
        if (nodeCorpuses != null && !nodeCorpuses.isEmpty()) {
            setCorpus();
        }
        
        setFacetsOfConcept(idConcept, idTheso, idLang);

        // deployement de l'arbre si l'option est true
        if (roleOnThesoBean.getNodePreference() != null) {
            if(roleOnThesoBean.getNodePreference().isBreadcrumb())
                pathOfConcept(idTheso, idConcept, idLang);
            
            if (roleOnThesoBean.getNodePreference().isAuto_expand_tree()) {
                tree.expandTreeToPath(
                        idConcept,
                        idTheso,
                        idLang);
                if (PrimeFaces.current().isAjaxRequest()) {
                    PrimeFaces.current().ajax().update("containerIndex:formLeftTab:tabTree:tree");
                    PrimeFaces.current().ajax().update("containerIndex:languageSelect");
                }
                selectedTheso.actionFromConceptToOn();
            }
        }
        
        countOfBranch = 0;
    }

    /**
     * récuparation des informations pour le concept sélectionné après une
     * sélection dans l'arbre
     *
     * @param idTheso
     * @param idConcept
     * @param idLang
     */
    public void getConceptForTree(String idTheso, String idConcept, String idLang) {
        offset = 0; 
        ConceptHelper conceptHelper = new ConceptHelper();
        nodeConcept = conceptHelper.getConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang, step+1, offset);
        if(nodeConcept == null) return;
        
        // permet de récupérer les qualificatifs
        if(roleOnThesoBean.getNodePreference().isUseCustomRelation()){
            String interfaceLang = getIdLangOfInterface();
            nodeConcept.setNodeCustomRelations(new RelationsHelper().getAllNodeCustomRelation(
                    connect.getPoolConnexion(), idConcept, idTheso, idLang, interfaceLang));
            setNodeCustomRelationWithReciprocal(nodeConcept.getNodeCustomRelations());
        }
            


        if(roleOnThesoBean.getNodePreference().isBreadcrumb())
            pathOfConcept(idTheso, idConcept, idLang);

        if(toggleSwitchAltLabelLang) {
            getAltLabelWithAllLanguages();
        }
        if(toggleSwitchNotesLang) {
            getNotesWithAllLanguages();
        } else {
            setNotes();
        }
        
        setOffset();
        
        // récupération des informations sur les corpus liés
        haveCorpus = false;
        nodeCorpuses = new CorpusHelper().getAllActiveCorpus(connect.getPoolConnexion(), idTheso);
        if (nodeCorpuses != null && !nodeCorpuses.isEmpty()) {
            setCorpus();
        }
        if (nodeConcept.getNodeGps() != null) {
            initMap();
        }

        setFacetsOfConcept(idConcept, idTheso, idLang);

        selectedLang = idLang;
        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();
        countOfBranch = 0;
    }

    /**
     * permet de récupérer toutes les notes dans toutes les langues
     */
    public void getNotesWithAllLanguages(){
        NoteHelper noteHelper = new NoteHelper();
        if(toggleSwitchNotesLang) {
            nodeConcept.setNodeNotesTerm(noteHelper.getListNotesTermAllLang(
                    connect.getPoolConnexion(), nodeConcept.getTerm().getId_term(), nodeConcept.getConcept().getIdThesaurus()));  
            nodeConcept.setNodeNotesConcept(noteHelper.getListNotesConceptAllLang(
                    connect.getPoolConnexion(), nodeConcept.getConcept().getIdConcept(), nodeConcept.getConcept().getIdThesaurus()));             
        } else {
            nodeConcept.setNodeNotesTerm(noteHelper.getListNotesTerm(
                    connect.getPoolConnexion(),
                    nodeConcept.getTerm().getId_term(),
                    nodeConcept.getConcept().getIdThesaurus(),
                    selectedLang));             
                   
            nodeConcept.setNodeNotesConcept(noteHelper.getListNotesConcept(
                    connect.getPoolConnexion(), nodeConcept.getConcept().getIdConcept(),
                    nodeConcept.getConcept().getIdThesaurus(),
                    selectedLang));               
        }
        setNotes();
        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }
    
    public void getAltLabelWithAllLanguages(){   
        TermHelper termHelper = new TermHelper();
        
        if(toggleSwitchAltLabelLang)
            nodeConcept.setNodeEM(termHelper.getAllNonPreferredTerms(
                connect.getPoolConnexion(), nodeConcept.getConcept().getIdConcept(), nodeConcept.getConcept().getIdThesaurus()));  
        else
            nodeConcept.setNodeEM(termHelper.getNonPreferredTerms(connect.getPoolConnexion(),
                    nodeConcept.getTerm().getId_term(),
                    nodeConcept.getConcept().getIdThesaurus(),
                    selectedLang));
        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }    
    
    
    private void setFacetsOfConcept(String idConcept, String idTheso, String idLang){
        FacetHelper facetHelper = new FacetHelper();
        List<String> facetIds = facetHelper.getAllIdFacetsConceptIsPartOf(connect.getPoolConnexion(), idConcept, idTheso);
        if(nodeFacets == null)
            nodeFacets = new ArrayList<>();
        else
            nodeFacets.clear();
        for (String facetId : facetIds) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(facetId);
            nodeIdValue.setValue(facetHelper.getLabelOfFacet(connect.getPoolConnexion(), facetId, idTheso, idLang));
            nodeFacets.add(nodeIdValue);
        }
    }

    public void countTheTotalOfBranch() {
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> listIdsOfBranch = conceptHelper.getIdsOfBranch(
                connect.getPoolConnexion(),
                nodeConcept.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        this.countOfBranch = listIdsOfBranch.size();
    }

    private void initMap() {
        LatLong place = new LatLong(nodeConcept.getNodeGps().getLatitude() + "",
                nodeConcept.getNodeGps().getLongitude() + "");

        String titre = nodeConcept.getTerm() != null ? nodeConcept.getTerm().getLexical_value() : "";
        titre = titre.replaceAll("'", "_");
        
        mapModel = new Map();
        mapModel.setWidth("100%");
        mapModel.setHeight("250px");
        mapModel.setCenter(place);
        mapModel.setZoom(13);
        mapModel.setAttribution("©<a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a>");
        mapModel.setMiniMap(false);
        mapModel.setLayerControl(false);
        mapModel.setDraggingEnabled(true);
        mapModel.setZoomEnabled(true);
        
        // pour marquer un point 
        Layer placesLayer = (new Layer()).setLabel(titre);
        placesLayer.addMarker(new Marker(new LatLong(nodeConcept.getNodeGps().getLatitude() + "",
                nodeConcept.getNodeGps().getLongitude() + ""), titre, new Pulse(true, 10, "#F47B2A")));
        
        // pour marquer un polyline
        Layer polycircleLayer = (new Layer()).setLabel(titre);
        polycircleLayer.addPolyline((new Polyline())
             /*   .addPoint(new LatLong(nodeConcept.getNodeGps().getLatitude() + "", nodeConcept.getNodeGps().getLongitude() + ""))
                .addPoint(new LatLong("45.7466304","4.8344027"))
                .addPoint(new LatLong("45.7575158","4.831995239159291"))
                .addPoint(new LatLong("45.766704700000005","4.833645109701492"))
                .addPoint(new LatLong("45.7592161","4.8473512"))  
                .addPoint(new LatLong(nodeConcept.getNodeGps().getLatitude() + "", nodeConcept.getNodeGps().getLongitude() + ""))*/
                .setColor("#F47B2A")
        );
        mapModel.addLayer(placesLayer).addLayer(polycircleLayer);
    }

    private void setCorpus() {
        if (nodeConcept != null) {
            for (NodeCorpus nodeCorpuse : nodeCorpuses) {
                // cas où on compose uniquement une URL de lien vers les notices
                if (nodeCorpuse.isIsOnlyUriLink()) {
                    if (nodeCorpuse.getUriLink().contains("##id##")) {
                        nodeCorpuse.setUriLink(nodeCorpuse.getUriLink().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                        haveCorpus = true;
                    }
                    if (nodeCorpuse.getUriLink().contains("##value##")) {
                        nodeCorpuse.setUriLink(nodeCorpuse.getUriLink().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                        haveCorpus = true;
                    }
                } else {
                    // recherche par Id
                    
                    /// pour le count par Id interne
                    if (nodeCorpuse.getUriCount().contains("##id##")) {
                        if (nodeCorpuse.getUriCount() != null && !nodeCorpuse.getUriCount().isEmpty()) {
                            nodeCorpuse.setUriCount(nodeCorpuse.getUriCount().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                        }
                    }
                    /// pour le count par Id ark
                    if (nodeCorpuse.getUriCount().contains("##arkid##")) {
                        if (nodeCorpuse.getUriCount() != null && !nodeCorpuse.getUriCount().isEmpty()) {
                            nodeCorpuse.setUriCount(nodeCorpuse.getUriCount().replace("##arkid##", nodeConcept.getConcept().getIdArk()));
                        }
                    }                    
                    
                    /// pour la construction de l'URL avec Id interne
                    if (nodeCorpuse.getUriLink().contains("##id##")) {
                        nodeCorpuse.setUriLink(nodeCorpuse.getUriLink().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                    }
                    /// pour la construction de l'URL avec Id Ark
                    if (nodeCorpuse.getUriLink().contains("##arkid##")) {
                        nodeCorpuse.setUriLink(nodeCorpuse.getUriLink().replace("##arkid##", nodeConcept.getConcept().getIdArk()));
                    }                    

                    // recherche par value
                    if (nodeCorpuse.getUriCount().contains("##value##")) {
                        if (nodeCorpuse.getUriCount() != null && !nodeCorpuse.getUriCount().isEmpty()) {
                            nodeCorpuse.setUriCount(nodeCorpuse.getUriCount().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                        }
                    }
                    if (nodeCorpuse.getUriLink().contains("##value##")) {
                        nodeCorpuse.setUriLink(nodeCorpuse.getUriLink().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                    }
                    setCorpusCount(nodeCorpuse);
                }
            }
        }
    }

    private void setCorpusCount(NodeCorpus nodeCorpus) {
        if (nodeConcept != null) {
            if (nodeCorpus == null) {
                return;
            }
            if (nodeCorpus.getUriCount().contains("https://")) {
                nodeCorpus.setCount(getCountOfResourcesFromHttps(nodeCorpus.getUriCount()));
            }
            if (nodeCorpus.getUriCount().contains("http://")) {
                nodeCorpus.setCount(getCountOfResourcesFromHttp(nodeCorpus.getUriCount()));
            }
        }
    }

    private int getCountOfResourcesFromHttps(String uri) {
        String output;
        String json = "";
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        // récupération du total des notices
        try {
            URL url = new URL(uri);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            int status = conn.getResponseCode();
            if (status != 200) {
                return -1;
            }
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((output = br.readLine()) != null) {
                json += output;
            }
            br.close();
            return getCountFromJson(json);

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private int getCountOfResourcesFromHttp(String uri) {
        String output;
        String json = "";

        // récupération du total des notices
        try {
            URL url = new URL(uri);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            int status = conn.getResponseCode();
            if (status != 200) {
                return -1;
            }
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((output = br.readLine()) != null) {
                json += output;
            }
            br.close();
            return getCountFromJson(json);

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (IOException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        }
        return -1;
    }

    private int getCountFromJson(String jsonText) {
        if (jsonText == null) {
            return -1;
        }
        JsonObject jsonObject;
        try {
            JsonReader reader = Json.createReader(new StringReader(jsonText));
            jsonObject = reader.readObject();
            //         System.err.println(jsonText + " #### " + nodeConcept.getConcept().getIdConcept());
            int count = jsonObject.getInt("count");
            if (count > 0) {
                haveCorpus = true;
            }
            return count;
        } catch (Exception e) {
            System.err.println(e + " " + jsonText + " " + nodeConcept.getConcept().getIdConcept());
            return -1;
        }
    }

    public String getMetaData() {
        if (nodeConcept == null || nodeConcept.getConcept() == null || nodeConcept.getConcept().getIdConcept().isEmpty()) {
            return "";
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(connect.getPoolConnexion(),
                nodeConcept.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                "application/ld+json");
        if (datas == null) {
            return "";
        }
        return datas;
    }

    public int getCountOfBranch() {
        return countOfBranch;
    }

    public void setCountOfBranch(int countOfBranch) {
        this.countOfBranch = countOfBranch;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset() {
        if(nodeConcept.getNodeNT().size() < step) {
            offset = 0;
            haveNext = false;
        } else {
            offset = offset + step+1;
            haveNext = true;
        } 

    }
    
    public void setNodeCustomRelationWithReciprocal(ArrayList<NodeCustomRelation> nodeCustomRelations) {
        nodeCustomRelationReciprocals = new ArrayList<>();
        for (NodeCustomRelation nodeCustomRelation : nodeCustomRelations) {
            if(nodeCustomRelation.isReciprocal())
                nodeCustomRelationReciprocals.add(nodeCustomRelation);
        }
        if(nodeCustomRelationReciprocals.isEmpty())
            nodeCustomRelationReciprocals = null;
    }
    
    public void getNextNT(String idTheso, String idConcept, String idLang) {
    /*    if(tree != null 
                && CollectionUtils.isNotEmpty(tree.getClickselectedNodes()) 
                && tree.getClickselectedNodes().get(0) != null 
                && tree.getClickselectedNodes().get(0).getData() != null) {*/
            if(tree != null && tree.getSelectedNode() != null && tree.getSelectedNode().getData() != null) {            
            RelationsHelper relationsHelper = new RelationsHelper();
          /*  ArrayList<NodeNT> nodeNTs = relationsHelper.getListNT(connect.getPoolConnexion(),
                    ((TreeNodeData) tree.getClickselectedNodes().get(0).getData()).getNodeId(),
                    idTheso,
                    idLang, step+1, offset);*/
            ArrayList<NodeNT> nodeNTs = relationsHelper.getListNT(connect.getPoolConnexion(),
                    ((TreeNodeData) tree.getSelectedNode().getData()).getNodeId(),
                    idTheso,
                    idLang, step+1, offset);            
            if(nodeNTs != null && !nodeNTs.isEmpty()) {
                nodeConcept.getNodeNT().addAll(nodeNTs);
                setOffset();
                return;
            }
            haveNext = false;
        }
    } 

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
    
    private void pathOfConcept(String idTheso, String idConcept, String idLang) {
        PathHelper pathHelper = new PathHelper();
        List<Path> paths = pathHelper.getPathOfConcept2(
                connect.getPoolConnexion(), idConcept, idTheso);
        if(pathHelper.getMessage() != null){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", pathHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        if (paths == null) {
            System.out.println("Erreur de path pour le concept :" + idConcept);
            if (pathLabel != null) {
                pathLabel.clear();
            }
            return;
        }
        //pathOfConcept = getPathFromArray(paths);
        pathLabel = pathHelper.getPathWithLabel(connect.getPoolConnexion(), paths, idTheso, idLang, idConcept);
    }

    public String getCreator() {
        if (nodeConcept == null || nodeConcept.getConcept() == null || nodeConcept.getTerm() == null) {
            return "";
        }
        UserHelper userHelper = new UserHelper();
        return userHelper.getNameUser(connect.getPoolConnexion(), nodeConcept.getConcept().getCreator());
    }

    public String getContributor() {
        if (nodeConcept == null || nodeConcept.getConcept() == null || nodeConcept.getTerm() == null) {
            return "";
        }
        UserHelper userHelper = new UserHelper();;
        return userHelper.getNameUser(connect.getPoolConnexion(), nodeConcept.getConcept().getContributor());
    }
    
    public String getNoteSource(String noteSource){
        if(StringUtils.isEmpty(noteSource))
            return "";
        else
            return " (" + noteSource + ")";
    }

/////////////////////////////////
/////////////////////////////////
// fonctions pour les notes /////    
/////////////////////////////////
/////////////////////////////////
    private void setNotes() {
        clearNotes();
        for (NodeNote nodeNote : nodeConcept.getNodeNotesConcept()) {
            switch (nodeNote.getNotetypecode()) {
                case "note":
                    notes.add(nodeNote);
                    break;
                case "scopeNote":
                    scopeNotes.add(nodeNote);
                    break;
            }
        }
        for (NodeNote nodeNote : nodeConcept.getNodeNotesTerm()) {
            switch (nodeNote.getNotetypecode()) {
                case "changeNote":
                    changeNotes.add(nodeNote);
                    break;
                case "definition":
                    definitions.add(nodeNote);
                    break;
                case "editorialNote":
                    editorialNotes.add(nodeNote);
                    break;
                case "example":
                    examples.add(nodeNote);
                    break;
                case "historyNote":
                    historyNotes.add(nodeNote);
                    break;
            }
        }
    }
    
    public String getColorOfTypeConcept(){
        if("concept".equalsIgnoreCase(nodeConcept.getConcept().getConceptType()))
                return "";
        else
            return "#fcd8bf";
    }
    
    public String geLabelReciprocal(NodeConceptType nodeConceptType){
        if("concept".equalsIgnoreCase(nodeConceptType.getCode())) {
            return "";
        }
        String idLang = languageBean.getIdLangue();
        if(nodeConceptType.isReciprocal()) {
            if("fr".equalsIgnoreCase(idLang)){
                return " - Relation réciproque";
            }
            if("en".equalsIgnoreCase(idLang)){
                return " - Reciprocal relation";
            }            
        } else {
            if("fr".equalsIgnoreCase(idLang)){
                return " - Relation à sens unique";
            }
            if("en".equalsIgnoreCase(idLang)){
                return " - One-way relationship";
            }               
        }
        return "";
    }
    
    public void changeStateAltLabelOtherLang() {
        
    }

    public NodeConcept getNodeConcept() {
        return nodeConcept;
    }

    public void setNodeConcept(NodeConcept nodeConcept) {
        this.nodeConcept = nodeConcept;
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

    public ArrayList<NodeCorpus> getNodeCorpuses() {
        return nodeCorpuses;
    }

    public void setNodeCorpuses(ArrayList<NodeCorpus> nodeCorpuses) {
        this.nodeCorpuses = nodeCorpuses;
    }

    public Map getMapModel() {
        return mapModel;
    }

    public boolean isHaveCorpus() {
        return haveCorpus;
    }

    public void setHaveCorpus(boolean haveCorpus) {
        this.haveCorpus = haveCorpus;
    }

    public boolean isHaveNext() {
        return haveNext;
    }

    public void setHaveNext(boolean haveNext) {
        this.haveNext = haveNext;
    }

    public List<ResponsiveOption> getResponsiveOptions() {
        return responsiveOptions;
    }

    public ArrayList<NodeIdValue> getNodeFacets() {
        return nodeFacets;
}

    public void setNodeFacets(ArrayList<NodeIdValue> nodeFacets) {
        this.nodeFacets = nodeFacets;
    }

    public boolean isToggleSwitchAltLabelLang() {
        return toggleSwitchAltLabelLang;
    }

    public void setToggleSwitchAltLabelLang(boolean toggleSwitchAltLabelLang) {
        this.toggleSwitchAltLabelLang = toggleSwitchAltLabelLang;
    }

    public boolean isToggleSwitchNotesLang() {
        return toggleSwitchNotesLang;
    }

    public void setToggleSwitchNotesLang(boolean toggleSwitchNotesLang) {
        this.toggleSwitchNotesLang = toggleSwitchNotesLang;
    }

    public ArrayList<NodeCustomRelation> getNodeCustomRelationReciprocals() {
        return nodeCustomRelationReciprocals;
    }

    public void setNodeCustomRelationReciprocals(ArrayList<NodeCustomRelation> nodeCustomRelationReciprocals) {
        this.nodeCustomRelationReciprocals = nodeCustomRelationReciprocals;
    }

    /**
     * permet de retouver la langue de l'interface et se limiter au fr et en
     * @return 
     */
    private String getIdLangOfInterface(){
        String idLang;
        if("en".equalsIgnoreCase(languageBean.getIdLangue()) || "fr".equalsIgnoreCase(languageBean.getIdLangue())){
            idLang = languageBean.getIdLangue();
        } else
            idLang = "en";
        return idLang;  
    }
}
