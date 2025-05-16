package fr.cnrs.opentheso.repositories.candidats;

import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.group.NodeGroup;

import java.sql.Connection;
import java.sql.ResultSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


@Slf4j
@Service
public class DomaineDao {

    @Autowired
    private DataSource dataSource;


    public void addNewDomaine(String idgroup, String idthesaurus, String idconcept) {
        try (var connect = dataSource.getConnection(); var stmt = connect.createStatement()){
            stmt.executeUpdate("INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES ('"
                    +idgroup+"', '"+idthesaurus+"', '"+idconcept+"')");
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }
    
    public void deleteAllDomaine(String idThesaurus, String idConcept) {
        try (var connect = dataSource.getConnection(); var stmt = connect.createStatement()){
            stmt.executeUpdate("DELETE FROM concept_group_concept WHERE idconcept = '"+idConcept+"'  AND idthesaurus = '"+idThesaurus+"'");
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }

    public void deleteDomaine(String idThesaurus, String idConcept, String idGroupe) {

        try (var connect = dataSource.getConnection(); var stmt = connect.createStatement()){
            stmt.executeUpdate("DELETE FROM concept_group_concept " +
                    "WHERE idconcept = '"+idConcept+"' AND idthesaurus = '"+idThesaurus+"' AND idgroup='"+idGroupe+"'");
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }

    public ArrayList<NodeIdValue> getDomaineCandidatByConceptAndThesaurusAndLang(String idconcept, String idThesaurus, String lang) {

        ArrayList<NodeGroup> nodeGroups = getListGroupOfConcept(idThesaurus, idconcept, lang);
        
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        for (NodeGroup nodeGroup : nodeGroups) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setValue(nodeGroup.getLexicalValue());
            nodeIdValue.setId(nodeGroup.getConceptGroup().getIdGroup());
            nodeIdValues.add(nodeIdValue);
        }
        return nodeIdValues;
    }

    public ArrayList<NodeGroup> getListGroupOfConcept(String idThesaurus, String idConcept, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeGroup> nodeGroups = new ArrayList<>();

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group_concept where idthesaurus = '" + idThesaurus + "'"
                            + " and idconcept = '" + idConcept + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeGroup nodeGroup = getThisConceptGroup(resultSet.getString("idgroup"), idThesaurus, idLang);
                        if (nodeGroup != null) {
                            nodeGroups.add(nodeGroup);
                        }
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Id or Groups of Concept : " + idConcept, sqle);
        }
        return nodeGroups;
    }

    public NodeGroup getThisConceptGroup(String idConceptGroup, String idThesaurus, String idLang) {

        NodeGroup nodeConceptGroup = null;
        fr.cnrs.opentheso.entites.ConceptGroup conceptGroup = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * from concept_group where LOWER(idgroup) = '" + idConceptGroup.toLowerCase() + "'"
                        + " and idthesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()){
                    if (resultSet.next()) {
                        if (resultSet.getRow() != 0) {
                            conceptGroup = new fr.cnrs.opentheso.entites.ConceptGroup();
                            conceptGroup.setIdGroup(idConceptGroup);
                            conceptGroup.setIdThesaurus(idThesaurus);
                            conceptGroup.setIdArk(resultSet.getString("id_ark"));
                            conceptGroup.setIdHandle(resultSet.getString("id_handle"));
                            conceptGroup.setIdTypeCode(resultSet.getString("idtypecode"));
                            conceptGroup.setNotation(resultSet.getString("notation"));
                            conceptGroup.setCreated(resultSet.getDate("created"));
                            conceptGroup.setModified(resultSet.getDate("modified"));
                        }
                    }
                }
                if (conceptGroup != null) {
                    stmt.executeQuery("SELECT * FROM concept_group_label WHERE"
                            + " LOWER(idgroup) = '" + conceptGroup.getIdGroup().toLowerCase() + "'"
                            + " AND idthesaurus = '" + idThesaurus + "'"
                            + " AND lang = '" + idLang + "'");
                    try (ResultSet resultSet = stmt.getResultSet()){
                        nodeConceptGroup = new NodeGroup();
                        if (resultSet.next()) {
                            nodeConceptGroup.setLexicalValue(resultSet.getString("lexicalvalue"));
                            nodeConceptGroup.setIdLang(idLang);
                            nodeConceptGroup.setCreated(resultSet.getDate("created"));
                            nodeConceptGroup.setModified(resultSet.getDate("modified"));

                        } else {
                            nodeConceptGroup.setLexicalValue("");
                            nodeConceptGroup.setIdLang(idLang);
                        }
                        nodeConceptGroup.setConceptGroup(conceptGroup);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while adding element : " + idThesaurus, sqle);
        }
        return nodeConceptGroup;
    }
}
