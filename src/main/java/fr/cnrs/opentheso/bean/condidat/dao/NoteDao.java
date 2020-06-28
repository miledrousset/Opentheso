package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class NoteDao extends BasicDao{

    public String getNoteCandidat(Statement stmt, String idconcept, String idThesaurus, String noteType) {
        String definition = null;
        try {
            stmt.executeQuery("SELECT lexicalvalue FROM note WHERE notetypecode = '" + noteType + "' AND id_concept = '"
                    + idconcept + "' AND id_thesaurus = '" + idThesaurus + "'");
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                definition = resultSet.getString("lexicalvalue");
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
        return definition;
    }

    public void SaveNote(Connect connect, String noteType, String noteValue, String idTerme, String idConcepte,
                         String idThesaurus, String lang) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeUpdate("INSERT INTO note(notetypecode, id_thesaurus, id_term, id_concept, lexicalvalue, lang) " +
                    "VALUES ('"+noteType+"', '"+idThesaurus+"', '"+idTerme+"', '"+idConcepte +"', '"+noteValue+"', '"+lang+"')");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println("Erreur : " + e);
        }
    }

    public void updateNote(Connect connect, String noteType, String noteValue, String idTerme,
            String idConcepte, String idThesaurus, String lang) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeUpdate("UPDATE note SET lexicalvalue='"+noteValue+"' AND lang = '"+lang+"' WHERE notetypecode= '"
                    +noteType +"' AND id_thesaurus='"+idThesaurus+"' AND id_term='"+idTerme+"' AND id_concept='"+idConcepte+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public void deleteNote(Connect connect, String noteType, String noteValue, String idTerme, String idConcepte, String idThesaurus) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeUpdate("DELETE FROM note WHEN lexicalvalue='"+noteValue+"' AND notetypecode= '" +noteType
                    +"' AND id_thesaurus='"+idThesaurus+"' AND id_term='"+idTerme+"' AND id_concept='"+idConcepte+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }
}
