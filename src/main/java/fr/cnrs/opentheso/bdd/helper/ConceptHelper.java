/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.HierarchicalRelationship;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeConceptArkId;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeFusion;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeHieraRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeMetaData;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptExport;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptSearch;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearch;
import fr.cnrs.opentheso.bdd.helper.nodes.status.NodeStatus;
import fr.cnrs.opentheso.bean.condidat.dao.CandidatDao;
import fr.cnrs.opentheso.bean.condidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.importexport.outils.HTMLLinkElement;
import fr.cnrs.opentheso.bean.importexport.outils.HtmlLinkExtraction;
import fr.cnrs.opentheso.bean.toolbox.statistique.ConceptStatisticData;
import fr.cnrs.opentheso.ws.ark.ArkHelper;
import fr.cnrs.opentheso.ws.ark.ArkHelper2;
import fr.cnrs.opentheso.ws.handle.HandleHelper;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miled.rousset
 */
public class ConceptHelper {

    private final Log log = LogFactory.getLog(ConceptHelper.class);

    //identifierType  1=numericId ; 2=alphaNumericId
    private NodePreference nodePreference;
    private String message = "";

    public ConceptHelper() {
    }

    /**
     * ************************************************************
     * /**************************************************************
     * Nouvelles fonctions stables auteur Miled Rousset
     * /**************************************************************
     * /*************************************************************
     */
    /**
     * Cette fonction permet de récupérer la liste des concepts suivant l'id du
     * Concept-Père et le thésaurus sous forme de classe NodeConceptTree (sans
     * les relations) elle fait le tri alphabétique ou par notation
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @param isSortByNotation
     * @return Objet class NodeConceptTree
     */
    public ArrayList<NodeConceptTree> getListConcepts(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang,
            boolean isSortByNotation) {

        // check pour choix de tri entre alphabétique sur terme ou sur notation  
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeConceptTree> nodeConceptTree = null;
        String query;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if (isSortByNotation) {
                        /// Notation Sort 
                        query = "SELECT concept.notation, hierarchical_relationship.id_concept2"
                                + " FROM concept, hierarchical_relationship"
                                + " WHERE "
                                + " concept.id_concept = hierarchical_relationship.id_concept2 AND"
                                + " concept.id_thesaurus = hierarchical_relationship.id_thesaurus AND"
                                + " hierarchical_relationship.id_thesaurus = '" + idThesaurus + "' AND"
                                + " hierarchical_relationship.id_concept1 = '" + idConcept + "' AND"
                                + " hierarchical_relationship.role ILIKE 'NT%'"
                                + " and concept.status != 'CA'"
                                + " ORDER BY"
                                + " concept.notation ASC;";
                    } else {
                        // alphabétique Sort
                        query = "select id_concept2 from hierarchical_relationship, concept"
                                + " where concept.id_thesaurus = hierarchical_relationship.id_thesaurus"
                                + " and concept.id_concept = hierarchical_relationship.id_concept2"
                                + " and hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'"
                                + " and id_concept1 = '" + idConcept + "'"
                                + " and role LIKE 'NT%'"
                                + " and concept.status != 'CA'";
                    }

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeConceptTree = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeConceptTree nodeConceptTree1 = new NodeConceptTree();
                            nodeConceptTree1.setIdConcept(resultSet.getString("id_concept2"));
                            if (isSortByNotation) {
                                nodeConceptTree1.setNotation(resultSet.getString("notation"));
                            }

                            nodeConceptTree1.setIdThesaurus(idThesaurus);
                            nodeConceptTree1.setIdLang(idLang);
                            nodeConceptTree1.setIsTerm(true);
                            nodeConceptTree.add(nodeConceptTree1);
                        }
                    }
                    for (NodeConceptTree nodeConceptTree1 : nodeConceptTree) {
                        query = "SELECT term.lexical_value, concept.status"
                                + " FROM concept, preferred_term, term"
                                + " WHERE concept.id_concept = preferred_term.id_concept AND"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                                + " preferred_term.id_term = term.id_term AND"
                                + " preferred_term.id_thesaurus = term.id_thesaurus AND"
                                + " concept.id_concept = '" + nodeConceptTree1.getIdConcept() + "' AND"
                                + " term.lang = '" + idLang + "' AND"
                                + " term.id_thesaurus = '" + idThesaurus + "';";

                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        if (resultSet != null) {
                            resultSet.next();
                            if (resultSet.getRow() == 0) {
                                nodeConceptTree1.setTitle("");
                                nodeConceptTree1.setStatusConcept("");
                            } else {
                                nodeConceptTree1.setTitle(resultSet.getString("lexical_value"));
                                if (resultSet.getString("status") == null) {
                                    nodeConceptTree1.setStatusConcept("");
                                } else {
                                    nodeConceptTree1.setStatusConcept(resultSet.getString("status"));
                                }
                            }
                            nodeConceptTree1.setHaveChildren(
                                    haveChildren(ds, idThesaurus, nodeConceptTree1.getIdConcept())
                            );
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
        if (!isSortByNotation) {
            Collections.sort(nodeConceptTree);
        }
        return nodeConceptTree;
    }

    /**
     * Cettte fonction permet de retourner la liste des TopConcept avec IdArk et
     * handle
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public ArrayList<NodeUri> getAllTopConcepts(
            HikariDataSource ds,
            String idTheso) {

        ArrayList<NodeUri> NodeUris = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept, id_ark, id_handle from concept"
                            + " where id_thesaurus = '" + idTheso + "'"
                            + " and top_concept = true and status !='CA'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
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
                        nodeUri.setIdConcept(resultSet.getString("id_concept"));
                        NodeUris.add(nodeUri);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Liste of TT of theso : " + idTheso, sqle);
        }
        return NodeUris;
    }

    /**
     * Cette fonction permet de déplacer une Branche
     *
     * @param ds
     * @param idConcept
     * @param idOldBTsToDelete
     * @param idNewConceptBT
     * @param idThesaurus
     * @param idUser
     * @return true or false #MR
     */
    public boolean moveBranchFromConceptToConcept(HikariDataSource ds,
            String idConcept,
            ArrayList<String> idOldBTsToDelete,
            String idNewConceptBT,
            String idThesaurus, int idUser) {
        try {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            for (String idOldBT : idOldBTsToDelete) {
                if (!new RelationsHelper().deleteRelationBT(conn, idConcept, idThesaurus, idOldBT, idUser)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
            }

            if (!new RelationsHelper().addRelationBT(conn, idConcept, idThesaurus, idNewConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Cette fonction permet de déplacer un concept/Branche de la racine vers un
     * concept dans le thésaurus
     *
     * @param ds
     * @param idConcept
     * @param idNewConceptBT
     * @param idThesaurus
     * @param idUser
     * @return true or false #MR
     */
    public boolean moveBranchFromRootToConcept(HikariDataSource ds,
            String idConcept,
            String idNewConceptBT,
            String idThesaurus, int idUser) {
        try {
            RelationsHelper relationsHelper = new RelationsHelper();
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            if (!relationsHelper.addRelationBT(conn, idConcept, idThesaurus, idNewConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            return setNotTopConcept(ds, idConcept, idThesaurus);
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Cette fonction permet de déplacer une Branche vers la racine, elle
     * devient topterme
     *
     * @param ds
     * @param idConcept
     * @param idOldConceptBT
     * @param idThesaurus
     * @param idUser
     * @return true or false #MR
     */
    public boolean moveBranchFromConceptToRoot(HikariDataSource ds,
            String idConcept,
            String idOldConceptBT,
            String idThesaurus, int idUser) {
        try {
            RelationsHelper relationsHelper = new RelationsHelper();
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            if (!relationsHelper.deleteRelationBT(conn, idConcept, idThesaurus, idOldConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            return setTopConcept(ds, idConcept, idThesaurus);

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Cette fonction permet de mettre à jour la notation pour un concept
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param notation
     * @return
     */
    public boolean updateNotation(HikariDataSource ds, String idConcept,
            String idTheso, String notation) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set notation ='" + notation + "'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";

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
            log.error("Error while updating or adding ArkId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de récupérer la liste des Ids of Topconcepts d'un
     * thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return #MR
     */
    public ArrayList<String> getAllTopTermOfThesaurus(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> listIdOfTopConcept = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and top_concept = true and status != 'CA'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        listIdOfTopConcept.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All Ids of TopConcept : " + idThesaurus, sqle);
        }
        return listIdOfTopConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Topconcepts suivant l'id
     * du thésaurus sous forme de classe NodeConceptTree (sans les relations) La
     * liste est triée
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param isSortByNotation
     * @return Objet class NodeConceptTree
     */
    public ArrayList<NodeConceptTree> getListOfTopConcepts(HikariDataSource ds,
            String idThesaurus, String idLang, boolean isSortByNotation) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeConceptTree> nodeConceptTree = null;
        String query;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if (isSortByNotation) {
                        query = "SELECT concept.notation, concept.status, concept.id_concept"
                                + " FROM concept WHERE"
                                + " concept.id_thesaurus = '" + idThesaurus + "' AND"
                                + " concept.top_concept = true and status != 'CA'"
                                + " ORDER BY concept.notation ASC";
                    } else {
                        query = "SELECT concept.status, concept.id_concept"
                                + " FROM concept WHERE"
                                + " concept.id_thesaurus = '" + idThesaurus + "' AND"
                                + " concept.top_concept = true";
                    }

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    nodeConceptTree = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeConceptTree nodeConceptTree1 = new NodeConceptTree();
                        nodeConceptTree1.setIdConcept(resultSet.getString("id_concept"));
                        if (isSortByNotation) {
                            nodeConceptTree1.setNotation(resultSet.getString("notation"));
                        }
                        nodeConceptTree1.setStatusConcept(resultSet.getString("status"));
                        nodeConceptTree1.setIdThesaurus(idThesaurus);
                        nodeConceptTree1.setIdLang(idLang);
                        nodeConceptTree1.setIsTopTerm(true);
                        nodeConceptTree.add(nodeConceptTree1);
                    }
                    for (NodeConceptTree nodeConceptTree1 : nodeConceptTree) {
                        query = "SELECT term.lexical_value FROM"
                                + " preferred_term, term WHERE"
                                + " preferred_term.id_term = term.id_term AND"
                                + " preferred_term.id_thesaurus = term.id_thesaurus AND"
                                + " term.lang = '" + idLang + "' AND"
                                + " preferred_term.id_concept = '" + nodeConceptTree1.getIdConcept() + "' AND"
                                + " term.id_thesaurus = '" + idThesaurus + "'";

                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        resultSet.next();
                        if (resultSet.getRow() == 0) {
                            nodeConceptTree1.setTitle("(" + nodeConceptTree1.getIdConcept() + ")");
                        } else {
                            nodeConceptTree1.setTitle(resultSet.getString("lexical_value"));
                        }
                        nodeConceptTree1.setHaveChildren(
                                haveChildren(ds, idThesaurus, nodeConceptTree1.getIdConcept())
                        );
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting TopConcept sorted of theso  : " + idThesaurus, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(nodeConceptTree);
        }
        return nodeConceptTree;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept par son id et son thésaurus et la langue le résultat est allégé
     * pour l'adapter à la recherche
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet class NodeConceptSerach #MR
     */
    public NodeConceptSearch getConceptForSearch(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {
        NodeConceptSearch nodeConceptSerach = new NodeConceptSearch();

        TermHelper termHelper = new TermHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        GroupHelper groupHelper = new GroupHelper();

        nodeConceptSerach.setIdConcept(idConcept);

        //récupération du PrefLabel
        nodeConceptSerach.setPrefLabel(getLexicalValueOfConcept(ds, idConcept, idThesaurus, idLang));

        //récupération des traductions
        nodeConceptSerach.setNodeTermTraductions(termHelper.getTraductionsOfConcept(ds, idConcept, idThesaurus, idLang));

        //récupération des termes génériques
        nodeConceptSerach.setNodeBT(relationsHelper.getListBT(ds, idConcept, idThesaurus, idLang));

        //récupération des termes spécifiques
        nodeConceptSerach.setNodeNT(relationsHelper.getListNT(ds, idConcept, idThesaurus, idLang));

        //récupération des termes associés
        nodeConceptSerach.setNodeRT(relationsHelper.getListRT(ds, idConcept, idThesaurus, idLang));

        String idTerm = termHelper.getIdTermOfConcept(ds, idConcept, idThesaurus);

        if (idTerm != null) {
            //récupération des Non Prefered Term
            nodeConceptSerach.setNodeEM(termHelper.getNonPreferredTerms(ds, idTerm, idThesaurus, idLang));
        }
        nodeConceptSerach.setNodeConceptGroup(groupHelper.getListGroupOfConcept(ds, idThesaurus, idConcept, idLang));

        return nodeConceptSerach;
    }

    /**
     * permet de retourner la liste des Top concepts pour un group donné retour au
     * format de NodeIdValue (informations pour construire l'arbre
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param idGroup
     * @param isSortByNotation
     * @return
     * #MR
     */
    public ArrayList<NodeIdValue> getListTopConceptsOfGroup(HikariDataSource ds,
            String idThesaurus, String idLang, String idGroup, boolean isSortByNotation) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        //ArrayList<String> tabIdConcept = new ArrayList<>();

        ArrayList<NodeIdValue> tabIdValues = new ArrayList<>();

        String lexicalValue;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                String query;
                try {
                    if (isSortByNotation) {
                        ArrayList<NodeIdValue> tabIdConcepts = new ArrayList<>();
                        query = "SELECT DISTINCT concept.id_concept, concept.notation"
                                + " FROM concept, concept_group_concept"
                                + " WHERE"
                                + " concept.id_concept = concept_group_concept.idconcept AND"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                                + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                                + " concept.status != 'CA' and"
                                + " concept.top_concept = true and"
                                + " concept_group_concept.idgroup = '" + idGroup + "' limit 2001;";
                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();

                        while (resultSet.next()) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(resultSet.getString("id_concept"));
                            nodeIdValue.setNotation(resultSet.getString("notation"));
                            tabIdConcepts.add(nodeIdValue);
                        }
                        for (NodeIdValue nodeIdValue1 : tabIdConcepts) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            lexicalValue = getLexicalValueOfConcept(ds, nodeIdValue1.getId(), idThesaurus, idLang);
                            if (lexicalValue == null || lexicalValue.isEmpty()) {
                                nodeIdValue.setValue("__" + nodeIdValue1.getId());
                            } else {
                                nodeIdValue.setValue(lexicalValue);
                            }
                            nodeIdValue.setId(nodeIdValue1.getId());
                            nodeIdValue.setNotation(nodeIdValue1.getNotation());
                            tabIdValues.add(nodeIdValue);
                        }
                    } else {
                        ArrayList<String> tabIdConcepts = new ArrayList<>();
                        query = "SELECT DISTINCT concept.id_concept, concept.notation"
                                + " FROM concept, concept_group_concept"
                                + " WHERE"
                                + " concept.id_concept = concept_group_concept.idconcept AND"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                                + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                                + " concept.status != 'CA' and"
                                + " concept.top_concept = true and"
                                + " concept_group_concept.idgroup = '" + idGroup + "' limit 2001;";
                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();

                        while (resultSet.next()) {
                            tabIdConcepts.add(resultSet.getString("id_concept"));
                        }
                        for (String idConcept : tabIdConcepts) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            lexicalValue = getLexicalValueOfConcept(ds, idConcept, idThesaurus, idLang);
                            if (lexicalValue == null || lexicalValue.isEmpty()) {
                                nodeIdValue.setValue("__" + idConcept);
                            } else {
                                nodeIdValue.setValue(lexicalValue);
                            }
                            nodeIdValue.setId(idConcept);
                            tabIdValues.add(nodeIdValue);
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
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(tabIdValues);
        }

        return tabIdValues;
    }    
    
    /**
     * permet de retourner la liste des concepts pour un group donné retour au
     * format de NodeConceptTree (informations pour construire l'arbre
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param idGroup
     * @param isSortByNotation
     * @return
     */
    public ArrayList<NodeIdValue> getListConceptsOfGroup(HikariDataSource ds,
            String idThesaurus, String idLang, String idGroup, boolean isSortByNotation) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        //ArrayList<String> tabIdConcept = new ArrayList<>();

        ArrayList<NodeIdValue> tabIdValues = new ArrayList<>();

        String lexicalValue;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                String query;
                try {
                    if (isSortByNotation) {
                        ArrayList<NodeIdValue> tabIdConcepts = new ArrayList<>();
                        query = "SELECT DISTINCT concept.id_concept, concept.notation"
                                + " FROM concept, concept_group_concept"
                                + " WHERE"
                                + " concept.id_concept = concept_group_concept.idconcept AND"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                                + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                                + " concept.status != 'CA' and"
                                + " concept_group_concept.idgroup = '" + idGroup + "' limit 2001;";
                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();

                        while (resultSet.next()) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(resultSet.getString("id_concept"));
                            nodeIdValue.setNotation(resultSet.getString("notation"));
                            tabIdConcepts.add(nodeIdValue);
                        }
                        for (NodeIdValue nodeIdValue1 : tabIdConcepts) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            lexicalValue = getLexicalValueOfConcept(ds, nodeIdValue1.getId(), idThesaurus, idLang);
                            if (lexicalValue == null || lexicalValue.isEmpty()) {
                                nodeIdValue.setValue("__" + nodeIdValue1.getId());
                            } else {
                                nodeIdValue.setValue(lexicalValue);
                            }
                            nodeIdValue.setId(nodeIdValue1.getId());
                            nodeIdValue.setNotation(nodeIdValue1.getNotation());
                            tabIdValues.add(nodeIdValue);
                        }
                    } else {
                        ArrayList<String> tabIdConcepts = new ArrayList<>();
                        query = "SELECT DISTINCT concept.id_concept, concept.notation"
                                + " FROM concept, concept_group_concept"
                                + " WHERE"
                                + " concept.id_concept = concept_group_concept.idconcept AND"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                                + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                                + " concept.status != 'CA' and"
                                + " concept_group_concept.idgroup = '" + idGroup + "' limit 2001;";
                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();

                        while (resultSet.next()) {
                            tabIdConcepts.add(resultSet.getString("id_concept"));
                        }
                        for (String idConcept : tabIdConcepts) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            lexicalValue = getLexicalValueOfConcept(ds, idConcept, idThesaurus, idLang);
                            if (lexicalValue == null || lexicalValue.isEmpty()) {
                                nodeIdValue.setValue("__" + idConcept);
                            } else {
                                nodeIdValue.setValue(lexicalValue);
                            }
                            nodeIdValue.setId(idConcept);
                            tabIdValues.add(nodeIdValue);
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
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(tabIdValues);
        }

        return tabIdValues;
    }

    /**
     * permet de retourner la liste des concepts pour un group donné
     *
     * @param ds
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public ArrayList<NodeUri> getListConceptsOfGroup(HikariDataSource ds,
            String idThesaurus, String idGroup) {

        String query = "SELECT DISTINCT concept.id_concept,"
                + " concept.id_ark, concept.id_handle"
                + " FROM concept, concept_group_concept"
                + " WHERE"
                + " concept.id_concept = concept_group_concept.idconcept AND"
                + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                + " concept_group_concept.idgroup = '" + idGroup +"'";

        return getConceptDetails(ds, query, idThesaurus);
    }

    /**
     * permet de retourner la liste des concepts sans group
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public ArrayList<NodeUri> getListConceptsWithoutGroup(HikariDataSource ds, String idThesaurus) {

        String query = "SELECT DISTINCT concept.id_concept, concept.id_ark, concept.id_handle FROM concept " +
                "WHERE id_thesaurus = '"+idThesaurus+"' " +
                "AND id_concept NOT IN (SELECT idconcept FROM concept_group_concept WHERE id_thesaurus = '"+idThesaurus+"')";

        return getConceptDetails(ds, query, idThesaurus);
    }

    private ArrayList<NodeUri> getConceptDetails(HikariDataSource ds, String query, String idThesaurus) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        ArrayList<NodeUri> nodeUris = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        NodeUri nodeUri = new NodeUri();
                        nodeUri.setIdConcept(resultSet.getString("id_concept"));
                        nodeUri.setIdArk(resultSet.getString("id_ark"));
                        nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        nodeUris.add(nodeUri);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return nodeUris;
    }

    /**
     * permet de retourner la liste des concepts pour un group donné retour au
     * format de NodeConceptTree (informations pour construire l'arbre
     *
     * @param ds
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public int getCountOfConceptsOfGroup(HikariDataSource ds,
            String idThesaurus, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        int count = 0;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT count(concept.id_concept)"
                            + " FROM concept, concept_group_concept"
                            + " WHERE"
                            + " concept.id_concept = concept_group_concept.idconcept AND"
                            + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                            + " concept.status != 'CA' AND "
                            + " concept_group_concept.idgroup = '" + idGroup + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if(resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return count;
    }

    /**
     * permet de retourner le nombre des concepts dans un thesaurus rattaché à aucun groupe
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public int getCountOfConceptsSansGroup(HikariDataSource ds, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        int count = 0;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT count(id_concept) FROM concept " +
                            " WHERE id_thesaurus = '"+idThesaurus+"' " +
                            " AND concept.status != 'CA'" +
                            " AND id_concept NOT IN (SELECT idconcept FROM concept_group_concept WHERE idthesaurus = '"+idThesaurus+"')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return count;
    }

    /**
     * permet de mettre à jour la date du concept quand il y a une modification
     *
     * @param ds
     * @param idTheso
     * @param idConcept
     */
    public void updateDateOfConcept(HikariDataSource ds,
            String idTheso, String idConcept) {
        Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set modified = current_date"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";

                    stmt.executeUpdate(query);
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating date of concept : " + idConcept, sqle);
        }
    }

    /**
     * Permet de retourner la date de la dernière modification sur un thésaurus
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public Date getLastModifcation(HikariDataSource ds,
            String idTheso) {
        Connection conn;

        Date date = null;
        String query;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                query = "select modified from concept where id_thesaurus = '" + idTheso + "' order by modified DESC limit 1 ";

                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
                if (resultSet.next()) {
                    date = resultSet.getDate("modified");
                }

            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return date;
    }

    /**
     * Permet de retourner la liste des concepts qui ont plusieurs groupes en
     * même temps
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public ArrayList<String> getConceptsHavingMultiGroup(HikariDataSource ds,
            String idTheso) {

        Connection conn;
        ArrayList<String> listIdConcept = new ArrayList<>();
        String query;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                query = "select idconcept from concept_group_concept where "
                        + " idthesaurus = '" + idTheso + "' "
                        + " group by idconcept having count(idconcept) > 1";
                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
                while (resultSet.next()) {
                    listIdConcept.add(resultSet.getString("idconcept"));
                }

            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return listIdConcept;
    }

    /**
     * Permet de retourner la liste des concepts qui ont uniquement un seul BT
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public ArrayList<String> getConceptsHavingOneBT(HikariDataSource ds,
            String idTheso) {

        Connection conn;
        ArrayList<String> listIdConcept = new ArrayList<>();
        String query;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                query = "select id_concept1 from hierarchical_relationship where"
                        + " id_thesaurus = '" + idTheso + "' and role ilike 'BT%'"
                        + " group by id_concept1 having count(id_concept1) = 1";
                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
                while (resultSet.next()) {
                    listIdConcept.add(resultSet.getString("id_concept1"));
                }

            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return listIdConcept;
    }

    /**
     * Permet de retourner la liste des concepts qui ont uniquement un seul BT
     * mais en filtrant par group
     *
     * @param ds
     * @param idTheso
     * @param idGroup
     * @return
     */
    public ArrayList<String> getConceptsHavingOneBTByGroup(HikariDataSource ds,
            String idTheso, String idGroup) {

        Connection conn;
        ArrayList<String> listIdConcept = new ArrayList<>();
        String query;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                query = "select id_concept1, count(id_concept1) from hierarchical_relationship, concept_group_concept where"
                        + " concept_group_concept.idthesaurus = hierarchical_relationship.id_thesaurus AND"
                        + " concept_group_concept.idconcept = hierarchical_relationship.id_concept1 AND"
                        + " id_thesaurus = '" + idTheso + "' and role ilike 'BT%' AND"
                        + " concept_group_concept.idgroup = '" + idGroup + "'"
                        + " group by id_concept1 having count(id_concept1) = 1";
                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
                while (resultSet.next()) {
                    listIdConcept.add(resultSet.getString("id_concept1"));
                }

            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return listIdConcept;
    }

    /**
     * Permet de retourner la liste des concepts qui ont plusieurs BT en même
     * temps
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public ArrayList<String> getConceptsHavingMultiBT(HikariDataSource ds,
            String idTheso) {

        Connection conn;
        ArrayList<String> listIdConcept = new ArrayList<>();
        String query;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                query = "select id_concept1 from hierarchical_relationship where"
                        + " id_thesaurus = '" + idTheso + "' and role ilike 'BT%'"
                        + " group by id_concept1 having count(id_concept1) > 1";
                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
                while (resultSet.next()) {
                    listIdConcept.add(resultSet.getString("id_concept1"));
                }

            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return listIdConcept;
    }

    /**
     * Permet de retourner la liste des concepts qui ont plusieurs BT en même
     * temps mais en filtrant par group
     *
     * @param ds
     * @param idTheso
     * @param idGroup
     * @return
     */
    public ArrayList<String> getConceptsHavingMultiBTByGroup(HikariDataSource ds,
            String idTheso, String idGroup) {

        Connection conn;
        ArrayList<String> listIdConcept = new ArrayList<>();
        String query;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                query = "select id_concept1, count(id_concept1) from hierarchical_relationship, concept_group_concept where"
                        + " concept_group_concept.idthesaurus = hierarchical_relationship.id_thesaurus AND"
                        + " concept_group_concept.idconcept = hierarchical_relationship.id_concept1 AND"
                        + " id_thesaurus = '" + idTheso + "' and role ilike 'BT%' AND"
                        + " concept_group_concept.idgroup = '" + idGroup + "'"
                        + " group by id_concept1 having count(id_concept1) > 1";
                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
                while (resultSet.next()) {
                    listIdConcept.add(resultSet.getString("id_concept1"));
                }

            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return listIdConcept;
    }

    /**
     * permet de retourner la liste des idConcept d'un thésaurus qui n'ont pas
     * d'identifiant numérique
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public ArrayList<String> getAllNonNumericId(HikariDataSource ds,
            String idTheso) {
        Connection conn;
        ArrayList<String> listIdConcept = new ArrayList<>();
        String query;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                query = "select id_concept from concept where id_concept like '%crt%'"
                        + " and id_thesaurus = '" + idTheso + "'";
                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
                while (resultSet.next()) {
                    listIdConcept.add(resultSet.getString("id_concept"));
                }

            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return listIdConcept;
    }

    public HashMap<String, String> getIdsAndValuesOfConcepts(
            HikariDataSource ds,
            ArrayList<String> idsToGet,
            String idLang,
            String idTheso) {
        HashMap<String, String> idsAndValues = new LinkedHashMap<>();
        String label;
        for (String idConcept : idsToGet) {
            label = getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
            if (label != null) {
                if (!label.isEmpty()) {
                    idsAndValues.put(idConcept, label);
                }
            }
        }
        return idsAndValues;
    }

    public ArrayList<NodeIdValue> getIdsAndValuesOfConcepts2(
            HikariDataSource ds,
            ArrayList<String> idsToGet,
            String idLang,
            String idTheso) {
        ArrayList<NodeIdValue> idsAndValues = new ArrayList<>();
        String label;
        for (String idConcept : idsToGet) {
            label = getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
            if (label != null) {
                if (!label.isEmpty()) {
                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId(idConcept);
                    nodeIdValue.setValue(label);
                    idsAndValues.add(nodeIdValue);
                }
            }
        }
        return idsAndValues;
    }

    /**
     * Cette fonction permet de retrouver tous tes identifiants d'une branche en
     * partant du concept en paramètre
     *
     * @param hd
     * @param idConceptDeTete
     * @param idTheso
     * @return
     */
    public ArrayList<String> getIdsOfBranch(HikariDataSource hd,
            String idConceptDeTete,
            String idTheso) {
        ArrayList<String> lisIds = new ArrayList<>();
        lisIds = getIdsOfBranch__(hd,
                idConceptDeTete,
                idTheso,
                lisIds);
        return lisIds;
    }

    private ArrayList<String> getIdsOfBranch__(HikariDataSource hd,
            String idConceptDeTete,
            String idTheso,
            ArrayList<String> lisIds) {

        lisIds.add(idConceptDeTete);

        ArrayList<String> listIdsOfConceptChildren
                = getListChildrenOfConcept(hd, idConceptDeTete, idTheso);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranch__(hd, listIdsOfConceptChildren1,
                    idTheso, lisIds);
        }
        return lisIds;
    }

    /**
     * permet de modifier l'identifiant du concept en numérique, la fonction
     * modifie toutes les tables dépendantes et les relations
     *
     * @param ds
     * @param idTheso
     * @param id
     * @return
     */
    public boolean setIdConceptToNumeric(HikariDataSource ds, String idTheso, String id) {

        if (id == null) {
            return false;
        }
        if (idTheso == null) {
            return false;
        }

        // on récupère un nouvel identifiant numérique
        String newId = getNumericConceptId(ds);
        if (newId == null) {
            return false;
        }
        GpsHelper gpsHelper = new GpsHelper();
        NoteHelper noteHelper = new NoteHelper();
        ImagesHelper imagesHelper = new ImagesHelper();
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        try {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            try {
                //table concept
                setIdConcept(conn, idTheso, id, newId);
                //table concept_group_concept
                setIdConceptGroupConcept(conn, idTheso, id, newId);
                //table concept_historique
                setIdConceptHistorique(conn, idTheso, id, newId);
                //table concept_orphan
                setIdConceptOrphan(conn, idTheso, id, newId);
                //table gps 
                gpsHelper.setIdConceptGPS(conn, idTheso, id, newId);
                //table hierarchical_relationship
                setIdConceptHieraRelation(conn, idTheso, id, newId);
                //table hierarchical_relationship_historique
                setIdConceptHieraRelationHisto(conn, idTheso, id, newId);
                //table note
                noteHelper.setIdConceptNote(conn, idTheso, id, newId);
                //table note_historique
                noteHelper.setIdConceptNoteHisto(conn, idTheso, id, newId);
                //table images 
                imagesHelper.setIdConceptImage(conn, idTheso, id, newId);
                //table ExternalImages 
                imagesHelper.setIdConceptExternalImages(conn, idTheso, id, newId);
                //table concept_fusion
                setIdConceptFusion(conn, idTheso, id, newId);
                //table preferred_term 
                setIdConceptPreferedTerm(conn, idTheso, id, newId);
                //table alignement
                alignmentHelper.setIdConceptAlignement(conn, idTheso, id, newId);
                conn.commit();
                conn.close();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                conn.close();
                return false;
            }

        } catch (SQLException sqle) {

        }
        return false;
    }

    /**
     * Permet de retourner un Id numérique et unique pour le Concept
     *
     * @param ds
     * @return
     */
    private String getNumericConceptId(HikariDataSource ds) {
        Connection conn;
        String idConcept = null;
        String query;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                query = "select nextval('concept__id_seq') from concept__id_seq";
                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
                if (resultSet.next()) {
                    int idNumerique = resultSet.getInt(1);
                    idNumerique++;
                    idConcept = "" + (idNumerique);
                    // si le nouveau Id existe, on l'incrémente
                    while (isIdExiste(conn, idConcept)) {
                        idConcept = "" + (++idNumerique);
                    }
                }

            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idConcept;
    }

    /**
     * Permet de retourner un Id numérique et unique pour le Concept
     *
     * @return
     */
    private String getNumericConceptId(Connection conn) {
        String idConcept = null;
        String query;
        Statement stmt;
        ResultSet resultSet;

        try {
            try {
                stmt = conn.createStatement();
                query = "select nextval('concept__id_seq') from concept__id_seq";
                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
                if (resultSet.next()) {
                    int idNumerique = resultSet.getInt(1);
                    idConcept = "" + (idNumerique);
                    // si le nouveau Id existe, on l'incrémente
                    while (isIdExiste(conn, idConcept)) {
                        idConcept = "" + (++idNumerique);
                    }
                }

            } finally {
                //    conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idConcept;
    }

    private String getAlphaNumericId(Connection conn) {
        ToolsHelper toolsHelper = new ToolsHelper();
        String id = toolsHelper.getNewId(15);
        while (isIdExiste(conn, id)) {
            id = toolsHelper.getNewId(15);
        }
        return id;
    }

    private String getAlphaNumericId(HikariDataSource ds) {
        ToolsHelper toolsHelper = new ToolsHelper();
        String id = toolsHelper.getNewId(15);
        while (isIdExiste(ds, id)) {
            id = toolsHelper.getNewId(15);
        }
        return id;
    }

    /**
     * focntion qui permet de récupérer le Delta des Id concepts créés ou
     * modifiéés le format de la date est (yyyy-MM-dd)
     *
     * @param ds
     * @param idTheso
     * @param date
     * @return
     */
    public ArrayList<String> getConceptsDelta(HikariDataSource ds,
            String idTheso, String date) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        ArrayList<String> ids = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where "
                            + " id_thesaurus = '" + idTheso + "'"
                            + " and (created > '" + date + "'"
                            + " or modified > '" + date + "')";

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        ids.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting delta of Concept  : " + idTheso, sqle);
        }
        return ids;
    }

    /**
     * Cette fonction regenère tous les idArk des concepts fournis en paramètre
     *
     * @param ds
     * @param idTheso
     * @param idConcepts
     * @return
     */
    public boolean generateArkId(
            HikariDataSource ds,
            String idTheso,
            ArrayList<String> idConcepts) {

        ArkHelper2 arkHelper2 = new ArkHelper2(nodePreference);
        if (!arkHelper2.login()) {
            message = "Erreur de connexion !!";
            return false;
        }

        NodeMetaData nodeMetaData;
        Concept concept;
        String privateUri;

        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArk()) {
            return false;
        }

        for (String idConcept : idConcepts) {

            //    System.out.println("génération ARK pour le concept : " + idConcept);
            nodeMetaData = getNodeMetaData(ds, idConcept,
                    nodePreference.getSourceLang(), idTheso);
            if (nodeMetaData == null) {
                return false;
            }
            concept = getThisConcept(ds, idConcept, idTheso);
            if (concept == null) {
                return false;
            }

            privateUri = "?idc=" + idConcept + "&idt=" + idTheso;

            /// cas où on n'a pas d'idArk dans le concept, il faut alors le créer sur Arkeo
            if (concept.getIdArk() == null || concept.getIdArk().isEmpty()) {
                // création d'un identifiant Ark + (Handle avec le serveur Ark de la MOM)
                if (!arkHelper2.addArk(privateUri, nodeMetaData)) {
                    message = arkHelper2.getMessage();
                    return false;
                }
                if (!updateArkIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdArk())) {
                    return false;
                }
                if (nodePreference.isGenerateHandle()) {
                    if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdHandle())) {
                        return false;
                    }
                }
            } else {
                // ark existe dans Opentheso, on vérifie si Ark est présent sur le serveur Ark 
                if (arkHelper2.isArkExistOnServer(concept.getIdArk())) {
                    // ark existe sur le serveur, alors on applique une mise à jour
                    // pour l'URL et les métadonnées
                    if (!arkHelper2.updateArk(concept.getIdArk(), privateUri, nodeMetaData)) {
                        message = arkHelper2.getMessage();
                        return false;
                    }
                    if (nodePreference.isGenerateHandle()) {
                        if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdHandle())) {
                            return false;
                        }
                    }
                } else {
                    // création d'un identifiant Ark avec en paramètre l'ID Ark existant sur Opentheso
                    // + (création de l'ID Handle avec le serveur Ark de la MOM)
                    if (!arkHelper2.addArkWithProvidedId(concept.getIdArk(), privateUri, nodeMetaData)) {
                        message = arkHelper2.getMessage();
                        return false;
                    }
                    if (!updateArkIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdArk())) {
                        return false;
                    }
                    if (nodePreference.isGenerateHandle()) {
                        if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdHandle())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Permet de : - Vérifier si l'identifiant Ark existe sur le serveur Arkéo -
     * S'il existe, on le met à jour pour l'URL - s'il n'existe pas, on le créé
     *
     * à utiliser avec précaution pour maintenance
     *
     * #MR
     *
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param idArk
     * @return
     */
    public boolean updateArkId(
            HikariDataSource ds,
            String idTheso,
            String idConcept,
            String idArk) {

        ArkHelper arkHelper = new ArkHelper(nodePreference);
        if (!arkHelper.login()) {
            return false;
        }

        NodeMetaData nodeMetaData;
        Concept concept;
        String privateUri;

        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArk()) {
            return false;
        }

        if (idArk == null || idArk.isEmpty()) {
            return false;
        }

        nodeMetaData = getNodeMetaData(ds, idConcept,
                nodePreference.getSourceLang(), idTheso);
        if (nodeMetaData == null) {
            return false;
        }
        concept = getThisConcept(ds, idConcept, idTheso);
        if (concept == null) {
            return false;
        }

        privateUri = "?idc=" + idConcept + "&idt=" + idTheso;

        if (arkHelper.isArkExistOnServer(idArk)) {
            // ark existe sur le serveur, alors on applique une mise à jour
            // pour l'URL et les métadonnées
            if (!arkHelper.updateArk(idArk, privateUri, nodeMetaData)) {
                message = arkHelper.getMessage();
                return false;
            }
            if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper.getIdHandle())) {
                return false;
            }
        } else {
            // création d'un identifiant Ark avec en paramètre l'ID Ark existant sur Opentheso
            // + (création de l'ID Handle avec le serveur Ark de la MOM)

            // on vérifie d'abord si idHandle existe sur handle.net, alors il faut le supprimer avant
            if (arkHelper.isHandleExistOnServer(idArk.replaceAll("/", "."))) {
                if (!arkHelper.deleteHandle(idArk.replaceAll("/", "."), privateUri, nodeMetaData)) {
                    message = arkHelper.getMessage();
                    return false;
                }
            }

            if (!arkHelper.addArkWithProvidedId(idArk, privateUri, nodeMetaData)) {
                message = arkHelper.getMessage();
                return false;
            }
            if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper.getIdHandle())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Pour préparer les données pour la création d'un idArk
     *
     * @param ds
     * @param idConcept
     * @param idLang
     * @param idTheso
     * @return
     */
    private NodeMetaData getNodeMetaData(HikariDataSource ds,
            String idConcept, String idLang, String idTheso) {
        NodeConcept nodeConcept;
        nodeConcept = getConcept(ds, idConcept, idTheso, idLang);
        if (nodeConcept == null) {
            return null;
        }
        NodeMetaData nodeMetaData = new NodeMetaData();
        nodeMetaData.setCreator(nodeConcept.getTerm().getSource());
        nodeMetaData.setTitle(nodeConcept.getTerm().getLexical_value());
        nodeMetaData.setDcElementsList(new ArrayList<>());
        return nodeMetaData;
    }

    /**
     * Cette fonction permet d'ajouter un Concept Si l'opération échoue, elle
     * envoi un NULL et ne modifie pas la base de données
     *
     * @param ds
     * @param idTheso
     * @param idParent
     * @param idGroup
     * @param idUser
     * @return null si le term existe ou si erreur, sinon le numero de Concept
     * #MR
     */
    /*  public String addConceptNew(HikariDataSource ds,
            String idTheso,
            String idParent,
            String idConcept,
            String idGroup,
            int idUser) {

        ArrayList<String> idConcepts = new ArrayList<>();
        Connection conn = null;
        
        // topTerm
        boolean topConcept = false;
        
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            if(idParent != null)
                topConcept = true;

            String idConcept = addConceptInTableNew(ds, idTheso, idConcept, idGroup, idGroup, idTheso, topConcept)conn, concept, idUser);
            new GroupHelper().addConceptGroupConcept(ds, concept.getIdGroup(), concept.getIdConcept(), concept.getIdThesaurus());
            if (idConcept == null) {
                conn.rollback();
                conn.close();
                return null;
            }

            String idTerm = termHelper.addTerm(conn, term, idConcept, idUser);
            if (idTerm == null) {
                conn.rollback();
                conn.close();
                return null;
            }
            term.setId_term(idTerm);

            /**
             * ajouter le lien hiérarchique
     */
 /*       String inverseRelation = "BT";
            if(relationType == null) 
                relationType = "NT";
            switch (relationType) {
                case "NT" :
                    inverseRelation = "BT";
                    break;
                case "NTG":
                    inverseRelation = "BTG";
                    break;
                case "NTP":
                    inverseRelation = "BTP";
                    break;
                case "NTI":
                    inverseRelation = "BTI";
                    break;
            }               
            
            HierarchicalRelationship hierarchicalRelationship = new HierarchicalRelationship();
            hierarchicalRelationship.setIdConcept1(idParent);
            hierarchicalRelationship.setIdConcept2(idConcept);
            hierarchicalRelationship.setIdThesaurus(concept.getIdThesaurus());
            hierarchicalRelationship.setRole(relationType);

            if (!addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                conn.rollback();
                conn.close();
                return null;
            }

            hierarchicalRelationship.setIdConcept1(idConcept);
            hierarchicalRelationship.setIdConcept2(idParent);
            hierarchicalRelationship.setIdThesaurus(concept.getIdThesaurus());
            hierarchicalRelationship.setRole(inverseRelation);

            if (!addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                conn.rollback();
                conn.close();
                return null;
            }
            if (nodePreference != null) {            
                // création de l'identifiant Handle
                if (nodePreference.isUseHandle()) {
                    if (!addIdHandle(conn, idConcept, concept.getIdThesaurus())) {
                        conn.rollback();
                        conn.close();
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Handle a échouée");
                    }
                }
            }
            conn.commit();
            conn.close();

            if (nodePreference != null) {
                // Si on arrive ici, c'est que tout va bien 
                // alors c'est le moment de récupérer le code ARK
                if (nodePreference.isUseArk()) {
                    idConcepts.add(idConcept);
                    if (!generateArkId(ds, concept.getIdThesaurus(),idConcepts)){ 
                        conn.rollback();
                        conn.close();
                        message = message + "La création Ark a échouée";
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Ark a échouée");
                    }
                }

            }

            return idConcept;

        } catch (SQLException ex) {
            try {
                Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex1) {
            }
        }
        return null;
    }*/
    /**
     * Cette fonction permet d'ajouter un Concept à la table Concept, en avec
     * RollBack
     *
     * @return #MR en construction
     */
    private String addConceptInTableNew(HikariDataSource ds,
            String idTheso,
            String idConcept,
            String idArk,
            String status,
            String notation,
            boolean isTopConcept) {

        Connection conn;
        Statement stmt;

        if (notation == null) {
            notation = "";
        }
        if (status == null) {
            status = "";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                String query;
                try {
                    // Si l'idConcept = null, c'est un nouveau concept sans Id fourni
                    if (idConcept == null) {
                        if (nodePreference.getIdentifierType() == 1) { // identifiants types alphanumérique
                            ToolsHelper toolsHelper = new ToolsHelper();
                            idConcept = toolsHelper.getNewId(10);
                            while (isIdExiste(ds, idConcept)) {
                                idConcept = toolsHelper.getNewId(10);
                            }
                        } else {
                            idConcept = getNumericConceptId(ds);
                        }
                    }
                    query = "Insert into concept "
                            + "(id_concept, id_thesaurus, id_ark, status, notation, top_concept)"
                            + " values ("
                            + "'" + idConcept + "'"
                            + ",'" + idTheso + "'"
                            + ",'" + idArk + "'"
                            + ",'" + status + "'"
                            + ",'" + notation + "'"
                            + "," + isTopConcept
                            + ")";

                    stmt.executeUpdate(query);
                    /**
                     * Ajout des informations dans la table Concept
                     */
                    /*    if (!addConceptHistorique(conn, concept, idUser)) {
                        stmt.close();
                        return null;
                    }*/

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            log.error("Error while adding new Concept : " + sqle);
            idConcept = null;
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept existe ou non
     *
     * @param ds
     * @param idConcept
     * @return boolean
     */
    public boolean isIdExiste(HikariDataSource ds,
            String idConcept) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where "
                            + "id_concept = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept existe ou non dans un
     * thésaurus en particulier
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @return boolean
     */
    public boolean isIdExiste(HikariDataSource ds,
            String idConcept, String idTheso) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where "
                            + "id_concept = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idTheso + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return existe;
    }

    /*
        public ArrayList<String> getIdsOfBranchParLot(HikariDataSource hd,
            String idConceptDeTete,
            String idTheso,
            ArrayList<String> lisIds, int id_alignement_source, ArrayList<String> tmp) {

        lisIds.add(idConceptDeTete);

        ArrayList<String> listIdsOfConceptChildren
                = getListChildrenOfConceptNotExist(hd, idConceptDeTete, idTheso,id_alignement_source);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranchParLot(hd, listIdsOfConceptChildren1,
                    idTheso, lisIds, id_alignement_source,tmp);
        }
        return lisIds;
    }*/
    /**
     * ************************************************************
     * /**************************************************************
     * Fin des nouvelles fonctions stables auteur Miled Rousset
     * /**************************************************************
     * /*************************************************************
     */
    // deprecated by Miled
    /**
     * Cette fonction permet d'ajouter un Top Concept avec le libellé et les
     * relations Si l'opération échoue, on rollback les modifications
     *
     * @param ds
     * @param idParent
     * @param concept
     * @param term
     * @param idUser
     * @return null si le term existe ou si erreur, sinon le numero de Concept
     */
    /* public String addTopConcept(HikariDataSource ds,
            String idParent,
            Concept concept, Term term,
            int idUser) {
        ArrayList<String> idConcepts = new ArrayList<>();
        Connection conn = null;

        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);

            TermHelper termHelper = new TermHelper();
            // controle si le term existe avant de rajouter un concept
            if (termHelper.isTermExist(ds, term.getLexical_value(),
                    term.getId_thesaurus(), term.getLang())) {
                conn.close();
                return null;
            }

            concept.setTopConcept(true);
            String idConcept = addConceptInTable(conn, concept, idUser);
            new GroupHelper().addConceptGroupConcept(ds, concept.getIdGroup(), concept.getIdConcept(), concept.getIdThesaurus());

            if (idConcept == null) {
                conn.rollback();
                conn.close();
                return null;
            }

            String idTerm = termHelper.addTerm(conn, term, idConcept, idUser);
            if (idTerm == null) {
                conn.rollback();
                conn.close();
                return null;
            }
            term.setId_term(idTerm);
            
            if (nodePreference != null) {
                // création de l'identifiant Handle
                if (nodePreference.isUseHandle()) {
                    if (!addIdHandle(conn, idConcept, concept.getIdThesaurus())) {
                        conn.rollback();
                        conn.close();
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Handle a échouée");
                    }
                }
            }

            conn.commit();
            conn.close();
            
            
            // Si on arrive ici, c'est que tout va bien 
            // alors c'est le moment de récupérer le code ARK
            if (nodePreference != null) {
                if (nodePreference.isUseArk()) {
                    idConcepts.add(idConcept);
                    if (!generateArkId(ds, concept.getIdThesaurus(),idConcepts)){ 
                        message = message + "La création Ark a échouée";
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Ark a échouée");
                    }
                }
            }            
            
            return idConcept;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex1) {
            }
        }
        return null;
    }*/
    ///// test à supprimer 
    /*   private void test(HikariDataSource ds){
        Statement stmt;
        ArrayList<String> conceptabush = new ArrayList<>();
        ResultSet resulset;
        String idConcept = "";
        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select * from concept"
                            + " where id_thesaurus ='1'" 
                            + "' and id_concept = '130'";
                    resulset = stmt.executeQuery(query);
                    while (resulset.next()) {
                        idConcept = resulset.getString("id_concept");
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting Concept : " + idConcept, sqle);
        }
    }*/
    /// fin test à supprimer
    /**
     * Cette fonction permet d'ajouter un Concept complet à la base avec le
     * libellé et les relations Si l'opération échoue, elle envoi un NULL et ne
     * modifie pas la base de données
     *
     * @param ds
     * @param idParent
     * @param relationType
     * @param concept
     * @param term
     * @param idUser
     * @return null si le term existe ou si erreur, sinon le numero de Concept
     * #MR
     */
    public String addConcept(HikariDataSource ds,
            String idParent,
            String relationType,
            Concept concept, Term term, int idUser) {

        ArrayList<String> idConcepts = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);

            TermHelper termHelper = new TermHelper();

            if (idParent == null) {
                concept.setTopConcept(true);
            }

            String idConcept = addConceptInTable(conn, concept, idUser);
            if (idConcept == null) {
                conn.rollback();
                conn.close();
                return null;
            }
            if (concept.getIdGroup() != null && !concept.getIdGroup().isEmpty()) {
                new GroupHelper().addConceptGroupConcept(ds, concept.getIdGroup(), concept.getIdConcept(), concept.getIdThesaurus());
            }

            String idTerm = termHelper.addTerm(conn, term, idConcept, idUser);
            if (idTerm == null) {
                conn.rollback();
                conn.close();
                return null;
            }
            term.setId_term(idTerm);

            /**
             * ajouter le lien hiérarchique avec le concept partent sauf si ce
             * n'est pas un TopConcept
             */
            if (!concept.isTopConcept()) {
                String inverseRelation = "BT";
                if (relationType == null) {
                    relationType = "NT";
                }
                switch (relationType) {
                    case "NT":
                        inverseRelation = "BT";
                        break;
                    case "NTG":
                        inverseRelation = "BTG";
                        break;
                    case "NTP":
                        inverseRelation = "BTP";
                        break;
                    case "NTI":
                        inverseRelation = "BTI";
                        break;
                }

                HierarchicalRelationship hierarchicalRelationship = new HierarchicalRelationship();
                hierarchicalRelationship.setIdConcept1(idParent);
                hierarchicalRelationship.setIdConcept2(idConcept);
                hierarchicalRelationship.setIdThesaurus(concept.getIdThesaurus());
                hierarchicalRelationship.setRole(relationType);

                if (!addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                    conn.rollback();
                    conn.close();
                    return null;
                }

                hierarchicalRelationship.setIdConcept1(idConcept);
                hierarchicalRelationship.setIdConcept2(idParent);
                hierarchicalRelationship.setIdThesaurus(concept.getIdThesaurus());
                hierarchicalRelationship.setRole(inverseRelation);

                if (!addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                    conn.rollback();
                    conn.close();
                    return null;
                }
            }
            if (nodePreference != null) {
                // création de l'identifiant Handle
                if (nodePreference.isUseHandle()) {
                    if (!addIdHandle(conn, idConcept, concept.getIdThesaurus())) {
                        conn.rollback();
                        conn.close();
                        message = message + "La création Handle a échouée";
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Handle a échouée");
                    }
                }
            }
            conn.commit();
            conn.close();

            if (nodePreference != null) {
                // Si on arrive ici, c'est que tout va bien 
                // alors c'est le moment de récupérer le code ARK
                if (nodePreference.isUseArk()) {
                    idConcepts.add(idConcept);
                    if (!generateArkId(ds, concept.getIdThesaurus(), idConcepts)) {
                        //    conn.rollback();
                        //    conn.close();
                        message = message + "La création Ark a échouée";
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Ark a échouée");
                    }
                }
            }
            return idConcept;

        } catch (SQLException ex) {
            try {
                Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex1) {
            }
        }
        return null;
    }

    /**
     * Cette fonction permet d'ajouter un Concept et de choisir le type de
     * relation complet à la base avec le libellé et les relations Si
     * l'opération échoue, elle envoi un NULL et ne modifie pas la base de
     * données
     *
     * @param ds
     * @param idParent
     * @param concept
     * @param term
     * @param BTname
     * @param NTname
     * @param idUser
     * @return null si le term existe ou si erreur, sinon le numero de Concept
     */
    public String addConceptSpecial(HikariDataSource ds,
            String idParent,
            Concept concept, Term term, String BTname, String NTname,
            int idUser) {

        Connection conn = null;
        ArrayList<String> idConcepts = new ArrayList<>();
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);

            TermHelper termHelper = new TermHelper();
            // controle si le term existe avant de rajouter un concept
            /*if (termHelper.isTermExist(ds, term.getLexical_value(),
             term.getId_thesaurus(), term.getLang())) {
             conn.close();
             return null;
             }*/
            concept.setTopConcept(false);

            String idConcept = addConceptInTable(conn, concept, idUser);
            new GroupHelper().addConceptGroupConcept(ds, concept.getIdGroup(), concept.getIdConcept(), concept.getIdThesaurus());
            if (idConcept == null) {
                conn.rollback();
                conn.close();
                return null;
            }

            String idTerm = termHelper.addTerm(conn, term, idConcept, idUser);
            if (idTerm == null) {
                conn.rollback();
                conn.close();
                return null;
            }
            term.setId_term(idTerm);

            /**
             * ajouter le lien hiérarchique
             */
            HierarchicalRelationship hierarchicalRelationship = new HierarchicalRelationship();
            hierarchicalRelationship.setIdConcept1(idParent);
            hierarchicalRelationship.setIdConcept2(idConcept);
            hierarchicalRelationship.setIdThesaurus(concept.getIdThesaurus());
            hierarchicalRelationship.setRole(NTname);

            if (!addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                conn.rollback();
                conn.close();
                return null;
            }

            hierarchicalRelationship.setIdConcept1(idConcept);
            hierarchicalRelationship.setIdConcept2(idParent);
            hierarchicalRelationship.setIdThesaurus(concept.getIdThesaurus());
            hierarchicalRelationship.setRole(BTname);

            if (!addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                conn.rollback();
                conn.close();
                return null;
            }

            // Si on arrive ici, c'est que tout va bien 
            // alors c'est le moment de récupérer le code ARK
            if (nodePreference != null) {
                // création de l'identifiant Handle
                if (nodePreference.isUseHandle()) {
                    if (!addIdHandle(conn, idConcept, concept.getIdThesaurus())) {
                        conn.rollback();
                        conn.close();
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Handle a échouée");
                        return null;
                    }
                }
            }

            conn.commit();
            conn.close();

            if (nodePreference != null) {
                // Si on arrive ici, c'est que tout va bien 
                // alors c'est le moment de récupérer le code ARK
                if (nodePreference.isUseArk()) {
                    idConcepts.add(idConcept);
                    if (!generateArkId(ds, concept.getIdThesaurus(), idConcepts)) {
                        //    conn.rollback();
                        //    conn.close();
                        message = message + "La création Ark a échouée";
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Ark a échouée");
                    }
                }
            }
            return idConcept;

        } catch (SQLException ex) {
            try {
                Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex1) {
            }
        }
        return null;
    }

    /**
     * Cette fonction permet de fusionner deux concepts. Le premier concept
     * reste, le second passe en état 'fusionné'.
     *
     * @param ds
     * @param idConcept1
     * @param idConcept2
     * @param idTheso
     * @param idUser
     * @return
     */
    public boolean addConceptFusion(HikariDataSource ds,
            String idConcept1, String idConcept2, String idTheso, int idUser) {
        boolean status = false;
        String idArk = "";
        Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            Concept concept = getThisConcept(ds, idConcept2, idTheso);
            concept.setStatus("hidden");

            if (!addConceptHistorique(conn, concept, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            } else if (!updateStatusConcept(ds, idConcept2, idTheso, "hidden")) {
                conn.rollback();
                conn.close();
                return false;
            }
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into concept_fusion "
                            + "(id_concept1, id_concept2, id_thesaurus, id_user)"
                            + " values ("
                            + "'" + idConcept1 + "'"
                            + ",'" + idConcept2 + "'"
                            + ",'" + idTheso + "'"
                            + ",'" + idUser + "')";
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
            log.error("Error while melting Concept : " + idConcept1 + " and " + idConcept2, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de désactiver un concept (hidden)
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idUser
     * @return
     */
    public boolean desactiveConcept(HikariDataSource ds, String idConcept,
            String idTheso, int idUser) {
        Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    Concept concept = getThisConcept(ds, idConcept, idTheso);
                    concept.setStatus("hidden");

                    if (!addConceptHistorique(conn, concept, idUser)) {
                        conn.rollback();
                        conn.close();
                        return false;
                    }

                    String query = "UPDATE concept "
                            + "set status='hidden'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";

                    stmt.executeUpdate(query);
                    conn.commit();
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error during desactivation of Concept : " + idConcept, sqle);
            return false;
        }
        return true;
    }

    /**
     * Cette fonction permet de réactiver un concept (!hidden)
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idUser
     * @return
     */
    public boolean reactiveConcept(HikariDataSource ds, String idConcept,
            String idTheso, int idUser) {
        Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    Concept concept = getThisConcept(ds, idConcept, idTheso);
                    concept.setStatus("D");

                    if (!addConceptHistorique(conn, concept, idUser)) {
                        conn.rollback();
                        conn.close();
                        return false;
                    }

                    String query = "UPDATE concept "
                            + "set status='D'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";

                    stmt.executeUpdate(query);
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error during reactivation of Concept : " + idConcept, sqle);
            return false;
        }
        return true;
    }

    /**
     * Cette fonction permet de supprimer un Concept avec ses relations et
     * traductions
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean deleteConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, int idUser) {

        RelationsHelper relationsHelper = new RelationsHelper();

        // controle si le Concept a des fils avant de le supprimer
        if (relationsHelper.isRelationNTExist(ds, idConcept, idThesaurus)) {
            return false;
        }
        if (!deleteConcept__(ds, idConcept, idThesaurus, idUser)) {
            return false;
        }
        return true;
    }

    private boolean deleteConcept__(HikariDataSource ds,
            String idConcept, String idThesaurus, int idUser) {
        // suppression du term avec les traductions et les synonymes
        // gestion du Rollback en cas d'erreur

        TermHelper termHelper = new TermHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        NoteHelper noteHelper = new NoteHelper();
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        String idTerm = new TermHelper().getIdTermOfConcept(ds, idConcept, idThesaurus);
        if (idTerm == null) {
            return false;
        }
        Connection conn = null;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);

            if (!termHelper.deleteTerm(conn, idTerm, idThesaurus, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!relationsHelper.deleteAllRelationOfConcept(conn, idConcept, idThesaurus, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!noteHelper.deleteNotesOfConcept(conn, idConcept, idThesaurus)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!noteHelper.deleteNotesOfTerm(conn, idTerm, idThesaurus)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!alignmentHelper.deleteAlignmentOfConcept(conn, idConcept, idThesaurus)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!deleteConceptFromTable(conn, idConcept, idThesaurus, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!deleteConceptFusion(conn, idThesaurus, idConcept)) {
                conn.rollback();
                conn.close();
                return false;
            }
            if (nodePreference != null) {
                // Si on arrive ici, c'est que tout va bien 
                // alors c'est le moment de supprimer le code ARK
                if (nodePreference.isUseArk()) {
                    // suppression de l'identifiant ARK
                }
                // suppression de l'identifiant Handle
                if (nodePreference.isUseHandle()) {
                    String idHandle = getIdHandleOfConcept(ds, idConcept, idThesaurus);
                    if (!deleteIdHandle(conn, idConcept, idHandle, idThesaurus)) {
                        conn.rollback();
                        conn.close();
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La suppression du Handle a échouée");
                        return false;
                    }
                }
            }
            conn.commit();
            conn.close();
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex1) {
                    Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            return false;
        }
    }

    /**
     * Cette fonction permet de supprimer un Concept avec ses relations et
     * traductions, notes, alignements, ... pas de controle s'il a des fils,
     * c'est une suppression définitive
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean deleteConceptWithoutControl(HikariDataSource ds,
            String idConcept, String idThesaurus, int idUser) {
        return deleteConcept__(ds, idConcept, idThesaurus, idUser);
    }

    /**
     * permet de supprimer l'appertenance d'un concept à un groupe
     *
     * @param ds
     * @param idConcept
     * @param idGroup
     * @param idThesaurus
     * @param idUser
     * @return
     */
    public boolean deleteGroupOfConcept(HikariDataSource ds,
            String idConcept, String idGroup, String idThesaurus, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_group_concept where"
                            + " idthesaurus ='" + idThesaurus + "'"
                            + " and idconcept ='" + idConcept + "'"
                            + " and idgroup ='" + idGroup + "'";
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
            log.error("Error while deleting groupe of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de supprimer le concept par ID de la table Concept
     *
     * @param conn
     * @param idConcept
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean deleteConceptFromTable(Connection conn,
            String idConcept, String idThesaurus, int idUser) {

        Statement stmt;
        boolean status = false;
        String idterm = "";
        ResultSet resulset;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_concept ='" + idConcept + "'";
                    stmt.executeUpdate(query);

                    query = "delete from permuted where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_concept ='" + idConcept + "'";
                    stmt.executeUpdate(query);

                    query = "select id_term from preferred_term where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_concept ='" + idConcept + "'";
                    resulset = stmt.executeQuery(query);
                    while (resulset.next()) {
                        idterm = resulset.getString(1);
                    }
                    query = "delete from preferred_term where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_concept ='" + idConcept + "'"
                            + " and id_term = '" + idterm + "'";
                    stmt.executeUpdate(query);

                    query = "delete from term where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_term ='" + idterm + "'";
                    stmt.executeUpdate(query);

                    bushenfants(conn, idConcept, idThesaurus);

                    query = "delete from hierarchical_relationship where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_concept1 ='" + idConcept + "'";
                    stmt.executeUpdate(query);

                    query = "delete from images where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_concept ='" + idConcept + "'";
                    stmt.executeUpdate(query);

                    query = "delete from note where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_concept ='" + idConcept + "'";
                    stmt.executeUpdate(query);

                    query = "delete from note where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_term ='" + idterm + "'";
                    stmt.executeUpdate(query);

                    query = "delete from hierarchical_relationship where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_concept2 ='" + idConcept + "'";
                    stmt.executeUpdate(query);

                    query = "delete from concept_orphan where"
                            + " id_thesaurus ='" + idThesaurus + "'"
                            + " and id_concept ='" + idConcept + "'";
                    stmt.executeUpdate(query);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
                //     conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting Concept : " + idConcept, sqle);
        }
        return status;
    }

    private void bushenfants(Connection conn, String idConcept, String idTheso) {
        Statement stmt;
        ArrayList<String> conceptabush = new ArrayList<>();
        ResultSet resulset;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2 from hierarchical_relationship"
                            + " where id_thesaurus ='" + idTheso
                            + "' and id_concept1 ='" + idConcept
                            + "' and role LIKE 'NT%'";
                    resulset = stmt.executeQuery(query);
                    while (resulset.next()) {
                        conceptabush.add(resulset.getString(1));
                    }
                    for (int i = 0; i < conceptabush.size(); i++) {
                        query = "Insert into concept_orphan (id_concept, id_thesaurus)"
                                + " values('" + conceptabush.get(i) + "', '" + idTheso + "')";
                        stmt.execute(query);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                //     conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting Concept : " + idConcept, sqle);
        }
    }

    /**
     * Cette fonction permet de déplacer une Branche vers un concept d'un autre
     * Groupe
     *
     * @param conn
     * @param idConcept
     * @param idOldConceptBT
     * @param idNewConceptBT
     * @param idThesaurus
     * @param idUser
     * @return true or false
     */
    public boolean moveBranchToConceptOtherGroup(
            Connection conn,
            String idConcept,
            String idOldConceptBT, String idNewConceptBT,
            String idThesaurus, int idUser) {
        try {
            if (!new RelationsHelper().deleteRelationBT(conn, idConcept, idThesaurus, idOldConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            if (!new RelationsHelper().addRelationBT(conn, idConcept, idThesaurus, idNewConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);

        }
        return false;
    }

    /**
     * Cette fonction permet de déplacer une Branche vers un domaine Le domaine
     * de destination est le même que la branche (déplamcent dans le même
     * domaine)
     *
     * @param conn
     * @param idConcept
     * @param idOldConceptBT
     * @param oldMT
     * @param idNewMT
     * @param idThesaurus
     * @param idUser
     * @return true or false
     */
    public boolean moveBranchToAnotherMT(Connection conn,
            String idConcept,
            String idOldConceptBT,
            String oldMT,
            String idNewMT,
            String idThesaurus, int idUser) {
        try {
            RelationsHelper relationsHelper = new RelationsHelper();
            conn.setAutoCommit(false);

            if (!relationsHelper.deleteRelationBT(conn, idConcept, idThesaurus, idOldConceptBT, idUser)) {
                return false;
            }
            // on attribue la relation TT  au concept qui va passer à la racine d'un autre Group,
            // mais comme on est en mode Autocommit= false, l'ancien Group du concept ne change pas tant qu'on a pas Commité  
            return relationsHelper.addRelationTT(conn, idConcept, oldMT, idThesaurus, idUser);

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);

        }
        return false;
    }

    /**
     *
     * @param conn
     * @param idConcept
     * @param idOldConceptBT
     * @param oldMT
     * @param idNewMT
     * @param idThesaurus
     * @param idUser
     * @return
     */
    public boolean moveTTToAnotherMT(Connection conn,
            String idConcept,
            String idOldConceptBT,
            String oldMT,
            String idNewMT,
            String idThesaurus, int idUser) {
        try {
            RelationsHelper relationsHelper = new RelationsHelper();
            conn.setAutoCommit(false);

            return relationsHelper.setRelationMT(conn, idConcept, idNewMT, idThesaurus);

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);

        }
        return false;
    }

    /**
     * Cette fonction permet de supprimer un ConceptCandidat
     *
     * @param ds
     * @param idConcept
     * @param idLang
     * @param idThesaurus
     * @return null si le term existe ou si erreur, sinon le numero de Concept
     */
    /*   public boolean deleteCandidat(HikariDataSource ds,
     String idConcept,
     String idLang, String idThesaurus) {

     TermHelper termHelper = new TermHelper();

     if(! deleteConceptCandidat(ds, idConcept, idThesaurus))
     return false;

     String idTermCandidat = termHelper.addTermCandidat(ds, lexical_value, idLang, idThesaurus, contributor);
     if (idTermCandidat == null) {
     return null;
     }
        
     if(!addRelationConceptTermCandidat(ds, idConceptCandidat,
     idTermCandidat, idThesaurus))
     return null;
        
     termHelper.addPropositionCandidat(ds, idConceptCandidat, contributor, idThesaurus, note, idParentConcept, idGroup);

     return idConceptCandidat;
     }*/
    /**
     * Cette fonction permet d'ajouter une traduction à un terme
     *
     * @param ds
     * @param term
     * @param idUser
     * @return null si le term existe ou si erreur, sinon le numero de Concept
     */
    public boolean addConceptTraduction(
            HikariDataSource ds, Term term, int idUser) {

        Connection conn = null;
        try {
            TermHelper termHelper = new TermHelper();
            // controle si le term existe avant de rajouter un concept
            /*       if (termHelper.isTermExist(ds, term.getLexical_value(),
                    term.getId_thesaurus(), term.getLang())) {
                return false;
            }*/
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            if (!termHelper.addTermTraduction(conn, term, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }

            conn.commit();
            conn.close();
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex1) {
                Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return false;
    }

    /**
     * Cette fonction permet d'ajouter une relation à la table
     * hierarchicalRelationship
     *
     * @param conn
     * @param hierarchicalRelationship
     * @param idUser
     * @return
     */
    public boolean addLinkHierarchicalRelation(Connection conn,
            HierarchicalRelationship hierarchicalRelationship, int idUser) {

        //     Connection conn;
        Statement stmt;
        try {
            //conn.setAutoCommit(false);
            // Get connection from pool
            //       conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {

                    if (!new RelationsHelper().addRelationHistorique(conn, hierarchicalRelationship.getIdConcept1(), hierarchicalRelationship.getIdThesaurus(), hierarchicalRelationship.getIdConcept2(), hierarchicalRelationship.getRole(), idUser, "ADD")) {
                        /*  conn.rollback();
                        conn.close();
                        return false;*/
                    }

                    String query = "Insert into hierarchical_relationship"
                            + "(id_concept1, id_thesaurus, role, id_concept2)"
                            + " values ("
                            + "'" + hierarchicalRelationship.getIdConcept1() + "'"
                            + ",'" + hierarchicalRelationship.getIdThesaurus() + "'"
                            + ",'" + hierarchicalRelationship.getRole() + "'"
                            + ",'" + hierarchicalRelationship.getIdConcept2() + "')";
                    stmt.executeUpdate(query);
                    //  conn.commit();
                } finally {
                    stmt.close();
                }
            } finally {
                // conn.close();
            }
        } catch (SQLException sqle) {
            // To avoid dupplicate Key
            //   System.out.println(sqle.toString());
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                System.out.println(sqle.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * Cette fonction permet d'ajouter un Concept à la table Concept, en
     * paramètre un objet Classe Concept
     *
     * @param conn
     * @param concept
     * @param idUser
     * @return
     */
    public String addConceptInTable(Connection conn,
            Concept concept, int idUser) {

        String idConcept = null;
        String idArk = "";
        //   Connection conn;
        Statement stmt;

        if (concept.getNotation() == null) {
            concept.setNotation("");
        }

        try {
            // Get connection from pool
            //     conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                String query;
                try {
                    if (concept.getIdConcept() == null) {
                        if (nodePreference.getIdentifierType() == 1) { // identifiants types alphanumérique
                            idConcept = getAlphaNumericId(conn);
                            concept.setIdConcept(idConcept);
                        } else {
                            idConcept = getNumericConceptId(conn);
                            concept.setIdConcept(idConcept);
                        }
                    } else {
                        idConcept = concept.getIdConcept();
                    }

                    query = "Insert into concept "
                            + "(id_concept, id_thesaurus, id_ark, status, notation, top_concept, id)"
                            + " values ("
                            + "'" + idConcept + "'"
                            + ",'" + concept.getIdThesaurus() + "'"
                            + ",'" + idArk + "'"
                            + ",'" + concept.getStatus() + "'"
                            + ",'" + concept.getNotation() + "'"
                            + "," + concept.isTopConcept()
                            + "," + concept.getIdConcept()
                            + ")";

                    stmt.executeUpdate(query);

                    /**
                     * Ajout des informations dans la table Concept
                     */
                    if (!addConceptHistorique(conn, concept, idUser)) {
                        stmt.close();
                        return null;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                //  conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept : " + idConcept, sqle);
            }
            idConcept = null;
        }
        return idConcept;
    }

    /**
     * cette funtion permet de savoir si le Id_concept déjà est utilicée
     *
     * @param conn
     * @param id_Concept
     * @return
     * @throws SQLException
     */
//    public boolean ilPeux(Connection conn, String id_Concept) throws SQLException {
//        Statement stmt;
//        ResultSet resultSet;
//
//        try {
//            // Get connection from pool
//            //     conn = ds.getConnection();
//            try {
//                stmt = conn.createStatement();
//                String query;
//                try {
//                    query = "SELECT id_concept from concept where id_concept ='" + id_Concept + "'";
//                    resultSet = stmt.executeQuery(query);
//                    if (!resultSet.next()) {
//                        return true;
//                    }
//
//                } finally {
//                    stmt.close();
//                }
//            } finally {
//                //  conn.close();
//            }
//        } catch (SQLException sqle) {
//            // Log exception
//            if (!sqle.getMessage().contains("duplicate key value violates unique constraint")) {
//                log.error("Error while adding Concept : " + id_Concept, sqle);
//            }
//        }
//        return false;
//    }
    /**
     * Cette fonction permet de savoir si l'ID du concept existe ou non
     *
     * @param conn
     * @param idConcept
     * @return boolean
     */
    public boolean isIdExiste(Connection conn,
            String idConcept) {

        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where "
                            + "id_concept = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept existe ou non
     *
     * @param ds
     * @param idThesaurus
     * @param notation
     * @return boolean
     */
    public boolean isNotationExist(HikariDataSource ds,
            String idThesaurus, String notation) {

        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        if (notation.isEmpty()) {
            return false;
        }
        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where "
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and notation ilike '" + notation.trim() + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if Notation exist : " + notation, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet d'ajouter l'historique d'un concept
     *
     * @param conn
     * @param concept
     * @param idUser
     * @return
     */
    public boolean addConceptHistorique(Connection conn,
            Concept concept, int idUser) {
        boolean status = false;
        String idArk = "";
        //   Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            //     conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into concept_historique "
                            + "(id_concept, id_thesaurus, id_ark, status, notation, top_concept, id_group, id_user)"
                            + " values ("
                            + "'" + concept.getIdConcept() + "'"
                            + ",'" + concept.getIdThesaurus() + "'"
                            + ",'" + idArk + "'"
                            + ",'" + concept.getStatus() + "'"
                            + ",'" + concept.getNotation() + "'"
                            + "," + concept.isTopConcept()
                            + ",'" + concept.getIdGroup() + "'"
                            + ",'" + idUser + "')";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //  conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept : " + concept.getIdConcept(), sqle);
            } else {
                status = true;
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de récupérer l'historique d'un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return String idGroup
     */
    public ArrayList<Concept> getConceptHisoriqueAll(HikariDataSource ds,
            String idConcept, String idThesaurus) {
        ArrayList<Concept> listeConcept = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT modified, status, notation, top_concept, id_group, username from concept_historique, users where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'"
                            + " and concept_historique.id_user=users.id_user"
                            + " order by modified DESC";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            Concept c = new Concept();
                            c.setIdConcept(idConcept);
                            c.setIdThesaurus(idThesaurus);
                            c.setModified(resultSet.getDate("modified"));
                            c.setStatus(resultSet.getString("status"));
                            c.setNotation(resultSet.getString("notation"));
                            c.setTopConcept(resultSet.getBoolean("top_concept"));
                            c.setIdGroup(resultSet.getString("id_group"));
                            c.setUserName(resultSet.getString("username"));
                            listeConcept.add(c);
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
            log.error("Error while getting historique of Concept : " + idConcept, sqle);
        }
        return listeConcept;
    }

    /**
     * Cette fonction permet de récupérer l'historique d'un concept à une date
     * précise
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param date
     * @return String idGroup
     */
    public ArrayList<Concept> getConceptHisoriqueFromDate(HikariDataSource ds,
            String idConcept, String idThesaurus, java.util.Date date) {
        ArrayList<Concept> listeConcept = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT modified, status, notation, top_concept, id_group, username from concept_historique, users where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'"
                            + " and concept_historique.id_user=users.id_user"
                            + " and modified <= '" + date
                            + "' order by modified DESC";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            Concept c = new Concept();
                            c.setIdConcept(idConcept);
                            c.setIdThesaurus(idThesaurus);
                            c.setModified(resultSet.getDate("modified"));
                            c.setStatus(resultSet.getString("status"));
                            c.setNotation(resultSet.getString("notation"));
                            c.setTopConcept(resultSet.getBoolean("top_concept"));
                            c.setIdGroup(resultSet.getString("id_group"));
                            c.setUserName(resultSet.getString("username"));
                            listeConcept.add(c);
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
            log.error("Error while getting date historique of Concept : " + idConcept, sqle);
        }
        return listeConcept;
    }

    /**
     * Permet de mettre à jour l'identifiant Handle
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public boolean updateIdHandle(HikariDataSource ds,
            String idConcept,
            String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        ConceptHelper conceptHelper = new ConceptHelper();

        String idHandle = conceptHelper.getIdHandleOfConcept(ds, idConcept, idThesaurus);

        String privateUri = "?idc=" + idConcept + "&idt=" + idThesaurus;
        HandleHelper handleHelper = new HandleHelper(nodePreference);
        idHandle = handleHelper.updateIdHandle(idHandle, privateUri);
        if (idHandle == null) {
            message = handleHelper.getMessage();
            return false;
        }
        return updateHandleIdOfConcept(ds, idConcept,
                idThesaurus, idHandle);
    }

    /**
     *
     * @param conn
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public boolean addIdHandle(Connection conn,
            String idConcept,
            String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        String privateUri = "?idc=" + idConcept + "&idt=" + idThesaurus;
        HandleHelper handleHelper = new HandleHelper(nodePreference);

        String idHandle = handleHelper.addIdHandle(privateUri);
        if (idHandle == null) {
            message = handleHelper.getMessage();
            return false;
        }
        return updateHandleIdOfConcept(conn, idConcept,
                idThesaurus, idHandle);
    }

    /**
     * permet de générer les identifiants Handle des concepts en paramètres
     *
     * @param conn
     * @param idConcepts
     * @param idThesaurus
     * @return
     */
    public boolean generateHandleId(HikariDataSource conn,
            ArrayList<String> idConcepts,
            String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        String privateUri;
        HandleHelper handleHelper = new HandleHelper(nodePreference);
        String idHandle;
        for (String idConcept : idConcepts) {
            privateUri = "?idc=" + idConcept + "&idt=" + idThesaurus;
            idHandle = handleHelper.addIdHandle(privateUri);
            if (idHandle == null) {
                message = handleHelper.getMessage();
                return false;
            }
            if (!updateHandleIdOfConcept(conn, idConcept,
                    idThesaurus, idHandle)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Permet de supprimer un identifiant Handle de la table Concept et de la
     * plateforme (handle.net) via l'API REST
     *
     * @param conn
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    private boolean deleteIdHandle(Connection conn,
            String idConcept,
            String idHandle,
            String idThesaurus) {
        /**
         * récupération du code Handle via WebServices
         *
         */
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        HandleHelper handleHelper = new HandleHelper(nodePreference);
        if (!handleHelper.deleteIdHandle(idHandle, idThesaurus)) {
            message = handleHelper.getMessage();
            return false;
        }
        return updateHandleIdOfConcept(conn, idConcept,
                idThesaurus, "");
    }

    /**
     * Permet de supprimer tous les identifiants Handle de la table Concept et
     * de la plateforme (handle.net) via l'API REST pour un thésaurus donné
     * suite à une suppression d'un thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public boolean deleteAllIdHandle(HikariDataSource ds,
            String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        ArrayList<String> tabIdHandle = getAllIdHandleOfThesaurus(ds, idThesaurus);
        HandleHelper handleHelper = new HandleHelper(nodePreference);
        if (!handleHelper.deleteAllIdHandle(tabIdHandle)) {
            message = handleHelper.getMessage();
            return false;
        }
        message = handleHelper.getMessage();
        return true;
    }

    /**
     * Cette fonction permet d'ajouter un domaine à un Concept dans la table
     * Concept, en paramètre un objet Classe Concept
     *
     * @param conn
     * @param concept
     * @param idUser
     * @return true or false
     */
    public boolean addNewGroupOfConcept(Connection conn,
            Concept concept, int idUser) {

        Statement stmt;
        boolean status = false;
        try {
            try {
                conn.setAutoCommit(false);
                stmt = conn.createStatement();
                try {

                    if (!addConceptHistorique(conn, concept, idUser)) {
                        conn.rollback();
                        conn.close();
                        return false;
                    }

                    String query = "Insert into concept_group_concept "
                            + "(idgroup, idthesaurus, idconcept)"
                            + " values ("
                            + "'" + concept.getIdConcept() + "'"
                            + ",'" + concept.getIdThesaurus() + "'"
                            + ",'" + concept.getIdArk() + "'"
                            + ",'" + concept.getStatus() + "'"
                            + ",'" + concept.getNotation() + "'"
                            + "," + concept.isTopConcept()
                            + ",'" + concept.getIdGroup() + "')";

                    stmt.executeUpdate(query);
                    status = true;
                    conn.commit();
                } finally {
                    stmt.close();
                }
            } finally {
                //         conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept : " + concept.getIdConcept(), sqle);
            }
        }
        return status;
    }


    public boolean addNewGroupOfConcept(HikariDataSource ds, String idconcept, String idgroup, String idthesaurus) {
        Statement stmt;
        boolean status = false;
        try {
            Connection conn = ds.getConnection();
            try {
                conn.setAutoCommit(false);
                stmt = conn.createStatement();
                try {
                    String query = "INSERT INTO concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('"
                    +idgroup+"', '"+idthesaurus+"', '"+idconcept+"');";

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
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept to Group : " + idgroup, sqle);
            }
        }
        return status;
    }

    /**
     * Cette fonction permet d'insérrer un Concept dans la table Concept avec un
     * idConcept existant (Import) avec Rollback
     *
     * @param ds
     * @param concept
     * @param idUser
     * @return
     */
    public boolean insertConceptInTable(HikariDataSource ds,
            Concept concept, int idUser) {

        Statement stmt;
        boolean status = false;

        if (concept.getCreated() == null) {
            concept.setCreated(new java.util.Date());
        }
        if (concept.getModified() == null) {
            //concept.setModified(new java.util.Date());
            concept.setModified(concept.getCreated());
        }
        if (concept.getStatus() == null) {
            concept.setStatus("D");
        }
        if (concept.getIdArk() == null) {
            concept.setIdArk("");
        }
        if (concept.getIdHandle() == null) {
            concept.setIdHandle("");
        }
        if (concept.getNotation()== null) {
            concept.setNotation("");
        }        

        try {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
            // Get connection from pool
            String query;
            try {
                stmt = conn.createStatement();
                try {
                    query = "Insert into concept "
                            + "(id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id_handle)"
                            + " values ("
                            + "'" + concept.getIdConcept() + "'"
                            + ",'" + concept.getIdThesaurus() + "'"
                            + ",'" + concept.getIdArk() + "'"
                            + ",'" + concept.getCreated() + "'"
                            + ",'" + concept.getModified() + "'"
                            + ",'" + concept.getStatus() + "'"
                            + ",'" + concept.getNotation() + "'"
                            + "," + concept.isTopConcept()
                            + ",'" + concept.getIdHandle() + "')";
                    stmt.executeUpdate(query);
                    status = true;
                    conn.commit();
                    conn.close();
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept : " + concept.getIdConcept(), sqle);
            } else {
                status = true;
            }
        }
        return status;
    }


    public NodeStatus getNodeStatus (HikariDataSource ds, String idConcept, String idThesaurus) {
        
        Statement stmt;
        boolean status = false;        
        NodeStatus nodeStatus = new NodeStatus();        
        try {
            Connection conn = ds.getConnection();
            // Get connection from pool
            String query;
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT * FROM candidat_status WHERE id_concept = '"+idConcept+"';";
                    stmt.executeQuery(query);
                    ResultSet resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        nodeStatus.setIdConcept(resultSet.getString("id_concept"));
                        nodeStatus.setIdStatus(resultSet.getString("id_status"));
                        nodeStatus.setDate(resultSet.getString("date"));
                        nodeStatus.setIdUser(resultSet.getString("id_user"));
                        nodeStatus.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeStatus.setMessage(resultSet.getString("message"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            log.error("Error while getting NodeStatus  du Concept : " + idConcept, sqle);
        }
        return nodeStatus;     
    }

    public boolean setNodeStatus (HikariDataSource ds, String idConcept, String idThesaurus, String idStatus, String date,
                               int idUser, String message) {
        Statement stmt;
        boolean status = false;        
        try {
            Connection conn = ds.getConnection();
            // Get connection from pool
            String query;
            try {
                stmt = conn.createStatement();
                try {
                    query = "INSERT INTO candidat_status(id_concept, id_status, date, id_user, id_thesaurus, message) VALUES ('"
                    +idConcept+"', '"+idStatus+"', '"+date+"', "+idUser+", '"+idThesaurus+"', '"+message+"');";
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
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while setNodeStatus du Concept : " + idConcept, sqle);
            } else {
                status = true;
            }
        }
        return status;
    }


    /**
     * deprecated by Miled Cette fonction permet d'insérrer un Concept dans la
     * table Concept avec un idConcept existant (Import)
     *
     * @param ds
     * @param concept
     * @param urlSite
     * @param isArkActive
     * @param idUser
     * @return
     */
    /*   public boolean insertConceptInTable(HikariDataSource ds,
            Concept concept, String urlSite, boolean isArkActive, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;
        if (concept.getCreated() == null) {
            concept.setCreated(new java.util.Date());
        }
        if (concept.getModified() == null) {
            concept.setModified(new java.util.Date());
        }
        if (concept.getStatus() == null) {
            concept.setStatus("D");
        }
        try {
            // Get connection from pool
            conn = ds.getConnection();
            String query;
            try {
                conn.setAutoCommit(false);
                stmt = conn.createStatement();
                try {*/
    /**
     * récupération du code Ark via WebServices
     *
     */
    /*             String idArk = "";
                    if (isArkActive) {
                        ArrayList<DcElement> dcElementsList = new ArrayList<>();
                        Ark_Client ark_Client = new Ark_Client();
                        idArk = ark_Client.getArkId(
                                new FileUtilities().getDate(),
                                urlSite + "?idc=" + concept.getIdConcept() + "&idt=" + concept.getIdThesaurus(),
                                "", "", dcElementsList, "pcrt"); // pcrt : p= pactols, crt=code DCMI pour collection
                        concept.setIdArk(idArk);
                    } else {
                        concept.setIdArk("");
                    }*/
    /**
     * Ajout des informations dans la table Concept
     */
    /*                   if (!addConceptHistorique(conn, concept, idUser)) {
                        conn.rollback();
                        conn.close();
                        return false;
                    }

                    if (concept.getCreated() == null || concept.getModified() == null) {
                        query = "Insert into concept "
                                + "(id_concept, id_thesaurus, id_ark, status, notation, top_concept)"
                                + " values ("
                                + "'" + concept.getIdConcept() + "'"
                                + ",'" + concept.getIdThesaurus() + "'"
                                + ",'" + concept.getIdArk() + "'"
                                + ",'" + concept.getStatus() + "'"
                                + ",'" + concept.getNotation() + "'"
                                + "," + concept.isTopConcept()
                                + "')";
                    } else {
                        query = "Insert into concept "
                                + "(id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept)"
                                + " values ("
                                + "'" + concept.getIdConcept() + "'"
                                + ",'" + concept.getIdThesaurus() + "'"
                                + ",'" + concept.getIdArk() + "'"
                                + ",'" + concept.getCreated() + "'"
                                + ",'" + concept.getModified() + "'"
                                + ",'" + concept.getStatus() + "'"
                                + ",'" + concept.getNotation() + "'"
                                + ",'" + concept.isTopConcept()
                                + "')";
                    }

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
            if (!sqle.getMessage().contains("duplicate key value violates unique constraint")) {
                log.error("Error while adding Concept : " + concept.getIdConcept(), sqle);
            }
        }
        return status;
    }*/
    /**
     * Cette fonction permet de récupérer un Concept par son id et son thésaurus
     * sous forme de classe Concept (sans les relations) ni le Terme
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public Concept getThisConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        Concept concept = null;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select * from concept " // left join concept_group_concept  on id_concept = idconcept and id_thesaurus = idthesaurus where id_thesaurus = '"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    resultSet.next();
                    if (resultSet.getRow() != 0) {
                        concept = new Concept();
                        concept.setIdConcept(idConcept);
                        concept.setIdThesaurus(idThesaurus);
                        concept.setIdArk(resultSet.getString("id_ark"));
                        concept.setIdHandle(resultSet.getString("id_handle"));
                        concept.setCreated(resultSet.getDate("created"));
                        concept.setModified(resultSet.getDate("modified"));
                        concept.setStatus(resultSet.getString("status"));
                        concept.setNotation(resultSet.getString("notation"));
                        concept.setTopConcept(resultSet.getBoolean("top_concept"));
                        concept.setIdGroup("");//resultSet.getString("idgroup"));
                    }
                    resultSet.close();

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Concept : " + idConcept, sqle);
        }
        return concept;
    }

    /**
     * Cette fonction permet de récupérer la date de modificatin du Concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public Date getModifiedDateOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        Date date = null;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select modified from concept where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    resultSet.next();
                    if (resultSet.getRow() != 0) {
                        date = resultSet.getDate("modified");
                    }
                    resultSet.close();

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting modified date of Concept : " + idConcept, sqle);
        }
        return date;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList
     */
    public ArrayList<String> getAllIdConceptOfThesaurus(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabIdConcept = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and concept.status != 'CA'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * qui n'ont pas de group, pour permettre de retrouver les groupes manquants
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList
     */
    public ArrayList<String> getAllIdConceptOfThesaurusWithoutGroup(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabIdConcept = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where id_thesaurus ='" + idThesaurus + "'"
                            + " and concept.status != 'CA'"
                            + " and id_concept not in (select idconcept from concept_group_concept where idthesaurus = '" + idThesaurus + "')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All IdConcept of Thesaurus without Group : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer le nombre de concepts d'un thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList
     */
    public int getConceptCountOfThesaurus(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        //int count = 0;
        int count = 0;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select count(id_concept) from concept where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and concept.status != 'CA'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        if (resultSet.getInt(1) != 0) {
                            count = resultSet.getInt(1);
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
            log.error("Error while getting count of all IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return count;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id Handle d'un thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList
     */
    public ArrayList<String> getAllIdHandleOfThesaurus(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabId = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_handle from concept where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and (id_handle != null or id_handle != '')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        if (resultSet.getString("id_handle") != null) {
                            if (!resultSet.getString("id_handle").isEmpty()) {
                                tabId.add(resultSet.getString("id_handle"));
                            }
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
            log.error("Error while getting All IdHandle of Thesaurus : " + idThesaurus, sqle);
        }
        return tabId;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id Ark d'un thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList
     */
    public ArrayList<String> getAllIdArkOfThesaurus(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabId = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_ark from concept where "
                            + "id_thesaurus = '" + idThesaurus + "'"
                            + " and (id_ark != null or id_ark != '')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        if (resultSet.getString("id_ark") != null) {
                            if (!resultSet.getString("id_ark").isEmpty()) {
                                tabId.add(resultSet.getString("id_ark"));
                            }
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
            log.error("Error while getting All IdHandle of Thesaurus : " + idThesaurus, sqle);
        }
        return tabId;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id Ark d'un thésaurus
     * sous forme de MAP avec idConcept + idArk
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList
     */
    public HashMap<String, String> getAllIdArkOfThesaurusMap(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        HashMap<String, String> tabIds = new LinkedHashMap<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept, id_ark from concept where "
                            + "id_thesaurus = '" + idThesaurus + "'"
                            + " and (id_ark != null or id_ark != '')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        if (resultSet.getString("id_ark") != null) {
                            if (!resultSet.getString("id_ark").isEmpty()) {
                                tabIds.put(resultSet.getString("id_concept"), resultSet.getString("id_ark"));
                            }
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
            log.error("Error while getting All IdHandle of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIds;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * qui n'ont pas d'identifiants Ark
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList
     */
    public ArrayList<String> getAllIdConceptOfThesaurusWithoutArk(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabIdConcept = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where "
                            + "id_thesaurus = '" + idThesaurus + "'"
                            + " and (id_ark = '' or id_ark = null)";;
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All IdConcept of Thesaurus without Ark : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * qui n'ont pas d'identifiants Handle
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList
     */
    public ArrayList<String> getAllIdConceptOfThesaurusWithoutHandle(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabIdConcept = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and (id_handle = '' or id_handle = null)";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All IdConcept of Thesaurus without Handle : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * en filtrant par Domaine/Group
     *
     * @param ds
     * @param idThesaurus
     * @param idGroup
     * @return ArrayList #MR
     */
    public ArrayList<String> getAllIdConceptOfThesaurusByGroup(HikariDataSource ds,
            String idThesaurus, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabIdConcept = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT DISTINCT concept.id_concept"
                            + " FROM concept, concept_group_concept"
                            + " WHERE"
                            + " concept.id_concept = concept_group_concept.idconcept AND"
                            + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                            + " concept_group_concept.idgroup = '" + idGroup + "';";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * retourne tous les concepts d'un Groupe pour un thésaurus Permet de
     * retourner une ArrayList de String (idConcept) par thésaurus et par groupe
     * / ou null si rien
     *
     * @param ds le pool de connexion
     * @param idGroup
     * @param idThesaurus
     * @return Objet Class ArrayList NodeConceptGroup
     */
    public ArrayList<String> getListConceptIdOfGroup(HikariDataSource ds,
            String idGroup,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList tabIdConceptGroup = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idconcept from concept_group_concept where "
                            + " idthesaurus = '" + idThesaurus + "' and "
                            + " idgroup = '" + idGroup + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    tabIdConceptGroup = new ArrayList();
                    while (resultSet.next()) {
                        tabIdConceptGroup.add(resultSet.getString("idconcept"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Id or Concepts of group : " + idGroup, sqle);
        }
        return tabIdConceptGroup;
    }

    /**
     * Permet de retourner tous les identifiants BT pour un concept donné dans
     * le même groupe cette fonction permet de connaitre la polyhierarchie d'un
     * concept dans un domaine
     *
     * @param ds
     * @param idConcept
     * @param idGroup
     * @param idTheso
     * @return #MR
     */
    public ArrayList<String> getAllBTOfConceptOfThisGroup(HikariDataSource ds,
            String idConcept, String idGroup, String idTheso) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabIdBT = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2 from hierarchical_relationship, concept_group_concept"
                            + " where"
                            + " concept_group_concept.idthesaurus = hierarchical_relationship.id_thesaurus AND"
                            + " concept_group_concept.idconcept = hierarchical_relationship.id_concept1 AND"
                            + " concept_group_concept.idgroup = '" + idGroup + "' AND"
                            + " hierarchical_relationship.role = 'BT' AND"
                            + " hierarchical_relationship.id_concept1 = '" + idConcept + "' AND"
                            + " hierarchical_relationship.id_thesaurus = '" + idTheso + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        tabIdBT.add(resultSet.getString("id_concept2"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All IdBT of this Group of Concept : " + idConcept, sqle);
        }
        return tabIdBT;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * (cette fonction sert pour la génération des identifiants pour Wikidata)
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList
     */
    public ArrayList<NodeConceptArkId> getAllConceptArkIdOfThesaurus(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeConceptArkId> nodeConceptArkIds = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept, id_ark from concept where id_thesaurus = '"
                            + idThesaurus + "' order by id_concept ASC";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        NodeConceptArkId nodeConceptArkId = new NodeConceptArkId();
                        nodeConceptArkId.setIdConcept(resultSet.getString("id_concept"));
                        nodeConceptArkId.setIdArk(resultSet.getString("id_ark"));
                        nodeConceptArkIds.add(nodeConceptArkId);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All Id of Concept _ Ark of Thesaurus : " + idThesaurus, sqle);
        }
        return nodeConceptArkIds;
    }

    public ArrayList<String> getAllIdConceptOfThesaurus(Connection conn,
            String idThesaurus) {

        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabIdConcept = new ArrayList<>();

        try {
            // Get connection from pool
            stmt = conn.createStatement();
            try {
                String query = "select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "'";
                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    tabIdConcept.add(resultSet.getString("id_concept"));
                }

            } finally {
                stmt.close();
            }

        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet d'exporter tous les concepts d'un thésaurus et les
     * charger dans la classe No
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param nodeConceptExports
     * @return
     */
    public ArrayList<NodeConceptExport> exportAllConcepts(HikariDataSource ds,
            String idConcept, String idThesaurus,
            ArrayList<NodeConceptExport> nodeConceptExports) {

        ConceptHelper conceptHelper = new ConceptHelper();

        ArrayList<String> listIdsOfConceptChildren = conceptHelper.getListChildrenOfConcept(ds, idConcept, idThesaurus);

        NodeConceptExport nodeConcept = conceptHelper.getConceptForExport(ds, idConcept, idThesaurus, false, false);

        //    System.out.println("IdConcept = " + idConcept);
        /// attention il y a un problème ici, il faut vérifier pourquoi nous avons un Concept Null
        if (nodeConcept == null || nodeConcept.getConcept() == null) {
            System.err.println("Attention Null proche de = : " + idConcept);
            return null;
        }

        nodeConceptExports.add(nodeConcept);

        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            nodeConcept = conceptHelper.getConceptForExport(ds, listIdsOfConceptChildren1, idThesaurus, false, false);
            nodeConceptExports.add(nodeConcept);
            if (!nodeConcept.getNodeListOfNT().isEmpty()) {
                for (int j = 0; j < nodeConcept.getNodeListOfNT().size(); j++) {

                    exportAllConcepts(ds,
                            nodeConcept.getNodeListOfNT().get(j).getUri().getIdConcept(),
                            idThesaurus, nodeConceptExports);
                }
            }
        }
        return nodeConceptExports;
    }

    /**
     * Cette fonction permet de récupérer le nom d'un Concept sinon renvoie un
     * une chaine vide
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet class Concept
     */
    public String getLexicalValueOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String lexicalValue = "";
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lexical_value from term, preferred_term where"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + " and preferred_term.id_concept = '" + idConcept + "'"
                            + " and term.lang = '" + idLang + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        lexicalValue = resultSet.getString("lexical_value");
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting LexicalValue of Concept : " + idConcept, sqle);
        }
        return lexicalValue.trim();
    }

    /**
     * Cette fonction permet de récupérer l'identifiant Ark sinon renvoie un une
     * chaine vide
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public String getIdArkOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String ark = "";
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_ark from concept where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {

                        ark = resultSet.getString("id_ark");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idArk of Concept : " + idConcept, sqle);
        }
        return ark;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant Handle sinon renvoie un
     * une chaine vide
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public String getIdHandleOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String handle = "";
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_handle from concept where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {

                        handle = resultSet.getString("id_handle");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idHandle of Concept : " + idConcept, sqle);
        }
        return handle;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après
     * l'idHandle
     *
     * @param ds
     * @param arkId
     * @return IdConcept
     */
    public String getIdConceptFromArkId(HikariDataSource ds, String arkId) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String idConcept = null;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where"
                            + " id_ark = '" + arkId + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {
                        idConcept = resultSet.getString("id_concept");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idConcept by idArk : " + arkId, sqle);
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après
     * l'idArk
     *
     * @param ds
     * @param handleId
     * @return IdConcept
     */
    public String getIdConceptFromHandleId(HikariDataSource ds, String handleId) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String idConcept = null;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where"
                            + " id_handle = '" + handleId + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {
                        idConcept = resultSet.getString("id_concept");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idConcept by idArk : " + handleId, sqle);
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après
     * l'idArk
     *
     * @param ds
     * @param arkId
     * @return IdConcept
     */
    public String getIdThesaurusFromArkId(HikariDataSource ds, String arkId) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String idThesaurus = null;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_thesaurus from concept where"
                            + " id_ark = '" + arkId + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {
                        idThesaurus = resultSet.getString("id_thesaurus");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idThesaurus by idArk : " + arkId, sqle);
        }
        return idThesaurus;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après
     * l'idHandle
     *
     * @param ds
     * @param handleId
     * @return IdConcept
     */
    public String getIdThesaurusFromHandleId(HikariDataSource ds, String handleId) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String idThesaurus = null;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_thesaurus from concept where"
                            + " id_handle = '" + handleId + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {
                        idThesaurus = resultSet.getString("id_thesaurus");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idThesaurus by idArk : " + handleId, sqle);
        }
        return idThesaurus;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Group d'un Concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return String idGroup
     */
    public String getGroupIdOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String idGroup = null;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group_concept where idthesaurus = '"
                            + idThesaurus + "'"
                            + " and idconcept = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        if (resultSet.next()) {
                            idGroup = resultSet.getString("idgroup");
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
            log.error("Error while getting Id of group of Concept : " + idConcept, sqle);
        }
        return idGroup;
    }

    public void insertID_grouptoPermuted(HikariDataSource ds, String id_thesaurus, String id_concept) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String idGroup = null;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "update permuted set id_group = (select id_group from concept"
                            + " where id_thesaurus = '" + id_thesaurus
                            + "' and id_concept = '" + id_concept
                            + "') where  id_concept ='" + id_concept
                            + "'";
                    stmt.execute(query);
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Id of group of Concept : " + id_concept, sqle);
        }

    }

    /**
     * Cette fonction permet de récupérer les identifiants des Group d'un
     * Concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return String idGroup
     */
    public ArrayList<String> getListGroupIdOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> idGroup = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    /*
                    String query = "select id_group from concept where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'";*/
                    String query = "select idgroup from concept_group_concept where idthesaurus = '"
                            + idThesaurus + "'"
                            + " and idconcept = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroup.add(resultSet.getString("idgroup"));
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
            log.error("Error while getting Id of group of Concept : " + idConcept, sqle);
        }
        return idGroup;
    }

    /**
     * Cette fonction permet de récupérer les identifiants des Group d'un
     * Concept dont il est le fils direct
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return String idGroup
     */
    public ArrayList<String> getListGroupParentIdOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> idGroup = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_group from concept where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'"
                            + " and top_concept=true";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroup.add(resultSet.getString("id_group"));
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
            log.error("Error while getting Id of group of Concept : " + idConcept, sqle);
        }
        return idGroup;
    }

    public ArrayList<String> getListGroupParentIdOfGroup(HikariDataSource ds,
            String idGRoup, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> idGroupParentt = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_group1 from relation_group where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and id_group2 = '" + idGRoup + "'"
                            + " and relation='sub'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroupParentt.add(resultSet.getString("id_group1"));
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
            log.error("Error while getting Id of group of Concept : " + idGRoup, sqle);
        }
        return idGroupParentt;
    }

    public ArrayList<String> getListGroupChildIdOfGroup(HikariDataSource ds,
            String idGRoup, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> idGroupParentt = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_group2 from relation_group where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and id_group1 = '" + idGRoup + "'"
                            + " and relation='sub'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroupParentt.add(resultSet.getString("id_group2"));
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
            log.error("Error while getting Id of group of Concept : " + idGRoup, sqle);
        }
        return idGroupParentt;
    }

    /**
     * Cette fonction permet de récupérer les identifiants des Group des parents
     * d'un concept SAUF les groupes du parent passé en paramètre
     *
     * @param ds
     * @param idConceptParent
     * @param idThesaurus
     * @param idNoGroup le parent dont on ne souhaite pas avoir les groupes
     * @return String idGroup
     */
    public ArrayList<String> getListGroupIdParentOfConceptOtherThan(HikariDataSource ds,
            ArrayList<String> idConceptParent, String idThesaurus, String idNoGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> idGroup = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT DISTINCT id_group from concept where id_thesaurus = '"
                            + idThesaurus + "' and (";
                    for (String s : idConceptParent) {
                        query += "id_concept = '" + s + "' or ";
                    }
                    query = query.substring(0, query.length() - 4);
                    query += ") and id_concept != '" + idNoGroup + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroup.add(resultSet.getString("id_group"));
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
            log.error("Error while getting Id group of parent of Concept", sqle);
        }
        return idGroup;
    }

    /**
     * Cettte fonction permet de retourner la liste des TopConcept avec IdArk et
     * handle pour un groupe
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return
     */
    public ArrayList<NodeUri> getListIdsOfTopConceptsForExport(HikariDataSource ds,
            String idGroup, String idThesaurus) {

        ArrayList<NodeUri> NodeUris = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept, id_ark, id_handle from concept"
                            + " left join concept_group_concept on id_concept = idconcept"
                            + " and id_thesaurus = idthesaurus"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and idgroup = '" + idGroup + "'"
                            + " and top_concept = true";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
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
                        nodeUri.setIdConcept(resultSet.getString("id_concept"));

                        NodeUris.add(nodeUri);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Liste ID of TT of Group with ark and handle : " + idGroup, sqle);
        }
        return NodeUris;
    }

    /**
     * Cette fonction permet de récupérer la liste des Ids of Topconcepts
     * suivant l'id du groupe et le thésaurus
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return Objet class NodeConceptTree
     */
    public ArrayList<String> getListIdsOfTopConceptsByGroup(HikariDataSource ds,
            String idGroup, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> listIdOfTopConcept = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and id_concept IN (SELECT idconcept FROM concept_group_concept WHERE idgroup = '"
                            + idGroup + "' AND idthesaurus = '"
                            + idThesaurus + "')"
                            + " and top_concept = true";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        listIdOfTopConcept.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Ids of TopConcept of Group : " + idGroup, sqle);
        }
        return listIdOfTopConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Ids of Topconcepts pour
     * un thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return Objet class NodeTT
     */
    public ArrayList<NodeTT> getAllListIdsOfTopConcepts(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeTT> listIdOfTopConcept = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept,"
                            + "id_ark, id_handle, idgroup from concept left join concept_group_concept on id_concept = idconcept and id_thesaurus = idthesaurus where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and top_concept = true";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeTT nodeTT = new NodeTT();
                        nodeTT.setIdConcept(resultSet.getString("id_concept"));
                        nodeTT.setIdArk(resultSet.getString("id_ark"));
                        nodeTT.setIdArk(resultSet.getString("id_handle"));
                        nodeTT.setIdGroup(resultSet.getString("idgroup"));
                        listIdOfTopConcept.add(nodeTT);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Ids of TopConcept of thésaurus : " + idThesaurus, sqle);
            listIdOfTopConcept = null;
        }
        return listIdOfTopConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Topconcepts suivant l'id
     * du groupe et le thésaurus sous forme de classe NodeConceptTree (sans les
     * relations)
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @param idLang
     * @param isSortByNotation
     * @return Objet class NodeConceptTree
     */
    public ArrayList<NodeConceptTree> getListTopConcepts(HikariDataSource ds,
            String idGroup, String idThesaurus, String idLang, boolean isSortByNotation) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeConceptTree> nodeConceptTree = null;
        String query;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if (isSortByNotation) {
                        query = "SELECT concept.notation, concept.status, concept.id_concept"
                                + " FROM concept, concept_group_concept WHERE"
                                + " concept_group_concept.idconcept = concept.id_concept AND"
                                + " concept_group_concept.idthesaurus = concept.id_thesaurus AND"
                                + " concept_group_concept.idgroup = '" + idGroup + "' AND"
                                + " concept.id_thesaurus = '" + idThesaurus + "' AND"
                                + " concept.top_concept = true"
                                + " ORDER BY concept.notation ASC";
                    } else {
                        query = "SELECT concept.status, concept.id_concept"
                                + " FROM concept, concept_group_concept WHERE"
                                + " concept_group_concept.idconcept = concept.id_concept AND"
                                + " concept_group_concept.idthesaurus = concept.id_thesaurus AND"
                                + " concept_group_concept.idgroup = '" + idGroup + "' AND"
                                + " concept.id_thesaurus = '" + idThesaurus + "' AND"
                                + " concept.top_concept = true";
                    }

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    nodeConceptTree = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeConceptTree nodeConceptTree1 = new NodeConceptTree();
                        nodeConceptTree1.setIdConcept(resultSet.getString("id_concept"));
                        if (isSortByNotation) {
                            nodeConceptTree1.setNotation(resultSet.getString("notation"));
                        }
                        nodeConceptTree1.setStatusConcept(resultSet.getString("status"));
                        nodeConceptTree1.setIdThesaurus(idThesaurus);
                        nodeConceptTree1.setIdLang(idLang);
                        nodeConceptTree1.setIsTopTerm(true);
                        nodeConceptTree.add(nodeConceptTree1);
                    }
                    for (NodeConceptTree nodeConceptTree1 : nodeConceptTree) {
                        query = "SELECT term.lexical_value FROM"
                                + " preferred_term, term WHERE"
                                + " preferred_term.id_term = term.id_term AND"
                                + " preferred_term.id_thesaurus = term.id_thesaurus AND"
                                + " term.lang = '" + idLang + "' AND"
                                + " preferred_term.id_concept = '" + nodeConceptTree1.getIdConcept() + "' AND"
                                + " term.id_thesaurus = '" + idThesaurus + "'";

                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        resultSet.next();
                        if (resultSet.getRow() == 0) {
                            nodeConceptTree1.setTitle("");
                        } else {
                            nodeConceptTree1.setTitle(resultSet.getString("lexical_value"));
                        }
                        nodeConceptTree1.setHaveChildren(
                                haveChildren(ds, idThesaurus, nodeConceptTree1.getIdConcept())
                        );
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting TopConcept of Group : " + idGroup, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(nodeConceptTree);
        }
        return nodeConceptTree;
    }

    /**
     * Cette fonction permet de rendre un Concept de type Topconcept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public boolean setNotTopConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {
        Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set top_concept = false"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idThesaurus + "'";

                    stmt.executeUpdate(query);
                    return true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating group of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de rendre un Concept de type Topconcept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public boolean setTopConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {
        Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set top_concept = true"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idThesaurus + "'";

                    stmt.executeUpdate(query);
                    return true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating group of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de savoir si le Concept est un TopConcept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idGroup
     * @return boolean
     */
    public boolean isTopConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select top_concept from concept where "
                            + " id_concept = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and id_group = '" + idGroup + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getBoolean("top_concept");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while Asking if TopConcept : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le Concept est un TopConcept sans
     * définir le group (pour permettre de nettoyer les orphelins)
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return boolean
     */
    public boolean isTopConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select top_concept from concept where "
                            + " id_concept = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getBoolean("top_concept");
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while Asking if TopConcept : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de récupérer les Ids des concepts suivant l'id du
     * Concept-Père et le thésaurus sous forme de classe tableau pas de tri
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet Array String
     */
    public ArrayList<String> getListChildrenOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> listIdsOfConcept = new ArrayList<>();

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
                        listIdsOfConcept.add(resultSet.getString("id_concept2"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of Id of Concept : " + idConcept, sqle);
        }
        return listIdsOfConcept;
    }

    /*    public ArrayList<String> getListChildrenOfConceptNotExist(HikariDataSource ds,
            String idConcept, String idThesaurus, int id_alignement_source) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> listIdsOfConcept = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept2 from hierarchical_relationship"
                            + " where id_concept2 not in ( SELECT "
                            + " hierarchical_relationship.id_concept2"
                            + " FROM "
                            + " public.alignement,"
                            + " public.hierarchical_relationship "
                            + " WHERE "
                            + " alignement.internal_id_concept = hierarchical_relationship.id_concept2 AND"
                            + " alignement.internal_id_thesaurus = hierarchical_relationship.id_thesaurus AND"
                            + " alignement.id_alignement_source = " + id_alignement_source + " AND "
                            + " hierarchical_relationship.role LIKE 'NT%'"
                            + " AND hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'"
                            + " and hierarchical_relationship.id_concept1 = '" + idConcept + "')"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and role LIKE 'NT%'"
                            + " and id_concept1= '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        listIdsOfConcept.add(resultSet.getString("id_concept2"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of Id of Concept : " + idConcept, sqle);
        }
        return listIdsOfConcept;
    }*/
    private ArrayList<NodeHieraRelation> getRelations(
            ArrayList<NodeHieraRelation> nodeHieraRelations,
            ArrayList<String> relations) {

        ArrayList<NodeHieraRelation> nodeHieraRelations1 = new ArrayList<>();

        for (NodeHieraRelation nodeHieraRelation : nodeHieraRelations) {
            if (relations.contains(nodeHieraRelation.getRole())) {
                nodeHieraRelations1.add(nodeHieraRelation);
            };
        }
        return nodeHieraRelations1;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept par son id et son thésaurus et la langue On récupère aussi les
     * IdArk si Ark est actif
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param isArkActive
     * @return Objet class NodeConcept #MR optimisation le 23/11/2018
     */
    public NodeConceptExport getConceptForExport(HikariDataSource ds,
            String idConcept, String idThesaurus, boolean isArkActive, boolean isCandidatExport) {

        NodeConceptExport nodeConceptExport = new NodeConceptExport();
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        TermHelper termHelper = new TermHelper();
        GroupHelper groupHelper = new GroupHelper();
        NoteHelper noteHelper = new NoteHelper();
        GpsHelper gpsHelper = new GpsHelper();
        ImagesHelper imagesHelper = new ImagesHelper();
        
        String htmlTagsRegEx = "<[^>]*>";

        // les relations BT, NT, RT
        ArrayList<NodeHieraRelation> nodeListRelations = relationsHelper.getAllRelationsOfConcept(ds, idConcept, idThesaurus);

        nodeConceptExport.setNodeListOfBT(getRelations(nodeListRelations, nodeConceptExport.getRelationsBT()));
        nodeConceptExport.setNodeListOfNT(getRelations(nodeListRelations, nodeConceptExport.getRelationsNT()));
        nodeConceptExport.setNodeListIdsOfRT(getRelations(nodeListRelations, nodeConceptExport.getRelationsRT()));

        //récupération du Concept        
        Concept concept = getThisConcept(ds, idConcept, idThesaurus);
        if (concept == null) {
            return null;
        }
        nodeConceptExport.setConcept(concept);

        //récupération les aligenemnts 
        nodeConceptExport.setNodeAlignmentsList(alignmentHelper.getAllAlignmentOfConceptNew(ds, idConcept, idThesaurus));

        //récupération des traductions        
        nodeConceptExport.setNodeTermTraductions(termHelper.getAllTraductionsOfConcept(ds, idConcept, idThesaurus));

        //récupération des Non Prefered Term        
        nodeConceptExport.setNodeEM(termHelper.getAllNonPreferredTerms(ds, idConcept, idThesaurus));

        //récupération des Groupes ou domaines 
        nodeConceptExport.setNodeListIdsOfConceptGroup(groupHelper.getListGroupOfConceptArk(ds, idThesaurus, idConcept));

        //récupération des notes du Terme
//#### SQL #### //        
        String idTerm = termHelper.getIdTermOfConcept(ds, idConcept, idThesaurus);
//#### SQL #### //        

//#### SQL #### //
        ArrayList<NodeNote> noteTerm = noteHelper.getListNotesTermAllLang(ds, idTerm, idThesaurus);
        if (isCandidatExport) {
            for (NodeNote note : noteTerm) {
                String str = formatLinkTag(note.getLexicalvalue());
                note.setLexicalvalue(str.replaceAll(htmlTagsRegEx, ""));
            }
        }
        nodeConceptExport.setNodeNoteTerm(noteTerm);
//#### SQL #### //        

        //récupération des Notes du Concept
//#### SQL #### //      

        ArrayList<NodeNote> noteConcept = noteHelper.getListNotesConceptAllLang(ds, idConcept, idThesaurus);
        if (isCandidatExport) {
            for (NodeNote note : noteConcept) {
                String str = formatLinkTag(note.getLexicalvalue());
                note.setLexicalvalue(str.replaceAll(htmlTagsRegEx, ""));
            }
        }
        nodeConceptExport.setNodeNoteConcept(noteConcept);
//#### SQL #### //

        //récupération des coordonnées GPS
//#### SQL #### //        
        NodeGps nodeGps = gpsHelper.getCoordinate(ds, idConcept, idThesaurus);
//#### SQL #### //        

        if (nodeGps != null) {
            nodeConceptExport.setNodeGps(nodeGps);
        }

        ArrayList<NodeImage> nodeImages = imagesHelper.getExternalImages(ds, idConcept, idThesaurus);
        if (nodeImages != null) {
            ArrayList<String> imagesUri = new ArrayList<>();
            for (NodeImage nodeImage : nodeImages) {
                imagesUri.add(nodeImage.getUri());
            }
            nodeConceptExport.setNodeimages(imagesUri);
        }

        if (isCandidatExport) {
            nodeConceptExport.setMessages(new MessageDao().getAllMessagesByCandidat(ds, idConcept, idThesaurus));
            nodeConceptExport.setVotes(new CandidatDao().getAllVotesByCandidat(ds, idConcept, idThesaurus));
        }

        return nodeConceptExport;
    }

    public static String formatLinkTag(String initialStr) {
        Pattern MY_PATTERN = Pattern.compile("<a(.*?)a>");
        Matcher m = MY_PATTERN.matcher(initialStr);
        while (m.find()) {
            String link = "<a" + m.group(1) + "a>";
            ArrayList<HTMLLinkElement> result = new HtmlLinkExtraction().extractHTMLLinks(link);
            initialStr = initialStr.replace(link, result.get(0).getLinkElement()
                    + " (" + result.get(0).getLinkAddress() + ")");
        }
        return initialStr;
    }

    public ArrayList<NodeFusion> getConceptFusion(HikariDataSource ds,
            String idConcept, String idLang, String idThesaurus) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeFusion> nf = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept1, id_concept2 from concept_fusion where"
                            + " concept_fusion.id_thesaurus = '" + idThesaurus + "'"
                            + " AND (concept_fusion.id_concept1 = '" + idConcept + "'"
                            + " OR concept_fusion.id_concept2 = '" + idConcept + "')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        NodeFusion n = new NodeFusion();
                        n.setIdConcept1(resultSet.getString("id_concept1"));
                        n.setIdConcept2(resultSet.getString("id_concept2"));
                        n.setLexicalValue1(getLexicalValueOfConcept(ds, resultSet.getString("id_concept1"), idThesaurus, idLang));
                        n.setLexicalValue2(getLexicalValueOfConcept(ds, resultSet.getString("id_concept2"), idThesaurus, idLang));
                        nf.add(n);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting fusion of Concept : " + idConcept, sqle);
        }
        return nf;
    }

    /**
     * Cette fonction permet de récupérer les Id Ark d'une liste d'Identifiants
     * de Groups et les rajouter dans le tableau de NodeUri
     *
     * @param nodeListIdOfGroup
     * @param idThesaurus
     * @return ArrayList<NodeUri>
     */
    private ArrayList<NodeUri> getListIdArkOfGroup(
            HikariDataSource ds,
            ArrayList<String> nodeListIdOfGroup,
            String idThesaurus) {

        ArrayList<NodeUri> nodeListIdOfGroup_idArk = new ArrayList<>();

        String idArk;
        for (String nodeListIdOfGroup1 : nodeListIdOfGroup) {
            idArk = new GroupHelper().getIdArkOfGroup(ds, nodeListIdOfGroup1, idThesaurus);
            NodeUri nodeUri = new NodeUri();
            if (idArk == null || idArk.trim().isEmpty()) {
                nodeUri.setIdArk("");
            } else {
                nodeUri.setIdArk(idArk);
            }
            nodeUri.setIdConcept(nodeListIdOfGroup1);
            nodeListIdOfGroup_idArk.add(nodeUri);
        }
        return nodeListIdOfGroup_idArk;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * ou plusieurs Concept par une chaîne de caractère, le thésaurus et la
     * langue
     *
     * @param ds
     * @param value
     * @param idThesaurus
     * @param idLang
     * @param isArkActif
     * @return Objet class NodeConcept
     */
    public ArrayList<NodeConceptExport> getMultiConceptForExport(HikariDataSource ds,
            String value, String idThesaurus, String idLang, boolean isArkActif) {

        ArrayList<NodeConceptExport> listNce = new ArrayList<>();

        //Récupération des concept
        ArrayList<NodeSearch> listRes = new SearchHelper().searchTermNew(ds, value, idLang, idThesaurus, "", 1, false);
        for (NodeSearch ns : listRes) {
            Concept concept = getThisConcept(ds, ns.getIdConcept(), idThesaurus);
            NodeConceptExport nce = new NodeConceptExport();
            nce.setConcept(concept);
            listNce.add(nce);
        }

        for (NodeConceptExport nce : listNce) {
            String idConcept = nce.getConcept().getIdConcept();
            RelationsHelper relationsHelper = new RelationsHelper();

            // récupération des BT
            ArrayList<NodeHieraRelation> nodeListIdOfBT_Ark
                    = relationsHelper.getListBT(ds, idConcept, idThesaurus);
            nce.setNodeListOfBT(nodeListIdOfBT_Ark);

            //récupération des termes spécifiques
            ArrayList<NodeHieraRelation> nodeListIdOfNT_Ark
                    = relationsHelper.getListNT(ds, idConcept, idThesaurus);
            nce.setNodeListOfNT(nodeListIdOfNT_Ark);

            //récupération des termes associés
            ArrayList<NodeHieraRelation> nodeListIdOfRT_Ark
                    = relationsHelper.getListRT(ds, idConcept, idThesaurus);
            nce.setNodeListIdsOfRT(nodeListIdOfRT_Ark);

            //récupération des Non Prefered Term
            nce.setNodeEM(new TermHelper().getAllNonPreferredTerms(ds, idConcept, idThesaurus));

            //récupération des traductions
            nce.setNodeTermTraductions(new TermHelper().getAllTraductionsOfConcept(ds, idConcept, idThesaurus));

            //récupération des Groupes
            ArrayList<NodeUri> nodeListIdsOfConceptGroup_Ark = getListIdArkOfGroup(ds,
                    new GroupHelper().getListIdGroupOfConcept(ds, idThesaurus, idConcept),
                    idThesaurus);
            nce.setNodeListIdsOfConceptGroup(nodeListIdsOfConceptGroup_Ark);

            //récupération des notes du Terme
            String idTerm = new TermHelper().getIdTermOfConcept(ds, idConcept, idThesaurus);
            nce.setNodeNoteTerm(new NoteHelper().getListNotesTermAllLang(ds, idTerm, idThesaurus));

            //récupération des Notes du Concept
            nce.setNodeNoteConcept(new NoteHelper().getListNotesConceptAllLang(ds, idConcept, idThesaurus));

            // récupération des Alignements
            nce.setNodeAlignmentsList(new AlignmentHelper().getAllAlignmentOfConceptNew(ds, idConcept, idThesaurus));
        }

        return listNce;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * ou plusieurs Concept par une chaîne de caractère, suivant le thésaurus,
     * la langue et le group
     *
     * @param ds
     * @param value
     * @param idThesaurus
     * @param idGroup
     * @param idLang
     * @param isArkActif
     * @return Objet class NodeConcept
     */
    public ArrayList<NodeConceptExport> getMultiConceptForExport(HikariDataSource ds,
            String value,
            String idLang,
            String idGroup,
            String idThesaurus,
            boolean isArkActif) {

        ArrayList<NodeConceptExport> listNce = new ArrayList<>();

        //Récupération des concept
        ArrayList<NodeSearch> listRes = new SearchHelper().searchTermNew(ds, value, idLang, idThesaurus, idGroup, 1, false);
        for (NodeSearch ns : listRes) {
            Concept concept = getThisConcept(ds, ns.getIdConcept(), idThesaurus);
            NodeConceptExport nce = new NodeConceptExport();
            nce.setConcept(concept);
            listNce.add(nce);
        }

        for (NodeConceptExport nce : listNce) {
            String idConcept = nce.getConcept().getIdConcept();
            RelationsHelper relationsHelper = new RelationsHelper();

            // récupération des BT
            ArrayList<NodeHieraRelation> nodeListIdOfBT_Ark
                    = relationsHelper.getListBT(ds, idConcept, idThesaurus);
            nce.setNodeListOfBT(nodeListIdOfBT_Ark);

            //récupération des termes spécifiques
            ArrayList<NodeHieraRelation> nodeListIdOfNT_Ark
                    = relationsHelper.getListNT(ds, idConcept, idThesaurus);
            nce.setNodeListOfNT(nodeListIdOfNT_Ark);

            //récupération des termes associés
            ArrayList<NodeHieraRelation> nodeListIdOfRT_Ark
                    = relationsHelper.getListRT(ds, idConcept, idThesaurus);
            nce.setNodeListIdsOfRT(nodeListIdOfRT_Ark);

            //récupération des Non Prefered Term
            nce.setNodeEM(new TermHelper().getAllNonPreferredTerms(ds, idConcept, idThesaurus));

            //récupération des traductions
            nce.setNodeTermTraductions(new TermHelper().getAllTraductionsOfConcept(ds, idConcept, idThesaurus));

            //récupération des Groupes
            ArrayList<NodeUri> nodeListIdsOfConceptGroup_Ark = getListIdArkOfGroup(ds,
                    new GroupHelper().getListIdGroupOfConcept(ds, idThesaurus, idConcept),
                    idThesaurus);
            nce.setNodeListIdsOfConceptGroup(nodeListIdsOfConceptGroup_Ark);

            //récupération des notes du Terme
            String idTerm = new TermHelper().getIdTermOfConcept(ds, idConcept, idThesaurus);
            nce.setNodeNoteTerm(new NoteHelper().getListNotesTermAllLang(ds, idTerm, idThesaurus));

            //récupération des Notes du Concept
            nce.setNodeNoteConcept(new NoteHelper().getListNotesConceptAllLang(ds, idConcept, idThesaurus));

            // récupération des Alignements
            nce.setNodeAlignmentsList(new AlignmentHelper().getAllAlignmentOfConceptNew(ds, idConcept, idThesaurus));
        }

        return listNce;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept par son id et son thésaurus et la langue
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet class NodeConcept
     */
    public NodeConcept getConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {
        NodeConcept nodeConcept = new NodeConcept();

        // récupération des BT
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<NodeBT> nodeListBT = relationsHelper.getListBT(ds, idConcept, idThesaurus, idLang);
        nodeConcept.setNodeBT(nodeListBT);

        //récupération du Concept
        Concept concept = getThisConcept(ds, idConcept, idThesaurus);
        if (concept == null) {
            return null;
        }
        nodeConcept.setConcept(concept);

        //récupération du Terme
        TermHelper termHelper = new TermHelper();
        Term term = termHelper.getThisTerm(ds, idConcept, idThesaurus, idLang);
        nodeConcept.setTerm(term);

        //récupération des termes spécifiques
        nodeConcept.setNodeNT(relationsHelper.getListNT(ds, idConcept, idThesaurus, idLang));

        //récupération des termes associés
        nodeConcept.setNodeRT(relationsHelper.getListRT(ds, idConcept, idThesaurus, idLang));

        //récupération des Non Prefered Term
        nodeConcept.setNodeEM(termHelper.getNonPreferredTerms(ds, term.getId_term(), idThesaurus, idLang));

        //récupération des traductions
        nodeConcept.setNodeTermTraductions(termHelper.getTraductionsOfConcept(ds, idConcept, idThesaurus, idLang));

        NoteHelper noteHelper = new NoteHelper();

        //récupération des notes du Concept
        nodeConcept.setNodeNotesConcept(noteHelper.getListNotesConcept(
                ds, idConcept, idThesaurus, idLang));
        //récupération des notes du term        
        nodeConcept.setNodeNotesTerm(noteHelper.getListNotesTerm(ds, term.getId_term(),
                idThesaurus, idLang));

        GroupHelper groupHelper = new GroupHelper();
        nodeConcept.setNodeConceptGroup(groupHelper.getListGroupOfConcept(ds, idThesaurus, idConcept, idLang));

        AlignmentHelper alignmentHelper = new AlignmentHelper();
        nodeConcept.setNodeAlignments(alignmentHelper.getAllAlignmentOfConcept(ds, idConcept, idThesaurus));

        GpsHelper gpsHelper = new GpsHelper();
        nodeConcept.setNodeGps(gpsHelper.getCoordinate(ds, idConcept, idThesaurus));

        ImagesHelper imagesHelper = new ImagesHelper();
        nodeConcept.setNodeimages(imagesHelper.getExternalImages(ds, idConcept, idThesaurus));

        return nodeConcept;
    }

    /**
     * Cette fonction permet de retourner l'id du Concept d'après un idTerm
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @return idConcept
     */
    public String getIdConceptOfTerm(HikariDataSource ds,
            String idTerm, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String idConcept = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_concept FROM"
                            + " preferred_term WHERE"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_term = '" + idTerm + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        idConcept = resultSet.getString("id_concept");
                    } else {
                        return null;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idConcept of idTerm : " + idConcept, sqle);
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de savoir si un concept a des fils ou non suivant
     * l'id du Concept et l'id du thésaurus sous forme de classe Concept (sans
     * les relations)
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class NodeConceptTree
     */
    public boolean haveChildren(HikariDataSource ds,
            String idThesaurus, String idConcept) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean children = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select count(*)  from hierarchical_relationship"
                            + " where id_thesaurus='" + idThesaurus + "'"
                            + " and id_concept1='" + idConcept + "'"
                            + " and role LIKE 'NT%'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        if (resultSet.getInt(1) != 0) {
                            children = true;
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
            log.error("Error while testing if haveChildren of Concept : " + idConcept, sqle);
        }
        return children;
    }

    /**
     * Focntion récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête en incluant les Groupes on
     * peut rencontrer plusieurs têtes en remontant, alors on construit à chaque
     * fois un chemin complet.
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param firstPath
     * @param path
     * @param tabId
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<ArrayList<String>> getInvertPathOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus,
            ArrayList<String> firstPath,
            ArrayList<String> path,
            ArrayList<ArrayList<String>> tabId) {

        RelationsHelper relationsHelper = new RelationsHelper();

        ArrayList<String> resultat = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if (resultat.size() > 1) {
            for (String path1 : path) {
                firstPath.add(path1);
            }
        }
        if (resultat.isEmpty()) {

            String group;

            do {
                group = getGroupIdOfConcept(ds, idConcept, idThesaurus);
                if (group == null) {
                    group = new GroupHelper().getIdFather(ds, idConcept, idThesaurus);
                }

                path.add(group);
                idConcept = group;
            } while (new GroupHelper().getIdFather(ds, group, idThesaurus) != null);

            ArrayList<String> pathTemp = new ArrayList<>();
            for (String path2 : firstPath) {
                pathTemp.add(path2);
            }
            for (String path1 : path) {
                if (pathTemp.indexOf(path1) == -1) {
                    pathTemp.add(path1);
                }
            }
            tabId.add(pathTemp);
            path.clear();
        }

        for (String resultat1 : resultat) {
            path.add(resultat1);
            getInvertPathOfConcept(ds, resultat1, idThesaurus, firstPath, path, tabId);
        }

        return tabId;

    }

    public ArrayList<ArrayList<String>> getPathOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, ArrayList<String> path, ArrayList<ArrayList<String>> tabId) {

        ArrayList<String> firstPath = new ArrayList<>();
        ArrayList<ArrayList<String>> tabIdInvert = getInvertPathOfConcept(ds, idConcept,
                idThesaurus,
                firstPath,
                path, tabId);

        for (int i = 0; i < tabIdInvert.size(); i++) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (int j = tabIdInvert.get(i).size(); j > 0; j--) {
                pathTemp.add(tabIdInvert.get(i).get(j - 1));
            }
            tabIdInvert.remove(i);
            tabIdInvert.add(i, pathTemp);
        }
        return tabIdInvert;
    }

    /**
     * Focntion récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param firstPath
     * @param path
     * @param tabId
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<ArrayList<String>> getInvertPathOfConceptWithoutGroup(HikariDataSource ds,
            String idConcept, String idThesaurus,
            ArrayList<String> firstPath,
            ArrayList<String> path,
            ArrayList<ArrayList<String>> tabId) {
        //    System.err.println("Concept = " + idConcept);
        RelationsHelper relationsHelper = new RelationsHelper();

        ArrayList<String> resultat = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if (resultat.size() > 1) {
            for (String path1 : path) {
                firstPath.add(path1);
            }
        }
        if (resultat.isEmpty()) {

            /*       String group;

            do {
                group = getGroupIdOfConcept(ds, idConcept, idThesaurus);
                if (group == null) {
                    group = new GroupHelper().getIdFather(ds, idConcept, idThesaurus);
                }

                path.add(group);
                idConcept = group;
            } while (new GroupHelper().getIdFather(ds, group, idThesaurus) != null);
             */
            ArrayList<String> pathTemp = new ArrayList<>();
            for (String path2 : firstPath) {
                pathTemp.add(path2);
            }
            for (String path1 : path) {
                if (pathTemp.indexOf(path1) == -1) {
                    pathTemp.add(path1);
                }
            }
            tabId.add(pathTemp);
            path.clear();
        }

        for (String resultat1 : resultat) {
            path.add(resultat1);
            getInvertPathOfConceptWithoutGroup(ds, resultat1, idThesaurus, firstPath, path, tabId);
        }

        return tabId;

    }

    public ArrayList<ArrayList<String>> getPathOfConceptWithoutGroup(HikariDataSource ds,
            String idConcept, String idThesaurus, ArrayList<String> path, ArrayList<ArrayList<String>> tabId) {

        ArrayList<String> firstPath = new ArrayList<>();
        ArrayList<ArrayList<String>> tabIdInvert = getInvertPathOfConceptWithoutGroup(ds, idConcept,
                idThesaurus,
                firstPath,
                path, tabId);

        for (int i = 0; i < tabIdInvert.size(); i++) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (int j = tabIdInvert.get(i).size(); j > 0; j--) {
                pathTemp.add(tabIdInvert.get(i).get(j - 1));
            }
            tabIdInvert.remove(i);
            tabIdInvert.add(i, pathTemp);
        }
        return tabIdInvert;
    }

    public void updateGroupOfConcept(HikariDataSource ds, String idConcept, String idNewDomaine, String idOldDomaine, String idTheso) {
        Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set id_group='" + idNewDomaine + "',"
                            + " modified = current_date"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'"
                            + " AND id_group ='" + idOldDomaine + "'";

                    stmt.executeUpdate(query);
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating group of concept : " + idConcept, sqle);
        }
    }

    /**
     * Cette fonction permet d'ajouter un Ark Id au concept ou remplacer l'Id
     * existant
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idArk
     * @return
     */
    public boolean updateArkIdOfConcept(HikariDataSource ds, String idConcept,
            String idTheso, String idArk) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set id_ark='" + idArk + "'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";

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
            log.error("Error while updating or adding ArkId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de modifier le status d'un concept
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param status
     */
    private boolean updateStatusConcept(HikariDataSource ds, String idConcept,
            String idTheso, String status) {
        Connection conn;
        Statement stmt;
        boolean res = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set status='" + status + "'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";

                    stmt.executeUpdate(query);
                    res = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating status of Concept : " + idConcept, sqle);
        }
        return res;
    }

    /**
     * Cette fonction permet d'ajouter un Ark Id au concept ou remplacer l'Id
     * existant
     *
     * @param conn
     * @param idConcept
     * @param idTheso
     * @param idArk
     * @return
     */
    public boolean updateArkIdOfConcept(Connection conn, String idConcept,
            String idTheso, String idArk) {

        Statement stmt;
        boolean status = false;
        try {

            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set id_ark='" + idArk + "'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";

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
            log.error("Error while updating or adding ArkId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet d'ajouter un Handle Id au concept ou remplacer l'Id
     * existant
     *
     * @param conn
     * @param idConcept
     * @param idTheso
     * @param idHandle
     * @return
     */
    public boolean updateHandleIdOfConcept(Connection conn, String idConcept,
            String idTheso, String idHandle) {

        Statement stmt;
        boolean status = false;
        try {

            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set id_handle='" + idHandle + "'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";

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
            log.error("Error while updating or adding HandleId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet d'ajouter un Handle Id au concept ou remplacer l'Id
     * existant
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idHandle
     * @return
     */
    public boolean updateHandleIdOfConcept(HikariDataSource ds, String idConcept,
            String idTheso, String idHandle) {

        Statement stmt;
        boolean status = false;
        Connection conn;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set id_handle='" + idHandle + "'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";

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
            log.error("Error while updating HandleId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de mettre à jour la notation pour un concept
     *
     * @param conn
     * @param idConcept
     * @param idTheso
     * @param notation
     * @return
     */
    public boolean updateNotation(Connection conn, String idConcept,
            String idTheso, String notation) {

        Statement stmt;
        boolean status = false;
        try {

            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept "
                            + "set notation ='" + notation + "'"
                            + " WHERE id_concept ='" + idConcept + "'"
                            + " AND id_thesaurus='" + idTheso + "'";

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
            log.error("Error while updating or adding ArkId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    public boolean haveThisGroup(HikariDataSource ds, String idConcept, String idDomaine, String idTheso) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean group = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT idconcept FROM concept_group_concept"
                            + " WHERE idthesaurus='" + idTheso + "'"
                            + " AND idconcept='" + idConcept + "'"
                            + " AND idgroup='" + idDomaine + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    resultSet.next();
                    group = (resultSet.getRow() != 0);

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while testing if haveChildren of Concept : " + idConcept, sqle);
        }
        return group;
    }

    public String getPereConcept(HikariDataSource ds, String id_theso, String id_concept) {
        String conceptPere = "";
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_concept2 FROM hierarchical_relationship"
                            + " WHERE id_thesaurus='" + id_theso + "'"
                            + " AND id_concept1='" + id_concept + "'"
                            + " AND role LIKE 'BT%'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        conceptPere = resultSet.getString("id_concept2");
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while get le pere du concept : " + id_concept, sqle);
        }
        return conceptPere;
    }

    /**
     * Change l'id d'un concept dans la table concept
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws java.sql.SQLException
     */
    public void setIdConcept(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE concept"
                    + " SET id_concept = '" + newIdConcept + "' "
                    + " WHERE id_concept = '" + idConcept + "' "
                    + " AND id_thesaurus = '" + idTheso + "' ";
            stmt.execute(query);

        } finally {
            stmt.close();
        }
    }

    /**
     * Change l'id d'un concept dans la table concept_group_concept
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws SQLException
     */
    public void setIdConceptGroupConcept(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE concept_group_concept"
                    + " SET idconcept = '" + newIdConcept + "'"
                    + " WHERE idconcept = '" + idConcept + "'"
                    + " AND idthesaurus = '" + idTheso + "'";
            stmt.execute(query);

        } finally {
            stmt.close();
        }
    }

    /**
     * Change l'id d'un concept dans la table concept_historique
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws SQLException
     */
    public void setIdConceptHistorique(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE concept_historique"
                    + " SET id_concept = '" + newIdConcept + "'"
                    + " WHERE id_concept = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);

        } finally {
            stmt.close();
        }
    }

    /**
     * Change l'id d'un concept dans la table concept_orphan
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws SQLException
     */
    public void setIdConceptOrphan(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE concept_orphan"
                    + " SET id_concept = '" + newIdConcept + "'"
                    + " WHERE id_concept = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
        } finally {
            stmt.close();
        }
    }

    /**
     * Change l'id d'un concept dans la table hierarchical_relationship
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws SQLException
     */
    public void setIdConceptHieraRelation(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE hierarchical_relationship"
                    + " SET id_concept1 = '" + newIdConcept + "'"
                    + " WHERE id_concept1 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            query += ";";
            query += "UPDATE hierarchical_relationship"
                    + " SET id_concept2 = '" + newIdConcept + "'"
                    + " WHERE id_concept2 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
        } finally {
            stmt.close();
        }
    }

    /**
     * Change l'id d'un concept dans la table
     * hierarchical_relationship_historique
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws SQLException
     */
    public void setIdConceptHieraRelationHisto(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE hierarchical_relationship_historique"
                    + " SET id_concept1 = '" + newIdConcept + "'"
                    + " WHERE id_concept1 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            query += ";";
            query += "UPDATE hierarchical_relationship_historique"
                    + " SET id_concept2 = '" + newIdConcept + "'"
                    + " WHERE id_concept2 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
        } finally {
            stmt.close();
        }
    }

    /**
     * Change l'id d'un concept dans la table concept_fusion
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws java.sql.SQLException
     */
    public void setIdConceptFusion(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE concept_fusion"
                    + " SET id_concept1 = '" + newIdConcept + "'"
                    + " WHERE id_concept1 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            query += ";";
            query += "UPDATE concept_fusion"
                    + " SET id_concept2 = '" + newIdConcept + "'"
                    + " WHERE id_concept2 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);

        } finally {
            stmt.close();
        }
    }

    /**
     * permet de supprimer un concept dans la table concept_fusion
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @return
     */
    public boolean deleteConceptFusion(Connection conn, String idTheso, String idConcept) {
        boolean status = false;
        try {
            Statement stmt;
            stmt = conn.createStatement();

            try {
                String query = "delete from concept_fusion"
                        + " WHERE id_concept1 = '" + idConcept + "'"
                        + " AND id_thesaurus = '" + idTheso + "'";
                query += ";";
                query += "delete from concept_fusion"
                        + " WHERE id_concept2 = '" + idConcept + "'"
                        + " AND id_thesaurus = '" + idTheso + "'";
                stmt.execute(query);
                status = true;

            } finally {
                stmt.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * Change l'id d'un concept dans la table preferred_term
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws java.sql.SQLException
     */
    public void setIdConceptPreferedTerm(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE preferred_term"
                    + " SET id_concept = '" + newIdConcept + "'"
                    + " WHERE id_concept = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);

        } finally {
            stmt.close();
        }
    }

    /**
     * Méthode pour récupérer une liste des identifiants BT à parti d'un
     * thesaurus et d'un concept
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @return
     */
    public ArrayList<String> getIdBtFromAConcept(Connection conn, String idTheso, String idConcept) {
        ArrayList<String> ret = new ArrayList();
        PreparedStatement stmt;
        ResultSet rs;
        String sql = "Select id_concept2 FROM hierarchical_relationship Where id_concept1=? and id_thesaurus=? and role=?";
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, idConcept);
            stmt.setString(2, idTheso);
            stmt.setString(3, "BT");
            try {
                rs = stmt.executeQuery();
                while (rs.next()) {
                    ret.add(rs.getString("id_concept2"));
                }
            } finally {
                stmt.close();
            }
        } catch (SQLException e) {
            log.error("error while getting id BT from a concept Id", e);
        }
        return ret;
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public int getNbrOfCanceptByThes(Connection conn, String idThesaurus) {
        Statement stmt;
        int nbrConcept = 0;
        try {
            stmt = conn.createStatement();
            stmt.executeQuery("SELECT count(*) FROM concept WHERE id_thesaurus = '"+idThesaurus+"' AND status != 'CA'");
            ResultSet resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                nbrConcept = resultSet.getInt("count");
            }
            stmt.close();
        } catch (SQLException sqle) {
            log.error("Error while getting List Id or Groups of thesaurus : " + idThesaurus, sqle);
        }
        return nbrConcept;
    }


    public List<ConceptStatisticData> searchAllCondidats(HikariDataSource hikariDataSource, String idThesaurus, String lang,
            String dateDebut, String dateFin, String collectionId, String nbrResultat) throws SQLException {

        List<ConceptStatisticData> temps = new ArrayList<>();

        Statement stmt = hikariDataSource.getConnection().createStatement();

        StringBuffer request = new StringBuffer()
                .append("SELECT con.id_concept, con.created, con.modified, users.username ")
                .append("FROM concept con, users ");

        if(!StringUtils.isEmpty(collectionId)) {
            request.append(", concept_group_concept con_group ");
        }

        request.append("WHERE con.status = 'D' ")
                .append("AND con.id_thesaurus = '").append(idThesaurus).append("' ");

        if(!StringUtils.isEmpty(collectionId)) {
            request.append("AND con.id_concept = con_group.idconcept ")
                    .append("AND con_group.idgroup = '").append(collectionId).append("' ");
        }

        if(!StringUtils.isEmpty(dateDebut) && !StringUtils.isEmpty(dateFin)) {
            request.append("AND con.modified BETWEEN '").append(dateDebut).append("' AND '").append(dateFin).append("' ");
        }

        request.append("ORDER BY con.id_concept ASC ");
        request.append("LIMIT " + nbrResultat);

        stmt.executeQuery(request.toString());
        ResultSet resultSet = stmt.getResultSet();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        while (resultSet.next()) {
            ConceptStatisticData concept = new ConceptStatisticData();
            concept.setIdConcept(resultSet.getString("id_concept"));
            concept.setDateCreation(formatter.format(resultSet.getDate("created")));
            concept.setDateModification(formatter.format(resultSet.getDate("modified")));
            concept.setUtilisateur(resultSet.getString("username"));
            temps.add(concept);
        }

        resultSet.close();
        stmt.close();

        return temps;
    }

}
