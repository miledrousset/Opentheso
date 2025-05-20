package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface ConceptDcTermRepository extends JpaRepository<ConceptDcTerm, Integer> {

    List<ConceptDcTerm> findAllByIdThesaurusAndIdConcept(String idThesaurus, String idConcept);

    @Modifying
    void deleteAllByIdThesaurus(String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE ConceptDcTerm t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);
}
