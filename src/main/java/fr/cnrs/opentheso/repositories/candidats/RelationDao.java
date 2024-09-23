package fr.cnrs.opentheso.repositories.candidats;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class RelationDao extends BasicDao {

    @Autowired
    private RelationsHelper relationsHelper;



    public boolean deleteAllRelations(Connect connect, String idConceptSelected, 
                    String idThesaurus, int idUser) {
        Connection conn = null;
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
    
    public void addRelationBT(Connect connect, String idConceptSelected, String idConceptdestination, String idThesaurus){
        
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
    
    public List<NodeIdValue> getCandidatRelationsBT(HikariDataSource hikariDataSource,
                                                    String idConceptSelected, String idThesaurus, String lang) throws SQLException {
        
        var nodeBTs = relationsHelper.getListBT(hikariDataSource, idConceptSelected, idThesaurus, lang);
        
        if(CollectionUtils.isNotEmpty(nodeBTs)) {
            return nodeBTs.stream()
                    .map(element -> NodeIdValue.builder()
                            .id(element.getIdConcept())
                            .value(element.getTitle())
                            .build())
                    .collect(Collectors.toList());
        }
        return List.of();
    } 
    
    public List<NodeIdValue> getCandidatRelationsRT(HikariDataSource hikariDataSource, String idConceptSelected,
            String idThesaurus, String lang) throws SQLException {
        
        var nodeRTs = relationsHelper.getListRT(hikariDataSource, idConceptSelected, idThesaurus, lang);
        
        if(CollectionUtils.isNotEmpty(nodeRTs)) {
            return nodeRTs.stream()
                    .map(element -> NodeIdValue.builder()
                            .id(element.getIdConcept())
                            .value(element.getTitle())
                            .build())
                    .collect(Collectors.toList());
        }
        return List.of();
    }     
    
}