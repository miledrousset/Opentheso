package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Roles;
import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.entites.UserRoleGroup;
import fr.cnrs.opentheso.models.users.NodeUserGroup;
import fr.cnrs.opentheso.models.users.NodeUserGroupUser;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface UserRoleGroupRepository extends JpaRepository<UserRoleGroup, Integer> {

    @Transactional
    void deleteByUserAndGroup(User user, UserGroupLabel group);

    @Transactional
    void deleteByGroup(UserGroupLabel group);

    @Transactional
    @Modifying
    @Query("UPDATE UserRoleGroup urg SET urg.role = :newRole WHERE urg.user = :user AND urg.group = :group")
    void updateUserRole(@Param("user") User user, @Param("group") UserGroupLabel group, @Param("newRole") Roles newRole);

    @Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserRoleGroup(" +
            "urg.role.id, r.name, urg.group.id, g.label, " +
            "CASE WHEN urg.role.id = 2 THEN true ELSE false END, " +
            "CASE WHEN urg.role.id = 3 THEN true ELSE false END, " +
            "CASE WHEN urg.role.id = 4 THEN true ELSE false END) " +
            "FROM UserRoleGroup urg " +
            "JOIN urg.role r " +
            "JOIN urg.group g " +
            "WHERE urg.user.id = :idUser")
    List<NodeUserRoleGroup> getUserRoleGroup(@Param("idUser") int idUser);

    @Query(value = "SELECT new fr.cnrs.opentheso.models.users.NodeUserRoleGroup(r.id, r.name, u.label, u.id) " +
            "FROM UserRoleGroup ur " +
            "JOIN Roles r ON ur.role.id = r.id " +
            "JOIN UserGroupLabel u ON ur.group.id = u.id " +
            "WHERE ur.user.id = :idUser AND ur.group.id = :idGroup")
    Optional<NodeUserRoleGroup> findUserRoleOnThisGroup(@Param("idUser") int idUser, @Param("idGroup") int idGroup);

    @Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserGroupUser(u.id, u.username, g.id, g.label, r.id, r.name) " +
            "FROM UserRoleGroup urg " +
            "JOIN urg.user u " +
            "JOIN urg.group g " +
            "JOIN urg.role r " +
            "ORDER BY LOWER(u.username)")
    List<NodeUserGroupUser> getAllGroupUser();

    @Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserGroup(ugl.id, ugl.label) " +
            "FROM UserRoleGroup urg JOIN urg.group ugl " +
            "WHERE urg.user.id = :idUser " +
            "AND urg.role.id = 2 " +
            "AND LOWER(ugl.label) LIKE LOWER(CONCAT('%', :projectName, '%')) " +
            "ORDER BY ugl.label")
    List<NodeUserGroup> findGroupByUserAndProject(@Param("idUser") int idUser, @Param("projectName") String projectName);

}
