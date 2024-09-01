package fr.cnrs.opentheso.bean.candidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TermeDao extends BasicDao {


    public void addNewTerme(HikariDataSource hikariDataSource, Term term) throws SQLException {

        term.setLexical_value(fr.cnrs.opentheso.utils.StringUtils.convertString(term.getLexical_value()));
        try {
            openDataBase(hikariDataSource);
            stmt.executeUpdate("INSERT INTO term (id_term, lexical_value, lang, id_thesaurus, status, contributor, creator) VALUES ('"
                    +term.getId_term()+"', '"+term.getLexical_value()+"', '"+term.getLang()+"', '"+term.getId_thesaurus()+"', '"
                    +term.getStatus()+"', "+term.getContributor()+", "+term.getCreator()+")");
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
    }
    
    public void saveIntitule(Connect connect, String intitule, String idThesaurus, String lang, String idConcept,
                             String idTerm) throws SQLException {
        
        stmt = connect.getPoolConnexion().getConnection().createStatement();

        // insert in non_preferred_term      
        stmt.executeUpdate(new StringBuffer("INSERT INTO non_preferred_term(lexical_value, lang, id_thesaurus, hiden, id_term) VALUES ('"
                    +intitule+"', '"+lang+"', '"+idThesaurus+"', false, '"+idTerm+"')").toString());
        
        stmt.close();
    }
    
    public void addNewEmployePour(Connect connect, 
            String intitule,
            String idThesaurus,
            String lang,
            String idTerm) throws SQLException{
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        
        intitule = fr.cnrs.opentheso.utils.StringUtils.convertString(intitule);
        
        // insert in non_preferred_term      
        stmt.executeUpdate(new StringBuffer("INSERT INTO non_preferred_term(lexical_value, lang, id_thesaurus, hiden, id_term) VALUES ('"
                    +intitule+"', '"+lang+"', '"+idThesaurus+"', false, '"+idTerm+"')").toString());
        
        stmt.close(); 
    }

    public String getIdTermeByCandidatAndThesaurus(HikariDataSource hikariDataSource, String idThesaurus, String idCandidat) throws SQLException {
        String idTerm = null;
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery("SELECT id_term FROM preferred_term where id_thesaurus = '"+idThesaurus+"' AND id_concept = '"+idCandidat+"'");
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

    public void updateTerme(HikariDataSource hikariDataSource,
            String idTerm, String newValue, String lang, String idTheso) throws SQLException {
        try {
            newValue = fr.cnrs.opentheso.utils.StringUtils.convertString(newValue);
            openDataBase(hikariDataSource);
            stmt.executeUpdate("UPDATE term SET lexical_value = '"+newValue + "' WHERE id_term = '"+idTerm+"' AND lang = '"+lang+"'"
            + " and id_thesaurus = '" +  idTheso + "'");
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
    }

    /**
     * permet de supprimer une traduction
     * @param hikariDataSource
     * @param idTerm
     * @param lang
     * @param idTheso
     * @throws SQLException 
     */
    public void deleteTermByIdTermAndLang(HikariDataSource hikariDataSource,
            String idTerm, String lang, String idTheso) throws SQLException {
        try {
            openDataBase(hikariDataSource);
            stmt.executeUpdate("DELETE FROM term WHERE id_term = '"+idTerm+"' AND lang = '"+lang+"'"
            + " and id_thesaurus = '" +  idTheso + "'");
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
    }

    public void deleteTermByIdTermAndLangAndValue(HikariDataSource hikariDataSource,
                                          String idTerm, String lang, String idTheso, String value) throws SQLException {
        openDataBase(hikariDataSource);
        stmt.executeUpdate("DELETE FROM term WHERE id_term = '"+idTerm+"' AND lang = '"+lang+"'"
                + " and id_thesaurus = '" +  idTheso + "' and lexical_value='"+value+"'");
        closeDataBase();
    }

    public void deleteTermsByIdTerm(HikariDataSource hikariDataSource,
            String idTerm, String lang, String idTheso) throws SQLException {
        try {
            openDataBase(hikariDataSource);
            stmt.executeUpdate("DELETE FROM term WHERE id_term = '"+idTerm+"' AND lang != '"+lang+"'"
            + " and id_thesaurus = '" +  idTheso + "'");
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
    }

    public void updateIntitule(HikariDataSource hikariDataSource,
            String intitule, String idTerm, String idThesaurus, String lang
                               ) throws SQLException {
        try {
            intitule = fr.cnrs.opentheso.utils.StringUtils.convertString(intitule);
            openDataBase(hikariDataSource);
            stmt.executeUpdate("update term set lexical_value = '" + intitule + "'"
                    + " WHERE id_term = '" + idTerm + "'" 
                    + " AND lang = '" + lang + "'"
                    + " AND id_thesaurus = '" + idThesaurus + "'");
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
    }

    public List<String> searchTermeByConceptAndThesaurusAndRoleAndLang(HikariDataSource hikariDataSource, String idConceptSelected,
                                     String idThesaurus, String role, String lang) throws SQLException {
        List<String> termes = new ArrayList<>();
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery(new StringBuffer("SELECT nomPreTer.lexical_value " +
                    "FROM hierarchical_relationship hie, non_preferred_term nomPreTer, preferred_term preTer " +
                    "WHERE nomPreTer.id_term = preTer.id_term " +
                    "AND hie.id_concept2 = preTer.id_concept " +
                    "AND hie.id_concept1 = '"+idConceptSelected+"'" +
                    "AND hie.id_thesaurus= '"+idThesaurus+"'" +
                    "AND hie.role = '"+role+"'" +
                    "AND nomPreTer.lang = '"+lang+"'").toString());
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                termes.add(resultSet.getString("lexical_value"));
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
    
    /**
     * permet de récupérer les temes non préférés ou synonymes
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idLang
     * @return 
     */
    public List<String> getEmployePour(HikariDataSource ds, String idConcept, String idTheso, String idLang){
        List<NodeEM> nodeEMs = new TermHelper().getNonPreferredTerms(ds, idConcept, idTheso, idLang);
        if(CollectionUtils.isNotEmpty(nodeEMs)) {
            return nodeEMs.stream().map(NodeEM::getLexical_value).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }   
    
    /**
     * permet de supprimer un synonymes 
     * @param hikariDataSource
     * @param idTerm
     * @param idTheso
     * @param lang
     * @throws SQLException 
     */
    public void deleteEMByIdTermAndLang(HikariDataSource hikariDataSource, 
            String idTerm, String idTheso, String lang) throws SQLException {
        try {
            openDataBase(hikariDataSource);
            stmt.executeUpdate("DELETE FROM non_preferred_term WHERE id_term = '"+idTerm+"' AND lang = '"+lang+"'"
            + " and id_thesaurus = '" + idTheso + "'");
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
    }
}
