package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Integer> {

}
