/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miledrousset
 */
public class CorpusHelper {
    private final Log log = LogFactory.getLog(ConceptHelper.class);
    public CorpusHelper() {
    }

    public ArrayList<NodeCorpus> getAllCorpus(
            HikariDataSource ds, 
            String idTheso){
        ArrayList <NodeCorpus> nodeCorpuses = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select corpus_name, uri_count, uri_link, active from corpus_link"
                            + " where id_theso = '" + idTheso + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeCorpus nodeCorpus = new NodeCorpus();
                        nodeCorpus.setCorpusName(resultSet.getString("corpus_name"));
                        nodeCorpus.setUriCount(resultSet.getString("uri_count"));
                        nodeCorpus.setUriLink(resultSet.getString("uri_link"));
                        nodeCorpus.setActive(resultSet.getBoolean("active")); 
                        nodeCorpuses.add(nodeCorpus);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Liste of linked corpus : " + idTheso, sqle);
        }
        return nodeCorpuses;
    }
    
    public ArrayList<NodeCorpus> getAllActiveCorpus(
            HikariDataSource ds, 
            String idTheso){
        ArrayList <NodeCorpus> nodeCorpuses = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select corpus_name, uri_count, uri_link, active from corpus_link"
                            + " where id_theso = '" + idTheso + "'" 
                            + " and active = true";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeCorpus nodeCorpus = new NodeCorpus();
                        nodeCorpus.setCorpusName(resultSet.getString("corpus_name"));
                        nodeCorpus.setUriCount(resultSet.getString("uri_count"));
                        nodeCorpus.setUriLink(resultSet.getString("uri_link"));
                        nodeCorpus.setActive(resultSet.getBoolean("active")); 
                        nodeCorpuses.add(nodeCorpus);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Liste of linked corpus : " + idTheso, sqle);
        }
        return nodeCorpuses;
    }    
    
    /**
     * permet de mettre à jour un corpus 
     * @param ds
     * @param idTheso
     * @param oldName
     * @param nodeCorpus
     * @return 
     */
    public boolean updateCorpus(HikariDataSource ds,
            String idTheso,
            String oldName,
            NodeCorpus nodeCorpus){
        Connection conn;
        Statement stmt;
        boolean status = false;
        
        oldName = new StringPlus().convertString(oldName);
        nodeCorpus.setCorpusName(new StringPlus().convertString(nodeCorpus.getCorpusName()));
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                String query;
                try {
                    query = "UPDATE corpus_link "
                            + " set corpus_name = '" + nodeCorpus.getCorpusName() + "'" 
                            + " ,uri_count = '" + nodeCorpus.getUriCount() + "'" 
                            + " ,uri_link = '" + nodeCorpus.getUriLink() + "'"
                            + " ,active = " + nodeCorpus.isActive()
                            + " where id_theso = '" + idTheso + "'"
                            + " and corpus_name = '" + oldName + "'";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            log.error("Error while udpading corpus : " + nodeCorpus.getCorpusName());
        }
        return status;    
    }
    
    /**
     * permet de savoir si le nom du corpus exite ou non
     * @param ds
     * @param idTheso
     * @param nodeCorpus
     * @return 
     */
    public boolean addNewCorpus(
            HikariDataSource ds, 
            String idTheso, NodeCorpus nodeCorpus){
        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "insert into corpus_link (id_theso, corpus_name, uri_count, uri_link, active) values "
                            + " ('" + idTheso + "','" 
                            + nodeCorpus.getCorpusName() + "','" 
                            + nodeCorpus.getUriCount() + "','" 
                            + nodeCorpus.getUriLink() + "'," 
                            + nodeCorpus.isActive() + ")";
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
            log.error("Error while insert new Corpus : " + nodeCorpus.getCorpusName(), sqle);
        }
        return status;
    }  
    
    
    /**
     * permet de suprimer un corpus
     * @param ds
     * @param idTheso
     * @param name
     * @return 
     */
    public boolean deleteCorpus(
            HikariDataSource ds, 
            String idTheso, String name){
        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from corpus_link where"
                            + " id_theso = '" + idTheso + "'"
                            + " and corpus_name = '" + name + "'";
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
            log.error("Error while deleting Corpus : " + name, sqle);
        }
        return status;
    }      
        
    
    /**
     * permet de savoir si le nom du corpus exite ou non
     * @param ds
     * @param idTheso
     * @param name
     * @return 
     */
    public boolean isCorpusExist(
            HikariDataSource ds, 
            String idTheso, String name){
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean exist = false;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_theso from corpus_link where "
                            + " id_theso = '" + idTheso + "'"
                            + " AND"
                            + " corpus_name = '" + name + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        exist = resultSet.getRow() != 0;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if corpus exist : " + name, sqle);
        }
        return exist;
    }    
    
}
