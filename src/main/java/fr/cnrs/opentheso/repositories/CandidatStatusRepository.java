package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.CandidatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface CandidatStatusRepository extends JpaRepository<CandidatStatus, Integer> {

    Optional<CandidatStatus> findByIdConcept(String idConcept);

    Optional<CandidatStatus> findAllByIdConceptAndIdThesaurus(String idConcept, String idThesaurus);

}
