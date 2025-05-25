package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.HierarchicalRelationship;
import fr.cnrs.opentheso.entites.HierarchicalRelationshipHistorique;
import fr.cnrs.opentheso.models.BroaderRelationProjection;
import fr.cnrs.opentheso.models.RelatedRelationProjection;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.relations.NodeRelation;
import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.terms.NodeRT;
import fr.cnrs.opentheso.repositories.HierarchicalRelationshipHistoriqueRepository;
import fr.cnrs.opentheso.repositories.HierarchicalRelationshipRepository;
import fr.cnrs.opentheso.repositories.TermRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class RelationService {

    private final TermRepository termRepository;
    private final HierarchicalRelationshipRepository hierarchicalRelationshipRepository;
    private final HierarchicalRelationshipHistoriqueRepository hierarchicalRelationshipHistoriqueRepository;


    public HierarchicalRelationship addHierarchicalRelation(String idConcept1, String idThesaurus, String role, String idConcept2) {

        var hierarchicalRelationship = HierarchicalRelationship.builder()
                .idConcept1(idConcept1)
                .idConcept2(idConcept2)
                .idThesaurus(idThesaurus)
                .role(role)
                .build();

        return hierarchicalRelationshipRepository.save(hierarchicalRelationship);
    }

    public void deleteAllByThesaurus(String idThesaurus) {

        log.info("Suppression des relation des concepts présents dans le thésaurus id {}", idThesaurus);
        hierarchicalRelationshipRepository.deleteAllByIdThesaurus(idThesaurus);
        hierarchicalRelationshipHistoriqueRepository.deleteAllByIdThesaurus(idThesaurus);
    }

    public void deleteAllRelationOfConcept(String idConcept, String idThesaurus) {

        log.info("Suppression des toutes relation avec le concept id {} présents dans le thésaurus id {}", idConcept, idThesaurus);
        hierarchicalRelationshipRepository.deleteAllByIdThesaurusAndIdConcept1(idThesaurus, idConcept);
        hierarchicalRelationshipRepository.deleteAllByIdThesaurusAndIdConcept2(idThesaurus, idConcept);
    }

    public void updateThesaurusId(String oldIdThesaurus, String newIdThesaurus) {

        log.info("Mise à jour du thésaurus id pour les relation entre les concepts présents dans le thésaurus id {}", oldIdThesaurus);
        hierarchicalRelationshipRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        hierarchicalRelationshipHistoriqueRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
    }

    public void addLinkHierarchicalRelation(HierarchicalRelationship hierarchicalRelationship, int idUser) {

        log.info("Ajouter une relation d'hiérarchique entre les deux concepts {} et {}",
                hierarchicalRelationship.getIdConcept1(), hierarchicalRelationship.getIdConcept2());

        hierarchicalRelationshipRepository.save(HierarchicalRelationship.builder()
                .idConcept1(hierarchicalRelationship.getIdConcept1())
                .idConcept2(hierarchicalRelationship.getIdConcept2())
                .role(hierarchicalRelationship.getRole())
                .idThesaurus(hierarchicalRelationship.getIdThesaurus())
                .build());

        log.info("Enregistrement du trace de la relation entre concepts");
        hierarchicalRelationshipHistoriqueRepository.save(HierarchicalRelationshipHistorique.builder()
                .idConcept1(hierarchicalRelationship.getIdConcept1())
                .idConcept2(hierarchicalRelationship.getIdConcept2())
                .idThesaurus(hierarchicalRelationship.getIdThesaurus())
                .idUser(idUser)
                .action("ADD")
                .role(hierarchicalRelationship.getRole())
                .build());
    }

    public List<NodeIdValue> getCandidatRelationsBT(String idConceptSelected, String idThesaurus, String lang) {

        var nodeBTs = getListBT(idConceptSelected, idThesaurus, lang);

        if(CollectionUtils.isNotEmpty(nodeBTs)) {
            return nodeBTs.stream()
                    .map(element -> NodeIdValue.builder()
                            .id(element.getIdConcept())
                            .value(element.getTitle())
                            .build())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public List<NodeBT> getListBT(String idConcept, String idThesaurus, String idLang) {

        log.info("Chargement des relations BT pour le concept '{}' dans le thésaurus '{}'", idConcept, idThesaurus);
        var broaderRelations = hierarchicalRelationshipRepository.findBroaderConcepts(idConcept, idThesaurus);

        List<NodeBT> result = new ArrayList<>();
        for (BroaderRelationProjection relation : broaderRelations) {
            NodeBT nodeBT = new NodeBT();
            nodeBT.setIdConcept(relation.getIdConcept2());
            nodeBT.setRole(relation.getRole());
            nodeBT.setStatus(relation.getStatus());

            var lexicalOpt = termRepository.getLexicalValueOfConcept(relation.getIdConcept2(), idThesaurus, idLang);
            if (lexicalOpt.isEmpty() || StringUtils.isBlank(lexicalOpt.get())) {
                nodeBT.setTitle("");
            } else {
                nodeBT.setTitle(lexicalOpt.get());
            }

            // Si besoin de redéfinir le statut avec celui du term (prioritaire)
            termRepository.findByIdTermAndIdThesaurusAndLang(relation.getIdConcept2(), idThesaurus, idLang)
                    .ifPresent(term -> {
                        if (StringUtils.isNotBlank(term.getStatus())) {
                            nodeBT.setStatus(term.getStatus());
                        }
                    });

            result.add(nodeBT);
        }

        result.sort(Comparator.naturalOrder());
        log.info("Nombre de termes BT trouvés : {}", result.size());
        return result;
    }

    public List<NodeIdValue> getCandidatRelationsRT(String idConceptSelected, String idThesaurus, String lang) {

        var nodeRTs = getListRT(idConceptSelected, idThesaurus, lang);

        if(CollectionUtils.isNotEmpty(nodeRTs)) {
            return nodeRTs.stream()
                    .map(element -> NodeIdValue.builder()
                            .id(element.getIdConcept())
                            .value(element.getTitle())
                            .build())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public List<NodeRT> getListRT(String idConcept, String idThesaurus, String idLang) {
        log.info("Chargement des relations RT pour le concept '{}' dans le thésaurus '{}'", idConcept, idThesaurus);

        var rawRelations = hierarchicalRelationshipRepository.findRelatedConcepts(idConcept, idThesaurus);
        List<NodeRT> result = new ArrayList<>();

        for (RelatedRelationProjection relation : rawRelations) {
            NodeRT nodeRT = new NodeRT();
            nodeRT.setIdConcept(relation.getIdConcept2());
            nodeRT.setRole(relation.getRole());
            nodeRT.setStatus(relation.getStatus());

            String lexicalValue = termRepository
                    .getLexicalValueOfConcept(relation.getIdConcept2(), idThesaurus, idLang)
                    .orElse("__" + relation.getIdConcept2());

            nodeRT.setTitle(lexicalValue);
            result.add(nodeRT);
        }

        result.sort(Comparator.naturalOrder());
        log.info("{} relations RT trouvées pour le concept '{}'", result.size(), idConcept);
        return result;
    }

    public List<HierarchicalRelationship> getListConceptRelationParRole(String idConcept, String idThesaurus, String role) {

        log.info("Recherche des concepts en relation avec le concept {} et avec le rôle {}", idConcept, role);
        return hierarchicalRelationshipRepository.findAllByIdThesaurusAndIdConcept1AndRoleLike(idThesaurus, idConcept, role);
    }

    public boolean isConceptHaveRelationBT(String idConcept, String idThesaurus) {

        log.info("Vérifier si un concept a une relation BT (term générique");
        var result = hierarchicalRelationshipRepository.findAllByIdThesaurusAndIdConcept1AndRoleLike(idThesaurus, idConcept, "BT");
        return CollectionUtils.isNotEmpty(result);
    }

    public List<String> getListIdBT(String idConcept, String idThesaurus) {

        log.info("Recherche des id des terms génériques du concept id {}", idConcept);
        var listIdBT = hierarchicalRelationshipRepository.findAllByIdThesaurusAndIdConcept1AndRoleLike(idThesaurus, idConcept, "BT");
        return listIdBT.stream().map(HierarchicalRelationship::getIdConcept2).toList();
    }

    public List<HierarchicalRelationship> getListLoopRelations(String role, String idThesaurus) {

        log.info("Recherche de relation dans le thésaurus {} et avec le rôle {}", idThesaurus, role);
        var result = hierarchicalRelationshipRepository.getListLoopRelations(idThesaurus, role);
        if (CollectionUtils.isEmpty(result)) {
            log.info("Aucune relation trouvée dans le thésaurus {} et avec le rôle {}", idThesaurus, role);
            return List.of();
        }
        return result;
    }

    public void deleteThisRelation(String idConcept1, String idThesaurus, String role, String idConcept2) {

        log.info("Suppression de la relation entre le concept {} et le concept {}", idConcept1, idThesaurus);
        hierarchicalRelationshipRepository.deleteAllByIdThesaurusAndIdConcept1AndIdConcept2AndRole(idThesaurus, idConcept1, idConcept2, role);
    }


    public List<String> getListIdWhichHaveNt(String idConcept, String idThesaurus) {

        var result = hierarchicalRelationshipRepository.findAllByIdThesaurusAndIdConcept2AndRoleLike(idThesaurus, idConcept, "NT");
        if (CollectionUtils.isEmpty(result)) {
            log.info("Aucune relation trouvée avec le concept le concept {} et avec le role NR", idConcept);
            return List.of();
        }
        return result.stream().map(HierarchicalRelationship::getIdConcept1).toList();
    }

    public NodeRelation getLoopRelation(String idTheso, String idConcept) {

        log.info("Recherche d'une relation en boucle pour le concept '{}' dans le thésaurus '{}'", idConcept, idTheso);
        return hierarchicalRelationshipRepository.findBtRelation(idTheso, idConcept)
                .flatMap(rel1 -> {
                    log.debug("Relation BT trouvée : {} → {}", rel1.getIdConcept1(), rel1.getIdConcept2());
                    return hierarchicalRelationshipRepository.findLoopBtRelation(idTheso, rel1.getIdConcept1(), rel1.getIdConcept2());
                })
                .map(rel -> {
                    log.info("Relation en boucle détectée : {} → {}", rel.getIdConcept1(), rel.getIdConcept2());
                    NodeRelation nodeRelation = new NodeRelation();
                    nodeRelation.setIdConcept1(rel.getIdConcept1());
                    nodeRelation.setIdConcept2(rel.getIdConcept2());
                    nodeRelation.setRelation(rel.getRole());
                    return nodeRelation;
                })
                .orElseGet(() -> {
                    log.info("Aucune relation en boucle trouvée pour le concept '{}'", idConcept);
                    return null;
                });
    }

    public List<String> getListIdOfTopTermForRepair(String idThesaurus) {
        try {
            List<String> topConcepts = hierarchicalRelationshipRepository.findTopConceptsWithNTOnly(idThesaurus);
            List<String> isolatedConcepts = hierarchicalRelationshipRepository.findIsolatedConcepts(idThesaurus);

            Set<String> merged = new LinkedHashSet<>();
            merged.addAll(topConcepts);
            merged.addAll(isolatedConcepts);

            log.info("Top terms à réparer pour le thésaurus '{}': {} concepts", idThesaurus, merged.size());
            return new ArrayList<>(merged);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des TopTerms pour réparation du thésaurus '{}'", idThesaurus, e);
            return new ArrayList<>();
        }
    }

}
