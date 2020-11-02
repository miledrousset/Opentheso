package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroupThesaurus;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroupUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRole;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bdd.tools.StringPlus;

public class UserHelper {

    public UserHelper() {
    }

    //// restructuration de la classe User le 05/04/2018 //////    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////// Nouvelles fontions #MR//////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////    


    /**
     * permet de rechercher un utilisateur avec son nom
     * @param ds
     * @param userName
     * @return 
     */
    public ArrayList<NodeUser> searchUser(HikariDataSource ds, String userName){
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeUser> nodeUsers = new ArrayList<>();
        userName = stringPlus.convertString(userName);

        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "select id_user, username from users where " +
                            " username ilike '%" + userName + "%' order by username";

                    resultSet = stmt.executeQuery(query);
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
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUsers;
    }

    
    /**
     * permet de rechercher les projets dont je suis Admin pour depalcer les thésaurus
     * @param ds
     * @param idUser
     * @param projectName
     * @return 
     */
    public ArrayList<NodeUserGroup> searchMyProject(
            HikariDataSource ds,
            int idUser,
            String projectName) {
        ArrayList<NodeUserGroup> nodeUserGroups = new ArrayList<>();

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT " +
                            " user_group_label.id_group," +
                            " user_group_label.label_group" +
                            " FROM" +
                            " user_role_group," +
                            " user_group_label" +
                            " WHERE" +
                            " user_role_group.id_group = user_group_label.id_group" +
                            " AND" +
                            " user_role_group.id_user = " + idUser + 
                            " AND" +
                            " user_role_group.id_role = 2" +
                            " and user_group_label.label_group ilike '%" + projectName + "%'" +
                            " order by label_group";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeUserGroup nodeUserGroup = new NodeUserGroup();
                        nodeUserGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroups.add(nodeUserGroup);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroups;
    }     
    
    /**
     * permet de rechercher les projets pour le SuperAdmin pour depalcer les thésaurus
     * @param ds
     * @param projectName
     * @return 
     */
    public ArrayList<NodeUserGroup> searchAllProject(
            HikariDataSource ds, String projectName) {
        ArrayList<NodeUserGroup> nodeUserGroups = new ArrayList<>();

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT " +
                            " user_group_label.id_group," +
                            " user_group_label.label_group" +
                            " FROM" +
                            " user_group_label" +
                            " WHERE" +
                            " user_group_label.label_group ilike '%" + projectName +"%'" +
                            " order by label_group";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeUserGroup nodeUserGroup = new NodeUserGroup();
                        nodeUserGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroups.add(nodeUserGroup);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroups;
    }         

    /**
     * Cette fonction permet d'ajouter un nouvel utilisateur sans le role
     *
     * @param ds
     * @param userName
     * @param mail
     * @param password
     * @param isSuperAdmin
     * @param alertMail
     * @return #MR
     */
    public boolean addUser(HikariDataSource ds,
            String userName, String mail,
            String password, boolean isSuperAdmin,
            boolean alertMail) {
        boolean active = true;
        boolean passtomodify = false;

        boolean status = false;
        Statement stmt;
        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into users"
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
                            + ")";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @param login
     * @param pwd
     * @return
     */
    public int getIdUser(HikariDataSource ds, String login, String pwd) {
        int idUser = -1;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_user FROM users WHERE username ilike '"
                            + login + "' AND password='" + pwd + "'";
                    resultSet = stmt.executeQuery(query);
                    if (resultSet.next()) {
                        idUser = resultSet.getInt("id_user");
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idUser;
    }

    /**
     * cette fonction permet de retourner les informations de l'utilisateur
     *
     * @param ds
     * @param idUser
     * @return #MR
     */
    public NodeUser getUser(HikariDataSource ds, int idUser) {
        NodeUser nodeUser = null;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT"
                            + "  users.id_user,"
                            + "  users.username,"
                            + "  users.active,"
                            + "  users.mail,"
                            + "  users.passtomodify,"
                            + "  users.alertmail,"
                            + "  users.issuperadmin"
                            + " FROM users"
                            + " WHERE "
                            + " users.id_user = " + idUser;
                    resultSet = stmt.executeQuery(query);
                    if (resultSet.next()) {
                        nodeUser = new NodeUser();
                        nodeUser.setIdUser(idUser);
                        nodeUser.setName(resultSet.getString("username"));
                        nodeUser.setIsActive(resultSet.getBoolean("active"));
                        nodeUser.setMail(resultSet.getString("mail"));
                        nodeUser.setIsAlertMail(resultSet.getBoolean("alertmail"));
                        nodeUser.setPasstomodify(resultSet.getBoolean("passtomodify"));
                        nodeUser.setIsSuperAdmin(resultSet.getBoolean("issuperadmin"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @param idGroup
     * @return
     */
    public String getGroupName(
            HikariDataSource ds, int idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        String groupLabel = null;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT"
                            + "  user_group_label.label_group"
                            + " FROM"
                            + "  user_group_label"
                            + " WHERE"
                            + "  user_group_label.id_group = " + idGroup;
                    resultSet = stmt.executeQuery(query);
                    if (resultSet.next()) {
                        groupLabel = resultSet.getString("label_group");
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return groupLabel;
    }

    /**
     * cette fonction permet de retourner la liste des groupes d'un utilisateur
     *
     *
     * @param ds
     * @param idUser
     * @return
     */
    public Map<String, String> getGroupsOfUser(
            HikariDataSource ds, int idUser) {
        HashMap<String, String> listGroup = new LinkedHashMap();

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT"
                            + "  user_group_label.id_group,"
                            + "  user_group_label.label_group"
                            + " FROM"
                            + "  user_role_group,"
                            + "  user_group_label"
                            + " WHERE"
                            + "  user_role_group.id_group = user_group_label.id_group AND"
                            + "  user_role_group.id_user = " + idUser + " order by label_group";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        listGroup.put("" + resultSet.getInt("id_group"), resultSet.getString("label_group"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listGroup;
    }

    /**
     * cette fonction permet de retourner tous les groupes existant au format
     * MAP c'est pour le SuperAdmin
     *
     *
     * @param ds
     * @return
     */
    public Map<String, String> getAllGroups(
            HikariDataSource ds) {
        HashMap<String, String> sortedHashMap = new LinkedHashMap();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT  user_group_label.id_group,  user_group_label.label_group FROM user_group_label order by label_group";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        sortedHashMap.put("" + resultSet.getInt("id_group"), resultSet.getString("label_group"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     *
     * @param ds
     * @return
     */
    public ArrayList<NodeUserGroup> getAllProject(
            HikariDataSource ds) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeUserGroup> nodeUserGroups = new ArrayList<>();

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT  user_group_label.id_group,  user_group_label.label_group FROM user_group_label order by label_group";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeUserGroup nodeUserGroup = new NodeUserGroup();
                        nodeUserGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroups.add(nodeUserGroup);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroups;
    }
    
    /**
     * retourne la liste de tous les projets où l'utilisateur a des droits Admin dessus
     * @param ds
     * @param idUser
     * @return 
     */
    public ArrayList<NodeUserGroup> getProjectsOfUserAsAdmin(
            HikariDataSource ds, int idUser) {
        ArrayList<NodeUserGroup> nodeUserGroups = new ArrayList<>();

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT" +
                                " user_group_label.id_group, user_group_label.label_group" +
                                " FROM" +
                                " user_role_group, user_group_label" +
                                " WHERE" +
                                " user_role_group.id_group = user_group_label.id_group" +
                                " AND" +
                                " user_role_group.id_user = " + idUser +
                                " AND" +
                                " user_role_group.id_role = 2" +
                                " order by label_group";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeUserGroup nodeUserGroup = new NodeUserGroup();
                        nodeUserGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroups.add(nodeUserGroup);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroups;
    }        
    
    /**
     * retourne la liste de tous les projets où l'utilisateur a des droits
     * @param ds
     * @param idUser
     * @return 
     */
    public ArrayList<NodeUserGroup> getProjectsOfUser(
            HikariDataSource ds, int idUser) {
        ArrayList<NodeUserGroup> nodeUserGroups = new ArrayList<>();

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT"
                            + "  user_group_label.id_group,"
                            + "  user_group_label.label_group"
                            + " FROM"
                            + "  user_role_group,"
                            + "  user_group_label"
                            + " WHERE"
                            + "  user_role_group.id_group = user_group_label.id_group AND"
                            + "  user_role_group.id_user = " + idUser + " order by label_group";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeUserGroup nodeUserGroup = new NodeUserGroup();
                        nodeUserGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroups.add(nodeUserGroup);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroups;
    }      
    
 
    /**
     * permet de retourner la liste des thésaurus pour un projet 
     *
     * @param ds
     * @param idProject
     * @param idLang
     * @return
     */
    public ArrayList<NodeIdValue> getThesaurusOfProject(
            HikariDataSource ds,
            int idProject,
            String idLang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT"
                            + "  thesaurus_label.title,"
                            + "  thesaurus_label.id_thesaurus"
                            + " FROM "
                            + "  user_group_thesaurus,"
                            + "  thesaurus_label"
                            + " WHERE "
                            + "  user_group_thesaurus.id_thesaurus = thesaurus_label.id_thesaurus AND"
                            + "  user_group_thesaurus.id_group = " + idProject
                            + " and thesaurus_label.lang = '" + idLang + "' order by title";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id_thesaurus"));
                        nodeIdValue.setValue(resultSet.getString("title"));
                        nodeIdValues.add(nodeIdValue);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIdValues;
    }    
    
    /**
     * permet de retourner la liste des thésaurus pour un groupe pour un
     * affichage IHM
     *
     * @param ds
     * @param idGroup
     * @param idLang
     * @return
     */
    public Map<String, String> getThesaurusLabelsOfGroup(HikariDataSource ds, int idGroup,
            String idLang) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        Map<String, String> listThesos = null;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT distinct"
                            + "  thesaurus_label.title,"
                            + "  thesaurus_label.id_thesaurus"
                            + " FROM "
                            + "  user_group_thesaurus,"
                            + "  thesaurus_label"
                            + " WHERE "
                            + "  user_group_thesaurus.id_thesaurus = thesaurus_label.id_thesaurus AND"
                            + "  user_group_thesaurus.id_group = " + idGroup
                            + " and thesaurus_label.lang = '" + idLang + "'";
                    resultSet = stmt.executeQuery(query);
                    listThesos = new HashMap<>();
                    while (resultSet.next()) {
                        listThesos.put(resultSet.getString("id_thesaurus"), resultSet.getString("title"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listThesos;
    }

    /**
     * permet de retourner la liste de tous les utilisateurs 
     *
     * @param ds
     * @return
     */
    public ArrayList<NodeUser> getAllUsers(HikariDataSource ds) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeUser> nodeUsers = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM users order by username";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeUser nodeUser = new NodeUser();
                        nodeUser.setIdUser(resultSet.getInt("id_user"));
                        nodeUser.setName(resultSet.getString("username"));
                        nodeUser.setIsActive(resultSet.getBoolean("active"));
                        nodeUser.setIsSuperAdmin(resultSet.getBoolean("issuperadmin"));
                        nodeUsers.add(nodeUser);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUsers;
    }    
    
    /**
     * permet de retourner la liste de tous les utilisateurs qui ne sont pas
     * SuperAdmin
     *
     * @param ds
     * @return
     */
    public Map<String, String> getAllUsersNotSuperadmin(HikariDataSource ds) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        Map<String, String> listUsers = null;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_user, username FROM users where issuperadmin != true order by username";

                    resultSet = stmt.executeQuery(query);
                    listUsers = new HashMap<>();
                    while (resultSet.next()) {
                        listUsers.put(resultSet.getString("id_user"), resultSet.getString("username"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listUsers;
    }

    /**
     * cette fonction permet de retourner la liste des thésaurus pour un
     * utilisateur l'utilisateur peut faire partie de plusieurs groupes, donc on
     * retourne la liste de tous ces thésaurus dans différents groupes
     *
     * @param ds
     * @param idUser
     * @return #MR
     */
    public List<String> getThesaurusOfUser(HikariDataSource ds, int idUser) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<String> nodeUserGroupThesauruses = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT distinct"
                            + " user_group_thesaurus.id_thesaurus"
                            + " FROM "
                            + "  user_group_thesaurus,"
                            + "  user_role_group"
                            + " WHERE "
                            + "  user_role_group.id_group = user_group_thesaurus.id_group AND"
                            + "  user_role_group.id_user = " + idUser
                            + " order by id_thesaurus";
                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        nodeUserGroupThesauruses.add(resultSet.getString("id_thesaurus"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserGroupThesauruses;
    }
    
    /**
     * cette fonction permet de retourner la liste des thésaurus où l'utilisateur a des droits admin l
     * l'utilisateur peut faire partie de plusieurs groupes, donc on
     * retourne la liste de tous ces thésaurus de différents groupes
     *
     * @param ds
     * @param idUser
     * @return #MR
     */
    public List<String> getThesaurusOfUserAsAdmin(HikariDataSource ds, int idUser) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<String> nodeUserGroupThesauruses = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT" +
                                " user_group_thesaurus.id_thesaurus" +
                                " FROM " +
                                " user_group_thesaurus, user_role_group" +
                                " WHERE " +
                                " user_role_group.id_group = user_group_thesaurus.id_group" +
                                " AND" +
                                " user_role_group.id_user =  " + idUser +
                                " AND" +
                                " user_role_group.id_role = 2" +
                                " order by id_thesaurus;";
                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        nodeUserGroupThesauruses.add(resultSet.getString("id_thesaurus"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @param idLangSource
     * @return #MR
     */
    public ArrayList<NodeUserGroupThesaurus> getAllGroupTheso(HikariDataSource ds, String idLangSource) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeUserGroupThesaurus> nodeUserGroupThesauruses = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + "  user_group_label.label_group,"
                            + "  thesaurus_label.title,"
                            + "  user_group_thesaurus.id_thesaurus,"
                            + "  user_group_thesaurus.id_group"
                            + " FROM "
                            + "  thesaurus_label, "
                            + "  user_group_thesaurus, "
                            + "  user_group_label"
                            + " WHERE "
                            + "  user_group_thesaurus.id_group = user_group_label.id_group AND"
                            + "  user_group_thesaurus.id_thesaurus = thesaurus_label.id_thesaurus AND"
                            + "  thesaurus_label.lang = '" + idLangSource + "'"
                            + " ORDER BY LOWER(thesaurus_label.title)";
                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeUserGroupThesaurus nodeUserGroupThesaurus = new NodeUserGroupThesaurus();
                        nodeUserGroupThesaurus.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeUserGroupThesaurus.setThesaurusName(resultSet.getString("title"));
                        nodeUserGroupThesaurus.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserGroupThesaurus.setGroupName(resultSet.getString("label_group"));

                        nodeUserGroupThesauruses.add(nodeUserGroupThesaurus);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @param idLangSource
     * @return #MR
     */
    public ArrayList<NodeUserGroupThesaurus> getAllThesoWithoutGroup(HikariDataSource ds, String idLangSource) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        /// fonction prête à intégrer        
        ArrayList<NodeUserGroupThesaurus> nodeUserGroupThesauruses = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT thesaurus_label.id_thesaurus, thesaurus_label.title "
                            + " FROM  thesaurus_label"
                            + " WHERE thesaurus_label.lang = '" + idLangSource + "'"
                            + " and thesaurus_label.id_thesaurus not in "
                            + "(select id_thesaurus from  user_group_thesaurus)"
                            + " ORDER BY LOWER(thesaurus_label.title)";
                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeUserGroupThesaurus nodeUserGroupThesaurus = new NodeUserGroupThesaurus();
                        nodeUserGroupThesaurus.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeUserGroupThesaurus.setThesaurusName(resultSet.getString("title"));
                        nodeUserGroupThesaurus.setIdGroup(-1);
                        nodeUserGroupThesaurus.setGroupName("");

                        nodeUserGroupThesauruses.add(nodeUserGroupThesaurus);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @param idLangSource
     * @return #MR
     */
    public ArrayList<NodeUserGroupUser> getAllGroupUser(HikariDataSource ds, String idLangSource) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        ArrayList<NodeUserGroupUser> nodeUserGroupUsers = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT"
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
                            + " roles.id = user_role_group.id_role";
                    resultSet = stmt.executeQuery(query);

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
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @param idLangSource
     * @return #MR
     */
    public ArrayList<NodeUserGroupUser> getAllGroupUserWithoutGroup(HikariDataSource ds, String idLangSource) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        ArrayList<NodeUserGroupUser> nodeUserGroupUsers = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT"
                            + " users.username,"
                            + " users.id_user"
                            + " FROM"
                            + " users"
                            + " WHERE "
                            + " users.issuperadmin != true"
                            + " and"
                            + " users.id_user not in"
                            + " (select user_role_group.id_user from user_role_group)";
                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeUserGroupUser nodeUserGroupUser = new NodeUserGroupUser();
                        nodeUserGroupUser.setIdUser(resultSet.getString("id_user"));
                        nodeUserGroupUser.setUserName(resultSet.getString("username"));
                        nodeUserGroupUser.setIdGroup(-1);
                        nodeUserGroupUser.setGroupName("");

                        nodeUserGroupUsers.add(nodeUserGroupUser);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @param userGroupName
     * @return
     */
    public boolean createUserGroup(HikariDataSource ds,
            String userGroupName) {
        Statement stmt;
        boolean status = false;
        
        userGroupName = new StringPlus().convertString(userGroupName);        
        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into user_group_label"
                            + "(label_group)"
                            + " values ('"
                            + userGroupName + "')";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de savoir si le groupe ou projet existe déja
     *
     * @param ds
     * @param userGroupName
     * @return
     */
    public boolean isUserGroupExist(HikariDataSource ds, String userGroupName) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_group FROM user_group_label WHERE label_group ilike '" + userGroupName + "'";
                    resultSet = stmt.executeQuery(query);
                    if (resultSet.next()) {
                        existe = true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }

    /**
     * pemret de mettre à jour le role d'un utilisateur et quelques préférences
     * module de modification de l'utilisateur
     *
     * @param conn
     * @param idUser // * @param idRole
     * @param isIsActive
     * @param isIsAlertMail // * @param isSuperAdmin
     * @return
     */
    public boolean updateUser(Connection conn,
            int idUser,
            //                    int idRole,
            boolean isIsActive,
            boolean isIsAlertMail
    //                  , boolean isSuperAdmin
    ) {
        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE users set alertmail = " + isIsAlertMail
                            + ", active = " + isIsActive
                            + //  ", issuperadmin = " + isSuperAdmin +
                            " WHERE id_user = " + idUser;
                    stmt.executeUpdate(query);
//                    if(!isSuperAdmin) {
//                        query = "UPDATE user_role_group set id_role = " + idRole +
//                                " WHERE id_user = " + idUser;
//                        stmt.executeUpdate(query);
//                    }                  
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }
    
    /**
     * permet de mettre à jour les alertes et activer/désactiver l'utilisateur e
     *
     * @param ds
     * @param idUser 
     * @param isIsActive
     * @param isIsAlertMail
     * @return
     */
    public boolean updateUser(HikariDataSource ds,
            int idUser,
            boolean isIsActive,
            boolean isIsAlertMail) {
        Statement stmt;
        boolean status = false;
        Connection conn;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE users set alertmail = " + isIsAlertMail
                            + ", active = " + isIsActive
                            + " WHERE id_user = " + idUser;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }    

    public boolean setIsSuperAdmin(Connection conn,
            int idUser, boolean isSuperAdmin) {
        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE users set "
                            + " issuperadmin = " + isSuperAdmin
                            + " WHERE id_user = " + idUser;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
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
        Statement stmt;
        boolean status = false;

        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from user_role_group where"
                            + " id_user =" + idUser;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException sqle) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return status;
    }

    /**
     * cette fonction permet de retourner les utilisateurs avec role SuperAdmin
     *
     *
     * @param ds
     * @return
     */
    public ArrayList<NodeUserRole> getListOfSuperAdmin(
            HikariDataSource ds) {

        ArrayList<NodeUserRole> nodeUserRoles = null;

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + "  users.id_user,"
                            + "  users.username,"
                            + " users.active"
                            + " FROM "
                            + "  users"
                            + " WHERE"
                            + "  users.issuperadmin = true"
                            + " ORDER BY"
                            + "  LOWER(users.username)";
                    resultSet = stmt.executeQuery(query);
                    nodeUserRoles = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeUserRole nodeUserRole = new NodeUserRole();
                        nodeUserRole.setIdUser(resultSet.getInt("id_user"));
                        nodeUserRole.setUserName(resultSet.getString("username"));
                        nodeUserRole.setIdRole(1);
                        nodeUserRole.setRoleName("SuperAdmin");
                        nodeUserRole.setIsActive(resultSet.getBoolean("active"));
                        nodeUserRoles.add(nodeUserRole);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoles;
    }

    /**
     * pemret de modifier le role de l'utilisateur sur un groupe
     *
     * @param ds
     * @param idUser
     * @param idRole
     * @param idGroup
     * @return
     */
    public boolean updateUserRoleOnGroup(HikariDataSource ds,
            int idUser, int idRole, int idGroup) {
        boolean status = false;
        boolean isSuperAdmin = false;

        if (idRole == 1) {
            isSuperAdmin = true;
        }

        try {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            /// si le role devient SuperAdmin, alors on supprime les roles sur les groupes
            if (isSuperAdmin) {
                if (!deleteRoleOnGroup(conn, idUser, idGroup)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
            } else {
                if (!updateUserRoleOnGroupRollBack(conn,
                        idUser, idRole, idGroup)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
            }
            if (!setIsSuperAdmin(conn, idUser, isSuperAdmin)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            status = true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    private boolean updateUserRoleOnGroupRollBack(Connection conn,
            int idUser, int idRole, int idGroup) {
        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE user_role_group set id_role = "
                            + idRole
                            + " WHERE id_user = " + idUser
                            + " and id_group = " + idGroup;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de modifier le role de l'utilisateur sur un groupe si
     * l'utilisateur change de role (SuperAdmin -> en Admin, on met à jour son
     * status dans la table Users (isSuperadmin = false)
     *
     * @param ds
     * @param idUser
     * @param idRole
     * @param idGroup
     * @return
     */
    public boolean addUserRoleOnGroup(HikariDataSource ds,
            int idUser, int idRole, int idGroup) {
        boolean status = false;
        boolean isSuperAdmin = false;

        if (idRole == 1) {
            isSuperAdmin = true;
        }
        try {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            /// si le role devient SuperAdmin, alors on supprime les roles sur les groupes
            if (isSuperAdmin) {
                if (!deleteRoleOnGroup(conn, idUser, idGroup)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
            } else {
                if (!addUserRoleOnGroupRollBack(conn,
                        idUser, idRole, idGroup)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
            }
            if (!setIsSuperAdmin(conn, idUser, isSuperAdmin)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            status = true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    private boolean addUserRoleOnGroupRollBack(Connection conn,
            int idUser, int idRole, int idGroup) {
        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "insert into user_role_group (id_user, id_role, id_group)"
                            + " values("
                            + idUser + ","
                            + idRole + ","
                            + idGroup + ")";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * Permet de passer un thésaurus d'un groupe à un autre
     *
     * @param ds
     * @param idTheso
     * @param oldGroup
     * @param newGroup
     * @return #MR
     */
    public boolean moveThesoToGroup(HikariDataSource ds,
            String idTheso, int oldGroup, int newGroup) {
        boolean status = false;
        try {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            if (!deleteThesoFromGroup(conn, idTheso/*, oldGroup*/)) {
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
            conn.close();
            status = true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    private boolean moveThesoToGroupRollBack(Connection conn,
            String idTheso, int newGroup) {
        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "insert into user_group_thesaurus(id_group, id_thesaurus)"
                            + " values('"
                            + newGroup + "',"
                            + "'" + idTheso + "')";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de supprimer l'appartenance d'un thesaurus à un groupe/projet
     *
     * @param conn
     * @param idTheso
     * @return #MR
     */
    public boolean deleteThesoFromGroup(Connection conn,
            String idTheso) {
        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from user_group_thesaurus where"
                            + " id_thesaurus ='" + idTheso + "'";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet d'ajouter un thesaurus à un groupe/projet
     *
     * @param conn
     * @param idTheso
     * @param idGroup
     * @return #MR
     */
    public boolean addThesoToGroup(Connection conn,
            String idTheso, int idGroup) {
        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "insert into user_group_thesaurus"
                            + " (id_group, id_thesaurus) values ("
                            + idGroup + ",'" + idTheso + "')";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de supprimer cet utilisateur
     *
     * @param ds
     * @param idUser
     * @return
     */
    public boolean deleteUser(HikariDataSource ds, int idUser) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from users where"
                            + " id_user =" + idUser;
                    stmt.executeUpdate(query);
                    query = "delete from user_role_group where"
                            + " id_user = " + idUser;
                    stmt.executeUpdate(query);
                    query = "update users_historique "
                            + " set delete = '" + new ToolsHelper().getDate()
                            + " ' where id_user = '" + idUser + "'";
                    stmt.executeUpdate(query);
                    status = true;
                    conn.commit();
                    conn.close();
                } finally {
                    stmt.close();
                }
            } finally {
                if (status == false) {
                    conn.rollback();
                }
                conn.close();
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
     * @param ds
     * @param idUser
     * @return #MR
     */
    public ArrayList<NodeUserRoleGroup> getUserRoleGroup(HikariDataSource ds, int idUser) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeUserRoleGroup> nodeUserRoleGroups = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + "  user_role_group.id_role,"
                            + "  roles.name,"
                            + "  user_role_group.id_group,"
                            + "  user_group_label.label_group"
                            + " FROM user_role_group, roles, user_group_label"
                            + " WHERE"
                            + "  user_role_group.id_role = roles.id AND"
                            + "  user_group_label.id_group = user_role_group.id_group AND"
                            + "  user_role_group.id_user =" + idUser;
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeUserRoleGroup nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id_role"));
                        nodeUserRoleGroup.setRoleName(resultSet.getString("name"));
                        nodeUserRoleGroup.setIdGroup(resultSet.getInt("id_group"));
                        nodeUserRoleGroup.setGroupName(resultSet.getString("label_group"));
                        //   nodeUserRoleGroup.setNodeUserGroupThesauruses(getUserThesaurusOfGroup(ds, resultSet.getInt("id_group")));

                        if (resultSet.getInt("id_role") == 2) {
                            nodeUserRoleGroup.setIsAdmin(true);
                        }
                        if (resultSet.getInt("id_role") == 3) {
                            nodeUserRoleGroup.setIsManager(true);
                        }
                        if (resultSet.getInt("id_role") == 4) {
                            nodeUserRoleGroup.setIsContributor(true);
                        }

                        nodeUserRoleGroups.add(nodeUserRoleGroup);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoleGroups;
    }

    /**
     * permet de retourner le groupe à lequel le thesaurus appartient
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public int getGroupOfThisTheso(HikariDataSource ds, String idTheso) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        int idGroup = -1;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT user_group_thesaurus.id_group"
                            + " FROM user_group_thesaurus"
                            + " WHERE"
                            + " user_group_thesaurus.id_thesaurus = '" + idTheso + "'";
                    resultSet = stmt.executeQuery(query);
                    if (resultSet.next()) {
                        idGroup = resultSet.getInt("id_group");
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @return
     */
    public ArrayList<NodeUserRole> getUsersWithoutGroup(HikariDataSource ds) {

        ArrayList<NodeUserRole> listUser = new ArrayList<>();

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT users.id_user, users.username, users.active"
                            + " FROM users where users.issuperadmin!=true and users.id_user not in "
                            + "(select distinct id_user from user_role_group order by id_user)"
                            + " ORDER BY LOWER(users.username)";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeUserRole nodeUserRole = new NodeUserRole();
                        nodeUserRole.setIdUser(resultSet.getInt("id_user"));
                        nodeUserRole.setUserName(resultSet.getString("username"));
                        nodeUserRole.setIsActive(resultSet.getBoolean("active"));
                        nodeUserRole.setIdRole(-1);
                        nodeUserRole.setRoleName("");
                        listUser.add(nodeUserRole);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listUser;
    }

    /**
     * cette fonction permet de retourner un objet vide de type
     * NodeUserRoleGroup pour un utilisateur qui n'a aucun role encore sur aucun
     * groupe
     *
     *
     * @param ds
     * @return
     */
    public NodeUserRoleGroup getUserRoleWithoutGroup(
            HikariDataSource ds) {

        //   NodeUserRoleGroup nodeUserRoleGroup = null;
        NodeUserRoleGroup nodeUserRoleGroup = new NodeUserRoleGroup();
        nodeUserRoleGroup.setIdRole(-1);
        nodeUserRoleGroup.setRoleName("");
        nodeUserRoleGroup.setGroupName("");
        nodeUserRoleGroup.setIdGroup(-1);
        nodeUserRoleGroup.setIsAdmin(false);
        nodeUserRoleGroup.setIsManager(false);
        nodeUserRoleGroup.setIsContributor(false);

        /*
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT \n" +
                                    "  roles.id, \n" +
                                    "  roles.name, \n" +
                                    "  user_group_label.label_group, \n" +
                                    "  user_group_label.id_group\n" +
                                    " FROM \n" +
                                    "  user_role_group, \n" +
                                    "  roles, \n" +
                                    "  user_group_label\n" +
                                    " WHERE \n" +
                                    "  user_role_group.id_role = roles.id AND\n" +
                                    "  user_group_label.id_group = user_role_group.id_group AND\n" +
                                    "  user_role_group.id_user = " + idUser;
                    resultSet = stmt.executeQuery(query);

                    if(resultSet.next()) {
                        nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id"));
                        nodeUserRoleGroup.setRoleName(resultSet.getString("name"));
                        nodeUserRoleGroup.setGroupName(resultSet.getString("label_group"));                        
                        nodeUserRoleGroup.setIdGroup(resultSet.getInt("id_group"));
                        if(resultSet.getInt("id") == 2) 
                            nodeUserRoleGroup.setIsAdmin(true);
                        if(resultSet.getInt("id") == 3) 
                            nodeUserRoleGroup.setIsManager(true);
                        if(resultSet.getInt("id") == 4) 
                            nodeUserRoleGroup.setIsContributor(true);                        
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper2.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return nodeUserRoleGroup;
    }

    /**
     * cette fonction permet de retourner la liste des utilisateurs pour un
     * groupe avec un role égale ou inférieur au role de l'utilisateur en cours
     *
     *
     * @param ds
     * @param idGroup
     * @param idRole
     * @return
     */
    public ArrayList<NodeUserRole> getUsersRolesByGroup(HikariDataSource ds,
            int idGroup, int idRole) {

        ArrayList<NodeUserRole> listUser = new ArrayList<>();

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT"
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
                            + " ORDER BY LOWER(users.username)";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeUserRole nodeUserRole = new NodeUserRole();
                        nodeUserRole.setIdUser(resultSet.getInt("id_user"));
                        nodeUserRole.setUserName(resultSet.getString("username"));
                        nodeUserRole.setIsActive(resultSet.getBoolean("active"));
                        nodeUserRole.setIdRole(resultSet.getInt("id"));
                        nodeUserRole.setRoleName(resultSet.getString("name"));
                        listUser.add(nodeUserRole);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listUser;
    }

    /**
     * cette fonction permet de retourner le role de l'utilisateur sur ce groupe
     *
     *
     * @param ds
     * @param idUser
     * @param idGroup
     * @return
     */
    public NodeUserRoleGroup getUserRoleOnThisGroup(
            HikariDataSource ds, int idUser, int idGroup) {

        NodeUserRoleGroup nodeUserRoleGroup = null;

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT \n"
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
                            + "  user_role_group.id_group = " + idGroup;
                    resultSet = stmt.executeQuery(query);

                    if (resultSet.next()) {
                        nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id"));
                        nodeUserRoleGroup.setRoleName(resultSet.getString("name"));
                        nodeUserRoleGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserRoleGroup.setIdGroup(resultSet.getInt("id_group"));
                        if (resultSet.getInt("id") == 2) {
                            nodeUserRoleGroup.setIsAdmin(true);
                        }
                        if (resultSet.getInt("id") == 3) {
                            nodeUserRoleGroup.setIsManager(true);
                        }
                        if (resultSet.getInt("id") == 4) {
                            nodeUserRoleGroup.setIsContributor(true);
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoleGroup;
    }

    /**
     * cette fonction permet de retourner le role du superAdmin
     *
     *
     * @param ds
     * @return
     */
    public NodeUserRoleGroup getUserRoleForSuperAdmin(
            HikariDataSource ds) {

        NodeUserRoleGroup nodeUserRoleGroup = null;

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + "  roles.id, "
                            + "  roles.name "
                            + " FROM "
                            + "  public.roles "
                            + " WHERE "
                            + "  roles.id = 1";
                    resultSet = stmt.executeQuery(query);

                    if (resultSet.next()) {
                        nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id"));
                        nodeUserRoleGroup.setRoleName(resultSet.getString("name"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoleGroup;
    }

    /**
     * permet de supprimer le role d'un utilisateur sur ce groupe
     *
     * @param conn
     * @param idUser
     * @param idGroup
     * @return
     */
    public boolean deleteRoleOnGroup(Connection conn,
            int idUser, int idGroup) {
        Statement stmt;
        boolean status = false;

        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from user_role_group where"
                            + " id_user =" + idUser
                            + " and id_group = " + idGroup;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException sqle) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return status;
    }
    
    /**
     * permet de supprimer le role d'un utilisateur sur ce groupe
     *
     * @param ds
     * @param idUser
     * @param idGroup
     * @return
     */
    public boolean deleteRoleOnGroup(HikariDataSource ds,
            int idUser, int idGroup) {
        Statement stmt;
        boolean status = false;
        Connection conn;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from user_role_group where"
                            + " id_user =" + idUser
                            + " and id_group = " + idGroup;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return status;
    }    

    /**
     * permet de supprimer le Projet ou groupe
     *
     * @param ds
     * @param idGroup
     * @return
     */
    public boolean deleteProjectGroup(HikariDataSource ds,
            int idGroup) {
        Statement stmt;
        boolean status = false;
        Connection conn = null;
        try {
            conn = ds.getConnection();
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
                    stmt.close();
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
     *
     * @param ds
     * @param idUser
     * @param idGroup
     * @return
     */
    public NodeUserRoleGroup getUserRoleOnThisGroupForSuperAdmin(
            HikariDataSource ds, int idUser, int idGroup) {

        NodeUserRoleGroup nodeUserRoleGroup = null;

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT \n"
                            + "  roles.id, \n"
                            + "  roles.name, \n"
                            + "  user_group_label.id_group, \n"
                            + "  user_group_label.label_group\n"
                            + " FROM \n"
                            + "  public.roles, \n"
                            + "  public.user_group_label\n"
                            + " WHERE \n"
                            + "  roles.id = " + idUser + " AND \n"
                            + "  user_group_label.id_group = " + idGroup;
                    resultSet = stmt.executeQuery(query);

                    if (resultSet.next()) {
                        nodeUserRoleGroup = new NodeUserRoleGroup();
                        nodeUserRoleGroup.setIdRole(resultSet.getInt("id"));
                        nodeUserRoleGroup.setRoleName(resultSet.getString("name"));
                        nodeUserRoleGroup.setGroupName(resultSet.getString("label_group"));
                        nodeUserRoleGroup.setIdGroup(resultSet.getInt("id_group"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeUserRoleGroup;
    }

    ///////////////////////////
    /// à vérifier
    //////////////////////////
    /**
     *
     * permet de retourner la liste des admins pour un thésaurus pour leur
     * envoyer des alertes candidats
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> getAdminMail(HikariDataSource ds,
            String idThesaurus) {
        ArrayList<String> lesMails = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + "  users.mail"
                            + " FROM "
                            + "  users, "
                            + "  user_role_group, "
                            + "  user_group_thesaurus"
                            + " WHERE "
                            + "  user_role_group.id_user = users.id_user AND"
                            + "  user_role_group.id_group = user_group_thesaurus.id_group AND"
                            + "  user_role_group.id_role = 2 AND "
                            + "  users.active = true AND "
                            + "  users.alertmail = true AND"
                            + "  user_group_thesaurus.id_thesaurus = '" + idThesaurus + "'";
                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        lesMails.add(resultSet.getString("mail"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lesMails;
    }

    /**
     * getMailUserForCandidat #JM méthode pour récupérer une arrayList de Mail
     * des utilisateurs qui ont proposé un candidat et qui a été inséré validé
     * ou refusé, entre deux dates debut et fin
     *
     * @param ds
     * @param id_thesaurus
     * @param debut
     * @param fin
     * @return
     */
    public ArrayList<String> getMailUserForCandidat(HikariDataSource ds, String id_thesaurus, Date debut, Date fin) {
        Connection conn;
        PreparedStatement stmt;
        ArrayList<String> mail = new ArrayList<>();
        ResultSet rs;
        try {
            conn = ds.getConnection();
            try {
                String sql = "SELECT mail FROM users INNER JOIN proposition ON proposition.id_user=users.id_user"
                        + " INNER JOIN  concept_candidat ON proposition.id_concept=concept_candidat.id_concept"
                        + " INNER JOIN concept_term_candidat ON concept_candidat.id_concept=concept_term_candidat.id_concept"
                        + " INNER JOIN term_candidat ON concept_term_candidat.id_term=term_candidat.id_term"
                        + " WHERE concept_candidat.id_thesaurus=?"
                        + " AND ( concept_candidat.status='i' OR concept_candidat.status='v' OR concept_candidat.status='r')"
                        + " AND ((concept_candidat.created BETWEEN ? AND ?)"
                        + " OR (concept_candidat.modified BETWEEN ? AND ?))";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, id_thesaurus);
                java.sql.Date d = new java.sql.Date(debut.getTime());
                java.sql.Date f = new java.sql.Date(fin.getTime());
                stmt.setDate(2, d);
                stmt.setDate(3, f);
                stmt.setDate(4, d);
                stmt.setDate(5, f);
                try {
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        mail.add(rs.getString("mail"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();

            }
        } catch (SQLException e) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, "error while selecting mail from users in getMailUserForCandidat id thesaurus =" + id_thesaurus, e);
        }
        return mail;
    }

    public boolean isUserMailExist(HikariDataSource ds, String mail) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT mail FROM users WHERE mail ilike '" + mail + "'";
                    resultSet = stmt.executeQuery(query);
                    resultSet.next();
                    if (resultSet.getRow() != 0) {
                        return true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * permet de retourner le pseudo de l'utilisateur d'après son Email
     *
     * @param ds
     * @param email
     * @return
     */
    public String getNameUser(HikariDataSource ds, String email) {

        String name = "";
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT username from users "
                            + " WHERE mail ilike '" + email + "'";

                    resultSet = stmt.executeQuery(query);
                    if (resultSet.next()) {
                        name = resultSet.getString("username");
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return name;
    }

    public boolean isneededpass(HikariDataSource ds, int id) {
        Statement stmt;
        boolean need = false;
        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Select passtomodify from users where id_user = '" + id + "'";
                    ResultSet rs = stmt.executeQuery(query);
                    if (rs.next()) {
                        need = rs.getBoolean("passtomodify");
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return need;
    }

    /**
     * Cette fonction permet de récupérer l'Id de l'utilisateur d'après son
     * Login et son passe
     *
     * @param ds
     * @param login
     * @param pwd
     * @return
     */
    public boolean isUserExist(HikariDataSource ds, String login, String pwd) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_user FROM users WHERE username ilike '" + login + "' AND password='" + pwd + "'";
                    resultSet = stmt.executeQuery(query);
                    //resultSet.first();
                    //resultSet.next();
                    if (resultSet.next()) {
                        existe = true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }

    public boolean updatePwd(HikariDataSource ds, int idUser, String newPwd) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE users set password = '" + newPwd
                            + "' WHERE id_user = " + idUser;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    public boolean updatePseudo(HikariDataSource ds, int idUser, String pseudo) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE users set username = '" + pseudo
                            + "' WHERE id_user = " + idUser;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    public String getNameUser(HikariDataSource ds, int iden) {

        String name = "";
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT username from users "
                            + " WHERE id_user =" + iden;

                    resultSet = stmt.executeQuery(query);
                    if (resultSet.next()) {
                        name = resultSet.getString("username");
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @param newValue
     * @param idProject
     * @return
     */
    public boolean updateProject(HikariDataSource ds,
            String newValue, int idProject) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE user_group_label set label_group = '" + newValue
                            + "' WHERE id_group = " + idProject;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    public boolean updateMail(HikariDataSource ds, int idUser, String newMail) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE users set mail = '" + newMail
                            + "' WHERE id_user = " + idUser;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de mette à jour le status des alertes mail pour l'utilisateur
     *
     * @param conn
     * @param idUser
     * @param alertMail
     * @return #MR
     */
    public boolean setAlertMailForUser(Connection conn, int idUser, boolean alertMail) {
        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE users set alertmail = " + alertMail
                            + " WHERE id_user = " + idUser;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                // conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }
    
    /**
     * permet de mette à jour le status des alertes mail pour l'utilisateur
     *
     * @param ds
     * @param idUser
     * @param alertMail
     * @return #MR
     */
    public boolean setAlertMailForUser(HikariDataSource ds, int idUser, boolean alertMail) {
        Statement stmt;
        boolean status = false;
        Connection conn;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE users set alertmail = " + alertMail
                            + " WHERE id_user = " + idUser;
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }    

    /**
     * Cette fonction permet de savoir si le Pseudo existe
     *
     * @param ds
     * @param pseudo
     * @return
     */
    public boolean isPseudoExist(HikariDataSource ds, String pseudo) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_user FROM users WHERE username='" + pseudo + "'";
                    resultSet = stmt.executeQuery(query);
                    //resultSet.first();
                    //resultSet.next();
                    if (resultSet.next()) {
                        existe = true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }

    public boolean isMailExist(HikariDataSource ds, String mail) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean status = false;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT mail FROM users WHERE mail ilike '" + mail + "'";
                    resultSet = stmt.executeQuery(query);
                    resultSet.next();
                    if (resultSet.getRow() != 0) {
                        status = true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return status;
    }

    /**
     * Permet de savoir si l'utilisateur a un droit d'admin sur le groupe
     *
     * @param ds
     * @param idUser
     * @param idGroup
     * @return
     */
    public boolean isAdminOnThisGroup(HikariDataSource ds,
            int idUser, int idGroup) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + "  user_role_group.id_user"
                            + " FROM "
                            + "  user_role_group"
                            + " WHERE "
                            + "  user_role_group.id_group = " + idGroup
                            + " AND user_role_group.id_user = " + idUser
                            + " AND user_role_group.id_role < 3";
                    resultSet = stmt.executeQuery(query);
                    resultSet.next();
                    if (resultSet.getRow() != 0) {
                        return true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @param idRoleFrom
     * @return
     */
    public ArrayList<Map.Entry<String, String>> getAuthorizedRoles(HikariDataSource ds,
            int idRoleFrom) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        Map map = new HashMap();

        try {
            // Get connection from pool
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id, name from roles "
                            + " where id >= " + idRoleFrom;

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            map.put(resultSet.getString("id"), resultSet.getString("name"));
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
     * @param ds
     * @param myIdRole
     * @return
     */
    public ArrayList<NodeIdValue> getMyAuthorizedRoles(HikariDataSource ds,
            int myIdRole) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();

            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id, name from roles "
                            + " where id >= " + myIdRole;

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id"));
                        nodeIdValue.setValue(resultSet.getString("name"));
                        nodeIdValues.add(nodeIdValue);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIdValues;
    }    
    
    /**
     * Cette fonction permet de retourner la liste des projets dont l'utilisateur est admin ou SuperAdmin 
     *
     * @param ds
     * @param idUser
     * @return
     */
    public ArrayList<NodeIdValue> getMyAuthorizedProjects(
            HikariDataSource ds,
            int idUser) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select user_group_label.id_group, user_group_label.label_group" +
                        " from user_role_group, user_group_label" +
                        " where " +
                        " user_role_group.id_group = user_group_label.id_group" +
                        " and" +
                        " id_role <= 2" +
                        " and" +
                        " id_user = " + idUser;
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id"));
                        nodeIdValue.setId(resultSet.getString("name"));
                        nodeIdValues.add(nodeIdValue);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
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
}
