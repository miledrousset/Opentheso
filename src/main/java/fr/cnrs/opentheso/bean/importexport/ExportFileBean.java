/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.importexport;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.core.exports.rdf4j.ExportRdf4jHelper;
import fr.cnrs.opentheso.core.exports.rdf4j.ExportRdf4jHelperNew;
import fr.cnrs.opentheso.core.exports.rdf4j.WriteRdf4j;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.primefaces.model.DefaultStreamedContent;

import org.primefaces.model.StreamedContent;

@Named(value = "exportFileBean")
@SessionScoped

/**
 *
 * @author miledrousset
 */
public class ExportFileBean implements Serializable{

    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    @Inject
    private Connect connect;
    
    
    // progressBar

    private double sizeOfTheso;
    private int progressBar = 0;
    private int progressStep = 0;
    
    private String messages;
    
    public ExportFileBean() {
    }
    
    public void init(){
        progressBar = 0;
        progressStep = 0;
    }
    
    public void onComplete(){
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Resultat !", messages);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    
    public double getSizeOfTheso() {
        return sizeOfTheso; 

        
    }    

    public void setSizeOfTheso(double sizeOfTheso) {
        this.sizeOfTheso = sizeOfTheso;
    }

    public int getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(int progressBar) {
        this.progressBar = progressBar;
    }

    public int getProgressStep() {
        return progressStep;
    }

    public void setProgressStep(int progressStep) {
        this.progressStep = progressStep;
    }

    public void cancel() {
        progressBar = 0;
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
        if(nodePreference == null) {
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

        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, format);

        //    StreamedContent file = new ByteArrayContent(out.toByteArray(), "application/xml", idTheso + "_" + idConcept + "_" + extention);
        StreamedContent file = DefaultStreamedContent.builder()
                .contentType("application/xml")
                .name(idTheso + "_" + idConcept + "_" + extention)
                .stream(()->new ByteArrayInputStream(out.toByteArray()))
                .build();
        return file;
    }
    
    /**
     * permet d'exporter un thésaurus complet au format RDF avec filtres :
     * - des langues choisies
     * - des groupes choisis
     * 
     * @param idTheso
     * @param selectedLanguages
     * @param selectedGroups
     * @param nodePreference
     * @param type
     * @return 
     */
    public StreamedContent thesoToRdf(String idTheso,
            List<NodeLangTheso> selectedLanguages,
            List<NodeGroup> selectedGroups,
            NodePreference nodePreference,
            String type) {

        init();
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
        if(writeRdf4j != null) {
            ByteArrayOutputStream out;
            out = new ByteArrayOutputStream();
            Rio.write(writeRdf4j.getModel(), out, format);
      //      file = new ByteArrayContent(out.toByteArray(), "application/xml", idTheso + " " + extention);
            file = DefaultStreamedContent.builder()
                .contentType("application/xml")
                .name(idTheso + " " + extention)
                .stream(()->new ByteArrayInputStream(out.toByteArray()))
                .build();
        }
        
        return file;
    }    
    
    private WriteRdf4j loadExportHelper(String idTheso,
            NodePreference nodePreference) {
        progressStep = 0;
        if(nodePreference == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Info :", "!!! Veuillez ouvrir le thésaurus pour l'initialiser avant export !!!"));
            return null;
        }
        
        // pour savoir quel Uri exporter, une seule possible
        boolean useUriArk = false;
        boolean userUriHandle = false;
        
        ConceptHelper conceptHelper = new ConceptHelper();
        sizeOfTheso = conceptHelper.getConceptCountOfThesaurus(connect.getPoolConnexion(), idTheso);

        ExportRdf4jHelperNew exportRdf4jHelperNew = new ExportRdf4jHelperNew();
        exportRdf4jHelperNew.setInfos(
                nodePreference,
                "dd-mm-yyyy",
                useUriArk,
                userUriHandle);
        exportRdf4jHelperNew.exportTheso(connect.getPoolConnexion(), idTheso);
        exportRdf4jHelperNew.exportCollections(connect.getPoolConnexion(), idTheso);
    
        ArrayList<String> allConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), idTheso);
        for (String idConcept : allConcepts) {
            exportRdf4jHelperNew.exportConcept(
                    connect.getPoolConnexion(),
                    idTheso,
                    idConcept);
            progressStep = progressStep + 1;
            progressBar = (int)(progressStep/sizeOfTheso) * 100; 
            if (progressBar > 100) {
                progressBar = 100;
            }
            setProgressBar(progressBar);
        }
        //pour afficher les messages dans primefaces
        exportRdf4jHelperNew.getMessages();
        
        WriteRdf4j writeRdf4j = new WriteRdf4j(exportRdf4jHelperNew.getSkosXmlDocument());
        return writeRdf4j;
    }
  
   
}
