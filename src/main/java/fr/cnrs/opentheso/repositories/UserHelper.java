package fr.cnrs.opentheso.repositories;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.users.NodeUserComplet;
import fr.cnrs.opentheso.models.users.NodeUserGroup;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import fr.cnrs.opentheso.models.users.NodeUserGroupUser;
import fr.cnrs.opentheso.models.users.NodeUserRole;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.models.userpermissions.NodeThesoRole;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.entites.UserGroupLabel;

import fr.cnrs.opentheso.utils.MD5Password;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Data
@Slf4j
@Service
public class UserHelper {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    /**
     * cette fonction permet de retourner tous les roles
     */
    public ArrayList<NodeUserRoleGroup> getAllRole() {
        ArrayList<NodeUserRoleGroup> nodeUserRoleGroups = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT "
                        + "  roles.id, "
                        + "  roles.name "
                        + " FROM "
                        + "  public.roles ");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserRoleGroup nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id"));
                        nodeUserRoleGroup.setRoleName(resultSet.getString("name"));
                        nodeUserRoleGroups.add(nodeUserRoleGroup);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoleGroups;
    }

    /**
     * permet de rechercher un utilisateur avec son nom
     *
     * @param userName
     * @return
     */
    public ArrayList<NodeUser> searchUser(String userName) {

        ArrayList<NodeUser> nodeUsers = new ArrayList<>();
        userName = fr.cnrs.opentheso.utils.StringUtils.convertString(userName);

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_user, username from users where username ilike '%"
                        + userName + "%' order by username");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUser nodeUser = new NodeUser();
                        nodeUser.setIdUser(resultSet.getInt("id_user"));
                        nodeUser.setName(resultSet.getString("username"));
                        if (userName.trim().equalsIgnoreCase(resultSet.getString("username"))) {
                            nodeUsers.add(0, nodeUser);
                        } else {
                            nodeUsers.add(nodeUser);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUsers;
    }

    /**
     * permet de rechercher les projets dont je suis Admin pour depalcer les
     * thésaurus
     *
     * @param idUser
     * @param projectName
     * @return
     */
    public ArrayList<NodeUserGroup> searchMyProject(int idUser, String projectName) {

        ArrayList<NodeUserGroup> nodeUserGroups = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery("SELECT user_group_label.id_group, user_group_label.label_group "
                        + "FROM user_role_group, user_group_label "
                        + "WHERE user_role_group.id_group = user_group_label.id_group "
                        + "AND user_role_group.id_user = " + idUser
                        + " AND user_role_group.id_role = 2"
                        + " AND user_group_label.label_group ilike '%" + projectName + "%' order by label_group")) {

                    while (resultSet.next()) {
                        NodeUserGroup nodeUserGroup = new NodeUserGroup();
                        nodeUserGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroups.add(nodeUserGroup);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroups;
    }

    /**
     * permet de rechercher les projets pour le SuperAdmin pour depalcer les
     * thésaurus
     *
     * @param projectName
     * @return
     */
    public ArrayList<NodeUserGroup> searchAllProject(String projectName) {
        ArrayList<NodeUserGroup> nodeUserGroups = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT "
                        + " user_group_label.id_group,"
                        + " user_group_label.label_group"
                        + " FROM"
                        + " user_group_label"
                        + " WHERE"
                        + " user_group_label.label_group ilike '%" + projectName + "%'"
                        + " order by label_group");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserGroup nodeUserGroup = new NodeUserGroup();
                        nodeUserGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroups.add(nodeUserGroup);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroups;
    }

    /**
     * Cette fonction permet d'ajouter un nouvel utilisateur sans le role
     *
     * @param userName
     * @param mail
     * @param password
     * @param isSuperAdmin
     * @param alertMail
     * @return #MR
     */
    public boolean addUser(String userName, String mail, String password, boolean isSuperAdmin, boolean alertMail) {

        boolean active = true;
        boolean passtomodify = false;

        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into users"
                        + "(username,password,active,mail,"
                        + "passtomodify,alertmail,issuperadmin)"
                        + " values ("
                        + "'" + userName + "'"
                        + ", '" + password + "'"
                        + "," + active
                        + ", '" + mail + "'"
                        + ", " + passtomodify
                        + ", " + alertMail
                        + ", " + isSuperAdmin
                        + ")");
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * Permet de récupérer l'Id de l'utilisateur si l'ulisateur existe ou si le
     * mot de passe est faux, on retourne (-1) sinon, on retourne l'ID de
     * l'utilisateur de retourner l'identifiant
     *
     * @param login
     * @param pwd
     * @return
     */
    public int getIdUser(String login, String pwd) {
        int idUser = -1;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_user FROM users WHERE username ilike '" + login + "' AND password='" + pwd + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idUser = resultSet.getInt("id_user");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idUser;
    }

    /**
     * Permet de récupérer l'Id de l'utilisateur à partir de son mail si
     * l'ulisateur existe ou si le mot de passe est faux, on retourne (-1)
     * sinon, on retourne l'ID de l'utilisateur de retourner l'identifiant
     *
     * @param mail
     * @return
     */
    public int getIdUserFromMail(String mail) {
        int idUser = -1;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_user FROM users WHERE mail ilike '"
                        + mail + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idUser = resultSet.getInt("id_user");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idUser;
    }

    /**
     * Permet de récupérer l'Id de l'utilisateur à partir de son mail si
     * l'ulisateur existe ou si le mot de passe est faux, on retourne (-1)
     * sinon, on retourne l'ID de l'utilisateur de retourner l'identifiant
     *
     * @param pseudo
     * @return
     */
    public int getIdUserFromPseudo(String pseudo) {
        int idUser = -1;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_user FROM users WHERE username ilike '"
                        + pseudo + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idUser = resultSet.getInt("id_user");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idUser;
    }

    public List<NodeUser> searchUserByCriteria(String mail, String username) {
        mail = StringUtils.isEmpty(mail) ? "" : mail;
        username = StringUtils.isEmpty(username) ? "" : username;
        List<NodeUser> userList = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + "  users.id_user,"
                        + "  users.username,"
                        + "  users.active,"
                        + "  users.mail,"
                        + "  users.passtomodify,"
                        + "  users.alertmail,"
                        + "  users.issuperadmin,"
                        + "  users.apiKey,"
                        + "users.key_never_expire,"
                        + "users.key_expires_at,"
                        + "users.isservice_account,"
                        + "users.key_description"
                        + " FROM users"
                        + " WHERE mail like '%" + mail + "%'"
                        + " AND username like '%" + username + "%'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        var nodeUser = new NodeUser();
                        nodeUser.setIdUser(resultSet.getInt("id_user"));
                        nodeUser.setName(resultSet.getString("username"));
                        nodeUser.setActive(resultSet.getBoolean("active"));
                        nodeUser.setMail(resultSet.getString("mail"));
                        nodeUser.setAlertMail(resultSet.getBoolean("alertmail"));
                        nodeUser.setPassToModify(resultSet.getBoolean("passtomodify"));
                        nodeUser.setSuperAdmin(resultSet.getBoolean("issuperadmin"));
                        nodeUser.setApiKey(resultSet.getString("apiKey"));
                        nodeUser.setKeyNeverExpire(resultSet.getBoolean("key_never_expire"));
                        nodeUser.setApiKeyExpireDate(resultSet.getDate("key_expires_at") == null ? null : resultSet.getDate("key_expires_at").toLocalDate());
                        nodeUser.setServiceAccount(resultSet.getBoolean("isservice_account"));
                        nodeUser.setKeyDescription(resultSet.getString("key_description"));
                        userList.add(nodeUser);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return userList;
    }

    public NodeUser getUserByApiKey(String apiKey) {
        NodeUser nodeUser = null;
        apiKey = MD5Password.getEncodedPassword(apiKey);
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + "  users.id_user,"
                        + "  users.username,"
                        + "  users.active,"
                        + "  users.mail,"
                        + "  users.passtomodify,"
                        + "  users.alertmail,"
                        + "  users.issuperadmin,"
                        + "  users.apiKey,"
                        + "users.key_never_expire,"
                        + "users.key_expires_at,"
                        + "users.isservice_account,"
                        + "users.key_description"
                        + " FROM users"
                        + " WHERE apikey ilike '" + apiKey + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeUser = new NodeUser();
                        nodeUser.setIdUser(resultSet.getInt("id_user"));
                        nodeUser.setName(resultSet.getString("username"));
                        nodeUser.setActive(resultSet.getBoolean("active"));
                        nodeUser.setMail(resultSet.getString("mail"));
                        nodeUser.setAlertMail(resultSet.getBoolean("alertmail"));
                        nodeUser.setPassToModify(resultSet.getBoolean("passtomodify"));
                        nodeUser.setSuperAdmin(resultSet.getBoolean("issuperadmin"));
                        nodeUser.setApiKey(resultSet.getString("apiKey"));
                        nodeUser.setKeyNeverExpire(resultSet.getBoolean("key_never_expire"));
                        nodeUser.setApiKeyExpireDate(resultSet.getDate("key_expires_at") == null ? null : resultSet.getDate("key_expires_at").toLocalDate());
                        nodeUser.setServiceAccount(resultSet.getBoolean("isservice_account"));
                        nodeUser.setKeyDescription(resultSet.getString("key_description"));

                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUser;
    }

    /**
     * cette fonction permet de retourner les informations de l'utilisateur
     *
     * @param idUser
     * @return #MR
     */
    public NodeUser getUser(int idUser) {
        NodeUser nodeUser = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + "  users.id_user,"
                        + "  users.username,"
                        + "  users.active,"
                        + "  users.mail,"
                        + "  users.passtomodify,"
                        + "  users.alertmail,"
                        + "  users.issuperadmin,"
                        + "  users.apiKey,"
                        + "users.key_never_expire,"
                        + "users.key_expires_at,"
                        + "users.isservice_account,"
                        + "users.key_description"
                        + " FROM users"
                        + " WHERE "
                        + " users.id_user = " + idUser);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeUser = new NodeUser();
                        nodeUser.setIdUser(idUser);
                        nodeUser.setName(resultSet.getString("username"));
                        nodeUser.setActive(resultSet.getBoolean("active"));
                        nodeUser.setMail(resultSet.getString("mail"));
                        nodeUser.setAlertMail(resultSet.getBoolean("alertmail"));
                        nodeUser.setPassToModify(resultSet.getBoolean("passtomodify"));
                        nodeUser.setSuperAdmin(resultSet.getBoolean("issuperadmin"));
                        nodeUser.setApiKey(resultSet.getString("apiKey"));
                        nodeUser.setKeyNeverExpire(resultSet.getBoolean("key_never_expire"));
                        nodeUser.setApiKeyExpireDate(resultSet.getDate("key_expires_at") == null ? null : resultSet.getDate("key_expires_at").toLocalDate());
                        nodeUser.setServiceAccount(resultSet.getBoolean("isservice_account"));
                        nodeUser.setKeyDescription(resultSet.getString("key_description"));

                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUser;
    }

    /**
     * cette fonction permet de retourner les informations de l'utilisateur à partir de son login et password
     */
    public NodeUserComplet getUserByLoginAndPassword(String login, String password) {
        NodeUserComplet nodeUser = null;
        try (var conn = dataSource.getConnection()) {
            try (var stmt = conn.createStatement()) {
                stmt.executeQuery(String.format("SELECT * FROM users WHERE users.username = '%s' AND users.password = '%s'", login, password));
                try (var resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeUser = new NodeUserComplet();
                        nodeUser.setIdUser(resultSet.getInt("id_user"));
                        nodeUser.setName(resultSet.getString("username"));
                        nodeUser.setActive(resultSet.getBoolean("active"));
                        nodeUser.setMail(resultSet.getString("mail"));
                        nodeUser.setAlertMail(resultSet.getBoolean("alertmail"));
                        nodeUser.setPassToModify(resultSet.getBoolean("passtomodify"));
                        nodeUser.setSuperAdmin(resultSet.getBoolean("issuperadmin"));
                        nodeUser.setApiKey(resultSet.getString("apiKey"));
                        nodeUser.setKeyNeverExpire(resultSet.getBoolean("key_never_expire"));
                        nodeUser.setApiKeyExpireDate(resultSet.getDate("key_expires_at") == null ? null : resultSet.getDate("key_expires_at").toLocalDate());
                        nodeUser.setServiceAccount(resultSet.getBoolean("isservice_account"));
                        nodeUser.setKeyDescription(resultSet.getString("key_description"));
                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUser;
    }

    /**
     * cette fonction permet de retourner le nom d'un groupe
     *
     *
     * @param projectName
     * @return
     */
    public int getThisProjectId(String projectName) {
        int projectId = -1;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + "  user_group_label.id_group"
                        + " FROM"
                        + "  user_group_label"
                        + " WHERE"
                        + "  user_group_label.label_group ilike '" + projectName + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        projectId = resultSet.getInt("id_group");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return projectId;
    }

    public boolean isApiKeyExpired(NodeUser nodeUser){
        if (nodeUser.getApiKeyExpireDate()==null){
            return false;
        }
        if(LocalDate.now().isAfter(nodeUser.getApiKeyExpireDate())){
            return true;
        }else {
            return false;
        }
    }

    /**
     * cette fonction permet de retourner le nom d'un groupe
     *
     * @param idGroup
     * @return
     */
    public String getGroupName(int idGroup) {
        String groupLabel = null;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + "  user_group_label.label_group"
                        + " FROM"
                        + "  user_group_label"
                        + " WHERE"
                        + "  user_group_label.id_group = " + idGroup);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        groupLabel = resultSet.getString("label_group");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return groupLabel;
    }

    /**
     * cette fonction permet de retourner la liste des groupes d'un utilisateur
     *
     * @param idUser
     * @return
     */
    public Map<String, String> getGroupsOfUser(int idUser) {

        HashMap<String, String> listGroup = new LinkedHashMap();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + "  user_group_label.id_group,"
                        + "  user_group_label.label_group"
                        + " FROM"
                        + "  user_role_group,"
                        + "  user_group_label"
                        + " WHERE"
                        + "  user_role_group.id_group = user_group_label.id_group AND"
                        + "  user_role_group.id_user = " + idUser + " order by label_group");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listGroup.put("" + resultSet.getInt("id_group"), resultSet.getString("label_group"));
                    }
                }
                stmt.executeQuery("SELECT user_group_label.id_group,  user_group_label.label_group "
                        + " FROM "
                        + " user_role_only_on,  user_group_label "
                        + " WHERE "
                        + " user_role_only_on.id_group = user_group_label.id_group "
                        + " AND  user_role_only_on.id_user = " + idUser
                        + " order by label_group");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listGroup.put("" + resultSet.getInt("id_group"), resultSet.getString("label_group"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listGroup;
    }
    
    /**
     * cette fonction permet de retourner la liste des projets d'un utilisateur
     *
     * @param idUser
     * @return
     * #MR
     */
    public List<UserGroupLabel> getProjectOfUser(int idUser) {

        List<UserGroupLabel> userGroupLabels = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT DISTINCT user_group_label.id_group, user_group_label.label_group"
                        + " FROM user_role_group, user_group_label"
                        + " WHERE user_role_group.id_group = user_group_label.id_group"
                        + " AND user_role_group.id_user = " + idUser
                        + " ORDER BY label_group");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        UserGroupLabel userGroupLabel = new UserGroupLabel();
                        userGroupLabel.setId(resultSet.getInt("id_group"));
                        userGroupLabel.setLabel(resultSet.getString("label_group"));
                        userGroupLabels.add(userGroupLabel);
                        //listGroup.put("" + resultSet.getInt("id_group"), resultSet.getString("label_group"));
                    }
                }
                stmt.executeQuery("SELECT distinct (user_group_label.id_group),  user_group_label.label_group "
                        + " FROM "
                        + " user_role_only_on,  user_group_label "
                        + " WHERE "
                        + " user_role_only_on.id_group = user_group_label.id_group "
                        + " AND  user_role_only_on.id_user = " + idUser
                        + " order by label_group");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        UserGroupLabel userGroupLabel = new UserGroupLabel();
                        userGroupLabel.setId(resultSet.getInt("id_group"));
                        userGroupLabel.setLabel(resultSet.getString("label_group"));
                        userGroupLabels.add(userGroupLabel);                        
                   //     listGroup.put("" + resultSet.getInt("id_group"), resultSet.getString("label_group"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return userGroupLabels;
    }    

    /**
     * cette fonction permet de retourner tous les groupes existant au format
     * MAP c'est pour le SuperAdmin
     *
     * @return
     */
    public Map<String, String> getAllGroups() {
        HashMap<String, String> sortedHashMap = new LinkedHashMap();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT  user_group_label.id_group,  user_group_label.label_group FROM user_group_label order by label_group");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        sortedHashMap.put("" + resultSet.getInt("id_group"), resultSet.getString("label_group"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sortedHashMap;
    }

    /**
     * cette fonction permet de retourner tous les projets/groupes existant au
     * format Objet pour la gestion
     *
     * @return
     */
    public ArrayList<NodeUserGroup> getAllProject() {
        ArrayList<NodeUserGroup> nodeUserGroups = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT user_group_label.id_group, user_group_label.label_group FROM user_group_label order by label_group");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserGroup nodeUserGroup = new NodeUserGroup();
                        nodeUserGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroups.add(nodeUserGroup);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroups;
    }

    /**
     * retourne la liste de tous les projets où l'utilisateur a des droits Admin
     * dessus
     *
     * @param idUser
     * @return
     */
    public ArrayList<NodeUserGroup> getProjectsOfUserAsAdmin(
             int idUser) {
        ArrayList<NodeUserGroup> nodeUserGroups = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + " user_group_label.id_group, user_group_label.label_group"
                        + " FROM"
                        + " user_role_group, user_group_label"
                        + " WHERE"
                        + " user_role_group.id_group = user_group_label.id_group"
                        + " AND"
                        + " user_role_group.id_user = " + idUser
                        + " AND"
                        + " user_role_group.id_role = 2"
                        + " order by label_group");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserGroup nodeUserGroup = new NodeUserGroup();
                        nodeUserGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroups.add(nodeUserGroup);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroups;
    }

    /**
     * permet de retourner la liste des thésaurus pour un projet
     *
     * @param idProject
     * @param idLang
     * @return
     */
    public ArrayList<NodeIdValue> getThesaurusOfProject(int idProject, String idLang) {

        ArrayList<String> listIdTheso = getIdThesaurusOfProject(idProject);
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        String idLangTemp;
        String title;
        for (String idTheso : listIdTheso) {
            idLangTemp = preferencesHelper.getWorkLanguageOfTheso(idTheso);
            if (StringUtils.isEmpty(idLangTemp)) {
                idLangTemp = idLang;
            }
            title = thesaurusHelper.getTitleOfThesaurus(idTheso, idLangTemp);

            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idTheso);
            nodeIdValue.setValue(title);
            nodeIdValues.add(nodeIdValue);
            nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(idTheso));
        }
        return nodeIdValues;
    }

    private ArrayList<String> getIdThesaurusOfProject(int idProject) {
        ArrayList<String> nodeIdTheso = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT user_group_thesaurus.id_thesaurus"
                        + " FROM user_group_thesaurus"
                        + " WHERE "
                        + " user_group_thesaurus.id_group = '" + idProject + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        nodeIdTheso.add(resultSet.getString("id_thesaurus"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIdTheso;
    }

    public List<NodeIdValue> getThesaurusOfProject(int idProject, String idLang, boolean isPrivate) {

        ArrayList<String> listIdTheso = getIdThesaurusOfProjectWithVisibility(idProject, isPrivate);

        if (CollectionUtils.isNotEmpty(listIdTheso)) {
            return listIdTheso.stream().map(idTheso -> {
                String idLangTemp = preferencesHelper.getWorkLanguageOfTheso(idTheso);
                if (StringUtils.isEmpty(idLangTemp)) {
                    idLangTemp = idLang;
                }

                NodeIdValue nodeIdValue = new NodeIdValue();
                nodeIdValue.setId(idTheso);
                nodeIdValue.setValue(thesaurusHelper.getTitleOfThesaurus(idTheso, idLangTemp));
                return nodeIdValue;
            }).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<String> getIdThesaurusOfProjectWithVisibility(int idProject, boolean isPrivate) {
        ArrayList<String> nodeIdTheso = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT the.id_thesaurus "
                        + "FROM user_group_thesaurus, thesaurus the "
                        + "WHERE the.id_thesaurus = user_group_thesaurus.id_thesaurus "
                        + "AND user_group_thesaurus.id_group = '" + idProject + "'"
                        + (isPrivate ? "" : " AND the.private = false"));
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        nodeIdTheso.add(resultSet.getString("id_thesaurus"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIdTheso;
    }

    /**
     * permet de retourner la liste des thésaurus pour un groupe pour un
     * affichage IHM
     *
     * @param idGroup
     * @param idLang
     * @return
     */
    public Map<String, String> getThesaurusLabelsOfGroup(int idGroup, String idLang) {
        Map<String, String> listThesos = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT distinct"
                        + "  thesaurus_label.title,"
                        + "  thesaurus_label.id_thesaurus"
                        + " FROM "
                        + "  user_group_thesaurus,"
                        + "  thesaurus_label"
                        + " WHERE "
                        + "  user_group_thesaurus.id_thesaurus = thesaurus_label.id_thesaurus AND"
                        + "  user_group_thesaurus.id_group = " + idGroup
                        + " and thesaurus_label.lang = '" + idLang + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    listThesos = new HashMap<>();
                    while (resultSet.next()) {
                        listThesos.put(resultSet.getString("id_thesaurus"), resultSet.getString("title"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listThesos;
    }

    /**
     * permet de retourner la liste de tous les utilisateurs
     */
    public ArrayList<NodeUser> getAllUsers() {
        ArrayList<NodeUser> nodeUsers = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * FROM users order by f_unaccent(lower(username))");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUser nodeUser = new NodeUser();
                        nodeUser.setIdUser(resultSet.getInt("id_user"));
                        nodeUser.setName(resultSet.getString("username"));
                        nodeUser.setActive(resultSet.getBoolean("active"));
                        nodeUser.setSuperAdmin(resultSet.getBoolean("issuperadmin"));
                        nodeUsers.add(nodeUser);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUsers;
    }

    /**
     * cette fonction permet de retourner la liste des thésaurus pour un
     * utilisateur l'utilisateur peut faire partie de plusieurs groupes, donc on
     * retourne la liste de tous ces thésaurus dans différents groupes
     *
     * @param idUser
     * @return #MR
     */
    public List<String> getThesaurusOfUser(int idUser) {
        List<String> nodeUserGroupThesauruses = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT distinct"
                        + " user_group_thesaurus.id_thesaurus"
                        + " FROM "
                        + "  user_group_thesaurus,"
                        + "  user_role_group"
                        + " WHERE "
                        + "  user_role_group.id_group = user_group_thesaurus.id_group AND"
                        + "  user_role_group.id_user = " + idUser
                        + " order by id_thesaurus DESC");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        nodeUserGroupThesauruses.add(resultSet.getString("id_thesaurus"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroupThesauruses;
    }

    /**
     * cette fonction permet de retourner la liste des thésaurus où
     * l'utilisateur a des droits admin l l'utilisateur peut faire partie de
     * plusieurs groupes, donc on retourne la liste de tous ces thésaurus de
     * différents groupes
     *
     * @param idUser
     * @return #MR
     */
    public List<String> getThesaurusOfUserAsAdmin(int idUser) {
        List<String> nodeUserGroupThesauruses = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + " user_group_thesaurus.id_thesaurus"
                        + " FROM "
                        + " user_group_thesaurus, user_role_group"
                        + " WHERE "
                        + " user_role_group.id_group = user_group_thesaurus.id_group"
                        + " AND"
                        + " user_role_group.id_user =  " + idUser
                        + " AND"
                        + " user_role_group.id_role = 2"
                        + " order by id_thesaurus DESC");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        nodeUserGroupThesauruses.add(resultSet.getString("id_thesaurus"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroupThesauruses;
    }

    /**
     * cette fonction permet de retourner la liste des thésaurus et les groupes
     * correspondants (uniquement les thésaurus qui font partie d'un groupe
     *
     * @param idLangSource
     * @return #MR
     */
    public ArrayList<NodeUserGroupThesaurus> getAllGroupTheso(String idLangSource) {
        ArrayList<NodeUserGroupThesaurus> nodeUserGroupThesauruses = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT "
                        + "  user_group_label.label_group,"
                        + "  thesaurus_label.title,"
                        + "  user_group_thesaurus.id_thesaurus,"
                        + "  user_group_thesaurus.id_group,"
                        + "  thesaurus.private"
                        + " FROM "
                        + "  thesaurus_label, "
                        + "  user_group_thesaurus, "
                        + "  user_group_label,"
                        + "  thesaurus"
                        + " WHERE "
                        + "  thesaurus.id_thesaurus = thesaurus_label.id_thesaurus and"
                        + "  user_group_thesaurus.id_group = user_group_label.id_group AND"
                        + "  user_group_thesaurus.id_thesaurus = thesaurus_label.id_thesaurus AND"
                        + "  thesaurus_label.lang = '" + idLangSource + "'"
                        + " ORDER BY LOWER(thesaurus_label.title)");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserGroupThesaurus nodeUserGroupThesaurus = new NodeUserGroupThesaurus();
                        nodeUserGroupThesaurus.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeUserGroupThesaurus.setThesaurusName(resultSet.getString("title"));
                        nodeUserGroupThesaurus.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroupThesaurus.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroupThesaurus.setPrivateTheso(resultSet.getBoolean("private"));

                        nodeUserGroupThesauruses.add(nodeUserGroupThesaurus);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroupThesauruses;
    }

    /**
     * cette fonction permet de retourner la liste des thésaurus qui
     * n'apprtiennent à aucun groupe
     *
     * @param idLangSource
     * @return #MR
     */
    public ArrayList<NodeUserGroupThesaurus> getAllThesoWithoutGroup(String idLangSource) {
        /// fonction prête à intégrer        
        ArrayList<NodeUserGroupThesaurus> nodeUserGroupThesauruses = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT thesaurus_label.id_thesaurus, thesaurus_label.title, thesaurus.private "
                        + " FROM  thesaurus_label, thesaurus"
                        + " WHERE thesaurus_label.lang = '" + idLangSource + "'"
                        + " and thesaurus.id_thesaurus = thesaurus_label.id_thesaurus"
                        + " and thesaurus_label.id_thesaurus not in "
                        + "(select id_thesaurus from  user_group_thesaurus)"
                        + " ORDER BY LOWER(thesaurus_label.title)");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserGroupThesaurus nodeUserGroupThesaurus = new NodeUserGroupThesaurus();
                        nodeUserGroupThesaurus.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeUserGroupThesaurus.setThesaurusName(resultSet.getString("title"));
                        nodeUserGroupThesaurus.setIdGroup(-1);
                        nodeUserGroupThesaurus.setGroupName("");
                        nodeUserGroupThesaurus.setPrivateTheso(resultSet.getBoolean("private"));

                        nodeUserGroupThesauruses.add(nodeUserGroupThesaurus);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroupThesauruses;
    }

    /**
     * cette fonction permet de retourner la liste des utilisateurs avec les
     * groupes correspondants on ignore les superAdmin et les utilisateurs sans
     * groupes.
     *
     * @return #MR
     */
    public ArrayList<NodeUserGroupUser> getAllGroupUser() {
        ArrayList<NodeUserGroupUser> nodeUserGroupUsers = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + " users.username,"
                        + " users.id_user,"
                        + " roles.name,"
                        + " user_group_label.label_group,"
                        + " user_group_label.id_group,"
                        + " roles.id"
                        + " FROM"
                        + " user_role_group,"
                        + " users,"
                        + " user_group_label,"
                        + " roles"
                        + " WHERE"
                        + " user_role_group.id_user = users.id_user AND"
                        + " user_group_label.id_group = user_role_group.id_group AND"
                        + " roles.id = user_role_group.id_role order by f_unaccent(lower(username)) ASC");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserGroupUser nodeUserGroupUser = new NodeUserGroupUser();
                        nodeUserGroupUser.setIdUser(resultSet.getString("id_user"));
                        nodeUserGroupUser.setUserName(resultSet.getString("username"));
                        nodeUserGroupUser.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroupUser.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroupUser.setIdRole(resultSet.getInt("id"));
                        nodeUserGroupUser.setRoleName(resultSet.getString("name"));
                        nodeUserGroupUsers.add(nodeUserGroupUser);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroupUsers;
    }

    /**
     * cette fonction permet de retourner la liste des utilisateurs qui n'ont
     * aucun groupe et qui ne sont pas des superAdmin.
     *
     * @return #MR
     */
    public ArrayList<NodeUserGroupUser> getAllGroupUserWithoutGroup() {
        ArrayList<NodeUserGroupUser> nodeUserGroupUsers = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + " users.username,"
                        + " users.id_user"
                        + " FROM"
                        + " users"
                        + " WHERE "
                        + " users.issuperadmin != true"
                        + " and"
                        + " users.id_user not in"
                        + " (select user_role_group.id_user from user_role_group)"
                        + " order by f_unaccent(lower(username))");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserGroupUser nodeUserGroupUser = new NodeUserGroupUser();
                        nodeUserGroupUser.setIdUser(resultSet.getString("id_user"));
                        nodeUserGroupUser.setUserName(resultSet.getString("username"));
                        nodeUserGroupUser.setIdGroup(-1);
                        nodeUserGroupUser.setGroupName("");

                        nodeUserGroupUsers.add(nodeUserGroupUser);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroupUsers;
    }

    /**
     * permet de retourner la liste de tous les utilisateurs qui sont SuperAdmin
     *
     * @return
     */
    public ArrayList<NodeUserGroupUser> getAllUsersSuperadmin() {
        ArrayList<NodeUserGroupUser> nodeUserGroupUsers = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_user, username FROM users where issuperadmin = true order by f_unaccent(lower(username))");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserGroupUser nodeUserGroupUser = new NodeUserGroupUser();
                        nodeUserGroupUser.setIdUser(resultSet.getString("id_user"));
                        nodeUserGroupUser.setUserName(resultSet.getString("username"));
                        nodeUserGroupUser.setIdGroup(-1);
                        nodeUserGroupUser.setGroupName("");
                        nodeUserGroupUser.setIdRole(1);
                        nodeUserGroupUser.setRoleName("SuperAdmin");
                        nodeUserGroupUsers.add(nodeUserGroupUser);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroupUsers;
    }

    /**
     * permet de créer un groupe ou projet pour regrouper les utilisateurs et
     * les thésaurus
     *
     * @param userGroupName
     * @return
     */
    public boolean addNewProject(String userGroupName) {
        boolean status = false;
        userGroupName = fr.cnrs.opentheso.utils.StringUtils.convertString(userGroupName);

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into user_group_label"
                        + "(label_group)"
                        + " values ('"
                        + userGroupName + "')");
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de savoir si le groupe ou projet existe déja
     *
     * @param userGroupName
     * @return
     */
    public boolean isUserGroupExist(String userGroupName) {
        boolean existe = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_group FROM user_group_label WHERE label_group ilike '" + userGroupName + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = true;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }

    /**
     * permet de mettre à jour les alertes et activer/désactiver l'utilisateur e
     *
     * @param idUser
     * @param name
     * @param email
     * @param isIsActive
     * @param isIsAlertMail
     * @return
     */
    public boolean updateUser(int idUser, String name, String email, boolean isIsActive, boolean isIsAlertMail) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE users set alertmail = " + isIsAlertMail
                        + ", username = '" + name + "'"
                        + ", mail = '" + email + "'"
                        + ", active = " + isIsActive
                        + " WHERE id_user = " + idUser);
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    public boolean setIsSuperAdmin(Connection conn, int idUser, boolean isSuperAdmin) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE users set "
                    + " issuperadmin = " + isSuperAdmin
                    + " WHERE id_user = " + idUser);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * permet de supprimer les roles d'un utilisateur sur les groupes cas où
     * l'utilisateur passe en superAdmin, plus besoin de rôles
     *
     * @param conn
     * @param idUser
     * @return
     */
    public boolean deleteRolesOfUser(Connection conn, int idUser) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("delete from user_role_group where" + " id_user =" + idUser);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * cette fonction permet de retourner les utilisateurs avec role SuperAdmin
     *
     * @return
     */
    public ArrayList<NodeUserRole> getListOfSuperAdmin() {
        ArrayList<NodeUserRole> nodeUserRoles = null;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT "
                        + "  users.id_user,"
                        + "  users.username,"
                        + " users.active"
                        + " FROM "
                        + "  users"
                        + " WHERE"
                        + "  users.issuperadmin = true"
                        + " ORDER BY"
                        + "  LOWER(users.username)");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    nodeUserRoles = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeUserRole nodeUserRole = new NodeUserRole();
                        nodeUserRole.setIdUser(resultSet.getInt("id_user"));
                        nodeUserRole.setUserName(resultSet.getString("username"));
                        nodeUserRole.setIdRole(1);
                        nodeUserRole.setRoleName("SuperAdmin");
                        nodeUserRole.setActive(resultSet.getBoolean("active"));
                        nodeUserRoles.add(nodeUserRole);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoles;
    }

    /**
     * pemret de modifier le role de l'utilisateur sur un groupe
     *
     * @param idUser
     * @param idRole
     * @param idGroup
     * @return
     */
    public boolean updateUserRoleOnGroup(int idUser, int idRole, int idGroup) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            /// si le role devient SuperAdmin, alors on supprime les roles sur les groupes
            if (idRole == 1) {
                if (!deleteRoleOnGroup(idUser, idGroup)) {
                    conn.rollback();
                    return false;
                }
            } else {
                if (!updateUserRoleOnGroupRollBack(conn, idUser, idRole, idGroup)) {
                    conn.rollback();
                    return false;
                }
            }
            if (!setIsSuperAdmin(conn, idUser, (idRole == 1))) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private boolean updateUserRoleOnGroupRollBack(Connection conn,
            int idUser, int idRole, int idGroup) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE user_role_group set id_role = "
                    + idRole
                    + " WHERE id_user = " + idUser
                    + " and id_group = " + idGroup);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * permet de modifier le role de l'utilisateur sur un groupe si
     * l'utilisateur change de role (SuperAdmin -> en Admin, on met à jour son
     * status dans la table Users (isSuperadmin = false)
     *
     * @param idUser
     * @param idRole
     * @param idGroup
     * @return
     */
    public boolean addUserRoleOnGroup(int idUser, int idRole, int idGroup) {
        boolean status = false;
        boolean isSuperAdmin = false;

        if (idRole == 1) {
            isSuperAdmin = true;
        }
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            /// si le role devient SuperAdmin, alors on supprime les roles sur les groupes
            if (isSuperAdmin) {
                if (!deleteRoleOnGroup(idUser, idGroup)) {
                    conn.rollback();
                    return false;
                }
            } else {
                if (!addUserRoleOnGroupRollBack(conn,
                        idUser, idRole, idGroup)) {
                    conn.rollback();
                    return false;
                }
            }
            if (!setIsSuperAdmin(conn, idUser, isSuperAdmin)) {
                conn.rollback();
                return false;
            }
            conn.commit();
            status = true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de supprimer un role pour un utilisateur sur un thésaurus cas où
     * l'utilisateur d'un projet a un rôle uniquement à une partie des thésaurus
     * du projet
     *
     * @param idUser
     * @param idRole
     * @param idProject
     * @param idTheso
     * @return
     */
    public boolean deleteUserRoleOnTheso(int idUser, int idRole, int idProject, String idTheso) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from user_role_only_on "
                        + " where "
                        + " id_user = " + idUser
                        + " and "
                        + " id_role = " + idRole
                        + " and "
                        + " id_group = " + idProject
                        + " and "
                        + " id_theso = '" + idTheso + "'"
                );
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de supprimer tous les roles pour un utilisateur sur les thésaurus
     * d'un projet cas des rôles uniquement à une partie des thésaurus du projet
     * à appliquer avent update
     *
     * @param idUser
     * @param idProject
     * @return
     */
    public boolean deleteAllUserRoleOnTheso(int idUser, int idProject) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from user_role_only_on "
                        + " where "
                        + " id_user = " + idUser
                        + " and "
                        + " id_group = " + idProject
                );
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet d'ajouter un role pour un utilisateur sur ce thésaurus cas où on
     * ne donne pas accès à l'utilisateur à un projet, mais uniquement à une
     * partie des thésaurus du projet
     *
     * @param idUser
     * @param idRole
     * @param idProject
     * @param idTheso
     * @return
     */
    public boolean addUserRoleOnThisTheso(int idUser, int idRole, int idProject, String idTheso) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            if (!addUserRoleOnTheso_(conn,
                    idUser, idRole, idTheso, idProject)) {
                conn.rollback();
                return false;
            }
            conn.commit();
            status = true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet d'ajouter un role pour un utilisateur sur un lot de thésaurus cas
     * où on ne donne pas accès à l'utilisateur à un projet, mais uniquement à
     * une partie des thésaurus du projet
     *
     * @param idUser
     * @param idRole
     * @param idProject
     * @param idThesos
     * @return
     */
    public boolean addUserRoleOnTheso(int idUser, int idRole, int idProject, List<String> idThesos) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            /// On supprime d'abord le rôle de l'utilisateur sur le groupe
            if (!deleteRoleOnGroup(idUser, idProject)) {
                conn.rollback();
                return false;
            }
            for (String idTheso : idThesos) {
                if (!addUserRoleOnTheso_(conn, idUser, idRole, idTheso, idProject)) {
                    conn.rollback();
                    return false;
                }
            }
            conn.commit();
            status = true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    private boolean addUserRoleOnTheso_(Connection conn, int idUser, int idRole, String idTheso, int idProject) {

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("insert into user_role_only_on (id_user, id_role, id_theso, id_group)"
                    + " values("
                    + idUser + ","
                    + idRole + ","
                    + "'" + idTheso + "'"
                    + "," + idProject
                    + ") on conflict do nothing");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean addUserRoleOnGroupRollBack(Connection conn,
            int idUser, int idRole, int idGroup) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("insert into user_role_group (id_user, id_role, id_group)"
                    + " values("
                    + idUser + ","
                    + idRole + ","
                    + idGroup + ")");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Permet de passer un thésaurus d'un groupe à un autre
     *
     * @param idTheso
     * @param newGroup
     * @return #MR
     */
    public boolean moveThesoToGroup(String idTheso, int newGroup) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            if (!deleteThesoFromGroup(idTheso)) {
                conn.rollback();
                conn.close();
                return false;
            }
            if (!moveThesoToGroupRollBack(conn, idTheso, newGroup)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            status = true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    private boolean moveThesoToGroupRollBack(Connection conn, String idTheso, int newGroup) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("insert into user_group_thesaurus(id_group, id_thesaurus)"
                    + " values('" + newGroup + "'," + "'" + idTheso + "')");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * permet de supprimer l'appartenance d'un thesaurus à un groupe/projet
     *
     * @param idTheso
     * @return #MR
     */
    public boolean deleteThesoFromGroup(String idTheso) {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("delete from user_group_thesaurus where"
                    + " id_thesaurus ='" + idTheso + "'");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * permet d'ajouter un thesaurus à un groupe/projet
     *
     * @param idTheso
     * @param idGroup
     * @return #MR
     */
    public boolean addThesoToGroup(String idTheso, int idGroup) {
        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("insert into user_group_thesaurus (id_group, id_thesaurus) values ("
                    + idGroup + ",'" + idTheso + "')");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * permet de supprimer cet utilisateur
     *
     * @param idUser
     * @return
     */
    public boolean deleteUser(int idUser) {
        boolean status = false;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from users where id_user =" + idUser);
                stmt.executeUpdate("delete from user_role_group where id_user = " + idUser);
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * cette fonction permet de retourner la liste des groupes - roles pour un
     * un utilisateur
     *
     * @param idUser
     * @return #MR
     */
    public ArrayList<NodeUserRoleGroup> getUserRoleGroup(int idUser) {
        ArrayList<NodeUserRoleGroup> nodeUserRoleGroups = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT "
                        + "  user_role_group.id_role,"
                        + "  roles.name,"
                        + "  user_role_group.id_group,"
                        + "  user_group_label.label_group"
                        + " FROM user_role_group, roles, user_group_label"
                        + " WHERE"
                        + "  user_role_group.id_role = roles.id AND"
                        + "  user_group_label.id_group = user_role_group.id_group AND"
                        + "  user_role_group.id_user =" + idUser);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserRoleGroup nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id_role"));
                        nodeUserRoleGroup.setRoleName(resultSet.getString("name"));
                        nodeUserRoleGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserRoleGroup.setGroupName(resultSet.getString("label_group"));
                        //   nodeUserRoleGroup.setNodeUserGroupThesauruses(getUserThesaurusOfGroup(resultSet.getInt("id_group")));

                        if (resultSet.getInt("id_role") == 2) {
                            nodeUserRoleGroup.setAdmin(true);
                        }
                        if (resultSet.getInt("id_role") == 3) {
                            nodeUserRoleGroup.setManager(true);
                        }
                        if (resultSet.getInt("id_role") == 4) {
                            nodeUserRoleGroup.setContributor(true);
                        }

                        nodeUserRoleGroups.add(nodeUserRoleGroup);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoleGroups;
    }

    /**
     * permet de retourner le groupe à lequel le thesaurus appartient
     *
     * @param idTheso
     * @return
     */
    public int getGroupOfThisTheso(String idTheso) {
        int idGroup = -1;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT user_group_thesaurus.id_group"
                        + " FROM user_group_thesaurus"
                        + " WHERE"
                        + " user_group_thesaurus.id_thesaurus = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idGroup = resultSet.getInt("id_group");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idGroup;
    }

    /**
     * cette fonction permet de retourner la liste des utilisateurs qui
     * n'appartiennent à aucun groupe donc aucun role défini
     *
     * @return
     */
    public ArrayList<NodeUserRole> getUsersWithoutGroup() {
        ArrayList<NodeUserRole> listUser = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT users.id_user, users.username, users.active"
                        + " FROM users where users.issuperadmin!=true and users.id_user not in "
                        + "(select distinct id_user from user_role_group order by id_user)"
                        + " ORDER BY LOWER(users.username)");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserRole nodeUserRole = new NodeUserRole();
                        nodeUserRole.setIdUser(resultSet.getInt("id_user"));
                        nodeUserRole.setUserName(resultSet.getString("username"));
                        nodeUserRole.setActive(resultSet.getBoolean("active"));
                        nodeUserRole.setIdRole(-1);
                        nodeUserRole.setRoleName("");
                        listUser.add(nodeUserRole);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listUser;
    }

    /**
     * cette fonction permet de retourner la liste des roles/thesos pour un un
     * utilisateur et un groupe pour les droits limités
     *
     * @param idGroup
     * @param idUser
     * @return
     */
    public ArrayList<NodeThesoRole> getAllRolesThesosByUserGroupLimited(int idGroup, int idUser) {
        ArrayList<NodeThesoRole> nodeThesoRoles = new ArrayList<>();
        String idLang;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT roles.name, roles.id, user_role_only_on.id_theso"
                        + " FROM roles, user_role_only_on "
                        + " WHERE "
                        + " user_role_only_on.id_role = roles.id "
                        + " AND"
                        + " user_role_only_on.id_group = " + idGroup
                        + " AND"
                        + " user_role_only_on.id_user = " + idUser
                        + " ORDER BY LOWER(id_theso)");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeThesoRole nodeThesoRole = new NodeThesoRole();
                        nodeThesoRole.setIdRole(resultSet.getInt("id"));
                        nodeThesoRole.setRoleName(resultSet.getString("name"));
                        nodeThesoRole.setIdTheso(resultSet.getString("id_theso"));
                        idLang = preferencesHelper.getWorkLanguageOfTheso(nodeThesoRole.getIdTheso());
                        nodeThesoRole.setThesoName(thesaurusHelper.getTitleOfThesaurus(nodeThesoRole.getIdTheso(), idLang));
                        nodeThesoRoles.add(nodeThesoRole);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeThesoRoles;
    }

    /**
     * cette fonction permet de retourner la liste des roles/thesos pour un un
     * utilisateur et un groupe
     *
     * @param idGroup
     * @param idUser
     * @return
     */
    public ArrayList<NodeThesoRole> getAllRolesThesosByUserGroup(int idGroup, int idUser) {
        ArrayList<NodeThesoRole> nodeThesoRoles = new ArrayList<>();
        String idLang;
        int idRole;

        ArrayList<String> listThesos = getIdThesaurusOfProject(idGroup);
        for (String idTheso : listThesos) {
            idRole = getRoleOnThisTheso(idUser, idGroup, idTheso);
            if (idRole != -1) {
                NodeThesoRole nodeThesoRole = new NodeThesoRole();
                nodeThesoRole.setIdRole(idRole);
                nodeThesoRole.setRoleName(getRoleName(idRole));
                nodeThesoRole.setIdTheso(idTheso);
                idLang = preferencesHelper.getWorkLanguageOfTheso(idTheso);
                nodeThesoRole.setThesoName(thesaurusHelper.getTitleOfThesaurus(idTheso, idLang));
                nodeThesoRoles.add(nodeThesoRole);
            }
        }
        return nodeThesoRoles;
    }

    /**
     * cette fonction permet de retourner la liste des utilisateurs pour un
     * groupe avec un role limité sur des thésaurus du group/projet en cours
     *
     * @param idGroup
     * @return
     */
    public ArrayList<NodeUserRole> getAllUsersRolesLimitedByTheso(int idGroup) {
        ArrayList<NodeUserRole> listUser = new ArrayList<>();
        String idLang;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT users.id_user, users.username, users.active, roles.name, roles.id, user_role_only_on.id_theso"
                        + " FROM"
                        + " users, roles, user_role_only_on "
                        + " WHERE "
                        + " user_role_only_on.id_role = roles.id"
                        + " AND"
                        + " users.id_user = user_role_only_on.id_user"
                        + " AND"
                        + " user_role_only_on.id_group = " + idGroup
                        + " AND"
                        + " users.issuperadmin != true "
                        + " ORDER BY LOWER(users.username)");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserRole nodeUserRole = new NodeUserRole();
                        nodeUserRole.setIdUser(resultSet.getInt("id_user"));
                        nodeUserRole.setUserName(resultSet.getString("username"));
                        nodeUserRole.setActive(resultSet.getBoolean("active"));
                        nodeUserRole.setIdRole(resultSet.getInt("id"));
                        nodeUserRole.setRoleName(resultSet.getString("name"));
                        nodeUserRole.setIdTheso(resultSet.getString("id_theso"));
                        idLang = preferencesHelper.getWorkLanguageOfTheso(nodeUserRole.getIdTheso());
                        nodeUserRole.setThesoName(thesaurusHelper.getTitleOfThesaurus(nodeUserRole.getIdTheso(), idLang));
                        listUser.add(nodeUserRole);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listUser;
    }

    /**
     * cette fonction permet de retourner la liste des utilisateurs pour un
     * groupe avec un role limité sur des thésaurus du group/projet en cours
     *
     * @param idUser
     * @return
     */
    public List<String> getListThesoLimitedRoleByUser(int idUser) {
        List<String> listThesos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT user_role_only_on.id_theso"
                        + " FROM"
                        + " user_role_only_on "
                        + " WHERE "
                        + " user_role_only_on.id_user = " + idUser
                        + " ORDER BY id_theso");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listThesos.add(resultSet.getString("id_theso"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listThesos;
    }

    /**
     * cette fonction permet de retourner la liste des utilisateurs pour un
     * groupe avec un role limité sur des thésaurus du group/projet en cours
     *
     * @param idUser
     * @return
     */
    public List<String> getListThesoLimitedRoleByUserAsAdmin(int idUser) {
        List<String> listThesos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT user_role_only_on.id_theso"
                        + " FROM"
                        + " user_role_only_on "
                        + " WHERE "
                        + " user_role_only_on.id_user = " + idUser
                        + " and user_role_only_on.id_role = 2"
                        + " ORDER BY id_theso");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listThesos.add(resultSet.getString("id_theso"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listThesos;
    }

    /**
     * cette fonction permet de retourner la liste des utilisateurs pour un
     * groupe avec un role limité sur des thésaurus du group/projet en cours
     *
     * @param idGroup
     * @param idUser
     * @return
     */
    public ArrayList<NodeUserRole> getListRoleByThesoLimited(int idGroup, int idUser) {

        ArrayList<NodeUserRole> listUser = new ArrayList<>();
        String idLang;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT user_role_only_on.id_theso, roles.name, roles.id "
                        + " FROM user_role_only_on, roles "
                        + " WHERE "
                        + " user_role_only_on.id_role = roles.id"
                        + " AND "
                        + " user_role_only_on.id_group = " + idGroup
                        + " AND "
                        + " user_role_only_on.id_user = " + idUser
                        + " ORDER BY LOWER(id_theso)");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserRole nodeUserRole = new NodeUserRole();
                        nodeUserRole.setIdRole(resultSet.getInt("id"));
                        nodeUserRole.setRoleName(resultSet.getString("name"));
                        nodeUserRole.setIdTheso(resultSet.getString("id_theso"));
                        idLang = preferencesHelper.getWorkLanguageOfTheso(nodeUserRole.getIdTheso());
                        nodeUserRole.setThesoName(thesaurusHelper.getTitleOfThesaurus(nodeUserRole.getIdTheso(), idLang));
                        listUser.add(nodeUserRole);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listUser;
    }

    /**
     * cette fonction permet de retourner la liste des utilisateurs pour un
     * groupe avec un role égale ou inférieur au role de l'utilisateur en cours
     *
     * @param idGroup
     * @param idRole
     * @return
     */
    public ArrayList<NodeUserRole> getUsersRolesByGroup(int idGroup, int idRole) {
        ArrayList<NodeUserRole> listUser = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT"
                        + "  users.id_user,"
                        + "  users.username,"
                        + "  users.active,"
                        + "  roles.name,"
                        + "  roles.id"
                        + " FROM "
                        + "  user_role_group,"
                        + "  users,"
                        + "  roles"
                        + " WHERE"
                        + "  user_role_group.id_role = roles.id AND"
                        + "  users.id_user = user_role_group.id_user AND"
                        + "  user_role_group.id_group =" + idGroup
                        + " and users.issuperadmin != " + true
                        + " and user_role_group.id_role >= " + idRole
                        + " ORDER BY LOWER(users.username)");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUserRole nodeUserRole = new NodeUserRole();
                        nodeUserRole.setIdUser(resultSet.getInt("id_user"));
                        nodeUserRole.setUserName(resultSet.getString("username"));
                        nodeUserRole.setActive(resultSet.getBoolean("active"));
                        nodeUserRole.setIdRole(resultSet.getInt("id"));
                        nodeUserRole.setRoleName(resultSet.getString("name"));
                        listUser.add(nodeUserRole);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listUser;
    }

    /**
     * cette fonction permet de retourner le role de l'utilisateur sur ce groupe
     *
     * @param idUser
     * @param idGroup
     * @return
     */
    public NodeUserRoleGroup getUserRoleOnThisGroup(
            int idUser, int idGroup) {
        NodeUserRoleGroup nodeUserRoleGroup = null;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT \n"
                        + "  roles.id, \n"
                        + "  roles.name, \n"
                        + "  user_group_label.label_group, \n"
                        + "  user_group_label.id_group\n"
                        + " FROM \n"
                        + "  user_role_group, \n"
                        + "  roles, \n"
                        + "  user_group_label\n"
                        + " WHERE \n"
                        + "  user_role_group.id_role = roles.id AND\n"
                        + "  user_group_label.id_group = user_role_group.id_group AND\n"
                        + "  user_role_group.id_user = " + idUser + " AND \n"
                        + "  user_role_group.id_group = " + idGroup);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id"));
                        nodeUserRoleGroup.setRoleName(resultSet.getString("name"));
                        nodeUserRoleGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserRoleGroup.setIdGroup(resultSet.getInt("id_group"));
                        if (resultSet.getInt("id") == 2) {
                            nodeUserRoleGroup.setAdmin(true);
                        }
                        if (resultSet.getInt("id") == 3) {
                            nodeUserRoleGroup.setManager(true);
                        }
                        if (resultSet.getInt("id") == 4) {
                            nodeUserRoleGroup.setContributor(true);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoleGroup;
    }

    public String getRoleName(int idRole) {
        switch (idRole) {
            case 1:
                return "superAdmin";
            case 2:
                return "admin";
            case 3:
                return "manager";
            case 4:
                return "contributor";
            default:
                return "";
        }
    }

    /**
     * cette fonction permet de retourner le role de l'utilisateur sur ce
     * thésaurus
     *
     * @param idUser
     * @param idGroup
     * @param idTheso
     * @return
     */
    public int getRoleOnThisTheso(int idUser, int idGroup, String idTheso) {
        int idRole = -1;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT user_role_only_on.id_role"
                        + " FROM  "
                        + " user_role_only_on"
                        + " WHERE "
                        + " user_role_only_on.id_theso = '" + idTheso + "'"
                        + " AND "
                        + " user_role_only_on.id_group = " + idGroup
                        + " AND "
                        + " user_role_only_on.id_user = " + idUser);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idRole = resultSet.getInt("id_role");
                    }
                }
                if (idRole == -1) {
                    stmt.executeQuery("select user_role_group.id_role "
                            + " from user_role_group, user_group_thesaurus"
                            + " where"
                            + " user_role_group.id_group = user_group_thesaurus.id_group"
                            + " and"
                            + " user_role_group.id_user = " + idUser
                            + " and"
                            + " user_role_group.id_group = " + idGroup
                            + " and"
                            + " user_group_thesaurus.id_thesaurus = '" + idTheso + "'");
                    try (ResultSet resultSet = stmt.getResultSet()) {
                        if (resultSet.next()) {
                            idRole = resultSet.getInt("id_role");
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, "erreur", ex);
        }
        return idRole;
    }

    /**
     * cette fonction permet de retourner le role de l'utilisateur sur ce
     * thésaurus
     *
     * @param idUser
     * @param idGroup
     * @param idTheso
     * @return
     */
    public NodeUserRoleGroup getUserRoleOnThisTheso(int idUser, int idGroup, String idTheso) {
        NodeUserRoleGroup nodeUserRoleGroup = null;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT user_role_only_on.id_role"
                        + " FROM  "
                        + " user_role_only_on"
                        + " WHERE "
                        + " user_role_only_on.id_theso = '" + idTheso + "'"
                        + " AND "
                        + " user_role_only_on.id_group = " + idGroup
                        + " AND "
                        + " user_role_only_on.id_user = " + idUser);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id_role"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoleGroup;
    }

    /**
     * cette fonction permet de retourner le role du superAdmin
     *
     * @return
     */
    public NodeUserRoleGroup getUserRoleForSuperAdmin() {
        NodeUserRoleGroup nodeUserRoleGroup = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT "
                        + "  roles.id, "
                        + "  roles.name "
                        + " FROM "
                        + "  public.roles "
                        + " WHERE "
                        + "  roles.id = 1");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id"));
                        nodeUserRoleGroup.setRoleName(resultSet.getString("name"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoleGroup;
    }

    /**
     * permet de supprimer le role d'un utilisateur sur ce groupe
     *
     * @param idUser
     * @param idGroup
     * @return
     */
    public boolean deleteRoleOnGroup(int idUser, int idGroup) {
        boolean status = false;
        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("delete from user_role_group where"
                    + " id_user =" + idUser
                    + " and id_group = " + idGroup);
            status = true;
        } catch (SQLException sqle) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return status;
    }

    /**
     * permet de supprimer le Projet ou groupe
     *
     * @param idGroup
     * @return
     */
    public boolean deleteProjectGroup(int idGroup) {
        Statement stmt;
        boolean status = false;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from user_role_group where"
                            + " id_group =" + idGroup;
                    stmt.executeUpdate(query);

                    query = "delete from user_group_thesaurus where"
                            + " id_group =" + idGroup;
                    stmt.executeUpdate(query);

                    query = "delete from user_group_label where"
                            + " id_group =" + idGroup;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
            } finally {
            }
        } catch (SQLException sqle) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, sqle);
        }

        try {
            if (conn != null) {
                if (status) {
                    conn.commit();
                    conn.close();
                } else {
                    conn.rollback();
                    conn.close();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * cette fonction permet de retourner le role de l'utilisateur sur ce groupe
     *
     * @param idUser
     * @param idGroup
     * @return
     */
    public NodeUserRoleGroup getUserRoleOnThisGroupForSuperAdmin(int idUser, int idGroup) {

        NodeUserRoleGroup nodeUserRoleGroup = null;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT "
                        + "  roles.id, "
                        + "  roles.name, "
                        + "  user_group_label.id_group, "
                        + "  user_group_label.label_group"
                        + " FROM "
                        + "  public.roles, "
                        + "  public.user_group_label"
                        + " WHERE "
                        + "  roles.id = " + idUser + " AND "
                        + "  user_group_label.id_group = " + idGroup);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id"));
                        nodeUserRoleGroup.setRoleName(resultSet.getString("name"));
                        nodeUserRoleGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserRoleGroup.setIdGroup(resultSet.getInt("id_group"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoleGroup;
    }

    public boolean isUserMailExist(String mail) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT mail FROM users WHERE mail ilike '" + mail + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    if (resultSet.getRow() != 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * permet de retourner le pseudo de l'utilisateur d'après son Email
     *
     * @param email
     * @return
     */
    public String getNameUser(String email) {
        String name = "";

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT username from users "
                        + " WHERE mail ilike '" + email + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        name = resultSet.getString("username");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return name;
    }

    /**
     * Cette fonction permet de récupérer l'Id de l'utilisateur d'après son
     * Login et son passe
     *
     * @param login
     * @param pwd
     * @return
     */
    public boolean isUserExist(String login, String pwd) {
        boolean existe = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_user FROM users WHERE username ilike '" + login + "' AND password='" + pwd + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = true;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }

    public boolean updatePwd(int idUser, String newPwd) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE users set password = '" + newPwd
                        + "' WHERE id_user = " + idUser);
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    public boolean updatePseudo(int idUser, String pseudo) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE users set username = '" + pseudo
                        + "' WHERE id_user = " + idUser);
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de retourner le nom de l'utilisateur
     *
     * @param userId
     * @return
     */
    public String getNameUser(int userId) {
        String name = "";

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT username from users "
                        + " WHERE id_user =" + userId);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        name = resultSet.getString("username");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return name;
    }

    ///////////////////////////
    /// fin à vérifier
    //////////////////////////    
    /**
     * permet de renommer un projet
     *
     * @param newValue
     * @param idProject
     * @return
     */
    public boolean updateProject(String newValue, int idProject) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE user_group_label set label_group = '" + newValue
                        + "' WHERE id_group = " + idProject);
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    public boolean updateMail(int idUser, String newMail) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE users set mail = '" + newMail
                        + "' WHERE id_user = " + idUser);
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de mette à jour le status des alertes mail pour l'utilisateur
     *
     * @param idUser
     * @param alertMail
     * @return #MR
     */
    public boolean setAlertMailForUser(int idUser, boolean alertMail) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE users set alertmail = " + alertMail
                        + " WHERE id_user = " + idUser);
                status = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * Cette fonction permet de savoir si le Pseudo existe
     *
     * @param pseudo
     * @return
     */
    public boolean isPseudoExist(String pseudo) {
        boolean existe = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_user FROM users WHERE username='" + pseudo + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = true;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }

    public boolean isMailExist(String mail) {
        boolean status = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT mail FROM users WHERE mail ilike '" + mail + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if (resultSet.getRow() != 0) {
                            status = true;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * Permet de savoir si l'utilisateur a un droit d'admin sur le groupe
     *
     * @param idUser
     * @param idGroup
     * @return
     */
    public boolean isAdminOnThisGroup(
            int idUser, int idGroup) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT "
                        + "  user_role_group.id_user"
                        + " FROM "
                        + "  user_role_group"
                        + " WHERE "
                        + "  user_role_group.id_group = " + idGroup
                        + " AND user_role_group.id_user = " + idUser
                        + " AND user_role_group.id_role < 3");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    if (resultSet.getRow() != 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Cette fonction permet de retourner la liste des roles autorisés pour un
     * utilisateur (c'est la liste qu'un utilisateur a le droit d'attribué à un
     * nouvel utilisateur)
     *
     * @param idRoleFrom
     * @return
     */
    public ArrayList<Map.Entry<String, String>> getAuthorizedRoles(int idRoleFrom) {
        Map map = new HashMap();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id, name from roles "
                        + " where id >= " + idRoleFrom);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        map.put(resultSet.getString("id"), resultSet.getString("name"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<Map.Entry<String, String>> listeRoles = new ArrayList<>(map.entrySet());
        return listeRoles;
    }

    /**
     * Cette fonction permet de retourner la liste des roles autorisés pour un
     * utilisateur (c'est la liste qu'un utilisateur a le droit d'attribué à un
     * nouvel utilisateur)
     *
     * @param myIdRole
     * @return
     */
    public ArrayList<NodeIdValue> getMyAuthorizedRoles(int myIdRole) {
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id, name from roles "
                        + " where id >= " + myIdRole);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id"));
                        nodeIdValue.setValue(resultSet.getString("name"));
                        nodeIdValues.add(nodeIdValue);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIdValues;
    }

    /**
     * Cette fonction permet de retourner la liste des projets dont
     * l'utilisateur est admin ou SuperAdmin
     *
     * @param idUser
     * @return
     */
    public ArrayList<NodeIdValue> getMyAuthorizedProjects(int idUser) {
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select user_group_label.id_group, user_group_label.label_group"
                        + " from user_role_group, user_group_label"
                        + " where "
                        + " user_role_group.id_group = user_group_label.id_group"
                        + " and"
                        + " id_role <= 2"
                        + " and"
                        + " id_user = " + idUser);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id"));
                        nodeIdValue.setId(resultSet.getString("name"));
                        nodeIdValues.add(nodeIdValue);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIdValues;
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    //////// fin des nouvelles fontions ////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////


    public Optional<Integer> getUserGroupId (int userId, String thesoId){
        try (Connection connection = dataSource.getConnection()){
            try( PreparedStatement stmt = connection.prepareStatement("SELECT user_role_group.id_group " +
                    "FROM user_role_group  " +
                    "JOIN user_group_thesaurus user_group_thesaurus ON user_role_group.id_group = user_group_thesaurus.id_group " +
                    "WHERE user_role_group.id_user = ? " +
                    "AND user_group_thesaurus.id_thesaurus = ? ")){
                stmt.setInt(1, userId);
                stmt.setString(2, thesoId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {return Optional.empty();}
                return Optional.of(rs.getInt("id_group"));

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Sauvegarde en base de donnée les informations concernant la clé d'API
     * @param userId
     * @param keyNeverExpire
     * @param apiKeyExpireDate
     */
    public boolean updateApiKeyInfos(int userId, Boolean keyNeverExpire, LocalDate apiKeyExpireDate) {
        String sql = "UPDATE users SET key_never_expire = ?, key_expires_at = ? WHERE id_user = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, keyNeverExpire);
            if (apiKeyExpireDate != null) {
                pstmt.setDate(2, java.sql.Date.valueOf(apiKeyExpireDate));
            } else {
                pstmt.setNull(2, java.sql.Types.DATE);
            }
            pstmt.setInt(3, userId);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
