package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class IntituleDao {

    private final Log LOG = LogFactory.getLog(IntituleDao.class);
    
    
    public void saveIntitule(Connect connect, Statement stmt, String intitule, 
            String idThesaurus, String lang, String idConcept) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            String idTerme = getIdTerme(stmt, idConcept, idThesaurus);
            stmt.executeUpdate("INSERT INTO public.non_preferred_term(lexical_value, lang, id_thesaurus, hiden, id_term) VALUES ('"
                    +intitule+"', '"+lang+"', '"+idThesaurus+"', false, '"+idTerme+"');");
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    public void updateIntitule(Connect connect, Statement stmt, String intitule, 
            String idThesaurus, String lang, String idConcept) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            String idTerme = getIdTerme(stmt, idConcept, idThesaurus);
            stmt.executeUpdate("UPDATE non_preferred_term SET lexical_value = '"+intitule+"', lang='"+lang+"' WHERE id_term ='"+idTerme+"'");
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    private String getIdTerme(Statement stmt, String idConcept, String idThesaurus) throws SQLException {
        String idTerme = null;
        stmt.executeQuery("SELECT id_term FROM public.preferred_term WHERE id_concept = '"+idConcept
                +"' AND id_thesaurus = '"+idThesaurus+"';");
        ResultSet resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            idTerme = resultSet.getString("id_term");
        }
        resultSet.close();
        return idTerme;
    }
    
}
