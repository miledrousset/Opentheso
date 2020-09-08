/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;

import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.core.exports.rdf4j.ExportRdf4jHelper;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import org.eclipse.rdf4j.rio.RDFFormat;

/**
 *
 * @author miled.rousset
 */
public class CopyAndPasteBetweenThesoHelper {

    public CopyAndPasteBetweenThesoHelper() {
    }
    
    public boolean pasteConceptLikeNT(
            String currentIdTheso,
            String currentIdConcept,
            String idConceptToMove) {

        return true;
    }
    
    public boolean pasteConceptLikeNTOfGroup(
            String currentIdTheso,
            String currentIdGroup,
            String idConceptToMove) {

        return true;
    }    
    
    public boolean pasteBranchLikeNT(
            HikariDataSource ds,
            String currentIdTheso,
            String currentIdConcept,
            String fromIdTheso,
            String fromIdConcept,
            String identifierType) {
        
        // récupération des concepts du thésaurus de départ
        SKOSXmlDocument sKOSXmlDocument = getBranch(ds,
                fromIdTheso, fromIdConcept);
        
        if(sKOSXmlDocument == null) return false;
        
        // import des concepts dans le thésaurus actuel
        
  /*      if(!addBranch(ds, sKOSXmlDocument, selectedTerme, roleOnThesoBean, identifierType)) {
            return false;
        }
*/
        return true;
    }
    
    private SKOSXmlDocument getBranch(
            HikariDataSource ds,
            String fromIdTheso,
            String fromIdConcept) {
        RDFFormat format = RDFFormat.RDFXML;

        NodePreference nodePreference =  new PreferencesHelper().getThesaurusPreferences(ds, fromIdTheso);
        if(nodePreference == null) return null;
        
        ExportRdf4jHelper exportRdf4jHelper = new ExportRdf4jHelper();
        exportRdf4jHelper.setInfos(ds, "dd-mm-yyyy", false, fromIdTheso, nodePreference.getCheminSite());
        exportRdf4jHelper.setNodePreference(nodePreference);
        
        exportRdf4jHelper.addBranch(fromIdTheso, fromIdConcept);
        
        return exportRdf4jHelper.getSkosXmlDocument();
        
/*        WriteRdf4j writeRdf4j = new WriteRdf4j(exportRdf4jHelper.getSkosXmlDocument());
        
        ByteArrayOutputStream out;
        out = new ByteArrayOutputStream();
        Rio.write(writeRdf4j.getModel(), out, format);
        return out.toString();*/
    }
    
/*    private boolean addBranch(HikariDataSource ds,
            SKOSXmlDocument sKOSXmlDocument,
            SelectedTerme selectedTerme, RoleOnThesoBean roleOnTheso,
            String identifierType) {
        int idGroup = -1; 
        String formatDate = "yyyy-MM-dd";
    
        try {
            ImportRdf4jHelper importRdf4jHelper = new ImportRdf4jHelper();
            importRdf4jHelper.setInfos(ds, formatDate,
                    roleOnTheso.getUser().getUser().getIdUser(),
                    idGroup, 
                    selectedTerme.getIdlangue());//connect.getWorkLanguage());
            // pour récupérer les identifiants pérennes type Ark ou Handle
            importRdf4jHelper.setIdentifierType(identifierType);
            
            importRdf4jHelper.setPrefixHandle("");
            importRdf4jHelper.setNodePreference(roleOnTheso.getNodePreference());
            
            importRdf4jHelper.setRdf4jThesaurus(sKOSXmlDocument);
            try {
                importRdf4jHelper.addBranch(selectedTerme);
                return true;
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        } finally {

        }
        return false;
    }    */
    
      
    
    
}
