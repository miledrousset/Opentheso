/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.datas.HierarchicalRelationship;
import fr.cnrs.opentheso.bdd.datas.Relation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeHieraRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeRT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTypeRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miled.rousset
 */
public class RelationsHelper {

    private final Log log = LogFactory.getLog(RelationsHelper.class);

    public RelationsHelper() {
    }

    //// restructuration de la classe Relations le 23/11/2018 //////    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////// Nouvelles fontions #MR//////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////        
    /**
     * permet de retourner la liste des termes spécifiques avec le libellé ##MR
     * ajout de limitNT, si = -1, pas de limit pour gérer la récupération par
     * saut (offset 42 fetch next 21 rows only)
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @param step
     * @param offset
     * @return
     */
    public ArrayList<NodeNT> getListNT(HikariDataSource ds, String idConcept, String idThesaurus, String idLang, int step, int offset) {

        ArrayList<NodeNT> nodeListNT = new ArrayList<>();
        String limit = "";
        if (step != -1) {
            limit = " offset " + offset + " fetch next " + step + " rows only";
        }

        try (Connection conn = ds.getConnection()) {
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
            nodeNT.setTitle(new ConceptHelper().getLexicalValueOfConcept(ds, nodeNT.getIdConcept(), idThesaurus, idLang));
        }
        Collections.sort(nodeListNT);
        return nodeListNT;
    }

    /**
     * Cette fonction permet de récupérer les termes associés d'un concept
     */
    public ArrayList<NodeRT> getListRT(HikariDataSource ds, String idConcept, String idThesaurus, String idLang) {

        ArrayList<NodeRT> nodeListRT = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {

                stmt.executeQuery("select id_concept2,role, status from hierarchical_relationship, concept"
                        + " where hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'"
                        + " and hierarchical_relationship.id_concept1 = concept.id_concept"
                        + " and hierarchical_relationship.id_thesaurus = concept.id_thesaurus"
                        + " and id_concept1 = '" + idConcept + "'"
                        + " and role = 'RT'");

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
            nodeRT.setTitle(new ConceptHelper().getLexicalValueOfConcept(ds, nodeRT.getIdConcept(), idThesaurus, idLang));
        }
        Collections.sort(nodeListRT);
        return nodeListRT;
    }

    /**
     * récupération des TopTerms qui ont au moins une hiérarchie fonction pour
     * la correction des cohérences
     */
    public ArrayList<String> getListIdOfTopTermForRepair(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> listIds = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
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
     * Cette fonction permet d'ajouter une relation unique qui est en paramètre
     * Fonction utilisée pour le controle de cohérence
     */
    public boolean addOneRelation(HikariDataSource ds, String idConcept1, String idThesaurus, String relation, String idConcept2) {

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2)"
                        + " values ('" + idConcept1 + "','" + idThesaurus + "','" + relation + "','" + idConcept2 + "')");
                return true;
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding relation : " + idConcept1 + " -> " + relation + " -> " + idConcept2, sqle);
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Cette fonction permet de récupérer la liste des relations d'un concept en
     * partant du concept1 (côté gauche) sans avoir les relations reciproques
     */
    public ArrayList<NodeRelation> getLeftRelationsOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeRelation> nodeRelations = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept1, role, id_concept2 "
                            + " from hierarchical_relationship "
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeRelation nodeRelation = new NodeRelation();
                        nodeRelation.setIdConcept1(resultSet.getString("id_concept1"));
                        nodeRelation.setRelation(resultSet.getString("role"));
                        nodeRelation.setIdConcept2(resultSet.getString("id_concept2"));
                        nodeRelations.add(nodeRelation);
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
            log.error("Error while getting All left relations of concept : " + idConcept, sqle);
        }
        return nodeRelations;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concepts avec les
     * relations BT, NT, RT les identifiants pérennes (Ark, Handle) sert à
     * l'export des données
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept #MR
     */
    public ArrayList<NodeHieraRelation> getAllRelationsOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeHieraRelation> nodeListIdOfConcept = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
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

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    //////// fin des nouvelles fonctions ////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////       
    /**
     * permet de changer la relation entre deux concepts concept1 = concept de
     * départ concept2 = concept d'arriver directRelation = la relation à mettre
     * en place exp NT, NTI ...inverseRelation = la relation reciproque qu'il
     * faut ajouter exp : BT, BTI ...
     *
     * @param conn
     * @param idConcept1
     * @param idConcept2
     * @param idTheso
     * @param directRelation
     * @param inverseRelation
     * @param idUser
     * @return
     */
    public boolean updateRelationNT(Connection conn,
            String idConcept1, String idConcept2, String idTheso,
            String directRelation, String inverseRelation, int idUser) {

        Statement stmt;
        boolean status = false;
        try {
            stmt = conn.createStatement();
            try {
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
            } finally {
                stmt.close();
            }
        } catch (SQLException sqle) {
            log.error("Error while updating hierarchical relation of Concept : " + idConcept1, sqle);
        }
        return status;
    }

    /**
     * permet de retourner les types de relations possibles en spécifique
     *
     * @param ds
     * @return
     */
    public ArrayList<NodeTypeRelation> getTypesRelationsNT(HikariDataSource ds) {
        ResultSet resultSet = null;
        ArrayList<NodeTypeRelation> typesRelationsNT = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    resultSet = stmt.executeQuery("select relation, description_fr, description_en from nt_type");
                    typesRelationsNT = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeTypeRelation nodeTypeRelation = new NodeTypeRelation();
                        nodeTypeRelation.setRelationType(resultSet.getString("relation"));
                        nodeTypeRelation.setDescription_fr(resultSet.getString("description_fr"));
                        nodeTypeRelation.setDescription_en(resultSet.getString("description_en"));
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
    public boolean insertHierarchicalRelation(HikariDataSource ds, String idConcept1, String idTheso, String role, String idConcept2) {

        try (Connection conn = ds.getConnection()) {
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

    public boolean insertHierarchicalRelation(Connection conn,
            String idConcept1, String idTheso,
            String role, String idConcept2) {

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
            // To avoid dupplicate Key
            //   System.out.println(sqle.toString());
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
     *
     * @param conn
     * @param idConcept
     * @param idGroup
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean addRelationMT(Connection conn,
            String idConcept, String idThesaurus,
            String idGroup, int idUser) {

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
                    /*  if (!new RelationsHelper().addRelationHistorique(conn, idConcept, idThesaurus, idConcept, "MT", idUser, "ADD")) {
                        return false;
                    }*/
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
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet class Concept
     */
    public ArrayList<NodeBT> getListBT(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeBT> nodeListBT = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public ArrayList<String> getListIdOfBT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<String> listIdOfBt = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * @param ds
     * @param role
     * @param idThesaurus
     * @return #MR
     */
    public ArrayList<HierarchicalRelationship> getListLoopRelations(HikariDataSource ds,
            String role, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<HierarchicalRelationship> listRelations = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * recuperela la liste des ids et le role des BT dans une array liste de
     * string[2] qui contien en [0] l'id et en [1] le role
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public ArrayList<String[]> getListIdAndRoleOfBT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<String[]> list = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2, role from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'BT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        String[] tab = {resultSet.getString("id_concept2"), resultSet.getString("role")};
                        list.add(tab);
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
        return list;
    }

    public ArrayList<String[]> getListIdAndRoleOfRT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<String[]> list = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2,role from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and (role = '" + "RT" + "'"
                            + " or role = 'RHP' or role = 'RPO')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        String tab[] = {resultSet.getString("id_concept2"), resultSet.getString("role")};
                        list.add(tab);
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
            log.error("Error while getting list Ids of RT of Concept : " + idConcept, sqle);
        }
        return list;
    }

    /**
     * recuperela la liste des ids et le role des NT dans une array liste de
     * string[2] qui contien en [0] l'id et en [1] le role
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public ArrayList<String[]> getListIdAndRoleOfNT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<String[]> list = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2, role from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'NT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        String[] tab = {resultSet.getString("id_concept2"), resultSet.getString("role")};
                        list.add(tab);
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
        return list;
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
    public boolean addRelationRT(Connection conn,
            String idConcept1, String idThesaurus,
            String idConcept2, int idUser) {

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
     * @param ds
     * @param idConcept1
     * @param idConcept2
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean addRelationRT(HikariDataSource ds,
            String idConcept1, String idThesaurus,
            String idConcept2, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * @param ds
     * @param idConceptNT
     * @param idThesaurus
     * @param idConceptBT
     * @param idUser
     * @return boolean
     */
    public boolean addRelationBT(HikariDataSource ds, String idConceptNT, String idThesaurus,
            String idConceptBT, int idUser) {

        boolean status = false;
        try (Connection conn = ds.getConnection()) {
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
     * @param conn
     * @param idConcept
     * @param idConceptNT
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean addRelationNT(Connection conn,
            String idConcept, String idThesaurus,
            String idConceptNT, int idUser) {

        Statement stmt;
        boolean status = false;
        try {
            stmt = conn.createStatement();
            try {
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
            } finally {
                stmt.close();
            }
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
    
    public NodeRelation getLoopRelation(HikariDataSource ds,
             String idTheso, String idConcept){
        // récupération la relation en Loop
        NodeRelation nodeRelation = null;
        try (Connection conn = ds.getConnection()) {
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
        try (Connection conn = ds.getConnection()) {
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
     * Cette fonction permet de récupérer la liste de l'historique des relations
     * d'un concept d'un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param lang
     * @return Objet class Concept
     */
    public ArrayList<Relation> getRelationHistoriqueAll(HikariDataSource ds,
            String idConcept, String idThesaurus, String lang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<Relation> listRel = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lexical_value, id_concept2, role, action, hierarchical_relationship_historique.modified, username "
                            + "from hierarchical_relationship_historique, users, preferred_term, term"
                            + " where hierarchical_relationship_historique.id_thesaurus = '" + idThesaurus + "'"
                            + " and hierarchical_relationship_historique.id_concept1=preferred_term.id_concept"
                            + " and preferred_term.id_term=term.id_term"
                            + " and term.lang='" + lang + "'"
                            + " and term.id_thesaurus='" + idThesaurus + "'"
                            + " and ( id_concept1 = '" + idConcept + "'"
                            + " or id_concept2 = '" + idConcept + "' )"
                            + " and hierarchical_relationship_historique.id_user=users.id_user"
                            + " order by hierarchical_relationship_historique.modified DESC";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        listRel = new ArrayList<>();
                        while (resultSet.next()) {
                            Relation r = new Relation();
                            r.setId_relation(resultSet.getString("role"));
                            r.setId_concept1(resultSet.getString("lexical_value"));
                            r.setId_concept2(resultSet.getString("id_concept2"));
                            r.setModified(resultSet.getDate("modified"));
                            r.setIdUser(resultSet.getString("username"));
                            r.setAction(resultSet.getString("action"));
                            r.setId_thesaurus(idThesaurus);
                            listRel.add(r);
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
            log.error("Error while getting all relation historique of Concept : " + idConcept, sqle);
        }
        return listRel;
    }

    /**
     * Cette fonction permet de récupérer la liste de l'historique des relations
     * d'un concept à une date précise d'un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param date
     * @param lang
     * @return Objet class Concept
     */
    public ArrayList<Relation> getRelationHistoriqueFromDate(HikariDataSource ds,
            String idConcept, String idThesaurus, Date date, String lang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<Relation> listRel = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lexical_value, id_concept2, role, action, hierarchical_relationship_historique.modified, username "
                            + "from hierarchical_relationship_historique, users, preferred_term, term"
                            + " where hierarchical_relationship_historique.id_thesaurus = '" + idThesaurus + "'"
                            + " and hierarchical_relationship_historique.id_concept1=preferred_term.id_concept"
                            + " and preferred_term.id_term=term.id_term"
                            + " and term.lang='" + lang + "'"
                            + " and term.id_thesaurus='" + idThesaurus + "'"
                            + " and ( id_concept1 = '" + idConcept + "'"
                            + " or id_concept2 = '" + idConcept + "' )"
                            + " and hierarchical_relationship_historique.id_user=users.id_user"
                            + " and hierarchical_relationship_historique.modified <= '" + date.toString()
                            + "' order by hierarchical_relationship_historique.modified ASC";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        listRel = new ArrayList<>();

                        while (resultSet.next()) {
                            if (resultSet.getString("action").equals("DEL")) {
                                for (Relation rel : listRel) {
                                    if (rel.getId_concept1().equals(resultSet.getString("lexical_value")) && rel.getId_concept2().equals(resultSet.getString("id_concept2")) && rel.getAction().equals("ADD") && rel.getId_relation().equals(resultSet.getString("role"))) {
                                        listRel.remove(rel);
                                        break;
                                    }
                                }
                            } else {
                                Relation r = new Relation();
                                r.setId_relation(resultSet.getString("role"));
                                r.setId_concept1(resultSet.getString("lexical_value"));
                                r.setId_concept2(resultSet.getString("id_concept2"));
                                r.setModified(resultSet.getDate("modified"));
                                r.setIdUser(resultSet.getString("username"));
                                r.setAction(resultSet.getString("action"));
                                r.setId_thesaurus(idThesaurus);
                                listRel.add(r);
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
            log.error("Error while getting date relation historique of Concept : " + idConcept, sqle);
        }
        return listRel;
    }

    /**
     * Cette fonction permet de supprimer une relation terme gÃ©nÃ©rique Ã  un
     * concept
     *
     * @param ds
     * @param idConceptNT
     * @param idThesaurus
     * @param idConceptBT
     * @param idUser
     * @return boolean
     */
    public boolean deleteRelationBT(HikariDataSource ds, String idConceptNT, String idThesaurus,
            String idConceptBT, int idUser) {

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                if (!new RelationsHelper().addRelationHistorique(conn, idConceptNT, idThesaurus, idConceptBT, "BT", idUser, "DEL")) {
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
    public boolean deleteRelationBT(Connection conn,
            String idConceptNT, String idThesaurus,
            String idConceptBT, int idUser) {

        boolean status = false;

        if (!new RelationsHelper().addRelationHistorique(conn, idConceptNT, idThesaurus, idConceptBT, "BT", idUser, "DEL")) {
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
     * Cette fonction permet de supprimer une relation terme associé à  un
     * concept
     *
     * @param ds
     * @param idConcept1
     * @param idThesaurus
     * @param idConcept2
     * @param idUser
     * @return boolean
     */
    public boolean deleteRelationRT(HikariDataSource ds,
            String idConcept1, String idThesaurus,
            String idConcept2, int idUser) {

        Connection conn = null;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * @param ds
     * @param idConcept1
     * @param idThesaurus
     * @param idConcept2
     * @param idUser
     * @return boolean
     */
    public boolean deleteRelationNT(HikariDataSource ds,
            String idConcept1, String idThesaurus,
            String idConcept2, int idUser) {

        Connection conn = null;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * @param ds
     * @param idConcept1
     * @param idThesaurus
     * @param role
     * @param idConcept2
     * @return boolean
     */
    public boolean deleteThisRelation(HikariDataSource ds,
            String idConcept1, String idThesaurus,
            String role,
            String idConcept2) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * Cette fonction permet de supprimer la relation TT d'un concept
     *
     * @param conn
     * @param idConcept
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean deleteRelationTT(Connection conn,
            String idConcept, String idThesaurus,
            int idUser) {

        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {

                    if (!new RelationsHelper().addRelationHistorique(conn, idConcept, idThesaurus, idConcept, "TT", idUser, "DEL")) {
                        return false;
                    }

                    String query = "UPDATE Concept set"
                            + " top_concept = false,"
                            + " modified = current_date"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus = '" + idThesaurus + "'";

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

                    /*    if (!new RelationsHelper().addRelationHistorique(conn, idConcept, idThesaurus, idConcept, "TT", idUser, "DEL")) {
                        return false;
                    }*/
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
    public boolean setRelationMT(Connection conn,
            String idConcept, String idGroup, String idThesaurus) {

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

                    /*    if (!new RelationsHelper().addRelationHistorique(conn, idConcept, idThesaurus, idConcept, "TT", idUser, "DEL")) {
                        return false;
                    }*/
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
     * Cette fonction permet d'ajouter une relation TT à un concept
     *
     * @param conn
     * @param idConcept
     * @param idGroup
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean addRelationTT(Connection conn,
            String idConcept, String idGroup, String idThesaurus,
            int idUser) {

        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {

                    if (!new RelationsHelper().addRelationHistorique(conn, idConcept, idThesaurus, idConcept, "TT", idUser, "ADD")) {
                        return false;
                    }

                    String query = "UPDATE Concept set"
                            + " top_concept = true,"
                            + " modified = current_date"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus = '" + idThesaurus + "'";

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
            log.error("Error while adding relation TT of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de supprimer toutes les relations d'un concept
     *
     * @param conn
     * @param idConcept
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean deleteAllRelationOfConcept(Connection conn,
            String idConcept, String idThesaurus, int idUser) {

        Statement stmt;
        boolean status = false;

        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from hierarchical_relationship"
                            + " where id_concept1 ='" + idConcept + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeUpdate(query);
                    query = "delete from hierarchical_relationship"
                            + " where id_concept2 ='" + idConcept + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeUpdate(query);

                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //      conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting All relations of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de changer le status du concept en TopConcept ou
     * non Le Concept n'est pas supprimÃ©
     *
     * @param conn
     * @param idConcept
     * @param idThesaurus
     * @param idGroup
     * @param status
     * @param idUser
     * @return boolean
     */
    public boolean setRelationTopConcept(Connection conn,
            String idConcept, String idThesaurus, String idGroup, boolean status, int idUser) {

        Statement stmt;
        boolean resultat = false;

        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE Concept set"
                            + " top_concept = " + status + ","
                            + " modified = current_date"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus = '" + idThesaurus + "'"
                            + " AND id_group = '" + idGroup + "'";

                    stmt.executeUpdate(query);
                    resultat = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //       conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while setting relation TopConcept of Concept : " + idConcept, sqle);
        }
        return resultat;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id des termes génériques
     * d'un concept avec les identifiants pérennes (Ark, Handle) sert à l'export
     * des données
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept #MR
     */
    public ArrayList<NodeHieraRelation> getListBT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeHieraRelation> nodeListIdOfConcept = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public ArrayList<String> getListIdBT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<String> listIdBT = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2,role from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'BT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        listIdBT = new ArrayList<>();
                        while (resultSet.next()) {
                            listIdBT.add(resultSet.getString("id_concept2"));
                        }
                    }
                } catch (Exception ex) {
                    log.error("Error while getting Liste ID of BT Concept : " + idConcept, ex);
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
            log.error("Error while getting Liste ID of BT Concept : " + idConcept, sqle);
        }
        if (listIdBT != null) {
            if (listIdBT.contains(idConcept)) {
                /// relation en boucle à supprimer
                deleteThisRelation(ds, idConcept, idThesaurus, "BT", idConcept);
                deleteThisRelation(ds, idConcept, idThesaurus, "NT", idConcept);
                getListBT(ds, idConcept, idThesaurus);
            }
        }
        return listIdBT;
    }

    public ArrayList<String> getListIdWhichHaveNt(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        PreparedStatement stmt;
        ResultSet rs;
        ArrayList<String> ret = new ArrayList();
        try {
            Connection conn = ds.getConnection();

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
     * Cette fonction permet de récupérer les termes spécifiques d'un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet class Concept
     */
    /*public ArrayList <NodeNT> getListNT(HikariDataSource ds,
     String idConcept, String idThesaurus, String idLang) {

     Connection conn;
     Statement stmt;
     ResultSet resultSet;
     ArrayList<NodeNT> nodeListNT = null;

     try {
     // Get connection from pool
     conn = ds.getConnection();
     try {
     stmt = conn.createStatement();
     try {
     String query = "SELECT term.lexical_value, " +
     " preferred_term.id_concept, concept.status" +
     " FROM term,preferred_term,concept,hierarchical_relationship" +
     " WHERE preferred_term.id_term = term.id_term AND" +
     " preferred_term.id_thesaurus = term.id_thesaurus AND" +
     " concept.id_concept = preferred_term.id_concept AND" +
     " concept.id_thesaurus = preferred_term.id_thesaurus AND" +
     " hierarchical_relationship.id_concept2 = concept.id_concept" +
     " and concept.id_thesaurus = '" + idThesaurus + "'" +
     " and hierarchical_relationship.role = 'NT'" +
     " and hierarchical_relationship.id_concept1 = '" + idConcept + "'" +
     " and term.lang = '" + idLang + "'" +
     " ORDER BY upper((unaccent_string(term.lexical_value))) ASC;";
                    
     stmt.executeQuery(query);
     resultSet = stmt.getResultSet();
     if (resultSet != null) {
     nodeListNT = new ArrayList<>();
     while (resultSet.next()) {
     NodeNT nodeNT = new NodeNT();
     nodeNT.setIdConcept(resultSet.getString("id_concept"));
     nodeNT.setStatus(resultSet.getString("status"));
     if(resultSet.getString("lexical_value").trim().equals(""))
     nodeNT.setTitle("");
     else
     nodeNT.setTitle(resultSet.getString("lexical_value").trim());
     nodeListNT.add(nodeNT);
     }
     }

     } finally {
     stmt.close();
     }
     } finally {
     conn.close();
     }
     } catch (SQLException sqle) {
     // Log exception
     log.error("Error while getting ListConcept of Concept : " + idConcept, sqle);
     }
     //  Collections.sort(nodeConceptTree);
     return nodeListNT;
     }*/
    /**
     * permet de retourner le nombre de NT pour un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public int getCountOfNT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        int count = 0;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select count(id_concept2) from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'NT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
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
            log.error("Error while getting count of NT of Concept : " + idConcept, sqle);
        }
        return count;
    }

    /**
     * permet de retourner le nombre de BT pour un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public int getCountOfBT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        int count = 0;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select count(id_concept2) from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'BT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
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
            log.error("Error while getting count of BT of Concept : " + idConcept, sqle);
        }
        return count;
    }

    /**
     * permet de retourner le nombre de RT pour un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public int getCountOfRT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        int count = 0;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select count(id_concept2) from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'R%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
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
            log.error("Error while getting count of RT of Concept : " + idConcept, sqle);
        }
        return count;
    }

    /**
     * permet de retourner le nombre de UF pour un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public int getCountOfUF(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        int count = 0;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select count(non_preferred_term.id_term) "
                            + " from non_preferred_term, preferred_term "
                            + " where"
                            + " non_preferred_term.id_term = preferred_term.id_term"
                            + " AND"
                            + " non_preferred_term.id_thesaurus = preferred_term.id_thesaurus"
                            + " AND"
                            + " preferred_term.id_concept = '" + idConcept + "'"
                            + " and preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + " and non_preferred_term.lang = '" + idLang + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
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
            log.error("Error while getting count of UF of Concept : " + idConcept, sqle);
        }
        return count;
    }

    /**
     * cette fonction est pour trier les concept NT par date chronologique
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public ArrayList<NodeNT> getListNTOrderByDate(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeNT> nodeListNT = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select hierarchical_relationship.id_concept2,hierarchical_relationship.role, concept.modified "
                            + " FROM concept, hierarchical_relationship"
                            + " where concept.id_thesaurus = '" + idThesaurus + "'"
                            + " and hierarchical_relationship.id_thesaurus = concept.id_thesaurus "
                            + " and concept.id_concept = hierarchical_relationship.id_concept2 "
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'NT%'"
                            + " order by modified DESC";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            NodeNT nodeNT = new NodeNT();
                            nodeNT.setIdConcept(resultSet.getString("id_concept2"));
                            nodeNT.setRole(resultSet.getString("role"));
                            nodeListNT.add(nodeNT);
                        }
                    }
                    for (NodeNT nodeNT : nodeListNT) {
                        query = "SELECT term.lexical_value, term.status FROM term, preferred_term"
                                + " WHERE preferred_term.id_term = term.id_term"
                                + " and preferred_term.id_thesaurus = term.id_thesaurus"
                                + " and preferred_term.id_concept ='" + nodeNT.getIdConcept() + "'"
                                + " and term.lang = '" + idLang + "'"
                                + " and term.id_thesaurus = '" + idThesaurus + "'";

                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        if (resultSet != null) {
                            resultSet.next();
                            if (resultSet.getRow() == 0) {
                                nodeNT.setTitle("");
                                nodeNT.setStatus("");
                            } else {
                                if (resultSet.getString("lexical_value") == null || resultSet.getString("lexical_value").equals("")) {
                                    nodeNT.setTitle("");
                                } else {
                                    nodeNT.setTitle(resultSet.getString("lexical_value"));
                                }
                                if (resultSet.getString("status") == null || resultSet.getString("status").equals("")) {
                                    nodeNT.setStatus("");
                                } else {
                                    nodeNT.setStatus(resultSet.getString("status"));
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
            log.error("Error while getting NT of Concept : " + idConcept, sqle);
        }

        return nodeListNT;
    }

    /**
     * Cette fonction permet de récupérer la liste des Ids des termes
     * spécifiques d'un concept Utilisée pour l'export des Concepts
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Array String
     */
    public ArrayList<String> getListIdsOfNT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<String> nodeListIdsNT = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2 from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'NT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        nodeListIdsNT.add(resultSet.getString("id_concept2"));
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
            log.error("Error while getting List Ids of NT of Concept : " + idConcept, sqle);
        }
        return nodeListIdsNT;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id des termes spécifiques
     * d'un concept avec les identifiants pérennes (Ark, Handle) sert à l'export
     * des données
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept #MR
     */
    public ArrayList<NodeHieraRelation> getListNT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeHieraRelation> nodeListIdOfConcept = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * Cette fonction permet de savoir si le Concept a une relation NT si oui,
     * on ne le supprime pas pour Ã©viter de supprimer toute la chaine
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean isRelationNTExist(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2 from hierarchical_relationship"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept1 = '" + idConcept + "'"
                            + " and role LIKE 'NT%'";
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
            log.error("Error while asking if relation NT exist of Concept : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le Concept1 a une relation RT avec le
     * concept2 permet d'éviter l'ajout des relations NT et RT en même temps
     * (c'est interdit par la norme)
     *
     * @param ds
     * @param idConcept1
     * @param idConcept2
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean isConceptHaveRelationRT(HikariDataSource ds,
            String idConcept1, String idConcept2, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean isConceptHaveRelationBT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * Cette fonction permet de savoir si le Concept1 a une relation NT avec le
     * concept2 permet d'éviter l'ajout des relations NT et RT en même temps
     * (c'est interdit par la norme)
     *
     * @param ds
     * @param idConcept1
     * @param idConcept2
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean isConceptHaveRelationNTorBT(HikariDataSource ds,
            String idConcept1, String idConcept2, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * @param ds
     * @param idConcept1
     * @param idConcept2
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean isConceptHaveBrother(HikariDataSource ds,
            String idConcept1, String idConcept2, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept #MR
     */
    public ArrayList<NodeHieraRelation> getListRT(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeHieraRelation> nodeListIdOfConcept = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
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
