package fr.cnrs.opentheso.repositories;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.users.NodeUserComplet;
import fr.cnrs.opentheso.models.users.NodeUserRole;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.models.userpermissions.NodeThesoRole;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.entites.UserGroupLabel;

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
public class UserHelper implements Serializable {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;


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
     * cette fonction permet de retourner la liste des groupes d'un utilisateur
     *
     * @param idUser
     * @return
     */
    public Map<String, String> getGroupsOfUser(int idUser) {

        HashMap<String, String> listGroup = new LinkedHashMap<>();

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
     * permet de retourner la liste des thésaurus pour un projet
     *
     * @param idProject
     * @param idLang
     * @return
     */
    //TODO TO DELETE
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

    // TODO TO DELETE
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
    //TODO Firas Replace by findUserRoleOnThisGroup
    public NodeUserRoleGroup getUserRoleOnThisGroup(int idUser, int idGroup) {
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

}
