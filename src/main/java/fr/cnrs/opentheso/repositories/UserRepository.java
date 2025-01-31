package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.models.users.NodeUserGroupUser;
import fr.cnrs.opentheso.models.users.NodeUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserGroupUser(u.id, u.username, -1, '', 0, '') " +
            "FROM User u " +
            "WHERE u.isSuperAdmin != true " +
            "AND u.id NOT IN (SELECT urg.user.id FROM UserRoleGroup urg) " +
            "ORDER BY LOWER(u.username)")
    List<NodeUserGroupUser> getAllGroupUserWithoutGroup();

    @Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserGroupUser(" +
            "u.id, u.username, -1, '', 1, 'SuperAdmin') " +
            "FROM User u " +
            "WHERE u.isSuperAdmin = true " +
            "ORDER BY LOWER(u.username)")
    List<NodeUserGroupUser> getAllUsersSuperadmin();

    @Query(value = "SELECT u.id_user AS idUser, u.username AS userName, u.active, r.name AS roleName, r.id AS idRole, uro.id_theso AS idTheso " +
            "FROM users u " +
            "JOIN user_role_only_on uro ON u.id_user = uro.id_user " +
            "JOIN roles r ON uro.id_role = r.id " +
            "WHERE uro.id_group = :idGroup " +
            "AND u.issuperadmin != true " +
            "ORDER BY LOWER(u.username)", nativeQuery = true)
    List<NodeUserRole> findAllUsersRolesLimitedByTheso(@Param("idGroup") int idGroup);


    /*@Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserRole(u.idUser, u.username, u.active, r.id, r.name) " +
            "FROM UserRoleGroup urg " +
            "JOIN urg.user u " +
            "JOIN urg.role r " +
            "WHERE urg.groupId = :groupId " +
            "AND r.id >= :roleId " +
            "AND u.isSuperAdmin = false " +
            "ORDER BY LOWER(u.username)")
    List<NodeUserRole> findUsersRolesByGroupAndRole(@Param("groupId") int groupId, @Param("roleId") int roleId);*/

}
