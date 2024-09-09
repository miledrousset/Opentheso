package fr.cnrs.opentheso.repositories.candidats;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.models.notes.NodeNote;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoteDao extends BasicDao {

    
    /**
     * Permet de récupérer toutes les notes d'un candidat
     */
    public List<NodeNote> getNotesCandidat(HikariDataSource hikariDataSource, String idconcept, String idThesaurus) {
        
        List<NodeNote> nodeNotes = new ArrayList<>();
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery(new StringBuffer("SELECT id, notetypecode, identifier, lang, lexicalvalue, id_user FROM note WHERE ")
                    .append(" (identifier = '").append(idconcept)
                    .append("') AND id_thesaurus = '").append(idThesaurus).append("'")
                    .toString());
            
            resultSet = stmt.getResultSet();
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
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return nodeNotes;
    }

    public void deleteNote(HikariDataSource hikariDataSource, String noteType, String noteValue, 
            String idTerme, String idConcepte, String idThesaurus) {
        try {
            openDataBase(hikariDataSource);
            stmt.executeUpdate(new StringBuffer("DELETE FROM note WHEN lexicalvalue='").append(noteValue)
                    .append("' AND notetypecode= '").append(noteType).append("' AND id_thesaurus='")
                    .append(idThesaurus).append("' AND id_term='").append(idTerme)
                    .append("' AND id_concept='").append(idConcepte).append("'").toString());
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }
}
