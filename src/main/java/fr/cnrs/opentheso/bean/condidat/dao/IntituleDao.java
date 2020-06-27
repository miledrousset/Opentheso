package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.SQLException;
import java.sql.Statement;


public class IntituleDao extends BasicDao {
    
    
    public void saveIntitule(Connect connect, String intitule, String idThesaurus, 
            String lang, String idConcept, String idUser, String idTerm) throws SQLException {
        
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        
        // insert in preferred_term
        executInsertRequest(stmt,
                "INSERT INTO preferred_term(id_concept, id_term, id_thesaurus) VALUES ('"
                        +idConcept+"', '"+idTerm+"', '"+idThesaurus+"')");
        
        // insert in non_preferred_term      
        executInsertRequest(stmt,
                new StringBuffer("INSERT INTO non_preferred_term(lexical_value, lang, id_thesaurus, hiden, id_term) VALUES ('"
                    +intitule+"', '"+lang+"', '"+idThesaurus+"', false, '"+idTerm+"')").toString());
        
        stmt.close();
    }

    public void updateIntitule(Connect connect, String intitule, String idThesaurus, String lang, 
            String idConcept, String idTerm) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            String idTerme = getIdTerme(stmt, idConcept, idThesaurus);
            stmt.executeUpdate("UPDATE non_preferred_term SET lexical_value = '"+intitule+"', lang='"
                    +lang+"' WHERE id_term ='"+idTerme+"' AND id_term = '"+idTerm+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    private String getIdTerme(Statement stmt, String idConcept, String idThesaurus) throws SQLException {
        String idTerme = null;
        stmt.executeQuery("SELECT id_term FROM preferred_term WHERE id_concept = '"+idConcept
                +"' AND id_thesaurus = '"+idThesaurus+"';");
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            idTerme = resultSet.getString("id_term");
        }
        resultSet.close();
        return idTerme;
    }
    
}
