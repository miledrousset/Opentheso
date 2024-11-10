package fr.cnrs.opentheso.repositories.candidats;

import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.terms.NodeEM;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class TermeDao {

    @Autowired
    private DataSource dataSource;


    public void addNewTerme(Term term) {

        term.setLexicalValue(fr.cnrs.opentheso.utils.StringUtils.convertString(term.getLexicalValue()));
        try (var connect = dataSource.getConnection(); var stmt = connect.createStatement()){
            stmt.executeUpdate("INSERT INTO term (id_term, lexical_value, lang, id_thesaurus, status, contributor, creator) VALUES ('"
                    +term.getIdTerm()+"', '"+term.getLexicalValue()+"', '"+term.getLang()+"', '"+term.getIdThesaurus()+"', '"
                    +term.getStatus()+"', "+term.getContributor()+", "+term.getCreator()+")");
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }
    
    public void addNewEmployePour(String intitule, String idThesaurus, String lang, String idTerm) {

        try (var connect = dataSource.getConnection(); var stmt = connect.createStatement()){
            intitule = fr.cnrs.opentheso.utils.StringUtils.convertString(intitule);
            stmt.executeUpdate(new StringBuffer("INSERT INTO non_preferred_term(lexical_value, lang, id_thesaurus, hiden, id_term) VALUES ('"
                    +intitule+"', '"+lang+"', '"+idThesaurus+"', false, '"+idTerm+"')").toString());
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }

    /**
     * Permet de supprimer une traduction
     */
    public void deleteTermByIdTermAndLang(String idTerm, String lang, String idTheso)  {
        try (var connect = dataSource.getConnection(); var stmt = connect.createStatement()){
            stmt.executeUpdate("DELETE FROM term WHERE id_term = '"+idTerm+"' AND lang = '"+lang+"'"
            + " and id_thesaurus = '" +  idTheso + "'");
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }

    public void updateIntitule(String intitule, String idTerm, String idThesaurus, String lang) {

        try (var connect = dataSource.getConnection(); var stmt = connect.createStatement()){
            intitule = fr.cnrs.opentheso.utils.StringUtils.convertString(intitule);
            stmt.executeUpdate("update term set lexical_value = '" + intitule + "'"
                    + " WHERE id_term = '" + idTerm + "'" 
                    + " AND lang = '" + lang + "'"
                    + " AND id_thesaurus = '" + idThesaurus + "'");
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }

    /**
     * Permet de récupérer les temes non préférés ou synonymes
     */
    public List<String> getEmployePour(String idConcept, String idTheso, String idLang){

        List<NodeEM> nodeEMs = getNonPreferredTerms(idConcept, idTheso, idLang);
        if(CollectionUtils.isNotEmpty(nodeEMs)) {
            return nodeEMs.stream().map(NodeEM::getLexicalValue).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private List<NodeEM> getNonPreferredTerms(String idConcept, String idThesaurus, String idLang) {

        ArrayList<NodeEM> nodeEMList = null;
        try (Connection conn = dataSource.getConnection()) {
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
    public void deleteEMByIdTermAndLang(String idTerm, String idTheso, String lang) {
        try (var connect = dataSource.getConnection(); var stmt = connect.createStatement()){
            stmt.executeUpdate("DELETE FROM non_preferred_term WHERE id_term = '"+idTerm+"' AND lang = '"+lang+"'"
            + " and id_thesaurus = '" + idTheso + "'");
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }
}
