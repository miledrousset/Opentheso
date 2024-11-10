package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ProjectDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class ProjectDescriptionRepository {

    @Autowired
    private DataSource dataSource;

    public void saveProjectDescription(ProjectDescription projectDescription) {
        try ( Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into project_description (id_group, lang, description) values ('"
                        + projectDescription.getIdGroup() + "', '" + projectDescription.getLang() + "', '" + projectDescription.getDescription() + "')");
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ProjectDescription getProjectDescription(String idGroup, String lang) {
        ProjectDescription projectDescription = null;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * FROM project_description projectDesc "
                        + "WHERE projectDesc.id_group = '" + idGroup + "' "
                        + "AND projectDesc.lang = '" + lang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        projectDescription = new ProjectDescription();
                        projectDescription.setId(resultSet.getInt("id"));
                        projectDescription.setIdGroup(resultSet.getString("id_group"));
                        projectDescription.setLang(resultSet.getString("lang"));
                        projectDescription.setDescription(resultSet.getString("description"));
                    }
                }
            }
        } catch (SQLException sqle) {

        }
        return projectDescription;
    }

    public void removeProjectDescription(ProjectDescription projectDescription) {
        try ( Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from project_description where id = " + projectDescription.getId());
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateProjectDescription(ProjectDescription projectDescription) {

        try ( Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE project_description set lang = '" + projectDescription.getLang() + "', description = '"
                        + projectDescription.getDescription() + "' WHERE id = " + projectDescription.getId());
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
