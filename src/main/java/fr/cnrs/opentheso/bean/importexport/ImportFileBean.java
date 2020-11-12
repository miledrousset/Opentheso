/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.importexport;

import fr.cnrs.opentheso.bdd.datas.Languages_iso639;
import fr.cnrs.opentheso.bdd.helper.LanguageHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bean.condidat.CandidatBean;
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

    @Inject private Connect connect;
    @Inject private CurrentUser currentUser;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private ViewEditionBean viewEditionBean;    
    @Inject private ConceptView conceptView;
    @Inject private Tree tree;    

    @Inject
    private CandidatBean candidatBean;
    
    @Inject
    private SelectedTheso selectedTheso;
    
    
    private double progress = 0;
    private double progressStep = 0;

    private int typeImport;
    private String selectedIdentifier ="sans";
    private String prefixHandle;
    private boolean isCandidatImport;
    
    // import CSV
    private char delimiterCsv = ',';
    private int choiceDelimiter = 0;
    private String thesaurusName;
    private ArrayList <CsvReadHelper.ConceptObject> conceptObjects;
    private ArrayList<String> langs;    
    
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
    /**
     *
     */
    public void init() {
        choiceDelimiter = 0;
        haveError = false;
        progress = 0;
        progressStep = 0;        
        info = "";
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
        if(conceptObjects != null)
            conceptObjects.clear();
        if(langs != null)
            langs.clear();
        
        // récupération des toutes les langues pour le choix de le langue source
        LanguageHelper languageHelper = new LanguageHelper();
        allLangs = languageHelper.getAllLanguages(connect.getPoolConnexion());
        selectedLang = null;
        thesaurusName = null;
        
        UserHelper userHelper = new UserHelper();
        selectedUserProject = "";
        if(currentUser.getNodeUser().isIsSuperAdmin()) {
            nodeUserProjects = userHelper.getAllProject(connect.getPoolConnexion());
        } else {
            nodeUserProjects = userHelper.getProjectsOfUserAsAdmin(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
            for (NodeUserGroup nodeUserProject : nodeUserProjects) {
                selectedUserProject = "" + nodeUserProject.getIdGroup();
            }
        }

   
        
    }
    
    public void actionChoice() {
        if(choiceDelimiter == 0)
            delimiterCsv = ',';
        if(choiceDelimiter == 1)
            delimiterCsv = ';';
        if(choiceDelimiter == 2)
            delimiterCsv = '\t';         
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
            try {
                CsvReadHelper csvReadHelper;
                try {
                    csvReadHelper = new CsvReadHelper(delimiterCsv);
                    Reader reader1 = new InputStreamReader(event.getFile().getInputStream());
                    if(! csvReadHelper.setLangs(reader1)){
                        error.append(csvReadHelper.getMessage());
                    }
                    Reader reader2 = new InputStreamReader(event.getFile().getInputStream());            
                    if (!csvReadHelper.readFile(reader2)) {
                        error.append(csvReadHelper.getMessage());
                    }                    
                    
                    warning = csvReadHelper.getMessage();
                    
                    conceptObjects = csvReadHelper.getConceptObjects();
                    if(conceptObjects != null) {
                        if(conceptObjects.get(0).getPrefLabels() != null) {
                            if(conceptObjects.get(0).getPrefLabels().isEmpty()) {
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
                } catch (Exception ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                }

            } catch (Exception e) {
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }

        }
    }
    
    
    /**
     * insérer un thésaurus dans la BDD (CSV)
     *
     */
    public void addCsvThesoToBDD() {
        if(conceptObjects == null || conceptObjects.isEmpty()) return;
        if(importInProgress) return;
        initError();
        loadDone = false;
        progressStep = 0;
        progress = 0;
        
        // préparer le projet pour le thésaurus
        int idProject; 
        if(selectedUserProject == null || selectedUserProject.isEmpty()) {
            idProject = -1;
        }
        else
            idProject = Integer.parseInt(selectedUserProject);        
        
        // préparer la langue source 
        if(selectedLang == null || selectedLang.isEmpty()) {
            selectedLang = connect.getWorkLanguage();
        }
        
        // création du thésaurus
        CsvImportHelper csvImportHelper = new CsvImportHelper();
        String idNewTheso = csvImportHelper.createTheso(
                connect.getPoolConnexion(),
                thesaurusName,
                selectedLang,
                idProject,
                currentUser.getNodeUser());
        
        // préparer les préférences du thésaurus, on récupérer les préférences du thésaurus en cours, ou on initialise des nouvelles.
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        NodePreference nodePreference = roleOnThesoBean.getNodePreference();
        
        if (nodePreference == null) {
            preferencesHelper.initPreferences(
                    connect.getPoolConnexion(),
                    idNewTheso,
                    selectedLang);
        } else {
            nodePreference.setPreferredName(thesaurusName);
            preferencesHelper.updateAllPreferenceUser(
                    connect.getPoolConnexion(),
                    nodePreference, idNewTheso);
        }        
        
        // ajout des concepts et collections
        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                switch (conceptObject.getType().trim().toLowerCase()) {
                    case "skos:concept":
                        // ajout de concept
                        if (!csvImportHelper.addConcept(connect.getPoolConnexion(), idNewTheso, conceptObject)) {
                            //return false;
                        }
                        break;
        /*           case "":
                        // ajout de concept
                        if (!addConcept(ds, idTheso, conceptObject1)) {
                            return false;
                        }
                        break;*/
                    case "skos:collection":
                        // ajout de groupe
                        if (!csvImportHelper.addGroup(connect.getPoolConnexion(),
                                idNewTheso, conceptObject)) {
                       //     return false;
                        }
                        break;
                    default:
                        break;
                }
                progressStep++;
                progress = progressStep / total * 100;                

            }

    //        CsvImportHelper csvImportHelper = new CsvImportHelper(roleOnTheso.getNodePreference());
        /*    csvImportHelper.setInfos(
                    formatDate, 
                    currentUser.getNodeUser().getIdUser(),
                    idProject,
                    selectedLang);

            
    
            if(!csvImportHelper.addTheso(
                        connect.getPoolConnexion(),
                        this,
                        thesaurusName, conceptObjects,
                        langs)) {
                error.append(csvImportHelper.getMessage());
            }*/

            //new UserHelper().addRole(connect.getPoolConnexion().getConnection(), idUser,idRole, ImportRdf4jHelper.getIdFromUri(uri) , "");
            
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            total = 0;

            info = "Thesaurus correctly insert into data base";
            info = info + "\n" + csvImportHelper.getMessage();
//            showError();
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

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("toolBoxForm");
            pf.ajax().update("toolBoxForm:listThesoForm");
            pf.ajax().update("messageIndex");            
        }  
    }
    
    /**
     * permet d'ajouter une liste de concepts en CSV sous le concept sélectionné
     * la liste peut être hiérarchisée si la relation BT est renseignée
     * @param nodeConcept 
     */
    public void addListCsvToConcept(NodeConcept nodeConcept){
        if (conceptObjects == null || conceptObjects.isEmpty()) {
            warning = "pas de valeurs";
            return;
        }
        if(importInProgress) return;
        initError();
        loadDone = false;
        progressStep = 0;
        progress = 0;

        // préparer les préférences du thésaurus, on récupérer les préférences du thésaurus en cours
        NodePreference nodePreference = roleOnThesoBean.getNodePreference();
        if(nodePreference == null) {
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
                if(conceptObject.getBroaders().isEmpty()) {
                    csvImportHelper.addSingleConcept(connect.getPoolConnexion(),
                        nodeConcept.getConcept().getIdThesaurus(),
                        idPere,
                        nodeConcept.getConcept().getIdGroup(),
                        currentUser.getNodeUser().getIdUser(),
                        conceptObject);
                } else {
                    for (String idBT : conceptObject.getBroaders()) {
                        csvImportHelper.addSingleConcept(connect.getPoolConnexion(),
                            nodeConcept.getConcept().getIdThesaurus(),
                            idBT,
                            nodeConcept.getConcept().getIdGroup(),
                            currentUser.getNodeUser().getIdUser(),
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
   //     PrimeFaces.current().executeScript("PF('deleteConcept').hide();");            
           
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
    }    

    private void loadSkos(FileUploadEvent event, Boolean isCandidatImport) {
        progress = 0;

        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            InputStream is = null;
            try {
                try {
                    is = event.getFile().getInputStream();
                } catch (IOException ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                } catch (Exception ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                }
                ReadRdf4j readRdf4j = null;
                try {
                    readRdf4j = new ReadRdf4j(is, 0, isCandidatImport);
                } catch (IOException ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                } catch (Exception ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.toString());
                }
                if(readRdf4j==null) {
                    error.append(System.getProperty("line.separator"));
                    error.append("Erreur de format RDF !!!");
                    showError();
                    return;
                }
                warning = readRdf4j.getMessage();
                sKOSXmlDocument = readRdf4j.getsKOSXmlDocument();
                total = sKOSXmlDocument.getConceptList().size();
                uri = sKOSXmlDocument.getTitle();
                loadDone = true;
                BDDinsertEnable = true;

                info = "File correctly loaded";

            } catch (Exception e) {
                System.out.println("erreur :" + e.getMessage());
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
            InputStream is = null;
            try {
                try {
                    is = event.getFile().getInputStream();
                } catch (IOException ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                } catch (Exception ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                }
                ReadRdf4j readRdf4j;
                try {
                    readRdf4j = new ReadRdf4j(is, 1, isCandidatImport);
                    warning = readRdf4j.getMessage();
                    sKOSXmlDocument = readRdf4j.getsKOSXmlDocument();
                    total = sKOSXmlDocument.getConceptList().size() ;
                    uri = sKOSXmlDocument.getTitle();
                    loadDone = true;
                    BDDinsertEnable = true;
                    info = "File correctly loaded";                    
                } catch (IOException ex) {
                   error.append(System.getProperty("line.separator"));
                   error.append(ex.getMessage());
                } catch (Exception ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                }
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
            InputStream is = null;
            try {
                try {
                    is = event.getFile().getInputStream();
                } catch (IOException ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                } catch (Exception ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                }
                ReadRdf4j readRdf4j;
                try {
                    readRdf4j = new ReadRdf4j(is, 3, isCandidatImport);
                    warning = readRdf4j.getMessage();
                    sKOSXmlDocument = readRdf4j.getsKOSXmlDocument();
                    total = sKOSXmlDocument.getConceptList().size();
                    uri = sKOSXmlDocument.getTitle();
                    loadDone = true;
                    BDDinsertEnable = true;
                    info = "File correctly loaded";                    
                } catch (IOException ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                } catch (Exception ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                }

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
            InputStream is = null;
            try {
                try {
                    is = event.getFile().getInputStream();
                } catch (IOException ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                } catch (Exception ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                }
                ReadRdf4j readRdf4j;
                try {
                    readRdf4j = new ReadRdf4j(is, 2, isCandidatImport);
                    warning = readRdf4j.getMessage();
                    sKOSXmlDocument = readRdf4j.getsKOSXmlDocument();
                    total = sKOSXmlDocument.getConceptList().size();
                    uri = sKOSXmlDocument.getTitle();
                    loadDone = true;
                    BDDinsertEnable = true;
                    info = "File correctly loaded";                    
                } catch (IOException ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                } catch (Exception ex) {
                    error.append(System.getProperty("line.separator"));
                    error.append(ex.getMessage());
                }
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
        
        if(importInProgress) return; 
        int idGroup; 
        
        if(selectedUserProject == null || selectedUserProject.isEmpty()) {
            idGroup = -1;
        }
        else
            idGroup = Integer.parseInt(selectedUserProject);
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

            importRdf4jHelper.setNodePreference(roleOnThesoBean.getNodePreference());
            importRdf4jHelper.setRdf4jThesaurus(sKOSXmlDocument);
            
            String idTheso = importRdf4jHelper.addThesaurus();
            if(idTheso == null) {
                error.append(importRdf4jHelper.getMessage());
                showError();
                return;
            }

            for (SKOSResource sKOSResource : sKOSXmlDocument.getConceptList()) {
                progressStep++;
                progress = progressStep / total * 100;
                importRdf4jHelper.addConcept(sKOSResource, idTheso, isCandidatImport);
            }

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
     /*       PrimeFaces pf = PrimeFaces.current();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("toolBoxForm:viewImportSkosForm");
     //           pf.ajax().update("toolBoxForm:listThesoForm");
                pf.ajax().update("messageIndex");       
            }   */         
        } catch (Exception e) {
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
        } finally {
            showError();
        }
    }

    /**
     * permet d'importer les candidats chargé en SKOS 
     */
    public void addSkosCandidatToBDD() {
        
        int idGroup; 
        if(selectedUserProject == null || selectedUserProject.isEmpty()) {
            idGroup = -1;
        } else
            idGroup = Integer.parseInt(selectedUserProject);
        
        try {
            
            candidatBean.setProgressBarStep(0);
            candidatBean.setProgressBarValue(0);
            
            ImportRdf4jHelper importRdf4jHelper = new ImportRdf4jHelper();
            importRdf4jHelper.setInfos(connect.getPoolConnexion(), formatDate, 
                    currentUser.getNodeUser().getIdUser(), idGroup, connect.getWorkLanguage());

            importRdf4jHelper.setNodePreference(roleOnThesoBean.getNodePreference());
            importRdf4jHelper.setRdf4jThesaurus(sKOSXmlDocument);
            
            candidatBean.setProgressBarStep(100 / sKOSXmlDocument.getConceptList().size());

            for (SKOSResource sKOSResource : sKOSXmlDocument.getConceptList()) {
                candidatBean.setProgressBarValue(candidatBean.getProgressBarValue() + candidatBean.getProgressBarStep());
                importRdf4jHelper.addConcept(sKOSResource, selectedTheso.getCurrentIdTheso(), isCandidatImport);
            }

            if (isCandidatImport) {
                candidatBean.getAllCandidatsByThesoAndLangue();
                candidatBean.setIsListCandidatsActivate(true);
            }
            
        } catch (Exception e) {
                
        } finally {
            
        }
    }    
    
    
    private Integer progress1;
    
    public void action() {
        PrimeFaces pf = PrimeFaces.current();
//        if (pf.isAjaxRequest()) {        
            PrimeFaces.current().executeScript("PF('pbAjax1').start();");
            PrimeFaces.current().executeScript("PF('startButton2').disable();");
//        }
        progressStep = 0;
        for(int i= 0; i< 10;i++) {
            progressStep++;
            progress = (progressStep / 10) * 100;
            progress1 = (int) progress;
        }
    }
 
    public Integer getProgress1() {
      //  progress1 = updateProgress(progress1);
        progressStep = 0;
        for(int i= 0; i< 10;i++) {
            progressStep++;
            progress = (progressStep / 10) * 100;
            progress1 = (int) progress;
        }      
        return progress1;
    }

 
    private Integer updateProgress(Integer progress) {
        if(progress == null) {
            progress = 0;
        }
        else {
            progress = progress + (int)(Math.random() * 35);
             
            if(progress > 100)
                progress = 100;
        }
         
        return progress;
    }
 
    public void setProgress1(Integer progress1) {
        this.progress1 = progress1;
    }
 

 
    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Progress Completed"));
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {         
            PrimeFaces.current().executeScript("PF('pbAjax').cancel();");        
        }
   //     progress = 100;
    }    
    public void cancel(ActionEvent event) {
        progress1 = null;
    }    
    public void cancel() {
        progress1 = null;
    }       
    
    private void showError() {
        if(info != null) {
            if (!info.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info :", info));
            }
        }
        if(error != null) {
            if (error.length() != 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error :", error.toString()));
            }
        }
        if(warning != null) {
            if (!warning.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning :", warning));
            }
        }
    }    

    private void initError(){
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

    public void stateChangeListener(AjaxBehaviorEvent e)
    {
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



}


