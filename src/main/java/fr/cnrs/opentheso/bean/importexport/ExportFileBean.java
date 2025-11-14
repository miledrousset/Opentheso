package fr.cnrs.opentheso.bean.importexport;

import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.models.candidats.CandidatDto;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewExportBean;
import fr.cnrs.opentheso.models.exports.UriHelper;
import fr.cnrs.opentheso.services.exports.ExportService;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.exports.rdf4j.ExportRdf4jHelperNew;
import fr.cnrs.opentheso.services.exports.rdf4j.WriteRdf4j;
import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import fr.cnrs.opentheso.utils.MessageUtils;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


@SessionScoped
@RequiredArgsConstructor
@Named(value = "exportFileBean")
public class ExportFileBean implements Serializable {

    private final ViewExportBean viewExportBean;
    private final CandidatBean candidatBean;
    private final SelectedTheso selectedTheso;
    private final ExportRdf4jHelperNew exportRdf4jHelperNew;
    private final UriHelper uriHelper;
    private final ExportService exportService;
    private final PreferenceService preferenceService;
    private final ConceptView conceptView;


    public StreamedContent exportCandidatsEnSkos() {

        RDFFormat format = null;
        String extention = switch (candidatBean.getSelectedExportFormat().toLowerCase()) {
            case "skos" -> {
                format = RDFFormat.RDFXML;
                yield ".rdf";
            }
            case "jsonld" -> {
                format = RDFFormat.JSONLD;
                yield ".json";
            }
            case "turtle" -> {
                format = RDFFormat.TURTLE;
                yield ".ttl";
            }
            case "json" -> {
                format = RDFFormat.RDFJSON;
                yield ".json";
            }
            default -> "xml";
        };

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

        /// permet d'exporter chaque collection sous forme thésaurus indépendant
        if(viewExportBean.isToogleExportByGroup()){
            if ("CSV".equalsIgnoreCase(viewExportBean.getFormat())) {
                return exportService.exportEachGroupAsThesaurusCSV(viewExportBean.getNodeIdValueOfTheso(),
                        viewExportBean.isToogleClearHtmlCharacter(), viewExportBean.getGroupList(),
                        viewExportBean.getSelectedLanguages(), viewExportBean.getCsvDelimiterChar());
            } else {
                return exportService.exportEachGroupAsThesaurusSKOS(viewExportBean.getNodeIdValueOfTheso().getId(),
                        viewExportBean.getSelectedExportFormat(), viewExportBean.getGroupList(),
                        viewExportBean.isToogleClearHtmlCharacter(), viewExportBean.getNodeIdValueOfTheso());
            }
        }

        if ("deprecated".equalsIgnoreCase(viewExportBean.getFormat())) {

            return exportService.exportDeprecatedFormat(viewExportBean.getNodeIdValueOfTheso(),
                    viewExportBean.isToogleFilterByGroup(),
                    viewExportBean.getSelectedIdLangTheso(),
                    viewExportBean.getCsvDelimiterChar());
        } else if ("CSV_STRUC".equalsIgnoreCase(viewExportBean.getFormat())) {

            return exportService.exportCsvStrucFormat(viewExportBean.getNodeIdValueOfTheso(),
                    viewExportBean.getSelectedIdLangTheso());
        } else if ("CSV_id".equalsIgnoreCase(viewExportBean.getFormat())) {

            return exportService.exportCsvIdFormat(viewExportBean.getNodeIdValueOfTheso(),
                    viewExportBean.isToogleFilterByGroup(),
                    viewExportBean.getCsvDelimiterChar(),
                    viewExportBean.getSelectedIdLangTheso(),
                    viewExportBean.getSelectedIdGroups());
        } else if ("PDF".equalsIgnoreCase(viewExportBean.getFormat())) {

            return exportService.exportPdfFormat(viewExportBean.getNodeIdValueOfTheso(),
                    viewExportBean.getTypes(),
                    viewExportBean.getTypeSelected(),
                    viewExportBean.isToogleExportImage(),
                    viewExportBean.getSelectedLang1_PDF(),
                    viewExportBean.getSelectedLang2_PDF(),
                    viewExportBean.isToogleFilterByGroup(),
                    viewExportBean.isToogleClearHtmlCharacter(),
                    viewExportBean.getSelectedIdGroups());
        } else if ("CSV".equalsIgnoreCase(viewExportBean.getFormat())) {

            return exportService.exportCsvFormat(viewExportBean.getNodeIdValueOfTheso(),
                    viewExportBean.getSelectedLanguages(),
                    viewExportBean.getCsvDelimiterChar(),
                    viewExportBean.isToogleFilterByGroup(),
                    viewExportBean.isToogleClearHtmlCharacter(),
                    viewExportBean.getSelectedIdGroups());
        } else {
            return exportService.exportSkosFormat(viewExportBean.getSelectedExportFormat(),
                    viewExportBean.getNodeIdValueOfTheso(),
                    viewExportBean.isToogleFilterByGroup(),
                    viewExportBean.isToogleClearHtmlCharacter(),
                    viewExportBean.getSelectedIdGroups());
        }
    }

    /**
     * Cette fonction permet d'exporter un concept en SKOS en temps réel dans la
     * page principale
     * @param type
     * @return
     */
    public StreamedContent conceptToFile(String type) {

        RDFFormat format = null;
        String extention = switch (type.toLowerCase()) {
            case "skos" -> {
                format = RDFFormat.RDFXML;
                yield ".rdf";
            }
            case "jsonld" -> {
                format = RDFFormat.JSONLD;
                yield ".json";
            }
            case "turtle" -> {
                format = RDFFormat.TURTLE;
                yield ".ttl";
            }
            case "json" -> {
                format = RDFFormat.RDFJSON;
                yield ".json";
            }
            default -> "";
        };

        var preferences = preferenceService.getThesaurusPreferences(conceptView.getNodeConcept().getConcept().getIdThesaurus());
        if (preferences == null) {
            MessageUtils.showErrorMessage("Absence des préférences !");
            return null;
        }
        if (StringUtils.isEmpty(preferences.getCheminSite())) {
            MessageUtils.showErrorMessage("Manque l'URL du site, veuillez paramétrer les préférences du thésaurus!");
            return null;
        }   
        if (StringUtils.isEmpty(preferences.getOriginalUri())) {
            MessageUtils.showErrorMessage("Manque l'URL du site, veuillez paramétrer les préférences du thésaurus!");
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        exportRdf4jHelperNew.setInfos(preferences);
        skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(conceptView.getNodeConcept().getConcept().getIdThesaurus(),
                conceptView.getNodeConcept().getConcept().getIdConcept(), false));
        
        WriteRdf4j writeRdf4j = new WriteRdf4j(skosXmlDocument);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, format);
        return DefaultStreamedContent.builder()
                .contentType("application/xml")
                .name(conceptView.getNodeConcept().getConcept().getIdThesaurus()
                        + "_" + conceptView.getNodeConcept().getConcept().getIdConcept() + "_" + extention)
                .stream(() -> new ByteArrayInputStream(out.toByteArray()))
                .build();
    }
}
