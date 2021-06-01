package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentSmall;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentType;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeSelectedAlignment;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.core.alignment.AlignementPreferences;
import fr.cnrs.opentheso.core.alignment.AlignementSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlignmentHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);
    private String message = "";

    /**
     * ************************************************************
     * /**************************************************************
     * Nouvelles fonctions stables auteur Miled Rousset
     * /**************************************************************
     * /*************************************************************
     */
    /**
     * cette fonction permet de récupérer les informations de la table des
     * sources d'alignement
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public ArrayList<NodeSelectedAlignment> getSelectedAlignementOfThisTheso(
            HikariDataSource ds, String idTheso) {

        ArrayList<NodeSelectedAlignment> listAlignementSourceSelected = new ArrayList<>();

        try {
            Connection conn = ds.getConnection();

            try ( Statement stmt = conn.createStatement()) {

                String query = "select alignement_source.source, alignement_source.description, id_alignement_source"
                        + " from thesaurus_alignement_source, alignement_source"
                        + " WHERE alignement_source.id = thesaurus_alignement_source.id_alignement_source"
                        + " and thesaurus_alignement_source.id_thesaurus = '" + idTheso + "'";

                try ( ResultSet resultSet = stmt.executeQuery(query)) {

                    while (resultSet.next()) {
                        NodeSelectedAlignment nodeSelectedAlignment = new NodeSelectedAlignment();
                        nodeSelectedAlignment.setIdAlignmnetSource(resultSet.getInt("id_alignement_source"));
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

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {

                String query = "select * from alignement_type";
                stmt.executeQuery(query);

                try ( ResultSet resultSet = stmt.getResultSet()) {
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
     *
     * @param ds
     * @param idAlignment
     * @param conceptTarget
     * @param thesaurusTarget
     * @param uriTarget
     * @param idTypeAlignment
     * @param idConcept
     * @param idThesaurus
     * @param id_alignement_source
     * @return #MR
     */
    public boolean updateAlignment(HikariDataSource ds,
            int idAlignment,
            String conceptTarget, String thesaurusTarget,
            String uriTarget, int idTypeAlignment,
            String idConcept, String idThesaurus, int id_alignement_source) {

        boolean status = false;
        uriTarget = new StringPlus().convertString(uriTarget);
        conceptTarget = new StringPlus().convertString(conceptTarget);

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
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
     *
     * @param ds
     * @param id_alignement_source
     * @param id_Theso
     * @param id_Concept
     * @param alignement_id_type
     * @param urlTarget
     * @return
     */
    public boolean isExistsAlignement(HikariDataSource ds, int id_alignement_source, String id_Theso,
            String id_Concept, int alignement_id_type, String urlTarget) {

        boolean status = false;
        StringPlus stringPlus = new StringPlus();
        urlTarget = stringPlus.addQuotes(urlTarget);

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "SELECT internal_id_concept from alignement"
                        + " where internal_id_concept = '" + id_Concept + "'"
                        + " and internal_id_thesaurus = '" + id_Theso + "'"
                        + " and id_alignement_source = '" + id_alignement_source + "'"
                        + " and alignement_id_type = '" + alignement_id_type + "'"
                        + " and uri_target = '" + urlTarget + "'";
                try ( ResultSet rs = stmt.executeQuery(query)) {
                    if (rs.next()) {
                        status = true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while search alignement with target : " + sqle);
        }
        return status;
    }

    /**
     * Permet de savoir si on besoin faire une update ou un insert dans la BDD
     *
     * @param ds
     * @param author
     * @param conceptTarget
     * @param thesaurusTarget
     * @param uriTarget
     * @param idTypeAlignment
     * @param idConcept
     * @param idThesaurus
     * @param id_alignement_source parametre que on prende de la BDD, si c'est 0
     * c'est alignement manuel
     * @return
     */
    public boolean addNewAlignment(HikariDataSource ds,
            int author,
            String conceptTarget, String thesaurusTarget,
            String uriTarget, int idTypeAlignment,
            String idConcept, String idThesaurus, int id_alignement_source) {
        boolean status = false;
        if (!isExistsAlignement(ds, id_alignement_source, idThesaurus,
                idConcept, idTypeAlignment,
                uriTarget)) {
            message = "<br>";//"Cet alignement n'exite pas, création en cours <br>";
            status = addNewAlignement2(ds, author, conceptTarget, thesaurusTarget, uriTarget, idTypeAlignment,
                    idConcept, idThesaurus, id_alignement_source);
            if (!status) {
                return false;
            }
            message += "<br> New alignment created ... ok";
        } else {
            // message = "Cette alignement exits, updating en cours";
            status = updateAlignment(ds, idTypeAlignment, conceptTarget, thesaurusTarget, uriTarget, idTypeAlignment,
                    idConcept, idThesaurus, id_alignement_source);
            if (!status) {
                return false;
            }
            message += "<br> Alignment updated ... Ok";
        }

        return status;
    }

    /**
     * Cette fonction permet d'ajouter un nouvel alignement sur un thésaurus
     * distant pour ce concept
     *
     * @param ds
     * @param author
     * @param conceptTarget
     * @param thesaurusTarget
     * @param uriTarget
     * @param idTypeAlignment
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    private boolean addNewAlignement2(HikariDataSource ds,
            int author,
            String conceptTarget, String thesaurusTarget,
            String uriTarget, int idTypeAlignment,
            String idConcept, String idThesaurus, int id_alignement_source) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        conceptTarget = new StringPlus().convertString(conceptTarget);
        uriTarget = new StringPlus().convertString(uriTarget);

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
            // Log exception
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                status = true;
            } else {
                log.error("Error while adding external alignement with target : " + uriTarget, sqle);
                return false;
            }
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
     * distant pour ce concept
     *
     * @param ds
     * @param nodeAlignment
     *
     * @return
     */
    public boolean addNewAlignment(HikariDataSource ds, NodeAlignment nodeAlignment) {

        nodeAlignment.setConcept_target(new StringPlus().convertString(nodeAlignment.getConcept_target()));
        nodeAlignment.setUri_target(new StringPlus().convertString(nodeAlignment.getUri_target()));

        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()){
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
            log.error("Error while adding external alignement with target : " + nodeAlignment.getUri_target(), sqle);
            return false;
        }
    }

    /**
     * Cette focntion permet de supprimer un alignement
     *
     * @param ds
     * @param idAlignment
     * @param idThesaurus
     * @return
     */
    public boolean deleteAlignment(HikariDataSource ds,
            int idAlignment, String idThesaurus) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from alignement "
                            + " where id = " + idAlignment
                            + " and internal_id_thesaurus = '" + idThesaurus + "'";

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
            log.error("Error while deleting alignment from thesaurus with idAlignment : " + idAlignment, sqle);
        }
        return status;
    }

    /**
     * Supprime toute les Alignement source pour un thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public boolean deleteAllALignementSourceOfTheso(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from thesaurus_alignement_source "
                            + " where id_thesaurus = '" + idThesaurus + "'";
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
            log.error("Error while deleting alignment source from thesaurus  : " + idThesaurus + sqle);
        }
        return status;

    }

    /**
     * Cette focntion permet de supprimer tous les aligenements d'un concept
     *
     * @param conn
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public boolean deleteAlignmentOfConcept(Connection conn,
            String idConcept, String idThesaurus) {

        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from alignement "
                            + " where internal_id_concept = '" + idConcept + "'"
                            + " and internal_id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeUpdate(query);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
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
    public ArrayList<NodeAlignmentSmall> getAllAlignmentOfConceptNew(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        ArrayList<NodeAlignmentSmall> nodeAlignmentList = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "SELECT uri_target, alignement_id_type"
                        + " FROM alignement"
                        + " where internal_id_concept = '" + idConcept + "'"
                        + " and internal_id_thesaurus ='" + idThesaurus + "'";
                stmt.executeQuery(query);
                try ( ResultSet resultSet = stmt.getResultSet()) {

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

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "SELECT alignement.id, created, modified, author, thesaurus_target, concept_target, uri_target,"
                        + " alignement_id_type, internal_id_thesaurus, internal_id_concept, id_alignement_source,"
                        + " alignement_type.label, alignement_type.label_skos"
                        + " FROM alignement, alignement_type where "
                        + " alignement.alignement_id_type = alignement_type.id"
                        + " and"
                        + " internal_id_concept = '" + idConcept + "'"
                        + " and internal_id_thesaurus ='" + idThesaurus + "'";
                stmt.executeQuery(query);
                try ( ResultSet resultSet = stmt.getResultSet()) {
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

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "select id, label_skos from alignement_type";
                stmt.executeQuery(query);
                try ( ResultSet resultSet = stmt.getResultSet()) {
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

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "select "
                        + " alignement_source.gps,"
                        + " alignement_source.source, alignement_source.requete,"
                        + " alignement_source.type_rqt, alignement_source.alignement_format,"
                        + " alignement_source.id, alignement_source.description, "
                        + " alignement_source.source_filter from alignement_source, thesaurus_alignement_source"
                        + " WHERE thesaurus_alignement_source.id_alignement_source = alignement_source.id"
                        + " AND thesaurus_alignement_source.id_thesaurus = '" + id_theso + "'"
                        + " order by alignement_source.source ASC";

                try ( ResultSet resultSet = stmt.executeQuery(query)) {
                    while (resultSet.next()) {
                        AlignementSource alignementSource = new AlignementSource();
                        alignementSource.setSource(resultSet.getString("source"));
                        alignementSource.setRequete(resultSet.getString("requete"));
                        alignementSource.setTypeRequete(resultSet.getString("type_rqt"));
                        alignementSource.setAlignement_format(resultSet.getString("alignement_format"));
                        alignementSource.setId(resultSet.getInt("id"));
                        alignementSource.setDescription(resultSet.getString("description"));
                        alignementSource.setSource_filter(resultSet.getString("source_filter"));
                        alignementSource.setIsGps(resultSet.getBoolean("gps"));
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
     * @param id_alignement_source
     * @return
     */
    public List<String> getSelectedAlignementOfThisTheso(HikariDataSource ds, int id_alignement_source) {

        List<String> listAlignementSourceSelected = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "select "
                        + " id_thesaurus from thesaurus_alignement_source"
                        + " WHERE id_alignement_source = " + id_alignement_source;
                try ( ResultSet resultSet = stmt.executeQuery(query)) {
                    listAlignementSourceSelected = new ArrayList<>();
                    while (resultSet.next()) {
                        listAlignementSourceSelected.add(resultSet.getString("id_thesaurus"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting colection of Type of Alignment : ", sqle);
        }
        return listAlignementSourceSelected;
    }

    public ArrayList<AlignementSource> getAlignementSourceSAdmin(HikariDataSource ds) {

        ArrayList<AlignementSource> alignementSources = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "select  * from alignement_source";
                try ( ResultSet resultSet = stmt.executeQuery(query)) {
                    while (resultSet.next()) {
                        AlignementSource alignementSource = new AlignementSource();
                        alignementSource.setSource(resultSet.getString("source"));
                        alignementSource.setRequete(resultSet.getString("requete"));
                        alignementSource.setTypeRequete(resultSet.getString("type_rqt"));
                        alignementSource.setAlignement_format(resultSet.getString("alignement_format"));
                        alignementSource.setId(resultSet.getInt("id"));
                        alignementSource.setDescription((resultSet.getString("description")));
                        alignementSource.setDescription((resultSet.getString("source_filter")));
                        alignementSources.add(alignementSource);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting colection of Type of Alignment : ", sqle);
        }
        return alignementSources;
    }

    public ArrayList<String> typesRequetes(HikariDataSource ds,
            String cherche) throws SQLException {

        ArrayList<String> les_types = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "select e.enumlabel\n"
                        + "from pg_type t, pg_enum e\n"
                        + "where t.oid = e.enumtypid\n"
                        + "and t.typname = '" + cherche + "'";
                try ( ResultSet resultSet = stmt.executeQuery(query)) {
                    les_types = new ArrayList<>();
                    while (resultSet.next()) {
                        les_types.add(resultSet.getString("enumlabel"));
                    }

                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Types : " + cherche, sqle);
        }
        return les_types;
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

        try (Connection conn = ds.getConnection()){
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

    public boolean isThesoEnCours(String idTheso, String currentIdTheso) {
        boolean status = false;
        if (idTheso.equals(currentIdTheso)) {
            status = true;
        }
        return status;
    }

    public int getId_Alignement(Connection conn, String source) {
        int id_alignement = 0;
        try ( Statement stmt = conn.createStatement()) {
            String query = "Select id from alignement_source "
                    + " where source = '" + source + "'";

            try ( ResultSet rs = stmt.executeQuery(query)) {
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
        try ( Statement stmt = conn.createStatement()) {
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
     * Permet de faire un update d'un alignement_source
     *
     * @param ds
     * @param alignements
     * @param id
     */
    public void update_alignementSource(HikariDataSource ds, List<AlignementSource> alignements, int id) {
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                for (AlignementSource alignement : alignements) {
                    String query = "update alignement_source set "
                            + "source ='" + alignement.getSource()
                            + "', requete ='" + alignement.getRequete()
                            + "', type_rqt ='" + alignement.getTypeRequete()
                            + "', alignement_format='" + alignement.getAlignement_format()
                            + "', description ='" + alignement.getDescription()
                            + "' where id =" + id;
                    stmt.execute(query);
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while insert new Alignement : ", sqle);
        }
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
        try ( Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            // suppression des anciennes relations
            /*for (Map.Entry<String, String> auEntry : authorizedThesaurus) {
                if (!deleteSourceAlignementFromTheso(conn,
                        auEntry.getValue(), idAlignement)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
            }*/
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
     *
     * @param conn
     * @param idTheso
     * @param idAlignement
     * @return
     */
    private boolean deleteSourceAlignementFromTheso(Connection conn, String idTheso, int idAlignement) {

        boolean status = false;

        try ( Statement stmt = conn.createStatement()) {
            String query = "delete from thesaurus_alignement_source"
                    + " where id_alignement_source = " + idAlignement
                    + " and id_thesaurus = '" + idTheso + "'";

            stmt.executeUpdate(query);
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
        try ( Statement stmt = conn.createStatement()) {
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

    /**
     * permet d'effacer une Alignement
     *
     * @param ds
     * @param id
     */
    public void efaceAligSour(HikariDataSource ds, int id) {
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from alignement_source where id =" + id);
            }
        } catch (SQLException sqle) {
            log.error("Error while delete Alignement : ", sqle);
        }
    }

    /**
     * permet de savoir le group du concept idconcept
     *
     * @param ds
     * @param id_Theso
     * @param id_concept
     * @return
     */
    public String getGroupOfConcept(HikariDataSource ds, String id_Theso, String id_concept) {
        String group = "";
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "select  id_group from concept where id_thesaurus = '" + id_Theso
                        + "' and id_concept = '" + id_concept + "'";
                try ( ResultSet rs = stmt.executeQuery(query)) {
                    if (rs.next()) {
                        group = rs.getString("id_group");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while get it back group : ", sqle);
        }
        return group;
    }

    /**
     * permet de savoir si le concept idconcept a des enfants o pas
     *
     * @param ds
     * @param idtheso
     * @param idconcept
     * @return
     */
    public boolean isHaveChildren(HikariDataSource ds, String idtheso, String idconcept) {

        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                try ( ResultSet rs = stmt.executeQuery("select id_concept2 from hierarchical_relationship "
                        + "where id_thesaurus = '" + idtheso + "' and id_concept1='" + idconcept
                        + "' and role ='NT'")) {
                    if (rs.next()) {
                        status = true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while search children", sqle);
        }
        return status;
    }

    public boolean isAlignedWithThisSource(HikariDataSource ds, String id_Concept,
            String id_Theso, int id_alignement_source) {

        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                try ( ResultSet rs = stmt.executeQuery("select id from alignement "
                        + "where internal_id_thesaurus = '" + id_Theso
                        + "' and internal_id_concept='" + id_Concept
                        + "' and id_alignement_source =" + id_alignement_source)) {
                    if (rs.next()) {
                        status = true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while see it's alignée", sqle);
        }
        return status;
    }

    public AlignementPreferences getListPreferencesAlignement(HikariDataSource ds,
            String id_theso, int id_user, String id_concept_depart, int id_alignement_source) {

        AlignementPreferences alignementPreferences = new AlignementPreferences();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                try ( ResultSet rs = stmt.executeQuery("select * from alignement_preferences where id_thesaurus = '" + id_theso
                        + "' and id_user = " + id_user
                        + " and id_concept_depart ='" + id_concept_depart + "'"
                        + " and  id_alignement_source=" + id_alignement_source)) {

                    if (rs.next()) {
                        alignementPreferences.setId_concetp_depart(rs.getString("id_concept_depart"));
                        alignementPreferences.setId_concept_tratees(rs.getString("id_concept_tratees"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while get alignement's preferences", sqle);
        }
        return alignementPreferences;
    }

    public boolean validate_Preferences(HikariDataSource ds, String id_theso, int id_user,
            String id_concept_depart, ArrayList<String> listConceptTratees, int id_alignement_source) {
        if (!existPreferences(ds, id_theso, id_user, id_concept_depart, id_alignement_source)) {
            if (!insert_validate_Preferences(ds, id_theso, id_user, id_concept_depart, listConceptTratees, id_alignement_source)) {
                return false;
            } else {  
                return true;
            }
        } else if (!enregistreProgres(ds, id_theso, id_concept_depart, id_user, listConceptTratees, id_alignement_source)) {
            return false;
        }
        return true;
    }

    /**
     * reinitialize le workFlow
     *
     * @param ds
     * @param id_theso
     * @param id_user
     * @param id_concept_depart
     */
    public void init_preferences(HikariDataSource ds, String id_theso, int id_user, String id_concept_depart) {
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM alignement_preferences WHERE "
                        + "id_thesaurus = '" + id_theso + "'"
                        + " AND "
                        + "id_user = '" + id_user + "'"
                        + " AND "
                        + "id_concept_depart = '" + id_concept_depart + "'");
            }
        } catch (SQLException sqle) {
            log.error("Error while get alignement's preferences", sqle);
        }
    }

    private boolean existPreferences(HikariDataSource ds, String id_theso, int id_user, String id_concept_depart, int id_alignement_source) {
        boolean status = false;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                try ( ResultSet rs = stmt.executeQuery("SELECT id from alignement_preferences"
                        + " where id_thesaurus ='" + id_theso + "'"
                        + " and id_user= " + id_user
                        + " and id_concept_depart ='" + id_concept_depart + "'"
                        + " and id_alignement_source =" + id_alignement_source)) {
                    if (rs.next()) {
                        status = true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while see it's exist", sqle);
        }
        return status;
    }

    private boolean insert_validate_Preferences(HikariDataSource ds, String id_theso, int id_user,
            String id_concept_depart, ArrayList<String> listConceptTratees, int id_alignement_source) {

        String conceptstratees = "";
        for (String id : listConceptTratees) {
            conceptstratees += id + "#";
        }

        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.execute("insert into alignement_preferences "
                        + " (id_thesaurus, id_user, id_concept_depart, id_concept_tratees, id_alignement_source )"
                        + " values('" + id_theso + "'," + id_user + ",'" + id_concept_depart + "',"
                        + " '" + conceptstratees + "'," + id_alignement_source + ")");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while see it's alignée", sqle);
        }
        return status;
    }

    public boolean enregistreProgres(HikariDataSource ds, String id_theso, String id_concept,
            int id_user, ArrayList<String> listConceptTratees, int id_alignement_source) {

        String conceptstratees = "";
        for (String id : listConceptTratees) {
            conceptstratees += id + "#";
        }
        
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "Update alignement_preferences"
                        + " SET id_concept_tratees ='" + conceptstratees + "'"
                        + " where id_thesaurus ='" + id_theso + "'"
                        + " and id_user =" + id_user
                        + " and id_concept_depart ='" + id_concept + "'"
                        + " and  id_alignement_source =" + id_alignement_source;
                stmt.execute(query);
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while save progress", sqle);
        }
        return status;
    }

    /**
     * public boolean update_IdConceptInTable(HikariDataSource ds, String
     * id_theso, int id_user) { Statement stmt; Connection conn; ResultSet rs;
     * boolean status = false; try { // Get connection from pool conn =
     * ds.getConnection(); try { stmt = conn.createStatement(); try { stmt =
     * conn.createStatement();
     *
     * String query = "Update alignement_preferences set " +" id_concept = '0'"
     * + " where id_thesaurus = '" + id_theso + "'" + " and id_user = " +
     * id_user; stmt.executeUpdate(query); status = true; } finally {
     * stmt.close(); } } finally { conn.close(); } } catch (SQLException sqle) {
     * // Log exception log.error("Error while reinit id_concept", sqle); }
     * return status; }
     */
    public String getListIdCTrates(HikariDataSource ds, String id_theso, int id_user) {
        String trates = "";
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                try ( ResultSet rs = stmt.executeQuery("SELECT id_concept_tratees from alignement_preferences"
                        + " where id_thesaurus = '" + id_theso + "'"
                        + " and id_user = " + id_user)) {
                    if (rs.next()) {
                        trates = rs.getString("id_concept_tratees");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while reinit id_concept", sqle);
        }
        return trates;
    }

    /**
     * Change l'id d'un concept dans la table alignement
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws java.sql.SQLException
     */
    public void setIdConceptAlignement(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        try ( Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE alignement"
                    + " SET internal_id_concept = '" + newIdConcept + "'"
                    + " WHERE internal_id_concept = '" + idConcept + "'"
                    + " AND internal_id_thesaurus = '" + idTheso + "'");
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
