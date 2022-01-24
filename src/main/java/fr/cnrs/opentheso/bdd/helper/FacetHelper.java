/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeFacet;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miled.rousset
 */
public class FacetHelper {

    private final Log log = LogFactory.getLog(FacetHelper.class);

    public FacetHelper() {
    }

    
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
///////////////////// Nouvelles méthodes MR //////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////    

    public ArrayList<NodeIdValue> searchFacet(HikariDataSource ds,
                                              String name, String lang, String idThesaurus) {
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("SELECT id_facet, lexical_value FROM node_label WHERE lang = '"+ lang
                        + "' AND lexical_value like unaccent(lower('%" + name + "%'))" + " AND id_thesaurus = '" + idThesaurus + "' order by lexical_value");
                try (ResultSet resultSet = stmt.getResultSet()){
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id_facet"));
                        nodeIdValue.setValue(resultSet.getString("lexical_value"));
                        nodeIdValues.add(nodeIdValue);
                    }
                }
            }

        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while serching for facet : " + name, sqle);
        }
        return nodeIdValues;
    }

    /**
     * permet de retourner la liste des id et valeur des facettes qui 
     * appartiennent à ce concept, ceci est pour les noeuds dans l'arbre
     * si le concept est traduit, on a le label, sinon, on a une chaine vide
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return 
     * #MR
     */
    public List<NodeIdValue> getAllIdValueFacetsOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

        List<String> listIdFacets = getAllIdFacetsOfConcept(ds, idConcept, idThesaurus);
        List<NodeIdValue> nodeIdValues = new ArrayList<>();
        for (String idFacet : listIdFacets) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idFacet);
            nodeIdValue.setValue(getLabelOfFacet(ds, idFacet, idThesaurus, idLang));
            nodeIdValues.add(nodeIdValue);
        }
        return nodeIdValues;
    }
    
    /**
     * permet de retourner la liste des id facettes qui appartiennent à ce concept
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return 
     * #MR
     */
    public List<String> getAllIdFacetsOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        List<String> listIdFacets = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("SELECT thesaurus_array.id_facet" +
                        " FROM thesaurus_array " +
                        " WHERE thesaurus_array.id_thesaurus = '" + idThesaurus + "'" +
                        " AND thesaurus_array.id_concept_parent = '" + idConcept + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdFacets.add(resultSet.getString("id_facet"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All id facets of concept : " + idConcept, sqle);
        }
        return listIdFacets;
    }       
    

    /**
     * permet de retourner un NodeFacet avec l'id, le label et le conceptParent de la facette
     * si le label n'est pas traduit, il est remplacé par une chaine vide
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param lang
     * @return 
     */
    public NodeFacet getThisFacet(HikariDataSource ds, String idFacet, String idThesaurus, String lang) {
        Connection conn;
        ResultSet resultSet = null;
        NodeFacet nodeFacet = new NodeFacet();

        try {
            conn = ds.getConnection();
            try (Statement stmt = conn.createStatement()){
                try {
                        String query = "SELECT node_label.id_facet, thesaurus_array.id_concept_parent" +
                                " FROM node_label, thesaurus_array" +
                                " WHERE" +
                                " node_label.id_facet=thesaurus_array.id_facet" +
                                " and" +
                                " node_label.id_thesaurus = thesaurus_array.id_thesaurus" +
                                " and node_label.id_facet ='" + idFacet +"'" +
                                " and node_label.id_thesaurus = '" + idThesaurus + "'";                        
                        
                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        if(resultSet.next()) {
                            nodeFacet.setIdFacet(resultSet.getString("id_facet"));
                            nodeFacet.setIdThesaurus(idThesaurus);
                            nodeFacet.setIdConceptParent(resultSet.getString("id_concept_parent"));
                            nodeFacet.setLexicalValue(getLabelOfFacet(ds, idFacet, idThesaurus, lang));
                            nodeFacet.setLang(lang);
                        }
                } finally {
                    if (resultSet != null) resultSet.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Facet : " + idFacet, sqle);
        }

        return nodeFacet;
    }    
    
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
///////////////////// fin nouvelles méthodes MR //////////////////////////////
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////     
    
    
    
    
    
    
    
    /**
     * Cette fonction permet d'ajouter une nouvelle Facette 
     * si l'indentifiant est fourni, on l'ajoute, sinon, on génère un nouveau
     * 
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param idConceptParent
     * @param lexicalValue
     * @param idLang
     * @param notation
     * @return Id of Facet
     */
    public String addNewFacet(HikariDataSource ds,
            String idFacet,
            String idThesaurus, String idConceptParent,
            String lexicalValue, String idLang, String notation) {

        Connection conn;
        Statement stmt;
        StringPlus stringPlus = new StringPlus();
        lexicalValue = stringPlus.convertString(lexicalValue);
        
        if(idFacet == null || idFacet.isEmpty()) {
            idFacet = getNewId(ds);
            // si le nouveau Id existe, on l'incrémente
            while (isIdFacetExist(ds, idFacet)) {
                idFacet = getNewId(ds);
            }            
        }
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeUpdate("INSERT INTO node_label(id_facet, id_thesaurus, lexical_value, created, modified, lang) VALUES ("
                            + "'" + idFacet + "', "
                            + "'" + idThesaurus + "', '"+lexicalValue+"', now(), now(), '"+idLang+"');");

                    stmt.executeUpdate("INSERT INTO thesaurus_array(id_thesaurus, id_concept_parent, id_facet) VALUES ('"+
                            idThesaurus+"', '"+idConceptParent+"', '"+idFacet+"');");
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getMessage().contains("duplicate key value violates unique constraint")) {
                log.error("Error while adding Concept to Facet");
                idFacet = null;
            }
        }
        return idFacet;
    }
    
    private String getNewId(HikariDataSource ds) {
        String idFacet = null;
        ResultSet resultSet = null;
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select nextval('thesaurus_array_facet_id_seq') from thesaurus_array_facet_id_seq");
                resultSet = stmt.getResultSet();
                if (resultSet.next()) {
                    int idNumerique = resultSet.getInt(1);
                    idFacet = "F" + (idNumerique);
                }
            } finally {
                if (resultSet != null) resultSet.close();
                conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return idFacet;
    }    
    
    /**
     * Cette fonction permet d'ajouter un concept dans une Facette 
     * 
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param idConcept
     * @return 
     */
    public boolean addConceptToFacet(HikariDataSource ds,
            String idFacet,
            String idThesaurus, String idConcept) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeUpdate("INSERT INTO concept_facet(id_facet, id_thesaurus, id_concept) VALUES ("
                            + "'" + idFacet +"', '"+idThesaurus+"', '"+idConcept+"');");
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getMessage().contains("duplicate key value violates unique constraint")) {
                log.error("Error while adding Concept to Facet : " + idFacet, sqle);
            }
        }
        return status;
    }    
    
    /**
     *  Cette fonction permet de rajouter une traduction à une facette existante.
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param lexicalValue 
     * @param idLang 
     * @return  
     */
    
    public boolean addFacetTraduction(HikariDataSource ds,
            String idFacet,
            String idThesaurus,
            String lexicalValue, String idLang) {
        Connection conn;
        Statement stmt;
        boolean status = false;

        lexicalValue = new StringPlus().convertString(lexicalValue);
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into node_label "
                            + "(id_facet, id_thesaurus, lexical_value, lang)"
                            + " values ("
                            + "'" + idFacet + "'"
                            + ",'" + idThesaurus + "'"
                            + ",'" + lexicalValue + "'"
                            + ",'" + idLang + "')";

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
            if (!sqle.getMessage().contains("duplicate key value violates unique constraint")) {
                log.error("Error while adding traduction of Facet : " + idFacet, sqle);
            }
        }
        return status;
    }
 
    /**
     * Cette fonction permet de mettre à jour une facette
     *
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param idLang
     * @param lexicalValue
     * @return
     */
    public boolean updateFacetTraduction(HikariDataSource ds,
            String idFacet,
            String idThesaurus,
            String idLang,
            String lexicalValue) {

        Connection conn;
        Statement stmt;
        boolean status = false;
        lexicalValue = new StringPlus().convertString(lexicalValue);
        
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeUpdate("UPDATE node_label set"
                            + " lexical_value = '" + lexicalValue + "'"
                            + " WHERE id_facet = '" + idFacet + "'"
                            + " AND id_thesaurus = '" + idThesaurus + "'"
                            + " AND lang = '" + idLang + "'");
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating Facet Traduction of FacetId: " + idFacet, sqle);
        }
        return status;
    }
    
    /**
     * Cette fonction permet de savoir s'il a une traduction dans cette langue 
     * 
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param idLang
     * @return Objet class NodeConceptTree
     */
    public boolean isTraductionExistOfFacet(HikariDataSource ds,
            String idFacet, String idThesaurus, String idLang) {

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
                    String query = "select id from node_label"
                            + " where"
                            + " id_facet = '" + idFacet + "'"
                            + " and lang = '" + idLang + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        resultSet.next();
                        if (resultSet.getRow() == 0) {
                            existe = false;
                        } else {
                            existe = true;
                        }
                    }

                } finally {
                    if (resultSet != null) resultSet.close();
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if Traduction of Facet exist : " + idFacet, sqle);
        }
        return existe;
    }    

    
    
    /**
     * permet de retourner le label de la facette, si la facette n'est pas traduite, elle retourne chaine vide
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param lang
     * @return 
     */
    public String getLabelOfFacet(HikariDataSource ds, String idFacet, 
            String idThesaurus, String lang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        String label = "";

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT lexical_value FROM node_label WHERE id_facet = '" + idFacet + "' AND id_thesaurus = '" + idThesaurus + "' AND lang = '"+lang+"'");
                    resultSet = stmt.getResultSet();
                    if(resultSet.next()) {
                        label = resultSet.getString("lexical_value");
                    }
                } finally {
                    if (resultSet != null) resultSet.close();
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting label of Facet : " + idFacet, sqle);
        }
        return label;
    }    
    
    public List<NodeFacet> getAllTraductionsFacet(HikariDataSource ds, String idFacet, 
            String idThesaurus, String lang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        List<NodeFacet> facetLists = new ArrayList();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT * FROM node_label WHERE id_facet = '" + idFacet + "' AND id_thesaurus = '" + idThesaurus + "' AND lang != '"+lang+"'");
                    resultSet = stmt.getResultSet();
                    while(resultSet.next()) {
                        NodeFacet facet = new NodeFacet();
                        facet.setIdFacet(resultSet.getString("id_facet"));
                        facet.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        facet.setLexicalValue(resultSet.getString("lexical_value"));
                        facet.setLang(resultSet.getString("lang"));
                        facetLists.add(facet);
                    }
                } finally {
                    if (resultSet != null) resultSet.close();
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Facet : " + idFacet, sqle);
        }

        return facetLists;
    }
    
    /**
     * Cette fonction permet de supprimer une Facette avec ses relations
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @return 
     */
    public boolean deleteFacet(HikariDataSource ds, String idFacet, String idThesaurus){
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from thesaurus_array where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_facet  = '" + idFacet + "'";
                    stmt.executeUpdate(query);
                    
                    query = "delete from concept_facet where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_facet  = '" + idFacet + "'";
                    stmt.executeUpdate(query);
                    
                    query = "delete from node_label where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_facet = '" + idFacet + "'";
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
            log.error("Error while deleting Facet : " + idFacet, sqle);
        }
        
        return status;
    }
    
    /**
     * Cette fonction permet de supprimer une traduction à une Facette
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param idLang
     * @return 
     */
    public boolean deleteTraductionFacet(HikariDataSource ds, String idFacet, String idThesaurus, 
            String idLang){
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from node_label where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and lang = '" + idLang + "'"
                            + " and id_facet = '" + idFacet + "'";
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
            log.error("Error while deleting Traduction of Facet : " + idFacet, sqle);
        }
        
        return status;
    }    
    
    /**
     * Cette fonction permet de supprimer un concept de la Facette
     * @param ds
     * @param idFacet
     * @param idConcept
     * @param idThesaurus
     * @return 
     */
    public boolean deleteConceptFromFacet(HikariDataSource ds,
            String idFacet, String idConcept, String idThesaurus){
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_facet where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'"
                            + " and id_facet  = '" + idFacet + "'";
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
            log.error("Error while deleting Concept from Facet : " + idFacet, sqle);
        }
        
        return status;
    }
    
    /**
     * Cette fonction permet de récupérer les Id des Concepts Parents qui continennent des Facettes
     *
     * @param ds
     * @param idThesaurus
     * @param lang
     * @return ArrayList of IdConcepts
     */
    public ArrayList<NodeConceptTree> getIdParentOfFacet(HikariDataSource ds, String idThesaurus, String lang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> listIdC = new ArrayList<>();
        ArrayList<NodeConceptTree> tabIdConcept = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT DISTINCT id_concept_parent"
                            + " FROM thesaurus_array WHERE"
                            + " id_thesaurus = '" + idThesaurus + "'"; 

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    tabIdConcept = new ArrayList<>();
                    while (resultSet.next()) {
                        listIdC.add(resultSet.getString("id_concept_parent"));
                    }
                    for (String idC : listIdC) {
                        query = "SELECT term.lexical_value FROM term, preferred_term" 
                                + " WHERE preferred_term.id_term = term.id_term"
                                + " and preferred_term.id_concept ='" + idC +"'"
                                + " and term.lang = '" + lang + "'"
                                + " and term.id_thesaurus = '" + idThesaurus + "'"
                                + " order by lexical_value DESC";                        
                        
                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        resultSet.next();
                        NodeConceptTree nct = new NodeConceptTree();
                        nct.setIdConcept(idC);
                        nct.setIdLang(lang);
                        nct.setIdThesaurus(idThesaurus);
                        if (resultSet.getRow() == 0) {
                            nct.setTitle("");
                        } else {
                            nct.setTitle(resultSet.getString("lexical_value"));
                        }
                        tabIdConcept.add(nct);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Facet of Thesaurus : " + idThesaurus, sqle);
        }

        return tabIdConcept;
    }
    
   
    /**
     * Cette fonction permet de retourner toutes les Facettes d'un thésaurus
     * sous forme de NodeFacet
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @return ArrayList de NodeFacet
     */
    public ArrayList<NodeFacet> getAllFacetsOfThesaurus(HikariDataSource ds,
            String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        ArrayList<NodeFacet> nodeFacetlist = new ArrayList();
        
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT node_label.lexical_value,"
                            + " node_label.id_facet, thesaurus_array.id_concept_parent FROM "
                            + " thesaurus_array, node_label WHERE"
                            + " thesaurus_array.id_facet = node_label.id_facet AND"
                            + " thesaurus_array.id_thesaurus = node_label.id_thesaurus"
                            + " and node_label.id_thesaurus = '" + idThesaurus + "'";
                    
                    if (idLang != null) {
                        query = query + " and node_label.lang = '" + idLang + "'";
                    }
                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()){
                        NodeFacet nodeFacet = new NodeFacet();
                        nodeFacet.setIdFacet(resultSet.getString("id_facet"));
                        nodeFacet.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeFacet.setIdConceptParent(resultSet.getString("id_concept_parent"));
                        nodeFacetlist.add(nodeFacet);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All Facet of Thesaurus : " + idThesaurus, sqle);
        }
        return nodeFacetlist;
    }  
    
    /**
     * Cette fonction permet de retourner toutes les Facettes d'un thésaurus
     * sous forme de NodeFacet
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList de NodeFacet
     */
    public ArrayList<NodeFacet> getAllFacetsDetailsOfThesaurus(HikariDataSource ds, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        ArrayList<NodeFacet> nodeFacetlist = new ArrayList();
        ConceptHelper conceptHelper = new ConceptHelper();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    /*stmt.executeQuery("SELECT * FROM node_label WHERE node_label.id_thesaurus = '"+
                            idThesaurus+"'");*/
                    stmt.executeQuery("SELECT node_label.*, thesaurus_array.id_concept_parent FROM node_label, thesaurus_array " +
                            " WHERE" +
                            " node_label.id_thesaurus = thesaurus_array.id_thesaurus" +
                            " and" +
                            " node_label.id_facet = thesaurus_array.id_facet" +
                            " and" +
                            " node_label.id_thesaurus = '" + idThesaurus + "'");
                    
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()){
                        NodeFacet nodeFacet = new NodeFacet();
                        nodeFacet.setIdConceptParent(resultSet.getString("id_concept_parent"));
                        nodeFacet.setIdFacet(resultSet.getString("id_facet"));
                        nodeFacet.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeFacet.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeFacet.setCreated(resultSet.getString("created"));
                        nodeFacet.setModified(resultSet.getString("modified"));
                        nodeFacet.setLang(resultSet.getString("lang"));
                        
                        //infos pour la constructions des Uris
                        NodeUri nodeUri = new NodeUri();
                        nodeUri.setIdArk(conceptHelper.getIdArkOfConcept(ds, nodeFacet.getIdConceptParent(), idThesaurus));
                        nodeUri.setIdHandle(conceptHelper.getIdHandleOfConcept(ds, nodeFacet.getIdConceptParent(), idThesaurus));
                        nodeUri.setIdConcept(nodeFacet.getIdConceptParent());
                        nodeFacet.setNodeUri(nodeUri);
                        
                        nodeFacetlist.add(nodeFacet);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All Facet of Thesaurus : " + idThesaurus, sqle);
        }
        return nodeFacetlist;
    }

    public boolean checkExistanceFacetByNameAndLangAndThesau(HikariDataSource ds, 
            String name, String lang, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean isFound = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT id_facet FROM node_label WHERE lang = '"+ lang 
                            + "' AND lexical_value = '" + name + "' AND id_thesaurus = '" + idThesaurus + "'");
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        isFound = true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All facettes names associeted to concept : " + idThesaurus, sqle);
        }
        return isFound;
    }
    
    /**
     *  déprécié par Miled
     * permet de retourner la liste des facettes qui appartiennent à ce concept
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param lang
     * @return 
     */
/*    public List<NodeFacet> getAllFacetsOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, String lang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeFacet> nodeFacets = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    
                    String query = "SELECT lexical_value, node.id_facet "
                            + "FROM node_label node, thesaurus_array the "
                            + "WHERE node.id_thesaurus = '" + idThesaurus + "' "
                            + "AND node.lang = '"+ lang +"' "
                            + "AND node.id_facet = the.id_facet "
                            + "AND the.id_concept_parent = '"+ idConcept +"'";
                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeFacet nodeFacet = new NodeFacet();
                        nodeFacet.setIdFacet(resultSet.getString("id_facet"));
                        nodeFacet.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeFacets.add(nodeFacet);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All facettes names associeted to concept : " + idThesaurus, sqle);
        }
        return nodeFacets;
    }
    */
  
    
    /**
     * permet de retourner la liste des concepts membres de la facette triés
     * @param ds
     * @param idFacet
     * @param idLang
     * @param idTheso
     * @return
     * #MR
     */
    public List<String> getAllMembersOfFacetSorted(HikariDataSource ds,
            String idFacet, String idLang, String idTheso){
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<String> members = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT concept_facet.id_concept FROM concept_facet, preferred_term, term " +
                            " WHERE " +
                            " concept_facet.id_concept = preferred_term.id_concept" +
                            " and" +
                            " concept_facet.id_thesaurus = preferred_term.id_thesaurus" +
                            " and" +
                            " term.id_term = preferred_term.id_term" +
                            " and" +
                            " term.id_thesaurus = preferred_term.id_thesaurus" +
                            " and" +
                            " concept_facet.id_thesaurus = '" + idTheso + "'" +
                            " and" +
                            " concept_facet.id_facet = '" + idFacet + "'" +
                            " and" +
                            " term.lang = '" + idLang + "'" +
                            " order by term.lexical_value");
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        members.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting concepts members of Facet : " + idFacet, sqle);
        }
        return members;
    }

    
    /**
     * permet de retourner la liste des concepts membres de la facette
     * @param ds
     * @param idFacet
     * @param idTheso
     * @return 
     * #MR
     */
    public List<String> getAllMembersOfFacet(HikariDataSource ds,
            String idFacet, String idTheso){
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<String> members = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT concept_facet.id_concept " +
                                " FROM concept_facet " +
                                " WHERE" +
                                " concept_facet.id_thesaurus = '" + idTheso + "'" +
                                " and" +
                                " concept_facet.id_facet = '" + idFacet + "'");
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        members.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting concepts members of Facet : " + idFacet, sqle);
        }
        return members;        
    }
    
    /* déprécié par Miled
    public List<String> getConceptAssocietedToFacette(HikariDataSource ds,
            String facetteName, String idThesaurus, String lang, String idConceptParent) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> listConceptsTerme = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT fac.id_concept "
                            + "FROM node_label node, concept_facet fac "
                            + "WHERE node.id_facet = fac.id_facet "
                            + "AND node.lang = '"+lang+"' "
                            + "AND node.id_thesaurus = '"+idThesaurus+"' "
                            + "AND node.lexical_value = '" + facetteName + "' "
                            + "AND fac.id_concept != '" + idConceptParent + "' ");
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        listConceptsTerme.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting concepts names associeted to facette : " + idThesaurus, sqle);
        }
        return listConceptsTerme;
    }*/

    public boolean isConceptParentInFacet(HikariDataSource ds, String idFacet, String idConcept) {
        boolean isExist = false;
        try {
            Connection conn = ds.getConnection();
            try {
                Statement stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT id_facet FROM thesaurus_array WHERE id_concept_parent = '"+
                            idConcept+"' AND id_facet = '" + idFacet + "'");
                    ResultSet resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        isExist = true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {}
        return isExist;
    }
    
    public boolean updateLabelFacet(HikariDataSource ds, String newLabel, String idFacet, String idThes, String lang) {
        boolean status = false;
        try {
            Connection conn = ds.getConnection();
            try {
                Statement stmt = conn.createStatement();
                try {
                    stmt.executeUpdate("UPDATE node_label SET lexical_value='"+newLabel+"' WHERE id_facet = '"
                            +idFacet+"' AND lang = '"+lang+"' AND id_thesaurus = '"+idThes+"'");
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            System.out.println(sqle.getMessage());
        }
        return status;
    }

    public void updateFacetParent(HikariDataSource ds, String idConceptParent, String idFacet, String idThes) {
        try {
            Connection conn = ds.getConnection();
            try {
                Statement stmt = conn.createStatement();
                try {
                    stmt.executeUpdate("UPDATE thesaurus_array SET id_concept_parent = '"+idConceptParent+"' WHERE id_facet='"+idFacet+"' AND id_thesaurus='"+idThes+"'");
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {}
    }
    
    public void deleteAllConceptAssocietedToFacet(HikariDataSource ds, String idFacet, String idThes) {
        try {
            Connection conn = ds.getConnection();
            try {
                Statement stmt = conn.createStatement();
                try {
                    stmt.executeUpdate("DELETE FROM concept_facet WHERE id_facet = '" + idFacet + "' AND id_thesaurus = '" + idThes + "'");
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {}
    }
    
    /**
     * Permet de savoir si la facette a ce concept en particulier 
     * @param ds
     * @param idFacet
     * @param idCocnept
     * @param idTheso
     * @return 
     */
    public boolean isFacetHaveThisMember(HikariDataSource ds,
            String idFacet, String idCocnept, String idTheso) {
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept_facet where "
                            + " id_thesaurus = '" + idTheso + "'"
                            + " and id_facet ='" + idFacet + "'"
                            + " and id_concept = '" + idCocnept + "'";
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
            log.error("Error while asking if Facet have this member : " + idFacet + ":" + idCocnept, sqle);
        }
        return existe;        
    }     
    
    /**
     * Permet de savoir si la facette a des concepts membres 
     * @param ds
     * @param idFacet
     * @return 
     */
    public boolean isIdFacetExist(HikariDataSource ds, String idFacet) {
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_facet from node_label where "
                            + " id_facet ='" + idFacet + "'";
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
            log.error("Error while asking if idFacet exist : " + idFacet, sqle);
        }
        return existe;        
    }        
    
    /**
     * Permet de savoir si la facette a des concepts membres 
     * @param ds
     * @param idFacet
     * @param idTheso
     * @return 
     */
    public boolean isFacetHaveMembers(HikariDataSource ds, String idFacet, String idTheso) {
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept_facet where "
                            + " id_thesaurus = '" + idTheso + "'"
                            + " and id_facet ='" + idFacet + "'";
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
            log.error("Error while asking if Facet have members : " + idFacet, sqle);
        }
        return existe;        
    }    
    
    /**
     * Permet de savoir si le concept a des facettes 
     * @param ds
     * @param idConcept
     * @param idTheso
     * @return 
     */
    public boolean isConceptHaveFacet(HikariDataSource ds, String idConcept, String idTheso) {
        boolean existe = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select count(id_facet) from thesaurus_array where "
                            + " id_thesaurus = '" + idTheso + "'"
                            + " and id_concept_parent ='" + idConcept + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if (resultSet.getInt(1) != 0) {
                            existe = resultSet.getRow() != 0;
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Concept have Facets : " + idConcept, sqle);
        }        
        return existe;        
    }

    /**
     * permet de retourner la liste des concepts membres de la facette triés
     * @param ds
     * @param idFacet
     * @param idLang
     * @param idTheso
     * @return
     * #MR
     */
    public List<String> getAllMembersOfFacetSorted(HikariDataSource ds,
                                                   String idFacet, String idLang, String idTheso){
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<String> members = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT concept_facet.id_concept FROM concept_facet, preferred_term, term " +
                            " WHERE " +
                            " concept_facet.id_concept = preferred_term.id_concept" +
                            " and" +
                            " concept_facet.id_thesaurus = preferred_term.id_thesaurus" +
                            " and" +
                            " term.id_term = preferred_term.id_term" +
                            " and" +
                            " term.id_thesaurus = preferred_term.id_thesaurus" +
                            " and" +
                            " concept_facet.id_thesaurus = '" + idTheso + "'" +
                            " and" +
                            " concept_facet.id_facet = '" + idFacet + "'" +
                            " and" +
                            " term.lang = '" + idLang + "'" +
                            " order by term.lexical_value");
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        members.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting concepts members of Facet : " + idFacet, sqle);
        }
        return members;
    }
}
