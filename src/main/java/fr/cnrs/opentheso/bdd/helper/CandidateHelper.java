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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeCandidatValue;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeMessageAdmin;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeProposition;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeTraductionCandidat;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.timeJob.LineCdt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miled.rousset
 */
public class CandidateHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    /**
     * ************************************************************
     * /**************************************************************
     * Nouvelles fonctions stables auteur Miled Rousset
     * /**************************************************************
     * /*************************************************************
     */
    
    /**
     * permet de réactiver un candidat s'il a été rejeté
     * @param ds
     * @param idTheso
     * @param idCandidat
     * @return 
     */
    public boolean reactivateRejectedCandidat (HikariDataSource ds, String idTheso, String idCandidat){
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("update candidat_status set id_status = 1" +
                        " WHERE id_concept = '" + idCandidat + "' and id_thesaurus = '" + idTheso + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List Group or Domain of thesaurus : " + idTheso, sqle);
        }
        return false;        
    }
    
    public ArrayList<String> getAllCandidatId(HikariDataSource ds, String idTheso) {

        ArrayList tabIdCandidat = new ArrayList();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept_candidat where id_thesaurus = '"
                        + idTheso + "'" + " order by id_concept ASC");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdCandidat.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List Group or Domain of thesaurus : " + idTheso, sqle);
        }
        return tabIdCandidat;
    }

    public int getCountOfCandidat(HikariDataSource ds, String idTheso) {

        int tot = 0;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select count(id_concept) from concept_candidat where id_thesaurus = '" + idTheso + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        tot = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting count of candidat of thesaurus : " + idTheso, sqle);
        }
        return tot;
    }

    public ArrayList<NodeCandidatValue> getOldCandidates(HikariDataSource ds, String idTheso) {
        ArrayList tabIdCandidat = new ArrayList();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept_candidat where id_thesaurus = '"
                        + idTheso + "'" + " order by id_concept ASC");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdCandidat.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List Group or Domain of thesaurus : " + idTheso, sqle);
        }
        return tabIdCandidat;
    }

    /**
     * ************************************************************
     * /**************************************************************
     * FIN des Nouvelles fonctions stables auteur Miled Rousset
     * /**************************************************************
     * /*************************************************************
     */
    /**
     * Cette fonction permet d'ajouter un candidat complet avec relations sans
     * les traductions
     *
     *
     * @param conn
     * @param lexical_value
     * @param idLang
     * @param idThesaurus
     * @param contributor
     * @param note
     * @param idParentConcept
     * @param idGroup
     * @return null si le term existe ou si erreur, sinon le numero de Concept
     */
    public String addCandidat_rollBack(Connection conn,
            String lexical_value,
            String idLang, String idThesaurus,
            int contributor, String note,
            String idParentConcept, String idGroup) {

        try {
            conn.setAutoCommit(false);

            CandidateHelper candidateHelper = new CandidateHelper();
            // controle si le term existe avant de rajouter un candidat
            if (candidateHelper.isCandidatExist_rollBack(conn, lexical_value, idThesaurus, idLang)) {
                return null;
            }

            String idConceptCandidat = addConceptCandidat_rollback(conn, idThesaurus);
            if (idConceptCandidat == null) {
                return null;
            }

            String idTermCandidat = candidateHelper.addTermCandidat_RollBack(conn, lexical_value, idLang, idThesaurus, contributor);
            if (idTermCandidat == null) {
                return null;
            }

            if (!addRelationConceptTermCandidat_RollBack(conn, idConceptCandidat,
                    idTermCandidat, idThesaurus)) {
                return null;
            }

            if (!candidateHelper.addPropositionCandidat_RollBack(conn,
                    idConceptCandidat, contributor, idThesaurus,
                    note, idParentConcept, idGroup)) {
                return null;
            }
            return idConceptCandidat;
        } catch (SQLException ex) {
            Logger.getLogger(CandidateHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Cette fonction permet d'ajouter une relation entre Concept_candidat et
     * terme_candidat
     *
     * @param conn
     * @param idConceptCandidat
     * @param idTermCandidat
     * @param idThesaurus
     * @return booelean
     */
    public boolean addRelationConceptTermCandidat_RollBack(Connection conn,
            String idConceptCandidat,
            String idTermCandidat, String idThesaurus) {

        boolean status = false;

        try ( Statement stmt = conn.createStatement()) {
            String query = "Insert into concept_term_candidat"
                    + "(id_concept, id_term, id_thesaurus)"
                    + " values ("
                    + "'" + idConceptCandidat + "'"
                    + ",'" + idTermCandidat + "'"
                    + ",'" + idThesaurus + "')";

            stmt.executeUpdate(query);
            status = true;
        } catch (SQLException sqle) {
            log.error("Error while adding Relation Candidat Term : " + idConceptCandidat, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet d'ajouter une relation entre Concept_candidat et
     * terme_candidat
     *
     * @param ds
     * @param idConceptCandidat
     * @param idTermCandidat
     * @param idThesaurus
     * @return booelean
     */
    public boolean addRelationConceptTermCandidat(HikariDataSource ds,
            String idConceptCandidat,
            String idTermCandidat, String idThesaurus) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into concept_term_candidat"
                            + "(id_concept, id_term, id_thesaurus)"
                            + " values ("
                            + "'" + idConceptCandidat + "'"
                            + ",'" + idTermCandidat + "'"
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
            log.error("Error while adding Relation Candidat Term : "
                    + idConceptCandidat, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet d'ajouter un Concept à la table Concept, en
     * paramètre un objet Classe Concept
     *
     * @param conn
     * @param idThesaurus
     * @return idConceptCandidat
     */
    public String addConceptCandidat_rollback(Connection conn,
            String idThesaurus) {

        String idConcept = null;
        ResultSet resultSet = null;

        try {
            try ( Statement stmt = conn.createStatement()) {
                try {
                    String query = "select max(id) from concept_candidat";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    resultSet.next();
                    int idNumerique = resultSet.getInt(1);
                    idConcept = "CA_" + (++idNumerique);
                    while (isCandidatExist(conn, idConcept, idThesaurus)) {
                        idConcept = "CA_" + (++idNumerique);
                    }
                    /**
                     * Ajout des informations dans la table Concept_candidat
                     */
                    query = "Insert into concept_candidat "
                            + "(id_concept, id_thesaurus)"
                            + " values ("
                            + "'" + idConcept + "'"
                            + ",'" + idThesaurus + "')";

                    stmt.executeUpdate(query);
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Concept_candidat : " + idConcept, sqle);
            idConcept = null;
        }
        return idConcept;
    }

    /**
     * Cette fonction permet d'ajouter un Concept à la table Concept, en
     * paramètre un objet Classe Concept
     *
     * @param ds
     * @param idThesaurus
     * @return idConceptCandidat
     */
    public String addConceptCandidat(HikariDataSource ds, String idThesaurus) {

        String idConcept = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select max(id) from concept_candidat");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    int idNumerique = resultSet.getInt(1);
                    idConcept = "CA_" + (++idNumerique);
                    while (isCandidatExist(ds.getConnection(), idConcept, idThesaurus)) {
                        idConcept = "CA_" + (++idNumerique);
                    }

                    //Ajout des informations dans la table Concept_candidat
                    stmt.executeUpdate("Insert into concept_candidat (id_concept, id_thesaurus)"
                            + " values ('" + idConcept + "','" + idThesaurus + "')");
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Concept*_candidat : " + idConcept, sqle);
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de supprimer un ConceptCandidat avec toutes les
     * relations
     *
     * @param ds
     * @param idConceptCandidat
     * @param idThesaurus
     * @return boolean
     */
    public boolean deleteConceptCandidat(HikariDataSource ds, String idConceptCandidat,
            String idThesaurus) {

        CandidateHelper candidateHelper = new CandidateHelper();
        if (!candidateHelper.deleteTermsCandidatsOfConcept(ds, idConceptCandidat, idThesaurus)) {
            return false;
        }
        return deleteThisConceptCandidat(ds, idConceptCandidat, idThesaurus);
    }

    /**
     * Cette fonction permet de récupérer un Concept par son id et son thésaurus
     * sous forme de classe Concept (sans les relations) ni le Terme
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean deleteThisConceptCandidat(HikariDataSource ds, String idConcept,
            String idThesaurus) {

        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from concept_candidat where"
                        + " id_thesaurus = '" + idThesaurus + "'"
                        + " and id_concept = '" + idConcept + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting this Concept_candidat : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de rajouter un term_candidat
     *
     * @param conn
     * @param lexical_value
     * @param idLang
     * @param idThesaurus
     * @param contributor
     * @return idConceptCandidat
     */
    public String addTermCandidat_RollBack(Connection conn, String lexical_value,
            String idLang, String idThesaurus, int contributor) {

        String idTerm = null;
        lexical_value = new StringPlus().convertString(lexical_value);

        try ( Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select max(id) from term_candidat");
            try ( ResultSet resultSet = stmt.getResultSet()) {
                resultSet.next();
                int idNumerique = resultSet.getInt(1);
                idTerm = "TC_" + (++idNumerique);

                /**
                 * Ajout des informations dans la table Concept
                 */
                stmt.executeUpdate("Insert into term_candidat (id_term, lexical_value, lang, "
                        + "id_thesaurus, contributor) values ('" + idTerm + "'"
                        + ",'" + lexical_value + "','" + idLang + "'"
                        + ",'" + idThesaurus + "'," + contributor + ")");
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Term_candidat  : " + idTerm, sqle);
            idTerm = null;
        }
        return idTerm;
    }

    /**
     * Cette fonction permet de rajouter un term_candidat
     *
     * @param ds
     * @param lexical_value
     * @param idLang
     * @param idThesaurus
     * @param contributor
     * @return idConceptCandidat
     */
    public String addTermCandidat(HikariDataSource ds, String lexical_value,
            String idLang, String idThesaurus, int contributor) {

        String idTerm = null;
        lexical_value = new StringPlus().convertString(lexical_value);

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select max(id) from term_candidat");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    int idNumerique = resultSet.getInt(1);
                    idTerm = "TC_" + (++idNumerique);

                    //Ajout des informations dans la table Concept
                    stmt.executeUpdate("Insert into term_candidat (id_term, lexical_value, lang, "
                            + "id_thesaurus, contributor) values ('" + idTerm + "'"
                            + ",'" + lexical_value + "','" + idLang + "'"
                            + ",'" + idThesaurus + "'," + contributor + ")");
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Term_candidat  : " + idTerm, sqle);
        }
        return idTerm;
    }

    /**
     * Cette fonction permet d'ajouter un message de justification sur un
     * candidat refusé
     *
     * @param ds
     * @param idConceptCandidat
     * @param message
     * @param adminId
     * @param idThesaurus
     * @return boolean
     */
    public boolean addAdminMessage(HikariDataSource ds, String idConceptCandidat,
            String idThesaurus, int adminId, String message) {

        boolean status = false;
        message = new StringPlus().convertString(message);

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Update concept_candidat set modified = current_date,"
                        + " admin_id = " + adminId + ", admin_message = '" + message + "'"
                        + " where id_concept = '" + idConceptCandidat + "'"
                        + " and id_thesaurus = '" + idThesaurus + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Admin Message of candidat  : " + idConceptCandidat, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de retourner le nombre de candidats d'un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class NodeMessageAdmin
     */
    public NodeMessageAdmin getMessageAdmin(HikariDataSource ds, String idThesaurus, String idConcept) {

        NodeMessageAdmin nodeMessageAdmin = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT users.username, users.id_user,"
                        + " concept_candidat.admin_message"
                        + " FROM concept_candidat, users WHERE"
                        + " concept_candidat.admin_id = users.id_user"
                        + " and concept_candidat.id_concept = '" + idConcept + "'"
                        + " and concept_candidat.id_thesaurus = '" + idThesaurus + "'");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeMessageAdmin = new NodeMessageAdmin();
                        nodeMessageAdmin.setId_user(resultSet.getInt("id_user"));
                        nodeMessageAdmin.setUser(resultSet.getString("username"));
                        nodeMessageAdmin.setMessage(resultSet.getString("admin_message"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Admin Message of candidat : " + idConcept, sqle);
        }
        return nodeMessageAdmin;
    }

    /**
     * Cette fonction permet de rajouter un term_candidat
     *
     * @param ds
     * @param status
     * @param idConceptCandidat
     * @param idThesaurus
     * @return boolean
     */
    public boolean updateCandidatStatus(HikariDataSource ds, String status,
            String idThesaurus, String idConceptCandidat) {

        boolean etat = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Update concept_candidat set status = '" + status + "',"
                        + " modified = current_date where id_concept = '" + idConceptCandidat + "'"
                        + " and id_thesaurus = '" + idThesaurus + "'");
                updateDateOfCandidat(conn, idConceptCandidat, idThesaurus);
                etat = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating Status of candidat  : " + idConceptCandidat, sqle);
        }
        return etat;
    }

    /**
     * Cette fonction permet de mettre à jour le commentaire d'un candidat, le
     * niveau et le groupe, cette modification est autorisée par propriétaire.
     *
     * @param ds
     * @param idCandidat
     * @param idUser
     * @param idThesaurus
     * @param note
     * @param idConceptParent
     * @param idGroup
     * @return boolean
     */
    public boolean updatePropositionCandidat(HikariDataSource ds, String idCandidat,
            int idUser, String idThesaurus, String note, String idConceptParent, String idGroup) {

        boolean etat = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Update proposition set note = '" + new StringPlus().convertString(note) + "',"
                        + " concept_parent = '" + idConceptParent + "', id_group = '" + idGroup + "',"
                        + " modified = current_date where id_concept = '" + idCandidat + "'"
                        + " and id_thesaurus = '" + idThesaurus + "' and id_user = " + idUser);
                updateDateOfCandidat(conn, idCandidat, idThesaurus);
                etat = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating proposition of candidat  : " + idCandidat, sqle);
        }
        return etat;
    }

    /**
     * Cette fonction permet de mettre à jour le nom d'un candidat qui vient
     * d'être déposé
     *
     * @param ds
     * @param idCandidat
     * @param idThesaurus
     * @param value
     * @return boolean
     */
    public boolean updateMotCandidat(HikariDataSource ds, String idCandidat, String idThesaurus, String value) {

        boolean etat = false;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String idTermCdt = getIdTermOfConceptCandidat(ds, idCandidat, idThesaurus);
                stmt.executeUpdate("Update term_candidat set lexical_value = '" + value + "',"
                        + " modified = current_date where id_term = '" + idTermCdt + "'"
                        + " and id_thesaurus = '" + idThesaurus + "'");
                updateDateOfCandidat(conn, idCandidat, idThesaurus);
                etat = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating proposition of candidat  : " + idCandidat, sqle);
        }
        return etat;
    }

    /**
     * Cette fonction permet de mettre à jour le status d'un candidat
     *
     * @param ds
     * @param idConceptCandidat
     * @param idThesaurus
     * @return idTermCandidat
     */
    public String getIdTermOfConceptCandidat(HikariDataSource ds,
            String idConceptCandidat, String idThesaurus) {

        String idTermCandidat = null;
        if (idConceptCandidat == null) {
            return null;
        }

        if (idConceptCandidat.isEmpty()) {
            return null;
        }

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_term FROM concept_term_candidat"
                        + " WHERE id_thesaurus = '" + idThesaurus + "'"
                        + " and id_concept = '" + idConceptCandidat + "'");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idTermCandidat = resultSet.getString("id_term");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idTermCandidat of idConceptCandidat : " + idConceptCandidat, sqle);
        }
        return idTermCandidat;
    }

    /**
     * Cette fonction permet de retourner l'Id du candidat d'après son nom
     *
     * @param ds
     * @param title
     * @param idThesaurus
     * @return idTermCandidat
     */
    public String getIdCandidatFromTitle(HikariDataSource ds, String title, String idThesaurus) {

        String idTermCandidat = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT concept_candidat.id_concept"
                        + " FROM concept_candidat, concept_term_candidat, term_candidat"
                        + " WHERE concept_candidat.id_concept = concept_term_candidat.id_concept"
                        + " AND concept_term_candidat.id_thesaurus = term_candidat.id_thesaurus"
                        + " AND term_candidat.id_term = concept_term_candidat.id_term"
                        + " AND term_candidat.id_thesaurus = '" + idThesaurus + "'"
                        + " AND term_candidat.lexical_value = '" + title + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idTermCandidat = resultSet.getString("id_concept");
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idCandidat from candidat value : " + title, sqle);
        }
        return idTermCandidat;
    }

    /**
     * Cette fonction permet de supprimer un term_candidat
     *
     * @param ds
     * @param idConceptCandidat
     * @param idLang
     * @param idThesaurus
     * @param contributor
     * @return boolean
     */
    public boolean deleteTraductionTermCandidat(HikariDataSource ds, String idConceptCandidat,
            String idLang, String idThesaurus, int contributor) {

        String idTermCandidat;
        boolean status = false;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                idTermCandidat = getIdTermOfConceptCandidat(ds, idConceptCandidat, idThesaurus);
                if (idTermCandidat == null) {
                    return false;
                }

                stmt.executeUpdate("delete from term_candidat where id_thesaurus = '" + idThesaurus + "'"
                        + " and id_term = '" + idTermCandidat + "'"
                        + " and lang = '" + idLang + "'"
                        + " and contributor = '" + contributor + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting Term_candidat of conceptCandidat : " + idConceptCandidat, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de supprimer un term_candidat
     *
     * @param ds
     * @param idConceptCandidat
     * @param idThesaurus
     * @return boolean
     */
    public boolean deleteTermsCandidatsOfConcept(HikariDataSource ds,
            String idConceptCandidat, String idThesaurus) {

        String idTermCandidat = null;
        boolean status = false;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                idTermCandidat = getIdTermOfConceptCandidat(ds, idConceptCandidat, idThesaurus);
                if (idTermCandidat == null) {
                    return false;
                }

                stmt.executeUpdate("delete from term_candidat where id_thesaurus = '" + idThesaurus + "'"
                        + " and id_term = '" + idTermCandidat + "'");

                stmt.executeUpdate("delete from concept_term_candidat where id_thesaurus = '" + idThesaurus + "'"
                        + " and id_concept = '" + idConceptCandidat + "'");

                stmt.executeUpdate("delete from proposition where id_thesaurus = '" + idThesaurus + "'"
                        + " and id_concept = '" + idConceptCandidat + "'");

                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting Term_candidat of conceptCandidat : " + idConceptCandidat, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de rajouter une traduction pour un term_candidat
     *
     * @param ds
     * @param idConcept
     * @param lexical_value
     * @param idLang
     * @param idThesaurus
     * @param contributor
     * @return idConceptCandidat
     */
    public boolean addTermCandidatTraduction(HikariDataSource ds, String idConcept, String lexical_value,
            String idLang, String idThesaurus, int contributor) {

        boolean status = false;
        String idTermCandidat = null;
        lexical_value = new StringPlus().convertString(lexical_value);

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                idTermCandidat = getIdTermOfConceptCandidat(ds, idConcept, idThesaurus);
                if (idTermCandidat == null) {
                    return false;
                }

                stmt.executeUpdate("Insert into term_candidat (id_term, lexical_value, lang, id_thesaurus, contributor)"
                        + " values ('" + idTermCandidat + "','" + lexical_value + "','" + idLang + "'"
                        + ",'" + idThesaurus + "'," + contributor + ")");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Traduction of Term_candidat  : " + idTermCandidat, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de rajouter une proposition de candidat dans la
     * table propositon
     *
     * @param conn
     * @param idConcept
     * @param idUser
     * @param idThesaurus
     * @param note
     * @param idConceptParent
     * @param idGroup
     * @return idConceptCandidat
     */
    public boolean addPropositionCandidat_RollBack(Connection conn, String idConcept, int idUser,
            String idThesaurus, String note, String idConceptParent, String idGroup) {

        note = new StringPlus().convertString(note);
        boolean status = false;

        try ( Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Insert into proposition (id_concept, id_user, id_thesaurus, note, concept_parent,"
                    + " id_group) values ('" + idConcept + "'," + idUser + ",'" + idThesaurus + "'"
                    + ",'" + note + "','" + idConceptParent + "','" + idGroup + "')");
            updateDateOfCandidat(conn, idConcept, idThesaurus);
            status = true;
        } catch (SQLException sqle) {
            log.error("Error while adding Proposition Candidat  : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * cette fonction permet de mettre à jour la date de modification du
     * candidat
     */
    private boolean updateDateOfCandidat(Connection conn, String idConcept, String idThesaurus) {

        try ( Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Update concept_candidat set modified = now() where id_concept = '"
                    + idConcept + "' AND id_thesaurus = '" + idThesaurus + "'");
            return true;
        } catch (SQLException sqle) {
            log.error("Error while uddating date of Candidat : " + idConcept);
        }
        return false;
    }

    /**
     * Cette fonction permet de rajouter une proposition de candidat dans la
     * table propositon
     *
     * @param ds
     * @param idConcept
     * @param idUser
     * @param idThesaurus
     * @param note
     * @param idConceptParent
     * @param idGroup
     * @return idConceptCandidat
     */
    public boolean addPropositionCandidat(HikariDataSource ds, String idConcept, int idUser,
            String idThesaurus, String note, String idConceptParent, String idGroup) {

        note = new StringPlus().convertString(note);
        boolean status = false;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into proposition (id_concept, id_user, id_thesaurus, note, concept_parent,"
                        + " id_group) values ('" + idConcept + "'," + idUser + ",'" + idThesaurus + "'"
                        + ",'" + note + "','" + idConceptParent + "','" + idGroup + "')");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Proposition Candidat  : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de supprimer une proposition de candidat dans la
     * table propositon
     *
     * @param ds
     * @param idConcept
     * @param idUser
     * @param idThesaurus
     *
     * @return idConceptCandidat
     */
    public boolean deletePropositionCandidat(HikariDataSource ds, String idConcept, int idUser, String idThesaurus) {

        boolean status = false;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from proposition where id_concept ='" + idConcept + "'"
                        + " and id_user =" + idUser + " and id_thesaurus = '" + idThesaurus + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting Proposition candidat  : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Permet de retourner une ArrayList de NodeConceptCandidat par thésaurus Si
     * le Candidat n'est pas traduit dans la langue en cours, on récupère
     * l'identifiant pour l'afficher à la place
     *
     * @param ds le pool de connexion
     * @param idConcept
     * @param idThesaurus
     * @param idUser
     * @return Objet Class ArrayList NodeProposition
     */
    public NodeProposition getNodePropositionOfUser(HikariDataSource ds,
            String idConcept, String idThesaurus, int idUser) {

        NodeProposition nodeProposition = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT proposition.id_user,"
                        + " users.username, proposition.note,"
                        + " proposition.created,"
                        + " proposition.modified,"
                        + " proposition.concept_parent,"
                        + " proposition.id_group"
                        + " FROM proposition, users WHERE "
                        + " proposition.id_user = users.id_user"
                        + " and proposition.id_concept = '" + idConcept + "'"
                        + " and proposition.id_thesaurus = '" + idThesaurus + "'"
                        + " and proposition.id_user = " + idUser);

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        if (resultSet.next()) {
                            nodeProposition = new NodeProposition();
                            nodeProposition.setId_user(resultSet.getInt("id_user"));
                            nodeProposition.setUser(resultSet.getString("username"));
                            nodeProposition.setNote(resultSet.getString("note"));
                            nodeProposition.setCreated(resultSet.getDate("created"));
                            nodeProposition.setModified(resultSet.getDate("modified"));
                            nodeProposition.setIdConceptParent(resultSet.getString("concept_parent"));
                            nodeProposition.setIdGroup(resultSet.getString("id_group"));
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List of node Proposition Candidats of Concept Candidat : " + idConcept, sqle);
        }
        return nodeProposition;
    }

    /**
     * Permet de retourner une ArrayList de NodeUser par thésaurus et Concept
     * c'est la liste des personnes qui ont déposé ce candidat
     *
     * @param ds le pool de connexion
     * @param idConcept
     * @param idThesaurus
     * @return Objet Class ArrayList NodeUSer
     */
    public ArrayList<NodeUser> getListUsersOfCandidat(HikariDataSource ds, String idConcept, String idThesaurus) {

        ArrayList<NodeUser> nodeUserList = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT users.username, users.id_user, users.mail, proposition.modified"
                        + " FROM proposition, users"
                        + " WHERE proposition.id_user = users.id_user"
                        + " AND proposition.id_concept = '" + idConcept + "'"
                        + " AND proposition.id_thesaurus = '" + idThesaurus + "'"
                        + " order By proposition.modified DESC;");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        nodeUserList = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeUser nodeUser = new NodeUser();
                            nodeUser.setIdUser(resultSet.getInt("id_user"));
                            nodeUser.setName(resultSet.getString("username"));
                            nodeUser.setMail(resultSet.getString("mail"));
                            nodeUserList.add(nodeUser);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List of nodeUsersCandidat of ConceptCandidat : " + idConcept, sqle);
        }
        return nodeUserList;
    }

    /**
     * Permet de retourner toutes les tradcutions pour un candidat au format
     * NodeTermTraduction
     *
     *
     * @param ds le pool de connexion
     * @param idCandidat
     * @param idThesaurus
     * @return Objet Class ArrayList nodeTraductionCandidat
     */
    public ArrayList<NodeTermTraduction> getAllTraductionOfCandidat(HikariDataSource ds,
            String idCandidat, String idThesaurus) {

        ArrayList<NodeTermTraduction> nodeTermTraductions = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lexical_value, lang from term_candidat, concept_term_candidat"
                        + " where concept_term_candidat.id_term = term_candidat.id_term AND"
                        + " concept_term_candidat.id_thesaurus = term_candidat.id_thesaurus AND"
                        + " term_candidat.id_thesaurus = '" + idThesaurus + "' AND "
                        + " concept_term_candidat.id_concept = '" + idCandidat + "'");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeTermTraduction nodeTermTraduction = new NodeTermTraduction();
                        nodeTermTraduction.setLang(resultSet.getString("lang"));
                        nodeTermTraduction.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeTermTraductions.add(nodeTermTraduction);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Traductions of Candidat : " + idCandidat, sqle);
        }
        return nodeTermTraductions;

    }

    /**
     * Permet de retourner une ArrayList de nodeTraductionCandidat par thésaurus
     *
     * @param ds le pool de connexion
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet Class ArrayList nodeTraductionCandidat
     */
    public ArrayList<NodeTraductionCandidat> getNodeTraductionCandidat(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

        ArrayList<NodeTraductionCandidat> nodeTraductionCandidatList = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String idTermCandidat = getIdTermOfConceptCandidat(ds, idConcept, idThesaurus);
                stmt.executeQuery("SELECT term_candidat.lexical_value, term_candidat.lang,"
                        + " users.username, users.id_user"
                        + " FROM users, term_candidat WHERE"
                        + " term_candidat.contributor = users.id_user"
                        + " and term_candidat.lang != '" + idLang + "'"
                        + " and term_candidat.id_thesaurus = '" + idThesaurus + "'"
                        + " and term_candidat.id_term = '" + idTermCandidat + "'"
                        + " order by users.username ASC");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        nodeTraductionCandidatList = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeTraductionCandidat nodeTraductionCandidat = new NodeTraductionCandidat();
                            nodeTraductionCandidat.setIdLang(resultSet.getString("lang"));
                            nodeTraductionCandidat.setTitle(resultSet.getString("lexical_value"));
                            nodeTraductionCandidat.setUseId(resultSet.getInt("id_user"));
                            nodeTraductionCandidat.setUser(resultSet.getString("username"));
                            nodeTraductionCandidatList.add(nodeTraductionCandidat);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Traductions of Candidat : " + idConcept, sqle);
        }
        return nodeTraductionCandidatList;
    }

    /**
     * Permet de retourner une ArrayList de NodeConceptCandidat par thésaurus,
     * c'est la liste des candidats en attente (status = a) Si le Candidat n'est
     * pas traduit dans la langue en cours, on récupère l'identifiant pour
     * l'afficher à la place
     *
     * @param ds le pool de connexion
     * @param idThesaurus
     * @param idLang
     * @return Objet Class ArrayList NodeCandidatValue
     */
    public ArrayList<NodeCandidatValue> getListCandidatsWaiting(HikariDataSource ds, String idThesaurus, String idLang) {

        ArrayList<NodeCandidatValue> nodeCandidatLists = null;
        ArrayList tabIdConcept = new ArrayList();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept_candidat where id_thesaurus = '" + idThesaurus + "'"
                        + " and status ='a' order by modified DESC");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                    nodeCandidatLists = new ArrayList<>();
                    for (Object tabIdConcept1 : tabIdConcept) {
                        NodeCandidatValue nodeCandidatValue;
                        nodeCandidatValue = getThisCandidat(ds, tabIdConcept1.toString(), idThesaurus, idLang);
                        if (nodeCandidatValue == null) {
                            return null;
                        }
                        nodeCandidatValue.setEtat("a");
                        nodeCandidatValue.setNbProp(getNbPropCandidat(ds, idThesaurus, tabIdConcept1.toString()));
                        nodeCandidatLists.add(nodeCandidatValue);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List Group or Domain of thesaurus : " + idThesaurus, sqle);
        }
        return nodeCandidatLists;
    }

    /**
     * Permet de retourner une ArrayList de NodeConceptCandidat par thésaurus et
     * par id_user c'est la liste des candidats en attente (status = a) Si le
     * Candidat n'est pas traduit dans la langue en cours, on récupère
     * l'identifiant pour l'afficher à la place
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param id_user
     * @return
     */
    public ArrayList<NodeCandidatValue> getListMyCandidatsWait(HikariDataSource ds, String idThesaurus,
            String idLang, Integer id_user) {

        ArrayList<NodeCandidatValue> nodeCandidatLists = null;
        ArrayList tabIdConcept = new ArrayList();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept_candidat.id_concept from concept_candidat, proposition"
                        + " where concept_candidat.id_concept = proposition.id_concept and"
                        + " concept_candidat.id_thesaurus= proposition.id_thesaurus"
                        + " and proposition.id_user =" + id_user + " and proposition.id_thesaurus ='" + idThesaurus
                        + "' and concept_candidat.status='a'");
                try ( ResultSet resultSet = stmt.getResultSet()) {

                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }

                    nodeCandidatLists = new ArrayList<>();
                    for (Object tabIdConcept1 : tabIdConcept) {
                        NodeCandidatValue nodeCandidatValue;
                        nodeCandidatValue = getThisCandidat(ds, tabIdConcept1.toString(), idThesaurus, idLang);
                        if (nodeCandidatValue == null) {
                            return null;
                        }
                        nodeCandidatValue.setEtat("a");
                        nodeCandidatValue.setNbProp(getNbPropCandidat(ds, idThesaurus, tabIdConcept1.toString()));
                        nodeCandidatLists.add(nodeCandidatValue);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List Group or Domain of thesaurus : " + idThesaurus, sqle);
        }
        return nodeCandidatLists;

    }

    /**
     * Permet de retourner une ArrayList de NodeConceptCandidat par thésaurus,
     * c'est la liste des candidats archivés tous les status sauf a et v
     * (a=attente, v=validé) v=validé,i=insérré,r=refusé) Si le Candidat n'est
     * pas traduit dans la langue en cours, on récupère l'identifiant pour
     * l'afficher à la place
     *
     * @param ds le pool de connexion
     * @param idThesaurus
     * @param idLang
     * @return Objet Class ArrayList NodeCandidatValue
     */
    public ArrayList<NodeCandidatValue> getListCandidatsArchives(HikariDataSource ds, String idThesaurus, String idLang) {

        ArrayList<NodeCandidatValue> nodeCandidatLists = null;
        ArrayList tabIdConcept = new ArrayList();
        ArrayList tabStatus = new ArrayList();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {

                stmt.executeQuery("select id_concept, status from concept_candidat where id_thesaurus = '"
                        + idThesaurus + "' and status != 'a' and status != 'v'"
                        + " order by modified DESC");

                try ( ResultSet resultSet = stmt.getResultSet()) {

                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                        tabStatus.add(resultSet.getString("status"));
                    }
                    nodeCandidatLists = new ArrayList<>();

                    int i = 0;
                    for (Object tabIdConcept1 : tabIdConcept) {
                        NodeCandidatValue nodeCandidatValue = getThisCandidat(ds, tabIdConcept1.toString(), idThesaurus, idLang);
                        if (nodeCandidatValue == null) {
                            return null;
                        }
                        nodeCandidatValue.setEtat(tabStatus.get(i++).toString());
                        nodeCandidatValue.setNbProp(getNbPropCandidat(ds, idThesaurus, tabIdConcept1.toString()));
                        nodeCandidatLists.add(nodeCandidatValue);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List Group or Domain of thesaurus : " + idThesaurus, sqle);
        }
        return nodeCandidatLists;
    }

    /**
     * Permet de retourner une ArrayList de NodeConceptCandidat par thésaurus,
     * c'est la liste des candidats validé mais pas encore insérré dans les
     * thésaurus (status = v) Si le Candidat n'est pas traduit dans la langue en
     * cours, on récupère l'identifiant pour l'afficher à la place
     *
     * @param ds le pool de connexion
     * @param idThesaurus
     * @param idLang
     * @return Objet Class ArrayList NodeCandidatValue
     */
    public ArrayList<NodeCandidatValue> getListCandidatsValidated(HikariDataSource ds,
            String idThesaurus, String idLang) {

        ArrayList<NodeCandidatValue> nodeCandidatLists = null;
        ArrayList tabIdConcept = new ArrayList();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept_candidat where id_thesaurus = '" + idThesaurus
                        + "' and status = 'v' order by modified DESC");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                    nodeCandidatLists = new ArrayList<>();
                    for (Object tabIdConcept1 : tabIdConcept) {
                        NodeCandidatValue nodeCandidatValue;
                        nodeCandidatValue = getThisCandidat(ds, tabIdConcept1.toString(), idThesaurus, idLang);
                        if (nodeCandidatValue == null) {
                            return null;
                        }
                        nodeCandidatValue.setEtat("v");
                        nodeCandidatValue.setNbProp(getNbPropCandidat(ds, idThesaurus, tabIdConcept1.toString()));
                        nodeCandidatLists.add(nodeCandidatValue);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List Group or Domain of thesaurus : " + idThesaurus, sqle);
        }
        return nodeCandidatLists;
    }

    /**
     * $$$$$$$ deprecated $$$$$$$ Cette fonction permet de récupérer la liste
     * des candidats
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet class NodeCandidatValue
     */
    public NodeCandidatValue getThisCandidatList(HikariDataSource ds, String idConcept,
            String idThesaurus, String idLang) {

        NodeCandidatValue nodeCandidatList = null;

        if (isTraductionExistOfCandidat(ds, idConcept, idThesaurus, idLang)) {
            try ( Connection conn = ds.getConnection()) {
                try ( Statement stmt = conn.createStatement()) {
                    stmt.executeQuery("SELECT DISTINCT term_candidat.lexical_value,"
                            + " concept_candidat.status FROM"
                            + " term_candidat, concept_term_candidat, concept_candidat"
                            + " WHERE concept_term_candidat.id_term = term_candidat.id_term"
                            + " and concept_term_candidat.id_concept = concept_candidat.id_concept"
                            + " and concept_term_candidat.id_concept ='" + idConcept + "'"
                            + " and term_candidat.lang = '" + idLang + "'"
                            + " and term_candidat.id_thesaurus = '" + idThesaurus + "'"
                            + " order by lexical_value DESC");
                    try ( ResultSet resultSet = stmt.getResultSet()) {
                        if (resultSet != null) {
                            while (resultSet.next()) {
                                nodeCandidatList = new NodeCandidatValue();
                                nodeCandidatList.setValue(resultSet.getString("lexical_value"));
                                nodeCandidatList.setIdConcept(idConcept);
                                nodeCandidatList.setEtat(resultSet.getString("status"));
                                nodeCandidatList.setNbProp(getNbPropCandidat(ds, idThesaurus, idConcept));
                            }
                        }
                    }
                }
            } catch (SQLException sqle) {
                log.error("Error while getting Concept : " + idConcept, sqle);
            }
        } else {
            try ( Connection conn = ds.getConnection()) {
                try ( Statement stmt = conn.createStatement()) {
                    stmt.executeQuery("SELECT concept_candidat.id_concept,"
                            + " concept_candidat.status FROM"
                            + " concept_candidat"
                            + " WHERE concept_candidat.id_concept ='" + idConcept + "'"
                            + " and concept_candidat.id_thesaurus = '" + idThesaurus + "'");
                    try ( ResultSet resultSet = stmt.getResultSet()) {
                        if (resultSet != null) {
                            while (resultSet.next()) {
                                nodeCandidatList = new NodeCandidatValue();
                                nodeCandidatList.setValue("");
                                nodeCandidatList.setIdConcept(idConcept);
                                nodeCandidatList.setEtat(resultSet.getString("status"));
                                nodeCandidatList.setNbProp(getNbPropCandidat(ds, idThesaurus, idConcept));
                            }
                        }

                    }
                }
            } catch (SQLException sqle) {
                log.error("Error while getting Concept : " + idConcept, sqle);
            }

        }
        return nodeCandidatList;
    }

    /**
     * Cette fonction permet de récupérer un candidat avec sa traduction, sinon,
     * son identifiant
     *
     * @param ds
     * @param idCandidat
     * @param idThesaurus
     * @param idLang
     * @return Objet class NodeCandidatValue
     */
    public NodeCandidatValue getThisCandidat(HikariDataSource ds, String idCandidat,
            String idThesaurus, String idLang) {

        NodeCandidatValue nodeCandidatList = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT term_candidat.lexical_value FROM concept_term_candidat, term_candidat"
                        + " WHERE concept_term_candidat.id_term = term_candidat.id_term"
                        + " AND concept_term_candidat.id_concept = '" + idCandidat + "'"
                        + " AND term_candidat.lang = '" + idLang + "'"
                        + " AND term_candidat.id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeCandidatList = new NodeCandidatValue();
                        nodeCandidatList.setValue(resultSet.getString("lexical_value").trim());
                        nodeCandidatList.setIdConcept(idCandidat);
                    } else {
                        nodeCandidatList = new NodeCandidatValue();
                        nodeCandidatList.setValue("");
                        nodeCandidatList.setIdConcept(idCandidat);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Concept : " + idCandidat, sqle);
        }

        return nodeCandidatList;
    }

    /**
     * Cette fonction permet de retourner le nombre de candidats d'un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class NodeConceptTree
     */
    public int getNbPropCandidat(HikariDataSource ds, String idThesaurus, String idConcept) {

        int count = 0;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select count(id_concept) from proposition where id_concept = '"
                        + idConcept + "' AND id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        resultSet.next();
                        if (resultSet.getInt(1) != 0) {
                            count = resultSet.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting count of candidat of Concept : " + idConcept, sqle);
        }
        return count;
    }

    /**
     * Cette fonction permet de savoir si le terme existe ou non
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet class NodeConceptTree
     */
    public boolean isTraductionExistOfCandidat(HikariDataSource ds, String idConcept,
            String idThesaurus, String idLang) {

        boolean existe = false;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select term_candidat.id_term from term_candidat, concept_term_candidat"
                        + " where term_candidat.id_term = concept_term_candidat.id_term and"
                        + " concept_term_candidat.id_concept = '" + idConcept + "'"
                        + " and term_candidat.lang = '" + idLang + "'"
                        + " and term_candidat.id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        resultSet.next();
                        if (resultSet.getRow() == 0) {
                            existe = false;
                        } else {
                            existe = true;
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Traduction of Candidat exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le Candidat existe ou non
     *
     * @param conn
     * @param title
     * @param idThesaurus
     * @param idLang
     * @return boolean
     */
    public boolean isCandidatExist_rollBack(Connection conn, String title, String idThesaurus, String idLang) {

        boolean existe = false;
        StringPlus stringPlus = new StringPlus();
        title = stringPlus.addQuotes(title);

        try ( Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select id_term from term_candidat where unaccent_string(lexical_value) ilike "
                    + "unaccent_string('" + title + "')  and lang = '" + idLang
                    + "' and id_thesaurus = '" + idThesaurus + "'");
            try ( ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet != null) {
                    resultSet.next();
                    if (resultSet.getRow() == 0) {
                        existe = false;
                    } else {
                        existe = true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Title of Candidat exist : " + title, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le Candidat existe ou non
     *
     * @param ds
     * @param idCandidat
     * @param idThesaurus
     * @param idUser
     * @return boolean
     */
    public boolean setStatusCandidatToInserted(HikariDataSource ds, String idCandidat, String idThesaurus, int idUser) {

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Update concept_candidat set status = 'i'"
                        + " where id_concept = '" + idCandidat + "'"
                        + " and id_thesaurus = '" + idThesaurus + "'");
                updateDateOfCandidat(conn, idCandidat, idThesaurus);
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Title of Candidat exist : " + sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de savoir si le Candidat existe ou non
     *
     * @param ds
     * @param title
     * @param idThesaurus
     * @param idLang
     * @return boolean
     */
    public boolean isCandidatExist(HikariDataSource ds, String title, String idThesaurus, String idLang) {

        boolean existe = false;
        StringPlus stringPlus = new StringPlus();
        title = stringPlus.addQuotes(title);

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_term from term_candidat where unaccent_string(lexical_value) ilike "
                        + "unaccent_string('" + title + "')  and lang = '" + idLang
                        + "' and id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Title of Candidat exist : " + title, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si l'id du Candidat existe, si oui, on
     * l'incrémente
     *
     * @param conn
     * @param idCandidat
     * @param idThesaurus
     * @return boolean
     */
    public boolean isCandidatExist(Connection conn, String idCandidat, String idThesaurus) {

        boolean existe = false;

        try ( Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select id_concept from concept_candidat where " + "id_concept = '"
                    + idCandidat + "'" + " and id_thesaurus = '" + idThesaurus + "'");
            try ( ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet.next()) {
                    existe = resultSet.getRow() != 0;
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Title of Candidat exist : ", sqle);
        }

        return existe;
    }

    /**
     * getInsertedValidedRefusedCdtDuingPeriod #JM Méthode qui fait une requête
     * sur la BDD, pour récupérer des candidats insérés validés ou refusés entre
     * deux dates, pour un thésaurus donné
     *
     * retourne une arrayList String, dont les lignes sont les valeurs des
     * candidats sous forme de tableau html
     *
     * @param ds
     * @param debut
     * @param fin
     * @param idThesaurus
     *
     * @return
     */
    public ArrayList<String> getInsertedValidedRefusedCdtDuringPeriod(HikariDataSource ds, Date debut, Date fin, String idThesaurus) {
        ArrayList<String> cdtList = new ArrayList<>();
        try ( Connection conn = ds.getConnection()) {
            try ( PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT concept_candidat.id_concept,concept_candidat.id_thesaurus,"
                    + "concept_candidat.created,concept_candidat.modified,concept_candidat.status,concept_candidat.admin_message,"
                    + " concept_term_candidat.id_term,term_candidat.lexical_value,"
                    + " proposition.id_user, thesaurus_label.title"
                    + " FROM concept_candidat "
                    + " INNER JOIN concept_term_candidat ON concept_candidat.id_concept=concept_term_candidat.id_concept "
                    + " INNER JOIN term_candidat ON concept_term_candidat.id_term=term_candidat.id_term"
                    + " INNER JOIN proposition ON concept_candidat.id_concept=proposition.id_concept"
                    + " INNER JOIN thesaurus_label ON concept_term_candidat.id_thesaurus=thesaurus_label.id_thesaurus "
                    + " WHERE concept_candidat.id_thesaurus=?"
                    + " AND (concept_candidat.status='i' OR concept_candidat.status='v' OR concept_candidat.status='r')"
                    + " AND( ( concept_candidat.created BETWEEN  ? AND  ? )"
                    + " OR ( concept_candidat.modified BETWEEN  ? AND  ? ) )")) {
                stmt.setString(1, idThesaurus);
                java.sql.Date d1 = new java.sql.Date(debut.getTime());
                java.sql.Date d2 = new java.sql.Date(fin.getTime() + (1000 * 60 * 60 * 24));
                stmt.setDate(2, d1);
                stmt.setDate(3, d2);
                stmt.setDate(4, d1);
                stmt.setDate(5, d2);
                try ( ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LineCdt lCdt = new LineCdt();
                        lCdt.setId_thesaurus(rs.getString("id_thesaurus"));
                        lCdt.setTitle_thesaurus(rs.getString("title"));
                        lCdt.setId_concept(rs.getString("id_concept"));
                        lCdt.setValeur_lexical(rs.getString("lexical_value"));
                        lCdt.setCreated(rs.getDate("created"));
                        lCdt.setModified(rs.getDate("modified"));
                        lCdt.setAdmin_message(rs.getString("admin_message"));
                        lCdt.setStatus(rs.getString("status"));
                        lCdt.setNote(rs.getString("note"));
                        cdtList.add(lCdt.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            log.error("error while getting database query on Valided and Inserted candidat", e);
        }
        return cdtList;
    }

    /**
     * getListOfCdtDuringPeriod
     *
     * Permet de récupérer la liste des candidats entre deux dates
     *
     * retourne une arrayList String ou chaque ligne donne les valeurs associés
     * à un candidat dans un tableau html #JM
     *
     * @param idTheso
     * @param d1
     * @param d2
     * @param poolConnexion
     * @return modifié par #MR
     */
    public ArrayList<String> getListOfCdtDuringPeriod(String idTheso, Date d1, Date d2, HikariDataSource poolConnexion) {

        ArrayList<String> listCdt = new ArrayList<>();
        java.sql.Date d11 = new java.sql.Date(d1.getTime());
        java.sql.Date d21 = new java.sql.Date(d2.getTime() + (1000 * 60 * 60 * 24));

        try ( Connection conn = poolConnexion.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "SELECT concept_candidat.id_concept, concept_candidat.id_thesaurus,"
                        + "  concept_candidat.created, concept_candidat.modified, concept_candidat.admin_message,"
                        + "  concept_candidat.status"
                        + " FROM concept_candidat"
                        + " WHERE concept_candidat.id_thesaurus = '" + idTheso
                        + "' AND concept_candidat.status = 'a' AND ("
                        + "  concept_candidat.created BETWEEN '" + d11 + "' AND '" + d21 + "' OR"
                        + "  concept_candidat.modified BETWEEN '" + d11 + "' AND '" + d21 + "');";

                stmt.executeQuery(query);

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        LineCdt lCdt = new LineCdt();
                        lCdt.setId_thesaurus(resultSet.getString("id_thesaurus"));
                        lCdt.setTitle_thesaurus("");
                        lCdt.setId_concept(resultSet.getString("id_concept"));
                        lCdt.setCreated(resultSet.getDate("created"));
                        lCdt.setModified(resultSet.getDate("modified"));
                        lCdt.setAdmin_message(resultSet.getString("admin_message"));
                        lCdt.setStatus(resultSet.getString("status"));
                        lCdt.setNodeProposition(
                                getAllPropositionsOfCandidat(poolConnexion,
                                        resultSet.getString("id_concept"),
                                        idTheso));
                        lCdt.setNodeTermTraductions(getAllTraductionOfCandidat(poolConnexion,
                                resultSet.getString("id_concept"), idTheso));
                        listCdt.add(lCdt.getMessage());
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting list of candidat between 2 dates of thesaurus : " + idTheso, sqle);
        }
        return listCdt;
    }

    /**
     * Permet de retourner la liste de toutes les propositions sur un candidat
     *
     * @param ds
     * @param idCandidat
     * @param idThesaurus
     * @return
     */
    public ArrayList<NodeProposition> getAllPropositionsOfCandidat(HikariDataSource ds, String idCandidat, String idThesaurus) {

        ArrayList<NodeProposition> nodePropositions = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {

                stmt.executeQuery("SELECT proposition.concept_parent, proposition.id_group, proposition.note, users.username "
                        + "FROM proposition, users "
                        + "WHERE users.id_user = proposition.id_user "
                        + "AND proposition.id_concept = '" + idCandidat
                        + "' AND proposition.id_thesaurus = '" + idThesaurus + "';");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeProposition nodeProposition = new NodeProposition();
                        if (!resultSet.getString("concept_parent").isEmpty()) {
                            nodeProposition.setLabelConceptParent(new ConceptHelper().getLexicalValueOfConcept(ds, resultSet.getString("concept_parent"),
                                    idThesaurus, "fr"));
                        }
                        nodeProposition.setIdConceptParent(resultSet.getString("concept_parent"));
                        nodeProposition.setIdGroup(resultSet.getString("id_group"));
                        nodeProposition.setUser(resultSet.getString("username"));
                        nodeProposition.setNote(resultSet.getString("note"));
                        nodePropositions.add(nodeProposition);
                    }

                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id of Candidat exist : " + idCandidat, sqle);
        }
        return nodePropositions;
    }
}
