package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Note;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NoteRepository extends JpaRepository<Note, Integer> {

}
