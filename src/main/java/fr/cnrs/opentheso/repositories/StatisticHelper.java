package fr.cnrs.opentheso.repositories;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import fr.cnrs.opentheso.bean.toolbox.statistique.ConceptStatisticData;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Data
@Slf4j
@Service
public class StatisticHelper {

    private int nombreConcept = 0;

    
    public int getNbAlignWikidata(HikariDataSource ds, String idThesaurus, String idGroup) {
        int count = 0;
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()){
                if(idGroup == null || idGroup.isEmpty()){
                    stmt.executeQuery("select count(alignement.internal_id_concept) from alignement " +
                        " where" +
                        " alignement.internal_id_thesaurus = '" + idThesaurus + "'" +
                        " and uri_target like '%wikidata.org%'" +
                        " and" +
                        " internal_id_concept NOT IN (SELECT idconcept FROM concept_group_concept WHERE idthesaurus = '" + idThesaurus + "')");
                } else{
                    stmt.executeQuery("select count(idconcept) from concept_group_concept, alignement " +
                        " where" +
                        " concept_group_concept.idconcept = alignement.internal_id_concept" +
                        " and" +
                        " concept_group_concept.idthesaurus = alignement.internal_id_thesaurus" +
                        " and" +
                        " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')" +
                        " and" +
                        " concept_group_concept.idthesaurus = '" + idThesaurus + "'" +
                        " and" +
                        " uri_target like '%wikidata.org%'");                    
                }
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting count of concept of thesaurus : " + idThesaurus, sqle);
        }
        return count;
    }
    
    public int getNbAlign(HikariDataSource ds, String idThesaurus, String idGroup) {
        int count = 0;
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()){
                if(idGroup == null || idGroup.isEmpty()) {
                    stmt.executeQuery("select count(alignement.internal_id_concept)" +
                        " from alignement " +
                        " where" +
                        " alignement.internal_id_thesaurus = '" + idThesaurus + "'" +
                        " and" +
                        " internal_id_concept NOT IN (SELECT idconcept FROM concept_group_concept WHERE idthesaurus = '" + idThesaurus + "')"); 
                } else {
                    stmt.executeQuery("select count(idconcept) from concept_group_concept, alignement " +
                        " where" +
                        " concept_group_concept.idconcept = alignement.internal_id_concept" +
                        " and" +
                        " concept_group_concept.idthesaurus = alignement.internal_id_thesaurus" +
                        " and" +
                        " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')" +
                        " and" +
                        " concept_group_concept.idthesaurus = '" + idThesaurus + "'");
                }
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting count of concept of thesaurus : " + idThesaurus, sqle);
        }
        return count;
    } 
    
    /**
     * méthode pour récupérer tous les concepts modifiés triés par date décroissant
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param limit
     * @return 
     * #MR
     */
    public List<ConceptStatisticData> getStatConcept(
            HikariDataSource ds,
            String idThesaurus, String idLang, int limit) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<ConceptStatisticData> conceptStatisticDatas = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT concept.id_concept, term.lexical_value, concept.created, concept.modified, users.username"
                            + " from concept, preferred_term, term, users"
                            + " where "
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_term = term.id_term"
                            + " and"
                            + " term.contributor = users.id_user"
                            + " and"
                            + " term.id_thesaurus = '" + idThesaurus + "'"
                            + " and"
                            + " term.lang = '" + idLang + "'"
                            + " ORDER BY concept.modified DESC LIMIT " + limit;                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                     while (resultSet.next()) {
                        ConceptStatisticData conceptStatisticData = new ConceptStatisticData();
                        conceptStatisticData.setIdConcept(resultSet.getString("id_concept"));
                        conceptStatisticData.setLabel(resultSet.getString("lexical_value"));                         
                        conceptStatisticData.setDateCreation(resultSet.getString("created"));
                        conceptStatisticData.setDateModification(resultSet.getString("modified")); 
                        conceptStatisticData.setUtilisateur(resultSet.getString("username")); 
                        conceptStatisticData.setType("skos:prefLabel");
                        conceptStatisticDatas.add(conceptStatisticData);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List statistic of Concept in thesaurus : " + idThesaurus, sqle);
        }
        return conceptStatisticDatas;
    }
    
    /**
     * méthode pour récupérer tous les concepts modifiés triés par date décroissant
     * avec filtre par collection
     * @param ds
     * @param idThesaurus
     * @param idGroup
     * @param idLang
     * @param limit
     * @return 
     * #MR
     */
    public List<ConceptStatisticData> getStatConceptLimitCollection(
            HikariDataSource ds,
            String idThesaurus, String idGroup, String idLang, int limit) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<ConceptStatisticData> conceptStatisticDatas = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT concept.id_concept, term.lexical_value, concept.created, concept.modified, users.username"
                            + " from concept, concept_group_concept, preferred_term, term, users"
                            + " where "
                            + " concept.id_concept = concept_group_concept.idconcept"
                            + " and"
                            + " concept.id_thesaurus = concept_group_concept.idthesaurus"
                            + " and"                            
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_term = term.id_term"
                            + " and"
                            + " term.contributor = users.id_user"
                            + " and"
                            + " term.id_thesaurus = '" + idThesaurus + "'"
                            + " and"
                            + " term.lang = '" + idLang + "'"
                            + " and"
                            + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')"
                            + " ORDER BY concept.modified DESC LIMIT " + limit;                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                     while (resultSet.next()) {
                        ConceptStatisticData conceptStatisticData = new ConceptStatisticData();
                        conceptStatisticData.setIdConcept(resultSet.getString("id_concept"));
                        conceptStatisticData.setLabel(resultSet.getString("lexical_value"));                         
                        conceptStatisticData.setDateCreation(resultSet.getString("created"));
                        conceptStatisticData.setDateModification(resultSet.getString("modified")); 
                        conceptStatisticData.setUtilisateur(resultSet.getString("username")); 
                        conceptStatisticData.setType("skos:prefLabel");
                        conceptStatisticDatas.add(conceptStatisticData);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List statistic of Concept in thesaurus : " + idThesaurus, sqle);
        }
        return conceptStatisticDatas;
    }    
    
    /**
     * méthode pour récupérer tous les concepts modifiés triés par date décroissant
     * avec filtre par collection et par date debut et fin
     * @param ds
     * @param idThesaurus
     * @param idGroup
     * @param idLang
     * @param dateDebut
     * @param datefin
     * @param limit
     * @return 
     * #MR
     */
    public List<ConceptStatisticData> getStatConceptByDateAndCollection(
            HikariDataSource ds,
            String idThesaurus, String idGroup, String idLang,
            String dateDebut, String datefin, int limit) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<ConceptStatisticData> conceptStatisticDatas = new ArrayList<>();
        String groupFilter = null;
        if(idGroup!= null && !idGroup.isEmpty()) {
            groupFilter = " and lower(concept_group_concept.idgroup) = lower('" + idGroup + "')";
        }
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "";
                    if(groupFilter == null) {
                        query = "SELECT concept.id_concept, term.lexical_value, concept.created, concept.modified, users.username"
                               + " from concept, preferred_term, term, users"
                               + " where "
                               + " concept.id_concept = preferred_term.id_concept"
                               + " and"
                               + " concept.id_thesaurus = preferred_term.id_thesaurus"
                               + " and"
                               + " preferred_term.id_thesaurus = term.id_thesaurus"
                               + " and"
                               + " preferred_term.id_term = term.id_term"
                               + " and"
                               + " term.contributor = users.id_user"
                               + " and"
                               + " term.id_thesaurus = '" + idThesaurus + "'"
                               + " and"
                               + " term.lang = '" + idLang + "'"
                               + " and"
                               + " concept.modified BETWEEN '" + dateDebut + "'"
                               + " and"
                               + " '" + datefin + "'"
                               + " ORDER BY concept.modified DESC LIMIT " + limit;                         
                    } else {
                        query = "SELECT concept.id_concept, term.lexical_value, concept.created, concept.modified, users.username"
                               + " from concept, concept_group_concept, preferred_term, term, users"
                               + " where "
                               + " concept.id_concept = concept_group_concept.idconcept"
                               + " and"
                               + " concept.id_thesaurus = concept_group_concept.idthesaurus"
                               + " and"                            
                               + " concept.id_concept = preferred_term.id_concept"
                               + " and"
                               + " concept.id_thesaurus = preferred_term.id_thesaurus"
                               + " and"
                               + " preferred_term.id_thesaurus = term.id_thesaurus"
                               + " and"
                               + " preferred_term.id_term = term.id_term"
                               + " and"
                               + " term.contributor = users.id_user"
                               + " and"
                               + " term.id_thesaurus = '" + idThesaurus + "'"
                               + " and"
                               + " term.lang = '" + idLang + "'"
                               + " and"
                               + " concept.modified BETWEEN '" + dateDebut + "'"
                               + " and"
                               + " '" + datefin + "'"
                               + groupFilter

                               + " ORDER BY concept.modified DESC LIMIT " + limit;  
                    }
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                     while (resultSet.next()) {
                        ConceptStatisticData conceptStatisticData = new ConceptStatisticData();
                        conceptStatisticData.setIdConcept(resultSet.getString("id_concept"));
                        conceptStatisticData.setLabel(resultSet.getString("lexical_value"));                         
                        conceptStatisticData.setDateCreation(resultSet.getString("created"));
                        conceptStatisticData.setDateModification(resultSet.getString("modified")); 
                        conceptStatisticData.setUtilisateur(resultSet.getString("username")); 
                        conceptStatisticData.setType("skos:prefLabel");
                        conceptStatisticDatas.add(conceptStatisticData);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List statistic of Concept in thesaurus : " + idThesaurus, sqle);
        }
        return conceptStatisticDatas;
    }
    
    
    /**
     * Cette fonction permet de récupérer le nombre des concepts suivant l'id du
     * Concept-Père et le thésaurus
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet Array String
     */
    public int getChildrenCountOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        int count = 0;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select count(id_concept2) from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role = '" + "NT" + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if(resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                    return count;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting count of childs for Concept : " + idConcept, sqle);
        }
        return -1;
    }    
    
    /**
     * Retourne le nombre de concepts sans les candidtas ni les concepts dépréciés 
     * 
     * @param ds
     * @param idThesaurus
     * @return 
     */
    public int getNbCpt(HikariDataSource ds, String idThesaurus) {
        int count = 0;
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery( "SELECT count(id_concept) FROM concept WHERE"
                            + " id_thesaurus = '" + idThesaurus + "' and status not in ('CA', 'DEP')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting count of concept of thesaurus : " + idThesaurus, sqle);
        }
        return count;
    }
    
    /**
     * Retourne le nombre de candidats
     * 
     * @param ds
     * @param idThesaurus
     * @return 
     */
    public int getNbCandidate(HikariDataSource ds, String idThesaurus) {
        int count = 0;
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery( "SELECT count(id_concept) FROM concept WHERE"
                            + " id_thesaurus = '" + idThesaurus + "' and status = 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting count of candidate of thesaurus : " + idThesaurus, sqle);
        }
        return count;
    }    
    
    /**
     * Retourne le nombre de concepts dépréciés
     * 
     * @param ds
     * @param idThesaurus
     * @return 
     */
    public int getNbOfDeprecatedConcepts(HikariDataSource ds, String idThesaurus) {
        int count = 0;
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery( "SELECT count(id_concept) FROM concept WHERE"
                            + " id_thesaurus = '" + idThesaurus + "' and status = 'DEP'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting count of candidate of thesaurus : " + idThesaurus, sqle);
        }
        return count;
    }

    public int getNbSynonymesByGroup(HikariDataSource ds, String idThesaurus, String idGroup, String idLang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        int count = 0;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT count(npt.id_term) \n" +
                        " FROM non_preferred_term npt\n" +
                        " inner JOIN preferred_term pt on pt.id_term = npt.id_term AND pt.id_thesaurus = npt.id_thesaurus\n" +
                        " inner JOIN concept_group_concept cgc on cgc.idthesaurus = pt.id_thesaurus AND cgc.idconcept = pt.id_concept\n" +
                        "\n" +
                        " WHERE \n" +
                        " npt.lang = '" + idLang + "'" +
                        " AND npt.id_thesaurus = '" + idThesaurus + "'" +
                        " and lower(cgc.idgroup) = lower('" + idGroup + "')";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        resultSet.next();
                        count = resultSet.getInt(1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting count of Synonyms of group : ", sqle);
        }
        return count;
    }    
    
    public int getNbDesSynonimeSansGroup(HikariDataSource ds, String idThesaurus, String idLang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        int count = 0;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT count(non_preferred_term.id_term) " +
                            "FROM non_preferred_term, preferred_term " +
                            "WHERE preferred_term.id_term = non_preferred_term.id_term " +
                            "AND preferred_term.id_thesaurus = non_preferred_term.id_thesaurus " +
                            " AND non_preferred_term.lang = '" + idLang + "'" +
                            "AND non_preferred_term.id_thesaurus = '"+idThesaurus+"' " +
                            "AND preferred_term.id_concept NOT " +
                                "IN (SELECT idconcept FROM concept_group_concept " +
                                "WHERE concept_group_concept.idthesaurus = '"+idThesaurus+"')";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        resultSet.next();
                        count = resultSet.getInt(1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting count of decriptor without group : ", sqle);
        }
        return count;
    }

    /**
     * Retourne le nombre des traductions filtré par groupe
     * @param ds
     * @param idThesaurus
     * @param idGroup
     * @param langue
     * @return 
     */
    public int getNbTradOfGroup(HikariDataSource ds, String idThesaurus, String idGroup, String langue) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        int count = 0;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query="SELECT " +
                        " count (term.id_term) FROM term, preferred_term, concept, concept_group_concept" +
                        " WHERE " +
                        " term.id_thesaurus = preferred_term.id_thesaurus" +
                        " and" +
                        " term.id_term = preferred_term.id_term" +
                        " and" +
                        " preferred_term.id_concept = concept.id_concept" +
                        " and" +
                        " preferred_term.id_thesaurus = concept.id_thesaurus" +
                        " and" +
                        " preferred_term.id_concept = concept_group_concept.idconcept" +
                        " and" +
                        " preferred_term.id_thesaurus = concept_group_concept.idthesaurus" +
                        " and" +
                        " concept.id_concept = concept_group_concept.idconcept" +
                        " and" +
                        " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')" +
                        " and" +
                        " concept.status != 'CA'" +
                        " and" +
                        " term.id_thesaurus='" + idThesaurus + "'" +
                        " and" +
                        " term.lang='" + langue + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        resultSet.next();
                        count = resultSet.getInt(1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting count of traduction of group : " + idGroup, sqle);
        }
        return count;
    }
    
    public int getNbTradWithoutGroup(HikariDataSource ds, String idThesaurus, String idLang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        int count = 0;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query="SELECT " +
                        "count (concept.id_concept)  FROM term, preferred_term, concept" +
                        " WHERE " +
                        " term.id_thesaurus = preferred_term.id_thesaurus" +
                        " and" +
                        " term.id_term = preferred_term.id_term" +
                        " and" +
                        " preferred_term.id_concept = concept.id_concept" +
                        " and" +
                        " preferred_term.id_thesaurus = concept.id_thesaurus" +
                        " and" +
                        " concept.status != 'CA' " +
                        " and" +
                        " term.id_thesaurus='" + idThesaurus + "'" +
                        " and" +
                        " term.lang='" + idLang + "'" +
                        " and" +
                        " concept.id_concept NOT IN (SELECT concept_group_concept.idconcept FROM concept_group_concept WHERE concept_group_concept.idthesaurus = '" + idThesaurus + "')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        resultSet.next();
                        count = resultSet.getInt(1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting count of traduction without group : " + idThesaurus, sqle);
        }
        return count;
    }
   
}
