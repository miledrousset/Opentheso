package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchRepository extends JpaRepository<Concept, Integer> {

    // Recherche sur les termes préférés (avec concepts dépréciés)
    @Query(value = """
        SELECT pt.id_concept as id, t.lexical_value as value
        FROM concept c
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN term t ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        WHERE c.status NOT IN ('CA', 'hidden')
          AND t.lang = :idLang
          AND t.id_thesaurus = :idTheso
          AND f_unaccent(lower(t.lexical_value)) LIKE %:value%
        ORDER BY t.lexical_value <-> :value
        LIMIT 20
    """, nativeQuery = true)
    List<Object[]> searchPreferredLabels(@Param("value") String value, @Param("idLang") String idLang, @Param("idTheso") String idTheso);

    // Recherche sur les termes non-préférés (avec concepts dépréciés)
    @Query(value = """
        SELECT pt.id_concept as id, npt.lexical_value || ' ->' || t.lexical_value as value
        FROM concept c
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN non_preferred_term npt ON pt.id_term = npt.id_term AND pt.id_thesaurus = npt.id_thesaurus
        JOIN term t ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        WHERE c.status NOT IN ('CA', 'hidden')
          AND npt.lang = :idLang
          AND npt.id_thesaurus = :idTheso
          AND f_unaccent(lower(npt.lexical_value)) LIKE %:value%
        ORDER BY npt.lexical_value <-> :value
        LIMIT 20
    """, nativeQuery = true)
    List<Object[]> searchAltLabels(@Param("value") String value, @Param("idLang") String idLang, @Param("idTheso") String idTheso);

    // Recherche sur les termes préférés (sans concepts dépréciés)
    @Query(value = """
        SELECT pt.id_concept as id, t.lexical_value as value
        FROM concept c
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN term t ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        WHERE c.status NOT IN ('CA', 'DEP', 'hidden')
          AND t.lang = :idLang
          AND t.id_thesaurus = :idTheso
          AND f_unaccent(lower(t.lexical_value)) LIKE %:value%
        ORDER BY t.lexical_value <-> :value
        LIMIT 20
    """, nativeQuery = true)
    List<Object[]> searchPreferredLabelsWithoutDeprecated(@Param("value") String value, @Param("idLang") String idLang, @Param("idTheso") String idTheso);

    // Recherche sur les termes non-préférés (sans concepts dépréciés)
    @Query(value = """
        SELECT pt.id_concept as id, npt.lexical_value || ' ->' || t.lexical_value as value
        FROM concept c
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN non_preferred_term npt ON pt.id_term = npt.id_term AND pt.id_thesaurus = npt.id_thesaurus
        JOIN term t ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        WHERE c.status NOT IN ('CA', 'DEP', 'hidden')
          AND npt.lang = :idLang
          AND npt.id_thesaurus = :idTheso
          AND f_unaccent(lower(npt.lexical_value)) LIKE %:value%
        ORDER BY npt.lexical_value <-> :value
        LIMIT 20
    """, nativeQuery = true)
    List<Object[]> searchAltLabelsWithoutDeprecated(@Param("value") String value, @Param("idLang") String idLang, @Param("idTheso") String idTheso);

    @Query(value = """
        SELECT pt.id_concept as id, t.lexical_value as value, c.concept_type
        FROM concept c
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN term t ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        WHERE c.status NOT IN ('CA', 'hidden')
          AND t.lang = :idLang
          AND t.id_thesaurus = :idTheso
          AND c.concept_type IS NOT NULL
          AND c.concept_type != 'concept'
          AND c.concept_type != ''
          AND f_unaccent(lower(t.lexical_value)) LIKE %:value%
        ORDER BY t.lexical_value <-> :value
        LIMIT 20
    """, nativeQuery = true)
    List<Object[]> searchPreferredCustomRelations(@Param("value") String value, @Param("idLang") String idLang, @Param("idTheso") String idTheso);

    @Query(value = """
        SELECT pt.id_concept as id, 
               npt.lexical_value || ' ->' || t.lexical_value as label,
               c.concept_type
        FROM concept c
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN non_preferred_term npt ON pt.id_term = npt.id_term AND pt.id_thesaurus = npt.id_thesaurus
        JOIN term t ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        WHERE c.status NOT IN ('CA', 'hidden')
          AND npt.lang = :idLang
          AND npt.id_thesaurus = :idTheso
          AND c.concept_type IS NOT NULL
          AND c.concept_type != 'concept'
          AND c.concept_type != ''
          AND f_unaccent(lower(npt.lexical_value)) LIKE %:value%
        ORDER BY npt.lexical_value <-> :value
        LIMIT 20
    """, nativeQuery = true)
    List<Object[]> searchAltCustomRelations(@Param("value") String value, @Param("idLang") String idLang, @Param("idTheso") String idTheso);
}