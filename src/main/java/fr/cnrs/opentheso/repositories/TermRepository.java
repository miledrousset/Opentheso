package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Term;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TermRepository extends JpaRepository<Term, Integer> {

}
