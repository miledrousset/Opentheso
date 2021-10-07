/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.importexport;

import fr.cnrs.opentheso.bdd.datas.Languages_iso639;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.LanguageHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentImport;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentSmall;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewEditionBean;
import fr.cnrs.opentheso.core.imports.csv.CsvImportHelper;
import fr.cnrs.opentheso.core.imports.csv.CsvReadHelper;
import fr.cnrs.opentheso.core.imports.rdf4j.ReadRdf4j;
import fr.cnrs.opentheso.core.imports.rdf4j.helper.ImportRdf4jHelper;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author miledrousset
 */
@Named(value = "importFileBean")
@SessionScoped

public class ImportFileBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private CurrentUser currentUser;
    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    @Inject
    private ViewEditionBean viewEditionBean;
    @Inject
    private ConceptView conceptView;
    @Inject
    private Tree tree;
    @Inject
    private CandidatBean candidatBean;
    @Inject
    private SelectedTheso selectedTheso;

    private double progress = 0;
    private double progressStep = 0;

    private int typeImport;
    private String selectedIdentifier = "sans";
    private String prefixHandle;
    private boolean isCandidatImport;
    private String prefixDoi;

    // import CSV
    private char delimiterCsv = ',';
    private int choiceDelimiter = 0;
    private String thesaurusName;
    private ArrayList<CsvReadHelper.ConceptObject> conceptObjects;
    private ArrayList<String> langs;
    private ArrayList<NodeAlignmentImport> nodeAlignmentImports; 
    
    private ArrayList<NodeNote> nodeNotes;     

    private String formatDate = "yyyy-MM-dd";
    private String uri;
    private double total;

    private boolean loadDone = false;
    private boolean BDDinsertEnable = false;
    private boolean importDone = false;
    private boolean importInProgress = false;

    private SKOSXmlDocument sKOSXmlDocument;
    private String info = "";
    private StringBuffer error = new StringBuffer();
    private String warning = "";

    private ArrayList<NodeUserGroup> nodeUserProjects;
    private String selectedUserProject;

    private ArrayList<Languages_iso639> allLangs;
    private String selectedLang;

    private boolean haveError;
    
    private boolean clearNoteBefore;

    @PreDestroy
    public void destroy() {
        clearMemory();
    }

    private void clearMemory() {
        if (conceptObjects != null) {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                conceptObject.clear();
            }
            conceptObjects.clear();
        }
        conceptObjects = null;

        if (langs != null) {
            langs.clear();
        }
        langs = null;

        if (allLangs != null) {
            allLangs.clear();
        }
        allLangs = null;
        if (nodeUserProjects != null) {
            nodeUserProjects.clear();
        }
        nodeUserProjects = null;
        if (sKOSXmlDocument != null) {
            sKOSXmlDocument.clear();
        }
        sKOSXmlDocument = null;
        if (nodeAlignmentImports != null) {
            nodeAlignmentImports.clear();
        }
        nodeAlignmentImports = null;        
        if (nodeNotes != null) {
            nodeNotes.clear();
        }        
        nodeNotes = null;

    }

    public void init() {
        choiceDelimiter = 0;
        delimiterCsv = ',';
        haveError = false;
        clearNoteBefore = false;
        progress = 0;
        progressStep = 0;
        info = "";
        prefixHandle = null;
        prefixDoi = null;
        error = new StringBuffer();
        warning = "";
        uri = "";
        formatDate = "yyyy-MM-dd";
        total = 0;
        loadDone = false;
        importDone = false;
        BDDinsertEnable = false;
        importInProgress = false;
        selectedIdentifier = "sans";
        sKOSXmlDocument = null;
        if (conceptObjects != null) {
            conceptObjects.clear();
        }
        if(nodeAlignmentImports != null) {
            nodeAlignmentImports.clear();
        }
        if (langs != null) {
            langs.clear();
        }
        
        if(nodeNotes != null) {
            nodeNotes.clear();
        }        

        // récupération des toutes les langues pour le choix de le langue source
        LanguageHelper languageHelper = new LanguageHelper();
        allLangs = languageHelper.getAllLanguages(connect.getPoolConnexion());
        selectedLang = null;
        thesaurusName = null;

        UserHelper userHelper = new UserHelper();
        selectedUserProject = "";
        if (currentUser.getNodeUser().isIsSuperAdmin()) {
            nodeUserProjects = userHelper.getAllProject(connect.getPoolConnexion());
        } else {
            nodeUserProjects = userHelper.getProjectsOfUserAsAdmin(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
            for (NodeUserGroup nodeUserProject : nodeUserProjects) {
                selectedUserProject = "" + nodeUserProject.getIdGroup();
            }
        }
    }

    public void actionChoice() {
        if (choiceDelimiter == 0) {
            delimiterCsv = ',';
        }
        if (choiceDelimiter == 1) {
            delimiterCsv = ';';
        }
        if (choiceDelimiter == 2) {
            delimiterCsv = '\t';
        }
    }
    
    
    /**
     * permet de charger un fichier de notes en Csv
     *
     * @param event
     */
    public void loadFileNoteCsv(FileUploadEvent event) {
        initError();
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);
            try (Reader reader1 = new InputStreamReader(event.getFile().getInputStream())) {

                if (!csvReadHelper.setLangs(reader1)) {
                    error.append(csvReadHelper.getMessage());
                } else {
                    try (Reader reader2 = new InputStreamReader(event.getFile().getInputStream())) {
                        if (!csvReadHelper.readFileNote(reader2)) {
                            error.append(csvReadHelper.getMessage());
                        }

                        warning = csvReadHelper.getMessage();
                        conceptObjects = csvReadHelper.getConceptObjects();
                        if (conceptObjects != null) {
                            if (conceptObjects.isEmpty()) {
                                haveError = true;
                                error.append(System.getProperty("line.separator"));
                                error.append("La lecture a échouée, vérifiez le séparateur des colonnes !!");
                                warning = "";
                            } else {
                                total = conceptObjects.size();
                                uri = "";//csvReadHelper.getUri();
                                loadDone = true;
                                BDDinsertEnable = true;
                                info = "File correctly loaded";
                            }
                        }
                    }                    
                }
            } catch (Exception e) {
                haveError = true;
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide()");            
        }
    }    
    
    /**
     * permet de charger un fichier en Csv
     *
     * @param event
     */
    public void loadFileAlignmentCsvToDelete(FileUploadEvent event) {
        initError();
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);

            try (Reader reader = new InputStreamReader(event.getFile().getInputStream())) {

                if (!csvReadHelper.readFileAlignmentToDelete(reader)) {
                    error.append(csvReadHelper.getMessage());
                }

                warning = csvReadHelper.getMessage();
                conceptObjects = csvReadHelper.getConceptObjects();
                if (conceptObjects != null) {
                    if (conceptObjects.isEmpty()) {
                        haveError = true;
                        error.append(System.getProperty("line.separator"));
                        error.append("La lecture a échouée, vérifiez le séparateur des colonnes !!");
                        warning = "";
                    } else {
                        total = conceptObjects.size();
                        uri = "";//csvReadHelper.getUri();
                        loadDone = true;
                        BDDinsertEnable = true;
                        info = "File correctly loaded";
                    }
                }
            } catch (Exception e) {
                haveError = true;
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
        }
    }    

    /**
     * permet de charger un fichier en Csv
     *
     * @param event
     */
    public void loadFileAlignmentCsv(FileUploadEvent event) {
        initError();
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);

            try (Reader reader = new InputStreamReader(event.getFile().getInputStream())) {

                if (!csvReadHelper.readFileAlignment(reader)) {
                    error.append(csvReadHelper.getMessage());
                }

                warning = csvReadHelper.getMessage();
                nodeAlignmentImports = csvReadHelper.getNodeAlignmentImports();
                if (nodeAlignmentImports != null) {
                    if (nodeAlignmentImports.isEmpty()) {
                        haveError = true;
                        error.append(System.getProperty("line.separator"));
                        error.append("La lecture a échouée, vérifiez le séparateur des colonnes !!");
                        warning = "";
                    } else {
                        total = nodeAlignmentImports.size();
                        uri = "";//csvReadHelper.getUri();
                        loadDone = true;
                        BDDinsertEnable = true;
                        info = "File correctly loaded";
                    }
                }
            } catch (Exception e) {
                haveError = true;
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
        }
    }

    /**
     * permet de charger un fichier en Csv
     *
     * @param event
     */
    public void loadFileCsv(FileUploadEvent event) {
        initError();
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);
            // première lecrture pour charger les langues
            try (Reader reader1 = new InputStreamReader(event.getFile().getInputStream())) {

                if (!csvReadHelper.setLangs(reader1)) {
                    error.append(csvReadHelper.getMessage());
                }
                //deuxième lecture pour les données
                try (Reader reader2 = new InputStreamReader(event.getFile().getInputStream())) {

                    if (!csvReadHelper.readFile(reader2)) {
                        error.append(csvReadHelper.getMessage());
                    }

                    warning = csvReadHelper.getMessage();
                    conceptObjects = csvReadHelper.getConceptObjects();
                    if (conceptObjects != null) {
                        if (conceptObjects.get(0).getPrefLabels() != null) {
                            if (conceptObjects.get(0).getPrefLabels().isEmpty()) {
                                haveError = true;
                                error.append(System.getProperty("line.separator"));
                                error.append("La lecture a échouée, vérifiez le séparateur des colonnes !!");
                                warning = "";
                            } else {
                                langs = csvReadHelper.getLangs();
                                total = conceptObjects.size();
                                uri = "";//csvReadHelper.getUri();
                                loadDone = true;
                                BDDinsertEnable = true;
                                info = "File correctly loaded";
                            }
                        }
                    }
                }
                PrimeFaces.current().executeScript("PF('waitDialog').hide()");
            } catch (Exception e) {
                haveError = true;
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide()");            
        }
    }

    /**
     * insérer un thésaurus dans la BDD (CSV)
     *
     */
    public void addCsvThesoToBDD() {

        loadDone = false;
        progressStep = 0;
        progress = 0;

        if (conceptObjects == null || conceptObjects.isEmpty()) {
            return;
        }

        if (importInProgress) {
            return;
        }

        initError();

        // préparer le projet pour le thésaurus
        int idProject;
        if (selectedUserProject == null || selectedUserProject.isEmpty()) {
            idProject = -1;
        } else {
            idProject = Integer.parseInt(selectedUserProject);
        }

        // préparer la langue source 
        if (selectedLang == null || selectedLang.isEmpty()) {
            selectedLang = connect.getWorkLanguage();
        }

        // création du thésaurus
        CsvImportHelper csvImportHelper = new CsvImportHelper();
        String idNewTheso = csvImportHelper.createTheso(connect.getPoolConnexion(), thesaurusName, selectedLang,
                idProject, currentUser.getNodeUser());

        // préparer les préférences du thésaurus, on récupérer les préférences du thésaurus en cours, ou on initialise des nouvelles.
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        NodePreference nodePreference = roleOnThesoBean.getNodePreference();

        if (nodePreference == null) {
            preferencesHelper.initPreferences(connect.getPoolConnexion(), idNewTheso, selectedLang);
        } else {
            nodePreference.setPreferredName(thesaurusName);
            nodePreference.setSourceLang(selectedLang);
            preferencesHelper.addPreference(connect.getPoolConnexion(), nodePreference, idNewTheso);
        }

        // ajout des concepts et collections
        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                switch (conceptObject.getType().trim().toLowerCase()) {
                    case "skos:concept":
                        // ajout de concept
                        csvImportHelper.addConcept(connect.getPoolConnexion(), idNewTheso, conceptObject);
                        break;
                    case "skos:collection":
                        // ajout de groupe
                        csvImportHelper.addGroup(connect.getPoolConnexion(), idNewTheso, conceptObject);
                        break;
                    default:
                        break;
                }

                progressStep++;
                progress = progressStep / total * 100;
            }

            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            total = 0;

            info = "Thesaurus correctly insert into data base";
            info = info + "\n" + csvImportHelper.getMessage();
            roleOnThesoBean.showListTheso();
            viewEditionBean.init();

            PrimeFaces pf = PrimeFaces.current();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("toolBoxForm");
                pf.ajax().update("toolBoxForm:listThesoForm");
            }

        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }

        conceptObjects = null;
        csvImportHelper = null;
        System.gc();
        System.gc();

        onComplete();
    }

///////////////////////////////////////////////////////////////////////////////
///////////////// Fonctions pour importer des données en CSV //////////////////
///////////////// L'ajout des données se fait en fusion  //////////////////////
///////////////////////////////////////////////////////////////////////////////    
    
    /**
     * permet d'ajouter une liste de notes en CSV au thésaurus
     *
     */
    public void addNoteList() {
        if (selectedTheso.getCurrentIdTheso() == null || selectedTheso.getCurrentIdTheso().isEmpty()) {
            warning = "pas de thésaurus sélectionné";
            return;
        }
        if (conceptObjects == null || conceptObjects.isEmpty()) {
            return;
        }
        if (importInProgress) {
            return;
        }
        initError();
        loadDone = false;
        progressStep = 0;
        progress = 0;
        total = 0;
        String idConcept = null;
        ConceptHelper conceptHelper = new ConceptHelper();
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        NodeAlignment nodeAlignment = new NodeAlignment();

        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                //définitions
                
                switch (conceptObject.getType().trim().toLowerCase()) {
                    case "skos:concept":
                        // ajout de concept
                        csvImportHelper.addConcept(connect.getPoolConnexion(), idNewTheso, conceptObject);
                        break;
                    case "skos:collection":
                        // ajout de groupe
                        csvImportHelper.addGroup(connect.getPoolConnexion(), idNewTheso, conceptObject);
                        break;
                    default:
                        break;
                }

                progressStep++;
                progress = progressStep / total * 100;
            }
        /*
        try {
            for (NodeAlignmentImport nodeAlignmentImport : nodeAlignmentImports) {
                if(nodeAlignmentImport == null) continue;
                if (nodeAlignmentImport.getLocalId()== null || nodeAlignmentImport.getLocalId().isEmpty()) {
                    continue;
                }
                if("ark".equalsIgnoreCase(selectedIdentifier)){
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.getPoolConnexion(), nodeAlignmentImport.getLocalId());
                }
                if("handle".equalsIgnoreCase(selectedIdentifier)){
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.getPoolConnexion(), nodeAlignmentImport.getLocalId());
                } 
                if("identifier".equalsIgnoreCase(selectedIdentifier)){
                    idConcept = nodeAlignmentImport.getLocalId();
                }                
                
                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                for (NodeAlignmentSmall nodeAlignmentSmall : nodeAlignmentImport.getNodeAlignmentSmalls()) {
                    if(nodeAlignmentSmall == null) continue;
                    nodeAlignment.setId_author(currentUser.getNodeUser().getIdUser());
                    nodeAlignment.setConcept_target("");
                    nodeAlignment.setThesaurus_target(nodeAlignmentSmall.getSource());
                    nodeAlignment.setInternal_id_concept(idConcept);
                    nodeAlignment.setInternal_id_thesaurus(selectedTheso.getCurrentIdTheso());
                    nodeAlignment.setAlignement_id_type(nodeAlignmentSmall.getAlignement_id_type());
                    nodeAlignment.setUri_target(nodeAlignmentSmall.getUri_target());
                    if (alignmentHelper.addNewAlignment(connect.getPoolConnexion(), nodeAlignment)) {
                        total++;
                    }
                }
            } */
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "import réussie, alignements importés = " + (int)total;            
            total = 0; 
        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
    }    
    
    
    /**
     * permet d'ajouter une liste d'alignements en CSV au thésaurus
     *
     */
    public void addAlignmentList() {
        if (selectedTheso.getCurrentIdTheso() == null || selectedTheso.getCurrentIdTheso().isEmpty()) {
            warning = "pas de thésaurus sélectionné";
            return;
        }
        if (nodeAlignmentImports == null || nodeAlignmentImports.isEmpty()) {
            warning = "pas de valeurs";
            return;
        }
        if (importInProgress) {
            return;
        }
        initError();
        loadDone = false;
        progressStep = 0;
        progress = 0;
        total = 0;
        String idConcept = null;
        ConceptHelper conceptHelper = new ConceptHelper();
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        NodeAlignment nodeAlignment = new NodeAlignment();

        try {
            for (NodeAlignmentImport nodeAlignmentImport : nodeAlignmentImports) {
                if(nodeAlignmentImport == null) continue;
                if (nodeAlignmentImport.getLocalId()== null || nodeAlignmentImport.getLocalId().isEmpty()) {
                    continue;
                }
                if("ark".equalsIgnoreCase(selectedIdentifier)){
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.getPoolConnexion(), nodeAlignmentImport.getLocalId());
                }
                if("handle".equalsIgnoreCase(selectedIdentifier)){
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.getPoolConnexion(), nodeAlignmentImport.getLocalId());
                } 
                if("identifier".equalsIgnoreCase(selectedIdentifier)){
                    idConcept = nodeAlignmentImport.getLocalId();
                }                
                
                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                for (NodeAlignmentSmall nodeAlignmentSmall : nodeAlignmentImport.getNodeAlignmentSmalls()) {
                    if(nodeAlignmentSmall == null) continue;
                    nodeAlignment.setId_author(currentUser.getNodeUser().getIdUser());
                    nodeAlignment.setConcept_target("");
                    nodeAlignment.setThesaurus_target(nodeAlignmentSmall.getSource());
                    nodeAlignment.setInternal_id_concept(idConcept);
                    nodeAlignment.setInternal_id_thesaurus(selectedTheso.getCurrentIdTheso());
                    nodeAlignment.setAlignement_id_type(nodeAlignmentSmall.getAlignement_id_type());
                    nodeAlignment.setUri_target(nodeAlignmentSmall.getUri_target());
                    if (alignmentHelper.addNewAlignment(connect.getPoolConnexion(), nodeAlignment)) {
                        total++;
                    }
                }
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "import réussie, alignements importés = " + (int)total;            
            total = 0;
        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
    }
    
    
    /**
     * permet de supprimer une liste d'alignements en CSV du thésaurus
     *
     */
    public void deleteAlignmentFromCsv() {
        if (selectedTheso.getCurrentIdTheso() == null || selectedTheso.getCurrentIdTheso().isEmpty()) {
            warning = "pas de thésaurus sélectionné";
            return;
        }
        if (conceptObjects == null || conceptObjects.isEmpty()) {
            warning = "pas de valeurs";
            return;
        }
        if (importInProgress) {
            return;
        }
        initError();
        loadDone = false;
        progressStep = 0;
        progress = 0;
        total = 0;
        String idConcept = null;
        ConceptHelper conceptHelper = new ConceptHelper();
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                if (conceptObject.getLocalId() == null || conceptObject.getLocalId().isEmpty()) {
                    continue;
                }
                if("ark".equalsIgnoreCase(selectedIdentifier)){
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.getPoolConnexion(), conceptObject.getLocalId());
                }
                if("handle".equalsIgnoreCase(selectedIdentifier)){
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.getPoolConnexion(), conceptObject.getLocalId());
                } 
                if("identifier".equalsIgnoreCase(selectedIdentifier)){
                    idConcept = conceptObject.getLocalId();
                }  
                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                for (NodeIdValue nodeIdValue : conceptObject.getAlignments()) {
                    if(alignmentHelper.deleteAlignmentByUri(connect.getPoolConnexion(),
                            nodeIdValue.getValue().trim(),
                            idConcept,
                            selectedTheso.getCurrentIdTheso())) {
                        total++;
                    }
                }
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "Suppression réussie, alignements supprimés = " + (int)total;            
            total = 0;
        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
    }    
///////////////////////////////////////////////////////////////////////////////
//////////////////Fin Ajout des alignements de Wikidata////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////   

    /**
     * permet d'ajouter une liste de concepts en CSV sous le concept sélectionné
     * la liste peut être hiérarchisée si la relation BT est renseignée
     *
     * @param nodeConcept
     */
    public void addListCsvToConcept(NodeConcept nodeConcept) {
        if (conceptObjects == null || conceptObjects.isEmpty()) {
            warning = "pas de valeurs";
            return;
        }
        if (importInProgress) {
            return;
        }
        initError();
        loadDone = false;
        progressStep = 0;
        progress = 0;

        // préparer les préférences du thésaurus, on récupérer les préférences du thésaurus en cours
        NodePreference nodePreference = roleOnThesoBean.getNodePreference();
        if (nodePreference == null) {
            warning = "pas de préférences";
            return;
        }

        CsvImportHelper csvImportHelper = new CsvImportHelper(nodePreference);

        // ajout des concepts
        String idPere = nodeConcept.getConcept().getIdConcept();

        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                // gestion de l'hiérarchie pour les listes NT,
                // si le BT est renseigné, alors on intègre le concept sous ce BT,
                // sinon, c'est le père du dossier en cours qui est pris en compte
                if (conceptObject.getBroaders().isEmpty()) {
                    csvImportHelper.addSingleConcept(connect.getPoolConnexion(), nodeConcept.getConcept().getIdThesaurus(),
                            idPere, nodeConcept.getConcept().getIdGroup(), currentUser.getNodeUser().getIdUser(),
                            conceptObject);
                } else {
                    for (String idBT : conceptObject.getBroaders()) {
                        csvImportHelper.addSingleConcept(connect.getPoolConnexion(), nodeConcept.getConcept().getIdThesaurus(),
                                idBT, nodeConcept.getConcept().getIdGroup(), currentUser.getNodeUser().getIdUser(),
                                conceptObject);
                    }
                }
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            total = 0;
            info = "import réussie";
            info = info + "\n" + csvImportHelper.getMessage();

            // mise à jour
            PrimeFaces pf = PrimeFaces.current();
            if (tree.getSelectedNode() != null) {
                tree.initAndExpandTreeToPath(conceptView.getNodeConcept().getConcept().getIdConcept(),
                        nodeConcept.getConcept().getIdThesaurus(),
                        conceptView.getSelectedLang());
            }
            conceptView.getConcept(
                    nodeConcept.getConcept().getIdThesaurus(),
                    nodeConcept.getConcept().getIdConcept(),
                    conceptView.getSelectedLang());

            if (pf.isAjaxRequest()) {
                pf.ajax().update("formRightTab:viewTabConcept:idConceptNarrower");
                pf.ajax().update("formLeftTab:tabTree:tree");
            }

        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
    }

    /**
     * permet de charger des fichier de type skos, json-ld ou turtle avec la
     * variable membre typeImport pour choisir le type 0=skos 1=jsons-ld
     * 2=turtle
     *
     * @param event
     */
    public void loadFileSkos(FileUploadEvent event) {

        isCandidatImport = Boolean.parseBoolean((String) event.getComponent().getAttributes().get("isCandidatImport"));

        error = new StringBuffer();
        info = "";
        warning = "";
        switch (typeImport) {
            case 0:
                loadSkos(event, isCandidatImport);
                break;
            case 1:
                loadJsonLd(event, isCandidatImport);
                break;
            case 2:
                loadTurtle(event, isCandidatImport);
                break;
            case 3:
                loadJson(event, isCandidatImport);
                break;
        }
        PrimeFaces.current().executeScript("PF('waitDialog').hide()");
    }

    private void loadSkos(FileUploadEvent event, Boolean isCandidatImport) {
        progress = 0;

        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            try (InputStream is = event.getFile().getInputStream()) {
                ReadRdf4j readRdf4j = new ReadRdf4j(is, 0, isCandidatImport, connect.getWorkLanguage());
                warning = readRdf4j.getMessage();
                sKOSXmlDocument = readRdf4j.getsKOSXmlDocument();
                total = sKOSXmlDocument.getConceptList().size();
                uri = sKOSXmlDocument.getTitle();
                loadDone = true;
                BDDinsertEnable = true;
                info = "File correctly loaded";
                readRdf4j.clean();
                System.gc();
            } catch (Exception e) {
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
        }
    }

    private void loadJsonLd(FileUploadEvent event, boolean isCandidatImport) {
        progress = 0;

        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            try (InputStream is = event.getFile().getInputStream()) {
                ReadRdf4j readRdf4j = new ReadRdf4j(is, 1, isCandidatImport, connect.getWorkLanguage());
                warning = readRdf4j.getMessage();
                sKOSXmlDocument = readRdf4j.getsKOSXmlDocument();
                total = sKOSXmlDocument.getConceptList().size();
                uri = sKOSXmlDocument.getTitle();
                loadDone = true;
                BDDinsertEnable = true;
                info = "File correctly loaded";
            } catch (Exception e) {
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
        }
    }

    private void loadJson(FileUploadEvent event, boolean isCandidatImport) {

        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            try (InputStream is = event.getFile().getInputStream()) {
                ReadRdf4j readRdf4j = new ReadRdf4j(is, 3, isCandidatImport, connect.getWorkLanguage());
                warning = readRdf4j.getMessage();
                sKOSXmlDocument = readRdf4j.getsKOSXmlDocument();
                total = sKOSXmlDocument.getConceptList().size();
                uri = sKOSXmlDocument.getTitle();
                loadDone = true;
                BDDinsertEnable = true;
                info = "File correctly loaded";
            } catch (Exception e) {
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
        }
    }

    private void loadTurtle(FileUploadEvent event, boolean isCandidatImport) {

        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            try (InputStream is = event.getFile().getInputStream()) {
                ReadRdf4j readRdf4j = new ReadRdf4j(is, 2, isCandidatImport, connect.getWorkLanguage());
                warning = readRdf4j.getMessage();
                sKOSXmlDocument = readRdf4j.getsKOSXmlDocument();
                total = sKOSXmlDocument.getConceptList().size();
                uri = sKOSXmlDocument.getTitle();
                loadDone = true;
                BDDinsertEnable = true;
                info = "File correctly loaded";
            } catch (Exception e) {
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
        }
    }

    /**
     * permet d'importer le thésaurus chargé en SKOS
     */
    public void addSkosThesoToBDD() {
        error = new StringBuffer();
        info = "";
        warning = "";
        loadDone = false;

        if (importInProgress) {
            return;
        }
        int idGroup;

        if (selectedUserProject == null || selectedUserProject.isEmpty()) {
            idGroup = -1;
        } else {
            idGroup = Integer.parseInt(selectedUserProject);
        }
        try {
            progress = 0;
            progressStep = 0;
            importInProgress = true;
            ImportRdf4jHelper importRdf4jHelper = new ImportRdf4jHelper();
            importRdf4jHelper.setInfos(connect.getPoolConnexion(),
                    formatDate,
                    currentUser.getNodeUser().getIdUser(),
                    idGroup,
                    connect.getWorkLanguage());

            // pour récupérer les identifiants pérennes type Ark ou Handle
            importRdf4jHelper.setSelectedIdentifier(selectedIdentifier);

            importRdf4jHelper.setPrefixHandle(prefixHandle);
            importRdf4jHelper.setPrefixDoi(prefixDoi);

            importRdf4jHelper.setNodePreference(roleOnThesoBean.getNodePreference());
            importRdf4jHelper.setRdf4jThesaurus(sKOSXmlDocument);

            String idTheso = importRdf4jHelper.addThesaurus();
            if (idTheso == null) {
                error.append(importRdf4jHelper.getMessage());
                showError();
                return;
            }

            for (SKOSResource sKOSResource : sKOSXmlDocument.getConceptList()) {
                progressStep++;
                progress = progressStep / total * 100;
                if (!sKOSResource.getLabelsList().isEmpty()) {
                    importRdf4jHelper.addConcept(sKOSResource, idTheso, isCandidatImport);
                }
            }
            importRdf4jHelper.addFacets(sKOSXmlDocument.getFacetList(), idTheso);
            importRdf4jHelper.addGroups(sKOSXmlDocument.getGroupList(), idTheso);
            importRdf4jHelper.addLangsToThesaurus(connect.getPoolConnexion(), idTheso);

            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            total = 0;

            info = "Thesaurus correctly insert into data base";
            info = info + "\n" + importRdf4jHelper.getMessage().toString();
            roleOnThesoBean.showListTheso();
            viewEditionBean.init();
        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
        onComplete();

    }

    /**
     * permet d'importer les candidats chargé en SKOS
     */
    public void addSkosCandidatToBDD() {

        int idGroup;
        if (selectedUserProject == null || selectedUserProject.isEmpty()) {
            idGroup = -1;
        } else {
            idGroup = Integer.parseInt(selectedUserProject);
        }

        try {

            progress = 0;
            progressStep = 0;

            ImportRdf4jHelper importRdf4jHelper = new ImportRdf4jHelper();
            importRdf4jHelper.setInfos(connect.getPoolConnexion(), formatDate,
                    currentUser.getNodeUser().getIdUser(), idGroup, connect.getWorkLanguage());

            importRdf4jHelper.setNodePreference(roleOnThesoBean.getNodePreference());
            importRdf4jHelper.setRdf4jThesaurus(sKOSXmlDocument);

            candidatBean.setProgressBarStep(100 / sKOSXmlDocument.getConceptList().size());

            for (SKOSResource sKOSResource : sKOSXmlDocument.getConceptList()) {
                progressStep++;
                progress = progressStep / sKOSXmlDocument.getConceptList().size() * 100;
                importRdf4jHelper.addConcept(sKOSResource, selectedTheso.getCurrentIdTheso(), isCandidatImport);
            }

            if (isCandidatImport) {
                candidatBean.getAllCandidatsByThesoAndLangue();
                candidatBean.setIsListCandidatsActivate(true);
            }
        } catch (Exception e) {

        } finally {

        }
        onComplete();
    }

    private Integer progress1;

    public void action() {
        PrimeFaces pf = PrimeFaces.current();
        PrimeFaces.current().executeScript("PF('pbAjax1').start();");
        PrimeFaces.current().executeScript("PF('startButton2').disable();");
        progressStep = 0;
        for (int i = 0; i < 10; i++) {
            progressStep++;
            progress = (progressStep / 10) * 100;
            progress1 = (int) progress;
        }
    }

    public Integer getProgress1() {
        //  progress1 = updateProgress(progress1);
        progressStep = 0;
        for (int i = 0; i < 10; i++) {
            progressStep++;
            progress = (progressStep / 10) * 100;
            progress1 = (int) progress;
        }
        return progress1;
    }

    private Integer updateProgress(Integer progress) {
        if (progress == null) {
            progress = 0;
        } else {
            progress = progress + (int) (Math.random() * 35);

            if (progress > 100) {
                progress = 100;
            }
        }

        return progress;
    }

    public void setProgress1(Integer progress1) {
        this.progress1 = progress1;
    }

    public void onComplete() {
        progress = 100.0;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info :", "import réussi"));
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
            pf.ajax().update("messageIndex");
        }
        clearMemory();
    }

    public void cancel(ActionEvent event) {
        progress1 = null;
    }

    public void cancel() {
        progress1 = null;
    }

    private void showError() {
        if (info != null) {
            if (!info.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info :", info));
            }
        }
        if (error != null) {
            if (error.length() != 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error :", error.toString()));
            }
        }
        if (warning != null) {
            if (!warning.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning :", warning));
            }
        }
    }

    private void initError() {
        haveError = false;
        info = "";
        error = new StringBuffer();
        warning = "";
    }

    public String getFormatDate() {
        return formatDate;
    }

    public void setFormatDate(String formatDate) {
        this.formatDate = formatDate;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public boolean isLoadDone() {
        return loadDone;
    }

    public void setLoadDone(boolean loadDone) {
        this.loadDone = loadDone;
    }

    public boolean isImportDone() {
        return importDone;
    }

    public void setImportDone(boolean importDone) {
        this.importDone = importDone;
    }

    public boolean isBDDinsertEnable() {
        return BDDinsertEnable;
    }

    public void setBDDinsertEnable(boolean BDDinsertEnable) {
        this.BDDinsertEnable = BDDinsertEnable;
    }

    public SKOSXmlDocument getsKOSXmlDocument() {
        return sKOSXmlDocument;
    }

    public void setsKOSXmlDocument(SKOSXmlDocument sKOSXmlDocument) {
        this.sKOSXmlDocument = sKOSXmlDocument;
    }

    public int getProgress() {
        return (int) progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public double getAbs_progress() {
        return progressStep;
    }

    public void setAbs_progress(double abs_progress) {
        this.progressStep = abs_progress;
    }

    public double getProgress_abs() {
        return progressStep;
    }

    public void setProgress_abs(double progress_abs) {
        this.progressStep = progress_abs;
    }

    public int getTypeImport() {
        return typeImport;
    }

    public void stateChangeListener(AjaxBehaviorEvent e) {
        /*    System.out.println("State Listener executed");
    SelectOneMenu x = (SelectOneMenu)e.getSource();
    String[] s = ((String)x.getValue()).split(" - ");
    //state = s[1];*/
    }

    public void setTypeImport(int typeImport) {
        this.typeImport = typeImport;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getError() {
        return error.toString();
    }

    public void setError(StringBuffer error) {
        this.error = error;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public boolean warningIsEmpty() {
        return warning == null || warning.isEmpty();
    }

    public String getSelectedIdentifier() {
        return selectedIdentifier;
    }

    public void setSelectedIdentifier(String selectedIdentifier) {
        this.selectedIdentifier = selectedIdentifier;
    }

    public String getPrefixHandle() {
        return prefixHandle;
    }

    public void setPrefixHandle(String prefixHandle) {
        this.prefixHandle = prefixHandle;
    }

    public String getPrefixDoi() {
        return prefixDoi;
    }

    public void setPrefixDoi(String prefixDoi) {
        this.prefixDoi = prefixDoi;
    }

    public char getDelimiterCsv() {
        return delimiterCsv;
    }

    public void setDelimiterCsv(char delimiterCsv) {
        this.delimiterCsv = delimiterCsv;
    }

    public String getThesaurusName() {
        return thesaurusName;
    }

    public void setThesaurusName(String thesaurusName) {
        this.thesaurusName = thesaurusName;
    }

    public int getChoiceDelimiter() {
        return choiceDelimiter;
    }

    public void setChoiceDelimiter(int choiceDelimiter) {
        this.choiceDelimiter = choiceDelimiter;
    }

    public ArrayList<NodeUserGroup> getNodeUserProjects() {
        return nodeUserProjects;
    }

    public void setNodeUserProjects(ArrayList<NodeUserGroup> nodeUserProjects) {
        this.nodeUserProjects = nodeUserProjects;
    }

    public String getSelectedUserProject() {
        return selectedUserProject;
    }

    public void setSelectedUserProject(String selectedUserProject) {
        this.selectedUserProject = selectedUserProject;
    }

    public boolean isImportInProgress() {
        return importInProgress;
    }

    public void setImportInProgress(boolean importInProgress) {
        this.importInProgress = importInProgress;
    }

    public ArrayList<Languages_iso639> getAllLangs() {
        return allLangs;
    }

    public void setAllLangs(ArrayList<Languages_iso639> allLangs) {
        this.allLangs = allLangs;
    }

    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public boolean isHaveError() {
        return haveError;
    }

    public void setHaveError(boolean haveError) {
        this.haveError = haveError;
    }

    public boolean isClearNoteBefore() {
        return clearNoteBefore;
    }

    public void setClearNoteBefore(boolean clearNoteBefore) {
        this.clearNoteBefore = clearNoteBefore;
    }

}
