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
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.core.alignment.AlignementSource;
import fr.cnrs.opentheso.core.alignment.GpsPreferences;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Miled.Rousset
 */
public class GpsHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    public GpsHelper() {
    }

    /**
     * permet d'ajouter les coordonnées GPS ou les mettre à jour
     */
    public boolean insertCoordonees(HikariDataSource ds, String idC, String idTheso, double lat, double lon) {
        if (isCoordoneesExist(ds, idC, idTheso)) {
            if (!updateCoordonees(ds, idC, idTheso, lat, lon)) {
                return false;
            }
        } else {
            if (!insertGpsCoordinate(ds, idC, idTheso, lat, lon)) {
                return false;
            }
            if (!updateConcept(ds, idC, idTheso)) {
                return false;
            }
        }
        return true;
    }

    private boolean insertGpsCoordinate(HikariDataSource ds, String idC, String idTheso, double lat, double lon) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into gps values('" + idC + "','" + idTheso + "'," + lat
                        + "," + lon + ")");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while Add coordonnes : " + idC, sqle);
        }
        return status;
    }

    private boolean updateConcept(HikariDataSource ds, String idC, String idTheso) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("update concept set gps = true where id_concept ='" + idC
                        + "' and id_thesaurus ='" + idTheso + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while Add coordonnes : " + idC, sqle);
        }
        return status;
    }

    private boolean updateCoordonees(HikariDataSource ds, String idC, String idTheso, double lat, double lon) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("update gps set latitude = " + lat + ", longitude = " + lon
                        + " where id_concept = '" + idC + "' and id_theso = '" + idTheso + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while update coordonnes : " + idC, sqle);
        }
        return status;
    }

    private boolean isCoordoneesExist(HikariDataSource ds, String idC, String idTheso) {
        boolean existe = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from gps where id_concept ='" + idC
                        + "' and id_theso = '" + idTheso + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Add coordonnes : " + idC, sqle);
        }
        return existe;
    }

    public boolean isHaveCoordinate(HikariDataSource ds, String id_concept, String id_theso) {
        boolean existe = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                try ( ResultSet resultSet = stmt.executeQuery("select gps from concept where id_concept ='"
                        + id_concept + "' and id_thesaurus = '" + id_theso + "'")) {
                    resultSet.next();
                    existe = resultSet.getBoolean("gps");
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Add coordonnes : " + id_concept, sqle);
        }
        return existe;
    }

    /**
     * permet de retourner les coordonnées GPS d'un concept
     */
    public NodeGps getCoordinate(HikariDataSource ds, String id_concept, String id_theso) {
        NodeGps coordonnees = null;
        if (isHaveCoordinate(ds, id_concept, id_theso)) {
            try ( Connection conn = ds.getConnection()) {
                try ( Statement stmt = conn.createStatement()) {
                    try ( ResultSet resultSet = stmt.executeQuery("select latitude, longitude from gps where id_concept ='"
                            + id_concept + "' and id_theso = '" + id_theso + "'")) {
                        if (resultSet.next()) {
                            coordonnees = new NodeGps();
                            coordonnees.setLatitude(resultSet.getDouble("latitude"));
                            coordonnees.setLongitude(resultSet.getDouble("longitude"));
                        }
                    }
                }
            } catch (SQLException sqle) {
                log.error("Error while Add coordonnes : " + id_concept, sqle);
            }
        }

        return coordonnees;
    }

    /**
     * fonction qui permet de supprimer les coordonnées GPS d'un Concept
     */
    public boolean deleteGpsCoordinate(HikariDataSource ds, String id_concept, String id_theso) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE from gps where id_concept ='" + id_concept
                        + "' and id_theso = '" + id_theso + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting coordinate GPS of Concept : " + id_concept, sqle);
        }
        return status;
    }

/////////////////////////////////////////////////////////////////////    
/////////////////////////////////////////////////////////////////////   
// fonctions obsolètes
/////////////////////////////////////////////////////////////////////   
/////////////////////////////////////////////////////////////////////       
    public boolean garderPreferences(HikariDataSource ds, String id_Theso,
            boolean integrerTraduction, boolean reemplacerTraduction, boolean alignementAutomatique, Integer id_gps_source, int id_user) {
        if (!existsPreferences(ds, id_Theso, id_gps_source, id_user)) {
            if (!insertPreferences(ds, id_Theso, integrerTraduction, reemplacerTraduction, alignementAutomatique, id_gps_source, id_user)) {
                return false;
            }
            return true;
        } else if (!updateTablePreferences(ds, id_Theso, integrerTraduction, reemplacerTraduction, alignementAutomatique, id_gps_source, id_user)) {
            return false;
        }
        return true;
    }

    public boolean existsPreferences(HikariDataSource ds, String id_Theso, Integer id_gps_source, int id_user) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                try ( ResultSet resultSet = stmt.executeQuery("SELECT id from gps_preferences where id_thesaurus ='" + id_Theso
                        + "' and id_user = " + id_user + " and id_alignement_source ='" + id_gps_source + "'")) {
                    if (resultSet.next()) {
                        status = true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while search preferences Gps : ", sqle);
        }
        return status;
    }

    public boolean insertPreferences(HikariDataSource ds, String id_Theso,
            boolean integrerTraduction, boolean reemplacerTraduction, boolean alignementAutomatique,
            int id_gps_source, int id_user) {
        
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO gps_preferences (id_thesaurus, id_user, id_alignement_source, "
                        + "gps_integrertraduction, gps_reemplacertraduction, gps_alignementautomatique) values('"
                        + id_Theso + "'," + id_user + "," + id_gps_source + ",'" + integrerTraduction
                        + "','" + reemplacerTraduction + "','" + alignementAutomatique + "')");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while add gps preferences : ", sqle);
        }

        return status;
    }

    public boolean updateTablePreferences(HikariDataSource ds, String id_Theso,
            boolean integrerTraduction, boolean reemplacerTraduction, boolean alignementAutomatique,
            Integer id_gps_source, int id_user) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Update gps_preferences set gps_integrertraduction ='" + integrerTraduction
                        + "', gps_reemplacertraduction = '" + reemplacerTraduction
                        + "', gps_alignementautomatique ='" + alignementAutomatique
                        + " ' where id_thesaurus ='" + id_Theso + "'"
                        + "  and id_user =" + id_user
                        + " and id_alignement_source =" + id_gps_source);
                status = true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while update gps preferences : ", sqle);
        }

        return status;
    }

    public AlignementSource find_alignement_gps(HikariDataSource ds, Integer idsource) {
        AlignementSource alig = new AlignementSource();;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                try ( ResultSet resultSet = stmt.executeQuery("select * from alignement_source where id = " + idsource)) {
                    resultSet.next();
                    alig.setSource(resultSet.getString("source"));
                    alig.setRequete(resultSet.getString("requete"));
                    alig.setTypeRequete(resultSet.getString("type_rqt"));
                    alig.setAlignement_format(resultSet.getString("alignement_format"));
                    alig.setId(resultSet.getInt("id"));
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Add coordonnes : ", sqle);
        }
        return alig;
    }

    public GpsPreferences getGpsPreferences(HikariDataSource ds, String id_theso, int iduser, int id_source) {
        GpsPreferences gpsPreferences = new GpsPreferences();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                try ( ResultSet resultSet = stmt.executeQuery("select * from gps_preferences where id_thesaurus = '" + id_theso
                        + "' and id_user =" + iduser + " and id_alignement_source =" + id_source)) {
                    resultSet.next();
                    gpsPreferences.setGps_alignementautomatique(resultSet.getBoolean("gps_alignementautomatique"));
                    gpsPreferences.setGps_integrertraduction(resultSet.getBoolean("gps_integrertraduction"));
                    gpsPreferences.setGps_reemplacertraduction(resultSet.getBoolean("gps_reemplacertraduction"));
                    gpsPreferences.setId_user(resultSet.getInt("id_user"));
                    gpsPreferences.setId_alignement_source(resultSet.getInt("id_alignement_source"));
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Add coordonnes : ", sqle);
        }
        return gpsPreferences;
    }

    /**
     * Change l'id d'un concept dans la table
     */
    public void setIdConceptGPS(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        try ( Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE gps SET id_concept = '" + newIdConcept + "' WHERE id_concept = '"
                    + idConcept + "' AND id_theso = '" + idTheso + "'");
        }
    }
}
