package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.RelationGroup;
import fr.cnrs.opentheso.models.concept.NodeUri;
import fr.cnrs.opentheso.repositories.RelationGroupRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class RelationGroupService {

    private final RelationGroupRepository relationGroupRepository;

    public boolean addSubGroup(String fatherNodeID, String childNodeID, String idThesaurus) {

        log.debug("Ajout d'un sous-group");
        relationGroupRepository.save(RelationGroup.builder()
                .idGroup1(fatherNodeID.toLowerCase())
                .idThesaurus(idThesaurus)
                .relation("sub")
                .idGroup2(childNodeID.toLowerCase())
                .build());
        return true;
    }

    public String getIdFather(String idGroup, String idThesaurus) {

        log.debug("Recherche l'Id du parent du groupe");
        var relationGroup = relationGroupRepository.findByIdThesaurusAndIdGroup2AndRelation(idThesaurus, idGroup, "sub");
        if (relationGroup.isEmpty()) {
            log.error("Aucune parent est rattaché au group id {}", idGroup);
            return null;
        }

        log.debug("Le group parent du groupe id {} est {}", idGroup, relationGroup.get().getIdGroup1());
        return relationGroup.get().getIdGroup1();
    }

    public boolean isHaveSubGroup(String idThesaurus, String idGroup) {

        log.debug("Vérifier si le group {} dispose des sous-groups ", idGroup);
        var relationGroups = relationGroupRepository.findByIdThesaurusAndIdGroup1AndRelation(idThesaurus, idGroup, "sub");
        log.debug("Le group {} dispose des sous-groups : {}", idGroup, !CollectionUtils.isEmpty(relationGroups));
        return CollectionUtils.isNotEmpty(relationGroups);
    }

    public void removeGroupFromGroup(String idGroup, String idParent, String idThesaurus){

        log.debug("Retirer le group id {} du group parent {}", idGroup, idParent);
        relationGroupRepository.deleteByIdGroup1AndIdGroup2AndIdThesaurus(idParent, idGroup, idThesaurus);
    }

    public List<String> getListGroupChildIdOfGroup(String idGroup, String idThesaurus) {

        log.debug("Recherche des sous-groupes de '{}' contenu dans le thésaurus '{}'", idGroup, idThesaurus);
        var relations = relationGroupRepository.findChildGroupIds(idThesaurus, idGroup);
        if (CollectionUtils.isEmpty(relations)) {
            log.debug("Aucun sous-group n'est trouvé pour '{}' contenu dans le thésaurus '{}'", idGroup, idThesaurus);
            return Collections.emptyList();
        }

        log.debug("{} sous-group trouvé pour le group id {}", relations.size(), idGroup);
        return relations;
    }

    public List<NodeUri> getListGroupChildOfGroup(String idThesaurus, String idGroupParent) {
        log.debug("Recherche des détails des sous-groupes de '{}' dans le thésaurus '{}'", idGroupParent, idThesaurus);
        var result = relationGroupRepository.findChildGroupDetails(idThesaurus, idGroupParent);
        if (CollectionUtils.isEmpty(result)) {
            log.debug("Aucun détails n'est trouvé pour les sous-groupes de '{}' dans le thésaurus '{}'", idGroupParent, idThesaurus);
            return Collections.emptyList();
        }

        log.debug("{} sous groups trouvés pour le group parent id {}", result.size(), idGroupParent);
        return result.stream().map(element ->
            NodeUri.builder()
                    .idConcept((String) element[0])
                    .idArk(element[1] != null ? (String) element[1] : "")
                    .idHandle(element[2] != null ? (String) element[2] : "")
                    .idDoi(element[3] != null ? (String) element[3] : "")
                    .build()
        ).toList();
    }

    public void deleteRelationGroup(String idThesaurus, String idGroupFils) {

        log.debug("Suppression de toutes les relations avec les groups id {}", idGroupFils);
        relationGroupRepository.deleteByIdThesaurusAndIdGroup2(idThesaurus, idGroupFils);
    }

    @Transactional
    public void deleteRelationGroupByThesaurus(String idThesaurus) {

        log.debug("Suppression de toutes les relations entre les groups présents dans le thésaurus id {}", idThesaurus);
        relationGroupRepository.deleteByIdThesaurus(idThesaurus);
    }

    public void updateIdThesaurus(String oldIdThesaurus, String newIdThesaurus) {

        log.debug("Mise à jour de toutes les relations entre les groups présents dans le thésaurus id {}", oldIdThesaurus);
        relationGroupRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
    }

}
