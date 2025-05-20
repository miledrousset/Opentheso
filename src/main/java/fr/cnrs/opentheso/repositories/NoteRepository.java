package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface NoteRepository extends JpaRepository<Note, Integer> {

    List<Note> findAllByIdConceptAndIdThesaurus(String idConcept, String idThesaurus);

    List<Note> findAllByIdentifierAndIdThesaurus(String identifier, String idThesaurus);

    Optional<Note> findByIdAndIdThesaurus(int id, String idThesaurus);

    Optional<Note> findAllByIdentifierAndIdThesaurusAndNotetypecodeAndLang(String identifier, String idThesaurus, String notetypecode, String lang);

    @Modifying
    void deleteAllByIdThesaurus(String idThesaurus);

    @Modifying
    void deleteByIdAndIdThesaurus(int idNote, String idThesaurus);

    @Modifying
    void deleteAllByIdentifierAndIdThesaurus(String identifier, String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE Note t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);
}
