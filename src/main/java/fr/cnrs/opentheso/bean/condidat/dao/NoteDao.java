package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NoteDao {

    private final Log LOG = LogFactory.getLog(NoteDao.class);

    public String getNoteCandidat(Statement stmt, String idconcept, String idThesaurus, String noteType) {
        String definition = null;
        try {
            stmt.executeQuery("SELECT lexicalvalue FROM note "
                    + "WHERE notetypecode = '" + noteType + "' "
                    + "AND id_concept = '" + idconcept + "' AND "
                    + "id_thesaurus = '" + idThesaurus + "'");
            ResultSet resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                definition = resultSet.getString("lexicalvalue");
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
        return definition;
    }

    public void SaveNote(Connect connect, Statement stmt, String noteType, String noteValue, String idTerme, 
            String idConcepte, String lang, String idThesaurus) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeUpdate("INSERT INTO public.note(notetypecode, id_thesaurus, id_term, "
                    + "id_concept, lang, lexicalvalue) VALUES ('"+noteType+"', '"+idThesaurus+"', "+idTerme+"', '"+idConcepte
                    +"', '"+lang+"', '"+noteValue+"')");
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public void updateNote(Connect connect, Statement stmt, String noteType, String noteValue, String idTerme, 
            String idConcepte, String lang, String idThesaurus) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeUpdate("UPDATE public.note SET lexicalvalue='"+noteValue+"' WHERE notetypecode= '"
                    +noteType+"' AND id_thesaurus='"+idThesaurus+"' AND id_term='"
                    +idTerme+"' AND id_concept='"+idConcepte+"' AND lang='"+lang+"'");
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }
}
