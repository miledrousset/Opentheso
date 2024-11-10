package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.models.nodes.NodeCorpus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Slf4j
@Service
public class CorpusHelper {

    @Autowired
    private DataSource dataSource;

    public ArrayList<NodeCorpus> getAllCorpus(String idTheso) {
        ArrayList<NodeCorpus> nodeCorpuses = new ArrayList<>();

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select corpus_name, uri_count, uri_link, active,only_uri_link from corpus_link where id_theso = '" + idTheso + "' order by sort");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeCorpus nodeCorpus = new NodeCorpus();
                        nodeCorpus.setCorpusName(resultSet.getString("corpus_name"));
                        nodeCorpus.setUriCount(resultSet.getString("uri_count"));
                        nodeCorpus.setUriLink(resultSet.getString("uri_link"));
                        nodeCorpus.setActive(resultSet.getBoolean("active"));
                        nodeCorpus.setOnlyUriLink(resultSet.getBoolean("only_uri_link"));
                        nodeCorpuses.add(nodeCorpus);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of linked corpus : " + idTheso, sqle);
        }
        return nodeCorpuses;
    }

    public ArrayList<NodeCorpus> getAllActiveCorpus(String idTheso) {
        ArrayList<NodeCorpus> nodeCorpuses = new ArrayList<>();
        try ( Connection conn = dataSource.getConnection()) {
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
                        nodeCorpus.setOnlyUriLink(resultSet.getBoolean("only_uri_link"));
                        nodeCorpuses.add(nodeCorpus);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of linked corpus : " + idTheso, sqle);
        }
        return nodeCorpuses;
    }
    
    public boolean isHaveActiveCorpus(String idTheso) {
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "select corpus_name from corpus_link"
                        + " where id_theso = '" + idTheso + "'"
                        + " and active = true";
                stmt.executeQuery(query);
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of linked corpus : " + idTheso, sqle);
        }
        return false;
    }    

    /**
     * permet de mettre à jour un corpus
     *
     * @param idTheso
     * @param oldName
     * @param nodeCorpus
     * @return 
     */
    public boolean updateCorpus(String idTheso, String oldName, NodeCorpus nodeCorpus) {

        boolean status = false;
        oldName = fr.cnrs.opentheso.utils.StringUtils.convertString(oldName);
        nodeCorpus.setCorpusName(fr.cnrs.opentheso.utils.StringUtils.convertString(nodeCorpus.getCorpusName()));
        
        if (StringUtils.isEmpty(nodeCorpus.getUriCount())) 
            nodeCorpus.setUriCount("");
        
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE corpus_link set corpus_name = '" + nodeCorpus.getCorpusName()
                        + "' ,uri_count = '" + nodeCorpus.getUriCount() + "' ,uri_link = '" + nodeCorpus.getUriLink()
                        + "' ,active = " + nodeCorpus.isActive() + ", only_uri_link = " + nodeCorpus.isOnlyUriLink()
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
     * @param idTheso
     * @param nodeCorpus
     * @return 
     */
    public boolean addNewCorpus(String idTheso, NodeCorpus nodeCorpus) {
        boolean status = false;
        nodeCorpus.setCorpusName(fr.cnrs.opentheso.utils.StringUtils.convertString(nodeCorpus.getCorpusName()));
        if (StringUtils.isEmpty(nodeCorpus.getUriCount())) 
            nodeCorpus.setUriCount("");
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("insert into corpus_link (id_theso, corpus_name, uri_count, uri_link, active, only_uri_link) values "
                        + " ('" + idTheso + "','" + nodeCorpus.getCorpusName() + "','" + nodeCorpus.getUriCount()
                        + "','" + nodeCorpus.getUriLink() + "'," + nodeCorpus.isActive()
                        + "," + nodeCorpus.isOnlyUriLink() + ")");
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
    public boolean deleteCorpus(String idTheso, String name) {
        boolean status = false;

        try ( Connection conn = dataSource.getConnection()) {
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
    public boolean isCorpusExist(String idTheso, String name) {
        boolean exist = false;
        try ( Connection conn = dataSource.getConnection()) {
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
