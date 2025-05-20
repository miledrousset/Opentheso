package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.Note;
import fr.cnrs.opentheso.entites.NoteHistorique;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.repositories.NoteHistoriqueRepository;
import fr.cnrs.opentheso.repositories.NoteRepository;
import fr.cnrs.opentheso.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteHistoriqueRepository noteHistoriqueRepository;


    public boolean updateNote(int idNote, String idConcept, String idLang, String idThesaurus,
                              String note, String noteSource, String noteTypeCode, int idUser) {

        log.info("Mise à jour de la note id {}", idNote);
        var noteValue = noteRepository.findByIdAndIdThesaurus(idNote, idThesaurus);
        if (noteValue.isEmpty()) {
            log.info("Aucune note n'existe pas l'id {}", idNote);
            return true;
        }

        log.info("Mise à jour de la note id {} ", idNote);
        noteValue.get().setLexicalvalue(note);
        noteValue.get().setNotesource(StringUtils.convertString(noteSource));
        noteValue.get().setModified(new Date());
        noteRepository.save(noteValue.get());

        idLang = normalizeIdLang(idLang);
        addConceptNoteHistorique(idConcept, idLang, idThesaurus, note, noteTypeCode, "update", idUser);
        return false;
    }

    public void addConceptNoteHistorique(String idConcept, String idLang, String idThesausus, String note,
                                         String noteTypeCode, String actionPerformed, int idUser) {

        log.info("Ajout de l'historique des actions sur les notes");
        noteHistoriqueRepository.save(NoteHistorique.builder()
                .idThesaurus(idThesausus)
                .idConcept(idConcept)
                .lang(idLang)
                .lexicalvalue(fr.cnrs.opentheso.utils.StringUtils.convertString(note))
                .actionPerformed(actionPerformed)
                .idUser(idUser)
                .notetypecode(noteTypeCode)
                .build());
    }

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

    public List<NodeNote> getNotesCandidat(String idConcept, String idThesaurus) {

        log.info("Recherche des notes du candidat {} (id thésaurus {})", idConcept, idThesaurus);
        var notes = noteRepository.findAllByIdConceptAndIdThesaurus(idConcept, idThesaurus);
        if (notes.isEmpty()) {
            log.info("Aucune note trouvée pour le candidat {} (id thésaurus {})", idConcept, idThesaurus);
        }

        log.info("{} notes trouvées pour le candidat {} (id thésaurus {})", notes.size(), idConcept, idThesaurus);
        return notes.stream()
                .map(note -> NodeNote.builder()
                        .idNote(note.getId())
                        .noteTypeCode(note.getNotetypecode())
                        .idConcept(note.getIdConcept())
                        .lang(note.getLang())
                        .lexicalValue(note.getLexicalvalue())
                        .idUser(note.getIdUser())
                        .build())
                .toList();
    }

    public boolean addNote(String identifier, String idLang, String idThesaurus, String note, String noteTypeCode, String noteSource, int idUser) {

        idLang = normalizeIdLang(idLang);
        if(isNoteExistInThatLang(identifier, idThesaurus, idLang, noteTypeCode)) {
            log.info("Mise à jour d'une note existante");
            var noteToUpdate = noteRepository.findAllByIdentifierAndIdThesaurusAndNotetypecodeAndLang(identifier, idThesaurus, noteTypeCode, idLang);
            if (noteToUpdate.isPresent()) {
                noteToUpdate.get().setLexicalvalue(note);
                noteToUpdate.get().setNotesource(noteSource);
                noteRepository.save(noteToUpdate.get());
            }
        } else {
            log.info("Enregistrement d'une nouvelle note");
            note = StringUtils.clearValue(note);
            note = StringEscapeUtils.unescapeXml(note);
            note = StringUtils.convertString(note);
            noteRepository.save(Note.builder()
                    .notetypecode(noteTypeCode)
                    .idThesaurus(idThesaurus)
                    .lang(idLang)
                    .lexicalvalue(note)
                    .identifier(identifier)
                    .notesource(StringUtils.convertString(noteSource))
                    .idUser(idUser)
                    .build());
        }

        return true;
    }

    public boolean isNoteExistInThatLang(String identifier, String idThesaurus, String idLang, String noteTypeCode) {

        log.info("Vérification l'existance du note {} dans le thésaurus id {}", identifier, idThesaurus);
        idLang = normalizeIdLang(idLang);
        var note = noteRepository.findAllByIdentifierAndIdThesaurusAndNotetypecodeAndLang(identifier, idThesaurus, noteTypeCode, idLang);
        return note.isPresent();
    }

    public void deleteNotes(String identifier, String idThesaurus) {

        log.info("Suppression du note id {} contenu dans le thésaurus id {}", identifier, idThesaurus);
        noteRepository.deleteAllByIdentifierAndIdThesaurus(identifier, idThesaurus);
    }

    private String normalizeIdLang(String idLang){
        return switch (idLang) {
            case "en-GB" -> "en";
            case "en-US" -> "en";
            case "pt-BR" -> "pt";
            case "pt-PT" -> "pt";
            default -> idLang;
        };
    }

    public List<NodeNote> getListNotesAllLang(String identifier, String idThesaurus) {

        log.info("Rechercher la liste des notes dans tous les langues avec l'identifier {}", identifier);
        var notes = noteRepository.findAllByIdentifierAndIdThesaurus(identifier, idThesaurus);
        if (notes.isEmpty()) {
            log.info("Aucune note n'est trouvée");
            return List.of();
        }

        log.info("{} notes trouvées pour le candidat {}", notes.size(), idThesaurus);
        return notes.stream()
                .map(element -> NodeNote.builder()
                        .idTerm(identifier)
                        .idNote(element.getId())
                        .lang(element.getLang())
                        .lexicalValue(element.getLexicalvalue())
                        .modified(element.getModified())
                        .created(element.getCreated())
                        .noteTypeCode(element.getNotetypecode())
                        .noteSource(element.getNotesource())
                        .identifier(element.getIdentifier())
                        .build())
                .toList();
    }

    public int getNoteByValueAndThesaurus(String value, String noteTypeCode, String idLang, String idThesaurus) {

        log.info("Recherche de l'Id note par value {}, lang {}, idThesaurus {} et NoteType {}", value, idThesaurus, noteTypeCode, idLang);
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        var note = noteRepository.findAllByIdentifierAndIdThesaurusAndNotetypecodeAndLang(value, idThesaurus, noteTypeCode, idLang);
        if (note.isEmpty()) {
            log.info("Aucune note n'est trouvée");
            return -1;
        }

        return note.get().getId();
    }

    public NodeNote getNoteByIdNote(int idNote) {

        log.info("Recherche de la note avec id {}", idNote);
        var note = noteRepository.findById(idNote);
        if (note.isEmpty()) {
            log.info("Aucune note n'existe avec l'id {}", idNote);
            return null;
        }

        return NodeNote.builder()
                .idNote(note.get().getId())
                .lexicalValue(note.get().getLexicalvalue())
                .modified(note.get().getModified())
                .created(note.get().getCreated())
                .noteTypeCode(note.get().getNotetypecode())
                .lang(note.get().getLang())
                .build();
    }

    public void deleteThisNote(int idNote, String identifier, String idLang, String idThesaurus, String noteTypeCode,
                                  String oldNote, int idUser) {

        log.info("Suppression de la note id {}", idNote);
        noteRepository.deleteByIdAndIdThesaurus(idNote, idThesaurus);
        addConceptNoteHistorique(identifier, idLang, idThesaurus, oldNote, noteTypeCode, "delete", idUser);
    }

}
