package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.models.concept.NodeConceptType;
import fr.cnrs.opentheso.models.relations.NodeCustomRelation;
import fr.cnrs.opentheso.models.relations.NodeHieraRelation;
import fr.cnrs.opentheso.models.terms.NodeNT;
import fr.cnrs.opentheso.models.terms.NodeRT;
import fr.cnrs.opentheso.models.relations.NodeTypeRelation;
import fr.cnrs.opentheso.models.concept.NodeUri;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
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

}
