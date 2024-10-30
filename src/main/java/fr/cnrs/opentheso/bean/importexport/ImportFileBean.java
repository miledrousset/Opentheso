package fr.cnrs.opentheso.bean.importexport;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.alignment.NodeAlignmentImport;
import fr.cnrs.opentheso.models.alignment.NodeAlignmentSmall;
import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.concept.NodeCompareTheso;
import fr.cnrs.opentheso.models.concept.NodeFullConcept;
import fr.cnrs.opentheso.models.languages.Languages_iso639;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.models.nodes.NodeTree;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.relations.NodeDeprecated;
import fr.cnrs.opentheso.models.relations.NodeReplaceValueByValue;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.users.NodeUserGroup;
import fr.cnrs.opentheso.repositories.AlignmentHelper;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.DeprecateHelper;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.ImagesHelper;
import fr.cnrs.opentheso.repositories.LanguageHelper;
import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.services.imports.rdf4j.ImportRdf4jHelper;
import fr.cnrs.opentheso.services.imports.rdf4j.ReadRDF4JNewGen;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewEditionBean;
import fr.cnrs.opentheso.services.exports.csv.CsvWriteHelper;
import fr.cnrs.opentheso.services.imports.csv.CsvImportHelper;
import fr.cnrs.opentheso.services.imports.csv.CsvReadHelper;
import fr.cnrs.opentheso.models.skosapi.SKOSProperty;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.AjaxBehaviorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author miledrousset
 */
@Slf4j
@Named(value = "importFileBean")
@SessionScoped
public class ImportFileBean implements Serializable {

    @Autowired @Lazy
    private Connect connect;
    @Autowired @Lazy
    private CurrentUser currentUser;
    @Autowired @Lazy
    private RoleOnThesoBean roleOnThesoBean;
    @Autowired @Lazy
    private ViewEditionBean viewEditionBean;
    @Autowired @Lazy
    private ConceptView conceptView;
    @Autowired @Lazy
    private Tree tree;
    @Autowired @Lazy
    private CandidatBean candidatBean;
    @Autowired @Lazy
    private SelectedTheso selectedTheso;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private SearchHelper searchHelper;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private CsvImportHelper csvImportHelper;

    @Autowired
    private LanguageHelper languageHelper;

    @Autowired
    private GroupHelper groupHelper;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private NoteHelper noteHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    @Autowired
    private DeprecateHelper deprecateHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private ImagesHelper imagesHelper;

    @Autowired
    private TermHelper termHelper;

    @Autowired
    private AlignmentHelper alignmentHelper;

    @Autowired
    private ImportRdf4jHelper importRdf4jHelper;


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
    private String idLang;
    private ArrayList<NodeAlignmentImport> nodeAlignmentImports;

    private ArrayList<NodeReplaceValueByValue> nodeReplaceValueByValues;
    private ArrayList<NodeDeprecated> nodeDeprecateds;

    //CSV Structuré
    private NodeTree racine;

    private String selectedIdentifierImportAlign;

    private ArrayList<NodeNote> nodeNotes;

    private String formatDate = "yyyy-MM-dd";
    private String uri;
    private int total;

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

    // pour les alignements
    private String selectedConcept;
    private String alignmentSource;

    private boolean haveError;

    private boolean clearBefore;

    private ArrayList<NodeIdValue> nodeIdValues;
    private ArrayList<NodeCompareTheso> nodeCompareThesos;

    private String fileName;

    private String selectedSearchType;

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
        fileName = null;
        selectedConcept = null;
        alignmentSource = null;
    }

    public void init() {
        selectedSearchType = "exactWord"; // containsExactWord, startWith, elastic
        selectedIdentifierImportAlign = "identifier";
        choiceDelimiter = 0;
        delimiterCsv = ',';
        haveError = false;
        clearBefore = false;
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
        fileName = null;
        if (conceptObjects != null) {
            conceptObjects.clear();
        }
        if (nodeAlignmentImports != null) {
            nodeAlignmentImports.clear();
        }
        if (langs != null) {
            langs.clear();
        }

        if (nodeNotes != null) {
            nodeNotes.clear();
        }
        idLang = null;
        selectedConcept = null;
        alignmentSource = null;

        // récupération des toutes les langues pour le choix de le langue source
        allLangs = languageHelper.getAllLanguages(connect.openConnexionPool());
        selectedLang = connect.getWorkLanguage();
        thesaurusName = null;
        if (roleOnThesoBean != null && roleOnThesoBean.getNodePreference() != null) {
            selectedLang = roleOnThesoBean.getNodePreference().getSourceLang();
        } else {
            selectedLang = connect.getWorkLanguage();
        }

        selectedUserProject = "";
        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeUserProjects = userHelper.getAllProject(connect.openConnexionPool());
        } else {
            nodeUserProjects = userHelper.getProjectsOfUserAsAdmin(connect.openConnexionPool(), currentUser.getNodeUser().getIdUser());
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

    public void actionChoiceLang(String lang) {
        //     selectedLang = lang;
    }

    public void actionChoiceIdentifier() {
        setSelectedIdentifier(selectedIdentifierImportAlign);
    }

    public void actionToggle() {
        //   this.clearBefore = clearBefore;
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
     * permet de charger un fichier de notes en Csv
     *
     * @param event
     */
    public void loadFileAltlabelCsv(FileUploadEvent event) {
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
                        if (!csvReadHelper.readFileAltlabel(reader2)) {
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
     * permet de charger un fichier de notes en Csv
     *
     * @param event
     */
    public void loadFileImageCsv(FileUploadEvent event) {
        initError();
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);
            try (Reader reader1 = new InputStreamReader(event.getFile().getInputStream())) {
                if (!csvReadHelper.readFileImage(reader1)) {
                    error.append(csvReadHelper.getMessage());
                }

                warning = csvReadHelper.getMessage();
                conceptObjects = csvReadHelper.getConceptObjects();
                if (conceptObjects != null) {
                    if (conceptObjects.isEmpty()) {
                        haveError = true;
                        error.append(System.getProperty("line.separator"));
                        error.append("La lecture a échoué, vérifiez le séparateur des colonnes !!");
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
            PrimeFaces.current().executeScript("PF('waitDialog').hide()");
        }
    }

    /**
     * permet de charger un fichier de notes en Csv
     *
     * @param event
     */
    public void loadFileNotationCsv(FileUploadEvent event) {
        initError();
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);
            try (Reader reader = new InputStreamReader(event.getFile().getInputStream())) {

                if (!csvReadHelper.readFileNotation(reader)) {
                    error.append(csvReadHelper.getMessage());
                }

                warning = csvReadHelper.getMessage();
                nodeIdValues = csvReadHelper.getNodeIdValues();
                if (nodeIdValues != null) {
                    if (nodeIdValues.isEmpty()) {
                        haveError = true;
                        error.append(System.getProperty("line.separator"));
                        error.append("La lecture a échouée, vérifiez le séparateur des colonnes !!");
                        warning = "";
                    } else {
                        total = nodeIdValues.size();
                        uri = "";//csvReadHelper.getUri();
                        loadDone = true;
                        BDDinsertEnable = true;
                        info = "File correctly loaded";
                    }
                }
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            } catch (Exception e) {
                haveError = true;
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
        }
    }

    /**
     * permet de charger un fichier de notes en Csv
     *
     * @param event
     */
    public void loadFileCollectionCsv(FileUploadEvent event) {
        initError();
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);
            try (Reader reader = new InputStreamReader(event.getFile().getInputStream())) {

                if (!csvReadHelper.readFileCollection(reader)) {
                    error.append(csvReadHelper.getMessage());
                }

                warning = csvReadHelper.getMessage();
                nodeIdValues = csvReadHelper.getNodeIdValues();
                if (nodeIdValues != null) {
                    if (nodeIdValues.isEmpty()) {
                        haveError = true;
                        error.append(System.getProperty("line.separator"));
                        error.append("La lecture a échouée, vérifiez le séparateur des colonnes !!");
                        warning = "";
                    } else {
                        total = nodeIdValues.size();
                        uri = "";//csvReadHelper.getUri();
                        loadDone = true;
                        BDDinsertEnable = true;
                        info = "File correctly loaded";
                    }
                }
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            } catch (Exception e) {
                haveError = true;
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
        }
    }

    /**
     * permet de charger un fichier en Csv
     *
     * @param event
     */
    public void loadFileArkCsv(FileUploadEvent event) {
        initError();
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);

            try (Reader reader = new InputStreamReader(event.getFile().getInputStream())) {

                if (!csvReadHelper.readFileArk(reader)) {
                    error.append(csvReadHelper.getMessage());
                }

                warning = csvReadHelper.getMessage();
                nodeIdValues = csvReadHelper.getNodeIdValues();
                if (nodeIdValues != null) {
                    if (nodeIdValues.isEmpty()) {
                        haveError = true;
                        error.append(System.getProperty("line.separator"));
                        error.append("La lecture a échouée, vérifiez le séparateur des colonnes !!");
                        warning = "";
                    } else {
                        total = nodeIdValues.size();
                        uri = "";//csvReadHelper.getUri();
                        loadDone = true;
                        BDDinsertEnable = true;
                        info = "File correctly loaded";
                    }
                }
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            } catch (Exception e) {
                haveError = true;
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            } finally {
                showError();
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
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
            ArrayList<String> headerSourceAlignList;

            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);
            try (Reader reader1 = new InputStreamReader(event.getFile().getInputStream())) {
                headerSourceAlignList = csvReadHelper.readHeadersFileAlignment(reader1);
                if (headerSourceAlignList == null || headerSourceAlignList.isEmpty()) {
                    error.append(csvReadHelper.getMessage());
                    return;
                }
            } catch (Exception e) {
                haveError = true;
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
                return;
            }

            try (Reader reader = new InputStreamReader(event.getFile().getInputStream())) {

                if (!csvReadHelper.readFileAlignment(reader, headerSourceAlignList)) {
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
                        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
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
        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
    }

    /**
     * permet de charger un fichier en Csv
     *
     * @param event
     */
    public void loadFileCsvList(FileUploadEvent event) {
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
                    // false to not read empty data
                    if (!csvReadHelper.readListFile(reader2)) {
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
                    // false to not read empty data
                    if (!csvReadHelper.readFile(reader2, false)) {
                        error.append(csvReadHelper.getMessage());
                    }

                    warning = csvReadHelper.getMessage();
                    conceptObjects = csvReadHelper.getConceptObjects();
                    if (conceptObjects != null && !conceptObjects.isEmpty()) {
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
                    total = conceptObjects.size();
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
     * permet de charger un fichier en Csv
     *
     * @param event
     */
    public void loadFileCsvForMerge(FileUploadEvent event) {
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
                    /// option true to read empty data
                    if (!csvReadHelper.readFile(reader2, true)) {
                        error.append(csvReadHelper.getMessage());
                    }

                    warning = csvReadHelper.getMessage();
                    conceptObjects = csvReadHelper.getConceptObjects();
                    if (conceptObjects != null) {
                        langs = csvReadHelper.getLangs();
                        total = conceptObjects.size();
                        uri = "";//csvReadHelper.getUri();
                        loadDone = true;
                        BDDinsertEnable = true;
                        info = "File correctly loaded";
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
     * permet de charger un fichier en Csv
     *
     * @param event
     */
    public void loadFileCsvForGetIdFromPrefLabel(FileUploadEvent event) {
        initError();

        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);
            fileName = event.getFile().getFileName();
            // première lecrture pour charger les langues
            try (Reader reader1 = new InputStreamReader(event.getFile().getInputStream())) {
                /// option true to read empty data
                if (!csvReadHelper.readFileCsvForGetIdFromPrefLabelSetLang(reader1)) {
                    error.append(csvReadHelper.getMessage());
                }
            } catch (Exception e) {
                haveError = true;
                error.append(System.getProperty("line.separator"));
                error.append(e.toString());
            }
            if (csvReadHelper.getIdLang() == null) {
                error.append("La langue n'a pas été déctectée");
                return;
            }

            try (Reader reader = new InputStreamReader(event.getFile().getInputStream())) {
                /// option true to read empty data
                if (!csvReadHelper.readFileCsvForGetIdFromPrefLabel(reader)) {
                    error.append(csvReadHelper.getMessage());
                }

                warning = csvReadHelper.getMessage();
                nodeCompareThesos = csvReadHelper.getNodeCompareThesos();
                idLang = csvReadHelper.getIdLang();
                if (nodeCompareThesos != null) {
                    total = nodeCompareThesos.size();
                    uri = "";//csvReadHelper.getUri();
                    loadDone = true;
                    BDDinsertEnable = true;
                    info = "File correctly loaded";
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
     * permet de charger un fichier en Csv
     *
     * @param event
     */
    public void loadFileCsvDeprecateConcepts(FileUploadEvent event) {
        initError();

        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);

            try (Reader reader = new InputStreamReader(event.getFile().getInputStream())) {
                /// option true to read empty data
                if (!csvReadHelper.readFileCsvDeprecateConcepts(reader)) {
                    error.append(csvReadHelper.getMessage());
                }

                warning = csvReadHelper.getMessage();
                nodeDeprecateds = csvReadHelper.getNodeDeprecateds();
                if (nodeDeprecateds != null) {
                    total = nodeDeprecateds.size();
                    uri = "";//csvReadHelper.getUri();
                    loadDone = true;
                    BDDinsertEnable = true;
                    info = "File correctly loaded";
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
     * permet de charger un fichier en Csv
     *
     * @param event
     */
    public void loadFileCsvForReplaceValueByNewValue(FileUploadEvent event) {
        initError();

        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            CsvReadHelper csvReadHelper = new CsvReadHelper(delimiterCsv);
            // première lecrture pour charger les langues
            ArrayList<String> usedLangs = thesaurusHelper.getAllUsedLanguagesOfThesaurus(connect.openConnexionPool(), selectedTheso.getCurrentIdTheso());
            try (Reader reader = new InputStreamReader(event.getFile().getInputStream())) {
                /// option true to read empty data
                if (!csvReadHelper.readFileReplaceValueByNewValue(reader, usedLangs)) {
                    error.append(csvReadHelper.getMessage());
                }

                warning = csvReadHelper.getMessage();
                nodeReplaceValueByValues = csvReadHelper.getNodeReplaceValueByValues();
                if (nodeReplaceValueByValues != null) {
                    total = nodeReplaceValueByValues.size();
                    uri = "";//csvReadHelper.getUri();
                    loadDone = true;
                    BDDinsertEnable = true;
                    info = "File correctly loaded";
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

    public void loadFileCsvStructured(FileUploadEvent event) throws IOException {
        total = 0;
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            String delimiterChar;
            switch (choiceDelimiter) {
                case 0:
                    delimiterChar = ",";
                    break;
                case 1:
                    delimiterChar = ";";
                    break;
                default:
                    delimiterChar = "\\t";
            }

            List<String[]> lines = new ArrayList<>();
            try (InputStreamReader isr = new InputStreamReader(event.getFile().getInputStream(), StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(isr)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (StringUtils.isNoneEmpty(line)) {
                        lines.add(line.split(delimiterChar));
                    }
                }
            } catch (IOException e) {
            }

            int nbrMaxElement = lines.get(0).length;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).length > nbrMaxElement) {
                    nbrMaxElement = lines.get(i).length;
                }
            }

            String[][] matrix = new String[lines.size() + 1][nbrMaxElement + 1];
            for (int i = 0; i < lines.size(); i++) {
                for (int j = 0; j < nbrMaxElement; j++) {
                    if (lines.get(i).length > j) {
                        matrix[i][j] = lines.get(i)[j];
                        if (StringUtils.isNotEmpty(lines.get(i)[j])) {
                            total = total + 1;
                        }
                    }
                }
            }

            racine = new NodeTree();

            for (int i = 0; i < matrix.length; i++) {
                if (StringUtils.isNotEmpty(matrix[i][0])) {
                    racine.getChildrens().add(createTree(matrix, i, 0));
                }
            }

            loadDone = true;

            PrimeFaces.current().executeScript("PF('waitDialog').hide()");
        }

        PrimeFaces.current().ajax().update("containerIndex:resultImportCSVStructure");
    }

    private NodeTree createTree(String[][] matrix, int ligne, int colone) {

        NodeTree element = new NodeTree();
        element.setPreferredTerm(matrix[ligne][colone]);

        colone++;
        ligne++;
        if (ligne < matrix.length && colone < matrix[ligne].length) {
            while (matrix[ligne][colone] != null) {
                if (matrix[ligne][colone - 1] != null && matrix[ligne][colone - 1].length() > 0 && !matrix[ligne][colone - 1].equals(element.getPreferredTerm())) {
                    break;
                }
                if (matrix[ligne][colone].length() > 0) {
                    element.getChildrens().add(createTree(matrix, ligne, colone));
                }
                ligne++;
            }
        }

        return element;
    }

    private NodeTree createTreeMR(String[][] matrix, int ligne, int colone) {

        NodeTree element = new NodeTree();
        element.setPreferredTerm(matrix[ligne][colone]);

        ligne++;

        if (ligne < matrix.length && colone < matrix[ligne].length) {
            if (matrix[ligne][colone] == null) {
                colone--;
            } else {
                if (matrix[ligne][colone].isEmpty()) {
                    colone++;
                    element.getChildrens().add(createTreeMR(matrix, ligne, colone));
                }
                if (!matrix[ligne][colone].isEmpty()) {
                    element.getChildrens().add(createTreeMR(matrix, ligne, colone));
                }
            }
        }
        return element;
    }

    public void addCsvStrucToDB() {

        if (StringUtils.isEmpty(selectedLang)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Le nom du thésaurus est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        // préparer le projet pour le thésaurus
        int idProject;
        if (selectedUserProject == null || selectedUserProject.isEmpty()) {
            idProject = -1;
        } else {
            idProject = Integer.parseInt(selectedUserProject);
        }

        // préparer la langue source
        if (StringUtils.isEmpty(selectedLang)) {
            selectedLang = connect.getWorkLanguage();
        }

        String idNewTheso = csvImportHelper.createTheso(connect.openConnexionPool(), thesaurusName, selectedLang,
                idProject, currentUser.getNodeUser());

        if (idNewTheso == null || idNewTheso.isEmpty()) {
            return;
        }

        // préparer les préférences du thésaurus, on récupérer les préférences du thésaurus en cours, ou on initialise des nouvelles.
        NodePreference nodePreference = roleOnThesoBean.getNodePreference();

        if (nodePreference == null) {
            preferencesHelper.initPreferences(connect.openConnexionPool(), idNewTheso, selectedLang);
        } else {
            nodePreference.setPreferredName(idNewTheso);
            nodePreference.setSourceLang(selectedLang);
            preferencesHelper.addPreference(connect.openConnexionPool(), nodePreference, idNewTheso);
        }
        conceptHelper.setNodePreference(preferencesHelper.getThesaurusPreferences(connect.openConnexionPool(), idNewTheso));
        // ajout des concepts et collections
        for (NodeTree nodeTree : racine.getChildrens()) {
            insertDB(nodeTree, idNewTheso, null);
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le thésaurus "
                + thesaurusName + " (" + idNewTheso + ") est correctement importé !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        roleOnThesoBean.showListTheso(currentUser);
        viewEditionBean.init();
    }

    private void insertDB(NodeTree nodeTree, String idNewTheso, String idConceptParent) {

        Concept concept = new Concept();
        concept.setIdThesaurus(idNewTheso);
        concept.setStatus("D");

        concept.setIdConcept(null);//id);

        Term terme = new Term();
        terme.setIdThesaurus(idNewTheso);
        terme.setLang(selectedLang);
        terme.setLexicalValue(nodeTree.getPreferredTerm().trim());
        terme.setSource("");
        terme.setStatus("D");
        concept.setTopConcept(false);

        String idConcept = conceptHelper.addConcept(connect.openConnexionPool(),
                idConceptParent, "NT", concept, terme, currentUser.getNodeUser().getIdUser());

        for (NodeTree node : nodeTree.getChildrens()) {
            insertDB(node, idNewTheso, idConcept);
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
        String idNewTheso = csvImportHelper.createTheso(connect.openConnexionPool(), thesaurusName, selectedLang,
                idProject, currentUser.getNodeUser());

        if (idNewTheso == null || idNewTheso.isEmpty()) {
            return;
        }

        csvImportHelper.addLangsToThesaurus(connect.openConnexionPool(), langs, idNewTheso);

        // préparer les préférences du thésaurus, on récupérer les préférences du thésaurus en cours, ou on initialise des nouvelles.
        NodePreference nodePreference = roleOnThesoBean.getNodePreference();

        if (nodePreference == null) {
            preferencesHelper.initPreferences(connect.openConnexionPool(), idNewTheso, selectedLang);
        } else {
            nodePreference.setPreferredName(thesaurusName);
            nodePreference.setSourceLang(selectedLang);
            if (nodePreference.getOriginalUri() == null || nodePreference.getOriginalUri().isEmpty()) {
                nodePreference.setOriginalUri("http://mondomaine.fr");
            }
            preferencesHelper.addPreference(connect.openConnexionPool(), nodePreference, idNewTheso);
        }
        csvImportHelper.setNodePreference(nodePreference);
        csvImportHelper.setFormatDate(formatDate);

        // ajout des concepts et collections
        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                switch (conceptObject.getType().trim().toLowerCase()) {
                    case "skos:concept":
                        // ajout de concept
                        csvImportHelper.addConcept(connect.openConnexionPool(), idNewTheso, conceptObject);
                        break;
                    case "skos:collection":
                        // ajout de groupe
                        csvImportHelper.addGroup(connect.openConnexionPool(), idNewTheso, conceptObject);
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
            roleOnThesoBean.showListTheso(currentUser);
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
        //    System.gc();
        //    System.gc();

        onComplete();
    }

    public void addCsvThesoToBDDV2() {

        if (conceptObjects == null || conceptObjects.isEmpty()) {
            return;
        }

        if (importInProgress) {
            return;
        }

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
        String idNewTheso = csvImportHelper.createTheso(connect.openConnexionPool(), thesaurusName, selectedLang,
                idProject, currentUser.getNodeUser());
        if (idNewTheso == null || idNewTheso.isEmpty()) {
            return;
        }

        csvImportHelper.addLangsToThesaurus(connect.openConnexionPool(), langs, idNewTheso);

        // préparer les préférences du thésaurus, on récupérer les préférences du thésaurus en cours, ou on initialise des nouvelles.
        NodePreference nodePreference = roleOnThesoBean.getNodePreference();

        if (nodePreference == null) {
            preferencesHelper.initPreferences(connect.openConnexionPool(), idNewTheso, selectedLang);
        } else {
            nodePreference.setPreferredName(thesaurusName);
            nodePreference.setSourceLang(selectedLang);
            preferencesHelper.addPreference(connect.openConnexionPool(), nodePreference, idNewTheso);
        }
        csvImportHelper.setNodePreference(preferencesHelper.getThesaurusPreferences(connect.openConnexionPool(), idNewTheso));
        csvImportHelper.setFormatDate(formatDate);

        // ajout des concepts et collections
        total = 0;
        for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
            switch (conceptObject.getType().trim().toLowerCase()) {
                case "skos:concept":
                    // ajout de concept
                    if (csvImportHelper.addConceptV2(connect.openConnexionPool(),
                            idNewTheso, conceptObject, currentUser.getNodeUser().getIdUser())) {
                        total++;
                    }
                    break;
                case "skos:collection":
                    // ajout de groupe
                    csvImportHelper.addGroup(connect.openConnexionPool(), idNewTheso, conceptObject);

                    // ajout des liens pour les sous groupes
                    for (String subGroup : conceptObject.getSubGroups()) {
                        groupHelper.addSubGroup(connect.openConnexionPool(), conceptObject.getIdConcept(), subGroup, idNewTheso);
                    }
                    break;

                case "skos-thes:thesaurusarray":
                    // ajout dde facettes
                    csvImportHelper.addFacets(connect.openConnexionPool(), conceptObject, idNewTheso);
                    break;
                default:
                    break;
            }
        }

        roleOnThesoBean.showListTheso(currentUser);
        viewEditionBean.init();

        if (!StringUtils.isEmpty(csvImportHelper.getMessage())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    csvImportHelper.getMessage(), "Total importé : " + total));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Total importé : " + total + "; Le thesaurus " + idNewTheso + " est correctement ajouté !", "import réussi"));
        }
        PrimeFaces.current().ajax().update("messageIndex");

        //    System.gc();
    }

    /**
     * insérer un thésaurus dans la BDD (CSV)
     *
     * @param idTheso
     * @param idUser1
     */
    public void mergeCsvThesoToBDD(String idTheso, int idUser1) {

        loadDone = false;
        progressStep = 0;
        progress = 0;
        total = 0;

        if (conceptObjects == null || conceptObjects.isEmpty()) {
            return;
        }

        if (importInProgress) {
            return;
        }

        initError();

        // mise à jouor des concepts
        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                if (csvImportHelper.updateConcept(connect.openConnexionPool(), idTheso, conceptObject, idUser1)) {
                    total++;
                    conceptHelper.updateDateOfConcept(connect.openConnexionPool(),
                            idTheso, conceptObject.getIdConcept(), idUser1);

                    ///// insert DcTermsData to add contributor
                    dcElementHelper.addDcElementConcept(connect.openConnexionPool(),
                            new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                            conceptObject.getIdConcept(), idTheso);
                    ///////////////
                }
            }

            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            //total = 0;
            info = info + "\n" + "total = " + total + "\n" + csvImportHelper.getMessage();
            roleOnThesoBean.showListTheso(currentUser);
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
        //    System.gc();
        //    System.gc();

        onComplete();
    }

    /**
     * permet de récupérer les identifiants depuis le prefLabel
     *
     * @param idTheso
     * @return
     */
    public StreamedContent getAlignmentsOfTheso(String idTheso) {

        loadDone = false;
        progressStep = 0;
        progress = 0;
        total = 0;

        if (StringUtils.isEmpty(idTheso)) {
            return null;
        }

        if (StringUtils.isEmpty(alignmentSource)) {
            error.append("La source est obligatoire !!");
            showError();
            return null;
        }

        initError();

        ArrayList<NodeIdValue> listAlignments = new ArrayList<>();
        ArrayList<String> branchIds;
        ArrayList<NodeAlignmentSmall> nodeAlignmentSmalls;
        try {
            if (StringUtils.isEmpty(selectedConcept)) {
                // on exporte tous les alignements
                branchIds = conceptHelper.getAllIdConceptOfThesaurus(
                        connect.openConnexionPool(),
                        idTheso);
            } else {
                // on exporte la branche
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), selectedConcept, idTheso)) {
                    error.append("L'identifiant n'existe pas !!");
                    showError();
                    return null;
                }
                branchIds = conceptHelper.getIdsOfBranch(
                        connect.openConnexionPool(),
                        selectedConcept,
                        idTheso);
            }
            if (branchIds != null) {
                for (String idConcept : branchIds) {
                    nodeAlignmentSmalls = alignmentHelper.getAllAlignmentOfConceptNew(connect.openConnexionPool(), idConcept, idTheso);
                    if (!nodeAlignmentSmalls.isEmpty()) {
                        for (NodeAlignmentSmall nodeAlignmentSmall : nodeAlignmentSmalls) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(idConcept);
                            nodeIdValue.setValue(nodeAlignmentSmall.getUri_target());
                            listAlignments.add(nodeIdValue);
                            total++;
                        }
                    }
                }
            }
            log.error(csvImportHelper.getMessage());

            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            //total = 0;
            info = info + "\n" + "total = " + total;
            error.append(csvImportHelper.getMessage());

            CsvWriteHelper csvWriteHelper = new CsvWriteHelper();
            byte[] datas = csvWriteHelper.writeCsvForAlignment(listAlignments, alignmentSource);

            try (ByteArrayInputStream input = new ByteArrayInputStream(datas)) {
                return DefaultStreamedContent.builder()
                        .contentType("text/csv")
                        .name("resultat.csv")
                        .stream(() -> input)
                        .build();
            } catch (IOException ex) {
                error.append(System.getProperty(ex.getMessage()));
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            return new DefaultStreamedContent();

        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
        return null;
    }

    /**
     * permet de récupérer les identifiants depuis le prefLabel
     *
     * @param idTheso
     * @param idUser
     * @return
     */
    public StreamedContent compareListToTheso(String idTheso, int idUser) {

        loadDone = false;
        progressStep = 0;
        progress = 0;
        total = 0;

        if (nodeCompareThesos == null || nodeCompareThesos.isEmpty()) {
            return null;
        }
        if (idTheso == null || idTheso.isEmpty()) {
            return null;
        }
        if (idLang == null || idLang.isEmpty()) {
            return null;
        }
        initError();

        PrimeFaces.current().executeScript("PF('waitDialog').show();");

        ArrayList<NodeSearchMini> nodeSearchMinis = new ArrayList<>();

        ArrayList<NodeCompareTheso> nodeCompareThesosTemp = new ArrayList<>();
        boolean writtenInfo;

        // mise à jouor des concepts
        try {
            for (NodeCompareTheso nodeCompareTheso : nodeCompareThesos) {
                writtenInfo = false;
                if (nodeCompareTheso == null) {
                    continue;
                }
                if (StringUtils.isEmpty(nodeCompareTheso.getOriginalPrefLabel())) {
                    continue;
                }
                switch (selectedSearchType) {
                    case "exactWord":
                        nodeSearchMinis = searchHelper.searchExactTermForAutocompletion(connect.openConnexionPool(), nodeCompareTheso.getOriginalPrefLabel(), idLang, idTheso);
                        break;
                    case "containsExactWord":
                        nodeSearchMinis = searchHelper.searchExactMatch(connect.openConnexionPool(), nodeCompareTheso.getOriginalPrefLabel(), idLang, idTheso);
                        break;
                    case "startWith":
                        nodeSearchMinis = searchHelper.searchStartWith(connect.openConnexionPool(), nodeCompareTheso.getOriginalPrefLabel(), idLang, idTheso);
                        break;
                    case "elastic":
                        nodeSearchMinis = searchHelper.searchFullTextElastic(connect.openConnexionPool(), nodeCompareTheso.getOriginalPrefLabel(), idLang, idTheso);
                        break;
                    default:
                        break;
                }

                for (NodeSearchMini nodeSearchMini : nodeSearchMinis) {
                    if (nodeSearchMini.isConcept() || nodeSearchMini.isAltLabel()) {
                        writtenInfo = true;
                        NodeCompareTheso nodeCompareTheso2 = new NodeCompareTheso();
                        nodeCompareTheso2.setOriginalPrefLabel(nodeCompareTheso.getOriginalPrefLabel());
                        nodeCompareTheso2.setIdConcept(nodeSearchMini.getIdConcept());
                        nodeCompareTheso2.setPrefLabel(nodeSearchMini.getPrefLabel());
                        nodeCompareTheso2.setAltLabel(nodeSearchMini.getAltLabelValue());
                        nodeCompareTheso2.setIdArk(conceptHelper.getIdArkOfConcept(connect.openConnexionPool(), nodeSearchMini.getIdConcept(), idTheso));
                        nodeCompareThesosTemp.add(nodeCompareTheso2);
                    }
                }
                if (!writtenInfo) {
                    NodeCompareTheso nodeCompareTheso2 = new NodeCompareTheso();
                    nodeCompareTheso2.setOriginalPrefLabel(nodeCompareTheso.getOriginalPrefLabel());
                    nodeCompareThesosTemp.add(nodeCompareTheso2);
                }
            }
            nodeCompareThesos = nodeCompareThesosTemp;
            log.error(csvImportHelper.getMessage());
            total = nodeCompareThesos.size();
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            //total = 0;
            info = info + "\n" + "total = " + total;
            error.append(csvImportHelper.getMessage());

            CsvWriteHelper csvWriteHelper = new CsvWriteHelper();
            byte[] datas = csvWriteHelper.writeCsvFromNodeCompareTheso(nodeCompareThesos, idLang);

            try (ByteArrayInputStream input = new ByteArrayInputStream(datas)) {
                return DefaultStreamedContent.builder()
                        .contentType("text/csv")
                        .name("resultat.csv")
                        .stream(() -> input)
                        .build();
            } catch (IOException ex) {
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            return new DefaultStreamedContent();

        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
        return null;
    }

    /**
     * permet de déprécier les concepts donnés par tableau CSV
     *
     * @param idTheso
     * @param idUser1
     */
    public void deprecateConcepts(String idTheso, int idUser1) {

        loadDone = false;
        progressStep = 0;
        progress = 0;
        total = 0;

        if (nodeDeprecateds == null || nodeDeprecateds.isEmpty()) {
            return;
        }
        if (StringUtils.isEmpty(idTheso)) {
            return;
        }

        if (importInProgress) {
            return;
        }

        initError();

        String idConcept;
        String idConceptReplacedBy;
        try {
            for (NodeDeprecated nodeDeprecated : nodeDeprecateds) {
                if (nodeDeprecated == null) {
                    continue;
                }
                if (StringUtils.isEmpty(nodeDeprecated.getDeprecatedId())) {
                    continue;
                }
                idConcept = getIdConcept(nodeDeprecated.getDeprecatedId(), idTheso);
                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), idConcept, idTheso)) {
                    continue;
                }
                if (!deprecateHelper.deprecateConcept(connect.openConnexionPool(), idConcept, idTheso, idUser1, conceptHelper)) {
                    error.append("ce concept n'a pas été déprécié : ");
                    error.append(idConcept);
                    return;
                }
                if (!StringUtils.isEmpty(nodeDeprecated.getReplacedById())) {
                    idConceptReplacedBy = getIdConcept(nodeDeprecated.getReplacedById(), idTheso);
                    if (!deprecateHelper.addReplacedBy(connect.openConnexionPool(),
                            idConcept, idTheso, idConceptReplacedBy, idUser1)) {
                        error.append("Ce concept n'a pas été replacé par : ");
                        error.append(idConceptReplacedBy);
                        return;
                    }
                }

                if (noteHelper.isNoteExist(
                        connect.openConnexionPool(),
                        idConcept,
                        idTheso,
                        nodeDeprecated.getNoteLang(),
                        nodeDeprecated.getNote(),
                        "note")) {
                } else {
                    noteHelper.addNote(
                            connect.openConnexionPool(),
                            idConcept,
                            nodeDeprecated.getNoteLang(),
                            idTheso,
                            nodeDeprecated.getNote(),
                            "note",
                            "",
                            idUser1);
                }

                conceptHelper.updateDateOfConcept(connect.openConnexionPool(),
                        selectedTheso.getCurrentIdTheso(),
                        idConcept, idUser1);

                ///// insert DcTermsData to add contributor
                dcElementHelper.addDcElementConcept(connect.openConnexionPool(),
                        new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                        idConcept, selectedTheso.getCurrentIdTheso());
                ///////////////
                total++;
            }
            log.error(csvImportHelper.getMessage());

            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            //total = 0;
            info = info + "\n" + "total = " + total;
            error.append(csvImportHelper.getMessage());
            roleOnThesoBean.showListTheso(currentUser);
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
        //    System.gc();
        //    System.gc();

        onComplete();
    }

    /**
     * insérer un thésaurus dans la BDD (CSV)
     *
     * @param idTheso
     * @param idUser1
     */
    public void replaceValueByNewValue(String idTheso, int idUser1) {

        loadDone = false;
        progressStep = 0;
        progress = 0;
        total = 0;

        if (nodeReplaceValueByValues == null || nodeReplaceValueByValues.isEmpty()) {
            return;
        }
        if (idTheso == null || idTheso.isEmpty()) {
            return;
        }

        if (importInProgress) {
            return;
        }

        initError();

        String idConcept;
        // mise à jouor des concepts
        try {
            for (NodeReplaceValueByValue nodeReplaceValueByValue : nodeReplaceValueByValues) {
                if (nodeReplaceValueByValue == null) {
                    continue;
                }
                if (nodeReplaceValueByValue.getIdConcept() == null || nodeReplaceValueByValue.getIdConcept().isEmpty()) {
                    continue;
                }
                idConcept = getIdConcept(nodeReplaceValueByValue.getIdConcept(), idTheso);
                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                nodeReplaceValueByValue.setIdConcept(idConcept);
                // controle pour vérifier l'existance de l'Id
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso())) {
                    continue;
                }

                if (nodeReplaceValueByValue.getSKOSProperty() == SKOSProperty.BROADER) {
                    String oldBt = null;
                    if (!StringUtils.isEmpty(nodeReplaceValueByValue.getOldValue())) {
                        oldBt = getIdConcept(nodeReplaceValueByValue.getOldValue(), idTheso);
                    }
                    String newBt = getIdConcept(nodeReplaceValueByValue.getNewValue(), idTheso);
                    if (StringUtils.isEmpty(newBt)) {
                        continue;
                    }
                    nodeReplaceValueByValue.setOldValue(oldBt);
                    nodeReplaceValueByValue.setNewValue(newBt);
                }
                if (StringUtils.isEmpty(nodeReplaceValueByValue.getNewValue())) {
                    continue;
                }
                if (csvImportHelper.updateConceptValueByNewValue(connect.openConnexionPool(), idTheso, nodeReplaceValueByValue, idUser1)) {
                    total++;
                    conceptHelper.updateDateOfConcept(connect.openConnexionPool(), idTheso, idConcept,
                            currentUser.getNodeUser().getIdUser());
                    ///// insert DcTermsData to add contributor
                    dcElementHelper.addDcElementConcept(connect.openConnexionPool(),
                            new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                            idConcept, idTheso);
                    ///////////////
                }
            }
            log.error(csvImportHelper.getMessage());

            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            //total = 0;
            info = info + "\n" + "total = " + total;
            error.append(csvImportHelper.getMessage());
            roleOnThesoBean.showListTheso(currentUser);
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
        //    System.gc();
        //    System.gc();

        onComplete();
    }

    private String getIdConcept(String idToFind, String idTheso) {
        String idConcept = null;
        if ("ark".equalsIgnoreCase(selectedIdentifierImportAlign)) {

            idConcept = conceptHelper.getIdConceptFromArkId(connect.openConnexionPool(), idToFind, idTheso);
        }
        if ("handle".equalsIgnoreCase(selectedIdentifierImportAlign)) {
            idConcept = conceptHelper.getIdConceptFromHandleId(connect.openConnexionPool(), idToFind);
        }
        if ("identifier".equalsIgnoreCase(selectedIdentifierImportAlign)) {
            idConcept = idToFind;
        }
        return idConcept;
    }

    private String getIdGroup(String idToFind, String idTheso) {
        String idGroup = null;
        if ("ark".equalsIgnoreCase(selectedIdentifierImportAlign)) {
            idGroup = groupHelper.getIdGroupFromArkId(connect.openConnexionPool(), idToFind, idTheso);
        }
        if ("handle".equalsIgnoreCase(selectedIdentifierImportAlign)) {
            idGroup = groupHelper.getIdGroupFromHandleId(connect.openConnexionPool(), idToFind);
        }
        if ("identifier".equalsIgnoreCase(selectedIdentifierImportAlign)) {
            idGroup = idToFind;
        }
        return idGroup;
    }

///////////////////////////////////////////////////////////////////////////////
///////////////// Fonctions pour importer des données en CSV //////////////////
///////////////// L'ajout des données se fait en fusion  //////////////////////
///////////////////////////////////////////////////////////////////////////////
    /**
     * permet d'ajouter une liste de notes en CSV au thésaurus
     *
     */
    public void addArkList() {
        if (selectedTheso.getCurrentIdTheso() == null || selectedTheso.getCurrentIdTheso().isEmpty()) {
            warning = "pas de thésaurus sélectionné";
            return;
        }
        if (nodeIdValues == null || nodeIdValues.isEmpty()) {
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
        try {
            for (NodeIdValue nodeIdValue : nodeIdValues) {
                if (nodeIdValue == null) {
                    continue;
                }
                if (nodeIdValue.getId() == null || nodeIdValue.getId().isEmpty()) {
                    continue;
                }
                // controle pour vérifier l'existance de l'Id
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), nodeIdValue.getId(), selectedTheso.getCurrentIdTheso())) {
                    continue;
                }

                if (clearBefore) {
                    if (conceptHelper.updateArkIdOfConcept(connect.openConnexionPool(), nodeIdValue.getId(), selectedTheso.getCurrentIdTheso(), nodeIdValue.getValue())) {
                        total++;
                    }
                } else {
                    if (!conceptHelper.isHaveIdArk(connect.openConnexionPool(), selectedTheso.getCurrentIdTheso(), nodeIdValue.getId())) {
                        if (conceptHelper.updateArkIdOfConcept(connect.openConnexionPool(), nodeIdValue.getId(), selectedTheso.getCurrentIdTheso(), nodeIdValue.getValue())) {
                            total++;
                        }
                    }
                }
                progressStep++;
                progress = progressStep / total * 100;
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "import réussi, Arks importés = " + (int) total;
            total = 0;
        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
    }

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
        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                if (conceptObject == null) {
                    continue;
                }
                if (conceptObject.getIdConcept() == null || conceptObject.getIdConcept().isEmpty()) {
                    continue;
                }
                if ("ark".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.openConnexionPool(), conceptObject.getIdConcept(), selectedTheso.getCurrentIdTheso());
                }
                if ("handle".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.openConnexionPool(), conceptObject.getIdConcept());
                }
                if ("identifier".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptObject.getIdConcept();
                }

                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                // controle pour vérifier l'existance de l'Id
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso())) {
                    continue;
                }

                if (clearBefore) {
                    try (Connection conn = connect.openConnexionPool().getConnection()) {
                        conn.setAutoCommit(false);
                        if (!noteHelper.deleteNotes(conn, idConcept, selectedTheso.getCurrentIdTheso())) {
                            conn.rollback();
                        } else {
                            conn.commit();
                        }
                    } catch (Exception e) {
                        error.append("erreur de suppression: ");
                        error.append(idConcept);
                    }
                }

                //definition
                for (CsvReadHelper.Label definition : conceptObject.getDefinitions()) {
                    if (!noteHelper.isNoteExist(connect.openConnexionPool(), idConcept,
                            selectedTheso.getCurrentIdTheso(), definition.getLang(),
                            definition.getLabel(), "definition")) {
                        noteHelper.addNote(connect.openConnexionPool(), idConcept, definition.getLang(),
                                selectedTheso.getCurrentIdTheso(), definition.getLabel(), "definition", "", -1);
                        total++;
                    }
                }
                // historyNote
                for (CsvReadHelper.Label historyNote : conceptObject.getHistoryNotes()) {
                    if (!noteHelper.isNoteExist(connect.openConnexionPool(), idConcept,
                            selectedTheso.getCurrentIdTheso(), historyNote.getLang(),
                            historyNote.getLabel(), "historyNote")) {
                        noteHelper.addNote(connect.openConnexionPool(), idConcept, historyNote.getLang(),
                                selectedTheso.getCurrentIdTheso(), historyNote.getLabel(), "historyNote", "", -1);
                        total++;
                    }
                }
                // changeNote
                for (CsvReadHelper.Label changeNote : conceptObject.getChangeNotes()) {
                    if (!noteHelper.isNoteExist(connect.openConnexionPool(), idConcept,
                            selectedTheso.getCurrentIdTheso(), changeNote.getLang(),
                            changeNote.getLabel(), "changeNote")) {
                        noteHelper.addNote(connect.openConnexionPool(), idConcept, changeNote.getLang(),
                                selectedTheso.getCurrentIdTheso(), changeNote.getLabel(), "changeNote", "", -1);
                        total++;
                    }
                }
                // editorialNote
                for (CsvReadHelper.Label editorialNote : conceptObject.getEditorialNotes()) {
                    if (!noteHelper.isNoteExist(connect.openConnexionPool(), idConcept,
                            selectedTheso.getCurrentIdTheso(), editorialNote.getLang(),
                            editorialNote.getLabel(), "editorialNote")) {
                        noteHelper.addNote(connect.openConnexionPool(), idConcept, editorialNote.getLang(),
                                selectedTheso.getCurrentIdTheso(), editorialNote.getLabel(), "editorialNote", "", -1);
                        total++;
                    }
                }
                // example
                for (CsvReadHelper.Label example : conceptObject.getExamples()) {
                    if (!noteHelper.isNoteExist(connect.openConnexionPool(), idConcept,
                            selectedTheso.getCurrentIdTheso(), example.getLang(),
                            example.getLabel(), "example")) {
                        noteHelper.addNote(connect.openConnexionPool(), idConcept, example.getLang(),
                                selectedTheso.getCurrentIdTheso(), example.getLabel(), "example", "", -1);
                        total++;
                    }
                }

                //pour Concept
                // note
                for (CsvReadHelper.Label note : conceptObject.getNote()) {
                    if (!noteHelper.isNoteExist(connect.openConnexionPool(),
                            idConcept,
                            selectedTheso.getCurrentIdTheso(), note.getLang(),
                            note.getLabel(), "note")) {
                        noteHelper.addNote(connect.openConnexionPool(),
                                idConcept, note.getLang(),
                                selectedTheso.getCurrentIdTheso(), note.getLabel(), "note", "", -1);
                        total++;
                    }
                }
                // scopeNote
                for (CsvReadHelper.Label scopeNote : conceptObject.getScopeNotes()) {
                    if (!noteHelper.isNoteExist(connect.openConnexionPool(),
                            idConcept,
                            selectedTheso.getCurrentIdTheso(), scopeNote.getLang(),
                            scopeNote.getLabel(), "scopeNote")) {
                        noteHelper.addNote(connect.openConnexionPool(),
                                idConcept, scopeNote.getLang(),
                                selectedTheso.getCurrentIdTheso(), scopeNote.getLabel(), "scopeNote", "", -1);
                        total++;
                    }
                }

                progressStep++;
                progress = progressStep / total * 100;
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "import réussi, notes importées = " + (int) total;
            total = 0;
        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
    }

    /**
     * permet d'ajouter une liste de notes en CSV au thésaurus
     *
     */
    public void deleteAltLabelList() {
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
        String idTerm ;
        String idConcept = null;
        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                if (conceptObject == null) {
                    continue;
                }
                if (conceptObject.getIdConcept() == null || conceptObject.getIdConcept().isEmpty()) {
                    continue;
                }
                if ("ark".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.openConnexionPool(), conceptObject.getIdConcept(), selectedTheso.getCurrentIdTheso());
                }
                if ("handle".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.openConnexionPool(), conceptObject.getIdConcept());
                }
                if ("identifier".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptObject.getIdConcept();
                }

                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                // controle pour vérifier l'existance de l'Id
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso())) {
                    continue;
                }

                //Suppression des synonymes
                idTerm = termHelper.getIdTermOfConcept(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso());
                if(idTerm != null) {
                    for (CsvReadHelper.Label altLabel : conceptObject.getAltLabels()) {
                        termHelper.deleteNonPreferedTerm(connect.openConnexionPool(), idTerm, altLabel.getLang(), altLabel.getLabel(), selectedTheso.getCurrentIdTheso(), currentUser.getNodeUser().getIdUser());
                        total++;
                    }
                }

                progressStep++;
                progress = progressStep / total * 100;
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "Suppression réussie, synonymes importés = " + (int) total;
            total = 0;
        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
    }

    /**
     * permet d'ajouter une liste de notes en CSV au thésaurus
     *
     */
    public void addAltLabelList() {
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
        String idTerm;
        String idConcept = null;
        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                if (conceptObject == null) {
                    continue;
                }
                if (conceptObject.getIdConcept() == null || conceptObject.getIdConcept().isEmpty()) {
                    continue;
                }
                if ("ark".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.openConnexionPool(), conceptObject.getIdConcept(), selectedTheso.getCurrentIdTheso());
                }
                if ("handle".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.openConnexionPool(), conceptObject.getIdConcept());
                }
                if ("identifier".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptObject.getIdConcept();
                }

                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                // controle pour vérifier l'existance de l'Id
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso())) {
                    continue;
                }

                if (clearBefore) {
                    if (!termHelper.deleteAllNonPreferedTerm(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso())) {
                        error.append("erreur de suppression: ");
                        error.append(idConcept);
                        return;
                    }
                }

                //ajout des synonymes
                idTerm = termHelper.getIdTermOfConcept(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso());
                if(idTerm != null) {
                    for (CsvReadHelper.Label altLabel : conceptObject.getAltLabels()) {
                        termHelper.addNonPreferredTerm(connect.openConnexionPool(), idTerm, altLabel.getLabel(), altLabel.getLang(), selectedTheso.getCurrentIdTheso(), "import",
                                "", false, currentUser.getNodeUser().getIdUser());
                        total++;
                    }
                }

                progressStep++;
                progress = progressStep / total * 100;
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "import réussi, synonymes importés = " + (int) total;
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
    public void addImageList() {
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
        PrimeFaces.current().executeScript("PF('waitDialog').show();");
        initError();
        loadDone = false;
        progressStep = 0;
        progress = 0;
        total = 0;
        String idConcept = null;

        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                if (conceptObject == null) {
                    continue;
                }
                if (conceptObject.getLocalId() == null || conceptObject.getLocalId().isEmpty()) {
                    continue;
                }
                if (conceptObject.getImages() == null || conceptObject.getImages().isEmpty()) {
                    continue;
                }
                if ("ark".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.openConnexionPool(), conceptObject.getLocalId(), selectedTheso.getCurrentIdTheso());
                }
                if ("handle".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.openConnexionPool(), conceptObject.getLocalId());
                }
                if ("identifier".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptObject.getLocalId();
                }

                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                // controle pour vérifier l'existance de l'Id
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso())) {
                    continue;
                }

                for (NodeImage nodeImage : conceptObject.getImages()) {
                    if (nodeImage == null) {
                        continue;
                    }
                    //   nodeImage.setUri(URLEncoder.encode(nodeImage.getUri(), "UTF-8"));
                    if (!fr.cnrs.opentheso.utils.StringUtils.urlValidator(nodeImage.getUri())) {
                        error.append("URL non valide : ");
                        error.append(uri);
                        continue;
                    }
                    if (!imagesHelper.addExternalImage(connect.openConnexionPool(),
                            idConcept, selectedTheso.getCurrentIdTheso(),
                            nodeImage.getImageName(),
                            nodeImage.getCopyRight(),
                            nodeImage.getUri(),
                            nodeImage.getCreator(),
                            currentUser.getNodeUser().getIdUser())) {
                        error.append("image non insérée: ");
                        error.append(nodeImage.getUri());
                    }
                    total++;
                }
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "import réussi, images importées = " + (int) total;
            //           total = 0;
        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
    }

    /**
     * permet d'ajouter une liste de notations en CSV au thésaurus
     *
     */
    public void addNotationList() {
        if (selectedTheso.getCurrentIdTheso() == null || selectedTheso.getCurrentIdTheso().isEmpty()) {
            warning = "pas de thésaurus sélectionné";
            return;
        }
        if (nodeIdValues == null || nodeIdValues.isEmpty()) {
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
        try {
            for (NodeIdValue nodeIdValue : nodeIdValues) {
                if (nodeIdValue == null) {
                    continue;
                }
                if (nodeIdValue.getId() == null || nodeIdValue.getId().isEmpty()) {
                    continue;
                }
                if ("ark".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.openConnexionPool(), nodeIdValue.getId(), selectedTheso.getCurrentIdTheso());
                }
                if ("handle".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.openConnexionPool(), nodeIdValue.getId());
                }
                if ("identifier".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = nodeIdValue.getId();
                }

                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }

                // controle pour vérifier l'existance de l'Id
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso())) {
                    continue;
                }

                if (clearBefore) {
                    if (conceptHelper.updateNotation(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso(), nodeIdValue.getValue())) {
                        total++;
                    }
                } else {
                    if (!conceptHelper.isHaveNotation(connect.openConnexionPool(), selectedTheso.getCurrentIdTheso(), idConcept)) {
                        if (conceptHelper.updateNotation(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso(), nodeIdValue.getValue())) {
                            total++;
                        }
                    }
                }
                progressStep++;
                progress = progressStep / total * 100;
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "import réussi, notations importées = " + (int) total;
            total = 0;
        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
    }

    /**
     * permet d'ajouter une liste de notations en CSV au thésaurus
     *
     */
    public void addCollectionListToConcept() {
        if (selectedTheso.getCurrentIdTheso() == null || selectedTheso.getCurrentIdTheso().isEmpty()) {
            warning = "pas de thésaurus sélectionné";
            return;
        }
        if (nodeIdValues == null || nodeIdValues.isEmpty()) {
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
        try {
            for (NodeIdValue nodeIdValue : nodeIdValues) {
                if (nodeIdValue == null) {
                    continue;
                }
                if (nodeIdValue.getId() == null || nodeIdValue.getId().isEmpty()) {
                    continue;
                }
                if ("ark".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.openConnexionPool(), nodeIdValue.getId(), selectedTheso.getCurrentIdTheso());
                }
                if ("handle".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.openConnexionPool(), nodeIdValue.getId());
                }
                if ("identifier".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = nodeIdValue.getId();
                }

                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }

                // controle pour vérifier l'existance de l'Id
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso())) {
                    continue;
                }

                // addConceptToGroup
                if (groupHelper.addConceptGroupConcept(connect.openConnexionPool(),
                        nodeIdValue.getValue(),
                        idConcept,
                        selectedTheso.getCurrentIdTheso())) {
                    total++;
                }
                progressStep++;
                progress = progressStep / total * 100;
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "import réussi, notations importées = " + (int) total;
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
        PrimeFaces.current().executeScript("PF('waitDialog').show();");
        initError();
        loadDone = false;
        progressStep = 0;
        progress = 0;
        total = 0;
        String idConcept = null;
        NodeAlignment nodeAlignment = new NodeAlignment();

        try {
            for (NodeAlignmentImport nodeAlignmentImport : nodeAlignmentImports) {
                if (nodeAlignmentImport == null) {
                    continue;
                }
                if (nodeAlignmentImport.getLocalId() == null || nodeAlignmentImport.getLocalId().isEmpty()) {
                    continue;
                }
                if ("ark".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.openConnexionPool(), nodeAlignmentImport.getLocalId(), selectedTheso.getCurrentIdTheso());
                }
                if ("handle".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.openConnexionPool(), nodeAlignmentImport.getLocalId());
                }
                if ("identifier".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = nodeAlignmentImport.getLocalId();
                }

                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                // controle pour vérifier l'existance de l'Id
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso())) {
                    continue;
                }

                for (NodeAlignmentSmall nodeAlignmentSmall : nodeAlignmentImport.getNodeAlignmentSmalls()) {
                    if (nodeAlignmentSmall == null) {
                        continue;
                    }
                    if (nodeAlignmentSmall.getUri_target() == null || nodeAlignmentSmall.getUri_target().isEmpty()) {
                        continue;
                    }
                    nodeAlignment.setId_author(currentUser.getNodeUser().getIdUser());
                    nodeAlignment.setConcept_target("");
                    nodeAlignment.setThesaurus_target(nodeAlignmentSmall.getSource());
                    nodeAlignment.setInternal_id_concept(idConcept);
                    nodeAlignment.setInternal_id_thesaurus(selectedTheso.getCurrentIdTheso());
                    nodeAlignment.setAlignement_id_type(nodeAlignmentSmall.getAlignement_id_type());
                    nodeAlignment.setUri_target(nodeAlignmentSmall.getUri_target());
                    if (alignmentHelper.addNewAlignment(connect.openConnexionPool(), nodeAlignment)) {
                        total++;
                    }
                }
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "import réussi, alignements importés = " + (int) total;
            total = 0;
        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
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

        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                if (conceptObject.getLocalId() == null || conceptObject.getLocalId().isEmpty()) {
                    continue;
                }
                if ("ark".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromArkId(connect.openConnexionPool(), conceptObject.getLocalId(), selectedTheso.getCurrentIdTheso());
                }
                if ("handle".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptHelper.getIdConceptFromHandleId(connect.openConnexionPool(), conceptObject.getLocalId());
                }
                if ("identifier".equalsIgnoreCase(selectedIdentifierImportAlign)) {
                    idConcept = conceptObject.getLocalId();
                }
                if (idConcept == null || idConcept.isEmpty()) {
                    continue;
                }
                // controle pour vérifier l'existance de l'Id
                if (!conceptHelper.isIdExiste(connect.openConnexionPool(), idConcept, selectedTheso.getCurrentIdTheso())) {
                    continue;
                }
                for (NodeIdValue nodeIdValue : conceptObject.getAlignments()) {
                    if (alignmentHelper.deleteAlignmentByUri(connect.openConnexionPool(),
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
            info = "Suppression réussi, alignements supprimés = " + (int) total;
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
     * permet d'ajouter une liste de concepts en CSV au thésaurus les concepts
     * seront placés au bon endroit suivant l'information du BT
     *
     * @param idTheso
     * @param idUser
     */
    public void addListConceptsToTheso(String idTheso, int idUser) {
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
        csvImportHelper.setFormatDate(formatDate);
        String idConcept;
        String BT_temp;
        String idGroup;
        total = 0;
        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                if (conceptObject == null) {
                    continue;
                }
                switch (conceptObject.getType().toLowerCase()) {
                    case "skos:collection":
                        if (conceptObject.getIdConcept() == null || conceptObject.getIdConcept().isEmpty()) {
                            conceptObject.setIdConcept(null);
                        } else {
                            idGroup = getIdGroup(conceptObject.getIdConcept(), idTheso);
                            if (idGroup == null || idGroup.isEmpty()) {
                                continue;
                            }
                            conceptObject.setIdConcept(idGroup);
                            // controle pour vérifier l'existance de l'Id
                            if (groupHelper.isIdGroupExiste(connect.openConnexionPool(), idGroup, idTheso)) {
                                continue;
                            }
                        }
                        if (csvImportHelper.addGroup(connect.openConnexionPool(), idTheso, conceptObject)) {
                            total++;
                        }

                        // ajout des liens pour les sous groupes
                        for (String subGroup : conceptObject.getSubGroups()) {
                            if (groupHelper.addSubGroup(connect.openConnexionPool(), conceptObject.getIdConcept(), subGroup, idTheso)) {
                                total++;
                            }
                        }

                        break;
                    default:
                        if (conceptObject.getIdConcept() == null || conceptObject.getIdConcept().isEmpty()) {
                            conceptObject.setIdConcept(null);
                        } else {
                            idConcept = getIdConcept(conceptObject.getIdConcept(), idTheso);
                            if (idConcept == null || idConcept.isEmpty()) {
                                continue;
                            }
                            conceptObject.setIdConcept(idConcept);
                            // controle pour vérifier l'existance de l'Id
                            if (conceptHelper.isIdExiste(connect.openConnexionPool(), conceptObject.getIdConcept(), idTheso)) {
                                continue;
                            }
                        }
                        if (csvImportHelper.addConceptV2(connect.openConnexionPool(),
                                idTheso, conceptObject, currentUser.getNodeUser().getIdUser())) {
                            total++;
                        }
                        break;
                }
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            info = "import réussi";

            info = info + "\n" + "total = " + total;
            error.append(csvImportHelper.getMessage());
            viewEditionBean.init();

        } catch (Exception e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
    }

    /**
     * permet d'ajouter une liste de concepts en CSV sous le concept sélectionné
     * la liste peut être hiérarchisée si la relation BT est renseignée
     *
     * @param nodeFullConcept
     */
    public void addListCsvToConcept(NodeFullConcept nodeFullConcept) {
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

        // ajout des concepts
        String idPere = nodeFullConcept.getIdentifier();

        try {
            for (CsvReadHelper.ConceptObject conceptObject : conceptObjects) {
                // gestion de l'hiérarchie pour les listes NT,
                // si le BT est renseigné, alors on intègre le concept sous ce BT,
                // sinon, c'est le père du dossier en cours qui est pris en compte
                if (conceptObject.getBroaders().isEmpty()) {
                    csvImportHelper.addSingleConcept(connect.openConnexionPool(), selectedTheso.getCurrentIdTheso(),
                            idPere, null, currentUser.getNodeUser().getIdUser(), conceptObject, nodePreference);
                } else {
                    for (String idBT : conceptObject.getBroaders()) {
                        csvImportHelper.addSingleConcept(connect.openConnexionPool(), selectedTheso.getCurrentIdTheso(),
                                idBT, null, currentUser.getNodeUser().getIdUser(), conceptObject, nodePreference);
                    }
                }
            }
            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            total = 0;
            info = "import réussi";
            info = info + "\n" + csvImportHelper.getMessage();

            // mise à jour
            PrimeFaces pf = PrimeFaces.current();
            //if (CollectionUtils.isNotEmpty(tree.getClickselectedNodes())) {
            if (tree.getSelectedNode() != null) {
                tree.initAndExpandTreeToPath(conceptView.getNodeFullConcept().getIdentifier(),
                        selectedTheso.getCurrentIdTheso(),
                        conceptView.getSelectedLang());
            }
            conceptView.getConcept(selectedTheso.getCurrentIdTheso(), nodeFullConcept.getIdentifier(), conceptView.getSelectedLang(), currentUser);

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
        progress = 0;

        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            try (InputStream is = event.getFile().getInputStream()) {
                if (StringUtils.isEmpty(selectedLang)) {
                    selectedLang = connect.getWorkLanguage();
                }

                sKOSXmlDocument = new ReadRDF4JNewGen().readRdfFlux(is, getRdfFormat(typeImport), selectedLang);
                total = sKOSXmlDocument.getConceptList().size();
                uri = sKOSXmlDocument.getTitle();
                loadDone = true;
                BDDinsertEnable = true;
                info = "File correctly loaded";
            } catch (Exception e) {
                error.append(System.getProperty("line.separator"));
            } finally {
                showError();
            }
        }

        PrimeFaces.current().executeScript("PF('waitDialog').hide()");
    }

    private RDFFormat getRdfFormat(int format) {
        RDFFormat rdfFormat = RDFFormat.RDFJSON;
        switch (format) {
            case 0:
                rdfFormat = RDFFormat.RDFXML;
                break;
            case 1:
                rdfFormat = RDFFormat.JSONLD;
                break;
            case 2:
                rdfFormat = RDFFormat.TURTLE;
                break;
        }
        return rdfFormat;
    }

    /**
     * permet d'importer le thésaurus chargé en SKOS
     */
    public void addSkosThesoToBDD() {
        long tempsDebut, tempsFin;
        double seconds;
        tempsDebut = System.currentTimeMillis();
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
            importRdf4jHelper.setInfos(connect.openConnexionPool(),
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
            importRdf4jHelper.addLangsToThesaurus(connect.openConnexionPool(), idTheso);

            loadDone = false;
            importDone = true;
            BDDinsertEnable = false;
            importInProgress = false;
            uri = null;
            total = 0;

            info = "Thesaurus correctly insert into data base";
            info = info + "\n" + importRdf4jHelper.getMessage().toString();
            roleOnThesoBean.showListTheso(currentUser);
            viewEditionBean.init();
        } catch (SQLException e) {
            error.append(System.getProperty("line.separator"));
            error.append(e.toString());
        } finally {
            showError();
        }
        onComplete();

        tempsFin = System.currentTimeMillis();
        seconds = (tempsFin - tempsDebut) / 1000F;
        System.out.println("Ancienne méthode : Opération effectuée en: " + Double.toString(seconds) + " secondes.");

    }

    public void addSkosThesoToBDDV2() throws SQLException {

        //     long tempsDebut = System.currentTimeMillis();
        if (StringUtils.isEmpty(selectedLang)) {
            selectedLang = connect.getWorkLanguage();
        }

        int idGroup;
        if (selectedUserProject == null || selectedUserProject.isEmpty()) {
            idGroup = -1;
        } else {
            idGroup = Integer.parseInt(selectedUserProject);
        }


        importRdf4jHelper.setInfos(connect.openConnexionPool(),
                formatDate,
                currentUser.getNodeUser().getIdUser(),
                idGroup,
                selectedLang);

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
            if (!sKOSResource.getLabelsList().isEmpty()) {
                importRdf4jHelper.addConceptV2(sKOSResource, idTheso);
            }
        }

        importRdf4jHelper.addFacetsV2(sKOSXmlDocument.getFacetList(), idTheso);
        importRdf4jHelper.addGroups(sKOSXmlDocument.getGroupList(), idTheso);
        importRdf4jHelper.addLangsToThesaurus(connect.openConnexionPool(), idTheso);
        importRdf4jHelper.addFoafImages(sKOSXmlDocument.getFoafImage(), idTheso);

        roleOnThesoBean.showListTheso(currentUser);
        viewEditionBean.init();

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Le thesaurus " + idTheso + " est correctement ajouté !", "import réussi"));
        PrimeFaces.current().ajax().update("messageIndex");

        /*    long tempsFin = System.currentTimeMillis();
        double seconds = (tempsFin - tempsDebut) / 1000F;
        System.out.println("Nouvelle méthode : Opération effectuée en: " + seconds + " secondes.");*/
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

            importRdf4jHelper.setInfos(connect.openConnexionPool(), formatDate,
                    currentUser.getNodeUser().getIdUser(), idGroup, roleOnThesoBean.getNodePreference().getSourceLang());//connect.getWorkLanguage());

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
        } catch (IOException e) {

        }
        onComplete();
    }

    private Integer progress1;

    public void action() {
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
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
            PrimeFaces.current().ajax().update("messageIndex");
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

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalInt() {
        return (int) total;
    }

    public void setTotalInt(int total) {
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

    public boolean isClearBefore() {
        return clearBefore;
    }

    public void setClearBefore(boolean clearBefore) {
        this.clearBefore = clearBefore;
    }

    public String getSelectedIdentifierImportAlign() {
        return selectedIdentifierImportAlign;
    }

    public void setSelectedIdentifierImportAlign(String selectedIdentifierImportAlign) {
        this.selectedIdentifierImportAlign = selectedIdentifierImportAlign;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSelectedSearchType() {
        return selectedSearchType;
    }

    public void setSelectedSearchType(String selectedSearchType) {
        this.selectedSearchType = selectedSearchType;
    }

    public String getSelectedConcept() {
        return selectedConcept;
    }

    public void setSelectedConcept(String selectedConcept) {
        this.selectedConcept = selectedConcept;
    }

    public String getAlignmentSource() {
        return alignmentSource;
    }

    public void setAlignmentSource(String alignmentSource) {
        this.alignmentSource = alignmentSource;
    }

}
