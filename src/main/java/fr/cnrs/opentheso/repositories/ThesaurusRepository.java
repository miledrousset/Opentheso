package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Thesaurus;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface ThesaurusRepository extends JpaRepository<Thesaurus, String> {

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

    @Query("SELECT new fr.cnrs.opentheso.models.thesaurus.Thesaurus(" +
            "t.idThesaurus, t.idArk, t.isPrivate, tl.title, tl.lang, tl.contributor, tl.coverage, tl.creator, " +
            "tl.description, tl.format, tl.publisher, tl.relation, tl.rights, tl.source, tl.subject, tl.type) " +
            "FROM Thesaurus t " +
            "JOIN ThesaurusLabel tl ON t.idThesaurus = tl.idThesaurus " +
            "WHERE t.idThesaurus = :idTheso AND tl.lang = :idLang")
    Optional<fr.cnrs.opentheso.models.thesaurus.Thesaurus> getThesaurusByIdAndLang(@Param("idTheso") String idTheso, @Param("idLang") String idLang);
}
