package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.CandidatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface CandidatStatusRepository extends JpaRepository<CandidatStatus, Integer> {

    Optional<CandidatStatus> findByIdConcept(String idConcept);

    Optional<CandidatStatus> findAllByIdConceptAndIdThesaurus(String idConcept, String idThesaurus);

    @Modifying
    void deleteAllByIdThesaurus(String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE CandidatStatus t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);
}
