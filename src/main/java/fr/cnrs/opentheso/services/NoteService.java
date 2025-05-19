package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.repositories.NoteHistoriqueRepository;
import fr.cnrs.opentheso.repositories.NoteRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@AllArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteHistoriqueRepository noteHistoriqueRepository;


    public void deleteByThesaurus(String idThesaurus) {

        log.info("Suppression des notes de thesaurus: " + idThesaurus);
        noteRepository.deleteAllByIdThesaurus(idThesaurus);
        noteHistoriqueRepository.deleteAllByIdThesaurus(idThesaurus);

    }

    public void updateThesaurusId(String oldIdThesaurus, String newIdThesaurus) {

        log.info("Mise à jour des thésaurus id pour les notes présents dnas le thésaurus: " + oldIdThesaurus);
        noteRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
        noteHistoriqueRepository.updateThesaurusId(newIdThesaurus, oldIdThesaurus);
    }

}
