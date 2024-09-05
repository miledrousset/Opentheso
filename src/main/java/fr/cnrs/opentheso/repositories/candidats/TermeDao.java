package fr.cnrs.opentheso.repositories.candidats;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class TermeDao extends BasicDao {


    public void addNewTerme(HikariDataSource hikariDataSource, Term term) throws SQLException {

        term.setLexicalValue(fr.cnrs.opentheso.utils.StringUtils.convertString(term.getLexicalValue()));
        try {
            openDataBase(hikariDataSource);
            stmt.executeUpdate("INSERT INTO term (id_term, lexicalValue, lang, id_thesaurus, status, contributor, creator) VALUES ('"
                    +term.getIdTerm()+"', '"+term.getLexicalValue()+"', '"+term.getLang()+"', '"+term.getIdThesaurus()+"', '"
                    +term.getStatus()+"', "+term.getContributor()+", "+term.getCreator()+")");
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
    }
    
    public void addNewEmployePour(Connect connect, String intitule, String idThesaurus, String lang,
            String idTerm) throws SQLException{

        stmt = connect.getPoolConnexion().getConnection().createStatement();
        
        intitule = fr.cnrs.opentheso.utils.StringUtils.convertString(intitule);
        
        // insert in non_preferred_term      
        stmt.executeUpdate(new StringBuffer("INSERT INTO non_preferred_term(lexicalValue, lang, id_thesaurus, hiden, id_term) VALUES ('"
                    +intitule+"', '"+lang+"', '"+idThesaurus+"', false, '"+idTerm+"')").toString());
        
        stmt.close(); 
    }

    /**
     * Permet de supprimer une traduction
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

    public void updateIntitule(HikariDataSource hikariDataSource, String intitule, String idTerm, String idThesaurus,
                               String lang) throws SQLException {

        try {
            intitule = fr.cnrs.opentheso.utils.StringUtils.convertString(intitule);
            openDataBase(hikariDataSource);
            stmt.executeUpdate("update term set lexicalValue = '" + intitule + "'"
                    + " WHERE id_term = '" + idTerm + "'" 
                    + " AND lang = '" + lang + "'"
                    + " AND id_thesaurus = '" + idThesaurus + "'");
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
            closeDataBase();
        }
    }

    /**
     * Permet de récupérer les temes non préférés ou synonymes
     */
    public List<String> getEmployePour(HikariDataSource ds, String idConcept, String idTheso, String idLang){

        List<NodeEM> nodeEMs = getNonPreferredTerms(ds, idConcept, idTheso, idLang);
        if(CollectionUtils.isNotEmpty(nodeEMs)) {
            return nodeEMs.stream().map(NodeEM::getLexicalValue).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private List<NodeEM> getNonPreferredTerms(HikariDataSource ds, String idConcept, String idThesaurus, String idLang) {

        ArrayList<NodeEM> nodeEMList = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery( "SELECT lexical_value, created, modified, source, status, hiden " +
                        " FROM non_preferred_term, preferred_term " +
                        " WHERE " +
                        " non_preferred_term.id_term = preferred_term.id_term" +
                        " and" +
                        " non_preferred_term.id_thesaurus = preferred_term.id_thesaurus" +
                        " and" +
                        " preferred_term.id_concept = '" + idConcept + "'" +
                        " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'" +
                        " and non_preferred_term.lang ='" + idLang + "'" +
                        " order by lexical_value ASC");
                try (ResultSet resultSet = stmt.getResultSet()){
                    nodeEMList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeEM nodeEM = new NodeEM();
                        nodeEM.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeEM.setCreated(resultSet.getDate("created"));
                        nodeEM.setModified(resultSet.getDate("modified"));
                        nodeEM.setSource(resultSet.getString("source"));
                        nodeEM.setStatus(resultSet.getString("status"));
                        nodeEM.setHiden(resultSet.getBoolean("hiden"));
                        nodeEM.setLang(idLang);
                        nodeEMList.add(nodeEM);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
        }
        return nodeEMList;
    }

    /**
     * Permet de supprimer un synonymes
     */
    public void deleteEMByIdTermAndLang(HikariDataSource hikariDataSource, String idTerm, String idTheso,
                                        String lang) throws SQLException {
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
