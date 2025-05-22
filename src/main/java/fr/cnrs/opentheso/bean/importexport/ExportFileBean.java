package fr.cnrs.opentheso.bean.importexport;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.models.nodes.NodeTree;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.group.NodeGroupLabel;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.models.candidats.CandidatDto;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewExportBean;
import fr.cnrs.opentheso.models.exports.UriHelper;
import fr.cnrs.opentheso.services.ExportService;
import fr.cnrs.opentheso.services.FacetService;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.exports.csv.CsvWriteHelper;
import fr.cnrs.opentheso.services.exports.pdf.PdfExportType;
import fr.cnrs.opentheso.services.exports.pdf.WritePdfNewGen;
import fr.cnrs.opentheso.services.exports.rdf4j.ExportRdf4jHelperNew;
import fr.cnrs.opentheso.services.exports.rdf4j.WriteRdf4j;
import fr.cnrs.opentheso.models.skosapi.SKOSProperty;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;

import org.primefaces.model.StreamedContent;


@Named(value = "exportFileBean")
@SessionScoped
public class ExportFileBean implements Serializable {

    @Autowired @Lazy
    private RoleOnThesoBean roleOnThesoBean;
    
    @Autowired @Lazy
    private ViewExportBean viewExportBean;
    @Autowired @Lazy
    private CandidatBean candidatBean;
    @Autowired @Lazy
    private SelectedTheso selectedTheso;

    @Autowired @Lazy
    private WritePdfNewGen writePdfNewGen;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ExportRdf4jHelperNew exportRdf4jHelperNew;

    @Autowired
    private UriHelper uriHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private CsvWriteHelper csvWriteHelper;

    @Autowired
    private ExportService exportService;

    @Autowired
    private PreferenceService preferenceService;

    @Autowired
    private Tree tree;

    // progressBar
    private int sizeOfTheso;
    private float progressBar, progressStep;

    int posJ = 0;
    int posX = 0;
    @Autowired
    private FacetService facetService;

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

        var skosXmlDocument = getCandidatsDatas(true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(new WriteRdf4j(skosXmlDocument).getModel(), out, format);

        return DefaultStreamedContent.builder()
                .contentType("application/xml")
                .name("candidats" + extention)
                .stream(() -> new ByteArrayInputStream(out.toByteArray()))
                .build();
    }

    private SKOSXmlDocument getCandidatsDatas(boolean isCandidatExport) {

        var skosXmlDocument = new SKOSXmlDocument();
        var nodePreference = preferenceService.getThesaurusPreferences(selectedTheso.getCurrentIdTheso());

        if (nodePreference == null) {
            return skosXmlDocument;
        }

        candidatBean.setProgressBarStep(100 / candidatBean.getCandidatList().size());

        skosXmlDocument.setConceptScheme(exportRdf4jHelperNew.exportThesoV2(selectedTheso.getCurrentIdTheso(), nodePreference));

        for (CandidatDto candidat : candidatBean.getCandidatList()) {
            candidatBean.setProgressBarValue(candidatBean.getProgressBarValue() + candidatBean.getProgressBarStep());
            skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(selectedTheso.getCurrentIdTheso(), candidat.getIdConcepte(), isCandidatExport));
        }

        return skosXmlDocument;
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
                    if (posX < tab.length-1) posX++;
                    if (posJ > tab.length - 1) posJ--;
                }
            }
            posJ--;
        }
    }

    private List<NodeTree> parcourirArbre(String thesoId, String langId, String parentId) {

        List<NodeTree> concepts = conceptHelper.getListChildrenOfConceptWithTerm(parentId, langId, thesoId);
        for (NodeTree concept : concepts) {
            sizeOfTheso++;
            concept.setIdParent(parentId);
            concept.setPreferredTerm(StringUtils.isEmpty(concept.getPreferredTerm()) ? "(" + concept.getIdConcept() + ")" : concept.getPreferredTerm());
            concept.setChildrens(parcourirArbre(thesoId, langId, concept.getIdConcept()));

            List<NodeTree> facettes = tree.searchFacettesForTree(parentId, thesoId, langId);
            if (CollectionUtils.isNotEmpty(facettes)) {
                sizeOfTheso += facettes.size();
                concept.getChildrens().addAll(facettes);
            }
        }

        return concepts;
    }

    /**
     * Fonction dépréciée 
     * @return 
     */
    public StreamedContent exportThesorus() {
        // permet d'initialiser les paramètres pour contruire les Uris
        var nodePreference = preferenceService.getThesaurusPreferences(viewExportBean.getNodeIdValueOfTheso().getId());
        if (nodePreference == null) {
            return null;
        }

        uriHelper.setIdTheso(viewExportBean.getNodeIdValueOfTheso().getId());
        uriHelper.setNodePreference(nodePreference);

        
        /// export des concepts dépréciés
        if ("deprecated".equalsIgnoreCase(viewExportBean.getFormat())) {
            byte[] datas;
            if (viewExportBean.isToogleFilterByGroup()) {
                datas = csvWriteHelper.writeCsvByDeprecated(
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(),
                        viewExportBean.getCsvDelimiterChar());
            } else {
                datas = csvWriteHelper.writeCsvByDeprecated(
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(),
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
            List<NodeTree> topConcepts = conceptHelper.getTopConceptsWithTermByTheso(
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

            String[][] tab = new String[sizeOfTheso+20][20+20];
            posX = 0;
            for (NodeTree topConcept : topConcepts) {
                posJ = 0;
                createMatrice(tab, topConcept);
            }

            byte[] str = csvWriteHelper.importTreeCsv(tab, ';');

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
            byte[] datas;
            if (viewExportBean.isToogleFilterByGroup()) {
                datas = csvWriteHelper.writeCsvById(
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), viewExportBean.getSelectedIdGroups(), viewExportBean.getCsvDelimiterChar());
            } else {
                datas = csvWriteHelper.writeCsvById(
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
        SKOSXmlDocument skosxd = getThesorusDatas(viewExportBean.getNodeIdValueOfTheso().getId(), viewExportBean.getSelectedIdGroups());

        if (skosxd == null) {
            return null;
        }

        if ("PDF".equalsIgnoreCase(viewExportBean.getFormat())) {

            PdfExportType pdfExportType = PdfExportType.ALPHABETIQUE;
            if (viewExportBean.getTypes().indexOf(viewExportBean.getTypeSelected()) == 0) {
                pdfExportType = PdfExportType.HIERARCHIQUE;
            }

            try ( ByteArrayInputStream flux = new ByteArrayInputStream(writePdfNewGen.createPdfFile(skosxd,
                    viewExportBean.getSelectedLang1_PDF(), viewExportBean.getSelectedLang2_PDF(), pdfExportType, viewExportBean.isToogleExportImage()))) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                if (flux != null && flux.available() > 0) {
                    return DefaultStreamedContent
                            .builder()
                            .contentType("application/pdf")
                            .name(viewExportBean.getNodeIdValueOfTheso().getId() + ".pdf")
                            .stream(() -> flux)
                            .build();
                } else{
                    return new DefaultStreamedContent();
                }
            } catch (Exception ex) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return new DefaultStreamedContent();
            }

        } else if ("CSV".equalsIgnoreCase(viewExportBean.getFormat())) {

            byte[] str = csvWriteHelper.writeCsv(skosxd,
                        viewExportBean.getSelectedLanguages(),
                        viewExportBean.getCsvDelimiterChar());            
            
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
        // permet d'initialiser les paramètres pour contruire les Uris
        var nodePreference = preferenceService.getThesaurusPreferences(viewExportBean.getNodeIdValueOfTheso().getId());
        if (nodePreference == null) {
            return null;
        }
        if(StringUtils.isEmpty(nodePreference.getOriginalUri())){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                    "Veuillez paramétrer l'URI de l'export dans les préférences !"));
            return null;
        }
        if("null".equalsIgnoreCase(nodePreference.getOriginalUri())){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                    "Veuillez paramétrer l'URI de l'export dans les préférences !"));            
            return null;            
        }

        uriHelper.setIdTheso(viewExportBean.getNodeIdValueOfTheso().getId());
        uriHelper.setNodePreference(nodePreference);

        if ("deprecated".equalsIgnoreCase(viewExportBean.getFormat())) {

            byte[] datas;
            if (viewExportBean.isToogleFilterByGroup()) {
                datas = csvWriteHelper.writeCsvByDeprecated(
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(),
                        viewExportBean.getCsvDelimiterChar());
            } else {
                datas = csvWriteHelper.writeCsvByDeprecated(
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(),
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
            List<NodeTree> topConcepts = conceptHelper.getTopConceptsWithTermByTheso(
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

            byte[] str = csvWriteHelper.importTreeCsv(tab, ';');

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
            byte[] datas;
            if (viewExportBean.isToogleFilterByGroup()) {
                datas = csvWriteHelper.writeCsvById(
                        viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedIdLangTheso(), viewExportBean.getSelectedIdGroups(), viewExportBean.getCsvDelimiterChar());
            } else {
                datas = csvWriteHelper.writeCsvById(
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
        
        
        /// permet d'exporter chaque collection sous forme thésaurus indépendant
        if(viewExportBean.isToogleExportByGroup()){
            if ("CSV".equalsIgnoreCase(viewExportBean.getFormat())) {
                return exportEachGroupAsThesaurusCSV(viewExportBean.getNodeIdValueOfTheso().getId());
            } else {
                return exportEachGroupAsThesaurusSKOS(viewExportBean.getNodeIdValueOfTheso().getId());
            }
        }
        
        
        
        SKOSXmlDocument skosxd = getConcepts(viewExportBean.getNodeIdValueOfTheso().getId());
        if (skosxd == null) {
            return null;
        }

        if ("PDF".equalsIgnoreCase(viewExportBean.getFormat())) {
            PdfExportType pdfExportType = PdfExportType.ALPHABETIQUE;
            if (viewExportBean.getTypes().indexOf(viewExportBean.getTypeSelected()) == 0) {
                pdfExportType = PdfExportType.HIERARCHIQUE;
            }
            try ( ByteArrayInputStream flux = new ByteArrayInputStream(writePdfNewGen.createPdfFile(skosxd,
                    viewExportBean.getSelectedLang1_PDF(), viewExportBean.getSelectedLang2_PDF(),
                    pdfExportType, viewExportBean.isToogleExportImage()))) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");

                if (flux != null && flux.available() > 0) {
                    return DefaultStreamedContent
                            .builder()
                            .contentType("application/pdf")
                            .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + ".pdf")
                            .stream(() -> flux)
                            .build();
                } else {
                    return new DefaultStreamedContent();  // Flux vide ou invalide
                }

            } catch (Exception ex) {
                PrimeFaces.current().executeScript("PF('waitDialog').hide();");
                return new DefaultStreamedContent();
            }

        } else if ("CSV".equalsIgnoreCase(viewExportBean.getFormat())) {

            byte[] str = csvWriteHelper.writeCsv(skosxd, viewExportBean.getSelectedLanguages(), viewExportBean.getCsvDelimiterChar());

            try ( ByteArrayInputStream flux = new ByteArrayInputStream(str)) {
                
                return DefaultStreamedContent.builder().contentType("text/csv")
                        .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + ".csv")
                        .stream(() -> flux)
                        .build();
            } catch (Exception ex) {
                return new DefaultStreamedContent();
            }
        } else {

            switch (viewExportBean.getSelectedExportFormat().toLowerCase()) {
                case "rdf":
                    return generateRdfResources(skosxd, RDFFormat.RDFXML, ".rdf");
                case "jsonld":
                    return generateRdfResources(skosxd, RDFFormat.JSONLD, ".json");
                case "turtle":
                    return generateRdfResources(skosxd, RDFFormat.TURTLE, ".ttl");
                case "json":
                    return generateRdfResources(skosxd, RDFFormat.RDFJSON, ".json");
                default:
                    throw new Exception("Le format d'export n'est pas valide !");
            }
        }

    }
    
    /**
     * Export de chaque collection en thésaurus à part, le résultat en renvoyé en Zip
     * @param idTheso
     * @return
     * @throws IOException 
     */
    private StreamedContent exportEachGroupAsThesaurusCSV(String idTheso) throws IOException {
        if(viewExportBean.getGroupList() == null || viewExportBean.getGroupList().isEmpty()) return null;
        
        List<ByteArrayInputStream> byteArrayInputStreams = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        String extension = ".csv";
        String name;
        
        /// export des thésaurus au format byteArrayInputStream, un thésaurus par collection
        for (NodeGroup nodeGroup : viewExportBean.getGroupList()) {
            // récupérer le SKOSXmlDocument
            SKOSXmlDocument skosxd;
            try {
                skosxd = getThesoByGroup(idTheso, nodeGroup.getConceptGroup().getIdGroup());
            } catch (Exception ex) {
                Logger.getLogger(ExportFileBean.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            if(skosxd == null) continue;
            
            
            // exporter la collection en thésaurus
            byte[] str = csvWriteHelper.writeCsv(skosxd, viewExportBean.getSelectedLanguages(), viewExportBean.getCsvDelimiterChar());
            try ( ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str)) {
                name = viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + nodeGroup.getLexicalValue() + extension;
                int i = 1;
                while (fileNames.contains(name)) {
                    name = viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + nodeGroup.getLexicalValue() + "_" + i + extension;
                    i++;
                }                
                fileNames.add(name);
                byteArrayInputStreams.add(byteArrayInputStream);
            } catch (Exception ex) {
                return new DefaultStreamedContent();
            }   
        }        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {
            for (int i = 0; i < byteArrayInputStreams.size(); i++) {
                // Ajoutez chaque ByteArrayInputStream au zip avec un nom de fichier unique
                addToZip(zipOut, fileNames.get(i), byteArrayInputStreams.get(i));
            }
        }
        return DefaultStreamedContent.builder()
                .contentType("application/zip")
                .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + ".zip")
                .stream(() -> new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))
                .build();          
    }   

    
    /**
     * Export de chaque collection en thésaurus à part, le résultat en renvoyé en Zip
     * @param idTheso
     * @return
     * @throws IOException 
     */
    private StreamedContent exportEachGroupAsThesaurusSKOS(String idTheso) throws IOException {
        if(viewExportBean.getGroupList() == null || viewExportBean.getGroupList().isEmpty()) return null;
        
        List<ByteArrayInputStream> byteArrayInputStreams = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        String extension = ".rdf";
        String name;
        
        /// export des thésaurus au format byteArrayInputStream, un thésaurus par collection
        for (NodeGroup nodeGroup : viewExportBean.getGroupList()) {
            // récupérer le SKOSXmlDocument
            SKOSXmlDocument skosxd;
            try {
                skosxd = getThesoByGroup(idTheso, nodeGroup.getConceptGroup().getIdGroup());
            } catch (Exception ex) {
                Logger.getLogger(ExportFileBean.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            if(skosxd == null) continue;
            
            ByteArrayInputStream byteArrayInputStream = null;
            // exporter la collection en thésaurus
            switch (viewExportBean.getSelectedExportFormat().toLowerCase()) {
                case "rdf":
                    byteArrayInputStream = generateRdfResourcesByteArray(skosxd, RDFFormat.RDFXML);
                    extension = ".rdf";
                    break;
                case "jsonld":
                    byteArrayInputStream = generateRdfResourcesByteArray(skosxd, RDFFormat.JSONLD);
                    extension = ".json";
                    break;
                case "turtle":
                    byteArrayInputStream = generateRdfResourcesByteArray(skosxd, RDFFormat.TURTLE);
                    extension = ".ttl";
                    break;
                case "json":
                    byteArrayInputStream = generateRdfResourcesByteArray(skosxd, RDFFormat.RDFJSON);
                    extension = ".json";
                    break;
                default:
                    break;
            }
            name = viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + nodeGroup.getLexicalValue() + extension;
            int i = 1;
            while (fileNames.contains(name)) {
                name = viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + nodeGroup.getLexicalValue() + "_" + i + extension;
                i++;
            }
            fileNames.add(name);
            byteArrayInputStreams.add(byteArrayInputStream);
        }        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {
            for (int i = 0; i < byteArrayInputStreams.size(); i++) {
                // Ajoutez chaque ByteArrayInputStream au zip avec un nom de fichier unique
                addToZip(zipOut, fileNames.get(i), byteArrayInputStreams.get(i));
            }
        }
        return DefaultStreamedContent.builder()
                .contentType("application/zip")
                .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + ".zip")
                .stream(() -> new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))
                .build();          
    }

    private void addToZip(ZipOutputStream zipOut, String fileName, ByteArrayInputStream inputStream) throws IOException {
        ZipEntry entry = new ZipEntry(fileName);
        zipOut.putNextEntry(entry);

        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            zipOut.write(buffer, 0, len);
        }
        zipOut.closeEntry();
    }    
    
     private ByteArrayInputStream generateRdfResourcesByteArray(SKOSXmlDocument xmlDocument, RDFFormat format) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            WriteRdf4j writeRdf4j = new WriteRdf4j(xmlDocument);
            Rio.write(writeRdf4j.getModel(), out, format);
            writeRdf4j.closeCache();

            ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
            return input;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }     
  
 
    


   
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private SKOSXmlDocument getThesoByGroup(String idTheso, String idGroup) throws Exception {

        var nodePreference = preferenceService.getThesaurusPreferences(idTheso);

        if (nodePreference == null) {
            return null;
        }
        //controle si l'URL d'origine est vide
        if(StringUtils.isEmpty(nodePreference.getOriginalUri())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                    "Veuillez ajouter une URI valide dans les préférences du thésaurus !"));  
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.setConceptScheme(exportRdf4jHelperNew.exportThesoV2(idTheso, nodePreference));

        String contextPath = FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
        String serverAdress = FacesContext.getCurrentInstance().getExternalContext().getRequestServerName();
        String protocole = FacesContext.getCurrentInstance().getExternalContext().getRequestScheme();
        HttpServletRequest request = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
        String baseUrl = protocole + "://" + serverAdress + ":" + request.getLocalPort() + contextPath;

        List<SKOSResource> concepts = new ArrayList<>();

        NodeGroupLabel nodeGroupLabel = groupService.getNodeGroupLabel(idGroup, idTheso);
        SKOSResource sKOSResource = new SKOSResource(exportRdf4jHelperNew.getUriFromGroup(nodeGroupLabel), SKOSProperty.CONCEPT_GROUP);
        sKOSResource.addRelation(nodeGroupLabel.getIdGroup(), exportRdf4jHelperNew.getUriFromGroup(nodeGroupLabel), SKOSProperty.MICROTHESAURUS_OF);
        skosXmlDocument.addGroup(exportRdf4jHelperNew.exportThisCollectionV2(idTheso, idGroup));

        concepts.addAll(exportService.getAllConcepts(idTheso, baseUrl, idGroup, nodePreference.getOriginalUri(), nodePreference, viewExportBean.isToogleClearHtmlCharacter()));

        // export des facettes filtrées
        List<SKOSResource> facettes = exportService.getAllFacettes(idTheso, baseUrl,
                nodePreference.getOriginalUri(), nodePreference);
        List<String> groups = new ArrayList<>();
        groups.add(idGroup);
        
        for (SKOSResource facette : facettes) {
            if(facetService.isFacetInGroups(idTheso, facette.getIdentifier(),  groups)){
                skosXmlDocument.addFacet(facette);
            }
        }              

        for (SKOSResource concept : concepts) {
            skosXmlDocument.addconcept(concept);
        }

        return skosXmlDocument;
    }    
    

    private StreamedContent generateRdfResources(SKOSXmlDocument xmlDocument, RDFFormat format, String extension) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            WriteRdf4j writeRdf4j = new WriteRdf4j(xmlDocument);
            Rio.write(writeRdf4j.getModel(), out, format);
            writeRdf4j.closeCache();

            ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());

            return DefaultStreamedContent.builder()
                    .contentType("application/xml")
                    .name(viewExportBean.getNodeIdValueOfTheso().getValue() + "_" + viewExportBean.getNodeIdValueOfTheso().getId() + extension)
                    .stream(() -> input)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SKOSXmlDocument getConcepts(String idTheso) throws Exception {

        var nodePreference = preferenceService.getThesaurusPreferences(idTheso);

        if (nodePreference == null) {
            return null;
        }
        //controle si l'URL d'origine est vide
        if(StringUtils.isEmpty(nodePreference.getOriginalUri())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                    "Veuillez ajouter une URI valide dans les préférences du thésaurus !"));  
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.setConceptScheme(exportRdf4jHelperNew.exportThesoV2(idTheso, nodePreference));
        String baseUrl;
        if(StringUtils.isEmpty(nodePreference.getCheminSite())) {
            String contextPath = FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
            String serverAdress = FacesContext.getCurrentInstance().getExternalContext().getRequestServerName();
            String protocole = FacesContext.getCurrentInstance().getExternalContext().getRequestScheme();
            HttpServletRequest request = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
            baseUrl = protocole + "://" + serverAdress + ":" + request.getLocalPort() + contextPath;
        } else {
            baseUrl = nodePreference.getCheminSite();
        }
        List<SKOSResource> concepts = new ArrayList<>();

        // cas d'export pour tout le thésaurus avec les collections et facettes
        if (!viewExportBean.isToogleFilterByGroup()) {
            var collectionsList = exportRdf4jHelperNew.exportCollectionsV2(idTheso);
            for (SKOSResource group : collectionsList) {
                skosXmlDocument.addGroup(group);
            }

            concepts = exportService.getAllConcepts(idTheso, baseUrl, null,
                    nodePreference.getOriginalUri(), nodePreference, viewExportBean.isToogleClearHtmlCharacter());
            
            // export des facettes
            List<SKOSResource> facettes = exportService.getAllFacettes(idTheso, baseUrl,
                    nodePreference.getOriginalUri(), nodePreference);
            for (SKOSResource facette : facettes) {
                skosXmlDocument.addFacet(facette);
            }            
            
        } else {
            /// Export filtré par collection, on filtre également les Facettes qui sont dans les collections sélectionnées
            for (String idGroup : viewExportBean.getSelectedIdGroups()) {
                NodeGroupLabel nodeGroupLabel = groupService.getNodeGroupLabel(idGroup, idTheso);
                SKOSResource sKOSResource = new SKOSResource(exportRdf4jHelperNew.getUriFromGroup(nodeGroupLabel), SKOSProperty.CONCEPT_GROUP);
                sKOSResource.addRelation(nodeGroupLabel.getIdGroup(), exportRdf4jHelperNew.getUriFromGroup(nodeGroupLabel), SKOSProperty.MICROTHESAURUS_OF);
                skosXmlDocument.addGroup(exportRdf4jHelperNew.exportThisCollectionV2(idTheso, idGroup));

                concepts.addAll(exportService.getAllConcepts(idTheso, baseUrl, idGroup, nodePreference.getOriginalUri(), nodePreference, viewExportBean.isToogleClearHtmlCharacter()));
            }
            
            // export des facettes filtrées
            List<SKOSResource> facettes = exportService.getAllFacettes(idTheso, baseUrl,
                    nodePreference.getOriginalUri(), nodePreference);
            for (SKOSResource facette : facettes) {
                if(facetService.isFacetInGroups(idTheso, facette.getIdentifier(),  viewExportBean.getSelectedIdGroups())){
                    skosXmlDocument.addFacet(facette);
                }
            }              
            
        }

        for (SKOSResource concept : concepts) {
            skosXmlDocument.addconcept(concept);
        }

        return skosXmlDocument;
    }



    private SKOSXmlDocument getThesorusDatas(String idTheso, List<String> selectedGroups) {

        var nodePreference = preferenceService.getThesaurusPreferences(idTheso);

        if (nodePreference == null) {
            return null;
        }

        /// permet de filtrer par collection
        ArrayList<String> allConcepts = new ArrayList<>();
        if (!viewExportBean.isToogleFilterByGroup()) {
            allConcepts = conceptHelper.getAllIdConceptOfThesaurus(idTheso);
        } else {
            for (String idGroup : selectedGroups) {
                ArrayList<String> allConceptsTemp;
                allConceptsTemp = conceptHelper.getAllIdConceptOfThesaurusByGroup(idTheso, idGroup);
                allConcepts.addAll(allConceptsTemp);
            }
        }
        if (allConcepts == null || allConcepts.isEmpty()) {
            return null;
        }

        sizeOfTheso = allConcepts.size();
        progressStep = (float) 100 / sizeOfTheso;

        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.setConceptScheme(exportRdf4jHelperNew.exportThesoV2(idTheso, nodePreference));

        if (!viewExportBean.isToogleFilterByGroup()) {
            var collectionsList = exportRdf4jHelperNew.exportCollectionsV2(idTheso);
            for (SKOSResource group : collectionsList) {
                skosXmlDocument.addGroup(group);
            }
        } else {
            var collections = exportRdf4jHelperNew.exportSelectedCollectionsV2(idTheso, selectedGroups);
            for (SKOSResource collection : collections) {
                skosXmlDocument.addGroup(collection);
            }
        }

        var facettesList = exportRdf4jHelperNew.exportFacettesV2(idTheso);
        for (SKOSResource facette : facettesList) {
            skosXmlDocument.addFacet(facette);
        }

        for (String idConcept : allConcepts) {
            progressBar += progressStep;
            skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        }

        viewExportBean.setExportDone(true);

        return skosXmlDocument;
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
        if (StringUtils.isEmpty(roleOnThesoBean.getNodePreference().getCheminSite())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "preference", "Manque l'URL du site, veuillez paramétrer les préférences du thésaurus!");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }   
        if (StringUtils.isEmpty(roleOnThesoBean.getNodePreference().getOriginalUri())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "preference", "Manque l'URL du site, veuillez paramétrer les préférences du thésaurus!");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        exportRdf4jHelperNew.setInfos(roleOnThesoBean.getNodePreference());
        skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(idTheso, idConcept, false));
        
        WriteRdf4j writeRdf4j = new WriteRdf4j(skosXmlDocument);

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
