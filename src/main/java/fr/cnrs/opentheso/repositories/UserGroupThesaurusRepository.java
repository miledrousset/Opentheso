package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Thesaurus;
import fr.cnrs.opentheso.entites.UserGroupThesaurus;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface UserGroupThesaurusRepository extends JpaRepository<UserGroupThesaurus, Integer> {

    List<UserGroupThesaurus> findAllByIdGroup(Integer idGroup);

    Optional<UserGroupThesaurus> findAllByThesaurus(Thesaurus thesaurus);

    @Modifying
    @Transactional
    void deleteByIdGroup(int groupId);

    @Modifying
    @Transactional
    void deleteByIdThesaurus(String thesaurusId);

    @Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus(ugt.idThesaurus, tl.title, ugt.idGroup, ugl.label, t.isPrivate, t.created) " +
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

    @Query("""
        SELECT ugt.idThesaurus
        FROM UserGroupThesaurus ugt
        JOIN Thesaurus t ON t.idThesaurus = ugt.idThesaurus
        WHERE ugt.idGroup = :idGroup
        AND (:isPrivate = TRUE OR t.isPrivate = FALSE)
        ORDER BY t.created DESC
    """)
    List<String> findThesaurusIdsByGroupAndVisibility(@Param("idGroup") int idGroup, @Param("isPrivate") boolean isPrivate);

    @Query("""
        SELECT DISTINCT ugt.idThesaurus 
        FROM UserGroupThesaurus ugt
        JOIN UserRoleGroup urg ON urg.group.id = ugt.idGroup
        WHERE urg.user.id = :idUser
        ORDER BY ugt.idThesaurus DESC
    """)
    List<String> findThesaurusIdsByUserId(@Param("idUser") int idUser);

    @Query("""
        SELECT DISTINCT ugt.idThesaurus 
        FROM UserGroupThesaurus ugt
        JOIN UserRoleGroup urg ON ugt.idGroup = urg.group.id
        WHERE urg.user.id = :idUser
        AND urg.role.id = 2
        ORDER BY ugt.idThesaurus DESC
    """)
    List<String> findThesaurusIdsWhereUserIsAdmin(@Param("idUser") int idUser);

    @Query("""
        SELECT ugt.idThesaurus 
        FROM UserGroupThesaurus ugt 
        WHERE ugt.idGroup = :groupId
    """)
    List<String> findThesaurusIdsByGroupId(@Param("groupId") int groupId);
}
