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

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    public FacetHelper() {
    }

    /**
     * Cette focntion permet d'ajouter une nouvelle Facette 
     * 
     * @param ds
     * @param idThesaurus
     * @param idConceptParent
     * @param lexicalValue
     * @param idLang
     * @param notation
     * @return Id of Facet
     */
    public int addNewFacet(HikariDataSource ds,
            String idThesaurus, String idConceptParent,
            String lexicalValue, String idLang, String notation) {

        int idFacet = -1;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        lexicalValue = new StringPlus().convertString(lexicalValue);
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select max(facet_id) from thesaurus_array where"
                            + " id_thesaurus='" + idThesaurus +"'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    resultSet.next();
                    idFacet = resultSet.getInt(1);
                    idFacet = idFacet + 1;
                    
                    query = "Insert into thesaurus_array "
                            + "(facet_id, id_thesaurus, id_concept_parent, "
                            + " notation)"
                            + " values ("
                            + idFacet
                            + ",'" + idThesaurus + "'"
                            + ",'" + idConceptParent + "'"
                            + ",'" + notation + "')";

                    stmt.executeUpdate(query);
                    
                    addFacetTraduction(ds, idFacet, idThesaurus, lexicalValue, idLang);

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getMessage().contains("duplicate key value violates unique constraint")) {
                log.error("Error while adding Facet with value : " + lexicalValue, sqle);
            }
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
            int idFacet,
            String idThesaurus, String idConcept) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean status = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into thesaurus_array_concept "
                            + "(thesaurusarrayid, id_concept, id_thesaurus)"
                            + " values ("
                            + idFacet
                            + ",'" + idConcept + "'"
                            + ",'" + idThesaurus + "')";

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
                log.error("Error while adding Concept to Facet : " + idFacet, sqle);
            }
        }
        return status;
    }    
    
    /**
     *  Cette fonction permet de rajouter une traduction à une facet existante.
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param lexicalValue 
     * @param idLang 
     * @return  
     */
    
    public boolean addFacetTraduction(HikariDataSource ds,
            int idFacet,
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
                            + "(facet_id, id_thesaurus, lexical_value, lang)"
                            + " values ("
                            + idFacet
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
            int idFacet,
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
                    
                    System.out.println(">> " + "UPDATE node_label set"
                            + " lexical_value = '" + lexicalValue + "'"
                            + " WHERE facet_id = " + idFacet
                            + " AND id_thesaurus = '" + idThesaurus + "'"
                            + " AND lang = '" + idLang + "'");
                    
                    stmt.executeUpdate("UPDATE node_label set"
                            + " lexical_value = '" + lexicalValue + "'"
                            + " WHERE facet_id = " + idFacet
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
            int idFacet, String idThesaurus, String idLang) {

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
                    String query = "select id from node_label"
                            + " where"
                            + " facet_id = " + idFacet
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

    
    public NodeFacet getThisFacet(HikariDataSource ds, int idFacet, String idThesaurus, String lang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        NodeFacet nf = new NodeFacet();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                        String query = "SELECT node_label.facet_id, node_label.lang, node_label.lexical_value, thesaurus_array.id_concept_parent FROM node_label, thesaurus_array"
                                + " WHERE node_label.facet_id=thesaurus_array.facet_id"
                                + " and node_label.facet_id ='" + idFacet +"'"
                                + " and node_label.lang = '" + lang + "'"
                                + " and node_label.id_thesaurus = '" + idThesaurus + "'"
                                + " order by node_label.lexical_value DESC";                        
                        
                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        while(resultSet.next()) {
                            nf.setIdFacet(resultSet.getInt("facet_id"));
                            nf.setIdConceptParent(resultSet.getString("id_concept_parent"));
                            nf.setLexicalValue(resultSet.getString("lexical_value"));
                            nf.setLang(resultSet.getString("lang"));
                        }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Facet : " + idFacet, sqle);
        }

        return nf;
    }
    
    public List<NodeFacet> getAllTraductionsFacet(HikariDataSource ds, int idFacet, 
            String idThesaurus, String lang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<NodeFacet> facetLists = new ArrayList();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT * FROM node_label WHERE facet_id = " + idFacet + " AND id_thesaurus = '" + idThesaurus + "' AND lang != '"+lang+"'");
                    resultSet = stmt.getResultSet();
                    while(resultSet.next()) {
                        NodeFacet facet = new NodeFacet();
                        facet.setIdFacet(resultSet.getInt("facet_id"));
                        facet.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        facet.setLexicalValue(resultSet.getString("lexical_value"));
                        facet.setLang(resultSet.getString("lang"));
                        facetLists.add(facet);
                    }
                } finally {
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
     * Cette fonction permet de récupérer les Id des Concepts regroupés dans cette Facette
     *
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @return ArrayList of IdConcepts
     */
    public ArrayList<String> getIdConceptsOfFacet(HikariDataSource ds,
            int idFacet, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabIdConcept = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_concept"
                            + " FROM thesaurus_array_concept WHERE"
                            + " thesaurusarrayid = " + idFacet 
                            + " and id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    tabIdConcept = new ArrayList<>();
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
            log.error("Error while getting IdConcepts of Facet : " + idFacet, sqle);
        }

        return tabIdConcept;
    }
    
    /**
     * Cette fonction permet de supprimer une Facette avec ses relations
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @return 
     */
    public boolean deleteFacet(HikariDataSource ds, int idFacet, String idThesaurus){
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
                            + " and facet_id  = " + idFacet;
                    stmt.executeUpdate(query);
                    
                    query = "delete from thesaurus_array_concept where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and thesaurusarrayid  = " + idFacet;
                    stmt.executeUpdate(query);
                    
                    query = "delete from node_label where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and facet_id = " + idFacet;
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
    public boolean deleteTraductionFacet(HikariDataSource ds, int idFacet, String idThesaurus, 
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
                            + " and facet_id = " + idFacet;
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
            int idFacet, String idConcept, String idThesaurus){
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from thesaurus_array_concept where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'"
                            + " and thesaurusarrayid  = " + idFacet;
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
     * Cette fonction permet de retourner la liste des Id des Facettes qui contiennent un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return ArrayList of Id Facet (int)
     */
    public ArrayList<Integer> getIdFacetOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        ArrayList<Integer> listIdFacet = new ArrayList();
        
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select thesaurusarrayid from thesaurus_array_concept"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()){
                        listIdFacet.add(resultSet.getInt("thesaurusarrayid"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Ids of Facet for Concept : " + idConcept, sqle);
        }
        return listIdFacet;
    }
    
    /**
     * Cette fonction permet de retourner la liste des Id des Facettes rangées sous un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return ArrayList of Id Facet (int)
     */
    public ArrayList<Integer> getIdFacetUnderConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        ArrayList<Integer> listIdFacet = new ArrayList();
        
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select facet_id from thesaurus_array"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept_parent = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()){
                        listIdFacet.add(resultSet.getInt("facet_id"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Ids of Facet for Concept : " + idConcept, sqle);
        }
        return listIdFacet;
    }
    
    /**
     * Cette fonction permet de retourner le concept paretn d'une facette
     *
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param lang
     * @return ArrayList of Id Facet (int)
     */
    public NodeConceptTree getConceptOnFacet(HikariDataSource ds,
            int idFacet, String idThesaurus, String lang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        NodeConceptTree nct =  new NodeConceptTree();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select thesaurus_array.id_concept_parent, term.lexical_value "
                            + "from thesaurus_array, term, preferred_term"
                            + " where thesaurus_array.id_concept_parent=preferred_term.id_concept"
                            + " and preferred_term.id_term=term.id_term"
                            + " and thesaurus_array.id_thesaurus=term.id_thesaurus"
                            + " and term.lang='" + lang.trim() + "'"
                            + " and thesaurus_array.id_thesaurus = '" + idThesaurus + "'"
                            + " and facet_id = '" + idFacet + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if(resultSet.next()) {
                       
                        nct.setHaveChildren(true);
                        nct.setIdLang(lang);
                        nct.setIdThesaurus(idThesaurus);
                        nct.setIdConcept(resultSet.getString("id_concept_parent"));
                        nct.setTitle(resultSet.getString("lexical_value"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Concept of Facet : " + idFacet, sqle);
        }
        return nct;
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
                            + " node_label.facet_id, thesaurus_array.id_concept_parent FROM "
                            + " thesaurus_array, node_label WHERE"
                            + " thesaurus_array.facet_id = node_label.facet_id AND"
                            + " thesaurus_array.id_thesaurus = node_label.id_thesaurus"
                            + " and node_label.id_thesaurus = '" + idThesaurus + "'";
                    
                    if (idLang != null) {
                        query = query + " and node_label.lang = '" + idLang + "'";
                    }
                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()){
                        NodeFacet nodeFacet = new NodeFacet();
                        nodeFacet.setIdFacet(resultSet.getInt("facet_id"));
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
     * @param idLang
     * @return ArrayList de NodeFacet
     */
    public ArrayList<NodeFacet> getAllFacetsDetailsOfThesaurus(HikariDataSource ds,
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
                    stmt.executeQuery("SELECT * FROM  node_label WHERE node_label.id_thesaurus = '"+
                            idThesaurus+"'");
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()){
                        NodeFacet nodeFacet = new NodeFacet();
                        nodeFacet.setIdFacet(resultSet.getInt("facet_id"));
                        nodeFacet.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeFacet.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeFacet.setCreated(resultSet.getString("created"));
                        nodeFacet.setModified(resultSet.getString("modified"));
                        nodeFacet.setLang(resultSet.getString("lang"));
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


    public List<String> getIdFacetsAssociatedToConceptParent(HikariDataSource ds, String idConcepte, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> listFacettes = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT id_facette FROM concept_facette WHERE id_thesaurus = '"
                            +idThesaurus+"' AND id_concept = '"+idConcepte+"'");
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        listFacettes.add(resultSet.getString("id_facette"));
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
        return listFacettes;
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
                    stmt.executeQuery("SELECT facet_id FROM node_label WHERE lang = '"+ lang 
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
    
    public List<NodeFacet> getFacettesAssociatedToConceptParent(HikariDataSource ds,
            String idConcepte, String idThesaurus, String lang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeFacet> listFacettes = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT lexical_value, node.facet_id "
                            + "FROM node_label node, thesaurus_array the "
                            + "WHERE node.id_thesaurus = '" + idThesaurus + "' "
                            + "AND node.lang = '"+ lang +"' "
                            + "AND node.facet_id = the.facet_id "
                            + "AND the.id_concept_parent = '"+ idConcepte +"'";
                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeFacet nodeFacet = new NodeFacet();
                        nodeFacet.setIdFacet(resultSet.getInt("facet_id"));
                        nodeFacet.setLexicalValue(resultSet.getString("lexical_value"));
                        listFacettes.add(nodeFacet);
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
        return listFacettes;
    }
    
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
                            + "FROM node_label node, concept_facette fac "
                            + "WHERE node.facet_id = fac.id_facette "
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
    }

    public boolean isConceptParentInFacet(HikariDataSource ds, String idFacet, String idConcept) {
        boolean isExist = false;
        try {
            Connection conn = ds.getConnection();
            try {
                Statement stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT facet_id FROM thesaurus_array WHERE id_concept_parent = '"+
                            idConcept+"' AND facet_id = " + idFacet);
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
    
    public void updateLabelFacet(HikariDataSource ds, String newLabel, int idFacet, String idThes, String lang) {
        try {
            Connection conn = ds.getConnection();
            try {
                Statement stmt = conn.createStatement();
                try {
                    stmt.executeUpdate("UPDATE node_label SET lexical_value='"+newLabel+"' WHERE facet_id = "
                            +idFacet+" AND lang = '"+lang+"' AND id_thesaurus = '"+idThes+"'");
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {}
    }

    public void updateFacetParent(HikariDataSource ds, String idConceptParent, int idFacet, String idThes) {
        try {
            Connection conn = ds.getConnection();
            try {
                Statement stmt = conn.createStatement();
                try {
                    stmt.executeUpdate("UPDATE thesaurus_array SET id_concept_parent = '"+idConceptParent+"' WHERE facet_id="+idFacet+" AND id_thesaurus='"+idThes+"'");
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {}
    }
    
    public void deleteAllConceptAssocietedToFacet(HikariDataSource ds, int idFacet, String idThes) {
        try {
            Connection conn = ds.getConnection();
            try {
                Statement stmt = conn.createStatement();
                try {
                    stmt.executeUpdate("DELETE FROM concept_facette WHERE id_facette = " + idFacet + " AND id_thesaurus = '" + idThes + "'");
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {}
    }
    
    
}
