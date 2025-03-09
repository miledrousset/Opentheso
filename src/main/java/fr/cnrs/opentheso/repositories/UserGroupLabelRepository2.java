package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.UserGroupLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserGroupLabelRepository2 extends JpaRepository<UserGroupLabel, Integer> {

    @Query("SELECT new fr.cnrs.opentheso.entites.UserGroupLabel(ugl.id, ugl.label) " +
            "FROM UserRoleGroup urg " +
            "JOIN UserGroupLabel ugl ON urg.group.id = ugl.id " +
            "WHERE urg.user.id = :idUser " +
            "AND urg.role.id = :idRole " +
            "ORDER BY ugl.label")
    List<UserGroupLabel> findProjectsByRole(@Param("idUser") int idUser, @Param("idRole") int idRole);

    Optional<UserGroupLabel> findByLabelLike(String label);
}
