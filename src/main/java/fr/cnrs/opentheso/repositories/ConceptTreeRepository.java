package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;


public interface ConceptTreeRepository extends JpaRepository<Concept, String> {

    @Query(value = """
        SELECT c.id_concept AS idConcept, c.notation
        FROM concept c
        JOIN hierarchical_relationship h ON c.id_concept = h.id_concept2 AND c.id_thesaurus = h.id_thesaurus
        WHERE h.id_thesaurus = :idThesaurus
          AND h.id_concept1 = :idConcept
          AND h.role ILIKE 'NT%'
          AND c.status != 'CA'
        ORDER BY c.notation ASC
        LIMIT 2000
    """, nativeQuery = true)
    List<Object[]> findConceptsByNotation(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT h.id_concept2 AS idConcept
        FROM hierarchical_relationship h
        JOIN concept c ON c.id_concept = h.id_concept2 AND c.id_thesaurus = h.id_thesaurus
        WHERE h.id_thesaurus = :idThesaurus
          AND h.id_concept1 = :idConcept
          AND h.role ILIKE 'NT%'
          AND c.status != 'CA'
        LIMIT 2000
    """, nativeQuery = true)
    List<Object[]> findConceptsAlphabetically(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT t.lexical_value, c.status
        FROM concept c
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN term t ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        WHERE c.id_concept = :idConcept
          AND t.lang = :idLang
          AND t.id_thesaurus = :idThesaurus
    """, nativeQuery = true)
    Optional<Object[]> findLexicalValueAndStatus(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus, @Param("idLang") String idLang);
}
