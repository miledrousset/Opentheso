package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.UserGroupThesaurus;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface UserGroupThesaurusRepository extends JpaRepository<UserGroupThesaurus, Integer> {

    List<UserGroupThesaurus> findAllByIdGroup(Integer idGroup);

    @Modifying
    @Transactional
    void deleteByIdGroup(int groupId);

    @Modifying
    @Transactional
    void deleteByIdThesaurus(String thesaurusId);

    @Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus(ugt.idThesaurus, tl.title, ugt.idGroup, ugl.label, t.isPrivate) " +
            "FROM UserGroupThesaurus ugt " +
            "JOIN Thesaurus t ON ugt.idThesaurus = t.idThesaurus " +
            "JOIN ThesaurusLabel tl ON ugt.idThesaurus = tl.idThesaurus " +
            "JOIN UserGroupLabel ugl ON ugt.idGroup = ugl.id " +
            "WHERE tl.lang = :idLang " +
            "ORDER BY LOWER(tl.title)")
    List<NodeUserGroupThesaurus> getAllGroupTheso(@Param("idLang") String idLang);

    @Modifying
    @Transactional
    @Query("UPDATE UserGroupThesaurus t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

}
