package fr.cnrs.opentheso.bean.condidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;

public class NoteDao extends BasicDao {

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

    public void SaveNote(HikariDataSource hikariDataSource, String noteType, String noteValue, String idTerme, String idConcepte,
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
}
