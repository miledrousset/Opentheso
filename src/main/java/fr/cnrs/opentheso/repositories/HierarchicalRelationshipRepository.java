package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.HierarchicalRelationship;
import org.springframework.data.jpa.repository.JpaRepository;


public interface HierarchicalRelationshipRepository extends JpaRepository<HierarchicalRelationship, Integer> {
}
