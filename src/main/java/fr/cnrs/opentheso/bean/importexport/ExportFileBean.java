package fr.cnrs.opentheso.bean.importexport;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewExportBean;
import fr.cnrs.opentheso.core.exports.csv.WriteCSV;
import fr.cnrs.opentheso.core.exports.pdf.WritePdf;
import fr.cnrs.opentheso.core.exports.rdf4j.ExportRdf4jHelper;
import fr.cnrs.opentheso.core.exports.rdf4j.ExportRdf4jHelperNew;
import fr.cnrs.opentheso.core.exports.rdf4j.WriteRdf4j;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.primefaces.model.DefaultStreamedContent;

import org.primefaces.model.StreamedContent;

/**
 *
 * @author miledrousset
 */
@Named(value = "exportFileBean")
@ViewScoped
public class ExportFileBean implements Serializable {

    @Inject
    private RoleOnThesoBean roleOnThesoBean;

    @Inject
    private Connect connect;

    @Inject
    private ViewExportBean viewExportBean;

    // progressBar
    private int sizeOfTheso;
    private float progressBar, progressStep;

    private ExportRdf4jHelper exportRdf4jHelper;
    private String codeLang, codeLang2;

    public StreamedContent exportThesorus() {

        initProgressBar();

        if ("PDF".equalsIgnoreCase(viewExportBean.getFormat())) {
            initDatas();
            WritePdf writePdf = new WritePdf(exportRdf4jHelper.getSkosXmlDocument(), codeLang, codeLang2, viewExportBean.getTypes()
                    .indexOf(viewExportBean.getTypeSelected()));
            return new DefaultStreamedContent(new ByteArrayInputStream(writePdf.getOutput().toByteArray()),
                    "application/pdf", viewExportBean.getNodeIdValueOfTheso().getId() + ".pdf");

        } else if ("CSV".equalsIgnoreCase(viewExportBean.getFormat())) {
            initDatas();
            WriteCSV writeCsv = new WriteCSV(exportRdf4jHelper.getSkosXmlDocument(), codeLang, codeLang2);
            return new DefaultStreamedContent(new ByteArrayInputStream(writeCsv.getOutput().toByteArray()),
                    "text/csv", viewExportBean.getNodeIdValueOfTheso().getId() + ".csv");
        } else {
            return thesoToRdf(viewExportBean.getNodeIdValueOfTheso().getId(), viewExportBean.getSelectedLanguages(),
                    viewExportBean.getSelectedGroups(), viewExportBean.getNodePreference(), viewExportBean.getSelectedExportFormat());
        }

    }

    private void initDatas() {
        exportRdf4jHelper = getThesorusDatas(viewExportBean.getNodeIdValueOfTheso().getId(),
                viewExportBean.getSelectedLanguages(), viewExportBean.getSelectedGroups());

        codeLang = viewExportBean.getSelectedLanguages().get(0).getCode();
        codeLang2 = viewExportBean.getSelectedLanguages().size() > 1 ? viewExportBean.getSelectedLanguages().get(1).getCode() : "";
    }

    private ExportRdf4jHelper getThesorusDatas(String idTheso, List<NodeLangTheso> selectedLanguages, List<NodeGroup> selectedGroups) {
        NodePreference nodePreference = new PreferencesHelper().getThesaurusPreferences(connect.getPoolConnexion(), idTheso);

        if (nodePreference == null) {
            return null;
        }

        sizeOfTheso = new ConceptHelper().getAllIdConceptOfThesaurus(connect.getPoolConnexion(), idTheso).size();
        progressStep = (float) 100 / sizeOfTheso;

        ExportRdf4jHelper exportRdf4jHelper = new ExportRdf4jHelper();
        exportRdf4jHelper.setInfos(connect.getPoolConnexion(), "dd-mm-yyyy", false, idTheso, nodePreference.getCheminSite());
        exportRdf4jHelper.setNodePreference(nodePreference);
        exportRdf4jHelper.addThesaurus(idTheso, selectedLanguages);
        exportRdf4jHelper.addGroup(idTheso, selectedLanguages, selectedGroups);
        exportRdf4jHelper.addConcept(idTheso, this, selectedLanguages);

        return exportRdf4jHelper;
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
        NodePreference nodePreference = roleOnThesoBean.getNodePreference();
        if (nodePreference == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "preference", "Absence des préférences !");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }
        ExportRdf4jHelper exportRdf4jHelper = new ExportRdf4jHelper();
        exportRdf4jHelper.setNodePreference(nodePreference);
        exportRdf4jHelper.setInfos(connect.getPoolConnexion(), "dd-mm-yyyy", false, idTheso, nodePreference.getCheminSite());
        exportRdf4jHelper.setNodePreference(nodePreference);
        exportRdf4jHelper.addSignleConcept(idTheso, idConcept);
        WriteRdf4j writeRdf4j = new WriteRdf4j(exportRdf4jHelper.getSkosXmlDocument());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, format);
        //    StreamedContent file = new ByteArrayContent(out.toByteArray(), "application/xml", idTheso + "_" + idConcept + "_" + extention);
        StreamedContent file = DefaultStreamedContent.builder()
                .contentType("application/xml")
                .name(idTheso + "_" + idConcept + "_" + extention)
                .stream(() -> new ByteArrayInputStream(out.toByteArray()))
                .build();
        return file;
    }

    /**
     * permet d'exporter un thésaurus complet au format RDF avec filtres : - des
     * langues choisies - des groupes choisis
     *
     * @param idTheso
     * @param selectedLanguages
     * @param selectedGroups
     * @param nodePreference
     * @param type
     * @return
     */
    public StreamedContent thesoToRdf(String idTheso, List<NodeLangTheso> selectedLanguages,
            List<NodeGroup> selectedGroups, NodePreference nodePreference, String type) {

        initProgressBar();

        RDFFormat format = null;
        String extention = "xml";
        StreamedContent file = null;

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

        //    WriteRdf4j writeRdf4j = loadExportHelper(idTheso, selectedLanguages, selectedGroups, nodePreference);
        WriteRdf4j writeRdf4j = loadExportHelper(idTheso, nodePreference);
        if (writeRdf4j != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rio.write(writeRdf4j.getModel(), out, format);
            //file = new ByteArrayContent(out.toByteArray(), "application/xml", idTheso + " " + extention);
            file = DefaultStreamedContent.builder().contentType("application/xml").name(idTheso + " " + extention)
                    .stream(() -> new ByteArrayInputStream(out.toByteArray())).build();
        }

        return file;
    }

    private WriteRdf4j loadExportHelper(String idTheso, NodePreference nodePreference) {

        if (nodePreference == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Info :", "!!! Veuillez ouvrir le thésaurus pour l'initialiser avant export !!!"));
            return null;
        }

        // pour savoir quel Uri exporter, une seule possible
        boolean useUriArk = false;
        boolean userUriHandle = false;

        ArrayList<String> allConcepts = new ConceptHelper().getAllIdConceptOfThesaurus(connect.getPoolConnexion(), idTheso);
        ArrayList<String> rootGroupList = new GroupHelper().getListIdOfRootGroup(connect.getPoolConnexion(), idTheso);
        progressStep = (float) 100 / (rootGroupList.size() + allConcepts.size()/4);

        ExportRdf4jHelperNew exportRdf4jHelperNew = new ExportRdf4jHelperNew();
        exportRdf4jHelperNew.setInfos(nodePreference, "dd-mm-yyyy", useUriArk, userUriHandle);
        exportRdf4jHelperNew.exportTheso(connect.getPoolConnexion(), idTheso);
        exportRdf4jHelperNew.exportCollections(connect.getPoolConnexion(), idTheso, this);
        
        for (String idConcept : allConcepts) {
            progressBar += progressStep;
            exportRdf4jHelperNew.exportConcept(connect.getPoolConnexion(), idTheso, idConcept);
        }
        
        return new WriteRdf4j(exportRdf4jHelperNew.getSkosXmlDocument());
    }

}
