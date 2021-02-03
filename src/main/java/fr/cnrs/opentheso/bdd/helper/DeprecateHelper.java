/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeHieraRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Miled.Rousset
 */
public class DeprecateHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    public DeprecateHelper() {
    }

    /**
     * permet de retourner les concepts qui remplacent ce concept déprécié
     * @param ds
     * @param idConcept
     * @param idLang
     * @param idTheso
     * @return 
     */
    public ArrayList<NodeIdValue> getAllReplacedBy(HikariDataSource ds,
            String idTheso, String idConcept, String idLang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        String label;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2 from concept_replacedby where "
                            + "id_concept1 ='" + idConcept + "'"
                            + " and id_thesaurus = '" + idTheso + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id_concept2"));
                        nodeIdValues.add(nodeIdValue);
                    }
                    
                    for (NodeIdValue nodeIdValue : nodeIdValues) {
                        label = conceptHelper.getLexicalValueOfConcept(ds, nodeIdValue.getId(), idTheso, idLang);
                        if(label == null || label.isEmpty())
                            nodeIdValue.setValue("");
                        else
                            nodeIdValue.setValue(label);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting replacedBy : " + idConcept, sqle);
        }
        return nodeIdValues;
    }
    
    /**
     * permet de retourner les concepts qui remplacent ce concept déprécié
     * @param ds
     * @param idConcept
     * @param idTheso
     * @return 
     */
    public ArrayList<NodeHieraRelation> getAllReplacedByWithArk(HikariDataSource ds,
            String idTheso, String idConcept) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeHieraRelation> nodeRelations = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2, id_ark, id_handle, id_doi " +
                            " from concept_replacedby, concept" +
                            " where " +
                            " concept.id_concept = concept_replacedby.id_concept1" +
                            " and " +
                            " concept.id_thesaurus = concept_replacedby.id_thesaurus" +
                            " and " +
                            " concept_replacedby.id_concept1 = '" + idConcept + "'" +
                            " and " +
                            " concept_replacedby.id_thesaurus = '" + idTheso + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeHieraRelation nodeHieraRelation = new NodeHieraRelation();
                        NodeUri nodeUri = new NodeUri();
                        if ((resultSet.getString("id_ark") == null) || (resultSet.getString("id_ark").trim().isEmpty())) {
                            nodeUri.setIdArk("");
                        } else {
                            nodeUri.setIdArk(resultSet.getString("id_ark"));
                        }
                        if ((resultSet.getString("id_handle") == null) || (resultSet.getString("id_handle").trim().isEmpty())) {
                            nodeUri.setIdHandle("");
                        } else {
                            nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        }
                        if ((resultSet.getString("id_doi") == null) || (resultSet.getString("id_doi").trim().isEmpty())) {
                            nodeUri.setIdDoi("");
                        } else {
                            nodeUri.setIdDoi(resultSet.getString("id_doi"));
                        }                        
                        nodeUri.setIdConcept(resultSet.getString("id_concept2"));

                        nodeHieraRelation.setRole("replacedBy");
                        nodeHieraRelation.setUri(nodeUri);
                        nodeRelations.add(nodeHieraRelation);
                    }
                    
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting replacedBy : " + idConcept, sqle);
        }
        return nodeRelations;
    }    
  
    /**
     * permet de retourner les concepts dépréciés que ce concept remplace 
     * @param ds
     * @param idConcept
     * @param idLang
     * @param idTheso
     * @return 
     */
    public ArrayList<NodeIdValue> getAllReplaces(HikariDataSource ds,
            String idTheso, String idConcept, String idLang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        String label;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept1 from concept_replacedby where "
                            + "id_concept2 ='" + idConcept + "'"
                            + " and id_thesaurus = '" + idTheso + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id_concept1"));
                        nodeIdValues.add(nodeIdValue);
                    }
                    
                    for (NodeIdValue nodeIdValue : nodeIdValues) {
                        label = conceptHelper.getLexicalValueOfConcept(ds, nodeIdValue.getId(), idTheso, idLang);
                        if(label == null || label.isEmpty())
                            nodeIdValue.setValue("");
                        else
                            nodeIdValue.setValue(label);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting replaces : " + idConcept, sqle);
        }
        return nodeIdValues;
    }    
    
    /**
     * permet de retourner les concepts dépréciés que ce concept remplace 
     * @param ds
     * @param idConcept
     * @param idTheso
     * @return 
     */
    public ArrayList<NodeHieraRelation> getAllReplacesWithArk(HikariDataSource ds,
            String idTheso, String idConcept) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeHieraRelation> nodeRelations = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept1, id_ark, id_handle, id_doi " +
                            " from concept_replacedby, concept" +
                            " where " +
                            " concept.id_concept = concept_replacedby.id_concept2" +
                            " and " +
                            " concept.id_thesaurus = concept_replacedby.id_thesaurus" +
                            " and " +
                            " concept_replacedby.id_concept2 = '" + idConcept + "'" +
                            " and " +
                            " concept_replacedby.id_thesaurus = '" + idTheso + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeHieraRelation nodeHieraRelation = new NodeHieraRelation();
                        NodeUri nodeUri = new NodeUri();
                        if ((resultSet.getString("id_ark") == null) || (resultSet.getString("id_ark").trim().isEmpty())) {
                            nodeUri.setIdArk("");
                        } else {
                            nodeUri.setIdArk(resultSet.getString("id_ark"));
                        }
                        if ((resultSet.getString("id_handle") == null) || (resultSet.getString("id_handle").trim().isEmpty())) {
                            nodeUri.setIdHandle("");
                        } else {
                            nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        }
                        if ((resultSet.getString("id_doi") == null) || (resultSet.getString("id_doi").trim().isEmpty())) {
                            nodeUri.setIdDoi("");
                        } else {
                            nodeUri.setIdDoi(resultSet.getString("id_doi"));
                        }                        
                        nodeUri.setIdConcept(resultSet.getString("id_concept1"));

                        nodeHieraRelation.setRole("replace");
                        nodeHieraRelation.setUri(nodeUri);
                        nodeRelations.add(nodeHieraRelation);
                    }
                    
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting replacedBy : " + idConcept, sqle);
        }
        return nodeRelations;
    }     
    
    
    
     /**
     * Cette fonction permet de déprécier un concept (status = DEP)
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idUser
     * @return
     */
    public boolean deprecateConcept(HikariDataSource ds, String idConcept,
            String idTheso, int idUser) {
        Connection conn;
        Statement stmt;

        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    ConceptHelper conceptHelper = new ConceptHelper();

                    String query = "UPDATE concept "
                            + "set status='DEP'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";
                    stmt.executeUpdate(query);
                    conn.commit();
                    Concept concept = conceptHelper.getThisConcept(ds, idConcept, idTheso);
                    if (!conceptHelper.addConceptHistorique(conn, concept, idUser)) {
                        conn.rollback();
                        conn.close();
                        return false;
                    }
                    conn.commit();
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error during desactivation of Concept : " + idConcept, sqle);
            return false;
        }
        return true;
    }

    /**
     * Cette fonction permet de réactiver un concept
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idUser
     * @return
     */
    public boolean approveConcept(HikariDataSource ds, String idConcept,
            String idTheso, int idUser) {
        Connection conn;
        Statement stmt;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    ConceptHelper conceptHelper = new ConceptHelper();

                    String query = "UPDATE concept "
                            + "set status='D'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";
                    stmt.executeUpdate(query);
                    conn.commit();
                    
                    Concept concept = conceptHelper.getThisConcept(ds, idConcept, idTheso);
                    if (!conceptHelper.addConceptHistorique(conn, concept, idUser)) {
                        conn.rollback();
                        conn.close();
                        return false;
                    }
                    conn.commit();
                    deleteAllReplacedBy(ds, idConcept, idTheso);
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error during activation of Concept : " + idConcept, sqle);
            return false;
        }
        return true;
    }   
    
    public boolean deleteAllReplacedBy(HikariDataSource ds, String idConcept, String idTheso) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_replacedby where "
                            + "id_concept1 ='" + idConcept + "'"
                            + " and id_thesaurus = '" + idTheso + "'";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error deleting all replacedBy : " + idConcept, sqle);
        }
        return status;
    }
    
    /**
     * permet de supprimer un concept qui remplace celui qui est déprécié 
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idConceptReplaceBy
     * @return 
     */
    public boolean deleteReplacedBy(HikariDataSource ds,
            String idConcept, String idTheso, String idConceptReplaceBy) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_replacedby where "
                            + " id_concept1 ='" + idConcept + "'"
                            + " and id_concept2 = '" + idConceptReplaceBy + "'"
                            + " and id_thesaurus = '" + idTheso + "'";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error deleting replacedBy : " + idConceptReplaceBy, sqle);
        }
        return status;
    }    
    
    /**
     * permet d'ajouter les concepts qui remplacent celui qui a été déprécié
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idConceptReplaceBy
     * @param id_user
     * @return 
     */
    public boolean addReplacedBy(HikariDataSource ds,
            String idConcept, String idTheso,
            String idConceptReplaceBy , int id_user) {
        Statement stmt;
        Connection conn;
        boolean status = false;
        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt = conn.createStatement();
                    query = "Insert into concept_replacedby"
                            + "(id_concept1,id_concept2,id_thesaurus,id_user)"
                            + " values("
                            + "'" +idConcept + "',"
                            + "'" +idConceptReplaceBy + "',"
                            + "'" +idTheso + "',"
                            + id_user + ");";
                    stmt.execute(query);
                    status = true;  
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while insert into concept_replacedby : ", sqle);
                return false;
            }
        }
        return status;
    }    

}
