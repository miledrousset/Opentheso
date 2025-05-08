package fr.cnrs.opentheso.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.terms.NodeTerm;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Slf4j
@Service
public class TermHelper {

    @Autowired
    private DataSource dataSource;
    
    
    /**
     * Cette fonction permet de récupérer les termes synonymes pour un
     * concept sous forme de classe NodeEM
     */
    public ArrayList<NodeEM> getNonPreferredTerms(String idConcept, String idThesaurus, String idLang) {

        ArrayList<NodeEM> nodeEMList = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery( "SELECT lexical_value, created, modified, source, status, hiden " +
                        " FROM non_preferred_term, preferred_term " +
                        " WHERE " +
                        " non_preferred_term.id_term = preferred_term.id_term" +
                        " and" +
                        " non_preferred_term.id_thesaurus = preferred_term.id_thesaurus" +
                        " and" +
                        " preferred_term.id_concept = '" + idConcept + "'" +
                        " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'" +
                        " and non_preferred_term.lang ='" + idLang + "'" +
                        " order by lexical_value ASC");
                try (ResultSet resultSet = stmt.getResultSet()){
                        nodeEMList = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeEM nodeEM = new NodeEM();
                            nodeEM.setLexicalValue(resultSet.getString("lexical_value"));
                            nodeEM.setCreated(resultSet.getDate("created"));
                            nodeEM.setModified(resultSet.getDate("modified"));
                            nodeEM.setSource(resultSet.getString("source"));
                            nodeEM.setStatus(resultSet.getString("status"));
                            nodeEM.setHiden(resultSet.getBoolean("hiden"));
                            nodeEM.setLang(idLang);
                            nodeEMList.add(nodeEM);
                        }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting NonPreferedTerm of Term : " + idConcept, sqle);
        }
        return nodeEMList;
    }    
    
    
    /**
     * Cette fonction permet de savoir si le terme est un parfait doublon ou non
     */
    public boolean isPrefLabelExist(String title, String idThesaurus, String idLang) {

        boolean existe = false;
        
        title = StringUtils.convertString(title);
        title = StringUtils.unaccentLowerString(title);

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_term from term where f_unaccent(lower(term.lexical_value)) like '"
                        + title + "' and lang = '" + idLang + "' and id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Title of Term exist : " + title, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le synonyme est un parfait doublon ou
     * non
     */
    public boolean isAltLabelExist(String title, String idThesaurus, String idLang) {

        boolean existe = false;

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_term from non_preferred_term where f_unaccent(lower(lexical_value)) like '"
                        + fr.cnrs.opentheso.utils.StringUtils.convertString(title) + "' and lang = '" + idLang + "' and id_thesaurus = '"
                        + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if Title of altLabel exist : " + fr.cnrs.opentheso.utils.StringUtils.convertString(title), sqle);
        }
        return existe;
    }

    /**
     * Permet de modifier le libellé d'un synonyme
     */
    public boolean updateTermSynonyme(String oldValue, String newValue, String idTerm, String idLang,
            String idTheso, boolean isHidden, int idUser) {

        Connection conn = null;
        Statement stmt;
        boolean isPassed = false;
        

        oldValue = StringUtils.convertString(oldValue);
        newValue = StringUtils.convertString(newValue);
        try {
            conn = dataSource.getConnection();
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
     */
    public boolean updateStatus(String idTerm, String value, String idLang, String idTheso, boolean isHidden, int idUser) {

        Connection conn = null;
        Statement stmt;
        boolean isPassed = false;
        value = (fr.cnrs.opentheso.utils.StringUtils.convertString(value));
        try {
            conn = dataSource.getConnection();
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
     */
    public boolean deleteNonPreferedTerm(String idTerm, String idLang,
            String lexicalValue, String idTheso, int idUser) {

        lexicalValue = fr.cnrs.opentheso.utils.StringUtils.convertString(lexicalValue);
        boolean isPassed = false;
        try (Connection conn = dataSource.getConnection()){
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from non_preferred_term where"
                            + " id_thesaurus = '" + idTheso + "'"
                            + " and id_term  = '" + idTerm + "'"
                            + " and lexical_value  ilike '" + lexicalValue + "'"
                            + " and lang  = '" + idLang + "'");

                addNonPreferredTermHistorique(conn, idTerm, lexicalValue, idLang, idTheso, "", "", false, "delete", idUser);
                isPassed = true;
            }
        } catch (SQLException sqle) {
            Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return isPassed;
    }
    
    /**
     * Cette fonction permet de supprimer un Terme Non descripteur ou synonyme
     */
    public boolean deleteAllNonPreferedTerm(String idConcept, String idTheso) {

        boolean isPassed = false;
        try (Connection conn = dataSource.getConnection()){
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM non_preferred_term" +
                        " USING preferred_term" +
                        " WHERE non_preferred_term.id_thesaurus = preferred_term.id_thesaurus" +
                        " AND non_preferred_term.id_term = preferred_term.id_term" +
                        " AND preferred_term.id_concept = '" + idConcept + "'" +
                        " AND non_preferred_term.id_thesaurus = '" + idTheso + "'");

                isPassed = true;
            }
        } catch (SQLException sqle) {
            Logger.getLogger(TermHelper.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return isPassed;
    }    

    /**
     * Cette fonction permet de rajouter des Termes Non descripteurs ou synonymes
     */
    public boolean addNonPreferredTerm(String idTerm, String value, String idLang, String idTheso,
                                       String source, String status, boolean isHidden, int idUser) {

        boolean isPassed = false;
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        try (Connection conn = dataSource.getConnection()){
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("Insert into non_preferred_term "
                            + "(id_term, lexical_value, lang, "
                            + "id_thesaurus, source, status, hiden)"
                            + " values ("
                            + "'" + idTerm + "'"
                            + ",'" + value + "'"
                            + ",'" + idLang + "'"
                            + ",'" + idTheso + "'"
                            + ",'" + source + "'"
                            + ",'" + status + "'"
                            + "," + isHidden + ")");
            }
            addNonPreferredTermHistorique(conn, idTerm, value, idLang, idTheso, source, status, isHidden, "ADD", idUser);            
            isPassed = true;
        } catch (SQLException sqle) {
            log.error("Error while adding NonPreferredTerm : " + idTerm, sqle);
        }
        return isPassed;
    }

    private boolean addNonPreferredTermHistorique(Connection conn, String idTerm, String value, String idLang,
                                                  String idTheso, String source, String status, boolean isHidden, String action, int idUser) {

        try (Statement stmt = conn.createStatement()){
            stmt.executeUpdate("Insert into non_preferred_term_historique (id_term, lexical_value, lang, id_thesaurus, source, status, id_user, action, hiden)"
                    + " values ('" + idTerm + "','" + value + "','" + idLang + "','" + idTheso + "','" + source + "'"
                    + ",'" + status + "','" + idUser + "','" + action + "'," + isHidden + ")");
            return true;
        } catch (SQLException sqle) {
            // Log exception
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cette fonction permet de supprimer une traduction
     */
    public boolean deleteTraductionOfTerm(String idTerm, String oldLabel, String idLang, String idTheso, int idUser) {

        boolean status = false;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from term where"
                        + " id_thesaurus = '" + idTheso + "'"
                        + " and id_term  = '" + idTerm + "'"
                        + " and lang = '" + idLang + "'");
            }
            status = true;
        } catch (SQLException sqle) {
            log.error("Error while deleting traduction of Term : " + idTerm, sqle);
        }
        return status;
    }

    /**
     * Permet d'ajouter une traduction à un Terme #MR
     */
    public boolean addTraduction(String label, String idTerm, String idLang, String source, String status, String idTheso, int idUser) {

        boolean passed = false;
        label = fr.cnrs.opentheso.utils.StringUtils.convertString(label);
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into term "
                        + "(id_term, lexical_value, lang, id_thesaurus, source, status,contributor, creator)"
                        + " values ("
                        + "'" + idTerm + "'"
                        + ",'" + label + "'"
                        + ",'" + idLang + "'"
                        + ",'" + idTheso + "'"
                        + ",'" + source + "'"
                        + ",'" + status + "'"
                        + ", " + idUser
                        + ", " + idUser + ")");
                if (addNewTermHistorique(conn, idTerm, label, idLang, idTheso, "", "New", idUser)) {
                    passed = true;
                }
            }
        } catch (SQLException sqle) {
            // duppliqué
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                passed = true;
            }
        }
        return passed;
    }

    /**
     * fonction qui permet de mettre à jour un label
     */
    public boolean updateTraduction(
            String label,
            String idTerm,
            String idLang,
            String idTheso,
            int idUser) {
        Connection conn = null;
        Statement stmt;
        boolean status = false;
        label = fr.cnrs.opentheso.utils.StringUtils.convertString(label);
        try {
            conn = dataSource.getConnection();
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
                    Logger.getLogger(TermHelper.class
                            .getName()).log(Level.SEVERE, null, ex);
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
     */
    public String getLexicalValue(
            String idTerm, String idThesaurus, String idLang) {

        String lexicalValue = "";
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lexical_value from term where"
                            + " term.id_thesaurus = '" + idThesaurus + "'"
                            + " and term.id_term = '" + idTerm + "'"
                            + " and term.lang = '" + idLang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        lexicalValue = resultSet.getString("lexical_value");
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting LexicalValue of Term : " + idTerm, sqle);
        }
        return lexicalValue.trim();
    }

    /**
     * Cette fonction permet de récupérer les altLabels d'un Concept d'après son idTerm
     * sinon renvoie un tableau vide
     */
    public ArrayList<String> getLexicalValueOfAltLabel(String idTerm, String idThesaurus, String idLang) {

        ArrayList<String> lexicalValues = new ArrayList<>();

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lexical_value from non_preferred_term where "
                        + " non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                        + " and"
                        + " non_preferred_term.id_term = '" + idTerm + "'"
                        + " and"
                        + " non_preferred_term.lang = '" + idLang + "'"
                        + " and "
                        + " non_preferred_term.hiden != 'true' ");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        lexicalValues.add(resultSet.getString("lexical_value"));
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting altLabels of Term : " + idTerm, sqle);
        }
        return lexicalValues;
    }


    public boolean addNewTermHistorique(Connection conn, String idTerm, String lexicalValue, String idLang, String idTheso, String source, String action, int idUser) {

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
     * Permet de savoir si le terme existe dans cette langue ou non
     */
    public boolean isTermExistInThisLang(String idTerm, String idLang, String idThesaurus) {

        Statement stmt;
        ResultSet resultSet = null;
        Connection conn;
        boolean existe = false;
        try {
            conn = dataSource.getConnection();
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
            log.error("Error while asking if is Term exist in this lang : " + idTerm, sqle);
        }
        return existe;
    }


    /**
     * Cette fonction permet d'ajouter un Terme à la table Term, en paramètre un
     * objet Classe Term
     */
    public String addTerm(Term term, String idConcept, int idUser) {

        String idTerm = addNewTerm(term, idUser);

        if (idTerm == null) {
            return null;
        }

        term.setIdTerm(idTerm);
        if (!addLinkTerm(term, idConcept)) {
            return null;
        }

        return idTerm;
    }

    /**
     * Cette fonction permet d'ajouter un Terme à la table Term, en paramètre un
     * objet Classe Term
     */
    public boolean insertTerm(NodeTerm nodeTerm, int idUser) {
        if (nodeTerm.getNodeTermTraduction().isEmpty()) {
            return false;
        }

        for (int i = 0; i < nodeTerm.getNodeTermTraduction().size(); i++) {
            insertTermTraduction(nodeTerm.getIdTerm(),
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
        insertLinkTerm(nodeTerm.getIdTerm(), nodeTerm.getIdThesaurus(), nodeTerm.getIdConcept());

        return true;
    }


    public void insertLinkTerm(String idTerm, String idThesaurus, String idConcept) {

        Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
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
     */
    public boolean addLinkTerm(Term term, String idConcept) {
        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Insert into preferred_term (id_concept, id_term, id_thesaurus) values ('"
                    + idConcept + "','" + term.getIdTerm() + "','" + term.getIdThesaurus() + "')");
            return true;
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                return true;
            }
            return false;
        }
    }


    public String addNewTerm(Term term, int idUser) {
        String idTerm = null;
        term.setLexicalValue(fr.cnrs.opentheso.utils.StringUtils.convertString(term.getLexicalValue()));
        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select max(id) from term");
            try ( ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet.next()) {
                    int idTermNum = resultSet.getInt(1);
                    idTermNum++;
                    idTerm = "" + (idTermNum);
                    // si le nouveau Id existe, on l'incrémente
                    while (isIdOfTermExist(conn, idTerm, term.getIdThesaurus())) {
                        idTerm = "" + (++idTermNum);
                    }
                }
                term.setIdTerm(idTerm);
            }
            if (term.getIdTerm() == null || term.getIdTerm().isEmpty()) {
                return null;
            }

            stmt.executeUpdate("Insert into term (id_term, lexical_value, lang, id_thesaurus, source, status, contributor, creator)"
                    + " values ('" + term.getIdTerm() + "','" + term.getLexicalValue() + "','" + term.getLang() + "'"
                    + ",'" + term.getIdThesaurus() + "','" + term.getSource() + "','" + term.getStatus() + "'"
                    + ", " + idUser + ", " + idUser + ")");
            if (!addNewTermHistorique(conn, term, idUser, "ADD")) {
                return null;
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                idTerm = null;
            }
        }

        return idTerm;
    }


    public boolean addNewTermHistorique(Connection conn, Term term, int idUser, String action) {

        try ( Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Insert into term_historique "
                    + "(id_term, lexical_value, lang, "
                    + "id_thesaurus, source, status, id_user, action)"
                    + " values ("
                    + "'" + term.getIdTerm() + "'"
                    + ",'" + term.getLexicalValue() + "'"
                    + ",'" + term.getLang() + "'"
                    + ",'" + term.getIdThesaurus() + "'"
                    + ",'" + term.getSource() + "'"
                    + ",'" + term.getStatus() + "'"
                    + "," + idUser
                    + ",'" + action + "')");
            return true;
        } catch (SQLException sqle) {
            System.out.println("Error : " + sqle.getMessage());
            return false;
        }
    }

    /**
     * Cette fonction permet de supprimer un Terme avec toutes les dépendances
     * (Prefered term dans toutes les langues) et (nonPreferedTerm dans toutes
     * les langues)
     */
    public boolean deleteTerm(Connection conn, String idTerm, String idThesaurus) {

        try ( Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("delete from term where id_thesaurus = '" + idThesaurus + "' and id_term  = '" + idTerm + "'");

            // Suppression de la relation Term_Concept
            stmt.executeUpdate("delete from preferred_term where id_thesaurus = '" + idThesaurus + "' and id_term  = '" + idTerm + "'");

            // suppression des termes synonymes
            stmt.executeUpdate("delete from non_preferred_term where id_thesaurus = '" + idThesaurus + "'"
                    + " and id_term  = '" + idTerm + "'");
            return true;
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting Term and relations : " + idTerm, sqle);
            return false;
        }
    }

    /**
     * Cette fonction permet de rajouter des Termes Non descripteurs ou
     * synonymes
     */
    public boolean addNonPreferredTerm(Term term, int idUser) {

        try ( Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            if (!addUSE(conn, term)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!addUSEHistorique(conn, term, idUser, "ADD")) {
                conn.rollback();
                conn.close();
                return false;
            }

            conn.commit();
            conn.close();
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(TermHelper.class
                    .getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private boolean addUSE(Connection conn, Term term) {

        term.setLexicalValue(fr.cnrs.opentheso.utils.StringUtils.convertString(term.getLexicalValue()));
        try ( Statement stmt = conn.createStatement()) {
            String query = "Insert into non_preferred_term "
                    + "(id_term, lexical_value, lang, "
                    + "id_thesaurus, source, status, hiden)"
                    + " values ("
                    + "'" + term.getIdTerm() + "'"
                    + ",'" + term.getLexicalValue() + "'"
                    + ",'" + term.getLang() + "'"
                    + ",'" + term.getIdThesaurus() + "'"
                    + ",'" + term.getSource() + "'"
                    + ",'" + term.getStatus() + "'"
                    + "," + term.isHidden() + ")";

            stmt.executeUpdate(query);
            return true;
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                return true;
            }
        }
        return false;
    }

    private boolean addUSEHistorique(Connection conn, Term term, int idUser, String action) {

        term.setLexicalValue(fr.cnrs.opentheso.utils.StringUtils.convertString(term.getLexicalValue()));
        try ( Statement stmt = conn.createStatement()) {
            String query = "Insert into non_preferred_term_historique "
                    + "(id_term, lexical_value, lang, "
                    + "id_thesaurus, source, status, id_user, action)"
                    + " values ("
                    + "'" + term.getIdTerm() + "'"
                    + ",'" + term.getLexicalValue() + "'"
                    + ",'" + term.getLang() + "'"
                    + ",'" + term.getIdThesaurus() + "'"
                    + ",'" + term.getSource() + "'"
                    + ",'" + term.getStatus() + "'"
                    + ",'" + idUser + "'"
                    + ",'" + action + "')";

            stmt.executeUpdate(query);
            return true;
        } catch (SQLException sqle) {
            return false;
        }
    }

    /**
     * #### déprécié par MR utiliser la nouvelle fonction Cette fonction permet
     * d'ajouter une traduction à un Terme
     */
    public boolean addTermTraduction(Connection conn, Term term, int idUser) {

        term.setLexicalValue(fr.cnrs.opentheso.utils.StringUtils.convertString(term.getLexicalValue()));
        try ( Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Insert into term (id_term, lexical_value, lang, id_thesaurus, source, status,contributor, creator)"
                    + " values ('" + term.getIdTerm() + "','" + term.getLexicalValue() + "','" + term.getLang() + "'"
                    + ",'" + term.getIdThesaurus() + "','" + term.getSource() + "','" + term.getStatus() + "'"
                    + ", " + term.getContributor() + ", " + term.getCreator() + ")");
            addNewTermHistorique(conn, term, idUser, "ADD");
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
     */
    public boolean insertTermTraduction(String idTerm, String idConcept, String lexicalValue,
            String lang, String idThesaurus, Date created, Date modified, String source, String status, int idUser) {

        Connection conn;
        Statement stmt;
        boolean etat = false;

        // cette fonction permet de remplir la table Permutée
        splitConceptForPermute(idConcept, getGroupIdOfConcept(idTerm, idThesaurus), idThesaurus, lang, lexicalValue);

        lexicalValue = fr.cnrs.opentheso.utils.StringUtils.convertString(lexicalValue);
        String query;
        try {
            // Get connection from pool
            conn = dataSource.getConnection();
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

    private String getGroupIdOfConcept(String idConcept, String idThesaurus) {

        String idGroup = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select idgroup from concept_group_concept where idthesaurus = '"
                        + idThesaurus + "' and idconcept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        if (resultSet.next()) {
                            idGroup = resultSet.getString("idgroup");
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Id of group of Concept : " + idConcept, sqle);
        }
        return idGroup;
    }

    /**
     * Cette fonction permet de découper les mots d'un concept (phrase) pour
     * remplir la table permutée
     *
     * @param idConcept
     * @param idGroup
     * @param lexicalValue
     * @param idLang
     * @param idThesaurus
     */
    public void splitConceptForPermute(String idConcept, String idGroup, String idThesaurus, String idLang, String lexicalValue) {

        Connection conn;
        Statement stmt;

        //ici c'est la fonction qui découpe la phrase en mots séparé pour la recherche permutée
        lexicalValue = lexicalValue.replaceAll("-", " ");
        lexicalValue = lexicalValue.replaceAll("\\(", " ");
        lexicalValue = lexicalValue.replaceAll("\\)", " ");
        lexicalValue = lexicalValue.replaceAll("\\/", " ");
        lexicalValue = fr.cnrs.opentheso.utils.StringUtils.convertString(lexicalValue.trim());
        String tabMots[] = lexicalValue.split(" ");

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
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
     * Fonction qui permet de mettre à jour une traduction
     */
    public boolean updateTermTraduction(Term term, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;
        term.setLexicalValue(StringUtils.convertString(term.getLexicalValue()));
        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE term set"
                            + " lexical_value = '" + term.getLexicalValue() + "',"
                            + " modified = current_date ,"
                            + " contributor = " + idUser
                            + " WHERE lang ='" + term.getLang() + "'"
                            + " AND id_term = '" + term.getIdTerm() + "'"
                            + " AND id_thesaurus = '" + term.getIdThesaurus() + "'";

                    stmt.executeUpdate(query);
                    status = true;

                    addNewTermHistorique(conn, term, idUser, "UPDATE");
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating Term Traduction : " + term.getIdTerm(), sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de récupérer un Term par son id et son thésaurus et
     * sa langue sous forme de classe Term (sans les relations)
     */
    public Term getThisTerm(String idConcept, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        Term term = null;

        if (isTraductionExistOfConcept(idConcept, idThesaurus, idLang)) {
            try {
                // Get connection from pool
                conn = dataSource.getConnection();
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

                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        if (resultSet.next()) {
                            term = new Term();
                            term.setIdTerm(resultSet.getString("id_term"));
                            term.setLexicalValue(resultSet.getString("lexical_value"));
                            term.setLang(idLang);
                            term.setIdThesaurus(idThesaurus);
                            term.setCreated(resultSet.getDate("created"));
                            term.setModified(resultSet.getDate("modified"));
                            term.setSource(resultSet.getString("source"));
                            term.setStatus(resultSet.getString("status"));
                            term.setContributor(resultSet.getInt("contributor"));
                            term.setCreator(resultSet.getInt("creator"));
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
                log.error("Error while getting Concept : " + idConcept, sqle);
            }
        } else {
            try {
                // Get connection from pool
                conn = dataSource.getConnection();
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
                            term.setIdTerm("");
                            term.setLexicalValue("");
                            term.setLang(idLang);
                            term.setIdThesaurus(idThesaurus);
                            term.setCreated(resultSet.getDate("created"));
                            term.setModified(resultSet.getDate("modified"));
                            term.setStatus(resultSet.getString("status"));
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
                log.error("Error while getting Concept : " + idConcept, sqle);
            }

        }

        return term;
    }

    /**
     * Cette fonction permet de retourner l'id du terme d'après un concept
     */
    public String getIdTermOfConcept(String idConcept, String idThesaurus) {

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_term FROM preferred_term WHERE id_thesaurus = '" + idThesaurus
                        + "' and id_concept = '" + idConcept + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        return resultSet.getString("id_term");
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idTerm of idConcept : " + idConcept, sqle);
            return null;
        }
    }

    /**
     * Cette fonction permet de récupérer les termes synonymes suivant un
     * id_term et son thésaurus et sa langue sous forme de classe NodeEM
     */
    public ArrayList<String> getNonPreferredTermsLabel(String idConcept, String idThesaurus, String idLang) {

        ArrayList<String> listAltLabel = new ArrayList<>();

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT non_preferred_term.lexical_value"
                        + " FROM non_preferred_term, preferred_term"
                        + " WHERE"
                        + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = non_preferred_term.id_term"
                        + " and"
                        + " preferred_term.id_concept = '" + idConcept + "'"
                        + " and "
                        + " non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                        + " and "
                        + " non_preferred_term.lang ='" + idLang + "'"
                        + " order by unaccent(lower(lexical_value)) ");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listAltLabel.add(resultSet.getString("lexical_value"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting NonPreferedTerm of Term : " + idConcept, sqle);
        }
        return listAltLabel;
    }

    /**
     * Cette fonction permet de récupérer les termes synonymes suivant un
     * id_term et son thésaurus et sa langue sous forme de classe NodeEM
     */
    public ArrayList<NodeEM> getAllNonPreferredTerms(String idConcept, String idThesaurus) {

        ArrayList<NodeEM> nodeEMList = null;

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT  non_preferred_term.lexical_value, non_preferred_term.created, non_preferred_term.modified,"
                        + " non_preferred_term.source,  non_preferred_term.status, non_preferred_term.hiden, non_preferred_term.lang"
                        + " FROM non_preferred_term, preferred_term"
                        + " WHERE preferred_term.id_term = non_preferred_term.id_term "
                        + " AND preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                        + " AND preferred_term.id_concept = '" + idConcept
                        + "' AND non_preferred_term.id_thesaurus = '" + idThesaurus
                        + "' ORDER BY non_preferred_term.lexical_value ASC;");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    nodeEMList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeEM nodeEM = new NodeEM();
                        nodeEM.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeEM.setCreated(resultSet.getDate("created"));
                        nodeEM.setModified(resultSet.getDate("modified"));
                        nodeEM.setSource(resultSet.getString("source"));
                        nodeEM.setStatus(resultSet.getString("status"));
                        nodeEM.setHiden(resultSet.getBoolean("hiden"));
                        nodeEM.setLang(resultSet.getString("lang"));
                        nodeEMList.add(nodeEM);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting NonPreferedTerm of Concept : " + idConcept, sqle);
        }

        return nodeEMList;
    }

    /**
     * Cette fonction permet de savoir si le terme existe ou non
     */
    public boolean isTraductionExistOfConcept(String idConcept, String idThesaurus, String idLang) {

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
            log.error("Error while asking if Traduction of Concept exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de retourner les traductions d'un term sauf la
     * langue en cours
     */
    public ArrayList<NodeTermTraduction> getTraductionsOfConcept(String idConcept, String idThesaurus, String idLang) {

        ArrayList<NodeTermTraduction> nodeTraductionsList = null;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("SELECT term.id_term, term.lexical_value, lang.code_pays, lang.iso639_1, " +
                        "CASE WHEN '"+idLang+"' = 'fr' THEN lang.french_name ELSE lang.english_name END lang_name " +
                        "FROM term, preferred_term, languages_iso639 lang " +
                        "WHERE term.id_term = preferred_term.id_term " +
                        "and term.id_thesaurus = preferred_term.id_thesaurus " +
                        "and term.lang = lang.iso639_1 " +
                        "and preferred_term.id_concept = '" + idConcept + "' " +
                        "and term.lang != '" + idLang + "' " +
                        "and term.id_thesaurus = '" + idThesaurus + "' " +
                        "order by term.lexical_value");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        nodeTraductionsList = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeTermTraduction nodeTraductions = new NodeTermTraduction();
                            nodeTraductions.setCodePays(resultSet.getString("code_pays"));
                            nodeTraductions.setLang(resultSet.getString("iso639_1"));
                            nodeTraductions.setLexicalValue(resultSet.getString("lexical_value"));
                            nodeTraductions.setNomLang(resultSet.getString("lang_name"));
                            nodeTraductionsList.add(nodeTraductions);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Traductions of Term  : " + idConcept, sqle);
        }
        return nodeTraductionsList;
    }

    /**
     * Cette fonction permet de retourner toutes les traductions d'un concept
     */
    public ArrayList<NodeTermTraduction> getAllTraductionsOfConcept(String idConcept, String idThesaurus) {

        ArrayList<NodeTermTraduction> nodeTraductionsList = new ArrayList<>();

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT term.id_term, term.lexical_value, term.lang FROM"
                        + " term, preferred_term WHERE term.id_term = preferred_term.id_term"
                        + " and term.id_thesaurus = preferred_term.id_thesaurus"
                        + " and preferred_term.id_concept = '" + idConcept + "'"
                        + " and term.id_thesaurus = '" + idThesaurus + "' order by term.lexical_value");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeTermTraduction nodeTraductions = new NodeTermTraduction();
                        nodeTraductions.setLang(resultSet.getString("lang"));
                        nodeTraductions.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeTraductionsList.add(nodeTraductions);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All Traductions of Concept  : " + idConcept, sqle);
        }
        return nodeTraductionsList;
    }

    /**
     * Cette fonction permet de savoir si le terme est un parfait doublon ou non
     * si oui, on retourne l'identifiant, sinon, on retourne null
     */
    public String isTermEqualTo(String title, String idThesaurus, String idLang) {

        String idTerm = null;

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_term from term where lexical_value = '" + fr.cnrs.opentheso.utils.StringUtils.convertString(title)
                        + "' and lang = '" + idLang + "' and id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idTerm = resultSet.getString("id_term");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Term exist : " + title, sqle);
        }
        return idTerm;
    }

    /**
     * Cette fonction permet de savoir si le terme existe ou non en ignorant
     * uniquement la casse
     */
    public boolean isTermExistIgnoreCase(String title, String idThesaurus, String idLang) {

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_term from term where lexical_value ilike '"
                        + fr.cnrs.opentheso.utils.StringUtils.convertString(title) + "'  and lang = '" + idLang
                        + "' and id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        return resultSet.getRow() != 0;
                    }
                    return false;
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Title of Term exist : " + title, sqle);
            return false;
        }
    }

    /**
     * Cette fonction permet de savoir si le terme existe ou non
     */
    public boolean isTermExist(String title, String idThesaurus, String idLang) {

        
        title = StringUtils.convertString(title);
        title = StringUtils.unaccentLowerString(title);

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_term from term where f_unaccent(lower(lexical_value)) like '" + title
                        + "' and lang = '" + idLang + "' and id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        return resultSet.getRow() != 0;
                    }
                    return false;
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Title of Term exist : " + title, sqle);
            return false;
        }
    }

    /**
     * Cette fonction permet de retourner l'ID du createur
     */
    public int getCreator(String idThesaurus, String idTerm, String idLang) {
        int idCreator = -1;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select creator from term where id_thesaurus = '" + idThesaurus + "' and id_term = '" + idTerm + "' and lang = '" + idLang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if ((resultSet.getInt("creator") != -1) && (resultSet.getInt("creator") != 0)) {
                            idCreator = resultSet.getInt("creator");
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting creator of : " + idTerm, sqle);
        }
        return idCreator;
    }

    /**
     * Cette fonction permet de retourner l'ID du contributeur

     */
    public int getContributor(String idThesaurus, String idTerm, String idLang) {
        int idContributor = -1;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select contributor from term where id_thesaurus = '" + idThesaurus + "' and id_term = '" + idTerm + "' and lang = '" + idLang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if ((resultSet.getInt("contributor") != -1) && (resultSet.getInt("contributor") != 0)) {
                            idContributor = resultSet.getInt("contributor");
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting contributor of : " + idTerm, sqle);
        }
        return idContributor;
    }

    /**
     * Cette fonction permet de savoir si le terme existe ou non
     */
    public boolean isIdOfTermExist(Connection conn, String idTerm, String idThesaurus) {
        try ( Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select id_term from term where id_term = '" + idTerm + "' and id_thesaurus = '" + idThesaurus + "'");
            try ( ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet.next()) {
                    return resultSet.getRow() != 0;
                }
                return false;
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id of Term exist : " + idTerm, sqle);
            return false;
        }
    }

}
