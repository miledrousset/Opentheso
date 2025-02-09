package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.models.users.NodeUserGroupUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {

    List<User> findAllByUsernameLike(String username);

    Optional<User> findAllByUsername(String username);

    Optional<User> findByApiKey(String apiKey);

    Optional<User> findByMail(String mail);

    Optional<User> findByUsernameAndPassword(String username, String password);

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
}
