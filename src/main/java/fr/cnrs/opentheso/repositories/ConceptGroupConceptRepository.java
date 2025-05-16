package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptGroupConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ConceptGroupConceptRepository extends JpaRepository<ConceptGroupConcept, Integer> {

    List<ConceptGroupConcept> findByIdThesaurusAndIdConcept(String idThesaurus, String idConcept);

    List<ConceptGroupConcept> findByIdGroupAndIdThesaurus(String idGroup, String idThesaurus);

    @Modifying
    void deleteByIdGroupAndIdConceptAndIdThesaurus(String idGroup, String idConcept, String idThesaurus);

    @Modifying
    void deleteAllByIdGroupAndIdThesaurus(String idGroup, String idThesaurus);

    @Modifying
    void deleteAllByIdThesaurus(String idThesaurus);

    @Query(value = """
        SELECT idgroup, idconcept
        FROM concept_group_concept
        WHERE idthesaurus = :idTheso
        AND idconcept NOT IN (SELECT id_concept FROM concept WHERE id_thesaurus = :idThesaurus)
    """, nativeQuery = true)
    List<Object[]> findGroupConceptLinksWithMissingConcepts(@Param("idThesaurus") String idThesaurus);
}
