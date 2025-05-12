package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Concept;
import fr.cnrs.opentheso.entites.Thesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;


public interface ConceptRepository extends JpaRepository<Concept, Integer> {

    List<Concept> findAllByThesaurusAndTopConceptAndStatusNotLike(Thesaurus thesaurus, boolean isTopConcept, String status);

    @Modifying
    @Transactional
    @Query("UPDATE Concept c SET c.topConcept = :status WHERE c.idConcept = :idConcept AND c.thesaurus.idThesaurus = :idThesaurus")
    void setTopConceptTag(@Param("status") boolean status, @Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE Concept c SET c.gps = :status WHERE c.idConcept = :idConcept AND c.thesaurus.idThesaurus = :idThesaurus")
    int setGpstTag(@Param("status") boolean status, @Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE Concept c SET c.status = :status WHERE c.idConcept = :idConcept AND c.thesaurus.idThesaurus = :idThesaurus")
    int setStatus(@Param("status") String status, @Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    List<Concept> findAllByThesaurusIdThesaurusAndStatus(String idThesaurus, String status);

    @Query(value = "SELECT nextval('concept__id_seq')", nativeQuery = true)
    Long getNextConceptNumericId();

    @Query(value = """
        SELECT created, modified, status 
        FROM concept 
        WHERE id_concept = :idConcept 
            AND id_thesaurus = :idThesaurus
    """, nativeQuery = true)
    Optional<Object[]> getConceptMetadata(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

}
