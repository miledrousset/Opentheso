package fr.cnrs.opentheso.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.cnrs.opentheso.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Data
@Slf4j
@Service
public class HtmlPageHelper {

    @Autowired
    private DataSource dataSource;

    /**
     * cette fonction permet de retourner le Home Page d'Opentheso c'est la page
     * de présentation de l'instance pour tous les thésaurus
     *
     * @param idLang
     * @return #MR
     */
    public String getHomePage(String idLang) {

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT htmlcode from homepage where lang ='" + idLang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        return resultSet.getString("htmlcode");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("error while trying to get homepage ", sqle);
        }        
        return "";
    }

    /**
     * cette fonction permet de retourner le Home Page d'Opentheso c'est la page
     * de présentation de l'instance pour tous les thésaurus
     *
     * @param htmlText
     * @param idLang
     * @return #MR
     */
    public boolean setHomePage(String htmlText, String idLang) {

        if (isHomeExist(idLang)) {
            if (!updateHome(htmlText, idLang)) {
                return false;
            }
        } else {
            if (!insertHome(htmlText, idLang)) {
                return false;
            }
        }
        return true;
    }

    private boolean updateHome(String htmlText, String idLang) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("update homepage set htmlcode = '" + fr.cnrs.opentheso.utils.StringUtils.convertString(htmlText) + "'"
                        + " where lang = '" + idLang + "'");
                return true;
            }
        } catch (SQLException ex) {
            this.log.error("error while updating homepage", ex);
        }
        return false;
    }

    private boolean insertHome(String htmlText, String idLang) {
        
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("insert into homepage (htmlcode, lang) values ('" + fr.cnrs.opentheso.utils.StringUtils.convertString(htmlText) + "', '" + idLang + "')");
                return true;
            }
        } catch (SQLException ex) {
            this.log.error("error while inserting homepage", ex);
        }
        return false;
    }

    /**
     * cette fonction permet de retourner le Theso Home Page du thésaurus séléectionné
     *
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public String getThesoHomePage(String idTheso, String idLang) {

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT htmlcode from thesohomepage where lang ='" + idLang + "'"
                            + " and idtheso = '" + idTheso + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        return resultSet.getString("htmlcode");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("error while trying to get thesoHomepage : " + idTheso, sqle);
        }        
        return "";
    }

    /**
     * cette fonction permet de sauvegarger le Home Page d'un thésaurus
     *
     * @param htmlText
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public boolean setThesoHomePage(String htmlText, String idTheso, String idLang) {

        if (isThesoHomeExist(idTheso, idLang)){
            if(!updateThesoHome(htmlText, idTheso, idLang)) {
                return false;
            } else
                return true;
        }

        if (!insertThesoHome(htmlText, idTheso, idLang)) {
            return false;
        }
        return true;
    }

    private boolean updateThesoHome(String htmlText, String idTheso, String idLang) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                htmlText = StringUtils.convertString(htmlText);
                stmt.executeUpdate("update thesohomepage set htmlcode = '" + htmlText + "'"
                        + " where lang = '" + idLang + "' and idtheso = '" + idTheso + "'");
                return true;
            }
        } catch (SQLException ex) {
            this.log.error("error while updating thesohomepage", ex);
        }
        return false;
    }

    private boolean insertThesoHome(String htmlText, String idTheso, String idLang) {

        htmlText = StringUtils.convertString(htmlText);
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("insert into thesohomepage (htmlcode, lang, idtheso) values ('" + htmlText + "', '" + idLang + "', '" + idTheso + "')");
                return true;
            }
        } catch (SQLException ex) {
            this.log.error("error while inserting thesohomepage", ex);
        }
        return false;
    }

    /**
     * vérifie si la page d'accueil du thésaurus exist déjà
     *
     * @param idTheso
     * @param idLang
     * @return
     */
    private boolean isThesoHomeExist(String idTheso, String idLang) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select idtheso from thesohomepage where idtheso = '" + idTheso + "' and lang = '" + idLang + "'");
                var resultSet = stmt.getResultSet();
                if (resultSet.next()) {
                    return resultSet.getRow() != 0;
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if thesoHome exist : " + idTheso, sqle);
        }
        return false;
    }

    /**
     * vérifie si la page Home d'Opentheso exist déjà
     *
     * @param idLang
     * @return
     */
    private boolean isHomeExist(String idLang) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lang from homepage where lang = '" + idLang + "'");
                var resultSet = stmt.getResultSet();
                if (resultSet.next()) {
                    return resultSet.getRow() != 0;
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Home of opentheso exist : " + idLang, sqle);
        }
        return false;
    }

}
