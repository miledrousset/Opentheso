package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Thesaurus;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ThesaurusRepository extends JpaRepository<Thesaurus, String> {

    @Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus(" +
            "t.idThesaurus, tl.title, -1, '', t.isPrivate) " +
            "FROM ThesaurusLabel tl " +
            "JOIN Thesaurus t ON tl.idThesaurus = t.idThesaurus " +
            "WHERE tl.lang = :idLang " +
            "AND tl.idThesaurus NOT IN (SELECT ugt.idThesaurus FROM UserGroupThesaurus ugt) " +
            "ORDER BY LOWER(tl.title)")
    List<NodeUserGroupThesaurus> getAllThesaurusWithoutGroup(@Param("idLang") String idLang);

}
