package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.TermHistorique;
import fr.cnrs.opentheso.models.TermHistoriqueProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


public interface HistoriqueRepository extends JpaRepository<TermHistorique, Integer> {

    @Query(value = """
        SELECT th.lexical_value AS lexicalValue, th.lang AS lang, th.action AS action, th.modified AS modified, u.username AS username 
        FROM term_historique th JOIN users u ON u.id_user = th.id_user 
        WHERE th.id_term = :idTerm AND th.id_thesaurus = :idThesaurus  
        ORDER BY th.modified DESC
    """, nativeQuery = true)
    List<TermHistoriqueProjection> findTermHistories(@Param("idTerm") String idTerm, @Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT lexical_value, lang, action, modified, username 
        FROM non_preferred_term_historique npth, users u
        WHERE u.id_user = npth.id_user 
        AND id_term = :idTerm 
        AND id_thesaurus = :idThesaurus 
        ORDER BY modified DESC
    """, nativeQuery = true)
    List<TermHistoriqueProjection> findSynonymHistories(@Param("idTerm") String idTerm, @Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT id_concept1, role, id_concept2, action, modified, username 
        FROM hierarchical_relationship_historique hr, users u 
        WHERE u.id_user = hr.id_user AND hr.id_concept1 = :idConcept AND hr.id_thesaurus = :idThesaurus 
        ORDER BY modified DESC
    """, nativeQuery = true)
    List<TermHistoriqueProjection> findgetRelationsHistories(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT lexicalvalue, notetypecode, lang, action_performed, modified, username 
        FROM note_historique, users 
        WHERE users.id_user = note_historique.id_user 
        AND (id_concept = :idConcept OR id_term = :idTerm) 
        AND id_thesaurus = :idThesaurus 
        ORDER BY modified DESC
    """, nativeQuery = true)
    List<TermHistoriqueProjection> getNotesHistories(@Param("idConcept") String idConcept,
                                                     @Param("idTerm") String idTerm,
                                                     @Param("idThesaurus") String idThesaurus);

}
