package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptReplacedBy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface ConceptReplacedByRepository extends JpaRepository<ConceptReplacedBy, Integer> {

    @Modifying
    @Transactional
    void deleteAllByIdThesaurus(String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE ConceptReplacedBy t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

    @Modifying
    @Transactional
    void deleteAllByIdConcept1AndIdThesaurus(String idConcept1, String idThesaurus);

    @Modifying
    @Transactional
    void deleteAllByIdConcept2AndIdThesaurus(String idConcept2, String idThesaurus);

}
