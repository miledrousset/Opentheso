package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Status;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StatusRepository extends JpaRepository<Status, Integer> {

}
