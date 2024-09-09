package fr.cnrs.opentheso.repositories;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.models.nodes.NodeGps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class GpsHelper {

    
    /**
     * permet d'ajouter les coordonnées GPS
    * 
    * @param ds
    * @param idC
    * @param idTheso
     * @param nodeGpses
    * @return 
    */
    public boolean addGpsCoordinates(HikariDataSource ds, String idC, String idTheso, List <NodeGps> nodeGpses) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                for (NodeGps nodeGpse : nodeGpses) {
                    stmt.executeUpdate("Insert into gps (id_concept, id_theso, latitude, longitude, position) values('" + idC + "','" + idTheso + "'," 
                            + nodeGpse.getLatitude() + ","
                            + nodeGpse.getLongitude() + "," 
                            + nodeGpse.getPosition() + ")");
                }
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while Add coordonnes : " + idC, sqle);
        }
        return status;
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

    public List<NodeGps> getCoordinate(HikariDataSource ds, String id_concept, String id_theso) {
        List<NodeGps> coordonnees = null;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select latitude, longitude from gps where id_concept ='"
                        + id_concept + "' and id_theso = '" + id_theso + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    coordonnees = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeGps nodeGps = new NodeGps();
                        nodeGps.setLatitude(resultSet.getDouble("latitude"));
                        nodeGps.setLongitude(resultSet.getDouble("longitude"));
                        coordonnees.add(nodeGps);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Add coordonnes : " + id_concept, sqle);
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

    public void removeGps(HikariDataSource ds, Gps gps) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM gps WHERE id = " + gps.getId());
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeGpsByConcept(HikariDataSource ds, String idConcept, String idTheso) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM gps WHERE id_concept = '" + idConcept + "' AND id_theso = '" + idTheso + "'");
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveNewGps(HikariDataSource ds, Gps gps) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("INSERT INTO gps (id_concept, id_theso, latitude, longitude, position) values ('"
                        + gps.getIdConcept() + "', '" + gps.getIdTheso() + "', " + gps.getLatitude() + ", " + gps.getLongitude() +", " + gps.getPosition() + ")");
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Gps> getGpsByConceptAndThesorus(HikariDataSource ds, String idConcept, String idTheso) {
        List<Gps> allGps = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * FROM gps WHERE gps.id_concept = '" +idConcept + "' AND gps.id_theso = '" + idTheso + "' ORDER BY position");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        Gps gps = new Gps();
                        gps.setId(resultSet.getInt("id"));
                        gps.setIdConcept(resultSet.getString("id_concept"));
                        gps.setIdTheso(resultSet.getString("id_theso"));
                        gps.setLatitude(resultSet.getDouble("latitude"));
                        gps.setLongitude(resultSet.getDouble("longitude"));
                        gps.setPosition(resultSet.getInt("position"));
                        allGps.add(gps);
                    }
                }
            }
        } catch (SQLException sqle) {

        }
        return allGps;
    }

}
