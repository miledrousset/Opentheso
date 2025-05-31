package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.HierarchicalRelationship;
import fr.cnrs.opentheso.models.BroaderRelationProjection;
import fr.cnrs.opentheso.models.RelatedRelationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface HierarchicalRelationshipRepository extends JpaRepository<HierarchicalRelationship, Integer> {

    List<HierarchicalRelationship> findAllByIdThesaurusAndIdConcept1AndRoleLike(String idThesaurus, String idConcept1, String roleLike);

    List<HierarchicalRelationship> findAllByIdThesaurusAndIdConcept2AndRoleLike(String idThesaurus, String idConcept2, String roleLike);

    @Modifying
    @Transactional
    void deleteAllByIdThesaurus(String idThesaurus);

    @Modifying
    @Transactional
    void deleteAllByIdThesaurusAndIdConcept1(String idThesaurus, String idConcept);

    @Modifying
    @Transactional
    void deleteAllByIdThesaurusAndIdConcept2(String idThesaurus, String idConcept);

    @Modifying
    @Transactional
    void deleteAllByIdThesaurusAndIdConcept1AndIdConcept2AndRole(String idThesaurus, String idConcept1, String idConcept2, String role);

    @Modifying
    @Transactional
    @Query("UPDATE HierarchicalRelationship t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

    @Query(value = """
        SELECT hr.*
        FROM hierarchical_relationship hr
        WHERE hr.id_thesaurus = :idThesaurus
        AND hr.id_concept1 = hr.id_concept2
        AND hr.role = :role
    """, nativeQuery = true)
    List<HierarchicalRelationship> getListLoopRelations(@Param("idThesaurus") String idThesaurus, @Param("role") String role);

    @Query(value = """
        SELECT hr.id_concept2 AS idConcept2, c.status AS status, hr.role AS role
        FROM hierarchical_relationship hr
            JOIN concept c ON c.id_concept = hr.id_concept1 AND c.id_thesaurus = hr.id_thesaurus
        WHERE hr.id_thesaurus = :idThesaurus
        AND hr.id_concept1 = :idConcept
        AND hr.role LIKE 'BT%'
    """, nativeQuery = true)
    List<BroaderRelationProjection> findBroaderConcepts(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT hr.id_concept2 AS idConcept2, hr.role AS role, c.status AS status
        FROM hierarchical_relationship hr
            JOIN concept c ON hr.id_concept2 = c.id_concept AND hr.id_thesaurus = c.id_thesaurus
        WHERE hr.id_thesaurus = :idThesaurus
        AND hr.id_concept1 = :idConcept
        AND hr.role = 'RT'
        AND c.status != 'CA'
    """, nativeQuery = true)
    List<RelatedRelationProjection> findRelatedConcepts(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Query("""
        SELECT h FROM HierarchicalRelationship h
        WHERE h.idThesaurus = :idThesaurus 
        AND h.idConcept1 = :idConcept 
        AND h.role = 'BT'
    """)
    Optional<HierarchicalRelationship> findBtRelation(@Param("idThesaurus") String idThesaurus, @Param("idConcept") String idConcept);

    @Query("""
        SELECT h FROM HierarchicalRelationship h
        WHERE h.idThesaurus = :idThesaurus 
        AND h.idConcept1 = :idConcept1 
        AND h.idConcept2 = :idConcept2 
        AND h.role = 'BT'
        AND h.idConcept1 IN (
            SELECT h2.idConcept2 FROM HierarchicalRelationship h2
            WHERE h2.idThesaurus = :idThesaurus 
            AND h2.idConcept1 = :idConcept2 
            AND h2.idConcept2 = :idConcept1 
            AND h2.role = 'BT'
        )
    """)
    Optional<HierarchicalRelationship> findLoopBtRelation(@Param("idThesaurus") String idThesaurus, @Param("idConcept1") String idConcept1,
                                                          @Param("idConcept2") String idConcept2);

    Optional<HierarchicalRelationship> findByIdThesaurusAndIdConcept1AndIdConcept2(String idThesaurus, String idConcept1, String idConcept2);

    @Query(value = """
        SELECT DISTINCT hr.id_concept1
        FROM hierarchical_relationship hr
        WHERE hr.id_thesaurus = :idThesaurus
        AND hr.role LIKE 'NT%'
        AND hr.id_concept1 NOT IN (SELECT hr2.id_concept2 FROM hierarchical_relationship hr2 WHERE hr2.id_thesaurus = :idThesaurus AND (hr2.role NOT LIKE 'BT%' AND hr2.role NOT LIKE 'RT%'))
    """, nativeQuery = true)
    List<String> findTopConceptsWithNTOnly(@Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT c.id_concept
        FROM concept c
        WHERE c.id_thesaurus = :idThesaurus
        AND c.id_concept NOT IN (SELECT DISTINCT hr.id_concept1 
                                 FROM hierarchical_relationship hr
                                 WHERE hr.id_thesaurus = :idThesaurus
                                 AND hr.role NOT LIKE 'RT%')
        """, nativeQuery = true)
    List<String> findIsolatedConcepts(@Param("idThesaurus") String idThesaurus);

}
