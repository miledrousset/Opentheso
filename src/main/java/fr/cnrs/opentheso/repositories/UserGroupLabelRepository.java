package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.UserGroupLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;



public interface UserGroupLabelRepository extends JpaRepository<UserGroupLabel, Integer> {

    @Query("SELECT new fr.cnrs.opentheso.entites.UserGroupLabel(ugl.id, ugl.label) " +
            "FROM UserRoleGroup urg " +
            "JOIN UserGroupLabel ugl ON urg.group.id = ugl.id " +
            "WHERE urg.user.id = :idUser " +
            "AND urg.role.id = :idRole " +
            "ORDER BY ugl.label")
    List<UserGroupLabel> findProjectsByRole(@Param("idUser") int idUser, @Param("idRole") int idRole);

    @Query("""
        SELECT new fr.cnrs.opentheso.entites.UserGroupLabel(ugl.id, ugl.label)
        FROM UserGroupLabel ugl
        WHERE ugl.id IN (
            SELECT urg.group.id FROM UserRoleGroup urg WHERE urg.user.id = :userId
            UNION
            SELECT uro.group.id FROM UserRoleOnlyOn uro WHERE uro.user.id = :userId
        )
        ORDER BY LOWER(ugl.label)
    """)
    List<UserGroupLabel> findAllGroupsOfUser(@Param("userId") int userId);

    Optional<UserGroupLabel> findByLabelLike(String label);

    @Query("SELECT DISTINCT new fr.cnrs.opentheso.entites.UserGroupLabel(ugl.id, ugl.label) " +
            "FROM UserRoleGroup urg " +
            "JOIN urg.group ugl " +
            "WHERE urg.user.id = :idUser " +
            "ORDER BY lower(ugl.label)")
    List<UserGroupLabel> findProjectsByUserId(@Param("idUser") int userId);

    @Query("""
        SELECT DISTINCT new fr.cnrs.opentheso.entites.UserGroupLabel(lab.id, lab.label)
        FROM UserGroupThesaurus ugt
        JOIN ugt.groupLabel lab
        JOIN ugt.thesaurus th
        WHERE th.isPrivate = :status""")
    List<UserGroupLabel> findProjectsByThesaurusStatus(@Param("status") boolean status);
}
