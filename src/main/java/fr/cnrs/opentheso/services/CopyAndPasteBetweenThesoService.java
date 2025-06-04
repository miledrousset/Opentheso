package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.services.exports.rdf4j.ExportRdf4jHelperNew;
import fr.cnrs.opentheso.services.imports.rdf4j.ImportRdf4jHelper;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
@AllArgsConstructor
public class CopyAndPasteBetweenThesoService {

    private final ImportRdf4jHelper importRdf4jHelper;
    private final ExportRdf4jHelperNew exportRdf4jHelperNew;
    private final PreferenceService preferenceService;
    private final ConceptAddService conceptAddService;
    private final ConceptService conceptService;
    private final ArkService arkService;
    private final RelationService relationService;


    public boolean pasteBranchLikeNT(String currentIdTheso, String currentIdConcept, String fromIdTheso,
                                     String fromIdConcept, String identifierType, int idUser, Preferences nodePreference) {

        // récupération des concepts du thésaurus de départ
        SKOSXmlDocument sKOSXmlDocument = getBranch(fromIdTheso, fromIdConcept);

        if (sKOSXmlDocument == null) {
            return false;
        }

        // import des concepts dans le thésaurus actuel
        addBranch(sKOSXmlDocument, nodePreference, currentIdTheso, idUser, identifierType);

        // suppression des anciens idArk si l'option est cochée
        if("ark".equalsIgnoreCase(identifierType)){
            for(SKOSResource skosResource : sKOSXmlDocument.getConceptList()){
                if(StringUtils.isNotEmpty(skosResource.getArkId())) {
                    arkService.updateArkIdOfConcept(skosResource.getIdentifier(), fromIdTheso, "");
                }
            }
        }

        // relier le concept copié au concept cible
        var nodeBT = relationService.getListIdBT(fromIdConcept, currentIdTheso);
        for (String idBT : nodeBT) {
            relationService.deleteRelationBT(fromIdConcept, currentIdTheso, idBT, idUser);
        }
        relationService.addRelationBT(fromIdConcept, currentIdTheso, currentIdConcept, idUser);
        return true;
    }

    
    public boolean pasteBranchToRoot(String currentIdThesaurus, String fromIdTheauruso, String fromIdConcept, String identifierType,
                                  int idUser, Preferences nodePreference) {

        // récupération des concepts du thésaurus de départ
        var sKOSXmlDocument = getBranch(fromIdTheauruso, fromIdConcept);
        if (sKOSXmlDocument == null) {
            return false;
        }

        // import des concepts dans le thésaurus actuel
        addBranch(sKOSXmlDocument, nodePreference, currentIdThesaurus, idUser, identifierType);

        // nettoyer le concept copié
        var nodeBT = relationService.getListIdBT(fromIdConcept, currentIdThesaurus);
        for (String idBT : nodeBT) {
            relationService.deleteRelationBT(fromIdConcept, currentIdThesaurus, idBT, idUser);
        }
        // Passer la branche en TopTerm
        conceptService.setTopConcept(fromIdConcept, currentIdThesaurus, true);
        return true;
    }

    private SKOSXmlDocument getBranch(String fromIdTheso, String fromIdConcept) {

        var nodePreference = preferenceService.getThesaurusPreferences(fromIdTheso);
        if (nodePreference == null) {
            return null;
        }

        var skosXmlDocument = new SKOSXmlDocument();
        skosXmlDocument.setConceptScheme(exportRdf4jHelperNew.exportThesoV2(fromIdTheso, nodePreference));

        var allConcepts = conceptService.getIdsOfBranch(fromIdConcept, fromIdTheso);
        for (String idConcept : allConcepts) {
            skosXmlDocument.addconcept(exportRdf4jHelperNew.exportConceptV2(fromIdTheso, idConcept, false));
        }        
        return skosXmlDocument;
    }

    private void addBranch(SKOSXmlDocument sKOSXmlDocument, Preferences nodePreference, String idThesaurus,
                           int idUser, String identifierType) {

        int idGroup = -1;
        String formatDate = "yyyy-MM-dd";

        importRdf4jHelper.setInfos(formatDate, idUser, idGroup, "");
        importRdf4jHelper.setSelectedIdentifier(identifierType);
        importRdf4jHelper.setPrefixHandle("");
        importRdf4jHelper.setNodePreference(nodePreference);
        importRdf4jHelper.setRdf4jThesaurus(sKOSXmlDocument);

        List<String> idConcepts = new ArrayList<>();
        for (SKOSResource sKOSResource : sKOSXmlDocument.getConceptList()) {
            importRdf4jHelper.addConceptV2(sKOSResource, idThesaurus);
            if("ark".equalsIgnoreCase(identifierType)){
                if(!StringUtils.isEmpty(sKOSResource.getArkId())) {
                    idConcepts.add(sKOSResource.getIdentifier());
                }
            }
        }

        // mise à jour des IdArk après une copie entre thésaurus
        if("ark".equalsIgnoreCase(identifierType)){
            if(CollectionUtils.isNotEmpty(idConcepts)) {
                conceptAddService.generateArkId(idThesaurus, idConcepts, nodePreference.getSourceLang(), null);
            }
        }
    }

}
