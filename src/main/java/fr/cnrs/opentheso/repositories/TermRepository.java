package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Term;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface TermRepository extends JpaRepository<Term, Integer> {

    @Query(value = """
        SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END 
        FROM term 
        WHERE f_unaccent(lower(lexical_value)) LIKE f_unaccent(lower(:title))
        AND lang = :lang 
        AND id_thesaurus = :idThesaurus
    """, nativeQuery = true)
    boolean existsPrefLabel(@Param("title") String title, @Param("lang") String lang, @Param("idThesaurus") String idThesaurus);

    Optional<Term> findByIdTermAndIdThesaurusAndLang(String idTerm, String idThesaurus, String lang);

    @Transactional
    @Modifying
    void deleteByIdTermAndIdThesaurus(String idTerm, String idThesaurus);

    @Transactional
    @Modifying
    void deleteByIdTermAndLangAndIdThesaurus(String idTerm, String lang, String idThesaurus);

    Optional<Term> findByLexicalValueAndLangAndIdThesaurus(String lexicalValue, String lang, String idThesaurus);

    @Query("SELECT COALESCE(MAX(t.id), 0) FROM Term t")
    int getMaxInternalId();

    Optional<Term> findByIdTermAndIdThesaurus(String idTerm, String idThesaurus);

    @Query(value = """
        SELECT new fr.cnrs.opentheso.models.terms.NodeTermTraduction(t.lexicalValue, t.lang)
        FROM Term t JOIN PreferredTerm pt ON pt.idTerm = t.idTerm AND pt.idThesaurus = t.idThesaurus
        WHERE pt.idConcept = :idConcept 
        AND t.idThesaurus = :idThesaurus
        ORDER BY t.lexicalValue
    """)
    List<NodeTermTraduction> findAllTraductionsOfConcept(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT COUNT(*) > 0 
        FROM term 
        WHERE lexical_value ILIKE :title 
        AND lang = :lang 
        AND id_thesaurus = :idThesaurus
    """, nativeQuery = true)
    boolean existsTermIgnoreCase(@Param("title") String title, @Param("idThesaurus") String idThesaurus, @Param("lang") String lang);

    @Query(value = """
        SELECT COUNT(*) > 0
        FROM term t
        JOIN preferred_term pt ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        WHERE pt.id_concept = :idConcept 
              AND t.lang = :idLang 
              AND t.id_thesaurus = :idThesaurus
    """, nativeQuery = true)
    boolean existsTranslationForConcept(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus,
                                        @Param("idLang") String idLang);

    @Query(value = """
        SELECT t.id_term, pt.id_concept, t.lexical_value, t.lang, t.id_thesaurus, t.created, t.modified, t.source, t.status, t.contributor, t.creator
        FROM term t
        JOIN preferred_term pt ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        WHERE pt.id_concept = :idConcept 
            AND t.id_thesaurus = :idThesaurus 
            AND t.lang = :idLang
        ORDER BY t.lexical_value DESC
        LIMIT 1
    """, nativeQuery = true)
    Optional<Object[]> getPreferredTermWithConceptInfo(@Param("idConcept") String idConcept, @Param("idThesaurus") String idThesaurus,
                                                       @Param("idLang") String idLang);

    @Query(value = """
        SELECT t.lang AS lang, t.lexical_value AS lexicalValue, l.code_pays AS codePays,
                CASE WHEN :currentLang = 'fr' THEN l.french_name
                    ELSE l.english_name
                END AS nomLang
        FROM term t 
                JOIN preferred_term pt ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
                JOIN languages_iso639 l ON t.lang = l.iso639_1
        WHERE pt.id_concept = :idConcept 
            AND t.id_thesaurus = :idThesaurus 
            AND t.lang <> :currentLang
        ORDER BY t.lexical_value
    """, nativeQuery = true)
    List<Object[]> getConceptTranslationsRaw(@Param("idConcept") String idConcept,
                                             @Param("idThesaurus") String idThesaurus,
                                             @Param("currentLang") String currentLang);

}
