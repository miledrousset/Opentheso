package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.CandidatMessages;
import fr.cnrs.opentheso.models.CandidatMessageProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CandidatMessageRepository extends JpaRepository<CandidatMessages, Integer> {

    @Query(value = """
        SELECT u.id_user AS idUser, u.username AS username, c.value AS value, c.date AS date
        FROM candidat_messages c JOIN users u ON c.id_user = u.id_user
        WHERE c.id_concept = :idConcept
        AND c.id_thesaurus = :idThesaurus
        ORDER BY c.date
    """, nativeQuery = true)
    List<CandidatMessageProjection> findMessagesByConceptAndThesaurus(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

}
