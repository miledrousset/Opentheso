package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface RoleRepository extends JpaRepository<Roles, Integer> {

    List<Roles> findAllByIdGreaterThanEqual(Integer roleId);

}
