package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.bean.concept.SynonymBean;
import fr.cnrs.opentheso.services.exports.rdf4j.ExportRdf4jHelperNew;
import fr.cnrs.opentheso.services.imports.rdf4j.ImportRdf4jHelper;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author miled.rousset
 */
@Service
public class CopyAndPasteBetweenThesoHelper {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ImportRdf4jHelper importRdf4jHelper;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private ExportRdf4jHelperNew exportRdf4jHelperNew;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    public boolean pasteBranchLikeNT(String currentIdTheso, String currentIdConcept, String fromIdTheso,
                                     String fromIdConcept, String identifierType, int idUser, NodePreference nodePreference) {

        // récupération des concepts du thésaurus de départ
        SKOSXmlDocument sKOSXmlDocument = getBranch(fromIdTheso, fromIdConcept);

        if (sKOSXmlDocument == null) {
            return false;
        }

        // import des concepts dans le thésaurus actuel
        if (!addBranch(sKOSXmlDocument, nodePreference, currentIdTheso, idUser, identifierType)) {
            return false;
        }
        // suppression des anciens idArk si l'option est cochée
        if("ark".equalsIgnoreCase(identifierType)){
            for(SKOSResource skosResource : sKOSXmlDocument.getConceptList()){
                if(StringUtils.isNotEmpty(skosResource.getArkId())) {
                    conceptHelper.updateArkIdOfConcept(skosResource.getIdentifier(), fromIdTheso, "");
                }
            }
        }

        // relier le concept copié au concept cible
        ArrayList<String> nodeBT = relationsHelper.getListIdBT(fromIdConcept, currentIdTheso);
        for (String idBT : nodeBT) {
            relationsHelper.deleteRelationBT(fromIdConcept, currentIdTheso, idBT, idUser);
        }
        try (Connection conn = dataSource.getConnection()){
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

    
    public boolean pasteBranchToRoot(String currentIdTheso, String fromIdTheso,
            String fromIdConcept, String identifierType, int idUser, NodePreference nodePreference) {

        // récupération des concepts du thésaurus de départ
        SKOSXmlDocument sKOSXmlDocument = getBranch(fromIdTheso, fromIdConcept);

        if (sKOSXmlDocument == null) {
            return false;
        }

        // import des concepts dans le thésaurus actuel
        if (!addBranch(sKOSXmlDocument, nodePreference, currentIdTheso, idUser, identifierType)) {
            return false;
        }

        // nettoyer le concept copié
        ArrayList<String> nodeBT = relationsHelper.getListIdBT(fromIdConcept, currentIdTheso);
        for (String idBT : nodeBT) {
            if (!relationsHelper.deleteRelationBT(fromIdConcept, currentIdTheso, idBT, idUser)) {
                
            }
        }
        
        // Passer la branche en TopTerm
        conceptHelper.setTopConcept(fromIdConcept, currentIdTheso);
        return true;
    }

    private SKOSXmlDocument getBranch(String fromIdTheso, String fromIdConcept) {

        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(fromIdTheso);
        if (nodePreference == null) {
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.setConceptScheme(exportRdf4jHelperNew.exportThesoV2(fromIdTheso, nodePreference));

        ArrayList<String> allConcepts = conceptHelper.getIdsOfBranch(fromIdConcept, fromIdTheso);
        for (String idConcept : allConcepts) {
            skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(fromIdTheso, idConcept, false));
        }        
        return skosXmlDocument;
    }

    private boolean addBranch(SKOSXmlDocument sKOSXmlDocument, NodePreference nodePreference,
            String idTheso, int idUser, String identifierType) {

        int idGroup = -1;
        String formatDate = "yyyy-MM-dd";

        importRdf4jHelper.setInfos(formatDate, idUser, idGroup, "");
        // pour récupérer les identifiants pérennes type Ark ou Handle
        importRdf4jHelper.setSelectedIdentifier(identifierType);

        importRdf4jHelper.setPrefixHandle("");
        importRdf4jHelper.setNodePreference(nodePreference);
        ArrayList<String> idConcepts = new ArrayList<>();

        importRdf4jHelper.setRdf4jThesaurus(sKOSXmlDocument);
        try {
            for (SKOSResource sKOSResource : sKOSXmlDocument.getConceptList()) {
                importRdf4jHelper.addConceptV2(sKOSResource, idTheso);
                if("ark".equalsIgnoreCase(identifierType)){
                    if(!StringUtils.isEmpty(sKOSResource.getArkId())) {
                        idConcepts.add(sKOSResource.getIdentifier());
                    }
                }
            }
            // mise à jour des IdArk après une copie entre thésaurus
            if("ark".equalsIgnoreCase(identifierType)){
                if(CollectionUtils.isNotEmpty(idConcepts)) {
                    conceptHelper.setNodePreference(nodePreference);
                    conceptHelper.generateArkId(idTheso, idConcepts, nodePreference.getSourceLang());
                }
            }
            return true;
        } catch (Exception e) {
        }
        return false;
    }

}
