package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.relations.NodeHieraRelation;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.concept.NodeUri;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Data
@Slf4j
@Service
@AllArgsConstructor
public class DeprecateService {

    private final DataSource dataSource;
    private final TermService termService;
    private final ConceptAddService conceptAddService;

    /**
     * permet de retourner les concepts qui remplacent ce concept déprécié
    *
    * @param idTheso
    * @param idConcept
    * @param idLang
    * @return 
    */ 
    public ArrayList<NodeIdValue> getAllReplacedBy(String idTheso, String idConcept, String idLang, ConceptHelper conceptHelper) {

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept2 from concept_replacedby where "
                        + "id_concept1 ='" + idConcept + "' and id_thesaurus = '" + idTheso + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id_concept2"));
                        nodeIdValues.add(nodeIdValue);
                    }

                    for (NodeIdValue nodeIdValue : nodeIdValues) {
                        var label = termService.getLexicalValueOfConcept(nodeIdValue.getId(), idTheso, idLang);
                        if (StringUtils.isEmpty(label)) {
                            nodeIdValue.setValue("");
                        } else {
                            nodeIdValue.setValue(label);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting replacedBy : " + idConcept, sqle);
        }
        return nodeIdValues;
    }

    /**
     * permet de retourner les concepts qui remplacent ce concept déprécié
    * @param idTheso
    * @param idConcept
    * @return 
    */
    public ArrayList<NodeHieraRelation> getAllReplacedByWithArk(String idTheso, String idConcept) {

        ArrayList<NodeHieraRelation> nodeRelations = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept2, id_ark, id_handle, id_doi "
                        + " from concept_replacedby, concept"
                        + " where concept.id_concept = concept_replacedby.id_concept2"
                        + " and concept.id_thesaurus = concept_replacedby.id_thesaurus"
                        + " and concept_replacedby.id_concept1 = '" + idConcept + "'"
                        + " and concept_replacedby.id_thesaurus = '" + idTheso + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
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
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting replacedBy : " + idConcept, sqle);
        }
        return nodeRelations;
    }

    /**
     * permet de retourner les concepts dépréciés que ce concept remplace
     */
    public ArrayList<NodeIdValue> getAllReplaces(String idTheso, String idConcept, String idLang, ConceptHelper conceptHelper) {

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept1 from concept_replacedby where "
                        + "id_concept2 ='" + idConcept + "' and id_thesaurus = '" + idTheso + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id_concept1"));
                        nodeIdValues.add(nodeIdValue);
                    }
                    for (NodeIdValue nodeIdValue : nodeIdValues) {
                        var label = termService.getLexicalValueOfConcept(nodeIdValue.getId(), idTheso, idLang);
                        if (label == null || label.isEmpty()) {
                            nodeIdValue.setValue("");
                        } else {
                            nodeIdValue.setValue(label);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting replaces : " + idConcept, sqle);
        }
        return nodeIdValues;
    }

    /**
     * permet de retourner les concepts dépréciés que ce concept remplace
    * @param idTheso
    * @param idConcept
    * @return 
    */
    public ArrayList<NodeHieraRelation> getAllReplacesWithArk(String idTheso, String idConcept) {
        ArrayList<NodeHieraRelation> nodeRelations = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept1, id_ark, id_handle, id_doi "
                        + " from concept_replacedby, concept"
                        + " where concept.id_concept = concept_replacedby.id_concept1"
                        + " and concept.id_thesaurus = concept_replacedby.id_thesaurus"
                        + " and concept_replacedby.id_concept2 = '" + idConcept + "'"
                        + " and concept_replacedby.id_thesaurus = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
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
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting replacedBy : " + idConcept, sqle);
        }
        return nodeRelations;
    }

    /**
     * Cette fonction permet de déprécier un concept (status = DEP)
     *
     * @param idConcept
     * @param idTheso
     * @param idUser
     * @return 
     */
    public boolean deprecateConcept(String idConcept, String idTheso, int idUser, ConceptHelper conceptHelper) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set status='DEP' WHERE id_concept ='" + idConcept + "' AND id_thesaurus='" + idTheso + "'");
                conn.commit();
                Concept concept = conceptHelper.getThisConcept(idConcept, idTheso);
                conceptAddService.addConceptHistorique(concept, idUser);
                conn.commit();
            }
        } catch (SQLException sqle) {
            log.error("Error during desactivation of Concept : " + idConcept, sqle);
            return false;
        }
        return true;
    }

    /**
     * Cette fonction permet de réactiver un concept
     */
    public boolean approveConcept(String idConcept, String idTheso, int idUser, ConceptHelper conceptHelper) {
        try ( Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set status='D' WHERE id_concept ='" + idConcept + "' AND id_thesaurus='" + idTheso + "'");
                conn.commit();
                var concept = conceptHelper.getThisConcept(idConcept, idTheso);
                conceptAddService.addConceptHistorique(concept, idUser);
                conn.commit();
                deleteAllReplacedBy(idConcept, idTheso);
            }
        } catch (SQLException sqle) {
            log.error("Error during activation of Concept : " + idConcept, sqle);
            return false;
        }
        return true;
    }

    public boolean deleteAllReplacedBy(String idConcept, String idTheso) {
        boolean status = false;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from concept_replacedby where id_concept1 ='"
                        + idConcept + "' and id_thesaurus = '" + idTheso + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error deleting all replacedBy : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * permet de supprimer un concept qui remplace celui qui est déprécié
     */
    public boolean deleteReplacedBy(String idConcept, String idTheso, String idConceptReplaceBy) {
        boolean status = false;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from concept_replacedby where id_concept1 ='"
                        + idConcept + "' and id_concept2 = '" + idConceptReplaceBy
                        + "' and id_thesaurus = '" + idTheso + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error deleting replacedBy : " + idConceptReplaceBy, sqle);
        }
        return status;
    }

    /**
     * permet d'ajouter les concepts qui remplacent celui qui a été déprécié
     */
    public boolean addReplacedBy(String idConcept, String idTheso, String idConceptReplaceBy, int id_user) {

        boolean status = false;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.execute("Insert into concept_replacedby (id_concept1,id_concept2,id_thesaurus,id_user) "
                        + "values('" + idConcept + "', '" + idConceptReplaceBy + "', '"
                        + idTheso + "'," + id_user + ");");
                status = true;
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
