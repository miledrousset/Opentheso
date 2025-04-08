package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ConceptDcTermRepository extends JpaRepository<ConceptDcTerm, Integer> {

    List<ConceptDcTerm> findAllByIdThesaurusAndIdConcept(String idThesaurus, String idConcept);

}
