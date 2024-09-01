/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;

import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bean.concept.SynonymBean;
import fr.cnrs.opentheso.models.exports.rdf4j.ExportRdf4jHelperNew;
import fr.cnrs.opentheso.models.imports.rdf4j.helper.ImportRdf4jHelper;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author miled.rousset
 */
public class CopyAndPasteBetweenThesoHelper {

    public boolean pasteConceptLikeNT(String currentIdTheso, String currentIdConcept, String idConceptToMove) {
        return true;
    }

    public boolean pasteConceptLikeNTOfGroup(String currentIdTheso, String currentIdGroup,
            String idConceptToMove) {
        return true;
    }

    public boolean pasteBranchLikeNT(HikariDataSource ds, String currentIdTheso, String currentIdConcept,
            String fromIdTheso, String fromIdConcept, String identifierType, int idUser,
            NodePreference nodePreference) {

        // récupération des concepts du thésaurus de départ
        SKOSXmlDocument sKOSXmlDocument = getBranch(ds,
                fromIdTheso, fromIdConcept);

        if (sKOSXmlDocument == null) {
            return false;
        }

        // import des concepts dans le thésaurus actuel
        if (!addBranch(ds, sKOSXmlDocument, nodePreference, currentIdTheso, idUser, identifierType)) {
            return false;
        }

        // relier le concept copié au concept cible
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<String> nodeBT = relationsHelper.getListIdBT(ds, fromIdConcept, currentIdTheso);
        for (String idBT : nodeBT) {
            if (!relationsHelper.deleteRelationBT(ds,
                    fromIdConcept,
                    currentIdTheso,
                    idBT,
                    idUser)) {
            }
        }
        try (Connection conn = ds.getConnection()){
            conn.setAutoCommit(false);
            if (!relationsHelper.addRelationBT(
                    conn,
                    fromIdConcept,
                    currentIdTheso,
                    currentIdConcept, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(SynonymBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    
    public boolean pasteBranchToRoot(HikariDataSource ds, String currentIdTheso, String fromIdTheso,
            String fromIdConcept, String identifierType, int idUser, NodePreference nodePreference) {

        // récupération des concepts du thésaurus de départ
        SKOSXmlDocument sKOSXmlDocument = getBranch(ds,
                fromIdTheso, fromIdConcept);

        if (sKOSXmlDocument == null) {
            return false;
        }

        // import des concepts dans le thésaurus actuel
        if (!addBranch(ds, sKOSXmlDocument, nodePreference, currentIdTheso, idUser, identifierType)) {
            return false;
        }

        // nettoyer le concept copié 
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<String> nodeBT = relationsHelper.getListIdBT(ds, fromIdConcept, currentIdTheso);
        for (String idBT : nodeBT) {
            if (!relationsHelper.deleteRelationBT(ds, fromIdConcept, currentIdTheso, idBT, idUser)) {
                
            }
        }
        
        // Passer la branche en TopTerm
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.setTopConcept(ds, fromIdConcept, currentIdTheso);
        return true;
    }

    private SKOSXmlDocument getBranch(
            HikariDataSource ds,
            String fromIdTheso,
            String fromIdConcept) {
    //    RDFFormat format = RDFFormat.RDFXML;

        NodePreference nodePreference = new PreferencesHelper().getThesaurusPreferences(ds, fromIdTheso);
        if (nodePreference == null) {
            return null;
        }
        String DATE_FORMAT = "dd-mm-yyyy";
        
        ExportRdf4jHelperNew exportRdf4jHelperNew = new ExportRdf4jHelperNew();
        exportRdf4jHelperNew.setInfos(nodePreference);
        exportRdf4jHelperNew.exportTheso(ds, fromIdTheso, nodePreference);        
        
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> allConcepts = conceptHelper.getIdsOfBranch(ds, fromIdConcept, fromIdTheso);
        
        for (String idConcept : allConcepts) {
            exportRdf4jHelperNew.exportConcept(ds, fromIdTheso, idConcept, false);
        }        
        return exportRdf4jHelperNew.getSkosXmlDocument();
        /*
        nodePreference.setOriginalUriIsArk(false);
        nodePreference.setOriginalUriIsHandle(false);   
        nodePreference.setOriginalUriIsDoi(false);

        ExportRdf4jHelper exportRdf4jHelper = new ExportRdf4jHelper();
        exportRdf4jHelper.setInfos(ds, "dd-mm-yyyy", false, fromIdTheso, nodePreference.getCheminSite());
        exportRdf4jHelper.setNodePreference(nodePreference);

        
        
        
        
        exportRdf4jHelper.addBranch(fromIdTheso, fromIdConcept);

        return exportRdf4jHelper.getSkosXmlDocument();*/
    }

    private boolean addBranch(HikariDataSource ds, SKOSXmlDocument sKOSXmlDocument, NodePreference nodePreference,
            String idTheso, int idUser, String identifierType) {
        int idGroup = -1;
        String formatDate = "yyyy-MM-dd";

        ImportRdf4jHelper importRdf4jHelper = new ImportRdf4jHelper();
        importRdf4jHelper.setInfos(ds, formatDate, idUser, idGroup, "");//connect.getWorkLanguage());
        // pour récupérer les identifiants pérennes type Ark ou Handle
        importRdf4jHelper.setSelectedIdentifier(identifierType);

        importRdf4jHelper.setPrefixHandle("");
        importRdf4jHelper.setNodePreference(nodePreference);

        importRdf4jHelper.setRdf4jThesaurus(sKOSXmlDocument);
        try {
            for (SKOSResource sKOSResource : sKOSXmlDocument.getConceptList()) {
                importRdf4jHelper.addConceptV2(sKOSResource, idTheso);
            }
            return true;
        } catch (Exception e) {
        }
        return false;
    }

}
