package fr.cnrs.opentheso.bean.condidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bean.condidat.enumeration.LanguageEnum;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class TermeDao extends BasicDao {
    
    
    public void saveIntitule(Connect connect, String intitule, String idThesaurus, String lang, String idConcept,
                             String idTerm) throws SQLException {
        
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        
        // insert in preferred_term
        //stmt.executeUpdate("INSERT INTO preferred_term(id_concept, id_term, id_thesaurus) VALUES ('" +idConcept+"', '" +idTerm+"', '"+idThesaurus+"')");

        // insert in non_preferred_term      
        stmt.executeUpdate(new StringBuffer("INSERT INTO non_preferred_term(lexical_value, lang, id_thesaurus, hiden, id_term) VALUES ('"
                    +intitule+"', '"+lang+"', '"+idThesaurus+"', false, '"+idTerm+"')").toString());
        
        stmt.close();
    }

    public String getIdTermeByValueAndLangAndThesaurus(HikariDataSource hikariDataSource, String idThesaurus, String lang,
                                                       String value) throws SQLException {
        String idTerm = null;
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery("SELECT id_term from term WHERE id_thesaurus='"+idThesaurus+"' AND lang = '"+ LanguageEnum.fromString(lang)
                    +"' AND lexical_value = '"+value+"'");
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                idTerm = resultSet.getString("id_term");
            }
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
        return idTerm;
    }

    public void deleteTermByValueAndLangAndThesaurus(HikariDataSource hikariDataSource, String idThesaurus, String lang,
                                                     String value) throws SQLException {
        try {
            String idTerm = getIdTermeByValueAndLangAndThesaurus(hikariDataSource, idThesaurus, lang, value);
            openDataBase(hikariDataSource);
            stmt.executeUpdate("DELETE FROM term WHERE id_term = '"+idTerm+"'");
            stmt.executeUpdate("DELETE FROM preferred_term WHERE id_term = '"+idTerm+"'");
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
    }

    public void updateIntitule(Connect connect, String intitule, String idThesaurus, String lang, String idConcept,
                               String idTerm) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            String idTerme = getIdPreferredTerme(stmt, idConcept, idThesaurus);
            stmt.executeUpdate("UPDATE non_preferred_term SET lexical_value = '"+intitule+"', lang='"
                    +lang+"' WHERE id_term ='"+idTerme+"' AND id_term = '"+idTerm+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public String searchTGByConceptAndThesaurus(HikariDataSource hikariDataSource, String idConceptSelected, 
            String idThesaurus) throws SQLException {
        String idTerme = null;
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery(new StringBuffer("SELECT id_concept2 FROM hierarchical_relationship WHERE id_concept1 = '")
                    .append(idConceptSelected).append("' AND id_thesaurus= '")
                    .append(idThesaurus).append("' AND role = 'BT'").toString());
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                idTerme = resultSet.getString("id_concept2");
            }
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
        return idTerme;
    }

    public List<String> searchTermeConceptAndThesaurusAndRole(HikariDataSource hikariDataSource, String idConceptSelected,
                                     String idThesaurus, String role) throws SQLException {
        List<String> termes = new ArrayList<>();
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery(new StringBuffer("SELECT id_concept2 FROM hierarchical_relationship WHERE id_concept1 = '")
                    .append(idConceptSelected).append("' AND id_thesaurus= '").append(idThesaurus).append("' AND role = '")
                    .append(role).append("'").toString());
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                termes.add(resultSet.getString("id_concept2"));
            }
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
        return termes;
    }

    public void saveNewTerme(Connect connect, String idConceptSelected, String idConceptdestination, String idThesaurus, String role) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            executInsertRequest(stmt, "INSERT INTO hierarchical_relationship(id_concept1, id_thesaurus, role, id_concept2) VALUES ('"
                    +idConceptSelected+"', '"+idThesaurus+"', '"+role+"', '"+idConceptdestination+"')");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public void updateTerme(Connect connect, String idConceptSelected, String idConceptdestination, String idThesaurus, String role) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            executInsertRequest(stmt, "UPDATE hierarchical_relationship SET id_concept2 = '"+idConceptdestination
                    +"' WHERE id_concept1= '"+idConceptSelected+"' AND id_thesaurus= '"+idThesaurus+"' AND role='"+role+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public void deleteExistingTerme(Connect connect, String idConceptSelected, String idConceptdestination, String idThesaurus, String role) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            executDeleteRequest(stmt, "DELETE FROM hierarchical_relationship WHERE id_concept1 = '"+idConceptSelected
                    +"'AND id_concept2 = '"+idConceptdestination+"' AND id_thesaurus = '"+idThesaurus+"' AND role = '"+role+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    public void deleteAllTermesByConcepteAndRole(Connect connect, String idConceptSelected, 
                    String idThesaurus, String role) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            executDeleteRequest(stmt, "DELETE FROM hierarchical_relationship WHERE id_concept1 = '"+idConceptSelected
                    +"' AND id_thesaurus = '"+idThesaurus+"' AND role = '"+role+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    private String getIdPreferredTerme(Statement stmt, String idConcept, String idThesaurus) throws SQLException {
        String idTerme = null;
        stmt.executeQuery("SELECT id_term FROM preferred_term WHERE id_concept = '"+idConcept
                +"' AND id_thesaurus = '"+idThesaurus+"'");
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            idTerme = resultSet.getString("id_term");
        }
        resultSet.close();
        return idTerme;
    }
    
}
