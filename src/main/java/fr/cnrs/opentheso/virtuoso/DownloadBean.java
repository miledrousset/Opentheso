package fr.cnrs.opentheso.virtuoso;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.core.exports.rdf4j.ExportRdf4jHelper;
import fr.cnrs.opentheso.core.exports.rdf4j.WriteRdf4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import java.io.*;
import java.util.List;


@ManagedBean(name = "downloadBean", eager = true)
@SessionScoped
public class DownloadBean implements Serializable {

    private final Log LOG = LogFactory.getLog(DownloadBean.class);

    @Inject
    private LanguageBean languageBean;

    @Inject
    private RoleOnThesoBean roleOnTheso;

    private HikariDataSource hikariDataSource;


    public StreamedContent thesoToFile(String idTheso, List<NodeLangTheso> selectedLanguages,
            List<NodeGroup> selectedGroups, int type) {

        RDFFormat format = null;
        String extention = "";
        switch (type) {
            case 0:
                format = RDFFormat.RDFXML;
                extention = "_skos.xml";
                break;
            case 1:
                format = RDFFormat.JSONLD;
                extention = "_json-ld.json";
                break;
            case 2:
                format = RDFFormat.TURTLE;
                extention = "_turtle.ttl";
                break;
            case 3:
                format = RDFFormat.RDFJSON;
                extention = "_json.json";
                break;                
        }

        WriteRdf4j writeRdf4j = loadExportHelper(idTheso, selectedLanguages, selectedGroups);
        if(writeRdf4j != null) {
            ByteArrayOutputStream out;
            out = new ByteArrayOutputStream();
            Rio.write(writeRdf4j.getModel(), out, format);

            try (ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray())) {
                return DefaultStreamedContent.builder()
                        .contentType("application/xml")
                        .name(idTheso + extention)
                        .stream(() -> input)
                        .build();
            } catch (IOException e) {
                e.printStackTrace();
                return new DefaultStreamedContent();
            }
        }
        return new DefaultStreamedContent();
    }


    private WriteRdf4j loadExportHelper(String idTheso, List<NodeLangTheso> selectedLanguages, List<NodeGroup> selectedGroups) {

        NodePreference nodePreference = new PreferencesHelper().getThesaurusPreferences(hikariDataSource, idTheso);
        if(nodePreference == null) {
            LOG.error("Thésaurus est manquant !");
            return null;
        }

        ExportRdf4jHelper exportRdf4jHelper = new ExportRdf4jHelper();
        exportRdf4jHelper.setNodePreference(nodePreference);
        exportRdf4jHelper.setInfos(hikariDataSource, "dd-mm-yyyy", false, idTheso,
                nodePreference.getCheminSite());
        exportRdf4jHelper.addThesaurus(idTheso, selectedLanguages);
        exportRdf4jHelper.addGroup(idTheso, selectedLanguages, selectedGroups);
        exportRdf4jHelper.addConcept(idTheso, this, selectedLanguages);
        WriteRdf4j writeRdf4j = new WriteRdf4j(exportRdf4jHelper.getSkosXmlDocument());
        return writeRdf4j;
    }

    public LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    public RoleOnThesoBean getRoleOnTheso() {
        return roleOnTheso;
    }

    public void setRoleOnTheso(RoleOnThesoBean roleOnTheso) {
        this.roleOnTheso = roleOnTheso;
    }

    public void setConnect(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }
}
