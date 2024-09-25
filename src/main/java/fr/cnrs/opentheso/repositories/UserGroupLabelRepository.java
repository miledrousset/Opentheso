package fr.cnrs.opentheso.repositories;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


@Service
public class UserGroupLabelRepository {

    public List<UserGroupLabel> getAllProjects(HikariDataSource ds) {
        List<UserGroupLabel> projects = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT DISTINCT label.*, lower(label.label_group) AS sorted_label_group FROM user_group_label label ORDER BY lower(label.label_group) ASC");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        UserGroupLabel userGroupLabel = new UserGroupLabel();
                        userGroupLabel.setId(resultSet.getInt("id_group"));
                        userGroupLabel.setLabel(resultSet.getString("sorted_label_group"));
                        projects.add(userGroupLabel);
                    }
                }
            }
        } catch (SQLException sqle) {

        }
        return projects;
    }

    public List<UserGroupLabel> getProjectsByThesoStatus(HikariDataSource ds, boolean status) {
        List<UserGroupLabel> projects = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT DISTINCT grp.id_group, lower(label.label_group) AS sorted_label_group "
                        + "FROM thesaurus the, user_group_thesaurus grp, user_group_label label "
                        + "WHERE the.private = " + status + " "
                        + "AND grp.id_thesaurus = the.id_thesaurus "
                        + "AND label.id_group = grp.id_group "
                        + "ORDER BY lower(label.label_group) ASC");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        UserGroupLabel userGroupLabel = new UserGroupLabel();
                        userGroupLabel.setId(resultSet.getInt("id_group"));
                        userGroupLabel.setLabel(resultSet.getString("sorted_label_group"));
                        projects.add(userGroupLabel);
                    }
                }
            }
        } catch (SQLException sqle) {}
        return projects;
    }

    public List<UserGroupLabel> getProjectsByUserId(HikariDataSource ds, int userId) {
        List<UserGroupLabel> projects = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT lab "
                        + "FROM user_role_group grp, UserGroupLabel lab "
                        + "WHERE grp.id_group = lab.id_group "
                        + "AND grp.id_user = " + userId + " "
                        + "ORDER BY lower(lab.label_group) ASC");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        UserGroupLabel userGroupLabel = new UserGroupLabel();
                        userGroupLabel.setId(resultSet.getInt("id_group"));
                        userGroupLabel.setLabel(resultSet.getString("label_group"));
                        projects.add(userGroupLabel);
                    }
                }
            }
        } catch (SQLException sqle) {}
        return projects;
    }

}
