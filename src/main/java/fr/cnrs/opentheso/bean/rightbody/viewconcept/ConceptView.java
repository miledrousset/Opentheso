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
import fr.cnrs.opentheso.bdd.helper.CorpusHelper;
import fr.cnrs.opentheso.bdd.helper.PathHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePath;
import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "conceptView")
@SessionScoped
public class ConceptView implements Serializable {

    @Inject private Connect connect;
    @Inject private IndexSetting indexSetting;
    @Inject private ViewEditorThesoHomeBean viewEditorThesoHomeBean;
    @Inject private ViewEditorHomeBean viewEditorHomeBean;
    @Inject private Tree tree;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private SelectedTheso selectedTheso;

    private Map mapModel;
    private NodeConcept nodeConcept;
    private String selectedLang;
    private ArrayList <NodeCorpus> nodeCorpuses;

    
    private ArrayList<String> pathOfConcept;
    private ArrayList<NodePath> pathLabel;
    
    private ArrayList<Path> paths; // les hiérarchies du concept
    
    /// pagination
    private int sizeToShowNT;
    
    // total de la branche
    private int countOfBranch;
    
    
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
        nodeCorpuses = null;
        countOfBranch = 0;
    }

    /**
     * récuparation des informations pour le concept sélectionné
     * c'est pour la navigation entre les concepts dans la vue de droite avec deployement de l'arbre
     *
     * @param idTheso
     * @param idConcept
     * @param idLang
     */
    public void getConcept(String idTheso, String idConcept, String idLang) {
        ConceptHelper conceptHelper = new ConceptHelper();
        nodeConcept = conceptHelper.getConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang);
        if(nodeConcept == null) return;

        if (nodeConcept.getNodeGps() != null) {
            initMap();
        }

        pathOfConcept(idTheso, idConcept, idLang);
        setNotes();
        setSizeToShowNT();
        selectedLang = idLang;
        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();


        // récupération des informations sur les corpus liés
        CorpusHelper corpusHelper = new CorpusHelper();


        nodeCorpuses = corpusHelper.getAllActiveCorpus(connect.getPoolConnexion(), idTheso);
        if(nodeCorpuses!= null && !nodeCorpuses.isEmpty()) {
            setCorpus();
        }

        // deployement de l'arbre si l'option est true
        if(roleOnThesoBean.getNodePreference() != null) {
            if(roleOnThesoBean.getNodePreference().isAuto_expand_tree()){
                tree.expandTreeToPath(
                    idConcept,
                    idTheso,
                    idLang);
                PrimeFaces pf = PrimeFaces.current();;
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("formLeftTab:tabTree:tree");
                    pf.ajax().update("formSearch:languageSelect");
                    pf.executeScript("srollToSelected()");
                }
                selectedTheso.actionFromConceptToOn();
            }
        }
        countOfBranch = 0;
    }

    /**
     * récuparation des informations pour le concept sélectionné
     * après une sélection dans l'arbre
     *
     * @param idTheso
     * @param idConcept
     * @param idLang
     */
    public void getConceptForTree(String idTheso, String idConcept, String idLang) {
        ConceptHelper conceptHelper = new ConceptHelper();
        nodeConcept = conceptHelper.getConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang);
        if(nodeConcept != null) {
            pathOfConcept(idTheso, idConcept, idLang);
            setNotes();
            setSizeToShowNT();
        }
        // récupération des informations sur les corpus liés
        CorpusHelper corpusHelper = new CorpusHelper();
        nodeCorpuses = corpusHelper.getAllActiveCorpus(connect.getPoolConnexion(), idTheso);
        if(nodeCorpuses!= null && !nodeCorpuses.isEmpty()) {
            setCorpus();
        }
        if (nodeConcept.getNodeGps() != null) {
            initMap();
        }

        selectedLang = idLang;
        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();
        countOfBranch = 0;
    }
    
    public void countTheTotalOfBranch() {
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> listIdsOfBranch = conceptHelper.getIdsOfBranch(
                connect.getPoolConnexion(),
                nodeConcept.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        this.countOfBranch = listIdsOfBranch.size();
    }



    private void initMap()  {

        LatLong place = new LatLong(nodeConcept.getNodeGps().getLatitude()+"",
                nodeConcept.getNodeGps().getLongitude()+"");

        String titre = nodeConcept.getTerm() != null ? nodeConcept.getTerm().getLexical_value() : "";

        //Configure Map
        mapModel = new Map();
        mapModel.setWidth("100%");
        mapModel.setHeight("250px");
        mapModel.setCenter(place);
        mapModel.setZoom(13);
        mapModel.setAttribution(nodeConcept.getTerm().getLexical_value());
        mapModel.setMiniMap(false);
        mapModel.setLayerControl(false);
        mapModel.setDraggingEnabled(true);
        mapModel.setZoomEnabled(true);

        //addMarker
        mapModel.addLayer(new Layer().addMarker(new Marker(place, titre, new Pulse(true, 10, "#F47B2A"))));
    }

    private void setCorpus(){
        if(nodeConcept != null) {
            for (NodeCorpus nodeCorpuse : nodeCorpuses) {
                // recherche par Id
                if(nodeCorpuse.getUriCount().contains("##id##")){
                    if(nodeCorpuse.getUriCount() != null && !nodeCorpuse.getUriCount().isEmpty()) {
                        nodeCorpuse.setUriCount(nodeCorpuse.getUriCount().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                    }
                }
                if(nodeCorpuse.getUriLink().contains("##id##")){
                    nodeCorpuse.setUriLink(nodeCorpuse.getUriLink().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                }
                
                // recherche par value
                if(nodeCorpuse.getUriCount().contains("##value##")){
                    if(nodeCorpuse.getUriCount() != null && !nodeCorpuse.getUriCount().isEmpty()) {
                        nodeCorpuse.setUriCount(nodeCorpuse.getUriCount().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                    }
                }                
                if(nodeCorpuse.getUriLink().contains("##value##")){
                    nodeCorpuse.setUriLink(nodeCorpuse.getUriLink().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                }
                setCorpusCount(nodeCorpuse);
            /*    // recherche par Id
                if(nodeCorpuse.getUriLink().contains("##id##")){
                    nodeCorpuse.setUriLink(nodeCorpuse.getUriLink().replace("##id##", nodeConcept.getConcept().getIdConcept()));

                    if(nodeCorpuse.getUriCount() != null && !nodeCorpuse.getUriCount().isEmpty()) {
                        nodeCorpuse.setUriCount(nodeCorpuse.getUriCount().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                        setCorpusCount(nodeCorpuse);
                    }
                }
                // recherche par value
                if(nodeCorpuse.getUriLink().contains("##value##")){
                    nodeCorpuse.setUriLink(nodeCorpuse.getUriLink().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                    nodeCorpuse.setUriCount(nodeCorpuse.getUriCount().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                    setCorpusCount(nodeCorpuse);
                }  */              
                
            }
        }
    }

    private void setCorpusCount(NodeCorpus nodeCorpus){
        if(nodeConcept != null) {
            if(nodeCorpus == null) return;
            if(nodeCorpus.getUriCount().contains("https://")) {
                nodeCorpus.setCount(getCountOfResourcesFromHttps(nodeCorpus.getUriCount()));
            }
            if(nodeCorpus.getUriCount().contains("http://")) {
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
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((output = br.readLine()) != null) {
                json += output;
            }
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
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((output = br.readLine()) != null) {
                json += output;
            }
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
    
    private int getCountFromJson(String jsonText) {
        if(jsonText == null) return -1;
        JsonObject jsonObject;
        try ( //{"responseCode":1,"handle":"20.500.11942/opentheso443"}
            JsonReader reader = Json.createReader(new StringReader(jsonText))) {
            jsonObject = reader.readObject();
            return jsonObject.getInt("count");
        }
    }

    public int getCountOfBranch() {
        return countOfBranch;
    }

    public void setCountOfBranch(int countOfBranch) {
        this.countOfBranch = countOfBranch;
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
                case "historyNote" :
                    historyNotes.add(nodeNote);
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

    public ArrayList<NodeCorpus> getNodeCorpuses() {
        return nodeCorpuses;
    }

    public void setNodeCorpuses(ArrayList<NodeCorpus> nodeCorpuses) {
        this.nodeCorpuses = nodeCorpuses;
    }

    public Map getMapModel() {
        return mapModel;
    }
    
    
}
