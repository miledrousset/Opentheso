package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.NoteType;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NoteTypeRepository extends JpaRepository<NoteType, Integer> {

}
