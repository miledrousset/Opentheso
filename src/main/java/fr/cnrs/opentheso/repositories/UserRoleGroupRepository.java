package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.dto.GroupInfoDTO;
import fr.cnrs.opentheso.entites.UserRoleGroup;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserRoleGroupRepository extends JpaRepository<UserRoleGroup, Integer> {

    //@Query("SELECT urg FROM UserRoleGroup urg WHERE urg.user.id = :userId AND urg.group.id = :groupId AND urg.role.id < 3")
    //Optional<UserRoleGroup> findByUserIdAndGroupIdAndRoleGreaterThan(@Param("userId") int userId, @Param("groupId") int groupId);


    @Query(value = "SELECT DISTINCT ugr.id_group AS idGroup, u.label_group AS labelGroup " +
            "FROM user_group_label u " +
            "LEFT JOIN user_role_group ugr ON ugr.id_group = u.id_group " +
            "LEFT JOIN user_role_only_on uro ON uro.id_group = u.id_group " +
            "WHERE (ugr.id_user = :idUser OR uro.id_user = :idUser) " +
            "ORDER BY u.label_group",
            nativeQuery = true)
    List<GroupInfoDTO> findGroupsOfUser(@Param("idUser") int idUser);

    @Query(value = "SELECT r.id AS idRole, r.name AS roleName, u.label_group AS groupName, u.id_group AS idGroup " +
            "FROM user_role_group ur " +
            "JOIN roles r ON ur.id_role = r.id " +
            "JOIN user_group_label u ON ur.id_group = u.id_group " +
            "WHERE ur.id_user = :idUser AND ur.id_group = :idGroup", nativeQuery = true)
    Optional<NodeUserRoleGroup> findUserRoleOnThisGroup(@Param("idUser") int idUser, @Param("idGroup") int idGroup);

}
