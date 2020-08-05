package fr.cnrs.opentheso.bean.condidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeRT;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.Connection;

import java.sql.SQLException;
import java.util.ArrayList;


public class RelationDao extends BasicDao {
    
   /**
     * 
     * @param connect
     * @param idConceptSelected
     * @param idThesaurus
     * @param idUser 
     * @return  
     */
    public boolean deleteAllRelations(Connect connect, String idConceptSelected, 
                    String idThesaurus, int idUser) {
        Connection conn = null;
        RelationsHelper relationsHelper = new RelationsHelper();
        try {
            conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);

            if (!relationsHelper.deleteAllRelationOfConcept(conn, idConceptSelected, idThesaurus, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            return true;

        } catch (SQLException ex) {
            LOG.error(ex);
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex1) {
                     LOG.error(ex1);
                }
            }
            return false;
        }        
    }  
    
    public void addRelationBT(Connect connect,
            String idConceptSelected,
            String idConceptdestination,
            String idThesaurus){
        
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            executInsertRequest(stmt, "INSERT INTO hierarchical_relationship(id_concept1, id_thesaurus, role, id_concept2) VALUES ('"
                    +idConceptSelected+"', '"+idThesaurus+"', 'BT', '"+idConceptdestination+"')");
            executInsertRequest(stmt, "INSERT INTO hierarchical_relationship(id_concept1, id_thesaurus, role, id_concept2) VALUES ('"
                    +idConceptdestination+"', '"+idThesaurus+"', 'NT', '"+idConceptSelected+"')");            
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
        }    
    }
    
    public void addRelationRT(Connect connect,
            String idConceptSelected,
            String idConceptdestination,
            String idThesaurus){
        
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            executInsertRequest(stmt, "INSERT INTO hierarchical_relationship(id_concept1, id_thesaurus, role, id_concept2) VALUES ('"
                    +idConceptSelected+"', '"+idThesaurus+"', 'RT', '"+idConceptdestination+"')");
            executInsertRequest(stmt, "INSERT INTO hierarchical_relationship(id_concept1, id_thesaurus, role, id_concept2) VALUES ('"
                    +idConceptdestination+"', '"+idThesaurus+"', 'RT', '"+idConceptSelected+"')");            
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
        }    
    }
    
    public ArrayList<NodeIdValue> getCandidatRelationsBT(
            HikariDataSource hikariDataSource,
            String idConceptSelected,
            String idThesaurus, String lang) throws SQLException {
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();

        RelationsHelper relationsHelper = new RelationsHelper();
        
        ArrayList<NodeBT> nodeBTs = relationsHelper.getListBT(hikariDataSource,
                idConceptSelected, idThesaurus, lang);
        
        if(nodeBTs != null) {
            for (NodeBT nodeBT : nodeBTs) {
                NodeIdValue nodeIdValue = new NodeIdValue();
                nodeIdValue.setId(nodeBT.getIdConcept());
                nodeIdValue.setValue(nodeBT.getTitle());
                nodeIdValues.add(nodeIdValue);
            }
        }
        return nodeIdValues;
    } 
    
    public ArrayList<NodeIdValue> getCandidatRelationsRT(
            HikariDataSource hikariDataSource,
            String idConceptSelected,
            String idThesaurus, String lang) throws SQLException {
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();

        RelationsHelper relationsHelper = new RelationsHelper();
        
        ArrayList<NodeRT> nodeRTs = relationsHelper.getListRT(hikariDataSource,
                idConceptSelected, idThesaurus, lang);
        
        if(nodeRTs != null) {
            for (NodeRT nodeRT : nodeRTs) {
                NodeIdValue nodeIdValue = new NodeIdValue();
                nodeIdValue.setId(nodeRT.getIdConcept());
                nodeIdValue.setValue(nodeRT.getTitle());
                nodeIdValues.add(nodeIdValue);
            }
        }
        return nodeIdValues;
    }     
    
}
