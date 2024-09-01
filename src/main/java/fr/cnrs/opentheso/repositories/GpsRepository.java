package fr.cnrs.opentheso.repositories;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.entites.Gps;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class GpsRepository {

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
