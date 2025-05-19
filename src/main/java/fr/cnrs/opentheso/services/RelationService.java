package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.HierarchicalRelationship;
import fr.cnrs.opentheso.entites.HierarchicalRelationshipHistorique;
import fr.cnrs.opentheso.repositories.HierarchicalRelationshipHistoriqueRepository;
import fr.cnrs.opentheso.repositories.HierarchicalRelationshipRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@AllArgsConstructor
public class RelationService {

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
}
