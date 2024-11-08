package fr.cnrs.opentheso.repositories.candidats;

import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class RelationDao {

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private DataSource dataSource;



    public boolean deleteAllRelations(String idConceptSelected, String idThesaurus) {
        try (var conn = dataSource.getConnection()){
            conn.setAutoCommit(false);

            if (!relationsHelper.deleteAllRelationOfConcept(idConceptSelected, idThesaurus)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            return true;
        } catch (SQLException ex) {
            log.error(ex.toString());
            return false;
        }        
    }  
    
    public void addRelationBT(String idConceptSelected, String idConceptdestination, String idThesaurus){

        try (var connexion = dataSource.getConnection(); var statement = connexion.createStatement()) {
            statement.executeUpdate("INSERT INTO hierarchical_relationship(id_concept1, id_thesaurus, role, id_concept2) VALUES ('"
                    +idConceptSelected+"', '"+idThesaurus+"', 'BT', '"+idConceptdestination+"')");
            statement.executeUpdate("INSERT INTO hierarchical_relationship(id_concept1, id_thesaurus, role, id_concept2) VALUES ('"
                    +idConceptdestination+"', '"+idThesaurus+"', 'NT', '"+idConceptSelected+"')");
        } catch (SQLException e) {
            log.error(e.toString());
        }    
    }
    
    public void addRelationRT(String idConceptSelected, String idConceptdestination, String idThesaurus){
        
        try (var connexion = dataSource.getConnection(); var statement = connexion.createStatement()){
            statement.executeUpdate("INSERT INTO hierarchical_relationship(id_concept1, id_thesaurus, role, id_concept2) VALUES ('"
                    +idConceptSelected+"', '"+idThesaurus+"', 'RT', '"+idConceptdestination+"')");
            statement.executeUpdate("INSERT INTO hierarchical_relationship(id_concept1, id_thesaurus, role, id_concept2) VALUES ('"
                    +idConceptdestination+"', '"+idThesaurus+"', 'RT', '"+idConceptSelected+"')");
        } catch (SQLException e) {
            log.error(e.toString());
        }    
    }
    
    public List<NodeIdValue> getCandidatRelationsBT(String idConceptSelected, String idThesaurus, String lang) {
        
        var nodeBTs = relationsHelper.getListBT(idConceptSelected, idThesaurus, lang);
        
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
    
    public List<NodeIdValue> getCandidatRelationsRT(String idConceptSelected, String idThesaurus, String lang) {
        
        var nodeRTs = relationsHelper.getListRT(idConceptSelected, idThesaurus, lang);
        
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
