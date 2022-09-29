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

    /**
     * permet de retourner les concepts qui remplacent ce concept déprécié
     */
    public ArrayList<NodeIdValue> getAllReplacedBy(HikariDataSource ds,
            String idTheso, String idConcept, String idLang) {

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        String label;

        try ( Connection conn = ds.getConnection()) {
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
                        label = conceptHelper.getLexicalValueOfConcept(ds, nodeIdValue.getId(), idTheso, idLang);
                        if (label == null || label.isEmpty()) {
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
    * @param ds
    * @param idTheso
    * @param idConcept
    * @return 
    */
    public ArrayList<NodeHieraRelation> getAllReplacedByWithArk(HikariDataSource ds,
            String idTheso, String idConcept) {

        ArrayList<NodeHieraRelation> nodeRelations = new ArrayList<>();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
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
    public ArrayList<NodeIdValue> getAllReplaces(HikariDataSource ds, String idTheso, String idConcept, String idLang) {

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        String label;
        try ( Connection conn = ds.getConnection()) {
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
                        label = new ConceptHelper().getLexicalValueOfConcept(ds, nodeIdValue.getId(), idTheso, idLang);
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
    * @param ds
    * @param idTheso
    * @param idConcept
    * @return 
    */
    public ArrayList<NodeHieraRelation> getAllReplacesWithArk(HikariDataSource ds, String idTheso, String idConcept) {
        ArrayList<NodeHieraRelation> nodeRelations = new ArrayList<>();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept1, id_ark, id_handle, id_doi "
                        + " from concept_replacedby, concept"
                        + " where concept.id_concept = concept_replacedby.id_concept1"
                        + " and concept.id_thesaurus = concept_replacedby.id_thesaurus"
                        + " and concept_replacedby.id_concept2 = '" + idConcept + "'"
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
     */
    public boolean deprecateConcept(HikariDataSource ds, String idConcept, String idTheso, int idUser) {
        try ( Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try ( Statement stmt = conn.createStatement()) {
                ConceptHelper conceptHelper = new ConceptHelper();
                stmt.executeUpdate("UPDATE concept set status='DEP' WHERE id_concept ='" + idConcept
                        + "' AND id_thesaurus='" + idTheso + "'");
                conn.commit();
                Concept concept = conceptHelper.getThisConcept(ds, idConcept, idTheso);
                if (!conceptHelper.addConceptHistorique(conn, concept, idUser)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
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
    public boolean approveConcept(HikariDataSource ds, String idConcept, String idTheso, int idUser) {
        try ( Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try ( Statement stmt = conn.createStatement()) {
                ConceptHelper conceptHelper = new ConceptHelper();
                stmt.executeUpdate("UPDATE concept set status='D' WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idTheso + "'");
                conn.commit();
                Concept concept = conceptHelper.getThisConcept(ds, idConcept, idTheso);
                if (!conceptHelper.addConceptHistorique(conn, concept, idUser)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
                conn.commit();
                deleteAllReplacedBy(ds, idConcept, idTheso);
            }
        } catch (SQLException sqle) {
            log.error("Error during activation of Concept : " + idConcept, sqle);
            return false;
        }
        return true;
    }

    public boolean deleteAllReplacedBy(HikariDataSource ds, String idConcept, String idTheso) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
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
    public boolean deleteReplacedBy(HikariDataSource ds, String idConcept, String idTheso,
            String idConceptReplaceBy) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
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
    public boolean addReplacedBy(HikariDataSource ds, String idConcept, String idTheso,
            String idConceptReplaceBy, int id_user) {

        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
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
