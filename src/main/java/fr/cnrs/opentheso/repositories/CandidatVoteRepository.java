package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.CandidatVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


public interface CandidatVoteRepository extends JpaRepository<CandidatVote, Integer> {

    List<CandidatVote> findAllByIdConceptAndIdThesaurus(String idConcept, String idThesaurus);

    List<CandidatVote> findAllByIdConceptAndIdThesaurusAndTypeVote(String idConcept, String idThesaurus, String typeVote);

    List<CandidatVote> findAllByIdConceptAndIdThesaurusAndIdUserAndTypeVote(String idConcept, String idThesaurus,
                                                                            Integer idUser, String typeVote);

    List<CandidatVote> findAllByIdConceptAndIdThesaurusAndIdUserAndIdNoteAndTypeVote(String idConcept, String idThesaurus,
                                                                                     Integer idUser, String idNote, String typeVote);
    @Modifying
    @Transactional
    void deleteAllByIdUserAndIdConceptAndIdThesaurusAndTypeVoteAndIdNote(Integer user, String idConcept, String idThesaurus,
                                                                         String typeVote, String idNote);
}
