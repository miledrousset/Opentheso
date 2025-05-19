package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptFacet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface ConceptFacetRepository extends JpaRepository<ConceptFacet, Integer> {

    @Modifying
    void deleteAllByIdThesaurus(String idThesaurus);

    @Modifying
    void deleteAllByIdConceptAndIdThesaurus(String idConcept, String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE ConceptFacet t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

}
