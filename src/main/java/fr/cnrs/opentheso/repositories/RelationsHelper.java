package fr.cnrs.opentheso.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.models.relations.HierarchicalRelationship;
import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.concept.NodeConceptType;
import fr.cnrs.opentheso.models.relations.NodeCustomRelation;
import fr.cnrs.opentheso.models.relations.NodeHieraRelation;
import fr.cnrs.opentheso.models.terms.NodeNT;
import fr.cnrs.opentheso.models.terms.NodeRT;
import fr.cnrs.opentheso.models.relations.NodeRelation;
import fr.cnrs.opentheso.models.relations.NodeTypeRelation;
import fr.cnrs.opentheso.models.concept.NodeUri;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;


@Slf4j
@Service
public class RelationsHelper {

    @Autowired
    private DataSource dataSource;


    /**
     * permet de retourner les informations sur le type du concept
     */
    public NodeConceptType getNodeTypeConcept(String conceptType, String idTheso) {

        NodeConceptType nodeConceptType = new NodeConceptType();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from concept_type"
                        + " where "
                        + " code = '" + conceptType + "'" 
                        + " and id_theso in ('" + idTheso + "', 'all')"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeConceptType.setCode(conceptType);
                        nodeConceptType.setLabelFr(resultSet.getString("label_fr"));
                        nodeConceptType.setLabelEn(resultSet.getString("label_en"));
                        nodeConceptType.setReciprocal(resultSet.getBoolean("reciprocal"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting NT of Concept : " + conceptType, sqle);
        }
        return nodeConceptType;
    }    
    
    
    /**
     * permet de retourner la liste des termes de type Qualifier avec les libellés
     */
    public ArrayList<NodeCustomRelation> getAllNodeCustomRelation(String idConcept, String idThesaurus, String idLang, String interfaceLang) {

        ArrayList<NodeCustomRelation> nodeCustomRelations = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept2, role from hierarchical_relationship, concept" +
                        " where hierarchical_relationship.id_concept2 = concept.id_concept" +
                        " and hierarchical_relationship.id_thesaurus = concept.id_thesaurus" +
                        " and hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'" +
                        " and id_concept1 = '" + idConcept + "'" +
                        " and role not in ('BT', 'BTG', 'BTP', 'BTI', 'NT', 'NTG', 'NTP', 'NTI', 'RT')" +
                        " and concept.status != 'CA'"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeCustomRelation nodeCustomRelation = new NodeCustomRelation();
                        nodeCustomRelation.setTargetConcept(resultSet.getString("id_concept2"));
                        nodeCustomRelation.setRelation(resultSet.getString("role"));
                        nodeCustomRelations.add(nodeCustomRelation);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting NT of Concept : " + idConcept, sqle);
        }
        for (NodeCustomRelation nodeCustomRelation : nodeCustomRelations) {
            nodeCustomRelation.setTargetLabel(getLexicalValueOfConcept(nodeCustomRelation.getTargetConcept(), idThesaurus, idLang));
            nodeCustomRelation = getLabelOfCustomRelation(nodeCustomRelation.getRelation(), idThesaurus, interfaceLang, nodeCustomRelation);
        }
        return nodeCustomRelations;
    }

    private String getLexicalValueOfConcept(String idConcept, String idThesaurus, String idLang) {

        String lexicalValue = "";
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lexical_value from term, preferred_term where"
                        + " preferred_term.id_term = term.id_term AND"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and term.id_thesaurus = '" + idThesaurus + "'"
                        + " and preferred_term.id_concept = '" + idConcept + "'"
                        + " and term.lang = '" + idLang + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        lexicalValue = resultSet.getString("lexical_value");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting LexicalValue of Concept : " + idConcept, sqle);
        }
        return lexicalValue.trim();
    }
    
    /**
     * permet de retourner les infos sur un type de concept
     */
    private NodeCustomRelation getLabelOfCustomRelation(String customRelation, String idTheso,
                                                        String interfaceLang, NodeCustomRelation nodeCustomRelation) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select label_" + interfaceLang +  ", reciprocal from concept_type" +
                        " where code = '" + customRelation.toLowerCase() + "'" +
                        " and id_theso in ('" + idTheso + "', 'all')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next()) {
                        nodeCustomRelation.setRelationLabel(resultSet.getString("label_" + interfaceLang));
                        nodeCustomRelation.setReciprocal(resultSet.getBoolean("reciprocal"));
                        return nodeCustomRelation;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting label of Custom Relation : " + customRelation, sqle);
        }
        return null;
    }      
    
    /**
     * permet de retourner le label du type de concept
     */
    public String getLabelOfTypeConcept(String customRelation, String idTheso, String idLang) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select label_" + idLang +  " from concept_type" +
                        " where code = '" + customRelation + "'" +
                        " and id_theso in ('" + idTheso + "', 'all')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next()) {
                        return resultSet.getString("label_" + idLang);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting label of Custom Relation : " + customRelation, sqle);
        }
        return "";
    }      
    
    
    /**
     * permet de retourner la liste des termes spécifiques avec le libellé ##MR
     * ajout de limitNT, si = -1, pas de limit pour gérer la récupération par
     * saut (offset 42 fetch next 21 rows only)
     */
    public ArrayList<NodeNT> getListNT(String idConcept, String idThesaurus, String idLang, int step, int offset) {

        ArrayList<NodeNT> nodeListNT = new ArrayList<>();
        String limit = "";
        if (step != -1) {
            limit = " offset " + offset + " fetch next " + step + " rows only";
        }

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept2, role from hierarchical_relationship, concept"
                        + " where hierarchical_relationship.id_concept2 = concept.id_concept"
                        + " and hierarchical_relationship.id_thesaurus = concept.id_thesaurus"
                        + " and hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'"
                        + " and id_concept1 = '" + idConcept + "'"
                        + " and role LIKE 'NT%'"
                        + " and concept.status != 'CA'" + limit
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeNT nodeNT = new NodeNT();
                        nodeNT.setIdConcept(resultSet.getString("id_concept2"));
                        nodeNT.setRole(resultSet.getString("role"));
                        nodeListNT.add(nodeNT);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting NT of Concept : " + idConcept, sqle);
        }
        for (NodeNT nodeNT : nodeListNT) {
            nodeNT.setTitle(getLexicalValueOfConcept(nodeNT.getIdConcept(), idThesaurus, idLang));
        }
        Collections.sort(nodeListNT);
        return nodeListNT;
    }

    /**
     * Cette fonction permet de récupérer les termes associés d'un concept
     */
    public ArrayList<NodeRT> getListRT(String idConcept, String idThesaurus, String idLang) {

        ArrayList<NodeRT> nodeListRT = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {

                stmt.executeQuery("select id_concept2,role, status from hierarchical_relationship, concept"
                        + " where hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'"
                        + " and hierarchical_relationship.id_concept2 = concept.id_concept"
                        + " and hierarchical_relationship.id_thesaurus = concept.id_thesaurus"
                        + " and id_concept1 = '" + idConcept + "'"
                        + " and role = 'RT'"
                        + " and concept.status != 'CA'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeRT nodeRT = new NodeRT();
                        nodeRT.setIdConcept(resultSet.getString("id_concept2"));
                        nodeRT.setStatus(resultSet.getString("status"));
                        nodeRT.setRole(resultSet.getString("role"));
                        nodeListRT.add(nodeRT);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting RT of Concept : " + idConcept, sqle);
        }
        for (NodeRT nodeRT : nodeListRT) {
            nodeRT.setTitle(getLexicalValueOfConcept(nodeRT.getIdConcept(), idThesaurus, idLang));
        }
        Collections.sort(nodeListRT);
        return nodeListRT;
    }

    /**
     * récupération des TopTerms qui ont au moins une hiérarchie fonction pour
     * la correction des cohérences
     */
    public ArrayList<String> getListIdOfTopTermForRepair(String idThesaurus) {

        ArrayList<String> listIds = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select DISTINCT hierarchical_relationship.id_concept1 from hierarchical_relationship where"
                        + " hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'"
                        + " AND"
                        + " hierarchical_relationship.role like 'NT%'"
                        + " AND"
                        + " hierarchical_relationship.id_concept1 not in "
                        + " (select hierarchical_relationship.id_concept2 from hierarchical_relationship where"
                        + " hierarchical_relationship.id_thesaurus = '" + idThesaurus + "' and"
                        + " (hierarchical_relationship.role not like 'BT%'"
                        + " AND "
                        + " hierarchical_relationship.role not like 'RT%'))");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIds.add(resultSet.getString("id_concept1"));
                    }
                }

                stmt.executeQuery("select id_concept from concept where concept.id_thesaurus = '" + idThesaurus + "'"
                        + " and concept.id_concept not in (select DISTINCT hierarchical_relationship.id_concept1 from hierarchical_relationship where"
                        + " hierarchical_relationship.id_thesaurus = '" + idThesaurus + "' AND hierarchical_relationship.role not like 'RT%')");

                try (ResultSet resultSet1 = stmt.getResultSet()) {
                    while (resultSet1.next()) {
                        listIds.add(resultSet1.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All TopTerm for Repair : " + idThesaurus, sqle);
            listIds.clear();
        }
        return listIds;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concepts avec les
     * relations BT, NT, RT les identifiants pérennes (Ark, Handle) sert à
     * l'export des données
     */
    public ArrayList<NodeHieraRelation> getAllRelationsOfConcept(String idConcept, String idThesaurus) {

        ResultSet resultSet = null;
        ArrayList<NodeHieraRelation> nodeListIdOfConcept = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()){
            try (Statement stmt = conn.createStatement()){
                String query = "select id_concept2, role, id_ark, id_handle, id_doi  "
                        + " from hierarchical_relationship, concept "
                        + " where "
                        + " hierarchical_relationship.id_concept2 = concept.id_concept"
                        + " and"
                        + " hierarchical_relationship.id_thesaurus = concept.id_thesaurus "
                        + " and"
                        + " hierarchical_relationship.id_thesaurus = '" + idThesaurus + "' "
                        + " and"
                        + " hierarchical_relationship.id_concept1 = '" + idConcept + "'"
                        + " and concept.status != 'CA'";
                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
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

                    nodeHieraRelation.setRole(resultSet.getString("role"));
                    nodeHieraRelation.setUri(nodeUri);
                    nodeListIdOfConcept.add(nodeHieraRelation);
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste ID of BT Concept with ark and handle : " + idConcept, sqle);
        }
        return nodeListIdOfConcept;
    }


    /**
     * permet de changer la relation entre deux concepts concept1 = concept de
     * départ concept2 = concept d'arriver directRelation = la relation à mettre
     * en place exp NT, NTI ...inverseRelation = la relation reciproque qu'il
     * faut ajouter exp : BT, BTI ...
     */
    public boolean updateRelationNT(String idConcept1, String idConcept2, String idTheso,
            String directRelation, String inverseRelation, int idUser) {

        boolean status = false;
        try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()){
            String query = "update hierarchical_relationship"
                    + " set role = '" + directRelation + "'"
                    + " where id_concept1 = '" + idConcept1 + "'"
                    + " and id_thesaurus = '" + idTheso + "'"
                    + " and id_concept2 ='" + idConcept2 + "'";

            stmt.executeUpdate(query);
            query = "update hierarchical_relationship"
                    + " set role = '" + inverseRelation + "'"
                    + " where id_concept1 = '" + idConcept2 + "'"
                    + " and id_thesaurus = '" + idTheso + "'"
                    + " and id_concept2 ='" + idConcept1 + "'";

            stmt.executeUpdate(query);
            if (!addRelationHistorique(conn, idConcept1, idTheso, idConcept2, directRelation, idUser, "Update")) {
                return false;
            }
            status = true;
        } catch (SQLException sqle) {
            log.error("Error while updating hierarchical relation of Concept : " + idConcept1, sqle);
        }
        return status;
    }

    /**
     * permet de retourner les types de relations possibles en spécifique
     */
    public ArrayList<NodeTypeRelation> getTypesRelationsNT() {

        ResultSet resultSet = null;
        ArrayList<NodeTypeRelation> typesRelationsNT = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    resultSet = stmt.executeQuery("select relation, description_fr, description_en from nt_type");
                    typesRelationsNT = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeTypeRelation nodeTypeRelation = new NodeTypeRelation();
                        nodeTypeRelation.setRelationType(resultSet.getString("relation"));
                        nodeTypeRelation.setDescriptionFr(resultSet.getString("description_fr"));
                        nodeTypeRelation.setDescriptionEn(resultSet.getString("description_en"));
                        typesRelationsNT.add(nodeTypeRelation);
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
            log.error("Error while getting type Of relations NT ", sqle);
        }
        return typesRelationsNT;
    }

    /**
     * Cette fonction permet d'ajouter une relation à la table
     * hierarchicalRelationship Sert à l'import
     */
    public boolean insertHierarchicalRelation(String idConcept1, String idTheso, String role, String idConcept2) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2)"
                        + " values ('" + idConcept1 + "','" + idTheso + "','" + role + "','" + idConcept2 + "')");
                return true;
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                return true;
            } else {
                log.error("Error while adding hierarchical relation of Concept : " + idConcept1, sqle);
                return false;
            }
        }
    }

    public boolean insertHierarchicalRelation(Connection conn, String idConcept1, String idTheso, String role, String idConcept2) {

        Statement stmt;
        boolean status = false;
        try {
            stmt = conn.createStatement();
            try {
                String query = "Insert into hierarchical_relationship"
                        + "(id_concept1, id_thesaurus, role, id_concept2)"
                        + " values ("
                        + "'" + idConcept1 + "'"
                        + ",'" + idTheso + "'"
                        + ",'" + role + "'"
                        + ",'" + idConcept2 + "')";
                stmt.executeUpdate(query);
                status = true;
            } finally {
                stmt.close();
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding hierarchical relation of Concept : " + idConcept1, sqle);
            } else {
                status = true;
            }

        }
        return status;
    }

    /**
     * Cette fonction permet de rajouter une relation type Groupe ou domaine à
     * un concept
     */
    public boolean addRelationMT(Connection conn, String idConcept, String idThesaurus, String idGroup) {

        Statement stmt;
        boolean status = false;

        String query;
        Savepoint savepoint = null;

        try {
            // Get connection from pool
            savepoint = conn.setSavepoint();
            try {
                stmt = conn.createStatement();
                try {
                    query = "Insert into concept_group_concept"
                            + "(idgroup, idthesaurus, idconcept)"
                            + " values ("
                            + "'" + idGroup + "'"
                            + ",'" + idThesaurus + "'"
                            + ",'" + idConcept + "')";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //    conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                try {
                    if (savepoint != null) {
                        conn.rollback(savepoint);
                        status = true;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(RelationsHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                log.error("Error while adding relation Group of Concept : " + idConcept, sqle);
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de rÃ©cupÃ©rer les termes gÃ©nÃ©riques d'un concept
     *
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet class Concept
     */
    public ArrayList<NodeBT> getListBT(String idConcept, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeBT> nodeListBT = null;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT hierarchical_relationship.id_concept2,"
                            + " concept.status, hierarchical_relationship.role FROM hierarchical_relationship,"
                            + " concept WHERE "
                            + " concept.id_thesaurus = hierarchical_relationship.id_thesaurus"
                            + " AND "
                            + " concept.id_concept = hierarchical_relationship.id_concept1"
                            + " AND"
                            + " hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'"
                            + " AND"
                            + " hierarchical_relationship.id_concept1 = '" + idConcept + "'"
                            + " AND"
                            + " hierarchical_relationship.role LIKE 'BT%'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeListBT = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeBT nodeBT = new NodeBT();
                            nodeBT.setIdConcept(resultSet.getString("id_concept2"));
                            nodeBT.setRole(resultSet.getString("role"));
                            nodeBT.setStatus(resultSet.getString("status"));
                            nodeListBT.add(nodeBT);
                        }
                    }
                    for (NodeBT nodeBT : nodeListBT) {
                        query = "SELECT term.lexical_value, term.status FROM term, preferred_term"
                                + " WHERE preferred_term.id_term = term.id_term"
                                + " and preferred_term.id_thesaurus = term.id_thesaurus "
                                + " and preferred_term.id_concept ='" + nodeBT.getIdConcept() + "'"
                                + " and term.lang = '" + idLang + "'"
                                + " and term.id_thesaurus = '" + idThesaurus + "'"
                                + " order by upper(unaccent_string(lexical_value)) DESC";

                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        if (resultSet != null) {
                            resultSet.next();
                            if (resultSet.getRow() == 0) {
                                nodeBT.setTitle("");
                                nodeBT.setStatus("");
                            } else {
                                if (resultSet.getString("lexical_value") == null || resultSet.getString("lexical_value").equals("")) {
                                    nodeBT.setTitle("");
                                } else {
                                    nodeBT.setTitle(resultSet.getString("lexical_value"));
                                }
                                if (resultSet.getString("status") == null || resultSet.getString("status").equals("")) {
                                    nodeBT.setStatus("");
                                } else {
                                    nodeBT.setStatus(resultSet.getString("status"));
                                }

                            }
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
            log.error("Error while getting BT of Concept : " + idConcept, sqle);
        }
        Collections.sort(nodeListBT);
        return nodeListBT;
    }

    /**
     * Cette fonction permet de récupérer la liste des Ids des termes génériques
     * d'un concept
     */
    public ArrayList<String> getListIdOfBT(String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<String> listIdOfBt = new ArrayList<>();

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2 from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'BT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        listIdOfBt.add(resultSet.getString("id_concept2"));
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
            log.error("Error while getting List Ids of BT of Concept : " + idConcept, sqle);
        }
        return listIdOfBt;
    }

    /**
     * Cette fonction permet de récupérer la liste des relations qui sont en
     * boucle pour une relation donnée (NT, BT, RT)
     *
     * @param role
     * @param idThesaurus
     * @return #MR
     */
    public ArrayList<HierarchicalRelationship> getListLoopRelations(String role, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<HierarchicalRelationship> listRelations = new ArrayList<>();

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select * from hierarchical_relationship"
                            + " where id_concept1 = id_concept2"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and role = '" + role + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        HierarchicalRelationship hierarchicalRelationship = new HierarchicalRelationship();
                        hierarchicalRelationship.setIdConcept1(resultSet.getString("id_concept1"));
                        hierarchicalRelationship.setIdConcept2(resultSet.getString("id_concept2"));
                        hierarchicalRelationship.setIdThesaurus(idThesaurus);
                        hierarchicalRelationship.setRole(role);
                        listRelations.add(hierarchicalRelationship);
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
            log.error("Error while getting List of Loop relations of thesaurus : " + idThesaurus, sqle);
        }
        return listRelations;
    }

    /**
     * Cette fonction permet d'ajouter une relation personnalisée entre le concept1 et le concept2
     *
     * @param idConcept1
     * @param idConcept2
     * @param idThesaurus
     * @param idUser
     * @param relationType
     * @param isReciprocal
     * @return boolean
     */
    public boolean addCustomRelationship(String idConcept1, String idThesaurus, String idConcept2, int idUser,
            String relationType, boolean isReciprocal) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into hierarchical_relationship"
                            + "(id_concept1, id_thesaurus, role, id_concept2)"
                            + " values ("
                            + "'" + idConcept1 + "'"
                            + ",'" + idThesaurus + "'"
                            + ",'" + relationType + "'"
                            + ",'" + idConcept2 + "') ON CONFLICT DO NOTHING");
                if(isReciprocal) {
                    stmt.executeUpdate("Insert into hierarchical_relationship"
                                + "(id_concept1, id_thesaurus, role, id_concept2)"
                                + " values ("
                                + "'" + idConcept2 + "'"
                                + ",'" + idThesaurus + "'"
                                + ",'" + relationType + "'"
                                + ",'" + idConcept1 + "') ON CONFLICT DO NOTHING");                    
                }
            }
            addRelationHistorique(conn, idConcept1, idThesaurus, idConcept2, relationType, idUser, "ADD");
            return true;
        } catch (SQLException sqle) {
            log.error("Error while adding relation RT of Concept : " + idConcept1, sqle);
        }
        return false;
    }    
    
    /**
     * Cette fonction permet de rajouter une relation associative entre deux
     * concepts
     *
     * @param conn
     * @param idConcept1
     * @param idConcept2
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean addRelationRT(Connection conn, String idConcept1, String idThesaurus, String idConcept2, int idUser) {

        Statement stmt;
        boolean status = false;

        try {
            stmt = conn.createStatement();
            try {
                if (!addRelationHistorique(conn, idConcept1, idThesaurus, idConcept2, "RT", idUser, "ADD")) {
                    return false;
                }

                String query = "Insert into hierarchical_relationship"
                        + "(id_concept1, id_thesaurus, role, id_concept2)"
                        + " values ("
                        + "'" + idConcept1 + "'"
                        + ",'" + idThesaurus + "'"
                        + ",'RT'"
                        + ",'" + idConcept2 + "')";

                stmt.executeUpdate(query);
                query = "Insert into hierarchical_relationship"
                        + "(id_concept1, id_thesaurus, role, id_concept2)"
                        + " values ("
                        + "'" + idConcept2 + "'"
                        + ",'" + idThesaurus + "'"
                        + ",'RT'"
                        + ",'" + idConcept1 + "')";
                stmt.executeUpdate(query);
                status = true;
                // conn.commit();
            } finally {
                stmt.close();
            }

        } catch (SQLException sqle) {
            // Log exception
            //  if (sqle.getMessage().contains("duplicate key value violates unique constraint")) {

            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding relation RT of Concept : " + idConcept1, sqle);
            } else {
                status = true;
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de rajouter une relation associative entre deux
     * concepts
     *
     * @param idConcept1
     * @param idConcept2
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean addRelationRT(String idConcept1, String idThesaurus, String idConcept2, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if (!addRelationHistorique(conn, idConcept1, idThesaurus, idConcept2, "RT", idUser, "ADD")) {
                        return false;
                    }

                    String query = "Insert into hierarchical_relationship"
                            + "(id_concept1, id_thesaurus, role, id_concept2)"
                            + " values ("
                            + "'" + idConcept1 + "'"
                            + ",'" + idThesaurus + "'"
                            + ",'RT'"
                            + ",'" + idConcept2 + "')";

                    stmt.executeUpdate(query);
                    query = "Insert into hierarchical_relationship"
                            + "(id_concept1, id_thesaurus, role, id_concept2)"
                            + " values ("
                            + "'" + idConcept2 + "'"
                            + ",'" + idThesaurus + "'"
                            + ",'RT'"
                            + ",'" + idConcept1 + "')";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                status = true;
            } else {
                log.error("Error while adding relation RT of Concept : " + idConcept1, sqle);
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de rajouter une relation terme gÃ©nÃ©rique Ã  un
     * concept
     *
     * @param idConceptNT
     * @param idThesaurus
     * @param idConceptBT
     * @param idUser
     * @return boolean
     */
    public boolean addRelationBT(String idConceptNT, String idThesaurus, String idConceptBT, int idUser) {

        boolean status = false;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                if (!addRelationHistorique(conn, idConceptNT, idThesaurus, idConceptBT, "BT", idUser, "ADD")) {
                    return false;
                }

                stmt.executeUpdate("Insert into hierarchical_relationship"
                        + "(id_concept1, id_thesaurus, role, id_concept2)"
                        + " values ('" + idConceptNT + "', '" + idThesaurus + "'"
                        + ",'BT', '" + idConceptBT + "')");

                stmt.executeUpdate("Insert into hierarchical_relationship"
                        + "(id_concept1, id_thesaurus, role, id_concept2)"
                        + " values ('" + idConceptBT + "', '" + idThesaurus + "'"
                        + ",'NT', '" + idConceptNT + "')");

                status = true;
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding relation BT of Concept : " + idConceptNT, sqle);
            } else {
                status = true;
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de rajouter une relation terme gÃ©nÃ©rique Ã  un
     * concept
     *
     * @param conn
     * @param idConceptNT
     * @param idThesaurus
     * @param idConceptBT
     * @param idUser
     * @return boolean
     */
    public boolean addRelationBT(Connection conn, String idConceptNT, String idThesaurus,
            String idConceptBT, int idUser) {

        boolean status = false;

        try (Statement stmt = conn.createStatement()) {

            if (!addRelationHistorique(conn, idConceptNT, idThesaurus, idConceptBT, "BT", idUser, "ADD")) {
                return false;
            }

            stmt.executeUpdate("Insert into hierarchical_relationship"
                    + "(id_concept1, id_thesaurus, role, id_concept2)"
                    + " values ('" + idConceptNT + "', '" + idThesaurus + "'"
                    + ",'BT', '" + idConceptBT + "')");

            stmt.executeUpdate("Insert into hierarchical_relationship"
                    + "(id_concept1, id_thesaurus, role, id_concept2)"
                    + " values ('" + idConceptBT + "', '" + idThesaurus + "'"
                    + ",'NT', '" + idConceptNT + "')");

            status = true;
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding relation BT of Concept : " + idConceptNT, sqle);
            } else {
                status = true;
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de rajouter une relation terme spécifique à un
     * concept
     *
     * @param idConcept
     * @param idConceptNT
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean addRelationNT(String idConcept, String idThesaurus, String idConceptNT, int idUser) {

        boolean status = false;
        try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()){
            if (!addRelationHistorique(conn, idConcept, idThesaurus, idConceptNT, "NT", idUser, "ADD")) {
                return false;
            }

            String query = "Insert into hierarchical_relationship"
                    + "(id_concept1, id_thesaurus, role, id_concept2)"
                    + " values ("
                    + "'" + idConcept + "'"
                    + ",'" + idThesaurus + "'"
                    + ",'NT'"
                    + ",'" + idConceptNT + "')";

            stmt.executeUpdate(query);
            query = "Insert into hierarchical_relationship"
                    + "(id_concept1, id_thesaurus, role, id_concept2)"
                    + " values ("
                    + "'" + idConceptNT + "'"
                    + ",'" + idThesaurus + "'"
                    + ",'BT'"
                    + ",'" + idConcept + "')";
            stmt.executeUpdate(query);
            status = true;
        } catch (SQLException sqle) {
            // Log exception
            //  if (sqle.getMessage().contains("duplicate key value violates unique constraint")) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding relation NT : " + idConcept + "-> " + idConceptNT, sqle);
            } else {
                status = true;
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de rajouter une relation dans l'historique
     *
     * @param conn
     * @param idConcept1
     * @param idThesaurus
     * @param idConcept2
     * @param role
     * @param idUser
     * @param action
     * @return boolean
     */
    public boolean addRelationHistorique(Connection conn, String idConcept1, String idThesaurus,
            String idConcept2, String role, int idUser, String action) {

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Insert into hierarchical_relationship_historique"
                    + "(id_concept1, id_thesaurus, role, id_concept2, id_user, action)"
                    + " values ("
                    + "'" + idConcept1 + "'"
                    + ",'" + idThesaurus + "'"
                    + ",'" + role + "'"
                    + ",'" + idConcept2 + "'"
                    + ",'" + idUser + "'"
                    + ",'" + action + "')");
            return true;
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding relation historique of Concept : " + idConcept1, sqle);
                return false;
            } else {
                return true;
            }
        }
    }
    
    public NodeRelation getLoopRelation(
             String idTheso, String idConcept){
        // récupération la relation en Loop
        NodeRelation nodeRelation = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept1, role, id_concept2 from hierarchical_relationship " +
                        "where " +
                        "id_concept1 = '" + idConcept + "' and role = 'BT' " +
                        "and id_thesaurus = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeRelation = new NodeRelation();
                        nodeRelation.setIdConcept1(resultSet.getString("id_concept1"));
                        nodeRelation.setRelation(resultSet.getString("role"));
                        nodeRelation.setIdConcept2(resultSet.getString("id_concept2"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }        
        if(nodeRelation == null) return null;
        
        NodeRelation nodeRelation2 = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept2, role, id_concept1 from hierarchical_relationship " +
                        "where " +
                        "id_concept1 = '" + nodeRelation.getIdConcept1() + "' and role = 'BT' and id_concept2 = '" + nodeRelation.getIdConcept2() + "' and id_thesaurus = '" + idTheso + "'" +
                        "and id_concept1 IN" +
                        "(select id_concept2 from hierarchical_relationship where " +
                        "id_concept1 = '" + nodeRelation.getIdConcept2() +"' and role = 'BT' and id_concept2 = '" + nodeRelation.getIdConcept1() + "' and id_thesaurus = '" + idTheso + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeRelation2 = new NodeRelation();
                        nodeRelation2.setIdConcept2(resultSet.getString("id_concept2"));
                        nodeRelation2.setRelation(resultSet.getString("role"));
                        nodeRelation2.setIdConcept1(resultSet.getString("id_concept1"));
                    }
                }
                return nodeRelation2;
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return null;          
    }

    /**
     * Cette fonction permet de supprimer une relation terme gÃ©nÃ©rique Ã  un
     * concept
     *
     * @param idConceptNT
     * @param idThesaurus
     * @param idConceptBT
     * @param idUser
     * @return boolean
     */
    public boolean deleteRelationBT(String idConceptNT, String idThesaurus, String idConceptBT, int idUser) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                if (!addRelationHistorique(conn, idConceptNT, idThesaurus, idConceptBT, "BT", idUser, "DEL")) {
                    conn.rollback();
                    conn.close();
                    return false;
                }

                stmt.executeUpdate("delete from hierarchical_relationship"
                        + " where id_concept1 ='" + idConceptNT + "'"
                        + " and id_thesaurus = '" + idThesaurus + "'"
                        + " and role LIKE 'BT%'"
                        + " and id_concept2 = '" + idConceptBT + "'");

                stmt.executeUpdate("delete from hierarchical_relationship"
                        + " where id_concept1 ='" + idConceptBT + "'"
                        + " and id_thesaurus = '" + idThesaurus + "'"
                        + " and role LIKE 'NT%'"
                        + " and id_concept2 = '" + idConceptNT + "'");
                return true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting relation BT of Concept : " + idConceptNT, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de supprimer une relation terme gÃ©nÃ©rique Ã  un
     * concept
     *
     * @param conn
     * @param idConceptNT
     * @param idThesaurus
     * @param idConceptBT
     * @param idUser
     * @return boolean
     */
    public boolean deleteRelationBT(Connection conn, String idConceptNT, String idThesaurus, String idConceptBT, int idUser) {

        boolean status = false;

        if (!addRelationHistorique(conn, idConceptNT, idThesaurus, idConceptBT, "BT", idUser, "DEL")) {
            return false;
        }
        try (Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("delete from hierarchical_relationship"
                    + " where id_concept1 ='" + idConceptNT + "'"
                    + " and id_thesaurus = '" + idThesaurus + "'"
                    + " and role LIKE 'BT%'"
                    + " and id_concept2 = '" + idConceptBT + "'");

            stmt.executeUpdate("delete from hierarchical_relationship"
                    + " where id_concept1 ='" + idConceptBT + "'"
                    + " and id_thesaurus = '" + idThesaurus + "'"
                    + " and role LIKE 'NT%'"
                    + " and id_concept2 = '" + idConceptNT + "'");

            status = true;
        } catch (SQLException sqle) {
            // Log exception
            System.out.println("ERREUR >> " + sqle);
            log.error("Error while deleting relation BT of Concept : " + idConceptNT, sqle);
        }
        return status;
    }

        
    /**
     * Cette fonction permet de supprimer une relation qualificatif à  un
     * concept
     *
     * @param idConcept1
     * @param idThesaurus
     * @param idConcept2
     * @param idUser
     * @param conceptType
     * @param isReciprocal
     * @return boolean
     */
    public boolean deleteCustomRelationship(String idConcept1, String idThesaurus, String idConcept2, int idUser,
                                            String conceptType, boolean isReciprocal) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from hierarchical_relationship"
                            + " where id_concept1 ='" + idConcept1 + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and role = '" + conceptType + "'"
                            + " and id_concept2 = '" + idConcept2 + "'");
                if(isReciprocal){
                    stmt.executeUpdate("delete from hierarchical_relationship"
                                + " where id_concept2 ='" + idConcept1 + "'"
                                + " and id_thesaurus = '" + idThesaurus + "'"
                                + " and role = '" + conceptType + "'"
                                + " and id_concept1 = '" + idConcept2 + "'");                    
                }
                addRelationHistorique(conn, idConcept1, idThesaurus, idConcept2, "QUALIFIER", idUser, "delete");
                return true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting relation QUALIFIER of Concept : " + idConcept1, sqle);
        }
        return true;
    }        
        
    /**
     * Cette fonction permet de supprimer une relation terme associé à  un
     * concept
     *
     * @param idConcept1
     * @param idThesaurus
     * @param idConcept2
     * @param idUser
     * @return boolean
     */
    public boolean deleteRelationRT(String idConcept1, String idThesaurus, String idConcept2, int idUser) {

        Connection conn = null;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {

                    if (!addRelationHistorique(conn, idConcept1, idThesaurus, idConcept2, "RT", idUser, "delete")) {
                        conn.rollback();
                        conn.close();
                        return false;
                    }

                    String query = "delete from hierarchical_relationship"
                            + " where id_concept1 ='" + idConcept1 + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and role = 'RT'"
                            + " and id_concept2 = '" + idConcept2 + "'";

                    stmt.executeUpdate(query);
                    query = "delete from hierarchical_relationship"
                            + " where id_concept1 ='" + idConcept2 + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and role = 'RT'"
                            + " and id_concept2 = '" + idConcept1 + "'";

                    stmt.executeUpdate(query);
                    status = true;
                    conn.commit();
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting relation RT of Concept : " + idConcept1, sqle);
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(RelationsHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de supprimer une relation terme spécifique entre le
     * concept1 et le concept2 si le concept2 n'a plus de (BT-TG, il devient
     * TopTerme)
     *
     * @param idConcept1
     * @param idThesaurus
     * @param idConcept2
     * @param idUser
     * @return boolean
     */
    public boolean deleteRelationNT(String idConcept1, String idThesaurus, String idConcept2, int idUser) {

        Connection conn = null;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    if (!addRelationHistorique(conn, idConcept1, idThesaurus, idConcept2, "RT", idUser, "delete")) {
                        conn.rollback();
                        conn.close();
                        return false;
                    }
                    String query = "delete from hierarchical_relationship"
                            + " where id_concept1 ='" + idConcept1 + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and role = 'NT'"
                            + " and id_concept2 = '" + idConcept2 + "'";
                    stmt.executeUpdate(query);

                    query = "delete from hierarchical_relationship"
                            + " where id_concept1 ='" + idConcept2 + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and role = 'BT'"
                            + " and id_concept2 = '" + idConcept1 + "'";
                    stmt.executeUpdate(query);
                    status = true;
                    conn.commit();
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting relation RT of Concept : " + idConcept1, sqle);
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(RelationsHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de supprimer une relation bien définie c'est à dire
     * une ligne dans la table Sert pour corriger les incohérences
     *
     * @param idConcept1
     * @param idThesaurus
     * @param role
     * @param idConcept2
     * @return boolean
     */
    public boolean deleteThisRelation(String idConcept1, String idThesaurus, String role, String idConcept2) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from hierarchical_relationship"
                            + " where id_concept1 ='" + idConcept1 + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and role = '" + role + "'"
                            + " and id_concept2 = '" + idConcept2 + "'";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting one relation of Concept : " + idConcept1, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de supprimer la relation MT ou domaine à un concept
     *
     * @param conn
     * @param idConcept
     * @param idGroup
     * @param idThesaurus
     * @return boolean
     */
    public boolean deleteRelationMT(Connection conn,
            String idConcept, String idGroup, String idThesaurus) {

        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_group_concept"
                            + " WHERE idconcept ='" + idConcept + "'"
                            + " AND idthesaurus = '" + idThesaurus + "'"
                            + " AND idgroup = '" + idGroup + "'";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //    conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting relation TT of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet d'ajouter une relation MT ou domaine à un concept
     *
     * @param conn
     * @param idConcept
     * @param idGroup
     * @param idThesaurus
     * @return boolean
     */
    public boolean setRelationMT(Connection conn, String idConcept, String idGroup, String idThesaurus) {

        Statement stmt;
        boolean status = false;
        String query;
        Savepoint savepoint = null;

        try {
            // Get connection from pool
            savepoint = conn.setSavepoint();
            try {
                stmt = conn.createStatement();
                try {
                    query = "UPDATE concept_group_concept set"
                            + " idgroup = '" + idGroup + "'"
                            + " WHERE idconcept ='" + idConcept + "'"
                            + " AND idthesaurus = '" + idThesaurus + "'";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //    conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                try {
                    if (savepoint != null) {
                        conn.rollback(savepoint);
                        status = true;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(RelationsHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                log.error("Error while deleting relation Group for Concept : " + idConcept, sqle);
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de supprimer toutes les relations d'un concept
     *
     * @param idConcept
     * @param idThesaurus
     * @return boolean
     */
    public boolean deleteAllRelationOfConcept(String idConcept, String idThesaurus) {

        boolean status = false;

        try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()){
            String query = "delete from hierarchical_relationship"
                    + " where id_concept1 ='" + idConcept + "'"
                    + " and id_thesaurus = '" + idThesaurus + "'";

            stmt.executeUpdate(query);
            query = "delete from hierarchical_relationship"
                    + " where id_concept2 ='" + idConcept + "'"
                    + " and id_thesaurus = '" + idThesaurus + "'";

            stmt.executeUpdate(query);

            status = true;
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting All relations of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id des termes génériques
     * d'un concept avec les identifiants pérennes (Ark, Handle) sert à l'export
     * des données
     *
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept #MR
     */
    public ArrayList<NodeHieraRelation> getListBT(String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeHieraRelation> nodeListIdOfConcept = new ArrayList<>();
        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2, role, id_ark, id_handle "
                            + " from hierarchical_relationship as hr"
                            + " left join concept as con on id_concept = id_concept2"
                            + " and hr.id_thesaurus = con.id_thesaurus"
                            + " where hr.id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'BT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
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
                        nodeUri.setIdConcept(resultSet.getString("id_concept2"));

                        nodeHieraRelation.setRole(resultSet.getString("role"));
                        nodeHieraRelation.setUri(nodeUri);
                        nodeListIdOfConcept.add(nodeHieraRelation);
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
            log.error("Error while getting Liste ID of BT Concept with ark and handle : " + idConcept, sqle);
        }
        return nodeListIdOfConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id des termes génériques
     * d'un concept
     *
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public ArrayList<String> getListIdBT(String idConcept, String idThesaurus) {

        ArrayList<String> listIdBT = null;

        try (Connection conn = dataSource.getConnection()){
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select id_concept2,role from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'BT%'");

                try (ResultSet resultSet = stmt.getResultSet()){
                    if (resultSet != null) {
                        listIdBT = new ArrayList<>();
                        while (resultSet.next()) {
                            listIdBT.add(resultSet.getString("id_concept2"));
                        }
                    }
                  /*  if (listIdBT != null) {
                        if (listIdBT.contains(idConcept)) {
                            /// relation en boucle à supprimer
                            deleteThisRelation(idConcept, idThesaurus, "BT", idConcept);
                            deleteThisRelation(idConcept, idThesaurus, "NT", idConcept);
                            getListBT(idConcept, idThesaurus);
                        }
                    }*/                    
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Liste ID of BT Concept : " + idConcept, sqle);
        }

        return listIdBT;
    }

    public ArrayList<String> getListIdWhichHaveNt(String idConcept, String idThesaurus) {

        PreparedStatement stmt;
        ResultSet rs;
        ArrayList<String> ret = new ArrayList();
        try {
            Connection conn = dataSource.getConnection();

            try {
                String sql = "SELECT id_concept1 FROM hierarchical_relationship WHERE id_concept2=? AND id_thesaurus=? AND role LIKE ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, idConcept);
                stmt.setString(2, idThesaurus);
                stmt.setString(3, "%NT%");

                try {
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        ret.add(rs.getString(1));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException e) {
            log.error("Error while getting list id of concept 1 for NT and concept2 : " + idConcept, e);
        }

        return ret;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id des termes spécifiques
     * d'un concept avec les identifiants pérennes (Ark, Handle) sert à l'export
     * des données
     *
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept #MR
     */
    public ArrayList<NodeHieraRelation> getListNT(String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeHieraRelation> nodeListIdOfConcept = new ArrayList<>();
        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2, role, id_ark, id_handle, id_doi "
                            + " from hierarchical_relationship as hr"
                            + " left join concept as con on id_concept = id_concept2"
                            + " and hr.id_thesaurus = con.id_thesaurus"
                            + " where hr.id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'NT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
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

                        nodeHieraRelation.setRole(resultSet.getString("role"));
                        nodeHieraRelation.setUri(nodeUri);
                        nodeListIdOfConcept.add(nodeHieraRelation);
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
            log.error("Error while getting Liste ID of NT Concept with ark and handle : " + idConcept, sqle);
        }
        return nodeListIdOfConcept;
    }

    /**
     * Cette fonction permet de savoir si le Concept1 a une relation RT avec le
     * concept2 permet d'éviter l'ajout des relations NT et RT en même temps
     * (c'est interdit par la norme)
     *
     * @param idConcept1
     * @param idConcept2
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean isConceptHaveRelationRT(String idConcept1, String idConcept2, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept1 from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept1 + "'"
                            + " and id_concept2 = '" + idConcept2 + "'"
                            + " and role LIKE 'RT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    existe = resultSet.next();
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
            log.error("Error while asking if relation RT exist of Concept1 : " + idConcept1 + " for concept2 : " + idConcept2, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le Concept a une relation BT (terme
     * générique)
     *
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean isConceptHaveRelationBT(String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept1 from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'BT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    existe = resultSet.next();
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
            log.error("Error while asking if relation BT exist of Concept : " + idConcept, sqle);
        }
        return existe;
    }
    
    /**
     * Cette fonction permet de savoir si le Concept au moins 2 relations BT (terme
     * générique)
     *
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean isConceptHaveManyRelationBT(String idConcept, String idThesaurus) {

        boolean existe = false;
        try (Connection conn = dataSource.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select count(id_concept1)" +
                        " from hierarchical_relationship" +
                        " where id_thesaurus = '" + idThesaurus + "'" +
                        " and id_concept1 = '" + idConcept + "'" +
                        " and role LIKE 'BT%'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next()) {
                        if(resultSet.getInt("count") > 1)
                            existe = true;
                    }
                }
            } 
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if relation BT exist of Concept : " + idConcept, sqle);
        }
        return existe;
    }    

    /**
     * Cette fonction permet de savoir si le Concept1 a une relation NT avec le
     * concept2 permet d'éviter l'ajout des relations NT et RT en même temps
     * (c'est interdit par la norme)
     *
     * @param idConcept1
     * @param idConcept2
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean isConceptHaveRelationNTorBT(String idConcept1, String idConcept2, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept1 from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept1 + "'"
                            + " and id_concept2 = '" + idConcept2 + "'"
                            + " and (role LIKE 'NT%' or role LIKE 'BT%') ";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    existe = resultSet.next();
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
            log.error("Error while asking if relation NT or BT exist of Concept1 : " + idConcept1 + " for concept2 : " + idConcept2, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le Concept1 est le frère du concept2
     * permet d'éviter l'ajout des relations NT et BT en même temps (c'est
     * interdit par la norme)
     *
     * @param idConcept1
     * @param idConcept2
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean isConceptHaveBrother(String idConcept1, String idConcept2, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2 from hierarchical_relationship"
                            + " where"
                            + " id_concept1 = '" + idConcept1 + "' and"
                            + " id_thesaurus = '" + idThesaurus + "' and"
                            + " role ilike 'BT%'"
                            + " and id_concept2 in"
                            + " (select id_concept2 from hierarchical_relationship"
                            + " where"
                            + " id_concept1 = '" + idConcept2 + "' and"
                            + " id_thesaurus = '" + idThesaurus + "' and"
                            + " role ilike 'BT%')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    existe = resultSet.next();
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
            log.error("Error while asking if relation NT or BT exist of Concept1 : " + idConcept1 + " for concept2 : " + idConcept2, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id des termes associés
     * d'un concept avec les identifiants pérennes (Ark, Handle) sert à l'export
     * des données
     *
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept #MR
     */
    public ArrayList<NodeHieraRelation> getListRT(String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeHieraRelation> nodeListIdOfConcept = new ArrayList<>();
        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2, role, id_ark, id_handle "
                            + " from hierarchical_relationship as hr"
                            + " left join concept as con on id_concept = id_concept2"
                            + " and hr.id_thesaurus = con.id_thesaurus"
                            + " where hr.id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and (role = '" + "RT" + "'"
                            + " or role = 'RHP' or role = 'RPO')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
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
                        nodeUri.setIdConcept(resultSet.getString("id_concept2"));

                        nodeHieraRelation.setRole(resultSet.getString("role"));
                        nodeHieraRelation.setUri(nodeUri);
                        nodeListIdOfConcept.add(nodeHieraRelation);
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
            log.error("Error while getting Liste ID of BT Concept with ark and handle : " + idConcept, sqle);
        }
        return nodeListIdOfConcept;
    }

}
