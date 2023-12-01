package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.helper.nodes.statistic.NodeStatConcept;
import fr.cnrs.opentheso.bean.toolbox.statistique.ConceptStatisticData;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StatisticHelper {
    
    private final Log log = LogFactory.getLog(ThesaurusHelper.class);
    private int nombreConcept = 0;
    
    public StatisticHelper() {
        
    }

    public int getNombreConcept() {
        return nombreConcept;
    }

    public void setNombreConcept(int nombreConcept) {
        this.nombreConcept = nombreConcept;
    }
    
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

    
    
    /*
    select count(concept.id_concept)
        from concept, concept_group_concept where
        concept_group_concept.idconcept = concept.id_concept
        and
        concept_group_concept.idthesaurus = concept.id_thesaurus
        and
        modified >= '2019-01-01' and modified <= '2020-06-03'
        and concept.id_thesaurus= 'th13' and concept_group_concept.idgroup = 'pcrtIriK2LwtHZ';
    */
    
    /*
    select count(term.id_term) 
        from term, preferred_term, concept_group_concept where
        concept_group_concept.idconcept = preferred_term.id_concept
        and
        concept_group_concept.idthesaurus = preferred_term.id_thesaurus
        and
        preferred_term.id_term = term.id_term
        and
        preferred_term.id_thesaurus = term.id_thesaurus
        and
        modified >= '2019-01-01' and modified <= '2020-06-04' 
        and term.id_thesaurus= 'th13' and lang = 'fr' and concept_group_concept.idgroup = 'pcrtIriK2LwtHZ'
    */
    
    
    /**
     * Fonction recursive qui permet de retrouver le nombre de concepts dans la branche + le concept lui même
     * @param ds
     * @param idConcept
     * @param idTheso
     * @return 
     */
    public int getConceptCountOfBranch(HikariDataSource ds, String idConcept,
            String idTheso) {
        ConceptHelper conceptHelper = new ConceptHelper();

        ArrayList <String> listIdsOfConceptChildren = conceptHelper.getListChildrenOfConcept(ds,
                        idConcept, idTheso);

        
        int compteur = getChildrenCountOfConcept(ds, idConcept, idTheso);
        if(compteur != -1) {
            nombreConcept = nombreConcept + compteur;
        }

        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
       //     if(!conceptHelper.deleteConceptForced(ds, listIdsOfConceptChildren1, idTheso, idUser))
        //        return false;
            getConceptCountOfBranch(ds, listIdsOfConceptChildren1, idTheso);
        }
        return nombreConcept + 1;
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
    
    public int getNbDescOfGroup(HikariDataSource ds, String idThesaurus, String idGroup) {
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
                    String query = "select count(id_concept) from concept left join concept_group_concept" +
                        " on id_concept = idconcept and id_thesaurus = idthesaurus" +
                        " where id_thesaurus = '" +idThesaurus + "'" +
                        " and lower(idgroup) = lower('" + idGroup + "')";
                    
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
            log.error("Error while getting count of decriptor of group : " + idGroup, sqle);
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
    
    public int getNbNonDescOfGroup(HikariDataSource ds, String idThesaurus, String idGroup, String langue) {
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
                   //modification query SQL #MR
                    String query = "SELECT " +
                        "  count(non_preferred_term.id_term)" +
                        " FROM" +
                        "  non_preferred_term, " +
                        "  preferred_term, " +
                        "  concept_group_concept" +
                        " WHERE" +
                        "  preferred_term.id_term = non_preferred_term.id_term AND" +
                        "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus AND" +
                        "  concept_group_concept.idconcept = preferred_term.id_concept AND" +
                        "  concept_group_concept.idthesaurus = preferred_term.id_thesaurus AND" +
                        "  non_preferred_term.lang = '" + langue + "' AND " +
                        "  non_preferred_term.id_thesaurus = '" + idThesaurus + "' AND " +
                        "  lower(concept_group_concept.idgroup) = lower('" + idGroup + "')";
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
            log.error("Error while getting count of non-decriptor of group : " + idGroup, sqle);
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
    
    public int getNbDefinitionNoteOfGroup(HikariDataSource ds, String idThesaurus, String langue, String idGroup) {
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
                    String query = "SELECT count(DISTINCT note.id) FROM concept, note WHERE"
                            + " concept.id_concept = note.id_concept"
                            + " and concept.id_thesaurus = note.id_thesaurus"
                            + " and concept.id_thesaurus = '" + idThesaurus + "'"
                            + " and concept.id_concept IN (SELECT idconcept FROM concept_group_concept"
                            + " WHERE lower(idgroup) = lower('"+ idGroup + "') and idthesaurus = '" + idThesaurus + "')"
                            + " and note.lang = '" + langue + "'";

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
            log.error("Error while getting count of definition note of group : " + idGroup, sqle);
        }
        return count;
    }
    
    /**
     * permet de retourner les stats d'un thésaurus pour les concepts modifiés 
     * pas de définition de dates mais une limite de nombre de résultat
     * @param ds
     * @param idThesaurus
     * @param langue
     * @param limit
     * @return 
     * #MR déprécié
     */
/*    public ArrayList<NodeStatConcept> getStatConcept(HikariDataSource ds,
            String idThesaurus, String langue, int limit) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeStatConcept> list = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.lexical_value," +
                        " concept_group_concept.idgroup," +
                        " concept_group_concept.idconcept, term.created, term.modified" +
                        " FROM concept_group_concept," +
                        " preferred_term, term WHERE" +
                        " preferred_term.id_thesaurus = term.id_thesaurus AND" +
                        " preferred_term.id_term = term.id_term AND" +
                        " preferred_term.id_thesaurus = concept_group_concept.idthesaurus AND" +
                        " preferred_term.id_concept = concept_group_concept.idconcept AND" +
                        " term.id_thesaurus = '" + idThesaurus + "' AND" +
                        " term.lang = '" + langue + "'" +
                        " order by term.created DESC" +
                        " LIMIT "+limit;
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeStatConcept nsc = new NodeStatConcept();
                        nsc.setDateCreat(resultSet.getDate("created"));
                        nsc.setDateEdit(resultSet.getDate("modified"));
                        String temp = new GroupHelper().getThisConceptGroup(ds, resultSet.getString("idgroup"), idThesaurus, langue).getLexicalValue();
                        nsc.setGroup(temp + "(" + resultSet.getString("idgroup") + ")");
                        nsc.setIdConcept(resultSet.getString("idconcept"));
                        nsc.setValue(resultSet.getString("lexical_value"));
                        list.add(nsc);
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
        return list;
    }*/
    
    public ArrayList<NodeStatConcept> getStatConceptCreat(HikariDataSource ds, String begin, String end, String idThesaurus, String langue,int limit) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeStatConcept> list = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.lexical_value," +
                        " concept_group_concept.idgroup," +
                        " concept_group_concept.idconcept, term.created, term.modified" +
                        " FROM concept_group_concept," +
                        " preferred_term, term WHERE" +
                        " preferred_term.id_thesaurus = term.id_thesaurus AND" +
                        " preferred_term.id_term = term.id_term AND" +
                        " preferred_term.id_thesaurus = concept_group_concept.idthesaurus AND" +
                        " preferred_term.id_concept = concept_group_concept.idconcept AND" +
                        " term.id_thesaurus = '" + idThesaurus + "' AND" +
                        " term.created <= '" + end + "'" +
                        " AND term.created >= '" + begin + "'" +
                        " AND term.lang = '" + langue + "'" +
                        " order by term.created DESC" +
                        " LIMIT "+limit;

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeStatConcept nsc = new NodeStatConcept();
                        nsc.setDateCreat(resultSet.getDate("created"));
                        nsc.setDateEdit(resultSet.getDate("modified"));
                        String temp = new GroupHelper().getThisConceptGroup(ds, resultSet.getString("idgroup"), idThesaurus, langue).getLexicalValue();
                        nsc.setGroup(temp + "(" + resultSet.getString("idgroup") + ")");
                        nsc.setIdConcept(resultSet.getString("idconcept"));
                        nsc.setValue(resultSet.getString("lexical_value"));
                        list.add(nsc);
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
        return list;
    }
    
    public ArrayList<NodeStatConcept> getStatConceptEdit(HikariDataSource ds, String begin, String end, String idThesaurus, String langue,int limit) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeStatConcept> list = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.lexical_value," +
                        " concept_group_concept.idgroup," +
                        " concept_group_concept.idconcept, term.created, term.modified" +
                        " FROM concept_group_concept," +
                        " preferred_term, term WHERE" +
                        " preferred_term.id_thesaurus = term.id_thesaurus AND" +
                        " preferred_term.id_term = term.id_term AND" +
                        " preferred_term.id_thesaurus = concept_group_concept.idthesaurus AND" +
                        " preferred_term.id_concept = concept_group_concept.idconcept AND" +
                        " term.id_thesaurus = '" + idThesaurus + "' AND" +
                        " term.modified <= '" + end + "'" +
                        " AND term.modified >= '" + begin + "'" +
                        " AND term.lang = '" + langue + "'" +
                        " order by term.created DESC" +
                        " LIMIT "+limit;
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeStatConcept nsc = new NodeStatConcept();
                        nsc.setDateCreat(resultSet.getDate("created"));
                        nsc.setDateEdit(resultSet.getDate("modified"));
                        String temp = new GroupHelper().getThisConceptGroup(ds, resultSet.getString("idgroup"), idThesaurus, langue).getLexicalValue();
                        nsc.setGroup(temp + "(" + resultSet.getString("idgroup") + ")");
                        nsc.setIdConcept(resultSet.getString("idconcept"));
                        nsc.setValue(resultSet.getString("lexical_value"));
                        list.add(nsc);
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
        return list;
    }
    /**
     * #JM
     * méthode pour récupérer les concepts selon un groupe et des dates 
     * la colonne modified ou created et intérrogée selon le paramètre column
     * @param ds
     * @param begin
     * @param end
     * @param column
     * @param idThesaurus
     * @param langue
     * @param selectedGroup
     * @param limit
     * @return 
     */
    public ArrayList<NodeStatConcept> getStatConceptByGroupAndDate(HikariDataSource ds, String begin, String end, String column, String idThesaurus, String langue, String selectedGroup,int limit) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeStatConcept> list = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.lexical_value," +
                        " concept_group_concept.idgroup," +
                        " concept_group_concept.idconcept, term.created, term.modified" +
                        " FROM concept_group_concept," +
                        " preferred_term, term WHERE" +
                        " preferred_term.id_thesaurus = term.id_thesaurus AND" +
                        " preferred_term.id_term = term.id_term AND" +
                        " preferred_term.id_thesaurus = concept_group_concept.idthesaurus AND" +
                        " preferred_term.id_concept = concept_group_concept.idconcept AND" +
                        " term.id_thesaurus = '" + idThesaurus + "' AND" +
                        " lower(idgroup) = lower('" + selectedGroup + "') AND" +
                        " term.created <= '" + end + "'" +
                        " AND term.created >= '" + begin + "'" +
                        " AND term.lang = '" + langue + "'" +
                        " order by term.created DESC" +
                        " LIMIT "+limit;                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                     while (resultSet.next()) {
                        NodeStatConcept nsc = new NodeStatConcept();
                        nsc.setDateCreat(resultSet.getDate("created"));
                        nsc.setDateEdit(resultSet.getDate("modified"));
                        String temp = new GroupHelper().getThisConceptGroup(ds, resultSet.getString("idgroup"), idThesaurus, langue).getLexicalValue();
                        nsc.setGroup(temp + "(" + resultSet.getString("idgroup") + ")");
                        nsc.setIdConcept(resultSet.getString("idconcept"));
                        nsc.setValue(resultSet.getString("lexical_value"));
                        list.add(nsc);
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
        return list;
    }

   
}
