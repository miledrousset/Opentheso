package fr.cnrs.opentheso.repositories.candidats;

import fr.cnrs.opentheso.models.notes.NodeNote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class NoteDao {

    @Autowired
    private DataSource dataSource;

    
    /**
     * Permet de récupérer toutes les notes d'un candidat
     */
    public List<NodeNote> getNotesCandidat(String idconcept, String idThesaurus) {
        
        List<NodeNote> nodeNotes = new ArrayList<>();
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()){
            stmt.executeQuery(new StringBuffer("SELECT id, notetypecode, identifier, lang, lexicalvalue, id_user FROM note WHERE ")
                    .append(" (identifier = '").append(idconcept)
                    .append("') AND id_thesaurus = '").append(idThesaurus).append("'")
                    .toString());
            
            var resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                NodeNote nodeNote = new NodeNote();
                nodeNote.setIdNote(resultSet.getInt("id"));
                nodeNote.setNoteTypeCode(resultSet.getString("notetypecode"));
                nodeNote.setIdConcept(idconcept);
                nodeNote.setLang(resultSet.getString("lang"));
                nodeNote.setLexicalValue(resultSet.getString("lexicalvalue"));
                nodeNote.setIdUser(resultSet.getInt("id_user"));
                
                nodeNotes.add(nodeNote);
            }
        } catch (SQLException e) {
            log.error(e.toString());
        }
        return nodeNotes;
    }

    public void deleteNote(String noteType, String noteValue, String idTerme, String idConcepte, String idThesaurus) {
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()){
            stmt.executeUpdate(new StringBuffer("DELETE FROM note WHEN lexicalvalue='").append(noteValue)
                    .append("' AND notetypecode= '").append(noteType).append("' AND id_thesaurus='")
                    .append(idThesaurus).append("' AND id_term='").append(idTerme)
                    .append("' AND id_concept='").append(idConcepte).append("'").toString());
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }
}
