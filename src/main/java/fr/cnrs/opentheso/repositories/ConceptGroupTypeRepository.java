package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptGroupType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface ConceptGroupTypeRepository extends JpaRepository<ConceptGroupType, Integer> {

    Optional<ConceptGroupType> findByCode(String code);

}
