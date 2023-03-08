package fr.cnrs.opentheso.bean.importexport;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.ExportHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTree;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupLabel;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.bean.candidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewEditionBean;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewExportBean;
import fr.cnrs.opentheso.core.exports.csv.CsvWriteHelper;
import fr.cnrs.opentheso.core.exports.csv.WriteCSV;
import fr.cnrs.opentheso.core.exports.pdf.WritePdf;
import fr.cnrs.opentheso.core.exports.rdf4j.ExportRdf4jHelperNew;
import fr.cnrs.opentheso.core.exports.rdf4j.WriteRdf4j;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;

import org.primefaces.model.StreamedContent;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

/**
 *
 * @author miledrousset
 */
@Named(value = "exportFileBean")
@SessionScoped
public class ExportFileBean implements Serializable {

    private final static String DATE_FORMAT = "dd-mm-yyyy";
    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    @Inject
    private Connect connect;
    @Inject
    private ViewExportBean viewExportBean;
    @Inject
    private CandidatBean candidatBean;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private ViewEditionBean viewEditionBean;

    // progressBar
    private int sizeOfTheso;
    private float progressBar, progressStep;

    int posJ = 0;
    int posX = 0;

    public StreamedContent exportCandidatsEnSkos() {
        initProgressBar();

        RDFFormat format = null;
        String extention = "xml";

        switch (candidatBean.getSelectedExportFormat().toLowerCase()) {
            case "skos":
                format = RDFFormat.RDFXML;
                extention = ".rdf";
                break;
            case "jsonld":
                format = RDFFormat.JSONLD;
                extention = ".json";
                break;
            case "turtle":
                format = RDFFormat.TURTLE;
                extention = ".ttl";
                break;
            case "json":
                format = RDFFormat.RDFJSON;
                extention = ".json";
                break;
        }

        ExportRdf4jHelperNew datas = getCandidatsDatas(true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(new WriteRdf4j(datas.getSkosXmlDocument()).getModel(), out, format);

        return DefaultStreamedContent.builder()
                .contentType("application/xml")
                .name("candidats" + extention)
                .stream(() -> new ByteArrayInputStream(out.toByteArray()))
                .build();
    }

    private ExportRdf4jHelperNew getCandidatsDatas(boolean isCandidatExport) {

        NodePreference nodePreference = new PreferencesHelper().getThesaurusPreferences(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso());

        if (nodePreference == null) {
            return null;
        }

        candidatBean.setProgressBarStep(100 / candidatBean.getCandidatList().size());

        ExportRdf4jHelperNew resources = new ExportRdf4jHelperNew();
        resources.setInfos(nodePreference, DATE_FORMAT, false, false);
        resources.exportTheso(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), nodePreference);

        for (CandidatDto candidat : candidatBean.getCandidatList()) {
            candidatBean.setProgressBarValue(candidatBean.getProgressBarValue() + candidatBean.getProgressBarStep());
            resources.exportConcept(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), candidat.getIdConcepte(), isCandidatExport);
        }

        return resources;
    }

    public void exportToVertuoso() {
        SKOSXmlDocument skosxd = getThesorusDatas(viewExportBean.getNodeIdValueOfTheso().getId(),
                viewExportBean.getSelectedIdGroups());

        if (skosxd == null) {
            return;
        }

        if (viewEditionBean.isViewImportVirtuoso()) {

            String msg = null;
            if (StringUtils.isEmpty(viewEditionBean.getUrlServer())) {
                msg = "L'URL du serveur Virtuoso est manquant !";
            } else if (StringUtils.isEmpty(viewEditionBean.getLogin())) {
                msg = "Le login pour se connecter au serveur Virtuoso est manquant !";
            } else if (StringUtils.isEmpty(viewEditionBean.getPassword())) {
                msg = "Le mot de passe pour se connecter au serveur Virtuoso est manquant !";
            }

            if (!StringUtils.isEmpty(msg)) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", msg));
                PrimeFaces.current().ajax().update("messageIndex");
                return;
            }

            boolean resultat = exportThesorusToVirtuoso(skosxd, viewEditionBean.getNomGraphe(), viewEditionBean.getUrlServer(),
                    viewEditionBean.getLogin(), viewEditionBean.getPassword());

            if (resultat) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "",
                        "Exportation du thésaurus '" + viewExportBean.getNodeIdValueOfTheso().getId() + "' est terminée avec succès"));
                PrimeFaces.current().ajax().update("messageIndex");
            }

        }
    }

    private void createMatrice(String[][] tab, NodeTree concept) {
        tab[posX][posJ] = concept.getPreferredTerm();
        if (CollectionUtils.isNotEmpty(concept.getChildrens())) {
            posJ++;
            if (posX < tab.length-1) posX++;
            for (NodeTree nodeTree : concept.getChildrens()) {
                if (CollectionUtils.isNotEmpty(nodeTree.getChildrens())) {
                    createMatrice(tab, nodeTree);
                } else {
                    tab[posX][posJ] = nodeTree.getPreferredTerm();
                    if (posJ > tab.length - 1) {
                        posJ--;
                    }
                }
            }
            posJ--;
            if (posX < tab.length-1) posX++;
        }
    }

    private List<NodeTree> parcourirArbre(String thesoId, String langId, String parentId) {

        ConceptHelper conceptHelper = new ConceptHelper();
        List<NodeTree> concepts = conceptHelper.getListChildrenOfConceptWithTerm(
                connect.getPoolConnexion(), parentId, langId, thesoId);
        for (NodeTree concept : concepts) {
            sizeOfTheso++;
            concept.setIdParent(parentId);
            concept.setPreferredTerm(StringUtils.isEmpty(concept.getPreferredTerm()) ? "(" + concept.getIdConcept() + ")" : concept.getPreferredTerm());
            concept.setChildrens(parcourirArbre(thesoId, langId, concept.getIdConcept()));

            List<NodeTree> facettes = new Tree().searchFacettesForTree(connect.getPoolConnexion(), parentId, thesoId, langId);
            if (CollectionUtils.isNotEmpty(facettes)) {
                sizeOfTheso += facettes.size();
                concept.getChildrens().addAll(facettes);
            }
        }

        return concepts;
    }

    public StreamedContent exportThesorus() throws SQLException {
        
        /// export des concepts dépréciés
        if ("deprecated".equalsIgnoreCase(viewExportBean.getFormat())) { 
            CsvWriteHelper csvWriteHelper = new CsvWriteHelper();
            byte[] datas;
            if (viewExportBean.isToogleFilterByGroup()) {
                datas = csvWriteHelper.writeCsvByDeprecated(connect.getPoolConnexion(),
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), viewExportBean.getSelectedIdGroups(),
                        viewExportBean.getCsvDelimiterChar());
            } else {
                datas = csvWriteHelper.writeCsvByDeprecated(connect.getPoolConnexion(),
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), null,
                        viewExportBean.getCsvDelimiterChar());
            }
            if (datas == null) {
                return null;
            }

            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            try ( ByteArrayInputStream input = new ByteArrayInputStream(datas)) {
                return DefaultStreamedContent.builder()
                        .contentType("text/csv")
                        .name(viewExportBean.getNodeIdValueOfTheso().getId() + ".csv")
                        .stream(() -> input)
                        .build();
            } catch (IOException ex) {
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            return new DefaultStreamedContent();
        }
        ///////////////////////////////////
        if ("CSV_STRUC".equalsIgnoreCase(viewExportBean.getFormat())) {
            sizeOfTheso = 0;
            List<NodeTree> topConcepts = new ConceptHelper().getTopConceptsWithTermByTheso(connect.getPoolConnexion(),
                    viewExportBean.getNodeIdValueOfTheso().getId(), 
                    viewExportBean.getSelectedIdLangTheso());

            for (NodeTree topConcept : topConcepts) {
                sizeOfTheso++;
                topConcept.setPreferredTerm(StringUtils.isEmpty(topConcept.getPreferredTerm())
                        ? "(" + topConcept.getIdConcept() + ")" : topConcept.getPreferredTerm());
                topConcept.setChildrens(parcourirArbre(viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), 
                        topConcept.getIdConcept()));
            }

            String[][] tab = new String[sizeOfTheso][20];
            posX = 0;
            for (NodeTree topConcept : topConcepts) {
                posJ = 0;
                createMatrice(tab, topConcept);
            }

            byte[] str = new WriteCSV().importTreeCsv(tab, ';');

            try ( ByteArrayInputStream flux = new ByteArrayInputStream(str)) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return DefaultStreamedContent.builder().contentType("text/csv")
                        .name(viewExportBean.getNodeIdValueOfTheso().getId() + ".csv")
                        .stream(() -> flux)
                        .build();
                
            } catch (Exception ex) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return new DefaultStreamedContent();
            }
        }

        ///////////////////////////////////
        /// export des concepts avec Id, label
        if ("CSV_id".equalsIgnoreCase(viewExportBean.getFormat())) {
            CsvWriteHelper csvWriteHelper = new CsvWriteHelper();
            byte[] datas;
            if (viewExportBean.isToogleFilterByGroup()) {
                datas = csvWriteHelper.writeCsvById(connect.getPoolConnexion(),
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), viewExportBean.getSelectedIdGroups(), viewExportBean.getCsvDelimiterChar());
            } else {
                datas = csvWriteHelper.writeCsvById(connect.getPoolConnexion(),
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), null, viewExportBean.getCsvDelimiterChar());
            }
            if (datas == null) {
                return null;
            } 

            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            try ( ByteArrayInputStream input = new ByteArrayInputStream(datas)) {
                return DefaultStreamedContent.builder()
                        .contentType("text/csv")
                        .name(viewExportBean.getNodeIdValueOfTheso().getId() + ".csv")
                        .stream(() -> input)
                        .build();
            } catch (IOException ex) {
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            return new DefaultStreamedContent();
        }
        ///////////////////////////////////

        /// autres exports
        SKOSXmlDocument skosxd = getThesorusDatas(viewExportBean.getNodeIdValueOfTheso().getId(),
                viewExportBean.getSelectedIdGroups());

        if (skosxd == null) {
            return null;
        }

        if ("PDF".equalsIgnoreCase(viewExportBean.getFormat())) {

            try ( ByteArrayInputStream flux = new ByteArrayInputStream(new WritePdf().createPdfFile(
                    connect.getPoolConnexion(),
                    skosxd,
                    viewExportBean.getSelectedLang1_PDF(),
                    viewExportBean.getSelectedLang2_PDF(),
                    viewExportBean.getTypes().indexOf(viewExportBean.getTypeSelected())))) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return DefaultStreamedContent
                        .builder()
                        .contentType("application/pdf")
                        .name(viewExportBean.getNodeIdValueOfTheso().getId() + ".pdf")
                        .stream(() -> flux)
                        .build();
            } catch (Exception ex) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return new DefaultStreamedContent();
            }

        } else if ("CSV".equalsIgnoreCase(viewExportBean.getFormat())) {

            //char separateur = "\\t".equals(viewExportBean.getCsvDelimiter()) ? '\t' : viewExportBean.getCsvDelimiter().charAt(0);
            //byte[] str = new WriteCSV().importCsv(skosxd, viewExportBean.getSelectedLanguages(), separateur);

            CsvWriteHelper csvWriteHelper = new CsvWriteHelper();
            byte[] str = csvWriteHelper.writeCsv(skosxd,
                        viewExportBean.getSelectedLanguages(),
                        viewExportBean.getCsvDelimiterChar());            
            
            try ( ByteArrayInputStream flux = new ByteArrayInputStream(str)) {

                str = null;
                skosxd = null;
            //    System.gc();
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return DefaultStreamedContent.builder().contentType("text/csv")
                        .name(viewExportBean.getNodeIdValueOfTheso().getId() + ".csv")
                        .stream(() -> flux)
                        .build();
            } catch (Exception ex) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return new DefaultStreamedContent();
            }
        } else {
            RDFFormat format = null;
            String extention = ".xml";

            switch (viewExportBean.getSelectedExportFormat().toLowerCase()) {
                case "rdf":
                    format = RDFFormat.RDFXML;
                    extention = ".rdf";
                    break;
                case "jsonld":
                    format = RDFFormat.JSONLD;
                    extention = ".json";
                    break;
                case "turtle":
                    format = RDFFormat.TURTLE;
                    extention = ".ttl";
                    break;
                case "json":
                    format = RDFFormat.RDFJSON;
                    extention = ".json";
                    break;
            }

            try ( ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                WriteRdf4j writeRdf4j = new WriteRdf4j(skosxd);
                Rio.write(writeRdf4j.getModel(), out, format);
                writeRdf4j.closeCache();

                skosxd.clear();
                skosxd = null;
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                try ( ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray())) {
                    return DefaultStreamedContent.builder()
                            .contentType("application/xml")
                            .name(viewExportBean.getNodeIdValueOfTheso().getId() + extention)
                            .stream(() -> input)
                            .build();
                }
            } catch (Exception ex) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return new DefaultStreamedContent();
            }
        }
    }

    public StreamedContent exportNewGen() throws Exception {
        if ("deprecated".equalsIgnoreCase(viewExportBean.getFormat())) {
            CsvWriteHelper csvWriteHelper = new CsvWriteHelper();
            byte[] datas;
            if (viewExportBean.isToogleFilterByGroup()) {
                datas = csvWriteHelper.writeCsvByDeprecated(connect.getPoolConnexion(),
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), viewExportBean.getSelectedIdGroups(),
                        viewExportBean.getCsvDelimiterChar());
            } else {
                datas = csvWriteHelper.writeCsvByDeprecated(connect.getPoolConnexion(),
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), null,
                        viewExportBean.getCsvDelimiterChar());
            }
            if (datas == null) {
                return null;
            }

            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            try ( ByteArrayInputStream input = new ByteArrayInputStream(datas)) {
                return DefaultStreamedContent.builder()
                        .contentType("text/csv")
                        .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + ".csv")
                        .stream(() -> input)
                        .build();
            } catch (IOException ex) {
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            return new DefaultStreamedContent();
        }
        ///////////////////////////////////
        if ("CSV_STRUC".equalsIgnoreCase(viewExportBean.getFormat())) {
            sizeOfTheso = 0;
            ConceptHelper conceptHelper = new ConceptHelper();
            List<NodeTree> topConcepts = conceptHelper.getTopConceptsWithTermByTheso(connect.getPoolConnexion(),
                    viewExportBean.getNodeIdValueOfTheso().getId(), viewExportBean.getSelectedIdLangTheso());

            for (NodeTree topConcept : topConcepts) {
                sizeOfTheso++;
                topConcept.setPreferredTerm(StringUtils.isEmpty(topConcept.getPreferredTerm())
                        ? "(" + topConcept.getIdConcept() + ")" : topConcept.getPreferredTerm());
                topConcept.setChildrens(parcourirArbre(viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), topConcept.getIdConcept()));
            }

            String[][] tab = new String[sizeOfTheso][20];
            posX = 0;
            for (NodeTree topConcept : topConcepts) {
                posJ = 0;
                createMatrice(tab, topConcept);
            }

            byte[] str = new WriteCSV().importTreeCsv(tab, ';');

            try ( ByteArrayInputStream flux = new ByteArrayInputStream(str)) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return DefaultStreamedContent.builder().contentType("text/csv")
                        .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + ".csv")
                        .stream(() -> flux)
                        .build();
            } catch (Exception ex) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return new DefaultStreamedContent();
            }
        }

        ///////////////////////////////////
        /// export des concepts avec Id, label
        if ("CSV_id".equalsIgnoreCase(viewExportBean.getFormat())) {
            CsvWriteHelper csvWriteHelper = new CsvWriteHelper();
            byte[] datas;
            if (viewExportBean.isToogleFilterByGroup()) {
                datas = csvWriteHelper.writeCsvById(connect.getPoolConnexion(),
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), viewExportBean.getSelectedIdGroups(), viewExportBean.getCsvDelimiterChar());
            } else {
                datas = csvWriteHelper.writeCsvById(connect.getPoolConnexion(),
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), null, viewExportBean.getCsvDelimiterChar());
            }
            if (datas == null) {
                return null;
            }

            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            try ( ByteArrayInputStream input = new ByteArrayInputStream(datas)) {
                return DefaultStreamedContent.builder()
                        .contentType("text/csv")
                        .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + ".csv")
                        .stream(() -> input)
                        .build();
            } catch (IOException ex) {
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            return new DefaultStreamedContent();
        }

        SKOSXmlDocument skosxd = getConcepts(viewExportBean.getNodeIdValueOfTheso().getId());

        if (skosxd == null) {
            return null;
        }

        if ("PDF".equalsIgnoreCase(viewExportBean.getFormat())) {

            try ( ByteArrayInputStream flux = new ByteArrayInputStream(new WritePdf().createPdfFile(
                    connect.getPoolConnexion(),
                    skosxd,
                    viewExportBean.getSelectedLang1_PDF(),
                    viewExportBean.getSelectedLang2_PDF(),
                    viewExportBean.getTypes().indexOf(viewExportBean.getTypeSelected())))) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");

                return DefaultStreamedContent
                        .builder()
                        .contentType("application/pdf")
                        .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + ".pdf")
                        .stream(() -> flux)
                        .build();
            } catch (Exception ex) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return new DefaultStreamedContent();
            }

        } else if ("CSV".equalsIgnoreCase(viewExportBean.getFormat())) {

            //char separateur = "\\t".equals(viewExportBean.getCsvDelimiter()) ? '\t' : viewExportBean.getCsvDelimiter().charAt(0);
            //byte[] str = new WriteCSV().importCsv(skosxd, viewExportBean.getSelectedLanguages(), separateur);
            
            CsvWriteHelper csvWriteHelper = new CsvWriteHelper();
            byte[] str = csvWriteHelper.writeCsv(skosxd,
                        viewExportBean.getSelectedLanguages(),
                        viewExportBean.getCsvDelimiterChar());               

            try ( ByteArrayInputStream flux = new ByteArrayInputStream(str)) {
                
                return DefaultStreamedContent.builder().contentType("text/csv")
                        .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + ".csv")
                        .stream(() -> flux)
                        .build();
            } catch (Exception ex) {
                return new DefaultStreamedContent();
            }
        } else {
            RDFFormat format = null;
            String extention = ".xml";

            switch (viewExportBean.getSelectedExportFormat().toLowerCase()) {
                case "rdf":
                    format = RDFFormat.RDFXML;
                    extention = ".rdf";
                    break;
                case "jsonld":
                    format = RDFFormat.JSONLD;
                    extention = ".json";
                    break;
                case "turtle":
                    format = RDFFormat.TURTLE;
                    extention = ".ttl";
                    break;
                case "json":
                    format = RDFFormat.RDFJSON;
                    extention = ".json";
                    break;
            }

            try ( ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                //   System.out.println("total 2 = " + skosxd.getConceptList().size());
                WriteRdf4j writeRdf4j = new WriteRdf4j(skosxd);
                Rio.write(writeRdf4j.getModel(), out, format);
                writeRdf4j.closeCache();

                skosxd.clear();
                skosxd = null;
            //    System.gc();
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                try ( ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray())) {
                    out.close();
                    /*          System.out.println(">>> FIN 2 : " + System.currentTimeMillis());
                    System.out.println(">>> FIN 2 : " + (System.currentTimeMillis() - start));
                    System.out.println(">>> FIN (SEC) 2 : " + ((System.currentTimeMillis() - start) / 1000));*/

                    return DefaultStreamedContent.builder()
                            .contentType("application/xml")
                            .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + extention)
                            .stream(() -> input)
                            .build();
                }
            } catch (Exception ex) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return new DefaultStreamedContent();
            }
        }

    }

    private SKOSXmlDocument getConcepts(String idTheso) throws Exception {

        NodePreference nodePreference = new PreferencesHelper().getThesaurusPreferences(connect.getPoolConnexion(),
                idTheso);

        if (nodePreference == null) {
            return null;
        }

        ExportRdf4jHelperNew exportRdf4jHelperNew = new ExportRdf4jHelperNew();
        exportRdf4jHelperNew.setInfos(nodePreference, DATE_FORMAT, false, false);
        exportRdf4jHelperNew.exportTheso(connect.getPoolConnexion(),
                idTheso, nodePreference);

        String contextPath = FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
        String serverAdress = FacesContext.getCurrentInstance().getExternalContext().getRequestServerName();
        String protocole = FacesContext.getCurrentInstance().getExternalContext().getRequestScheme();
        HttpServletRequest request = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
        String baseUrl = protocole + "://" + serverAdress + ":" + request.getLocalPort() + contextPath;

        List<SKOSResource> concepts = new ArrayList<>();

        if (!viewExportBean.isToogleFilterByGroup()) {
            exportRdf4jHelperNew.exportCollections(connect.getPoolConnexion(), idTheso);
            concepts = new ExportHelper().getAllConcepts(connect.getPoolConnexion(), idTheso,
                    baseUrl, null, nodePreference.getOriginalUri(), nodePreference);
        } else {
            for (String idGroup : viewExportBean.getSelectedIdGroups()) {
                NodeGroupLabel nodeGroupLabel = new GroupHelper().getNodeGroupLabel(connect.getPoolConnexion(), idGroup, idTheso);
                SKOSResource sKOSResource = new SKOSResource(
                        exportRdf4jHelperNew.getUriFromGroup(nodeGroupLabel), SKOSProperty.ConceptGroup);
                sKOSResource.addRelation(nodeGroupLabel.getIdGroup(),
                        exportRdf4jHelperNew.getUriFromGroup(nodeGroupLabel), SKOSProperty.microThesaurusOf);
                exportRdf4jHelperNew.addChildsGroupRecursive(connect.getPoolConnexion(), idTheso, idGroup, sKOSResource);

                concepts.addAll(new ExportHelper().getAllConcepts(connect.getPoolConnexion(),
                        idTheso, baseUrl, idGroup, nodePreference.getOriginalUri(), nodePreference));
            }
        }

        List<SKOSResource> facettes = new ExportHelper().getAllFacettes(connect.getPoolConnexion(), idTheso, baseUrl,
                nodePreference.getOriginalUri(), nodePreference);
        for (SKOSResource facette : facettes) {
            exportRdf4jHelperNew.getSkosXmlDocument().addFacet(facette);
        }

        for (SKOSResource concept : concepts) {
            exportRdf4jHelperNew.getSkosXmlDocument().addconcept(concept);
        }

        exportRdf4jHelperNew.exportFacettes(connect.getPoolConnexion(), idTheso);

        return exportRdf4jHelperNew.getSkosXmlDocument();
    }

    private boolean exportThesorusToVirtuoso(SKOSXmlDocument skosxd, String nomGraphe, String url, String login, String password) {

        VirtGraph virtGraph = null;
        try {
            virtGraph = new VirtGraph(nomGraphe, "jdbc:virtuoso://" + url, login, password);

            VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create("CLEAR GRAPH <" + nomGraphe + ">", virtGraph);
            vur.exec();

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Rio.write(new WriteRdf4j(skosxd).getModel(), out, RDFFormat.RDFXML);
                
                Model model = ModelFactory.createDefaultModel();
                model.read(new ByteArrayInputStream(out.toByteArray()), null);
                StmtIterator iter = model.listStatements();
                while (iter.hasNext()) {
                    Statement stmt = iter.nextStatement();
                    Resource subject = stmt.getSubject();
                    Property predicate = stmt.getPredicate();
                    RDFNode object = stmt.getObject();
                    Triple tri = new Triple(subject.asNode(), predicate.asNode(), object.asNode());
                    virtGraph.add(tri);
                }
            }
            virtGraph.close();
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            return true;
        } catch (IOException | NoSuchElementException | RDFHandlerException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                    "Problème de communication avec le serveur Virtuoso !"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
            if (virtGraph != null) {
                virtGraph.close();
            }
            PrimeFaces.current().executeScript("PF('waitDialog').hide();");
            return false;
        }
    }

    private SKOSXmlDocument getThesorusDatas(String idTheso, List<String> selectedGroups) {

        NodePreference nodePreference = new PreferencesHelper().getThesaurusPreferences(connect.getPoolConnexion(), idTheso);

        if (nodePreference == null) {
            return null;
        }

        ConceptHelper conceptHelper = new ConceptHelper();
        /// permet de filtrer par collection
        ArrayList<String> allConcepts = new ArrayList<>();
        if (!viewExportBean.isToogleFilterByGroup()) {
            allConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), idTheso);
      /*      allConcepts = conceptHelper.getAllIdConceptOfThesaurusByUser(connect.getPoolConnexion(), idTheso);
            ArrayList<String> allConcepts2 = conceptHelper.getAllIdConceptOfThesaurusByUser2(connect.getPoolConnexion(), idTheso);
            for (String id : allConcepts2) {
                if(!allConcepts.contains(id))
                    allConcepts.add(id);
            }*/
            
        } else {
            for (String idGroup : selectedGroups) {
                ArrayList<String> allConceptsTemp;
                allConceptsTemp = conceptHelper.getAllIdConceptOfThesaurusByGroup(connect.getPoolConnexion(), idTheso, idGroup);
                allConcepts.addAll(allConceptsTemp);
            }
        }
        if (allConcepts == null || allConcepts.isEmpty()) {
            return null;
        }

        sizeOfTheso = allConcepts.size();
        progressStep = (float) 100 / sizeOfTheso;

        ExportRdf4jHelperNew exportRdf4jHelperNew = new ExportRdf4jHelperNew();
        exportRdf4jHelperNew.setInfos(nodePreference, DATE_FORMAT, false, false);
        exportRdf4jHelperNew.exportTheso(connect.getPoolConnexion(), idTheso, nodePreference);

        if (!viewExportBean.isToogleFilterByGroup()) {
            exportRdf4jHelperNew.exportCollections(connect.getPoolConnexion(), idTheso);
        } else {
            exportRdf4jHelperNew.exportSelectedCollections(connect.getPoolConnexion(), idTheso, selectedGroups);
        }

        exportRdf4jHelperNew.exportFacettes(connect.getPoolConnexion(), idTheso);

        for (String idConcept : allConcepts) {
            progressBar += progressStep;
            exportRdf4jHelperNew.exportConcept(connect.getPoolConnexion(), idTheso, idConcept, false);
        }

        viewExportBean.setExportDone(true);

        return exportRdf4jHelperNew.getSkosXmlDocument();
    }

    public float getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(float progressBar) {
        this.progressBar = progressBar;
    }

    public float getProgressStep() {
        return progressStep;
    }

    public void setProgressStep(float progressStep) {
        this.progressStep = progressStep;
    }

    public void initProgressBar() {
        progressBar = 0;
        progressStep = 0;
    }

    /**
     * Cette fonction permet d'exporter un concept en SKOS en temps réel dans la
     * page principale
     *
     * @param idConcept
     * @param idTheso
     * @param type
     * @return
     */
    public StreamedContent conceptToFile(String idConcept, String idTheso, String type) {

        RDFFormat format = null;
        String extention = "";

        switch (type.toLowerCase()) {
            case "skos":
                format = RDFFormat.RDFXML;
                extention = ".rdf";
                break;
            case "jsonld":
                format = RDFFormat.JSONLD;
                extention = ".json";
                break;
            case "turtle":
                format = RDFFormat.TURTLE;
                extention = ".ttl";
                break;
            case "json":
                format = RDFFormat.RDFJSON;
                extention = ".json";
                break;
        }

        if (roleOnThesoBean.getNodePreference() == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "preference", "Absence des préférences !");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        ExportRdf4jHelperNew exportRdf4jHelperNew = new ExportRdf4jHelperNew();
        exportRdf4jHelperNew.setInfos(roleOnThesoBean.getNodePreference(), DATE_FORMAT, false, false);
        exportRdf4jHelperNew.exportConcept(connect.getPoolConnexion(), idTheso, idConcept, false);

        WriteRdf4j writeRdf4j = new WriteRdf4j(exportRdf4jHelperNew.getSkosXmlDocument());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, format);
        StreamedContent file = DefaultStreamedContent.builder()
                .contentType("application/xml")
                .name(idTheso + "_" + idConcept + "_" + extention)
                .stream(() -> new ByteArrayInputStream(out.toByteArray()))
                .build();
        return file;
    }
}
