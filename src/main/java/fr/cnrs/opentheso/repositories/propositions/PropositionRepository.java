package fr.cnrs.opentheso.repositories.propositions;

import fr.cnrs.opentheso.entites.PropositionModification;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PropositionRepository extends JpaRepository<PropositionModification, Integer> {


}
