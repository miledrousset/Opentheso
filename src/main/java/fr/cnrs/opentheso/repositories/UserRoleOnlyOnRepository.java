package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Roles;
import fr.cnrs.opentheso.entites.Thesaurus;
import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.entites.UserRoleOnlyOn;
import fr.cnrs.opentheso.models.users.NodeUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface UserRoleOnlyOnRepository extends JpaRepository<UserRoleOnlyOn, Integer> {

    @Query("SELECT new fr.cnrs.opentheso.models.users.NodeUserRole(u.theso.idThesaurus, r.name, r.id) " +
            "FROM UserRoleOnlyOn u " +
            "JOIN u.role r " +
            "WHERE u.group.id = :idGroup " +
            "AND u.user.id = :idUser " +
            "ORDER BY LOWER(u.theso.idThesaurus)")
    List<NodeUserRole> getListRoleByThesoLimited(@Param("idGroup") int idGroup, @Param("idUser") int idUser);

    @Transactional
    void deleteByUserAndGroup(User user, UserGroupLabel group);

    @Transactional
    void deleteByUserAndGroupAndRoleAndTheso(User user, UserGroupLabel group, Roles role, Thesaurus theso);

    List<NodeUserRole> findAllByUserOrderByTheso(User user);

    UserRoleOnlyOn findByUserAndGroupAndTheso(User user, UserGroupLabel group, Thesaurus theso);
}
