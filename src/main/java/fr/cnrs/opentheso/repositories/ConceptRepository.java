package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Concept;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;


public interface ConceptRepository extends JpaRepository<Concept, Integer> {

    Optional<Concept> findByIdConcept(String idConcept);

    Optional<Concept> findByIdConceptAndIdThesaurus(String idConcept, String idThesaurus);

    List<Concept> findAllByIdThesaurusAndTopConceptAndStatusNotLike(String idThesaurus, boolean isTopConcept, String status);

    @Modifying
    @Transactional
    @Query("UPDATE Concept c SET c.topConcept = :status WHERE c.idConcept = :idConcept AND c.idThesaurus = :idThesaurus")
    void setTopConceptTag(@Param("status") boolean status, @Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE Concept c SET c.gps = :status WHERE c.idConcept = :idConcept AND c.idThesaurus = :idThesaurus")
    int setGpsTag(@Param("status") boolean status, @Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE Concept c SET c.status = :status WHERE c.idConcept = :idConcept AND c.idThesaurus = :idThesaurus")
    int setStatus(@Param("status") String status, @Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    List<Concept> findAllByIdThesaurusAndStatus(String idThesaurus, String status);

    List<Concept> findAllByIdThesaurusAndStatusNot(String idThesaurus, String status);

    @Query(value = "SELECT nextval('concept__id_seq')", nativeQuery = true)
    Long getNextConceptNumericId();

    @Query(value = """
        SELECT created, modified, status 
        FROM concept 
        WHERE id_concept = :idConcept AND id_thesaurus = :idThesaurus
    """, nativeQuery = true)
    Optional<Object[]> getConceptMetadata(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE Concept c SET c.notation = '' WHERE c.notation ilike 'null'")
    void cleanConcept();

    @Modifying
    void deleteAllByIdThesaurus(String thesaurus);

    @Modifying
    void deleteAllByIdThesaurusAndIdConcept(String thesaurus, String idConcept);

    @Modifying
    @Transactional
    @Query("UPDATE Concept t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

    @Query(value = """
        SELECT DISTINCT c.id_concept, c.notation
        FROM concept c JOIN concept_group_concept gc ON c.id_concept = gc.idconcept AND c.id_thesaurus = gc.idthesaurus
        WHERE c.id_thesaurus = :idThesaurus
            AND LOWER(gc.idgroup) = LOWER(:idGroup)
            AND c.top_concept IS TRUE
            AND c.status != 'CA'
            LIMIT 2001
        """, nativeQuery = true)
    List<Object[]> findTopConceptsByGroup(@Param("idThesaurus") String idThesaurus, @Param("idGroup") String idGroup);

    @Query(value = """
        SELECT DISTINCT c.id_concept, c.notation
        FROM concept c JOIN concept_group_concept gc ON c.id_concept = gc.idconcept AND c.id_thesaurus = gc.idthesaurus
        WHERE c.id_thesaurus = :idThesaurus
        AND LOWER(gc.idgroup) = LOWER(:idGroup)
        AND c.status != 'CA'
        LIMIT 2001
    """, nativeQuery = true)
    List<Object[]> findConceptsByGroup(@Param("idThesaurus") String idThesaurus, @Param("idGroup") String idGroup);

    @Query(value = """
        SELECT concept.id_concept
        FROM concept
        JOIN preferred_term ON concept.id_concept = preferred_term.id_concept AND concept.id_thesaurus = preferred_term.id_thesaurus
        JOIN non_preferred_term ON preferred_term.id_term = non_preferred_term.id_term AND preferred_term.id_thesaurus = non_preferred_term.id_thesaurus
        WHERE non_preferred_term.id_thesaurus = :idTheso
        AND non_preferred_term.lang = :idLang
        AND LOWER(non_preferred_term.lexical_value) = LOWER(:label)
        AND concept.status != 'DEP'
        LIMIT 1
    """, nativeQuery = true)
    Optional<String> findConceptIdFromAltLabel(@Param("idTheso") String idTheso, @Param("label") String label, @Param("idLang") String idLang);

    @Query(value = """
        SELECT c.id_concept
        FROM concept c
                JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
                JOIN term t ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        WHERE t.id_thesaurus = :idTheso
        AND t.lang = :idLang
        AND LOWER(t.lexical_value) = LOWER(:label)
        AND c.status NOT IN ('DEP', 'CA')
        LIMIT 1
    """, nativeQuery = true)
    Optional<String> findConceptIdFromLabel(@Param("idTheso") String idTheso, @Param("label") String label, @Param("idLang") String idLang);

    @Query("""
        SELECT c.modified FROM Concept c 
        WHERE c.idThesaurus = :idThesaurus 
        AND c.status <> 'CA' 
        AND c.modified IS NOT NULL 
        ORDER BY c.modified DESC
    """)
    List<Date> findLastModifiedDates(@Param("idThesaurus") String idThesaurus, PageRequest pageable);
}
