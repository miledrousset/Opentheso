package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Permuted;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PermutedRepository extends JpaRepository<Permuted, Integer> {

}
