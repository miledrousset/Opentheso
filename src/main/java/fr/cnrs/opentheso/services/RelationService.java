package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.HierarchicalRelationship;
import fr.cnrs.opentheso.repositories.HierarchicalRelationshipRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class RelationService {

    private HierarchicalRelationshipRepository hierarchicalRelationshipRepository;


    public HierarchicalRelationship addHierarchicalRelation(String idConcept1, String idThesaurus, String role, String idConcept2) {

        var hierarchicalRelationship = HierarchicalRelationship.builder()
                .idConcept1(idConcept1)
                .idConcept2(idConcept2)
                .idThesaurus(idThesaurus)
                .role(role)
                .build();

        return hierarchicalRelationshipRepository.save(hierarchicalRelationship);
    }
}
