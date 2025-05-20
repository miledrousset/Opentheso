package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.HierarchicalRelationship;
import fr.cnrs.opentheso.entites.HierarchicalRelationshipHistorique;
import fr.cnrs.opentheso.models.BroaderRelationProjection;
import fr.cnrs.opentheso.models.RelatedRelationProjection;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
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
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class RelationService {

    private final TermRepository termRepository;
    private HierarchicalRelationshipRepository hierarchicalRelationshipRepository;
    private HierarchicalRelationshipHistoriqueRepository hierarchicalRelationshipHistoriqueRepository;


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
}
