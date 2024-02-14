package fr.cnrs.opentheso.bean.candidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoteDao extends BasicDao {

    
    /**
     * Permet de récupérer toutes les notes d'un candidat
     * @param hikariDataSource
     * @param idconcept
     * @param idThesaurus
     * @return 
     */
    public List<NodeNote> getNotesCandidat(HikariDataSource hikariDataSource,
            String idconcept, String idThesaurus) {
        
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
                nodeNote.setId_note(resultSet.getInt("id"));
                nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
                nodeNote.setId_concept(idconcept);                
                nodeNote.setLang(resultSet.getString("lang"));
                nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                nodeNote.setIdUser(resultSet.getInt("id_user"));
                
                nodeNotes.add(nodeNote);
                
              ///  definitions.add(resultSet.getString("lexicalvalue").replaceAll("(\r\n|\n)", "<br />"));
            }
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return nodeNotes;
    }        
    
    public String getNoteCandidat(HikariDataSource hikariDataSource, String idconcept, String idThesaurus, 
            String noteType, String lang) {
        
        String definition = null;
        try {

            openDataBase(hikariDataSource);
            stmt.executeQuery(new StringBuffer("SELECT lexicalvalue FROM note WHERE notetypecode = '")
                    .append(noteType).append("' AND id_concept = '").append(idconcept)
                    .append("' AND id_thesaurus = '").append(idThesaurus).append("' AND lang='")
                    .append(lang).append("'").toString());
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                definition = resultSet.getString("lexicalvalue");
            }
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return definition;
    }

    /// déprécié par Miled
    /*
    public List<String> getNotesCandidat(HikariDataSource hikariDataSource, String idconcept, String idThesaurus, 
            String noteType, String lang) {
        
        List<String> definitions = new ArrayList<>();
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery(new StringBuffer("SELECT lexicalvalue FROM note WHERE notetypecode = '")
                    .append(noteType).append("' AND id_concept = '").append(idconcept)
                    .append("' AND id_thesaurus = '").append(idThesaurus).append("' AND lang='")
                    .append(lang).append("'").toString());
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                definitions.add(resultSet.getString("lexicalvalue").replaceAll("(\r\n|\n)", "<br />"));
            }
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return definitions;
    }*/

    public void saveNote(HikariDataSource hikariDataSource, String noteType, String noteValue, String idTerme, String idConcepte,
                         String idThesaurus, String lang) {
        try {
            openDataBase(hikariDataSource);
            stmt.executeUpdate(new StringBuffer("INSERT INTO note(notetypecode, id_thesaurus, id_term, id_concept, lexicalvalue, lang) ")
                    .append("VALUES ('").append(noteType).append("', '").append(idThesaurus).append("', '")
                    .append(idTerme).append("', '").append(idConcepte).append("', '")
                    .append(noteValue).append("', '").append(lang).append("')").toString());
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public void updateNote(HikariDataSource hikariDataSource, String noteType, String noteValue, String idTerme,
            String idConcepte, String idThesaurus, String lang) {
        try {
            openDataBase(hikariDataSource);
            stmt.executeUpdate(new StringBuffer("UPDATE note SET lexicalvalue='").append(noteValue)
                    .append("' AND lang = '").append(lang).append("' WHERE notetypecode= '").append(noteType)
                    .append("' AND id_thesaurus='").append(idThesaurus).append("' AND id_term='")
                    .append(idTerme).append("' AND id_concept='").append(idConcepte).append("'").toString());
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
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

    public void deleteAllNoteByConceptAndThesaurusAndType(HikariDataSource hikariDataSource, String noteType,
                                                          String idConcepte, String idThesaurus) {
        try {
            openDataBase(hikariDataSource);
            stmt.executeUpdate(new StringBuffer("DELETE FROM note WHERE notetypecode= '").append(noteType).append("' AND id_thesaurus='")
                    .append(idThesaurus).append("' AND id_concept='").append(idConcepte).append("'").toString());
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }
}
