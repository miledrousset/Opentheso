package fr.cnrs.opentheso.repositories;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import fr.cnrs.opentheso.models.facets.NodeFacet;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.concept.NodeUri;

import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class FacetHelper {
    
    /**
     * permet de savoir si le Parent de la Facette est dans ces collections
     * @param ds
     * @param idTheso
     * @param idFacet
     * @param groups
     * @return 
     */
    public boolean isFacetInGroups(HikariDataSource ds, 
            String idTheso, String idFacet, List<String> groups){
        String existId = null;
        
        if(groups == null || groups.isEmpty()) return false;
        
        String requestGroup = null;//"(";
        for (String idGroup : groups) {
            if(StringUtils.isEmpty(requestGroup)){
                requestGroup = "'" + idGroup.toLowerCase() + "'";
            } else {
                requestGroup = requestGroup + ", '" + idGroup.toLowerCase() + "'";
            }
        }
        
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept_parent from thesaurus_array " +
                        " where" +
                        " id_facet = '" + idFacet + "'" +
                        " and" +
                        " id_thesaurus = '" + idTheso + "'" +
                        " and " +
                        " id_concept_parent in " +
                        " (select idconcept from concept_group_concept" +
                        "  where idthesaurus = '" + idTheso + "' " + 
                        " and " +
                        " lower(idgroup) in (" + requestGroup + ")" + // (lower('g27'), lower('g26'), lower('g34'))" +
                        " )");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existId = resultSet.getString("id_concept_parent");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if facet is in collection : " + groups.toString(), sqle);
        }
        return existId != null;
    }
    
    /**
     * permet de mettre à jour la date du concept quand il y a une modification
     *
     * @param ds
     * @param idTheso
     * @param idFacet
     * @param contributor
     */
    public void updateDateOfFacet(HikariDataSource ds, String idTheso, String idFacet, int contributor) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE thesaurus_array set modified = current_date, contributor = " + contributor + " WHERE id_facet ='" + idFacet + "'"
                        + " AND id_thesaurus='" + idTheso + "'");
            }
        } catch (SQLException sqle) {
            log.error("Error while updating date of facet : " + idFacet, sqle);
        }
    }    
    
    
    public ArrayList<NodeIdValue> searchFacet(HikariDataSource ds,
            String name, String lang, String idThesaurus) {
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        
        name = fr.cnrs.opentheso.utils.StringUtils.convertString(name);
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_facet, lexical_value FROM node_label WHERE lang = '" + lang
                        + "' AND unaccent(lower(lexical_value)) like unaccent(lower('%" + name + "%'))" + " AND id_thesaurus = '" + idThesaurus + "' order by lexical_value");
                try ( ResultSet resultSet = stmt.getResultSet()) {
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
     * appartiennent à ce concept, ceci est pour les noeuds dans l'arbre si le
     * concept est traduit, on a le label, sinon, on a une chaine vide
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return #MR
     */
    public List<NodeIdValue> getAllIdValueFacetsOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

        List<NodeIdValue> listFacets = new ArrayList<>();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery(
                        "SELECT thesaurus_array.id_facet" +
                        " FROM thesaurus_array" +
                        " WHERE" +
                        " thesaurus_array.id_thesaurus = '" + idThesaurus + "'" +
                        " AND" +
                        " thesaurus_array.id_concept_parent = '" + idConcept + "'");
                       
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id_facet"));
                        nodeIdValue.setValue(getLabelOfFacet(ds, nodeIdValue.getId(), idThesaurus, idLang));
                        listFacets.add(nodeIdValue);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All id facets of concept : " + idConcept, sqle);
        }

        Collections.sort(listFacets);
        return listFacets;
    }

    /**
     * permet de retourner la liste des id facettes où ce concept en fait partie
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return #MR
     */
    public List<String> getAllIdFacetsConceptIsPartOf(HikariDataSource ds, String idConcept, String idThesaurus) {

        List<String> listIdFacets = new ArrayList<>();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT concept_facet.id_facet"
                        + " FROM concept_facet "
                        + " WHERE id_thesaurus = '" + idThesaurus + "'"
                        + " AND id_concept = '" + idConcept + "'");

                try ( ResultSet resultSet = stmt.getResultSet()) {
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
     * permet de retourner la liste des id facettes qui sont sous ce concept
     * où ce concept est le parent
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return #MR
     */
    public List<String> getAllIdFacetsOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        List<String> listIdFacets = new ArrayList<>();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT thesaurus_array.id_facet"
                        + " FROM thesaurus_array "
                        + " WHERE thesaurus_array.id_thesaurus = '" + idThesaurus + "'"
                        + " AND thesaurus_array.id_concept_parent = '" + idConcept + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
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
     * permet de retourner un NodeFacet avec l'id, le label et le conceptParent
     * de la facette si le label n'est pas traduit, il est remplacé par une
     * chaine vide
     *
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
            try ( Statement stmt = conn.createStatement()) {
                try {
                    String query = "SELECT node_label.id_facet, thesaurus_array.id_concept_parent"
                            + " FROM node_label, thesaurus_array"
                            + " WHERE"
                            + " node_label.id_facet=thesaurus_array.id_facet"
                            + " and"
                            + " node_label.id_thesaurus = thesaurus_array.id_thesaurus"
                            + " and node_label.id_facet ='" + idFacet + "'"
                            + " and node_label.id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        nodeFacet.setIdFacet(resultSet.getString("id_facet"));
                        nodeFacet.setIdThesaurus(idThesaurus);
                        nodeFacet.setIdConceptParent(resultSet.getString("id_concept_parent"));
                        nodeFacet.setLexicalValue(getLabelOfFacet(ds, idFacet, idThesaurus, lang));
                        nodeFacet.setLang(lang);
                    }
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
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
    
    /**
     * permet de retourner l'id parent d'une facette
     *
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @return
     */
    public String getIdConceptParentOfFacet(HikariDataSource ds, String idFacet, String idThesaurus) {
        String idParentFacet = null;
        try (Connection conn = ds.getConnection()){
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT thesaurus_array.id_concept_parent"
                            + " FROM thesaurus_array"
                            + " WHERE"
                            + " thesaurus_array.id_facet ='" + idFacet + "'"
                            + " and thesaurus_array.id_thesaurus = '" + idThesaurus + "'");

                try( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idParentFacet = resultSet.getString("id_concept_parent");
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idParent of Facet : " + idFacet, sqle);
        }
        return idParentFacet;
    }

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
///////////////////// fin nouvelles méthodes MR //////////////////////////////
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////     
    /**
     * Cette fonction permet d'ajouter une nouvelle Facette si l'indentifiant
     * est fourni, on l'ajoute, sinon, on génère un nouveau
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
        
        lexicalValue = fr.cnrs.opentheso.utils.StringUtils.convertString(lexicalValue);

        if (idFacet == null || idFacet.isEmpty()) {
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
                            + "'" + idThesaurus + "', '" + lexicalValue + "', now(), now(), '" + idLang + "');");

                    stmt.executeUpdate("INSERT INTO thesaurus_array(id_thesaurus, id_concept_parent, id_facet) VALUES ('"
                            + idThesaurus + "', '" + idConceptParent + "', '" + idFacet + "');");
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
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select nextval('thesaurus_array_facet_id_seq') from thesaurus_array_facet_id_seq");
                resultSet = stmt.getResultSet();
                if (resultSet.next()) {
                    int idNumerique = resultSet.getInt(1);
                    idFacet = "F" + (idNumerique);
                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
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
                            + "'" + idFacet + "', '" + idThesaurus + "', '" + idConcept + "');");
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
     * Cette fonction permet de rajouter une traduction à une facette existante.
     *
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
        boolean status = false;
        lexicalValue = fr.cnrs.opentheso.utils.StringUtils.convertString(lexicalValue);
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into node_label "
                        + "(id_facet, id_thesaurus, lexical_value, lang)"
                        + " values ("
                        + "'" + idFacet + "'"
                        + ",'" + idThesaurus + "'"
                        + ",'" + lexicalValue + "'"
                        + ",'" + idLang + "')");
                status = true;
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
        boolean status = false;
        lexicalValue = fr.cnrs.opentheso.utils.StringUtils.convertString(lexicalValue);

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE node_label set"
                        + " lexical_value = '" + lexicalValue + "'"
                        + " WHERE id_facet = '" + idFacet + "'"
                        + " AND id_thesaurus = '" + idThesaurus + "'"
                        + " AND lang = '" + idLang + "'");
                status = true;
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
            log.error("Error while asking if Traduction of Facet exist : " + idFacet, sqle);
        }
        return existe;
    }

    /**
     * permet de retourner le label de la facette, si la facette n'est pas
     * traduite, elle retourne chaine vide
     *
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
                    stmt.executeQuery("SELECT lexical_value FROM node_label WHERE id_facet = '" + idFacet + "' AND id_thesaurus = '" + idThesaurus + "' AND lang = '" + lang + "'");
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        label = resultSet.getString("lexical_value");
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
                    stmt.executeQuery("SELECT * FROM node_label WHERE id_facet = '" + idFacet + "' AND id_thesaurus = '" + idThesaurus + "' AND lang != '" + lang + "'");
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeFacet facet = new NodeFacet();
                        facet.setIdFacet(resultSet.getString("id_facet"));
                        facet.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        facet.setLexicalValue(resultSet.getString("lexical_value"));
                        facet.setLang(resultSet.getString("lang"));
                        facetLists.add(facet);
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
            log.error("Error while getting Facet : " + idFacet, sqle);
        }

        return facetLists;
    }

    /**
     * Cette fonction permet de supprimer une Facette avec ses relations
     *
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @return
     */
    public boolean deleteFacet(HikariDataSource ds, String idFacet, String idThesaurus) {
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
     *
     * @param ds
     * @param idFacet
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public boolean deleteTraductionFacet(HikariDataSource ds, String idFacet, String idThesaurus,
            String idLang) {
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
     *
     * @param ds
     * @param idFacet
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public boolean deleteConceptFromFacet(HikariDataSource ds,
            String idFacet, String idConcept, String idThesaurus) {
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
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT node_label.*, thesaurus_array.id_concept_parent FROM node_label, thesaurus_array "
                            + " WHERE"
                            + " node_label.id_thesaurus = thesaurus_array.id_thesaurus"
                            + " and"
                            + " node_label.id_facet = thesaurus_array.id_facet"
                            + " and"
                            + " node_label.id_thesaurus = '" + idThesaurus + "'");

                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
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
                        nodeUri.setIdArk(getIdArkOfConcept(ds, nodeFacet.getIdConceptParent(), idThesaurus));
                        nodeUri.setIdHandle(getIdHandleOfConcept(ds, nodeFacet.getIdConceptParent(), idThesaurus));
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

    private String getIdHandleOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        String handle = "";
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_handle from concept where id_thesaurus = '"
                        + idThesaurus + "' and id_concept = '" + idConcept + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        handle = resultSet.getString("id_handle");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idHandle of Concept : " + idConcept, sqle);
        }
        return handle;
    }

    private String getIdArkOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        String ark = "";
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_ark from concept where id_thesaurus = '" + idThesaurus
                        + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        ark = resultSet.getString("id_ark").trim();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idArk of Concept : " + idConcept, sqle);
        }
        return ark;
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
                    stmt.executeQuery("SELECT id_facet FROM node_label WHERE lang = '" + lang
                            + "' AND lower(lexical_value) = lower('" + name + "') AND id_thesaurus = '" + idThesaurus + "'");
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
     * permet de retourner la liste des concepts membres de la facette triés
     *
     * @param ds
     * @param idFacet
     * @param idLang
     * @param idTheso
     * @return #MR
     */
    public ArrayList<NodeIdValue> getAllMembersOfFacetSorted(HikariDataSource ds,
            String idFacet, String idLang, String idTheso) {

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                    stmt.executeQuery("SELECT concept_facet.id_concept "
                            + " FROM concept_facet" 
                            + " WHERE  concept_facet.id_thesaurus = '" + idTheso + "'"
                            + " and concept_facet.id_facet = '" + idFacet + "'");
                    try (ResultSet resultSet = stmt.getResultSet()) {
                        while (resultSet.next()) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(resultSet.getString("id_concept"));
                            nodeIdValues.add(nodeIdValue);
                        }
                    }

                    for (NodeIdValue nodeIdValue : nodeIdValues) {
                        stmt.executeQuery("SELECT term.lexical_value, concept.status"
                                + " FROM concept, preferred_term, term"
                                + " WHERE concept.id_concept = preferred_term.id_concept AND"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                                + " preferred_term.id_term = term.id_term AND"
                                + " preferred_term.id_thesaurus = term.id_thesaurus AND"
                                + " concept.id_concept = '" + nodeIdValue.getId() + "' AND"
                                + " term.lang = '" + idLang + "' AND"
                                + " term.id_thesaurus = '" + idTheso + "';");
                        try ( ResultSet resultSet = stmt.getResultSet()) {
                            if ((resultSet.next())) {
                                nodeIdValue.setValue(resultSet.getString("lexical_value"));
                            } else {
                                nodeIdValue.setValue("");
                            }
                        }
                    }
                }
            } catch (SQLException sqle) {
                // Log exception
                log.error("Error while getting concepts members of Facet : " + idFacet, sqle);
            }
            Collections.sort(nodeIdValues);
            return nodeIdValues;
        }
        /**
         * permet de retourner la liste des concepts membres de la facette
         *
         * @param ds
         * @param idFacet
         * @param idTheso
         * @return #MR
         */
    public List<String> getAllMembersOfFacet(HikariDataSource ds,
            String idFacet, String idTheso) {
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
                    stmt.executeQuery("SELECT concept_facet.id_concept "
                            + " FROM concept_facet "
                            + " WHERE"
                            + " concept_facet.id_thesaurus = '" + idTheso + "'"
                            + " and"
                            + " concept_facet.id_facet = '" + idFacet + "'");
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

    public boolean updateLabelFacet(HikariDataSource ds, String newLabel, String idFacet, String idThes, String lang) {
        boolean status = false;
        newLabel = (fr.cnrs.opentheso.utils.StringUtils.convertString(newLabel));
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE node_label SET lexical_value='" + newLabel + "' WHERE id_facet = '"
                        + idFacet + "' AND lang = '" + lang + "' AND id_thesaurus = '" + idThes + "'");
                status = true;
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
                    stmt.executeUpdate("UPDATE thesaurus_array SET id_concept_parent = '" + idConceptParent + "' WHERE id_facet='" + idFacet + "' AND id_thesaurus='" + idThes + "'");
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
        }
    }

    /**
     * Permet de savoir si la facette a ce concept en particulier
     *
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
     *
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
     *
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
     *
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
}
