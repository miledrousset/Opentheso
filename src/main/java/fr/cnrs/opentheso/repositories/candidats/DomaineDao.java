package fr.cnrs.opentheso.repositories.candidats;

import com.zaxxer.hikari.HikariDataSource;

import fr.cnrs.opentheso.models.concept.NodeUri;
import fr.cnrs.opentheso.models.group.ConceptGroup;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.candidats.DomaineDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.Connection;
import java.sql.ResultSet;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class DomaineDao extends BasicDao {

    public void addNewDomaine(Connect connect, String idgroup, String idthesaurus, String idconcept) throws SQLException {
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        executInsertRequest(stmt,"INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES ('"
                +idgroup+"', '"+idthesaurus+"', '"+idconcept+"')");
        stmt.close();
    }
    
    public void deleteAllDomaine(Connect connect, String idThesaurus, String idConcept) throws SQLException {
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        executDeleteRequest(stmt,"DELETE FROM concept_group_concept WHERE idconcept = '"+idConcept+"'  AND idthesaurus = '"+idThesaurus+"'");
        stmt.close();
    }

    public void deleteDomaine(Connect connect, String idThesaurus, String idConcept, String idGroupe) throws SQLException {
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        executDeleteRequest(stmt,"DELETE FROM concept_group_concept " +
                "WHERE idconcept = '"+idConcept+"' AND idthesaurus = '"+idThesaurus+"' AND idgroup='"+idGroupe+"'");
        stmt.close();
    }

    public void updateDomaine(Connect connect, String oldIdgroup, String newIdgroup, String idthesaurus, String idconcept) throws SQLException {

        stmt = connect.getPoolConnexion().getConnection().createStatement();

        if (!StringUtils.isEmpty(newIdgroup)) {
            executInsertRequest(stmt,"INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES ('"
                    +newIdgroup+"', '"+idthesaurus+"', '"+idconcept+"')");
        } else {
            executDeleteRequest(stmt,"DELETE FROM concept_group_concept WHERE idconcept = '"+idconcept+"' AND idgroup = '"
                    +oldIdgroup+"' AND idthesaurus = '"+idthesaurus+"'");
        }

        stmt.close();
    }

    public ArrayList<NodeIdValue> getDomaineCandidatByConceptAndThesaurusAndLang(HikariDataSource hikariDataSource, String idconcept,
                                                              String idThesaurus, String lang) {

        ArrayList<NodeGroup> nodeGroups = getListGroupOfConcept(hikariDataSource, idThesaurus, idconcept, lang);
        
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        for (NodeGroup nodeGroup : nodeGroups) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setValue(nodeGroup.getLexicalValue());
            nodeIdValue.setId(nodeGroup.getConceptGroup().getIdgroup());
            nodeIdValues.add(nodeIdValue);
        }
        return nodeIdValues;
    }

    public ArrayList<NodeGroup> getListGroupOfConcept(HikariDataSource ds, String idThesaurus, String idConcept, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeGroup> nodeGroups = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group_concept where idthesaurus = '" + idThesaurus + "'"
                            + " and idconcept = '" + idConcept + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeGroup nodeGroup = getThisConceptGroup(ds, resultSet.getString("idgroup"), idThesaurus, idLang);
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

    public NodeGroup getThisConceptGroup(HikariDataSource ds,
                                         String idConceptGroup, String idThesaurus, String idLang) {

        NodeGroup nodeConceptGroup = null;
        ConceptGroup conceptGroup = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * from concept_group where "
                        + " LOWER(idgroup) = '" + idConceptGroup.toLowerCase() + "'"
                        + " and idthesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()){
                    if (resultSet.next()) {
                        if (resultSet.getRow() != 0) {
                            conceptGroup = new ConceptGroup();
                            conceptGroup.setIdgroup(idConceptGroup);
                            conceptGroup.setIdthesaurus(idThesaurus);
                            conceptGroup.setIdARk(resultSet.getString("id_ark"));
                            conceptGroup.setIdHandle(resultSet.getString("id_handle"));
                            conceptGroup.setIdtypecode(resultSet.getString("idtypecode"));
                            conceptGroup.setNotation(resultSet.getString("notation"));
                            conceptGroup.setCreated(resultSet.getDate("created"));
                            conceptGroup.setModified(resultSet.getDate("modified"));
                        }
                    }
                }
                if (conceptGroup != null) {
                    stmt.executeQuery("SELECT * FROM concept_group_label WHERE"
                            + " LOWER(idgroup) = '" + conceptGroup.getIdgroup().toLowerCase() + "'"
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
