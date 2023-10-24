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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miledrousset
 */
public class CorpusHelper {

    private final Log log = LogFactory.getLog(ConceptHelper.class);

    public ArrayList<NodeCorpus> getAllCorpus(HikariDataSource ds, String idTheso) {
        ArrayList<NodeCorpus> nodeCorpuses = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select corpus_name, uri_count, uri_link, active,only_uri_link from corpus_link where id_theso = '" + idTheso + "' order by sort");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeCorpus nodeCorpus = new NodeCorpus();
                        nodeCorpus.setCorpusName(resultSet.getString("corpus_name"));
                        nodeCorpus.setUriCount(resultSet.getString("uri_count"));
                        nodeCorpus.setUriLink(resultSet.getString("uri_link"));
                        nodeCorpus.setActive(resultSet.getBoolean("active"));
                        nodeCorpus.setIsOnlyUriLink(resultSet.getBoolean("only_uri_link"));
                        nodeCorpuses.add(nodeCorpus);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of linked corpus : " + idTheso, sqle);
        }
        return nodeCorpuses;
    }

    public ArrayList<NodeCorpus> getAllActiveCorpus(HikariDataSource ds, String idTheso) {
        ArrayList<NodeCorpus> nodeCorpuses = new ArrayList<>();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "select corpus_name, uri_count, uri_link, active, only_uri_link from corpus_link"
                        + " where id_theso = '" + idTheso + "'"
                        + " and active = true order by sort";
                stmt.executeQuery(query);
                try ( ResultSet resultSet = stmt.getResultSet()) {

                    while (resultSet.next()) {
                        NodeCorpus nodeCorpus = new NodeCorpus();
                        nodeCorpus.setCorpusName(resultSet.getString("corpus_name"));
                        nodeCorpus.setUriCount(resultSet.getString("uri_count"));
                        nodeCorpus.setUriLink(resultSet.getString("uri_link"));
                        nodeCorpus.setActive(resultSet.getBoolean("active"));
                        nodeCorpus.setIsOnlyUriLink(resultSet.getBoolean("only_uri_link"));
                        nodeCorpuses.add(nodeCorpus);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of linked corpus : " + idTheso, sqle);
        }
        return nodeCorpuses;
    }

    /**
     * permet de mettre à jour un corpus
     * 
     * @param ds
     * @param idTheso
     * @param oldName
     * @param nodeCorpus
     * @return 
     */
    public boolean updateCorpus(HikariDataSource ds, String idTheso, String oldName, NodeCorpus nodeCorpus) {

        boolean status = false;
        oldName = new StringPlus().convertString(oldName);
        nodeCorpus.setCorpusName(new StringPlus().convertString(nodeCorpus.getCorpusName()));
        
        if (StringUtils.isEmpty(nodeCorpus.getUriCount())) 
            nodeCorpus.setUriCount("");
        
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE corpus_link set corpus_name = '" + nodeCorpus.getCorpusName()
                        + "' ,uri_count = '" + nodeCorpus.getUriCount() + "' ,uri_link = '" + nodeCorpus.getUriLink()
                        + "' ,active = " + nodeCorpus.isActive() + ", only_uri_link = " + nodeCorpus.isIsOnlyUriLink()
                        + " where id_theso = '" + idTheso
                        + "' and corpus_name = '" + oldName + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while udpading corpus : " + nodeCorpus.getCorpusName());
        }
        return status;
    }

    /**
     * permet de savoir si le nom du corpus exite ou non
     * 
     * @param ds
     * @param idTheso
     * @param nodeCorpus
     * @return 
     */
    public boolean addNewCorpus(HikariDataSource ds, String idTheso, NodeCorpus nodeCorpus) {
        boolean status = false;
        nodeCorpus.setCorpusName(new StringPlus().convertString(nodeCorpus.getCorpusName()));
        if (StringUtils.isEmpty(nodeCorpus.getUriCount())) 
            nodeCorpus.setUriCount("");
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("insert into corpus_link (id_theso, corpus_name, uri_count, uri_link, active, only_uri_link) values "
                        + " ('" + idTheso + "','" + nodeCorpus.getCorpusName() + "','" + nodeCorpus.getUriCount()
                        + "','" + nodeCorpus.getUriLink() + "'," + nodeCorpus.isActive()
                        + "," + nodeCorpus.isIsOnlyUriLink() + ")");
                status = true;
            } catch (SQLException sqle) {
                log.error("Error while insert new Corpus : " + nodeCorpus.getCorpusName(), sqle);
            }
        } catch (SQLException sqle) {
            log.error("Error while insert new Corpus : " + nodeCorpus.getCorpusName(), sqle);
        }
        return status;
    }

    /**
     * permet de suprimer un corpus
     */
    public boolean deleteCorpus(HikariDataSource ds, String idTheso, String name) {
        boolean status = false;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from corpus_link where id_theso = '" + idTheso
                        + "' and corpus_name = '" + name + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting Corpus : " + name, sqle);
        }
        return status;
    }

    /**
     * permet de savoir si le nom du corpus exite ou non
     */
    public boolean isCorpusExist(HikariDataSource ds, String idTheso, String name) {
        boolean exist = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_theso from corpus_link where id_theso = '"
                        + idTheso + "' AND corpus_name = '" + name + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        exist = resultSet.getRow() != 0;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if corpus exist : " + name, sqle);
        }
        return exist;
    }

}
