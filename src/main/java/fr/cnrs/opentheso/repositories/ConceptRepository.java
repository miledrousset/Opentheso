package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Concept;
import fr.cnrs.opentheso.entites.Thesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


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

    List<Concept> findAllByThesaurusIdThesaurusAndStatus(String idThesaurus, String status);

}
