package fr.cnrs.opentheso.repositories;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.models.releases.ReleaseDto;
import fr.cnrs.opentheso.entites.Release;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ReleaseRepository {


    public void saveRelease(HikariDataSource ds, Release release) {
        try ( Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into releases (version, url, date, description) values ('"
                        + release.getVersion() + "', '" + release.getUrl() + "', " + release.getDate() + ", '" + release.getDescription() + "')");
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveAllReleases(HikariDataSource ds, List<Release> releases) {
        for (Release release : releases) {
            if (ObjectUtils.isEmpty(getReleaseByVersion(ds, release.getVersion()))) {
                saveRelease(ds, release);
            }
        }
    }

    public List<Release> getAllReleases(HikariDataSource ds) {
        List<Release> allReleases = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * FROM releases");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        Release release = new Release();
                        release.setId(resultSet.getInt("id"));
                        release.setVersion(resultSet.getString("version"));
                        release.setUrl(resultSet.getString("url"));
                        var date = resultSet.getDate("date");
                        if (date != null) {
                            release.setDate(LocalDate.of(date.getYear(), date.getMonth()+1, date.getDay()));
                        }
                        release.setDescription(resultSet.getString("description"));
                        allReleases.add(release);
                    }
                }
            }
        } catch (SQLException sqle) {

        }
        return allReleases;
    }

    public Release getReleaseByVersion(HikariDataSource ds, String version) {

        Release release = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * FROM releases WHERE version = '" + version + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        release = new Release();
                        release.setId(resultSet.getInt("id"));
                        release.setVersion(resultSet.getString("version"));
                        release.setUrl(resultSet.getString("url"));

                        var date = resultSet.getDate("date");
                        if (date != null) {
                            release.setDate(LocalDate.of(date.getYear(), date.getMonth()+1, date.getDay()));
                        }
                        release.setDescription(resultSet.getString("description"));
                    }
                }
            }
        } catch (SQLException sqle) {

        }
        return release;
    }


    public Release toRelease(ReleaseDto releaseDto) {
        Release release = new Release();
        release.setVersion(releaseDto.getTag_name());
        release.setUrl(releaseDto.getHtml_url());
        release.setDate(toLocalDate(releaseDto.getPublished_at()));
        release.setDescription(releaseDto.getBody());
        return release;
    }

    private LocalDate toLocalDate(String date) {
        if (StringUtils.isNotEmpty(date)) {
            Instant instant = Instant.parse(date);
            return instant.atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

}
