package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.PreferredTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface PreferredTermRepository extends JpaRepository<PreferredTerm, Integer> {

    @Modifying
    @Transactional
    void deleteByIdThesaurusAndIdTerm(String idThesaurus, String idTerm);

    @Modifying
    @Transactional
    void deleteByIdThesaurus(String idThesaurus);

    Optional<PreferredTerm> findByIdThesaurusAndIdConcept(String idThesaurus, String idConcept);

    @Modifying
    @Transactional
    @Query("UPDATE PreferredTerm t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

}
