package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Roles;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Roles, Integer> {

}
