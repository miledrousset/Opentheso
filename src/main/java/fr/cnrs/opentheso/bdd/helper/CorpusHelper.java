/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;
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
    
}
