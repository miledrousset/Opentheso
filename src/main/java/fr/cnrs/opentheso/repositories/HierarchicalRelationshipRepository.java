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


public interface HierarchicalRelationshipRepository extends JpaRepository<HierarchicalRelationship, Integer> {

    @Modifying
    void deleteAllByIdThesaurus(String idThesaurus);

    @Modifying
    void deleteAllByIdThesaurusAndIdConcept1(String idThesaurus, String idConcept);

    @Modifying
    void deleteAllByIdThesaurusAndIdConcept2(String idThesaurus, String idConcept);

    @Modifying
    @Transactional
    @Query("UPDATE HierarchicalRelationship t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

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
}
