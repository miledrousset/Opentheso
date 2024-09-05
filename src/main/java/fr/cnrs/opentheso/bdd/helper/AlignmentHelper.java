package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.alignment.NodeAlignmentSmall;
import fr.cnrs.opentheso.models.alignment.NodeAlignmentType;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.alignment.NodeSelectedAlignment;
import fr.cnrs.opentheso.models.alignment.AlignementSource;
import fr.cnrs.opentheso.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;



@Data
@Slf4j
public class AlignmentHelper {

    private String message = "";

    /**
     * ************************************************************
     * /**************************************************************
     * Nouvelles fonctions stables auteur Miled Rousset
     * /**************************************************************
     * /*************************************************************
     */
    /**
     * cette fonction permet de récupérer le concept aligné avec Ontome
     *
     * @param ds
     * @param idTheso
     * @param cidocClass
     * @return
     */
    public ArrayList<NodeIdValue> getLinkedConceptsWithOntome(HikariDataSource ds, String idTheso, String cidocClass) {

        ArrayList<NodeIdValue> listAlignementsOntome = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select alignement.internal_id_concept, alignement.uri_target"
                        + " from alignement"
                        + " where"
                        + " internal_id_thesaurus = '" + idTheso + "'"
                        + " and "
                        + " uri_target ilike '%ontome.net/ontology/" + cidocClass + "'"
                        + " or uri_target ilike '%ontome.net/ontology/c" + cidocClass + "'"
                        + " or uri_target ilike '%ontome.net/class/" + cidocClass + "'"
                        + " and"
                        + " alignement_id_type = 1");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("internal_id_concept"));
                        nodeIdValue.setValue(resultSet.getString("uri_target"));

                        listAlignementsOntome.add(nodeIdValue);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting alignements selected of thesaurus " + idTheso, sqle);
        }
        return listAlignementsOntome;
    }

    /**
     * cette fonction permet de récupérer les alignements avec Ontome
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public ArrayList<NodeIdValue> getAllLinkedConceptsWithOntome(HikariDataSource ds, String idTheso) {

        ArrayList<NodeIdValue> listAlignementsOntome = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select alignement.internal_id_concept, alignement.uri_target"
                        + " from alignement"
                        + " where"
                        + " internal_id_thesaurus = '" + idTheso + "'"
                        + " and "
                        + " uri_target ilike '%ontome.net/ontology%'"
                        + " or uri_target ilike '%ontome.net/class%'"
                        + " and"
                        + " alignement_id_type = 1");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("internal_id_concept"));
                        nodeIdValue.setValue(resultSet.getString("uri_target"));

                        listAlignementsOntome.add(nodeIdValue);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting alignements selected of thesaurus " + idTheso, sqle);
        }
        return listAlignementsOntome;
    }

    /**
     * cette fonction permet de récupérer les informations de la table des
     * sources d'alignement
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public ArrayList<NodeSelectedAlignment> getSelectedAlignementOfThisTheso(HikariDataSource ds, String idTheso) {

        ArrayList<NodeSelectedAlignment> listAlignementSourceSelected = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select alignement_source.source, alignement_source.description, id_alignement_source"
                        + " from thesaurus_alignement_source, alignement_source"
                        + " WHERE alignement_source.id = thesaurus_alignement_source.id_alignement_source"
                        + " and thesaurus_alignement_source.id_thesaurus = '" + idTheso + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeSelectedAlignment nodeSelectedAlignment = new NodeSelectedAlignment();
                        nodeSelectedAlignment.setIdAlignmentSource(resultSet.getInt("id_alignement_source"));
                        nodeSelectedAlignment.setSourceLabel(resultSet.getString("source"));
                        nodeSelectedAlignment.setSourceDescription(resultSet.getString("description"));

                        listAlignementSourceSelected.add(nodeSelectedAlignment);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting alignements selected of thesaurus " + idTheso, sqle);
        }
        return listAlignementSourceSelected;
    }

    /**
     * Permet de retourner les types d'alignement
     *
     * @param ds
     * @return #MR
     */
    public ArrayList<NodeAlignmentType> getAlignmentsType(HikariDataSource ds) {

        ArrayList<NodeAlignmentType> nodeAlignmentTypes = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {

                String query = "select * from alignement_type";
                stmt.executeQuery(query);

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeAlignmentType nodeAlignmentType = new NodeAlignmentType();
                        nodeAlignmentType.setId(resultSet.getInt("id"));
                        nodeAlignmentType.setIsocode(resultSet.getString("isocode"));
                        nodeAlignmentType.setLabel(resultSet.getString("label"));
                        nodeAlignmentType.setLabelSkos(resultSet.getString("label_skos"));
                        nodeAlignmentTypes.add(nodeAlignmentType);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Type of Alignment : ", sqle);
        }
        return nodeAlignmentTypes;
    }

    /**
     * permet de modifier un alignement
     */
    public boolean updateAlignment(HikariDataSource ds, int idAlignment, String conceptTarget, String thesaurusTarget,
                                   String uriTarget, int idTypeAlignment, String idConcept, String idThesaurus) {

        boolean status = false;
        uriTarget = fr.cnrs.opentheso.utils.StringUtils.convertString(uriTarget);
        conceptTarget = fr.cnrs.opentheso.utils.StringUtils.convertString(conceptTarget);

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "UPDATE alignement set concept_target = '" + conceptTarget + "',"
                        + " modified = current_date,"
                        + " thesaurus_target = '" + thesaurusTarget + "',"
                        + " uri_target = '" + uriTarget + "',"
                        + " alignement_id_type = " + idTypeAlignment
                        + " WHERE internal_id_thesaurus = '" + idThesaurus + "'"
                        + " AND internal_id_concept = '" + idConcept + "'"
                        + " AND id = " + idAlignment;

                stmt.executeUpdate(query);
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating Alignment : " + idAlignment, sqle);
        }
        return status;
    }

    /**
     * Permet de savoir si le concept 'id_concept' a déjà une alignement ou pas
     */
    public boolean isExistsAlignement(HikariDataSource ds, String id_Theso, String id_Concept, int alignement_id_type, String urlTarget) {
        
        urlTarget = StringUtils.addQuotes(urlTarget);

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "SELECT internal_id_concept from alignement"
                        + " where internal_id_concept = '" + id_Concept + "'"
                        + " and internal_id_thesaurus = '" + id_Theso + "'"
                        + " and alignement_id_type = '" + alignement_id_type + "'"
                        + " and uri_target = '" + urlTarget + "'";
                try (ResultSet rs = stmt.executeQuery(query)) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while search alignement with target : " + sqle);
        }
        return false;
    }

    /**
     * Permet de savoir si on a besoin de faire un update ou un insert dans la BDD
     */
    public boolean addNewAlignment(HikariDataSource ds,
            int author,
            String conceptTarget, String thesaurusTarget,
            String uriTarget, int idTypeAlignment,
            String idConcept, String idThesaurus, int id_alignement_source) {
        
        thesaurusTarget = StringUtils.convertString(thesaurusTarget);

        if (!isExistsAlignement(ds, idThesaurus, idConcept, idTypeAlignment, uriTarget)) {
            message = "";
            return addNewAlignement2(ds, author, conceptTarget, thesaurusTarget, uriTarget, idTypeAlignment,
                    idConcept, idThesaurus, id_alignement_source);
        } else {
            return updateAlignment(ds, idTypeAlignment, conceptTarget, thesaurusTarget, uriTarget, idTypeAlignment,
                    idConcept, idThesaurus);
        }
    }

    /**
     * Cette fonction permet d'ajouter un nouvel alignement sur un thésaurus distant pour ce concept
     */
    private boolean addNewAlignement2(HikariDataSource ds,
            int author,
            String conceptTarget, String thesaurusTarget,
            String uriTarget, int idTypeAlignment,
            String idConcept, String idThesaurus, int id_alignement_source) {
        Connection conn;
        Statement stmt;
        boolean status;
        conceptTarget = fr.cnrs.opentheso.utils.StringUtils.convertString(conceptTarget);
        uriTarget = fr.cnrs.opentheso.utils.StringUtils.convertString(uriTarget);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into alignement "
                            + "(author, concept_target, thesaurus_target,"
                            + " uri_target, alignement_id_type,"
                            + " internal_id_thesaurus, internal_id_concept,"
                            + " id_alignement_source)"
                            + " values ("
                            + author
                            + ",'" + conceptTarget + "'"
                            + ",'" + thesaurusTarget + "'"
                            + ",'" + uriTarget + "'"
                            + "," + idTypeAlignment
                            + ",'" + idThesaurus + "'"
                            + ",'" + idConcept + "',"
                            + id_alignement_source + " )";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            log.error("Error while adding external alignement with target : " + uriTarget, sqle);
            return false;
        }
        return status;
    }

    /**
     * ************************************************************
     * /**************************************************************
     * Fin nouvelles fonctions stables auteur Miled Rousset
     * /**************************************************************
     * /*************************************************************
     */
    /**
     * Cette fonction permet d'ajouter un nouvel alignement sur un thésaurus
     * distant pour ce concept, utilisée uniquement pour les imports
     *
     * @param ds
     * @param nodeAlignment
     *
     * @return
     */
    public boolean addNewAlignment(HikariDataSource ds, NodeAlignment nodeAlignment) {

        nodeAlignment.setConcept_target(fr.cnrs.opentheso.utils.StringUtils.convertString(nodeAlignment.getConcept_target()));
        nodeAlignment.setUri_target(fr.cnrs.opentheso.utils.StringUtils.convertString(nodeAlignment.getUri_target()));

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into alignement "
                        + "(author, concept_target, thesaurus_target,"
                        + " uri_target, alignement_id_type,"
                        + " internal_id_thesaurus, internal_id_concept)"
                        + " values ("
                        + nodeAlignment.getId_author()
                        + ",'" + nodeAlignment.getConcept_target() + "'"
                        + ",'" + nodeAlignment.getThesaurus_target() + "'"
                        + ",'" + nodeAlignment.getUri_target() + "'"
                        + "," + nodeAlignment.getAlignement_id_type()
                        + ",'" + nodeAlignment.getInternal_id_thesaurus() + "'"
                        + ",'" + nodeAlignment.getInternal_id_concept() + "')");
                return true;
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding external alignement with target : " + nodeAlignment.getUri_target(), sqle);
            }
        }
        return false;
    }

    /**
     * Cette focntion permet de supprimer un alignement
     */
    public boolean deleteAlignment(HikariDataSource ds, int idAlignment, String idThesaurus) {

        boolean status = false;
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("delete from alignement where id = " + idAlignment + " and internal_id_thesaurus = '" + idThesaurus + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting alignment from thesaurus with idAlignment : " + idAlignment, sqle);
        }
        return status;
    }

    /**
     * Cette focntion permet de supprimer un alignement
     */
    public boolean deleteAlignment(HikariDataSource ds, String idConcept, String idThesaurus, String uri) {

        boolean status = false;
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("delete from alignement where internal_id_concept = '" + idConcept + "' and internal_id_thesaurus = '"+idThesaurus+"' and uri_target = '" + uri + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting alignment from thesaurus with uri : " + uri, sqle);
        }
        return status;
    }

    /**
     * Cette focntion permet de supprimer un alignement par URI
     */
    public boolean deleteAlignmentByUri(HikariDataSource ds,
            String uri, String idConcept, String idThesaurus) {

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from alignement "
                        + " where uri_target = '" + uri + "'"
                        + " and internal_id_thesaurus = '" + idThesaurus + "'"
                        + " and internal_id_concept = '" + idConcept + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting alignment from thesaurus with URI : " + uri, sqle);
        }
        return false;
    }

    /**
     * Cette focntion permet de supprimer tous les aligenements d'un concept
     */
    public boolean deleteAlignmentOfConcept(Connection conn, String idConcept, String idThesaurus) {

        Statement stmt;
        boolean status = false;

        try {
            stmt = conn.createStatement();
            String query = "delete from alignement "
                    + " where internal_id_concept = '" + idConcept + "'"
                    + " and internal_id_thesaurus = '" + idThesaurus + "'";

            stmt.executeUpdate(query);
            status = true;
            stmt.close();
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting alignment from thesaurus with idConcept : " + idConcept, sqle);
        }
        return status;
    }

//        exactMatch   = 1;
//        closeMatch   = 2;
//        broadMatch   = 3;
//        relatedMatch = 4;        
//        narrowMatch  = 5;    
    /**
     * Cette fonction permet de supprimer tous les aligenements d'un concept par
     * type
     *
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param type
     * @return
     */
    public boolean deleteAlignmentOfConceptByType(HikariDataSource ds,
            String idConcept, String idThesaurus, int type) {

        boolean status = false;
        try (Connection conn = ds.getConnection()) {
            // Get connection from pool
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from alignement "
                        + " where internal_id_concept = '" + idConcept + "'"
                        + " and internal_id_thesaurus = '" + idThesaurus + "'"
                        + " and alignement_id_type = " + type
                );
                status = true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting alignment from thesaurus with idConcept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de retourner la liste des alignements pour un
     * concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class #MR
     */
    public ArrayList<NodeAlignmentSmall> getAllAlignmentOfConceptNew(HikariDataSource ds, String idConcept, String idThesaurus) {

        ArrayList<NodeAlignmentSmall> nodeAlignmentList = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "SELECT uri_target, alignement_id_type"
                        + " FROM alignement"
                        + " where internal_id_concept = '" + idConcept + "'"
                        + " and internal_id_thesaurus ='" + idThesaurus + "'";
                stmt.executeQuery(query);
                try (ResultSet resultSet = stmt.getResultSet()) {

                    while (resultSet.next()) {
                        NodeAlignmentSmall nodeAlignmentSmall = new NodeAlignmentSmall();
                        nodeAlignmentSmall.setUri_target(resultSet.getString("uri_target").trim());
                        nodeAlignmentSmall.setAlignement_id_type(resultSet.getInt("alignement_id_type"));
                        nodeAlignmentList.add(nodeAlignmentSmall);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All list of alignment of Concept  : " + idConcept, sqle);
        }
        return nodeAlignmentList;
    }

    /**
     * Cette fonction permet de retourner la liste des alignements pour un
     * concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class
     */
    public ArrayList<NodeAlignment> getAllAlignmentOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        ArrayList<NodeAlignment> nodeAlignmentList = null;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "SELECT alignement.id, created, modified, author, thesaurus_target, concept_target, uri_target,"
                        + " alignement_id_type, internal_id_thesaurus, internal_id_concept, id_alignement_source,"
                        + " alignement_type.label, alignement_type.label_skos, alignement.url_available"
                        + " FROM alignement, alignement_type"
                        + " WHERE alignement.alignement_id_type = alignement_type.id"
                        + " AND internal_id_concept = '" + idConcept + "'"
                        + " AND internal_id_thesaurus ='" + idThesaurus + "'";
                stmt.executeQuery(query);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    nodeAlignmentList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeAlignment nodeAlignment = new NodeAlignment();
                        nodeAlignment.setId_alignement(resultSet.getInt("id"));
                        nodeAlignment.setCreated(resultSet.getDate("created"));
                        nodeAlignment.setModified(resultSet.getDate("modified"));
                        nodeAlignment.setId_author(resultSet.getInt("author"));
                        nodeAlignment.setThesaurus_target(resultSet.getString("thesaurus_target"));
                        nodeAlignment.setConcept_target(resultSet.getString("concept_target"));
                        nodeAlignment.setUri_target(resultSet.getString("uri_target").trim());
                        nodeAlignment.setAlignement_id_type(resultSet.getInt("alignement_id_type"));
                        nodeAlignment.setInternal_id_thesaurus(resultSet.getString("internal_id_thesaurus"));
                        nodeAlignment.setInternal_id_concept(resultSet.getString("internal_id_concept"));
                        nodeAlignment.setId_source(resultSet.getInt("id_alignement_source"));
                        nodeAlignment.setAlignmentLabelType(resultSet.getString("label"));
                        nodeAlignment.setAlignmentLabelType(resultSet.getString("label_skos"));
                        nodeAlignment.setAlignementLocalValide(resultSet.getBoolean("url_available"));
                        nodeAlignmentList.add(nodeAlignment);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All list of alignment of Concept  : " + idConcept, sqle);
        }
        return nodeAlignmentList;
    }

    /**
     * Retourne la liste des types d'alignements sous forme de MAP (id + Nom)
     *
     * @param ds
     * @return
     */
    public HashMap<String, String> getAlignmentType(HikariDataSource ds) {

        HashMap<String, String> map = new HashMap<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "select id, label_skos from alignement_type";
                stmt.executeQuery(query);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        map.put(
                                String.valueOf(resultSet.getInt("id")),
                                resultSet.getString("label_skos"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Map of Type of Alignment : " + map.toString(), sqle);
        }
        return map;
    }

    /**
     * cette fonction permet de récupérer les informations de la table des
     * sources d'alignement
     *
     * @param ds
     * @param id_theso
     * @return
     */
    public ArrayList<AlignementSource> getAlignementSource(HikariDataSource ds, String id_theso) {

        ArrayList<AlignementSource> alignementSources = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "select "
                        + " alignement_source.gps,"
                        + " alignement_source.source, alignement_source.requete,"
                        + " alignement_source.type_rqt, alignement_source.alignement_format,"
                        + " alignement_source.id, alignement_source.description, "
                        + " alignement_source.source_filter from alignement_source, thesaurus_alignement_source"
                        + " WHERE thesaurus_alignement_source.id_alignement_source = alignement_source.id"
                        + " AND thesaurus_alignement_source.id_thesaurus = '" + id_theso + "'"
                        + " order by alignement_source.source ASC";

                try (ResultSet resultSet = stmt.executeQuery(query)) {
                    while (resultSet.next()) {
                        AlignementSource alignementSource = new AlignementSource();
                        alignementSource.setSource(resultSet.getString("source"));
                        alignementSource.setRequete(resultSet.getString("requete"));
                        alignementSource.setTypeRequete(resultSet.getString("type_rqt"));
                        alignementSource.setAlignement_format(resultSet.getString("alignement_format"));
                        alignementSource.setId(resultSet.getInt("id"));
                        alignementSource.setDescription(resultSet.getString("description"));
                        alignementSource.setSource_filter(resultSet.getString("source_filter"));
                        alignementSource.setGps(resultSet.getBoolean("gps"));
                        alignementSources.add(alignementSource);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting colection of Type of Alignment : ", sqle);
        }
        return alignementSources;
    }

    /**
     * cette fonction permet de récupérer les informations de la table des
     * sources d'alignement
     *
     * @param ds
     * @param id
     * @return
     */
    public AlignementSource getThisAlignementSource(HikariDataSource ds, int id) {

        AlignementSource alignementSource = new AlignementSource();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "select * from alignement_source where id = " + id;

                try (ResultSet resultSet = stmt.executeQuery(query)) {
                    while (resultSet.next()) {
                        alignementSource.setSource(resultSet.getString("source"));
                        alignementSource.setRequete(resultSet.getString("requete"));
                        alignementSource.setTypeRequete(resultSet.getString("type_rqt"));
                        alignementSource.setAlignement_format(resultSet.getString("alignement_format"));
                        alignementSource.setId(resultSet.getInt("id"));
                        alignementSource.setDescription(resultSet.getString("description"));
                        alignementSource.setSource_filter(resultSet.getString("source_filter"));
                        alignementSource.setGps(resultSet.getBoolean("gps"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting alignement source : ", sqle);
        }
        return alignementSource;
    }

    public ArrayList<AlignementSource> getAlignementSourceSAdmin(HikariDataSource ds) {

        ArrayList<AlignementSource> alignementSources = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "select  * from alignement_source order by id desc";
                try (ResultSet resultSet = stmt.executeQuery(query)) {
                    while (resultSet.next()) {
                        AlignementSource alignementSource = new AlignementSource();
                        alignementSource.setSource(resultSet.getString("source"));
                        alignementSource.setRequete(resultSet.getString("requete"));
                        alignementSource.setTypeRequete(resultSet.getString("type_rqt"));
                        alignementSource.setAlignement_format(resultSet.getString("alignement_format"));
                        alignementSource.setId(resultSet.getInt("id"));
                        alignementSource.setDescription((resultSet.getString("description")));
                        alignementSource.setSource_filter((resultSet.getString("source_filter")));
                        alignementSources.add(alignementSource);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting colection of Type of Alignment : ", sqle);
        }
        return alignementSources;
    }

    /**
     * permet d'ajouter une nouvelle source d'alignement dans la base de données
     * Si currentIdTheso =null, on associe pas la source au thésaurus, sinon, on
     * l'associe automatiquement au thésaurus en cours
     *
     * @param ds
     * @param alignement
     * @param id_user
     * @param currentIdTheso
     * @return
     */
    public boolean addNewAlignmentSource(HikariDataSource ds, AlignementSource alignement, int id_user, String currentIdTheso) {
        int id_alignement;

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            if (!insertAlignementSource(conn, alignement, id_user)) {
                conn.rollback();
                conn.close();
                return false;
            }
            if (currentIdTheso != null) {
                id_alignement = getId_Alignement(conn, alignement.getSource());
                if (!insertSourceAlignementToTheso(conn, currentIdTheso, id_alignement)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
            }
            conn.commit();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(AlignmentHelper.class.getName()).log(Level.SEVERE, null, ex);
            message = ex.toString();
            return false;
        }
    }

    public int getId_Alignement(Connection conn, String source) {
        int id_alignement = 0;
        try (Statement stmt = conn.createStatement()) {
            String query = "Select id from alignement_source "
                    + " where source = '" + source + "'";

            try (ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    id_alignement = rs.getInt("id");
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while insert new Alignement : ", sqle);
        }
        return id_alignement;
    }

    private boolean insertAlignementSource(Connection conn, AlignementSource alig, int id_user) {

        boolean status = false;
        try (Statement stmt = conn.createStatement()) {
            String query = "Insert into alignement_source (source,requete,type_rqt,"
                    + "alignement_format, id_user, description) values('"
                    + alig.getSource() + "','"
                    + alig.getRequete() + "','"
                    + alig.getTypeRequete() + "','"
                    + alig.getAlignement_format() + "',"
                    + id_user + ",'"
                    + alig.getDescription() + "');";
            stmt.execute(query);
            status = true;
        } catch (SQLException sqle) {
            log.error("Error while insert new Alignement : ", sqle);
            message = sqle.toString();
        }
        return status;
    }

    /**
     *
     * @param ds
     * @param alignementSource
     * 
     * @return
     */
    public boolean updateAlignmentSource(HikariDataSource ds, 
            AlignementSource alignementSource) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "update alignement_source set "
                        + "source ='" + alignementSource.getSource()
                        + "', requete ='" + alignementSource.getRequete()
                        + "', description ='" + alignementSource.getDescription()
                        + "' where id = " + alignementSource.getId();
                stmt.execute(query);
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while update Alignment source : ", sqle);
        }
        return false;
    }
    
    /**
     *
     * @param ds
     * @param id
     * 
     * @return
     */
    public boolean deleteAlignmentSource(HikariDataSource ds, 
            int id) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "delete from alignement_source "
                        + " where id = " + id ;
                stmt.execute(query);
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting Alignment source : ", sqle);
        }
        return false;
    }

    /**
     * permet d'ajouter une source d'alignement à un ou plusieurs thésaurus on
     * supprime d'abord les anciennes valeurs, puis on ajoute les nouvelles
     *
     * @param ds
     * @param idTheso
     * @param idAlignement
     * @return
     */
    public boolean addSourceAlignementToTheso(HikariDataSource ds, String idTheso, int idAlignement) {
        boolean status = false;
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            if (!insertSourceAlignementToTheso(conn, idTheso, idAlignement)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            status = true;

        } catch (SQLException ex) {
            Logger.getLogger(AlignmentHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * Permet d'effacer le alignement "idAlignement" du theso "idTheso"
     */
    public boolean deleteSourceAlignementFromTheso(Connection conn, String idTheso, int idAlignement) {

        boolean status = false;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("delete from thesaurus_alignement_source"
                    + " where id_alignement_source = " + idAlignement
                    + " and id_thesaurus = '" + idTheso + "'");
            status = true;
        } catch (SQLException sqle) {
            log.error("Error while insert new Alignement to theasurus : " + idTheso + " id_alignement : " + idAlignement, sqle);
        }
        return status;
    }

    /**
     * Permet de ajouté un alignement a un theso
     *
     * @param conn
     * @param idTheso
     * @param idAlignement
     * @return
     */
    private boolean insertSourceAlignementToTheso(Connection conn, String idTheso, int idAlignement) {
        boolean status = false;
        try (Statement stmt = conn.createStatement()) {
            String query = "Insert into thesaurus_alignement_source"
                    + "(id_thesaurus, id_alignement_source) values("
                    + "'" + idTheso + "',"
                    + idAlignement + ")";
            stmt.executeUpdate(query);
            status = true;
        } catch (SQLException sqle) {
            log.error("Error while insert new Alignement to theasurus : " + idTheso + " id_alignement : " + idAlignement, sqle);
        }
        return status;
    }

    public boolean updateAlignmentUrlStatut(HikariDataSource ds, int idAlignment, boolean newStatut,
                                            String idConcept, String idThesaurus) {

        boolean status = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "UPDATE alignement set url_available = " + newStatut
                        + " WHERE internal_id_thesaurus = '" + idThesaurus + "'"
                        + " AND internal_id_concept = '" + idConcept + "'"
                        + " AND id = " + idAlignment;

                stmt.executeUpdate(query);
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating Alignment : " + idAlignment, sqle);
        }
        return status;
    }

}
