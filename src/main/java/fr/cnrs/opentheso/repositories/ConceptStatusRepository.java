package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Concept;
import fr.cnrs.opentheso.models.ConceptGroupProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;
import java.util.List;


public interface ConceptStatusRepository extends JpaRepository<Concept, Integer> {

    @Query(value = """
        SELECT COUNT(id_concept) FROM concept WHERE id_thesaurus = :idThesaurus AND status NOT IN ('CA', 'DEP')
    """, nativeQuery = true)
    int countValidConceptsByThesaurus(@Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT COUNT(c.id_concept)
        FROM concept c
        JOIN concept_group_concept cgc ON c.id_concept = cgc.idconcept AND c.id_thesaurus = cgc.idthesaurus
        WHERE c.id_thesaurus = :idThesaurus AND LOWER(cgc.idgroup) = LOWER(:idGroup) AND c.status != 'CA'
    """, nativeQuery = true)
    int countConceptsInGroup(@Param("idThesaurus") String idThesaurus, @Param("idGroup") String idGroup);

    @Query(value = """
        SELECT COUNT(id_concept)
        FROM concept
        WHERE id_thesaurus = :idThesaurus
            AND status != 'CA'
            AND id_concept NOT IN (SELECT idconcept FROM concept_group_concept WHERE idthesaurus = :idThesaurus)
    """, nativeQuery = true)
    int countConceptsWithoutGroup(@Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT COUNT(npt.id_term)
        FROM non_preferred_term npt
        INNER JOIN preferred_term pt  ON pt.id_term = npt.id_term AND pt.id_thesaurus = npt.id_thesaurus
        INNER JOIN concept_group_concept cgc ON cgc.idthesaurus = pt.id_thesaurus AND cgc.idconcept = pt.id_concept
        WHERE npt.lang = :idLang
            AND npt.id_thesaurus = :idThesaurus
            AND LOWER(cgc.idgroup) = LOWER(:idGroup)
        """, nativeQuery = true)
    int countNonPreferredTermsByLangAndGroup(@Param("idThesaurus") String idThesaurus, @Param("idGroup") String idGroup,
                                             @Param("idLang") String idLang);

    @Query(value = """
        SELECT COUNT(npt.id_term)
        FROM non_preferred_term npt, preferred_term pt
        WHERE pt.id_term = npt.id_term
            AND pt.id_thesaurus = npt.id_thesaurus
            AND npt.lang = :idLang
            AND npt.id_thesaurus = :idThesaurus
            AND pt.id_concept NOT IN (SELECT idconcept FROM concept_group_concept WHERE idthesaurus = :idThesaurus)
        """, nativeQuery = true)
    int countNonPreferredTermsNotInGroup(@Param("idLang") String idLang, @Param("idThesaurus") String idThesaurus);

    @Query(value = """
        SELECT COUNT(c.id_concept)
        FROM term t
        JOIN preferred_term pt ON t.id_thesaurus = pt.id_thesaurus AND t.id_term = pt.id_term
        JOIN concept c ON pt.id_concept = c.id_concept AND pt.id_thesaurus = c.id_thesaurus
        WHERE c.status != 'CA'
            AND t.id_thesaurus = :idThesaurus
            AND t.lang = :idLang
            AND c.id_concept NOT IN (SELECT idconcept FROM concept_group_concept WHERE idthesaurus = :idThesaurus)
    """, nativeQuery = true)
    int countConceptsWithoutGroupByLangAndThesaurus(@Param("idThesaurus") String idThesaurus, @Param("idLang") String idLang);

    @Query(value = """
        SELECT c.id_concept AS idConcept, t.lexical_value AS lexicalValue, c.created AS created, c.modified AS modified, u.username AS username
        FROM concept c
        JOIN concept_group_concept cgc ON c.id_concept = cgc.idconcept AND c.id_thesaurus = cgc.idthesaurus
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN term t ON pt.id_thesaurus = t.id_thesaurus AND pt.id_term = t.id_term
        JOIN users u ON t.contributor = u.id_user
        WHERE t.id_thesaurus = :idThesaurus AND t.lang = :idLang AND LOWER(cgc.idgroup) = LOWER(:idGroup)
        ORDER BY c.modified DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<ConceptGroupProjection> findConceptsByGroupAndLang(@Param("idThesaurus") String idThesaurus,
                                                            @Param("idLang") String idLang,
                                                            @Param("idGroup") String idGroup,
                                                            @Param("limit") int limit);

    @Query(value = """
        SELECT c.id_concept AS idConcept, t.lexical_value AS lexicalValue, c.created AS created, c.modified AS modified, u.username AS username
        FROM concept c
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN term t ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        JOIN users u ON t.contributor = u.id_user
        WHERE t.id_thesaurus = :idThesaurus AND t.lang = :idLang
        ORDER BY c.modified DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<ConceptGroupProjection> findRecentConceptsByLangAndThesaurus(@Param("idThesaurus") String idThesaurus,
            @Param("idLang") String idLang, @Param("limit") int limit);

    @Query(value = """
        SELECT c.id_concept AS idConcept, t.lexical_value AS lexicalValue, c.created AS created, c.modified AS modified, u.username AS username
        FROM concept c
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN term t ON pt.id_term = t.id_term AND pt.id_thesaurus = t.id_thesaurus
        JOIN users u ON t.contributor = u.id_user
        WHERE t.id_thesaurus = :idThesaurus AND t.lang = :idLang AND c.modified BETWEEN :dateDebut AND :dateFin
        ORDER BY c.modified DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<ConceptGroupProjection> findConceptsModifiedBetween(@Param("idThesaurus") String idThesaurus, @Param("idLang") String idLang,
            @Param("dateDebut") Date dateDebut, @Param("dateFin") Date dateFin, @Param("limit") int limit);

    @Query(value = """
        SELECT c.id_concept AS idConcept, t.lexical_value AS lexicalValue, c.created AS created, c.modified AS modified, u.username AS username
        FROM concept c
        JOIN concept_group_concept cgc ON c.id_concept = cgc.idconcept AND c.id_thesaurus = cgc.idthesaurus
        JOIN preferred_term pt ON c.id_concept = pt.id_concept AND c.id_thesaurus = pt.id_thesaurus
        JOIN term t ON pt.id_thesaurus = t.id_thesaurus AND pt.id_term = t.id_term
        JOIN users u ON t.contributor = u.id_user
        WHERE t.id_thesaurus = :idThesaurus AND t.lang = :idLang AND LOWER(cgc.idgroup) = LOWER(:idGroup) AND c.modified BETWEEN :dateDebut AND :dateFin
        ORDER BY c.modified DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<ConceptGroupProjection> findConceptsByGroupLangDate(@Param("idThesaurus") String idThesaurus, @Param("idLang") String idLang,
            @Param("idGroup") String idGroup, @Param("dateDebut") Date dateDebut, @Param("dateFin") Date dateFin, @Param("limit") int limit);

}
