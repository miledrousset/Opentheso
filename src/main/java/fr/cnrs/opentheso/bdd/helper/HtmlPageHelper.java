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
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Miled.Rousset
 */
public class HtmlPageHelper {

    private final Log log = LogFactory.getLog(HtmlPageHelper.class);

    /**
     * cette fonction permet de retourner le Home Page d'Opentheso c'est la page
     * de présentation de l'instance pour tous les thésaurus
     *
     * @param ds
     * @param idLang
     * @return #MR
     */
    public String getHomePage(HikariDataSource ds, String idLang) {
        String homePage = "";

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT htmlcode from homepage where lang ='" + idLang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        homePage = resultSet.getString("htmlcode");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("error while trying to get homepage ", sqle);
        }        
        return homePage;
    }

    /**
     * cette fonction permet de retourner le Home Page d'Opentheso c'est la page
     * de présentation de l'instance pour tous les thésaurus
     *
     * @param ds
     * @param htmlText
     * @param idLang
     * @return #MR
     */
    public boolean setHomePage(HikariDataSource ds, String htmlText, String idLang) {

        if (isHomeExist(ds, idLang)) {
            if (!updateHome(ds, htmlText, idLang)) {
                return false;
            }
        } else {
            if (!insertHome(ds, htmlText, idLang)) {
                return false;
            }
        }
        return true;
    }

    private boolean updateHome(HikariDataSource ds, String htmlText, String idLang) {

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("update homepage set htmlcode = '" + new StringPlus().convertString(htmlText) + "'"
                        + " where lang = '" + idLang + "'");
                return true;
            }
        } catch (SQLException ex) {
            this.log.error("error while updating homepage", ex);
        }
        return false;
    }

    private boolean insertHome(HikariDataSource ds, String htmlText, String idLang) {
        
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("insert into homepage (htmlcode, lang) values ('" + new StringPlus().convertString(htmlText) + "', '" + idLang + "')");
                return true;
            }
        } catch (SQLException ex) {
            this.log.error("error while inserting homepage", ex);
        }
        return false;
    }

    /**
     * cette fonction permet de retourner le Theso Home Page du thésaurus
     * séléectionné
     *
     * @param ds
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public String getThesoHomePage(HikariDataSource ds,
            String idTheso, String idLang) {
        String homePage = "";

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT htmlcode from thesohomepage where"
                            + " lang ='" + idLang + "'"
                            + " and"
                            + " idtheso = '" + idTheso + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        homePage = resultSet.getString("htmlcode");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("error while trying to get thesoHomepage : " + idTheso, sqle);
        }        
        return homePage;
    }

    /**
     * cette fonction permet de sauvegarger le Home Page d'un thésaurus
     *
     * @param ds
     * @param htmlText
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public boolean setThesoHomePage(HikariDataSource ds,
            String htmlText,
            String idTheso,
            String idLang) {

        if (isThesoHomeExist(ds, idTheso, idLang)) {
            if (!updateThesoHome(ds,
                    htmlText,
                    idTheso,
                    idLang)) {
                return false;
            }
        } else {
            if (!insertThesoHome(ds,
                    htmlText,
                    idTheso,
                    idLang)) {
                return false;
            }
        }
        return true;
    }

    private boolean updateThesoHome(HikariDataSource ds,
            String htmlText,
            String idTheso,
            String idLang) {

        StringPlus stringPlus = new StringPlus();
        htmlText = stringPlus.convertString(htmlText);

        String query;
        Statement stmt;
        Connection conn;
        boolean isPassed = false;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "update thesohomepage set "
                            + " htmlcode = '" + htmlText + "'"
                            + " where lang = '" + idLang + "'"
                            + " and idtheso = '" + idTheso + "'";
                    stmt.executeUpdate(query);
                    isPassed = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            this.log.error("error while updating thesohomepage", ex);
        }
        return isPassed;
    }

    private boolean insertThesoHome(HikariDataSource ds,
            String htmlText,
            String idTheso,
            String idLang) {

        StringPlus stringPlus = new StringPlus();
        htmlText = stringPlus.convertString(htmlText);

        String query;
        Statement stmt;
        Connection conn;
        boolean isPassed = false;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "insert into thesohomepage "
                            + "(htmlcode, lang, idtheso) values"
                            + " ('" + htmlText + "', "
                            + " '" + idLang + "',"
                            + " '" + idTheso + "')";
                    stmt.executeUpdate(query);
                    isPassed = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            this.log.error("error while inserting thesohomepage", ex);
        }
        return isPassed;
    }

    /**
     * vérifie si la page d'accueil du thésaurus exist déjà
     *
     * @param ds
     * @param idTheso
     * @param idLang
     * @return
     */
    private boolean isThesoHomeExist(
            HikariDataSource ds,
            String idTheso,
            String idLang) {

        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idtheso from thesohomepage where "
                            + " idtheso = '" + idTheso + "'"
                            + " and lang = '" + idLang + "'";
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
            log.error("Error while asking if thesoHome exist : " + idTheso, sqle);
        }
        return existe;
    }

    /**
     * vérifie si la page Home d'Opentheso exist déjà
     *
     * @param ds
     * @param idLang
     * @return
     */
    private boolean isHomeExist(HikariDataSource ds, String idLang) {

        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lang from homepage where "
                            + " lang = '" + idLang + "'";
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
            log.error("Error while asking if Home of opentheso exist : " + idLang, sqle);
        }
        return existe;
    }

    /**
     * cette fonction permet de retourner le copyright d'un thésaurus
     *
     * @param ds
     * @param idTheso
     * @return #MR
     */
    public String getCopyright(HikariDataSource ds, String idTheso) {
        String copyright = "";
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT copyright FROM copyright WHERE copyright.id_thesaurus='" + idTheso + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        copyright = resultSet.getString("copyright");
                    }
                }
            }
        } catch (SQLException ex) {
            this.log.error("error while trying to proced result from database", ex);
        }
        return copyright;
    }

    /**
     * cette fonction permet de mettre à jour le copyright du thésaurus (dans ce
     * cas, un copyright est déjà existant)
     *
     * @param ds
     * @param idTheso
     * @param copyright
     * @return
     */
    public boolean updateCopyright(HikariDataSource ds, String idTheso, String copyright) {
        boolean status = false;
        copyright = new StringPlus().convertString(copyright);
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("update copyright SET copyright= '"
                        + copyright + "' WHERE id_thesaurus='" + idTheso + "'");
                status = true;
            }
        } catch (SQLException ex) {
            this.log.error("error while trying to update copyright", ex);
        }
        return status;
    }

    /**
     * permet d'ajouter un copyright à un thésaurus
     */
    public boolean addCopyright(HikariDataSource ds, String idTheso, String copyright) {

        boolean status = false;
        copyright = new StringPlus().convertString(copyright);
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("INSERT INTO copyright (id_thesaurus,copyright) VALUES ('"
                        + idTheso + "','" + copyright + "')");
                status = true;
            }
        } catch (SQLException ex) {
            this.log.error("error while trying to insert a copyright", ex);
        }
        return status;
    }

    /**
     * permet de savoir si le thésaurus a un copyright
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public boolean isThesoHaveCopyRight(HikariDataSource ds,
            String idThesaurus) {

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
                    String query = "select id_thesaurus from copyright where "
                            + " id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = true;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if theso have a copyright ", sqle);
        }
        return existe;
    }

}
