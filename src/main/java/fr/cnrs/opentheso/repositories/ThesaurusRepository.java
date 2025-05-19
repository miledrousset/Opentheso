package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Thesaurus;
import fr.cnrs.opentheso.models.NodeLangThesaurusProjection;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface ThesaurusRepository extends JpaRepository<Thesaurus, String> {

    List<Thesaurus> findAllByIsPrivateFalse();

    @Query(value = "SELECT id_thesaurus FROM thesaurus WHERE REPLACE(id_ark, '-', '') = REPLACE(:arkId, '-', '') LIMIT 1", nativeQuery = true)
    Optional<String> findIdThesaurusByArkId(@Param("arkId") String arkId);

    @Query(value = "SELECT nextval('thesaurus_id_seq')", nativeQuery = true)
    Long getNextThesaurusSequenceValue();

    @Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus(" +
            "t.idThesaurus, tl.title, -1, '', t.isPrivate) " +
            "FROM ThesaurusLabel tl " +
            "JOIN Thesaurus t ON tl.idThesaurus = t.idThesaurus " +
            "WHERE tl.lang = :idLang " +
            "AND tl.idThesaurus NOT IN (SELECT ugt.idThesaurus FROM UserGroupThesaurus ugt) " +
            "ORDER BY LOWER(tl.title)")
    List<NodeUserGroupThesaurus> getAllThesaurusWithoutGroup(@Param("idLang") String idLang);

    @Modifying
    @Transactional
    @Query("UPDATE Thesaurus t SET t.isPrivate = :isPrivate WHERE t.idThesaurus = :idTheso")
    int updateVisibility(@Param("idTheso") String idTheso, @Param("isPrivate") boolean isPrivate);

    @Modifying
    @Transactional
    @Query("UPDATE Thesaurus t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

    @Query("SELECT new fr.cnrs.opentheso.models.thesaurus.Thesaurus(" +
            "t.idThesaurus, t.idArk, t.isPrivate, tl.title, tl.lang, tl.contributor, tl.coverage, tl.creator, " +
            "tl.description, tl.format, tl.publisher, tl.relation, tl.rights, tl.source, tl.subject, tl.type) " +
            "FROM Thesaurus t " +
            "JOIN ThesaurusLabel tl ON t.idThesaurus = tl.idThesaurus " +
            "WHERE t.idThesaurus = :idTheso AND tl.lang = :idLang")
    Optional<fr.cnrs.opentheso.models.thesaurus.Thesaurus> getThesaurusByIdAndLang(@Param("idTheso") String idTheso, @Param("idLang") String idLang);

    @Query(value = """
        SELECT ROW_NUMBER() OVER () AS id, l.iso639_1 AS code, l.code_pays AS codeFlag, l.french_name AS frenchName, 
                   l.english_name AS englishName, tl.title AS labelTheso
        FROM thesaurus_label tl
        JOIN languages_iso639 l ON tl.lang = l.iso639_1
        WHERE tl.id_thesaurus = :idThesaurus
        ORDER BY l.french_name
        """, nativeQuery = true)
    List<NodeLangThesaurusProjection> findAllUsedLanguagesOfThesaurus(@Param("idThesaurus") String idThesaurus);

}
