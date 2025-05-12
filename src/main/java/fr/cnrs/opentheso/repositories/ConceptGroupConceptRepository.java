package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptGroupConcept;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ConceptGroupConceptRepository extends JpaRepository<ConceptGroupConcept, Integer> {

    Optional<ConceptGroupConcept> findByIdThesaurusAndIdConcept(String idThesaurus, String idConcept);

}
