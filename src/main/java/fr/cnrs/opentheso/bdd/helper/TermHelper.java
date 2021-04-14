/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

//
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAutoCompletion;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTab2Levels;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTerm;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miled.rousset
 */
public class TermHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    public TermHelper() {
    }

    /**
     * ************************************************************
     * /**************************************************************
     * Nouvelles fonctions stables auteur Miled Rousset
     * /**************************************************************
     * /*************************************************************
     */
    
    
    /**
     * Cette fonction permet de savoir si le terme est un parfait doublon ou non
     *
     *
     * @param ds
     * @param title
     * @param idThesaurus
     * @param idLang
     * @return idTerm or null
     */
    public boolean isPrefLabelExist(HikariDataSource ds,
            String title, String idThesaurus, String idLang) {

        Connection conn;
        ResultSet resultSet = null;
        boolean existe = false;
        StringPlus stringPlus = new StringPlus();
        title = stringPlus.convertString(title);
        title = stringPlus.unaccentLowerString(title);        

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try (Statement stmt = conn.createStatement()){
                try {
                    String query = "select id_term from term where "
                            + " f_unaccent(lower(term.lexical_value)) like '" + title + "'"
                            + " and lang = '" + idLang
                            + "' and id_thesaurus = '" + idThesaurus
                            + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }

                } finally {
                    if (resultSet != null) resultSet.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if Title of Term exist : " + title, sqle);
        }
        return existe;
    }

    public int getNbrTermSansGroup(HikariDataSource ds, String idThesaurus, String lang) {
        Connection conn;
        ResultSet resultSet = null;
        int count = 0;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try (Statement stmt = conn.createStatement()){
                try {
                    String query = "SELECT count(term.id_term) FROM term INNER JOIN " +
                            "(SELECT preferred_term.id_concept,preferred_term.id_term FROM preferred_term " +
                            "WHERE preferred_term.id_concept NOT IN (SELECT idconcept FROM concept_group_concept " +
                            "WHERE idthesaurus='"+idThesaurus+"')) AS Tabl ON Tabl.id_term=term.id_term " +
                            "WHERE term.lang='"+lang+"' AND id_thesaurus='"+idThesaurus+"';";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        resultSet.next();
                        count = resultSet.getInt(1);
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
            log.error("Error while getting count of decriptor without group : ", sqle);
        }
        return count;
    }
    
    /**
     * Cette fonction permet de savoir si le synonyme est un parfait doublon ou non
     *
     *
     * @param ds
     * @param title
     * @param idThesaurus
     * @param idLang
     * @return idTerm or null
     */
    public boolean isAltLabelExist(HikariDataSource ds,
            String title, String idThesaurus, String idLang) {

        Connection conn;
        ResultSet resultSet = null;
        boolean existe = false;
        title = new StringPlus().convertString(title);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try (Statement stmt = conn.createStatement()){
                try {
                    String query = "select id_term from non_preferred_term" +
                            "where" +
                            "f_unaccent(lower(lexical_value)) like '" + title + "'" +
                            "and lang = '" + idLang +"'" +
                            "and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
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
            log.error("Error while asking if Title of altLabel exist : " + title, sqle);
        }
        return existe;
    }    

    /**
     * Permet de modifier le libellé d'un synonyme
     *
     * @param ds
     * @param idTerm
     * @param newValue
     * @param idLang
     * @param oldValue
     * @param idTheso
     * @param isHidden
     * @param idUser
     * @return
     */
    public boolean updateTermSynonyme(HikariDataSource ds,
            String oldValue, String newValue,
            String idTerm, String idLang,
            String idTheso, boolean isHidden, int idUser) {

        Connection conn = null;
        Statement stmt;
        boolean isPassed = false;
        StringPlus stringPlus = new StringPlus();
        
        oldValue = stringPlus.convertString(oldValue);
        newValue = stringPlus.convertString(newValue);
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE non_preferred_term set"
                            + " lexical_value = '" + newValue + "',"
                            + " hiden = " + isHidden + ","
                            + " modified = current_date "
                            + " WHERE lang ='" + idLang + "'"
                            + " AND id_thesaurus = '" + idTheso + "'"
                            + " AND id_term = '" + idTerm + "'"
                            + " AND lexical_value = '" + oldValue + "'";

                    stmt.executeUpdate(query);
                    if (addNonPreferredTermHistorique(conn, idTerm, newValue, idLang, idTheso, "", "", isHidden, "update", idUser)) {
                        conn.commit();
                        isPassed = true;
                    } else {
                        conn.rollback();
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, sqle);
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return isPassed;
    }
    
    /**
     * Permet de modifier le status du synonyme (caché ou non)
     *
     * @param ds
     * @param idTerm
     * @param value
     * @param idLang
     * @param idTheso
     * @param isHidden
     * @param idUser
     * @return
     */
    public boolean updateStatus(HikariDataSource ds,
            String idTerm, String value, String idLang,
            String idTheso, boolean isHidden, int idUser) {

        Connection conn = null;
        Statement stmt;
        boolean isPassed = false;
        value = (new StringPlus().convertString(value));        
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE non_preferred_term set"
                            + " hiden = " + isHidden + ","
                            + " modified = current_date "
                            + " WHERE lang ='" + idLang + "'"
                            + " AND id_thesaurus = '" + idTheso + "'"
                            + " AND id_term = '" + idTerm + "'" 
                            + " AND lexical_value = '" + value + "'";

                    stmt.executeUpdate(query);
                    if (addNonPreferredTermHistorique(conn, idTerm, value, idLang, idTheso, "", "", isHidden, "update", idUser)) {
                        conn.commit();
                        isPassed = true;
                    } else {
                        conn.rollback();
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, sqle);
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return isPassed;
    }    

    /**
     * Cette fonction permet de supprimer un Terme Non descripteur ou synonyme
     *
     * @param ds
     * @param idTerm
     * @param idLang
     * @param lexicalValue
     * @param idTheso
     * @param status
     * @param idUser
     * @return
     */
    public boolean deleteNonPreferedTerm(HikariDataSource ds,
            String idTerm, String idLang,
            String lexicalValue, String idTheso, String status, int idUser) {

        Connection conn = null;
        Statement stmt;
        lexicalValue = new StringPlus().convertString(lexicalValue);
        boolean isPassed = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from non_preferred_term where"
                            + " id_thesaurus = '" + idTheso + "'"
                            + " and id_term  = '" + idTerm + "'"
                            + " and lexical_value  = '" + lexicalValue + "'"
                            + " and lang  = '" + idLang + "'";
                    stmt.executeUpdate(query);

                    if (!addNonPreferredTermHistorique(conn, idTerm, lexicalValue, idLang, idTheso, "", "", false, "delete", idUser)) {
                        conn.rollback();
                    } else {
                        conn.commit();
                        isPassed = true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, sqle);
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return isPassed;
    }

    /**
     * Cette fonction permet de rajouter des Termes Non descripteurs ou
     * synonymes
     *
     * @param ds
     * @param idTerm
     * @param idUser
     * @param idLang
     * @param value
     * @param idTheso
     * @param status
     * @param isHidden
     * @param source
     * @return boolean
     */
    public boolean addNonPreferredTerm(HikariDataSource ds,
            String idTerm,
            String value,
            String idLang,
            String idTheso,
            String source,
            String status,
            boolean isHidden,
            int idUser) {

        Connection conn = null;
        boolean isPassed = false;

        Statement stmt;
        value = new StringPlus().convertString(value);
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into non_preferred_term "
                            + "(id_term, lexical_value, lang, "
                            + "id_thesaurus, source, status, hiden)"
                            + " values ("
                            + "'" + idTerm + "'"
                            + ",'" + value + "'"
                            + ",'" + idLang + "'"
                            + ",'" + idTheso + "'"
                            + ",'" + source + "'"
                            + ",'" + status + "'"
                            + "," + isHidden + ")";

                    stmt.executeUpdate(query);
                    if (addNonPreferredTermHistorique(conn, idTerm, value, idLang, idTheso, source, status, isHidden, "ADD", idUser)) {
                        conn.commit();
                        isPassed = true;
                    } else {
                        isPassed = false;
                        conn.rollback();
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, sqle);
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return isPassed;
    }

    /**
     *
     * @param conn
     * @param idUser
     * @param action
     * @return idTerm
     */
    private boolean addNonPreferredTermHistorique(Connection conn,
            String idTerm,
            String value,
            String idLang,
            String idTheso,
            String source,
            String status,
            boolean isHidden,
            String action,
            int idUser) {

        boolean isPassed = false;
        Statement stmt;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into non_preferred_term_historique "
                            + "(id_term, lexical_value, lang, "
                            + "id_thesaurus, source, status, id_user, action, hiden)"
                            + " values ("
                            + "'" + idTerm + "'"
                            + ",'" + value + "'"
                            + ",'" + idLang + "'"
                            + ",'" + idTheso + "'"
                            + ",'" + source + "'"
                            + ",'" + status + "'"
                            + ",'" + idUser + "'"
                            + ",'" + action + "'"
                            + "," + isHidden + ")";

                    stmt.executeUpdate(query);
                    isPassed = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //    conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                isPassed = true;
            }
        }
        return isPassed;
    }

    /**
     * Cette fonction permet de supprimer une traduction
     *
     * @param ds
     * @param idLang
     * @param oldLabel
     * @param idTerm
     * @param idTheso
     * @param idUser
     * @return
     */
    public boolean deleteTraductionOfTerm(HikariDataSource ds,
            String idTerm, String oldLabel,
            String idLang, String idTheso, int idUser) {

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
                    String query = "delete from term where"
                            + " id_thesaurus = '" + idTheso + "'"
                            + " and id_term  = '" + idTerm + "'"
                            + " and lang = '" + idLang + "'";
                    stmt.executeUpdate(query);
                    if (addNewTermHistorique(conn, idTerm, oldLabel, idLang, idTheso, "", "Delete", idUser)) {
                        conn.commit();
                        status = true;
                    } else {
                        conn.rollback();
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting traduction of Term : " + idTerm, sqle);
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return status;
    }

    /**
     * Permet d'ajouter une traduction à un Terme #MR
     *
     * @param ds
     * @param label
     * @param idTerm
     * @param idTheso
     * @param source
     * @param status
     * @param idLang
     * @param idUser
     * @return
     */
    public boolean addTraduction(
            HikariDataSource ds,
            String label,
            String idTerm,
            String idLang,
            String source,
            String status,
            String idTheso,
            int idUser) {

        Statement stmt;
        Connection conn = null;
        boolean passed = false;

        label = new StringPlus().convertString(label);
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into term "
                            + "(id_term, lexical_value, lang, "
                            + "id_thesaurus, source, status,contributor, creator)"
                            + " values ("
                            + "'" + idTerm + "'"
                            + ",'" + label + "'"
                            + ",'" + idLang + "'"
                            + ",'" + idTheso + "'"
                            + ",'" + source + "'"
                            + ",'" + status + "'"
                            + ", " + idUser
                            + ", " + idUser + ")";

                    stmt.executeUpdate(query);
                    if (addNewTermHistorique(conn, idTerm, label, idLang, idTheso, "", "New", idUser)) {
                        conn.commit();
                        passed = true;
                    } else {
                        conn.rollback();
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // duppliqué
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                passed = true;
            }
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return passed;
    }

    /**
     * fonction qui permet de mettre à jour un label
     *
     * @param ds
     * @param label
     * @param idTerm
     * @param idLang
     * @param idTheso
     * @param idUser
     * @return
     */
    public boolean updateTraduction(HikariDataSource ds,
            String label,
            String idTerm,
            String idLang,
            String idTheso,
            int idUser) {
        Connection conn = null;
        Statement stmt;
        boolean status = false;
        label = new StringPlus().convertString(label);
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE term set"
                            + " lexical_value = '" + label + "',"
                            + " modified = current_date ,"
                            + " contributor = " + idUser
                            + " WHERE lang ='" + idLang + "'"
                            + " AND id_term = '" + idTerm + "'"
                            + " AND id_thesaurus = '" + idTheso + "'";

                    stmt.executeUpdate(query);
                    if (addNewTermHistorique(conn, idTerm, label, idLang, idTheso, "", "Rename", idUser)) {
                        conn.commit();
                        status = true;
                    } else {
                        conn.rollback();
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            // Log exception
            log.error("Error while updating Term Traduction : " + idTerm, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de récupérer le nom d'un Concept d'après son idTerm
     * sinon renvoie une chaine vide
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @param idLang
     * @return Objet class Concept
     */
    public String getLexicalValue(HikariDataSource ds,
            String idTerm, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        String lexicalValue = "";
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lexical_value from term where"
                            + " term.id_thesaurus = '" + idThesaurus + "'"
                            + " and term.id_term = '" + idTerm + "'"
                            + " and term.lang = '" + idLang + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        lexicalValue = resultSet.getString("lexical_value");
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
            log.error("Error while getting LexicalValue of Term : " + idTerm, sqle);
        }
        return lexicalValue.trim();
    }

    /**
     *
     * @param conn
     * @param idTerm
     * @param lexicalValue
     * @param idLang
     * @param idTheso
     * @param idUser
     * @param action
     * @param source
     * @return
     */
    public boolean addNewTermHistorique(Connection conn,
            String idTerm,
            String lexicalValue,
            String idLang,
            String idTheso,
            String source,
            String action,
            int idUser) {
        //     Connection conn;
        Statement stmt;
        boolean isPassed = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into term_historique "
                            + "(id_term, lexical_value, lang, "
                            + "id_thesaurus, source, action, id_user)"
                            + " values ("
                            + "'" + idTerm + "'"
                            + ",'" + lexicalValue + "'"
                            + ",'" + idLang + "'"
                            + ",'" + idTheso + "'"
                            + ",'" + source + "'"
                            + ",'" + action + "'"
                            + ",'" + idUser + "')";

                    stmt.executeUpdate(query);
                    isPassed = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //    conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            System.out.println("Error : " + sqle.getMessage());
        }
        return isPassed;
    }

    /**
     * Cette fonction permet de savoir si le terme existe dans cette langue ou
     * non
     *
     * @param ds
     * @param idTerm
     * @param idLang
     * @param idThesaurus
     * @return boolean
     */
    public boolean isTermExistInThisLang(HikariDataSource ds,
            String idTerm, String idLang, String idThesaurus) {

        Statement stmt;
        ResultSet resultSet = null;
        Connection conn;
        boolean existe = false;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_term from term where "
                            + " id_term = '" + idTerm + "'"
                            + " and lang = '" + idLang + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
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
            log.error("Error while asking if is Term exist in this lang : " + idTerm, sqle);
        }
        return existe;
    }

    /**
     * ************************************************************
     * /**************************************************************
     * Fin Nouvelles fonctions stables auteur Miled Rousset
     * /**************************************************************
     * /*************************************************************
     */
    /**
     * Cette fonction permet d'ajouter un Terme à la table Term, en paramètre un
     * objet Classe Term
     *
     * @param conn
     * @param term
     * @param idConcept
     * @param idUser
     * @return
     */
    public String addTerm(Connection conn,
            Term term, String idConcept, int idUser) {

        String idTerm = addNewTerm(conn, term, idUser);

        if (idTerm == null) {
            return null;
        }

        term.setId_term(idTerm);
        if (!addLinkTerm(conn, term, idConcept, idUser)) {
            return null;
        }

        return idTerm;
    }

    /**
     * Cette fonction permet d'ajouter un Terme à la table Term, en paramètre un
     * objet Classe Term
     *
     * @param ds
     * @param nodeTerm
     * @param idUser
     * @return
     */
    public boolean insertTerm(HikariDataSource ds,
            NodeTerm nodeTerm, int idUser) {
        if (nodeTerm.getNodeTermTraduction().isEmpty()) {
            return false;
        }

        for (int i = 0; i < nodeTerm.getNodeTermTraduction().size(); i++) {
            insertTermTraduction(ds,
                    nodeTerm.getIdTerm(),
                    nodeTerm.getIdConcept(),
                    nodeTerm.getNodeTermTraduction().get(i).getLexicalValue(),
                    nodeTerm.getNodeTermTraduction().get(i).getLang(),
                    nodeTerm.getIdThesaurus(),
                    nodeTerm.getCreated(),
                    nodeTerm.getModified(),
                    nodeTerm.getSource(),
                    nodeTerm.getStatus(),
                    idUser
            );

        }
        insertLinkTerm(ds, nodeTerm.getIdTerm(), nodeTerm.getIdThesaurus(),
                nodeTerm.getIdConcept(), idUser);

        return true;
    }

    /**
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @param idConcept
     * @param idUser
     */
    public void insertLinkTerm(HikariDataSource ds,
            String idTerm,
            String idThesaurus,
            String idConcept, int idUser) {
        Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into preferred_term "
                            + "(id_concept, id_term, id_thesaurus)"
                            + " values ("
                            + "'" + idConcept + "'"
                            + ",'" + idTerm + "'"
                            + ",'" + idThesaurus + "')";

                    stmt.executeUpdate(query);

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getMessage().contains("duplicate key value violates unique constraint")) {
                log.error("Error while adding Link prefered term : " + idTerm, sqle);
            }
        }
    }

    /**
     * Cette fonction permet de rajouter une relation Terme Préféré
     *
     * @param conn
     * @param term
     * @param idConcept
     * @param idUser
     * @return
     */
    public boolean addLinkTerm(Connection conn,
            Term term, String idConcept, int idUser) {

        boolean status = false;
        Statement stmt;
        try {
            // Get connection from pool
            //           conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into preferred_term "
                            + "(id_concept, id_term, id_thesaurus)"
                            + " values ("
                            + "'" + idConcept + "'"
                            + ",'" + term.getId_term() + "'"
                            + ",'" + term.getId_thesaurus() + "')";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //              conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            //  System.out.println(sqle);
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                status = true;
            }
        }
        return status;
    }

    /**
     *
     * @param conn
     * @param term
     * @param idUser
     * @return idTerm
     */
    public String addNewTerm(Connection conn,
            Term term, int idUser) {
        String idTerm = null;
        //     Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        term.setLexical_value(new StringPlus().convertString(term.getLexical_value()));
        try {
            // Get connection from pool
            //   conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select max(id) from term";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    resultSet.next();
                    int idTermNum = resultSet.getInt(1);
                    idTermNum++;
                    idTerm = "" + (idTermNum);
                    // si le nouveau Id existe, on l'incrémente
                    while (isIdOfTermExist(conn, idTerm, term.getId_thesaurus())) {
                        idTerm = "" + (++idTermNum);
                    }
                    term.setId_term(idTerm);
                    /**
                     * Ajout des informations dans la table Concept
                     */
                    query = "Insert into term "
                            + "(id_term, lexical_value, lang, id_thesaurus, source, status, contributor, creator)"
                            + " values ("
                            + "'" + term.getId_term() + "'"
                            + ",'" + term.getLexical_value() + "'"
                            + ",'" + term.getLang() + "'"
                            + ",'" + term.getId_thesaurus() + "'"
                            + ",'" + term.getSource() + "'"
                            + ",'" + term.getStatus() + "'"
                            + ", " + idUser + ""
                            + ", " + idUser + ")";

                    stmt.executeUpdate(query);
                    if (!addNewTermHistorique(conn, term, idUser)) {
                        return null;
                    }

                } finally {
                    if (resultSet != null) resultSet.close();
                    stmt.close();
                }
            } finally {
                //    conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                idTerm = null;
            }
        }

        return idTerm;
    }

    /**
     *
     * @param conn
     * @param term
     * @param idUser
     * @return
     */
    public boolean addNewTermHistorique(Connection conn,
            Term term, int idUser) {
        //     Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into term_historique "
                            + "(id_term, lexical_value, lang, "
                            + "id_thesaurus, source, status, id_user)"
                            + " values ("
                            + "'" + term.getId_term() + "'"
                            + ",'" + term.getLexical_value() + "'"
                            + ",'" + term.getLang() + "'"
                            + ",'" + term.getId_thesaurus() + "'"
                            + ",'" + term.getSource() + "'"
                            + ",'" + term.getStatus() + "'"
                            + ",'" + idUser + "')";

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
            System.out.println("Error : " + sqle.getMessage());
        }
        return status;
    }

    /**
     * Cette fonction permet d'insérrer un Term par import avec un identifiant
     * existant
     *
     * @param ds
     * @param term
     * @param idTerm_import
     * @param idUser
     * @return idTerm
     */
    public boolean insertNewTerm(HikariDataSource ds,
            Term term, String idTerm_import, int idUser) {

        idTerm_import = "" + idTerm_import;
        term.setId_term(idTerm_import);
        Connection conn;
        Statement stmt;

        term.setLexical_value(new StringPlus().convertString(term.getLexical_value()));

        boolean resultat = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {

                    /**
                     * Ajout des informations dans la table Concept
                     */
                    String query = "Insert into term "
                            + "(id_term, lexical_value, lang, "
                            + "id_thesaurus, source, status)"
                            + " values ("
                            + "'" + term.getId_term() + "'"
                            + ",'" + term.getLexical_value() + "'"
                            + ",'" + term.getLang() + "'"
                            + ",'" + term.getId_thesaurus() + "'"
                            + ",'" + term.getSource() + "'"
                            + ",'" + term.getStatus() + "')";

                    stmt.executeUpdate(query);
                    resultat = true;

                    addNewTermHistorique(conn, term, idUser);

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Term Import : " + idTerm_import, sqle);
            } else {
                resultat = true;
            }
        }
        return resultat;
    }

    /**
     * Cette fonction permet de supprimer un Terme avec toutes les dépendances
     * (Prefered term dans toutes les langues) et (nonPreferedTerm dans toutes
     * les langues)
     *
     * @param conn
     * @param idTerm
     * @param idThesaurus
     * @param idUser
     * @return
     */
    public boolean deleteTerm(Connection conn,
            String idTerm, String idThesaurus, int idUser) {

        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from term where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_term  = '" + idTerm + "'";
                    stmt.executeUpdate(query);

                    // Suppression de la relation Term_Concept
                    query = "delete from preferred_term where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_term  = '" + idTerm + "'";
                    stmt.executeUpdate(query);

                    // suppression des termes synonymes
                    query = "delete from non_preferred_term where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_term  = '" + idTerm + "'";
                    stmt.executeUpdate(query);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
                //           conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting Term and relations : " + idTerm, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de supprimer les données de la table Permuted pour
     * un thésaurus donné
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public boolean deletePermutedTable(HikariDataSource ds,
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
                    String query = "delete from permuted where"
                            + " id_thesaurus = '" + idThesaurus + "'";
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
            log.error("Error while deleting data from Permuted for thesaurus : " + idThesaurus, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de rajouter des Termes Non descripteurs ou
     * synonymes
     *
     * @param ds
     * @param term
     * @param idUser
     * @return boolean
     */
    public boolean addNonPreferredTerm(HikariDataSource ds,
            Term term, int idUser) {

        Connection conn;
        boolean status;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            if (!addUSE(conn, term, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            if (!addUSEHistorique(conn, term, idUser, "ADD")) {
                conn.rollback();
                conn.close();
                return false;
            }
            // cette fonction permet de remplir la table Permutée de NonPreferredTerm
/*
            String idConcept = new ConceptHelper().getIdConceptOfTerm(ds, term.getId_term(), term.getId_thesaurus());
            String idGroup = new ConceptHelper().getGroupIdOfConcept(ds, idConcept, term.getId_thesaurus());
            splitConceptForNonPermuted(ds,
                    idConcept,
                    idGroup,
                    term.getId_thesaurus(),
                    term.getLang(),
                    term.getLexical_value());
             */

            conn.commit();
            conn.close();
            status = true;
        } catch (SQLException ex) {
            Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, ex);
            status = false;
        }
        return status;
    }

    /**
     *
     * @param conn
     * @param term
     * @param idUser
     * @return idTerm
     */
    private boolean addUSE(Connection conn,
            Term term, int idUser) {
        boolean status = false;
        //     Connection conn;
        Statement stmt;
        term.setLexical_value(new StringPlus().convertString(term.getLexical_value()));
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into non_preferred_term "
                            + "(id_term, lexical_value, lang, "
                            + "id_thesaurus, source, status, hiden)"
                            + " values ("
                            + "'" + term.getId_term() + "'"
                            + ",'" + term.getLexical_value() + "'"
                            + ",'" + term.getLang() + "'"
                            + ",'" + term.getId_thesaurus() + "'"
                            + ",'" + term.getSource() + "'"
                            + ",'" + term.getStatus() + "'"
                            + "," + term.isHidden() + ")";

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
                status = true;
            }
        }
        return status;
    }

    /**
     *
     * @param conn
     * @param term
     * @param idUser
     * @param action
     * @return idTerm
     */
    private boolean addUSEHistorique(Connection conn,
            Term term, int idUser, String action) {
        boolean status = false;
        //     Connection conn; 
        Statement stmt;
        term.setLexical_value(new StringPlus().convertString(term.getLexical_value()));
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into non_preferred_term_historique "
                            + "(id_term, lexical_value, lang, "
                            + "id_thesaurus, source, status, id_user, action)"
                            + " values ("
                            + "'" + term.getId_term() + "'"
                            + ",'" + term.getLexical_value() + "'"
                            + ",'" + term.getLang() + "'"
                            + ",'" + term.getId_thesaurus() + "'"
                            + ",'" + term.getSource() + "'"
                            + ",'" + term.getStatus() + "'"
                            + ",'" + idUser + "'"
                            + ",'" + action + "')";

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
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                status = false;
            }
        }
        return status;
    }

    /**
     * #### déprécié par MR utiliser la nouvelle fonction Cette fonction permet
     * d'ajouter une traduction à un Terme
     *
     * @param conn
     * @param term
     * @param idUser
     * @return
     */
    public boolean addTermTraduction(Connection conn,
            Term term, int idUser) {

        Statement stmt;
        term.setLexical_value(new StringPlus().convertString(term.getLexical_value()));
        try {
            // Get connection from pool
            //        conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into term "
                            + "(id_term, lexical_value, lang, "
                            + "id_thesaurus, source, status,contributor, creator)"
                            + " values ("
                            + "'" + term.getId_term() + "'"
                            + ",'" + term.getLexical_value() + "'"
                            + ",'" + term.getLang() + "'"
                            + ",'" + term.getId_thesaurus() + "'"
                            + ",'" + term.getSource() + "'"
                            + ",'" + term.getStatus() + "'"
                            + ", " + term.getContributor()
                            + ", " + term.getCreator() + ")";

                    stmt.executeUpdate(query);
                    addNewTermHistorique(conn, term, idUser);
                } finally {
                    stmt.close();
                }
            } finally {
                //  conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Cette fonction permet d'ajouter une traduction à un Terme cette fonction
     * est utilisée pour les imports
     *
     * @param ds
     * @param idTerm
     * @param idConcept
     * @param lexicalValue
     * @param lang
     * @param idThesaurus
     * @param created
     * @param modified
     * @param status
     * @param source
     * @param idUser
     * @return
     */
    public boolean insertTermTraduction(HikariDataSource ds,
            String idTerm,
            String idConcept,
            String lexicalValue,
            String lang, String idThesaurus,
            Date created,
            Date modified,
            String source, String status, int idUser) {

        Connection conn;
        Statement stmt;
        boolean etat = false;

        // cette fonction permet de remplir la table Permutée
        splitConceptForPermute(ds,
                idConcept,
                new ConceptHelper().getGroupIdOfConcept(ds, idTerm, idThesaurus),
                idThesaurus,
                lang,
                lexicalValue);

        lexicalValue = new StringPlus().convertString(lexicalValue);
        String query;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if (modified == null || created == null) {
                        query = "Insert into term "
                                + "(id_term, lexical_value, lang, "
                                + "id_thesaurus, source, status)"
                                + " values ("
                                + "'" + idTerm + "'"
                                + ",'" + lexicalValue + "'"
                                + ",'" + lang + "'"
                                + ",'" + idThesaurus + "'"
                                + ",'" + source + "'"
                                + ",'" + status + "')";
                    } else {
                        query = "Insert into term "
                                + "(id_term, lexical_value, lang, "
                                + "id_thesaurus, created, modified, source, status, contributor)"
                                + " values ("
                                + "'" + idTerm + "'"
                                + ",'" + lexicalValue + "'"
                                + ",'" + lang + "'"
                                + ",'" + idThesaurus + "'"
                                + ",'" + created + "'"
                                + ",'" + modified + "'"
                                + ",'" + source + "'"
                                + ",'" + status + "'"
                                + ", " + idUser + ")";
                    }

                    stmt.executeUpdate(query);
                    etat = true;

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getMessage().contains("duplicate key value violates unique constraint")) {
                log.error("Error while adding Term Traduction : " + idTerm, sqle);
            }
        }
        return etat;
    }

    /**
     * Cette fonction permet de découper les mots d'un concept (phrase) pour
     * remplir la table permutée
     *
     * @param ds
     * @param idConcept
     * @param idGroup
     * @param lexicalValue
     * @param idLang
     * @param idThesaurus
     */
    public void splitConceptForPermute(HikariDataSource ds,
            String idConcept, String idGroup,
            String idThesaurus, String idLang,
            String lexicalValue) {

        Connection conn;
        Statement stmt;

        //ici c'est la fonction qui découpe la phrase en mots séparé pour la recherche permutée
        lexicalValue = lexicalValue.replaceAll("-", " ");
        lexicalValue = lexicalValue.replaceAll("\\(", " ");
        lexicalValue = lexicalValue.replaceAll("\\)", " ");
        lexicalValue = lexicalValue.replaceAll("\\/", " ");
//        lexicalValue = lexicalValue.replaceAll("'", " ");
        lexicalValue = new StringPlus().convertString(lexicalValue.trim());
        String tabMots[] = lexicalValue.split(" ");

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    int index = 1;
                    for (String value : tabMots) {
                        String query = "Insert into permuted "
                                + "(ord, id_concept, id_group, id_thesaurus,"
                                + " id_lang, lexical_value, ispreferredterm,original_value)"
                                + " values ("
                                + "" + index++ + ""
                                + ",'" + idConcept + "'"
                                + ",'" + idGroup + "'"
                                + ",'" + idThesaurus + "'"
                                + ",'" + idLang + "'"
                                + ",'" + value + "'"
                                + "," + true
                                + ",'" + lexicalValue + "')";

                        stmt.executeUpdate(query);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getMessage().contains("duplicate key value violates unique constraint")) {
                log.error("Error while adding values in Permuted table : " + idConcept, sqle);
            }
        }
    }

    /**
     * Cette fonction permet de découper les mots Synonymes d'un concept
     * (phrase) pour remplir la table Non permutée
     *
     * @param ds
     * @param idConcept
     * @param idGroup
     * @param lexicalValue
     * @param idLang
     * @param idThesaurus
     */
    public void splitConceptForNonPermuted(HikariDataSource ds,
            String idConcept, String idGroup,
            String idThesaurus, String idLang,
            String lexicalValue) {

        Connection conn;
        Statement stmt;

        //ici c'est la fonction qui découpe la phrase en mots séparé pour la recherche permutée
        lexicalValue = lexicalValue.replaceAll("-", " ");
        lexicalValue = lexicalValue.replaceAll("\\(", " ");
        lexicalValue = lexicalValue.replaceAll("\\)", " ");
        lexicalValue = lexicalValue.replaceAll("\\/", " ");
//        lexicalValue = lexicalValue.replaceAll("'", " ");

        lexicalValue = new StringPlus().convertString(lexicalValue.trim());

        String tabMots[] = lexicalValue.split(" ");

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    int index = 1;
                    for (String value : tabMots) {
                        String query = "Insert into permuted"
                                + " (ord, id_concept, id_group, id_thesaurus,"
                                + " id_lang, lexical_value, ispreferredterm, original_value)"
                                + " values ("
                                + "" + index++ + ""
                                + ",'" + idConcept + "'"
                                + ",'" + idGroup + "'"
                                + ",'" + idThesaurus + "'"
                                + ",'" + idLang + "'"
                                + ",'" + value + "'"
                                + "," + false
                                + ",'" + lexicalValue + "')";

                        stmt.executeUpdate(query);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                //  if (!sqle.getMessage().contains("duplicate key value violates unique constraint")) {
                log.error("Error while adding values in table Permuted for Non_Preferred_term : " + idConcept, sqle);
            }
        }
    }

    /**
     * Cette fonction permet de mettre à jour un Terme à la table Term, en
     * paramètre un objet Classe Term
     *
     * @param ds
     * @param term
     * @param idUser
     * @return
     */
    // Deprecated by Miled
    /*  public boolean updateTermTraduction(HikariDataSource ds,
            Term term, int idUser) {
        if (isTermExist(ds, term.getLexical_value(), term.getId_thesaurus(), term.getLang())){//isExitsTraduction(ds, term)) {
            // terme existe, il faut créer une relation
            return false;
        }
        if (!updateTermIntoTable(ds, term, idUser)) {
            return false;
        }
        return true;
    }*/
    /**
     * fonction qui permet de mettre à jour une traduction
     *
     * @param ds
     * @param term
     * @param idUser
     * @return
     */
    public boolean updateTermTraduction(HikariDataSource ds,
            Term term, int idUser) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        term.setLexical_value(new StringPlus().convertString(term.getLexical_value()));
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE term set"
                            + " lexical_value = '" + term.getLexical_value() + "',"
                            + " modified = current_date ,"
                            + " contributor = " + idUser
                            + " WHERE lang ='" + term.getLang() + "'"
                            + " AND id_term = '" + term.getId_term() + "'"
                            + " AND id_thesaurus = '" + term.getId_thesaurus() + "'";

                    stmt.executeUpdate(query);
                    status = true;

                    addNewTermHistorique(conn, term, idUser);
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating Term Traduction : " + term.getId_term(), sqle);
        }
        return status;
    }

    /*
    public boolean updateTermSynonyme(HikariDataSource ds,
            String oldValue, Term term, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;
        term.setLexical_value(new StringPlus().convertString(term.getLexical_value()));
        oldValue = (new StringPlus().convertString(oldValue));
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE non_preferred_term set"
                            + " lexical_value = '" + term.getLexical_value() + "',"
                            + " modified = current_date "
                            + " WHERE lang ='" + term.getLang() + "'"
                            + " AND id_thesaurus = '" + term.getId_thesaurus() + "'"
                            + " AND id_term = '" + term.getId_term() + "'"
                            + " AND lexical_value = '" + oldValue + "'";

                    stmt.executeUpdate(query);
                    status = true;

                    addNewTermHistorique(conn, term, idUser);
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating Synonym Modification : " + term.getId_term(), sqle);
        }
        return status;
    }*/

    /**
     * Cette fonction permet de récupérer un Term par son id et son thésaurus et
     * sa langue sous forme de classe Term (sans les relations)
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet class Concept
     */
    public Term getThisTerm(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        Term term = null;

        if (isTraductionExistOfConcept(ds, idConcept, idThesaurus, idLang)) {
            try {
                // Get connection from pool
                conn = ds.getConnection();
                try {
                    stmt = conn.createStatement();
                    try {
                        String query = "SELECT term.* FROM term, preferred_term"
                                + " WHERE preferred_term.id_term = term.id_term"
                                + " and preferred_term.id_thesaurus = term.id_thesaurus"
                                + " and preferred_term.id_concept ='" + idConcept + "'"
                                + " and term.lang = '" + idLang + "'"
                                + " and term.id_thesaurus = '" + idThesaurus + "'"
                                + " order by lexical_value DESC";

                        /* query = "select * from term where id_concept = '"
                         + idConcept + "'"
                         + " and id_thesaurus = '" + idThesaurus + "'"
                         + " and lang = '" + idLang + "'"
                         + " and prefered = true";*/
                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        if (resultSet.next()) {
                            term = new Term();
                            term.setId_term(resultSet.getString("id_term"));
                            term.setLexical_value(resultSet.getString("lexical_value"));
                            term.setLang(idLang);
                            term.setId_thesaurus(idThesaurus);
                            term.setCreated(resultSet.getDate("created"));
                            term.setModified(resultSet.getDate("modified"));
                            term.setSource(resultSet.getString("source"));
                            term.setStatus(resultSet.getString("status"));
                            term.setContributor(resultSet.getInt("contributor"));
                            term.setCreator(resultSet.getInt("creator"));
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
                log.error("Error while getting Concept : " + idConcept, sqle);
            }
        } else {
            try {
                // Get connection from pool
                conn = ds.getConnection();
                try {
                    stmt = conn.createStatement();
                    try {
                        String query = "select * from concept where id_concept = '"
                                + idConcept + "'"
                                + " and id_thesaurus = '" + idThesaurus + "'";

                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        if (resultSet.next()) {
                            term = new Term();
                            term.setId_term("");
                            term.setLexical_value("");
                            term.setLang(idLang);
                            term.setId_thesaurus(idThesaurus);
                            term.setCreated(resultSet.getDate("created"));
                            term.setModified(resultSet.getDate("modified"));
                            term.setStatus(resultSet.getString("status"));
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
                log.error("Error while getting Concept : " + idConcept, sqle);
            }

        }

        return term;
    }

    /**
     * Cette fonction permet de retourner l'id du terme d'après un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return idTermCandidat
     */
    public String getIdTermOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        String idTerm = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_term"
                            + " FROM preferred_term"
                            + " WHERE id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        idTerm = resultSet.getString("id_term");
                    } else {
                        return null;
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
            log.error("Error while getting idTerm of idConcept : " + idConcept, sqle);
        }
        return idTerm;
    }

    /**
     * Cette fonction permet de récupérer les termes synonymes suivant un
     * id_term et son thésaurus et sa langue sous forme de classe NodeEM
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @param idLang
     * @return Objet class Concept
     */
    public ArrayList<NodeEM> getNonPreferredTerms(HikariDataSource ds,
            String idTerm, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeEM> nodeEMList = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT lexical_value, created, modified,"
                            + " source, status, hiden"
                            + " FROM non_preferred_term"
                            + " WHERE non_preferred_term.id_term = '" + idTerm + "'"
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + " and non_preferred_term.lang ='" + idLang + "'"
                            + " order by lexical_value ASC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeEMList = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeEM nodeEM = new NodeEM();
                            nodeEM.setLexical_value(resultSet.getString("lexical_value"));
                            nodeEM.setCreated(resultSet.getDate("created"));
                            nodeEM.setModified(resultSet.getDate("modified"));
                            nodeEM.setSource(resultSet.getString("source"));
                            nodeEM.setStatus(resultSet.getString("status"));
                            nodeEM.setHiden(resultSet.getBoolean("hiden"));
                            nodeEM.setLang(idLang);
                            nodeEMList.add(nodeEM);
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
            log.error("Error while getting NonPreferedTerm of Term : " + idTerm, sqle);
        }

        return nodeEMList;
    }
    
    /**
     * Cette fonction permet de récupérer les termes synonymes suivant un
     * id_term et son thésaurus et sa langue sous forme de classe NodeEM
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @param idLang
     * @return Objet class Concept
     */
    public ArrayList<String> getNonPreferredTermsLabel(HikariDataSource ds,
            String idTerm, String idThesaurus, String idLang) {

        ArrayList<String> listAltLabel = new ArrayList<>();
        
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT lexical_value"
                            + " FROM non_preferred_term"
                            + " WHERE non_preferred_term.id_term = '" + idTerm + "'"
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + " and non_preferred_term.lang ='" + idLang + "'"
                            + " order by lexical_value ASC");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listAltLabel.add(resultSet.getString("lexical_value"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting NonPreferedTerm of Term : " + idTerm, sqle);
        }        
        return listAltLabel;
    }    

    /**
     * Cette fonction permet de récupérer l'historique des termes synonymes d'un
     * terme
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @param idLang
     * @return Objet class Concept
     */
    public ArrayList<NodeEM> getNonPreferredTermsHistoriqueAll(HikariDataSource ds,
            String idTerm, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeEM> nodeEMList = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT lexical_value, modified, source, status, hiden, action, username FROM non_preferred_term_historique, users"
                            + " WHERE id_term = '" + idTerm + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and lang ='" + idLang + "'"
                            + " and non_preferred_term_historique.id_user=users.id_user"
                            + " order by modified DESC, lexical_value ASC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeEMList = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeEM nodeEM = new NodeEM();
                            nodeEM.setLexical_value(resultSet.getString("lexical_value"));
                            nodeEM.setModified(resultSet.getDate("modified"));
                            nodeEM.setSource(resultSet.getString("source"));
                            nodeEM.setStatus(resultSet.getString("status"));
                            nodeEM.setHiden(resultSet.getBoolean("hiden"));
                            nodeEM.setAction(resultSet.getString("action"));
                            nodeEM.setIdUser(resultSet.getString("username"));
                            nodeEM.setLang(idLang);
                            nodeEMList.add(nodeEM);
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
            log.error("Error while getting NonPreferedTerm all historique of Term : " + idTerm, sqle);
        }

        return nodeEMList;
    }

    /**
     * Cette fonction permet de récupérer l'historique des termes synonymes d'un
     * terme à une date précise
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @param idLang
     * @param date
     * @return Objet class Concept
     */
    public ArrayList<NodeEM> getNonPreferredTermsHistoriqueFromDate(HikariDataSource ds,
            String idTerm, String idThesaurus, String idLang, Date date) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeEM> nodeEMList = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT lexical_value, modified, source, status, hiden, action, username FROM non_preferred_term_historique, users"
                            + " WHERE id_term = '" + idTerm + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and lang ='" + idLang + "'"
                            + " and non_preferred_term_historique.id_user=users.id_user"
                            + " and modified <= '" + date.toString()
                            + "' order by modified, lexical_value ASC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeEMList = new ArrayList<>();
                        while (resultSet.next()) {
                            if (resultSet.getString("action").equals("DEL")) {
                                for (NodeEM nem : nodeEMList) {
                                    if (nem.getLexical_value().equals(resultSet.getString("lexical_value")) && nem.getAction().equals("ADD") && nem.getStatus().equals(resultSet.getString("status"))) {
                                        nodeEMList.remove(nem);
                                        break;
                                    }
                                }
                            } else {
                                NodeEM nodeEM = new NodeEM();
                                nodeEM.setLexical_value(resultSet.getString("lexical_value"));
                                nodeEM.setModified(resultSet.getDate("modified"));
                                nodeEM.setSource(resultSet.getString("source"));
                                nodeEM.setStatus(resultSet.getString("status"));
                                nodeEM.setHiden(resultSet.getBoolean("hiden"));
                                nodeEM.setAction(resultSet.getString("action"));
                                nodeEM.setIdUser(resultSet.getString("username"));
                                nodeEM.setLang(idLang);
                                nodeEMList.add(nodeEM);
                            }

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
            log.error("Error while getting NonPreferedTerm date historique of Term : " + idTerm, sqle);
        }

        return nodeEMList;
    }

    /**
     * Cette fonction permet de récupérer l'historique d'un terme
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @param idLang
     * @return Objet class Concept
     */
    public ArrayList<Term> getTermsHistoriqueAll(HikariDataSource ds,
            String idTerm, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<Term> nodeTermList = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT lexical_value, modified, source, status, username FROM term_historique, users"
                            + " WHERE id_term = '" + idTerm + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and lang ='" + idLang + "'"
                            + " and term_historique.id_user=users.id_user"
                            + " order by modified DESC, lexical_value ASC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeTermList = new ArrayList<>();
                        while (resultSet.next()) {
                            Term t = new Term();
                            t.setId_term(idTerm);
                            t.setId_thesaurus(idThesaurus);
                            t.setLexical_value(resultSet.getString("lexical_value"));
                            t.setModified(resultSet.getDate("modified"));
                            t.setSource(resultSet.getString("source"));
                            t.setStatus(resultSet.getString("status"));
                            t.setIdUser(resultSet.getString("username"));
                            t.setLang(idLang);
                            nodeTermList.add(t);
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
            log.error("Error while getting all historique of Term : " + idTerm, sqle);
        }

        return nodeTermList;
    }

    /**
     * Cette fonction permet de récupérer l'historique d'un terme à une date
     * précise
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @param idLang
     * @param date
     * @return Objet class Concept
     */
    public ArrayList<Term> getTermsHistoriqueFromDate(HikariDataSource ds,
            String idTerm, String idThesaurus, String idLang, Date date) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<Term> nodeTermList = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT lexical_value, modified, source, status, username FROM term_historique, users"
                            + " WHERE id_term = '" + idTerm + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and lang ='" + idLang + "'"
                            + " and term_historique.id_user=users.id_user"
                            + " and modified <= '" + date.toString()
                            + "' order by modified DESC, lexical_value ASC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeTermList = new ArrayList<>();
                        resultSet.next();
                        Term t = new Term();
                        t.setId_term(idTerm);
                        t.setId_thesaurus(idThesaurus);
                        t.setLexical_value(resultSet.getString("lexical_value"));
                        t.setModified(resultSet.getDate("modified"));
                        t.setSource(resultSet.getString("source"));
                        t.setStatus(resultSet.getString("status"));
                        t.setIdUser(resultSet.getString("username"));
                        t.setLang(idLang);
                        nodeTermList.add(t);
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
            log.error("Error while getting date historique of Term : " + idTerm, sqle);
        }

        return nodeTermList;
    }

    /**
     * Cette fonction permet de récupérer les termes synonymes suivant un
     * id_term et son thésaurus et sa langue sous forme de classe NodeEM
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class Concept #MR
     */
    public ArrayList<NodeEM> getAllNonPreferredTerms(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeEM> nodeEMList = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT \n"
                            + "  non_preferred_term.lexical_value, \n"
                            + "  non_preferred_term.created, \n"
                            + "  non_preferred_term.modified, \n"
                            + "  non_preferred_term.source, \n"
                            + "  non_preferred_term.status, \n"
                            + "  non_preferred_term.hiden, \n"
                            + "  non_preferred_term.lang\n"
                            + " FROM \n"
                            + "  non_preferred_term, \n"
                            + "  preferred_term\n"
                            + " WHERE \n"
                            + "  preferred_term.id_term = non_preferred_term.id_term AND\n"
                            + "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus AND\n"
                            + "  preferred_term.id_concept = '" + idConcept + "' AND \n"
                            + "  non_preferred_term.id_thesaurus = '" + idThesaurus + "'\n"
                            + " ORDER BY\n"
                            + "  non_preferred_term.lexical_value ASC;";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeEMList = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeEM nodeEM = new NodeEM();
                            nodeEM.setLexical_value(resultSet.getString("lexical_value"));
                            nodeEM.setCreated(resultSet.getDate("created"));
                            nodeEM.setModified(resultSet.getDate("modified"));
                            nodeEM.setSource(resultSet.getString("source"));
                            nodeEM.setStatus(resultSet.getString("status"));
                            nodeEM.setHiden(resultSet.getBoolean("hiden"));
                            nodeEM.setLang(resultSet.getString("lang"));
                            nodeEMList.add(nodeEM);
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
            log.error("Error while getting NonPreferedTerm of Concept : " + idConcept, sqle);
        }

        return nodeEMList;
    }

    /**
     * Cette fonction permet de récupérer la liste de idTermes des
     * NonPreferredTerm (synonymes) pour un Thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList (idConcept, idTerm)
     */
    public ArrayList<NodeTab2Levels> getAllIdOfNonPreferredTerms(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;

        ArrayList<NodeTab2Levels> tabIdNonPreferredTerm = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT DISTINCT preferred_term.id_concept,"
                            + " preferred_term.id_term FROM"
                            + " non_preferred_term, preferred_term WHERE"
                            + " preferred_term.id_term = non_preferred_term.id_term AND"
                            + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeTab2Levels nodeTab2Levels = new NodeTab2Levels();
                        nodeTab2Levels.setIdConcept(resultSet.getString("id_concept"));
                        nodeTab2Levels.setIdTerm(resultSet.getString("id_term"));
                        tabIdNonPreferredTerm.add(nodeTab2Levels);
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
            log.error("Error while getting All Id of NonPreferedTerm of Thesaurus : " + idThesaurus, sqle);
        }

        return tabIdNonPreferredTerm;
    }

    /**
     * Cette fonction permet de récupérer la liste de idTermes des
     * NonPreferredTerm (synonymes) pour un Thésaurus en filtrant par Group
     *
     * @param ds
     * @param idThesaurus
     * @param idGroup
     * @return ArrayList (idConcept, idTerm)
     */
    public ArrayList<NodeTab2Levels> getAllIdOfNonPreferredTermsByGroup(HikariDataSource ds,
            String idThesaurus, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;

        ArrayList<NodeTab2Levels> tabIdNonPreferredTerm = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT DISTINCT preferred_term.id_concept,"
                            + " preferred_term.id_term FROM"
                            + " non_preferred_term, preferred_term, concept_group_concept WHERE"
                            + " preferred_term.id_term = non_preferred_term.id_term AND"
                            + " concept_group_concept.idconcept = preferred_term.id_concept AND"
                            + " concept_group_concept.idthesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "' AND"
                            + " concept_group_concept.idgroup = '" + idGroup + "' order by id_concept ASC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeTab2Levels nodeTab2Levels = new NodeTab2Levels();
                        nodeTab2Levels.setIdConcept(resultSet.getString("id_concept"));
                        nodeTab2Levels.setIdTerm(resultSet.getString("id_term"));
                        tabIdNonPreferredTerm.add(nodeTab2Levels);
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
            log.error("Error while getting All Id of NonPreferedTerm of Thesaurus : " + idThesaurus, sqle);
        }

        return tabIdNonPreferredTerm;
    }

    /**
     * permet de retourner la liste des concepts possibles pour ajouter une
     * relation NT (en ignorant les relations interdites) on ignore les concepts
     * de type TT on ignore les concepts de type RT
     *
     * On recherche aussi dans les Synonymes
     *
     * @param ds
     * @param idThesaurus
     * @param text
     * @param idLang
     * @return Objet class NodeAutoCompletion #MR
     */
    public List<NodeAutoCompletion> getAutoCompletForRelationNT(HikariDataSource ds,
            String idThesaurus, String idLang, String text) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        List<NodeAutoCompletion> nodeAutoCompletionList = new ArrayList<>();

        StringPlus stringPlus = new StringPlus();

        text = stringPlus.convertString(text);
        text = stringPlus.unaccentLowerString(text);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query
                            = "SELECT term.lexical_value, concept.id_concept, concept_group_concept.idgroup "
                            + "FROM preferred_term, term, concept,concept_group_concept "
                            + "WHERE "
                            + "idThesaurus = concept.id_thesaurus AND "
                            + "concept_group_concept.idconcept = concept.id_concept AND "
                            + "preferred_term.id_term = term.id_term AND "
                            + "preferred_term.id_thesaurus = term.id_thesaurus AND "
                            + "concept.id_concept = preferred_term.id_concept AND "
                            + "concept.id_thesaurus = preferred_term.id_thesaurus AND "
                            + "term.id_thesaurus = '" + idThesaurus + "' AND "
                            + "term.lang = '" + idLang + "' AND "
                            + "concept.status != 'hidden' AND "
                            + "concept.top_concept != 'true' AND "
                            + " f_unaccent(lower(term.lexical_value)) LIKE '%" + text + "%'"
                            + " order by term.lexical_value <-> '" + text + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {

                        while (resultSet.next()) {
                            if (resultSet.getRow() != 0) {
                                NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();

                                nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                                nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                                nodeAutoCompletion.setGroupLexicalValue(
                                        new GroupHelper().getLexicalValueOfGroup(ds, resultSet.getString("idgroup"), idThesaurus, idLang));
                                nodeAutoCompletion.setIdGroup(resultSet.getString("idgroup"));
                                //  if(!nodeAutoCompletionList.contains(nodeAutoCompletion))
                                nodeAutoCompletion.setIsAltLabel(false);
                                nodeAutoCompletionList.add(nodeAutoCompletion);
                            }
                        }
                    }
                    /* Deprecated by Miled
                    query = "SELECT DISTINCT "
                            + "non_preferred_term.lexical_value,"
                            + "concept.id_concept,"
                            + "concept_group_concept.idgroup"
                            + " FROM preferred_term, non_preferred_term, concept,concept_group_concept"
                            + "  WHERE"
                            + "  concept_group_concept.idthesaurus = concept.id_thesaurus "
                            + "  AND"
                            + "   concept_group_concept.idconcept = concept.id_concept"
                            + " AND preferred_term.id_term = non_preferred_term.id_term "
                            + " AND preferred_term.id_thesaurus = non_preferred_term.id_thesaurus "
                            + " AND concept.id_concept = preferred_term.id_concept "
                            + " AND concept.id_thesaurus = preferred_term.id_thesaurus "
                            + " AND non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + " AND non_preferred_term.lang = '" + idLang + "'"
                            + " AND concept.status != 'hidden'"
                            + " AND concept.top_concept != 'true'"
                            + " AND unaccent_string(non_preferred_term.lexical_value) ILIKE unaccent_string('" + text +"%') ORDER BY non_preferred_term.lexical_value ASC LIMIT 20";
                     */
                    query = "SELECT "
                            + "  non_preferred_term.lexical_value as npt,"
                            + "  term.lexical_value as pt,"
                            + "  preferred_term.id_concept,"
                            + "  concept_group_concept.idgroup"
                            + " FROM "
                            + "  concept, "
                            + "  preferred_term, "
                            + "  non_preferred_term, "
                            + "  term, "
                            + "  concept_group_concept"
                            + " WHERE "
                            + "  concept.id_concept = concept_group_concept.idconcept AND"
                            + "  concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  non_preferred_term.id_term = preferred_term.id_term AND"
                            + "  non_preferred_term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.id_term = preferred_term.id_term AND"
                            + "  term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.lang = non_preferred_term.lang AND"
                            + "  concept.status != 'hidden' AND"
                            + "  concept.top_concept != true AND"
                            + "  non_preferred_term.id_thesaurus = '" + idThesaurus + "' AND"
                            + "  non_preferred_term.lang = '" + idLang + "' AND"
                            + "  f_unaccent(lower(non_preferred_term.lexical_value)) LIKE '%" + text + "%' "
                            + "  order by non_preferred_term.lexical_value <-> '" + text + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();

                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setAltLabel(resultSet.getString("npt"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("pt"));
                        nodeAutoCompletion.setGroupLexicalValue(
                                new GroupHelper().getLexicalValueOfGroup(ds, resultSet.getString("idgroup"), idThesaurus, idLang));
                        nodeAutoCompletion.setIdGroup(resultSet.getString("idgroup"));
                        //  if(!nodeAutoCompletionList.contains(nodeAutoCompletion))
                        nodeAutoCompletion.setIsAltLabel(true);
                        nodeAutoCompletionList.add(nodeAutoCompletion);
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
            log.error("Error while getting List of autocompletion of Text : " + text, sqle);
        }

        return nodeAutoCompletionList;
    }

    /**
     * Cette fonction permet de récupérer une liste de termes pour
     * l'autocomplétion avec les synonymes
     *
     * @param ds
     * @param idThesaurus
     * @param text
     * @param idLang
     * @return Objet class Concept
     */
    public List<NodeAutoCompletion> getAutoCompletionTerm(HikariDataSource ds,
            String idThesaurus, String idLang, String text) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        List<NodeAutoCompletion> nodeAutoCompletionList = new ArrayList<>();
        StringPlus stringPlus = new StringPlus();

        text = stringPlus.convertString(text);
        text = stringPlus.unaccentLowerString(text);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    /* deprecated by Miled ... si le groupe n'est pas tradui dans la langue de recherche, on n'a pas de résultat.
                    String query = "SELECT " +
                        "  preferred_term.id_concept, " +
                        "  term.lexical_value, " +
                        "  concept_group_concept.idgroup, " +
                        "  concept_group_label.lexicalvalue" +
                        " FROM " +
                        "  concept_group_concept, " +
                        "  preferred_term, " +
                        "  term, " +
                        "  concept_group_label" +
                        " WHERE " +
                        "  preferred_term.id_concept = concept_group_concept.idconcept AND" +
                        "  preferred_term.id_thesaurus = concept_group_concept.idthesaurus AND" +
                        "  preferred_term.id_term = term.id_term AND" +
                        "  preferred_term.id_thesaurus = term.id_thesaurus AND" +
                        "  concept_group_label.idgroup = concept_group_concept.idgroup AND" +
                        "  concept_group_label.idthesaurus = concept_group_concept.idthesaurus AND" +
                        "  concept_group_label.lang = term.lang AND" +
                        "  term.lang = '" + idLang + "' AND " +
                        "  term.id_thesaurus = '" + idThesaurus + "' AND " +
                        "  f_unaccent(lower(term.lexical_value)) LIKE '%" + text + "%' limit 20";
                     */
                    String query = "SELECT "
                            + " term.lexical_value,"
                            + " preferred_term.id_concept,"
                            + " concept_group_concept.idgroup"
                            + " FROM"
                            + " concept, preferred_term, term, concept_group_concept"
                            + " WHERE"
                            + " concept.id_concept = concept_group_concept.idconcept "
                            + " AND  concept.id_thesaurus = concept_group_concept.idthesaurus "
                            + " AND  preferred_term.id_concept = concept.id_concept "
                            + " AND  preferred_term.id_thesaurus = concept.id_thesaurus "
                            + " AND  term.id_term = preferred_term.id_term "
                            + " AND  term.id_thesaurus = preferred_term.id_thesaurus "
                            + " AND  concept.status != 'hidden' "
                            + " AND term.lang = '" + idLang + "'"
                            + " AND term.id_thesaurus = '" + idThesaurus + "'"
                            + " AND f_unaccent(lower(term.lexical_value)) LIKE '%" + text + "%' order by term.lexical_value <-> '" + text + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();

                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeAutoCompletion.setGroupLexicalValue(//resultSet.getString("lexicalvalue"));
                                new GroupHelper().getLexicalValueOfGroup(ds, resultSet.getString("idgroup"), idThesaurus, idLang));
                        nodeAutoCompletion.setIdGroup(resultSet.getString("idgroup"));
                        //  if(!nodeAutoCompletionList.contains(nodeAutoCompletion))
                        nodeAutoCompletion.setIsAltLabel(false);
                        nodeAutoCompletionList.add(nodeAutoCompletion);
                    }
                    /* Deprecated by Miled
                    query = "SELECT \n" +
                        "  preferred_term.id_concept, \n" +
                        "  concept_group_concept.idgroup, \n" +
                        "  concept_group_label.lexicalvalue, \n" +
                        "  non_preferred_term.lexical_value\n" +
                        " FROM \n" +
                        "  public.concept_group_concept, \n" +
                        "  public.preferred_term, \n" +
                        "  public.concept_group_label, \n" +
                        "  public.non_preferred_term\n" +
                        " WHERE \n" +
                        "  preferred_term.id_concept = concept_group_concept.idconcept AND\n" +
                        "  preferred_term.id_thesaurus = concept_group_concept.idthesaurus AND\n" +
                        "  preferred_term.id_term = non_preferred_term.id_term AND\n" +
                        "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus AND\n" +
                        "  concept_group_label.idgroup = concept_group_concept.idgroup AND\n" +
                        "  concept_group_label.idthesaurus = concept_group_concept.idthesaurus AND\n" +
                        "  concept_group_label.lang = non_preferred_term.lang AND\n" +
                        "  non_preferred_term.lang = '" + idLang +"' AND \n" +
                        "  non_preferred_term.id_thesaurus = '" + idThesaurus + "' AND \n" +
                        "  f_unaccent(lower(non_preferred_term.lexical_value)) LIKE '%" + text + "%' limit 20";*/

                    query = "SELECT "
                            + "  non_preferred_term.lexical_value as npt,"
                            + "  term.lexical_value as pt,"
                            + "  preferred_term.id_concept,"
                            + "  concept_group_concept.idgroup"
                            + " FROM "
                            + "  concept, "
                            + "  preferred_term, "
                            + "  non_preferred_term, "
                            + "  term, "
                            + "  concept_group_concept"
                            + " WHERE "
                            + "  concept.id_concept = concept_group_concept.idconcept AND"
                            + "  concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  non_preferred_term.id_term = preferred_term.id_term AND"
                            + "  non_preferred_term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.id_term = preferred_term.id_term AND"
                            + "  term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.lang = non_preferred_term.lang AND"
                            + "  concept.status != 'hidden' AND"
                            + "  non_preferred_term.id_thesaurus = '" + idThesaurus + "' AND"
                            + "  non_preferred_term.lang = '" + idLang + "' AND"
                            + " f_unaccent(lower(non_preferred_term.lexical_value)) LIKE '%" + text + "%'"
                            + " order by non_preferred_term.lexical_value <-> '" + text + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();

                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setAltLabel(resultSet.getString("npt"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("pt"));
                        nodeAutoCompletion.setGroupLexicalValue(//resultSet.getString("lexicalvalue")); /// déscativé parceque si le groupe n'est pas traduit dans la langue de recherche, on n'a pas de résultat
                                new GroupHelper().getLexicalValueOfGroup(ds, resultSet.getString("idgroup"), idThesaurus, idLang));
                        nodeAutoCompletion.setIdGroup(resultSet.getString("idgroup"));
                        //  if(!nodeAutoCompletionList.contains(nodeAutoCompletion))
                        nodeAutoCompletion.setIsAltLabel(true);
                        nodeAutoCompletionList.add(nodeAutoCompletion);
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
            log.error("Error while getting List of autocompletion of Text : " + text, sqle);
        }

        return nodeAutoCompletionList;
    }

    /**
     * Cette fonction permet de récupérer une liste des terms pour
     * l'autocomplétion en se limitant à un Group et en ignorant le terme lui
     * même et ses BT
     *
     * @param ds
     * @param idSelectedConcept
     * @param idBTs
     * @param idThesaurus
     * @param text
     * @param idGroup
     * @param idLang
     * @return Objet class Concept
     */
    public List<NodeAutoCompletion> getAutoCompletionTerm(HikariDataSource ds,
            String idSelectedConcept, ArrayList<String> idBTs, // le concept à ignorer
            String idThesaurus, String idLang, String idGroup, String text) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        List<NodeAutoCompletion> nodeAutoCompletionList = new ArrayList<>();
        text = new StringPlus().convertString(text);

        String BT = "";
        for (String idBt : idBTs) {
            if (!BT.isEmpty()) {
                BT = BT + ",'" + idBt + "'";
            } else {
                BT = "'" + idBt + "'";
            }
        }

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query
                            = "SELECT DISTINCT term.lexical_value, concept.id_concept, idgroup "
                            + "FROM preferred_term, term, concept,concept_group_concept WHERE "
                            + "preferred_term.id_term = term.id_term AND "
                            + "concept_group_concept.idconcept = concept.id_concept AND "
                            + "concept_group_concept.idthesaurus = term.id_thesaurus"
                            + "preferred_term.id_thesaurus = term.id_thesaurus AND "
                            + "concept.id_concept = preferred_term.id_concept AND "
                            + "concept.id_thesaurus = preferred_term.id_thesaurus AND "
                            + "term.id_thesaurus = '" + idThesaurus + "' AND "
                            + "term.lang = '" + idLang + "' AND "
                            + "concept_group_concept.idgroup = '" + idGroup + "' AND "
                            + "concept.id_concept != '" + idSelectedConcept + "' AND "
                            + "concept.id_concept not in (" + BT + ") AND "
                            + "concept.status != 'hidden' AND "
                            + "unaccent_string(term.lexical_value) ILIKE unaccent_string('" + text + "%')"
                            + " ORDER BY term.lexical_value ASC LIMIT 20";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {

                        while (resultSet.next()) {
                            if (resultSet.getRow() != 0) {
                                NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();

                                nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                                nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                                nodeAutoCompletion.setGroupLexicalValue(
                                        new GroupHelper().getLexicalValueOfGroup(ds, resultSet.getString("idgroup"), idThesaurus, idLang));
                                nodeAutoCompletion.setIdGroup(resultSet.getString("idgroup"));
                                //  if(!nodeAutoCompletionList.contains(nodeAutoCompletion))
                                nodeAutoCompletionList.add(nodeAutoCompletion);
                            }
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
            log.error("Error while getting List of autocompletion of Text : " + text, sqle);
        }

        return nodeAutoCompletionList;
    }

    /**
     * Cette fonction permet de récupérer une liste des terms pour
     * l'autocomplétion en se limitant à un Group et en ignorant le terme lui
     * même
     *
     * @param ds
     * @param idSelectedConcept
     * @param idThesaurus
     * @param text
     * @param idGroup
     * @param idLang
     * @return Objet class Concept
     */
    public List<NodeAutoCompletion> getAutoCompletionTerm(HikariDataSource ds,
            String idSelectedConcept, // le concept à ignorer
            String idThesaurus, String idLang, String idGroup, String text) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        List<NodeAutoCompletion> nodeAutoCompletionList = new ArrayList<>();
        text = new StringPlus().convertString(text);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    /* ca ne mrche pas quand on n'a pas des notes
                    String query
                            = "SELECT DISTINCT term.lexical_value, concept.id_concept, idgroup, note.lexicalvalue "
                            + "FROM preferred_term, term, concept,concept_group_concept, note WHERE "
                            + "preferred_term.id_term = term.id_term AND "
                            + "concept_group_concept.idconcept = concept.id_concept AND "
                            + "concept_group_concept.idthesaurus = term.id_thesaurus AND "
                            + "preferred_term.id_thesaurus = term.id_thesaurus AND "
                            + "concept.id_concept = preferred_term.id_concept AND "
                            + "concept.id_thesaurus = preferred_term.id_thesaurus AND "
                            + " term.id_thesaurus = note.id_thesaurus AND"
                            + " term.id_term = note.id_term AND"
                            
                            + " term.id_thesaurus = '" + idThesaurus + "' AND "
                            + "term.lang = '" + idLang + "' AND "
                            + "idgroup = '" + idGroup + "' AND "
                            + "concept.id_concept != '" + idSelectedConcept + "' AND "
                            + "concept.status != 'hidden' AND "
                            + "unaccent_string(term.lexical_value) ILIKE unaccent_string('" + text + "%')"
                            + " ORDER BY term.lexical_value ASC LIMIT 20";
                     */
                    String query = "  SELECT term.lexical_value,   preferred_term.id_concept,   note.lexicalvalue,   concept_group_concept.idgroup "
                            + "FROM"
                            + " term INNER JOIN preferred_term ON preferred_term.id_term = term.id_term and preferred_term.id_thesaurus = term.id_thesaurus"
                            + " LEFT JOIN note ON note.id_term = term.id_term and note.lang = term.lang"
                            + " INNER JOIN concept_group_concept ON concept_group_concept.idconcept = preferred_term.id_concept and concept_group_concept.idthesaurus = preferred_term.id_thesaurus"
                            + " INNER JOIN concept ON concept.id_concept = preferred_term.id_concept and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " WHERE "
                            + " term.id_thesaurus = '" + idThesaurus + "' AND "
                            + "term.lang = '" + idLang + "' AND "
                            + "idgroup = '" + idGroup + "' AND "
                            + "concept.id_concept != '" + idSelectedConcept + "' AND "
                            + "concept.status != 'hidden' AND "
                            + "unaccent_string(term.lexical_value) ILIKE unaccent_string('" + text + "%')"
                            + " ORDER BY term.lexical_value ASC LIMIT 40";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {

                        while (resultSet.next()) {
                            if (resultSet.getRow() != 0) {
                                NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();

                                nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                                nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                                nodeAutoCompletion.setGroupLexicalValue(
                                        new GroupHelper().getLexicalValueOfGroup(ds, resultSet.getString("idgroup"), idThesaurus, idLang));
                                nodeAutoCompletion.setIdGroup(resultSet.getString("idgroup"));
                                nodeAutoCompletion.setDefinition(resultSet.getString("lexicalvalue"));
                                //  if(!nodeAutoCompletionList.contains(nodeAutoCompletion))
                                nodeAutoCompletionList.add(nodeAutoCompletion);
                            }
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
            log.error("Error while getting List of autocompletion of Text : " + text, sqle);
        }

        return nodeAutoCompletionList;
    }

    /**
     * Cette fonction permet de récupérer une liste des terms pour
     * l'autocomplétion en se évitant le Group actuel et en ignorant le terme
     * lui même
     *
     * @param ds
     * @param idSelectedConcept
     * @param idThesaurus
     * @param text
     * @param idGroup
     * @param idLang
     * @return Objet class Concept
     */
    public List<NodeAutoCompletion> getAutoCompletionTermOfOtherGroup(HikariDataSource ds,
            String idSelectedConcept, // le concept à ignorer
            String idThesaurus, String idLang, String idGroup, String text) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        List<NodeAutoCompletion> nodeAutoCompletionList = new ArrayList<>();
        text = new StringPlus().convertString(text);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query
                            = "SELECT DISTINCT term.lexical_value, concept.id_concept, idgroup "
                            + "FROM preferred_term, term, concept,concept_group_concept WHERE "
                            + "preferred_term.id_term = term.id_term AND "
                            + "concept_group_concept.idconcept = concept.id_concept AND "
                            + "concept_group_concept.idthesaurus = term.id_thesaurus AND "
                            + "preferred_term.id_thesaurus = term.id_thesaurus AND "
                            + "concept.id_concept = preferred_term.id_concept AND "
                            + "concept.id_thesaurus = preferred_term.id_thesaurus AND "
                            + "term.id_thesaurus = '" + idThesaurus + "' AND "
                            + "term.lang = '" + idLang + "' AND "
                            + "concept_group_concept.idgroup != '" + idGroup + "' AND "
                            + "concept.id_concept != '" + idSelectedConcept + "' AND "
                            + "concept.status != 'hidden' AND "
                            + "unaccent_string(term.lexical_value) ILIKE unaccent_string('" + text + "%')"
                            + " ORDER BY term.lexical_value ASC LIMIT 20";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {

                        while (resultSet.next()) {
                            if (resultSet.getRow() != 0) {
                                NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();

                                nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                                nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                                nodeAutoCompletion.setGroupLexicalValue(
                                        new GroupHelper().getLexicalValueOfGroup(ds, resultSet.getString("idgroup"), idThesaurus, idLang));
                                nodeAutoCompletion.setIdGroup(resultSet.getString("idgroup"));
                                //  if(!nodeAutoCompletionList.contains(nodeAutoCompletion))
                                nodeAutoCompletionList.add(nodeAutoCompletion);
                            }
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
            log.error("Error while getting List of autocompletion of Text : " + text, sqle);
        }

        return nodeAutoCompletionList;
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
    public boolean isTraductionExistOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

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
                    String query = "select term.id_term from term, preferred_term"
                            + " where term.id_term = preferred_term.id_term and"
                            + " preferred_term.id_concept = '" + idConcept + "'"
                            + " and term.lang = '" + idLang + "'"
                            + " and term.id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        resultSet.next();
                        existe = resultSet.getRow() != 0;
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
            log.error("Error while asking if Traduction of Concept exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de retourner les traductions d'un term sauf la
     * langue en cours
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return Objet class NodeConceptTree
     */
    public ArrayList<NodeTermTraduction> getTraductionsOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeTermTraduction> nodeTraductionsList = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.id_term, term.lexical_value, term.lang FROM"
                            + " term, preferred_term WHERE"
                            + " term.id_term = preferred_term.id_term"
                            + " and term.id_thesaurus = preferred_term.id_thesaurus"
                            + " and preferred_term.id_concept = '" + idConcept + "'"
                            + " and term.lang != '" + idLang + "'"
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + " order by term.lexical_value";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeTraductionsList = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeTermTraduction nodeTraductions = new NodeTermTraduction();
                            nodeTraductions.setLang(resultSet.getString("lang"));
                            nodeTraductions.setLexicalValue(resultSet.getString("lexical_value"));
                            nodeTraductionsList.add(nodeTraductions);
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
            log.error("Error while getting Traductions of Term  : " + idConcept, sqle);
        }
        return nodeTraductionsList;
    }

    /**
     * Cette fonction permet de retourner toutes les traductions d'un concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return Objet class NodeConceptTree
     */
    public ArrayList<NodeTermTraduction> getAllTraductionsOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeTermTraduction> nodeTraductionsList = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.id_term, term.lexical_value, term.lang FROM"
                            + " term, preferred_term WHERE"
                            + " term.id_term = preferred_term.id_term"
                            + " and term.id_thesaurus = preferred_term.id_thesaurus"
                            + " and preferred_term.id_concept = '" + idConcept + "'"
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + " order by term.lexical_value";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeTermTraduction nodeTraductions = new NodeTermTraduction();
                        nodeTraductions.setLang(resultSet.getString("lang"));
                        nodeTraductions.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeTraductionsList.add(nodeTraductions);
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
            log.error("Error while getting All Traductions of Concept  : " + idConcept, sqle);
        }
        return nodeTraductionsList;
    }

    /**
     * Cette fonction permet de retourner le nombre de traductions pour un
     * concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return
     * @returne
     */
    public int getCountOfTraductions(HikariDataSource ds,
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
                    String query = "SELECT count(term.id_term) FROM"
                            + " term, preferred_term WHERE"
                            + " term.id_term = preferred_term.id_term"
                            + " and term.id_thesaurus = preferred_term.id_thesaurus"
                            + " and preferred_term.id_concept = '" + idConcept + "'"
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + " and term.lang != '" + idLang + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
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
            log.error("Error while getting Count of Traductions of Concept  : " + idConcept, sqle);
        }
        return count;
    }

    /**
     * Cette fonction permet de savoir si le terme est un parfait doublon ou non
     * si oui, on retourne l'identifiant, sinon, on retourne null
     *
     * @param ds
     * @param title
     * @param idThesaurus
     * @param idLang
     * @return idTerm or null
     */
    public String isTermEqualTo(HikariDataSource ds,
            String title, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        String idTerm = null;
        title = new StringPlus().convertString(title);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_term from term where "
                            + "lexical_value = '" + title + "'"
                            + " and lang = '" + idLang + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        idTerm = resultSet.getString("id_term");
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
            log.error("Error while asking if Term exist : " + title, sqle);
        }
        return idTerm;
    }
    
    /**
     * Cette fonction permet de savoir si le terme existe ou non
     * en ignorant uniquement la casse 
     *
     * @param ds
     * @param title
     * @param idThesaurus
     * @param idLang
     * @return boolean
     */
    public boolean isTermExistIgnoreCase(HikariDataSource ds,
            String title, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;
        title = new StringPlus().convertString(title);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_term from term where "
                            + "lexical_value ilike "
                            + "'" + title + "'  and lang = '" + idLang
                            + "' and id_thesaurus = '" + idThesaurus
                            + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
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
            log.error("Error while asking if Title of Term exist : " + title, sqle);
        }
        return existe;
    }    

    /**
     * Cette fonction permet de savoir si le terme existe ou non
     *
     * @param ds
     * @param title
     * @param idThesaurus
     * @param idLang
     * @return boolean
     */
    public boolean isTermExist(HikariDataSource ds,
            String title, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;
        StringPlus stringPlus = new StringPlus();
        title = stringPlus.convertString(title);
        title = stringPlus.unaccentLowerString(title);        

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_term from term" +
                            "where" +
                            "f_unaccent(lower(lexical_value)) like '" + title + "'" +
                            "and lang = '" + idLang + "'" +
                            "and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
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
            log.error("Error while asking if Title of Term exist : " + title, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le terme existe ou non
     *
     * @param conn
     * @param idTerm
     * @param idThesaurus
     * @return boolean
     */
    public boolean isIdOfTermExist(Connection conn,
            String idTerm, String idThesaurus) {

        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_term from term where "
                            + " id_term = '" + idTerm + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }
                } finally {
                    if (resultSet != null) resultSet.close();
                    stmt.close();
                }
            } finally {
                //conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if id of Term exist : " + idTerm, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le terme existe ou non dans le
     * thésaurus mais il faut ignorer le terme lui même; ceci nous permet de
     * faire la modification dans le cas suivant : helene -> en Hélène
     *
     * @param ds
     * @param title
     * @param idThesaurus
     * @param idTerm
     * @param idLang
     * @return boolean
     */
    public boolean isTermExistForEdit(HikariDataSource ds,
            String title, String idThesaurus, String idTerm, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;
        title = new StringPlus().convertString(title);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_term from term where "
                            + "unaccent_string(lexical_value) ilike "
                            + "unaccent_string('" + title
                            + "')  and lang = '" + idLang
                            + "' and id_thesaurus = '" + idThesaurus + "'"
                            + " and id_term != '" + idTerm + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = true;
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
            log.error("Error while asking if Title of Term exist : " + title, sqle);
        }
        return existe;
    }

    /**
     * on recupere le valeur du WKT: ... C'est le coordonées pour les affiches
     * dans le map
     *
     * @param ds
     * @param idT
     * @param idTheso
     * @return
     */
    /*    public String recuperateDonnesMaps(HikariDataSource ds
                    ,String idT, String idTheso)
    {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String coordenadas=null;
        String fina = null;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lexical_value from non_preferred_term where "
                            + "id_term = '"+idT+"'"
                            + " and id_thesaurus = '"+idTheso+"'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while(resultSet.next()) {
                        coordenadas = resultSet.getString("lexical_value");
                        if(coordenadas.contains("WKT:"))
                        {
                            fina = coordenadas.substring(coordenadas.indexOf(":")+1, coordenadas.length()).trim();
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
            log.error("Error while asking coordinates : " , sqle);
        }
        return fina;
    }*/
}
