package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Proposition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface PropositionRepository extends JpaRepository<Proposition, Integer> {

    List<Proposition> findAllByIdConceptAndIdThesaurusOrderByCreated(String idConcept, String idThesaurus);

}