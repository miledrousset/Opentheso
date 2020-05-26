package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.bean.language.LanguageBean;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import fr.cnrs.opentheso.bdd.helper.BaseDeDoneesHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroupThesaurus;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroupUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRole;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bdd.tools.MD5Password;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.PrimeFaces;

@Named(value = "ManagerGroupsUsers")
@SessionScoped

public class ManagerGroupsUsers implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private LanguageBean languageBean;
    @Inject
    private Connect connect;
    @Inject
    private CurrentUser currentUser;    

    private String pseudo = null;
    private String pseudoEdit;
    private String pwd = null;

    private boolean isLogged = false;

    private NodeUser userEdit;
    private int idEdit;
    private String pseudoAdded;
    private String mailAdded;

    private String pwdAdded1 = "";
    private String pwdAdded2 = "";
    private String pwdAdded3 = "";
    private boolean alertmail = false;

    private String groupAdded;
    private int userSelected;
    private int roleAdded;

    
    //pref
    private String langSourceEdit;

    private String versionOfOpentheso;

    private boolean isActive = false;

    private String password5;

    private boolean editPassed = false;

    private NodeUserGroup nodeUserGroupEdit;
    
    
    
    
    
    private String selectedGroup;
    private String selectedGroupName;

    private Map<String, String> listeGroupsOfUser;

    private Map<String, String> listeOfUsersWithoutSuperadmin;

    private ArrayList<Entry<String, String>> authorizedRoles;

    private ArrayList<NodeUserRole> listeUsersOfGroup; // la liste des utilisateur du groupe
    private ArrayList<NodeUserRole> listeUserSuperAdmin;

    private ArrayList<NodeUserGroupThesaurus> listeAllGroupTheso;

    // pour le superAdmin pour gérer la multi apparetenance des utilisateurs aux groupes
    private ArrayList<NodeUserGroupUser> listeAllGroupUser;

    // pour récupérer tous les projets pour la gestion
    private ArrayList<NodeUserGroup> nodeAllProject;

    private NodeUserGroupThesaurus nodeUserGroupThesaurusSelected; // le theso et le group selectionnés pour déplacement à un autre groupe

    private Map<String, String> listeThesoOfGroup;
    private NodeUserRoleGroup nodeUserRoleOnThisGroup;
    private NodeUserRoleGroup nodeUserRoleSuperAdmin;

    private NodeUserRoleGroup nodeUserRoleOnThisGroupEdit; //pour modifier un utilisateur 
    private NodeUserRoleGroup nodeUserRoleSuperAdminForEdit; //pour modifier un utilisateur

    private ArrayList<NodeUserRoleGroup> nodeUserRoleGroups;

    // vues pour afficher User/Group/SuperAdmin 
    private boolean vueListUser = false;
    private boolean vueListTheso = false;
    private boolean vueListSuperAdmin = false;
    private boolean vueManageProject = false;
    private boolean vueMoveThesoIntoGroupes = false;
    private boolean vueMoveUserIntoGroupes = false;





    @PostConstruct
    public void initUser() {
    }

    //// restructuration de la classe User le 05/04/2018 //////    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////// Nouvelles fontions //////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////   


    // déprécié par #MR
//    public String connect() {
//        int idUser;
//        UserHelper userHelper = new UserHelper();
//        idUser = userHelper.getIdUser(connect.getPoolConnexion(), pseudo, MD5Password.getEncodedPassword(pwd));
//        if (idUser == -1) {
//            // utilisateur ou mot de passe n'existent pas
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error1")));
//            return "";
//        }
//
//        // on récupère le compte de l'utilisatreur 
//        nodeUser = userHelper.getUser(connect.getPoolConnexion(), idUser);
//        if (nodeUser == null) {
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error1")));
//            return "";
//        }
//        isLogged = true;
//        if (nodeUser.isPasstomodify()) {
//            return "changePass.xhtml?faces-redirect=true";// nouvelle pass web pour changer le motpasstemp
//        }
//        pseudo = "";
//        pwd = "";
//
//        // initialisation des informations et des droits pour l'utilisateur 
//        setInfos();
//
//        return "index.xhtml?faces-redirect=true";
//    }

    public String getOpenthesoVersionFromWar() {
        return FacesContext.getCurrentInstance().getExternalContext().getInitParameterMap().get("version");
    }

    /**
     * Permet de mettre à jour toutes les informations concernant un user #MR
     */
    private void setInfos() {

//        addAuthorizedThesoToHM();
        listeUsersOfGroup = null;
        listeGroupsOfUser = null;
        nodeUserRoleOnThisGroup = null;
        listeThesoOfGroup = null;
        FacesContext context = FacesContext.getCurrentInstance();
        String version_Opentheso = context.getExternalContext().getInitParameter("version");
        versionOfOpentheso = new BaseDeDoneesHelper().getVersionOfOpentheso(connect.getPoolConnexion());
        new BaseDeDoneesHelper().updateVersionOpentheso(connect.getPoolConnexion(), version_Opentheso);
    }

    public void initUserEdit() {
        mailAdded = "";
        pwdAdded1 = "";
        pwdAdded2 = "";
        pseudoAdded = "";
        roleAdded = -1;
        groupAdded = "";
        initAuthorizedRoles();
    }

    public void initProjectEdit() {
        groupAdded = "";
        editPassed = false;
        nodeAllProject = null;
        getAllProject();
        vueListUser = false;
        vueListTheso = false;
        vueListSuperAdmin = false;
        vueManageProject = true;
        vueMoveThesoIntoGroupes = false;
        vueMoveUserIntoGroupes = false;
    }

    public void initProjectRename(NodeUserGroup nodeUserGroup) {
        nodeUserGroupEdit = nodeUserGroup;
        initProjectEdit();
    }

    public void initPassword() {
        if (password5 == null) {
            return;
        }
        UserHelper userHelper2 = new UserHelper();
        if (!userHelper2.updatePwd(connect.getPoolConnexion(), userEdit.getIdUser(), MD5Password.getEncodedPassword(password5))) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("BDD" + " :", "Error"));
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("user.info1")));
    }

    public void setLists() {
        listeUsersOfGroup = null;
        listeThesoOfGroup = null;
    }

    /**
     * permet de récupérer la liste des groupes/projets d'un utilisateur #MR
     */
    private void getGroupsOfUser() {
        UserHelper userHelper = new UserHelper();
        if (currentUser.getNodeUser().isIsSuperAdmin()) {// l'utilisateur est superAdmin
            listeGroupsOfUser = userHelper.getAllGroups(connect.getPoolConnexion());
            return;
        }
        listeGroupsOfUser = userHelper.getGroupsOfUser(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
    }

    public void initVue() {
        vueListUser = false;
        vueListTheso = false;
        vueListSuperAdmin = false;
        vueManageProject = false;
        vueMoveThesoIntoGroupes = false;
        vueMoveUserIntoGroupes = false;
    }

    /**
     * retoure la liste d'utilisateurs par groupe
     *
     * #MR
     */
    public void getListUsersByGroup() {
        listUsersByGroup();
        vueListUser = true;
        vueListTheso = false;
        vueListSuperAdmin = false;
        vueManageProject = false;
        vueMoveThesoIntoGroupes = false;
        vueMoveUserIntoGroupes = false;
    }

    /**
     * permet de récupérer la liste des utilisateurs suivants les options
     * choisies
     */
    private void listUsersByGroup() {
        if (selectedGroup == null) {
            return;
        }
        UserHelper userHelper = new UserHelper();
        // récupération des utilisateurs sans groupe
        if (selectedGroup.isEmpty()) {
            listeUsersOfGroup = userHelper.getUsersWithoutGroup(connect.getPoolConnexion());
        } else {
            int idGroup = Integer.parseInt(selectedGroup);
            setUserRoleOnThisGroup();
            if (currentUser.getNodeUser().isIsSuperAdmin()) {// l'utilisateur est superAdmin
                listeUsersOfGroup = userHelper.getUsersRolesByGroup(connect.getPoolConnexion(),
                        idGroup, nodeUserRoleSuperAdmin.getIdRole());
            } else {
                if (nodeUserRoleOnThisGroup != null) {
                    listeUsersOfGroup = userHelper.getUsersRolesByGroup(connect.getPoolConnexion(),
                            idGroup, nodeUserRoleOnThisGroup.getIdRole());
                } else {
                    if (listeUsersOfGroup != null) {
                        listeUsersOfGroup.clear(); //cas où on supprime l'utilisateur en cours
                    }
                }
            }
        }
    }

    /**
     * retoure la liste d'utilisateurs par groupe on affiche aussi la vue
     * correspondante #MR
     */
    public void getListSuperAdmin() {
        listSuperAdmin();
        vueListUser = false;
        vueListTheso = false;
        vueMoveThesoIntoGroupes = false;
        vueMoveUserIntoGroupes = false;
        vueManageProject = false;
        vueListSuperAdmin = true;
    }

    private void listSuperAdmin() {
        UserHelper userHelper = new UserHelper();
        listeUserSuperAdmin = userHelper.getListOfSuperAdmin(connect.getPoolConnexion());
    }

    /**
     * permet de retourner la liste des thésaurus et les groupes correspondants
     * on affiche aussi la vue correspondante
     */
    public void getListAllGroupTheso() {
        listAllGroupTheso();
        vueListUser = false;
        vueListTheso = false;
        vueMoveThesoIntoGroupes = true;
        vueMoveUserIntoGroupes = false;
        vueListSuperAdmin = false;
        vueManageProject = false;
    }

    private void listAllGroupTheso() {
        UserHelper userHelper = new UserHelper();
        listeAllGroupTheso = userHelper.getAllGroupTheso(connect.getPoolConnexion(), connect.getWorkLanguage());

        // les thésos sans groupe 
        ArrayList<NodeUserGroupThesaurus> listeAllGroupTheso_wtihoutGroup;
        listeAllGroupTheso_wtihoutGroup = userHelper.getAllThesoWithoutGroup(
                connect.getPoolConnexion(), connect.getWorkLanguage());
        if (listeAllGroupTheso_wtihoutGroup != null) {
            if (!listeAllGroupTheso_wtihoutGroup.isEmpty()) {
                listeAllGroupTheso.addAll(listeAllGroupTheso_wtihoutGroup);
            }
        }
    }

    /**
     * permet de retourner la liste des utilisateurs avec les groupes
     * correspondants on affiche aussi la vue correspondante
     */
    public void getListAllGroupUser() {
        listAllGroupUser();
        vueListUser = false;
        vueListTheso = false;
        vueMoveThesoIntoGroupes = false;
        vueListSuperAdmin = false;
        vueManageProject = false;
        vueMoveUserIntoGroupes = true;
    }

    private void listAllGroupUser() {
        UserHelper userHelper = new UserHelper();
        listeAllGroupUser = userHelper.getAllGroupUser(connect.getPoolConnexion(), connect.getWorkLanguage());

        // les users sans groupe en ignorant les superAdmin 
        ArrayList<NodeUserGroupUser> nodeUserGroupUser_wtihoutGroup = userHelper.getAllGroupUserWithoutGroup(
                connect.getPoolConnexion(), connect.getWorkLanguage());
        if (nodeUserGroupUser_wtihoutGroup != null) {
            if (!nodeUserGroupUser_wtihoutGroup.isEmpty()) {
                listeAllGroupUser.addAll(nodeUserGroupUser_wtihoutGroup);
            }
        }
    }

    /**
     * setting du role de l'utilisateur sur le group séléctionné
     *
     * #MR
     *
     * @return
     */
    private void setUserRoleOnThisGroup() {
        if (selectedGroup == null) {
            return;
        }
        if (selectedGroup.isEmpty()) {
            return;
        }
        int idGroup = Integer.parseInt(selectedGroup);
        UserHelper userHelper = new UserHelper();
        if (currentUser.getNodeUser().isIsSuperAdmin()) {// l'utilisateur est superAdmin
            nodeUserRoleSuperAdmin = userHelper.getUserRoleForSuperAdmin(
                    connect.getPoolConnexion());
            return;
        }
        nodeUserRoleOnThisGroup = userHelper.getUserRoleOnThisGroup(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser(), idGroup);
    }

    /**
     * retourne la liste des thésaurus par groupe
     */
    public void getListThesoByGroup() {
        listThesoByGroup();
        vueListUser = false;
        vueListTheso = true;
        vueListSuperAdmin = false;
        vueManageProject = false;
        vueMoveThesoIntoGroupes = false;
        vueMoveUserIntoGroupes = false;
    }

    private void listThesoByGroup() {
        if (selectedGroup == null) {
            return;
        }
        if (selectedGroup.isEmpty()) {
            return;
        }
        int idGroup = Integer.parseInt(selectedGroup);

        UserHelper userHelper = new UserHelper();
        if (currentUser.getNodeUser().isIsSuperAdmin()) {// l'utilisateur est superAdmin
            //      return selectAllUsers();
        }
        listeThesoOfGroup = userHelper.getThesaurusLabelsOfGroup(connect.getPoolConnexion(), idGroup,
                connect.getWorkLanguage());
        listeUsersOfGroup = null;
        setUserRoleOnThisGroup();
    }

    /**
     * permet de savoir si l'utilisateur est Admin sur ce Groupe / SuperAdmin
     *
     * @return
     */
    public boolean isAdminOnThisGroup() {
        if (currentUser.getNodeUser().isIsSuperAdmin()) {
            return true;
        }
        if (selectedGroup == null) {
            return false;
        }
        if (selectedGroup.isEmpty()) {
            return false;
        }
        int idGroup = Integer.parseInt(selectedGroup);
        UserHelper userHelper = new UserHelper();
        return userHelper.isAdminOnThisGroup(connect.getPoolConnexion(),
                currentUser.getNodeUser().getIdUser(), idGroup);
    }

    /**
     * permet de récupérer la liste des roles autorisés pour un utilisateur
     * c'est la liste des roles qu'il aura le droit d'attribuer aux nouveaux
     * utilisateurs
     */
    public void initAuthorizedRoles() {
        int idRoleFrom = 4;
        if (currentUser.getNodeUser().isIsSuperAdmin()) {
            idRoleFrom = 1; // l'utilisateur est SuperAdmin
        } else {
            if (nodeUserRoleOnThisGroup == null) {
                return;
            }
            if (nodeUserRoleOnThisGroup.isIsAdmin()) {
                idRoleFrom = 2; // l'utilisateur est Admin            
            }
            if (nodeUserRoleOnThisGroup.isIsManager()) {
                idRoleFrom = 3; // l'utilisateur est Manager            
            }
            if (nodeUserRoleOnThisGroup.isIsContributor()) {
                idRoleFrom = 4; // l'utilisateur est Contributeur / user       
            }
        }
        roleAdded = idRoleFrom;
        UserHelper userHelper = new UserHelper();
        this.authorizedRoles = userHelper.getAuthorizedRoles(connect.getPoolConnexion(),
                idRoleFrom);
    }

    public void initAllUsersNotSuperadmin() {
        UserHelper userHelper = new UserHelper();
        listeOfUsersWithoutSuperadmin = userHelper.getAllUsersNotSuperadmin(connect.getPoolConnexion());
        initAuthorizedRoles();
    }

    public void addNewRoleOnGroup() {
        UserHelper userHelper = new UserHelper();
        if (groupAdded == null || groupAdded.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    languageBean.getMsg("error") + " :", languageBean.getMsg("Groupe manquant")));
            return;
        }
        int idGroup = Integer.parseInt(groupAdded);
        if (!userHelper.addUserRoleOnGroup(
                connect.getPoolConnexion(),
                userSelected,
                roleAdded,
                idGroup)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
            return;
        }
        listUsersByGroup();
        listAllGroupUser();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("profile.ModifyRoleMessage")));
    }

    /**
     * permet de récupérer la liste de role/groupe pour un utilisateur
     *
     * @param idUser
     */
    public void selectUserGroup(int idUser) {
        UserHelper userHelper = new UserHelper();
        userEdit = userHelper.getUser(connect.getPoolConnexion(), idUser);
        nodeUserRoleGroups = userHelper.getUserRoleGroup(connect.getPoolConnexion(), idUser);
    }

    /**
     * permet de selectionner l'utilisateur dans la liste avec toutes les
     * informations nécessaires pour sa modification
     *
     * @param idUser
     * @param selectedGroup1
     */
    public void selectUser(int idUser, String selectedGroup1) {
        UserHelper userHelper = new UserHelper();
        userEdit = userHelper.getUser(connect.getPoolConnexion(), idUser);
        selectedGroup = selectedGroup1;
        if (userEdit.isIsSuperAdmin()) {
            // cas d'un SuperAdmin
            nodeUserRoleSuperAdmin = userHelper.getUserRoleForSuperAdmin(connect.getPoolConnexion());
            roleAdded = nodeUserRoleSuperAdmin.getIdRole();//1;
        } else {
            // cas d'un utilisateur avec aucun role/groupe
            if (selectedGroup == null || selectedGroup.isEmpty()) {
                nodeUserRoleOnThisGroupEdit = userHelper.getUserRoleWithoutGroup(connect.getPoolConnexion());
                roleAdded = nodeUserRoleOnThisGroupEdit.getIdRole();
            } else {
                // utilisateur normal
                nodeUserRoleOnThisGroupEdit = userHelper.getUserRoleOnThisGroup(connect.getPoolConnexion(), idUser, Integer.parseInt(selectedGroup));
                roleAdded = nodeUserRoleOnThisGroupEdit.getIdRole();
            }
        }
        initAuthorizedRoles();
    }

    /**
     * permet de selectionner le thésaurus à deplacer et le groupe actuel où il
     * appartenait
     *
     * @param nodeUserGroupThesaurus
     */
    public void selectThesoGroup(NodeUserGroupThesaurus nodeUserGroupThesaurus) {
        nodeUserGroupThesaurusSelected = nodeUserGroupThesaurus;
    }

    /**
     * permet de déplacer un thésaurus d'un groupe à un autre
     */
    public void moveThesoToGroup() {
        if (nodeUserGroupThesaurusSelected == null) {
            return;
        }
        if (groupAdded == null) {
            return;
        }
        if (groupAdded.isEmpty()) {
            return;
        }

        UserHelper userHelper = new UserHelper();
        if (!userHelper.moveThesoToGroup(connect.getPoolConnexion(),
                nodeUserGroupThesaurusSelected.getIdThesaurus(),
                nodeUserGroupThesaurusSelected.getIdGroup(),
                Integer.parseInt(groupAdded))) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
            return;
        }
        listAllGroupTheso();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("profile.moveThesoGroupMessage")));
    }

    /**
     * permet de créer un nouvel utilisateur avec le role et dans le groupe en
     * cours
     *
     * @param idGroup
     * @return
     */
    public boolean addUser(String idGroup) {
        UserHelper userHelper = new UserHelper();
        if (pwdAdded1 == null || pwdAdded1.equals("") || pwdAdded2 == null || pwdAdded2.equals("")
                || pseudoAdded == null || pseudoAdded.equals("") || mailAdded.equals("") || mailAdded == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error2")));
            return false;
        }
        if (!pwdAdded1.equals(pwdAdded2)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error3")));
            return false;
        }
        if (userHelper.isPseudoExist(connect.getPoolConnexion(), pseudoAdded)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error5")));
            return false;
        }
        if (userHelper.isMailExist(connect.getPoolConnexion(), mailAdded)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.info7")));
            return false;
        }
        int idUser;
        int idGroupTemp = -1;
        boolean isSuperAdmin = false;

        if (idGroup != null) {
            if (!idGroup.isEmpty()) {
                idGroupTemp = Integer.parseInt(idGroup);
            } else {
                idGroupTemp = -1;
            }
        }
        if (roleAdded == 1) {
            isSuperAdmin = true;
        }

        if (!userHelper.addUser(connect.getPoolConnexion(),
                pseudoAdded, mailAdded, MD5Password.getEncodedPassword(pwdAdded1),
                isSuperAdmin, alertmail)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
            return false;
        }
        idUser = userHelper.getIdUser(connect.getPoolConnexion(), pseudoAdded, MD5Password.getEncodedPassword(pwdAdded1));
        if (idUser == -1) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
            return false;
        }
        /// si idGroupTemp = -1 : création d'utilisateur sans groupe
        if (!isSuperAdmin && idGroupTemp != -1) {
            // on ajoute l'utilisateur au groupe séléctionné par l'utilisateur
            if (!userHelper.addUserRoleOnGroup(connect.getPoolConnexion(), idUser, roleAdded, idGroupTemp)) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
                return false;
            }
        }

        mailAdded = "";
        pwdAdded1 = "";
        pwdAdded2 = "";
        pseudoAdded = "";
        roleAdded = -1;
        groupAdded = "";

        listUsersByGroup();
        if (isSuperAdmin) {
            listSuperAdmin();
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("user.info3")));
        return true;
    }

    /**
     * permet de créer un groupe/projet
     *
     * @return
     */
    public boolean addUserGroup() {
        editPassed = false;
        if (!currentUser.getNodeUser().isIsSuperAdmin()) {
            return false;
        }
        if (groupAdded == null) {
            return false;
        }
        if (groupAdded.isEmpty()) {
            return false;
        }
        UserHelper userHelper = new UserHelper();
        if (userHelper.isUserGroupExist(connect.getPoolConnexion(), groupAdded)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", "Ce projet existe déjà"));
            return false;
        }
        if (!userHelper.createUserGroup(connect.getPoolConnexion(), groupAdded)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", "Erreur BDD"));
            return false;
        }
        editPassed = true;
        getGroupsOfUser();
        getAllProject();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("info") + " :", "Projet ajouté"));
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("idAddUserGroupDlg");
        }
        return true;
    }

    /**
     * permet d'ajouter un role pour l'utilisateur sur ce groupe
     *
     * @param idUser
     * @param idRole
     * @param idGroup
     * @return
     */
    public boolean addUserRoleOnGroup(int idUser, int idRole, int idGroup) {
        if (idRole > 1) {
            if (idGroup == -1 || idGroup == 0) {
                FacesContext.getCurrentInstance().
                        addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :",
                                languageBean.getMsg("profile.needGroupMessage")));
                return false;
            }
        }
        UserHelper userHelper = new UserHelper();
        if (idRole == 1) { // cas de changement en superAdmin
            //      userHelper.setIsSuperAdmin(conn, idUser, vueListSuperAdmin)
        }
        if (!userHelper.addUserRoleOnGroup(connect.getPoolConnexion(), idUser, idRole, idGroup)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
            return false;
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("profile.ModifyRoleMessage")));
        nodeUserRoleGroups = userHelper.getUserRoleGroup(connect.getPoolConnexion(), idUser);
        listUsersByGroup();
        listSuperAdmin();
        return true;
    }

    /**
     * permet de modifier le role d'un utilisateur sur un groupe
     *
     * @param idUser
     * @param idRole
     * @param idGroup
     */
    public void updateUserRoleOnGroup(int idUser, int idRole, int idGroup) {
        if (idGroup == -1 || idGroup == 0) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :",
                            languageBean.getMsg("profile.needGroupMessage")));
            return;
        }
        UserHelper userHelper = new UserHelper();
        if (!userHelper.updateUserRoleOnGroup(connect.getPoolConnexion(), idUser, idRole, idGroup)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
            return;
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("profile.ModifyRoleMessage")));
        nodeUserRoleGroups = userHelper.getUserRoleGroup(connect.getPoolConnexion(), idUser);
        listUsersByGroup();
        listAllGroupUser();
    }

    /**
     * permet de mettre à jour les alertes mail pour l'utilisateur en cours
     *
     * @return
     */
    public boolean updateAlertMail() {
        try {
            UserHelper userHelper = new UserHelper();
            Connection conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            if (!userHelper.setAlertMailForUser(connect.getPoolConnexion().getConnection(), currentUser.getNodeUser().getIdUser(), alertmail)) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("conf.alertMailMessage")));
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(ManagerGroupsUsers.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Permet de modifier le mot de passe de l'utilisateur
     */
    public void updatePassword() {
        if (pwdAdded1 == null || pwdAdded1.equals("") || pwdAdded2 == null || pwdAdded2.equals("") || pwdAdded3 == null || pwdAdded3.equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error2")));
        } else if (!pwdAdded2.equals(pwdAdded3)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error3")));
        } else if (!new UserHelper().isUserExist(connect.getPoolConnexion(), currentUser.getNodeUser().getName(), MD5Password.getEncodedPassword(pwdAdded1))) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error4")));
        } else {
            new UserHelper().updatePwd(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser(), MD5Password.getEncodedPassword(pwdAdded2));
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("user.info1")));
        }
        pwdAdded1 = "";
        pwdAdded2 = "";
        pwdAdded3 = "";
    }

    /**
     * permet de changer l'adress mail de l'utilisateur
     */
    public void updateMail() {
        if (mailAdded == null || mailAdded.equals("")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error6")));
        } else if (!mailAdded.contains("@") || mailAdded.lastIndexOf(".") < mailAdded.indexOf("@")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error7")));
        } else if (new UserHelper().isUserMailExist(connect.getPoolConnexion(), mailAdded)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error8")));
        } else {
            new UserHelper().updateMail(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser(), mailAdded);
            currentUser.getNodeUser().setMail(mailAdded);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("user.info5")));
        }
        mailAdded = "";
    }

    /**
     * permet de changer le Pseudo
     */
    public void updatePseudo() {
        if (pseudoEdit == null || pseudoEdit.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error6")));
        } else if (new UserHelper().isPseudoExist(connect.getPoolConnexion(), pseudoEdit)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("user.error5")));
        } else {
            new UserHelper().updatePseudo(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser(), pseudoEdit);
            currentUser.getNodeUser().setName(pseudoEdit);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("user.info5")));
        }
        pseudo = pseudoEdit;
        pseudoEdit = "";
    }

    /**
     * permet de mettre les préférences d'un utilisateur (alertes, activé ..
     */
    public void updateUser() {
        UserHelper userHelper = new UserHelper();
        //   boolean isSuperAdmin = false;
        try {
            Connection conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            /*        if (roleAdded == 1) {
                isSuperAdmin = true;
                // on supprime les roles sur les groupes
                if (!userHelper.deleteRolesOfUser(conn, userEdit.getIdUser())) {
                    conn.rollback();
                    conn.close();
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, langueBean.getMsg("error") + " :", langueBean.getMsg("error.BDD")));
                    return;
                }
            }*/
            if (!userHelper.updateUser(conn,
                    userEdit.getIdUser(),
                    userEdit.isIsActive(),
                    userEdit.isIsAlertMail())) {
                conn.rollback();
                conn.close();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
                return;
            }
            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ManagerGroupsUsers.class
                    .getName()).log(Level.SEVERE, null, ex);
            return;
        }
        roleAdded = -1;
        listUsersByGroup();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("user.info4")));
    }



    /**
     * permet de supprimer un utilisateur
     *
     * @param idUser
     */
    public void delUser(int idUser) {
        UserHelper userHelper = new UserHelper();
        if (!userHelper.deleteUser(connect.getPoolConnexion(), idUser)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
            return;
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("user.info2")));
        listUsersByGroup();
        if (currentUser.getNodeUser().isIsSuperAdmin()) {
            listSuperAdmin();
        }
    }

    /**
     * permet de supprimer le role de l'utilisateur sur ce groupe
     *
     * @param idUser
     * @param idGroup
     */
    public void delUserRoleOnGroup(int idUser, int idGroup) {
        UserHelper userHelper = new UserHelper();

        try {
            Connection conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            if (!userHelper.deleteRoleOnGroup(conn, idUser, idGroup)) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
                conn.rollback();
                conn.close();
                return;
            }
            conn.commit();
            conn.close();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("profile.deleteRoleMessage")));
            nodeUserRoleGroups = userHelper.getUserRoleGroup(connect.getPoolConnexion(), idUser);
            listUsersByGroup();
            listAllGroupUser();
        } catch (SQLException ex) {
            Logger.getLogger(ManagerGroupsUsers.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * initialisation des variables
     */
    public void reInit() {
        pwdAdded1 = "";
        pwdAdded2 = "";
        pwdAdded3 = "";
        pseudoAdded = "";
    }

    ////////////////////////////////////////////////////////////////////
    //////// fonctions pour les roles par thésaurus ////////////////////
    ////////////////////////////////////////////////////////////////////     
    /**
     * retourne le role de l'utilisateur sur le group séléctionné
     *
     * #MR
     *
     * @param idGroup
     * @return
     */
    public NodeUserRoleGroup getUserRoleOnThisGroup(int idGroup) {
        UserHelper userHelper = new UserHelper();
        if (currentUser.getNodeUser().isIsSuperAdmin()) {// l'utilisateur est superAdmin
            return userHelper.getUserRoleForSuperAdmin(
                    connect.getPoolConnexion());
        }
        if (idGroup == -1) {
            return null;
        }
        return userHelper.getUserRoleOnThisGroup(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser(), idGroup);
    }

    ////////////////////////////////////////////////////////////////////
    //////// fonctions pour la gestion des projets/groupes /////////////
    ////////////////////////////////////////////////////////////////////  
    /**
     * permet de renommer un groupe/projet
     *
     * @return
     */
    public boolean renameUserGroup() {
        editPassed = false;
        if (!currentUser.getNodeUser().isIsSuperAdmin()) {
            return false;
        }
        if (nodeUserGroupEdit == null) {
            return false;
        }
        if (nodeUserGroupEdit.getGroupName().isEmpty()) {
            return false;
        }
        UserHelper userHelper = new UserHelper();
        if (userHelper.isUserGroupExist(connect.getPoolConnexion(), nodeUserGroupEdit.getGroupName().trim())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", "Ce projet existe déjà"));
            return false;
        }
        if (!userHelper.updateProject(connect.getPoolConnexion(),
                nodeUserGroupEdit.getGroupName(),
                nodeUserGroupEdit.getIdGroup())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Modification refusée: ", nodeUserGroupEdit.getGroupName()));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Modification réussie: ", nodeUserGroupEdit.getGroupName()));
        }

        editPassed = true;
        getGroupsOfUser();
        getAllProject();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("idEditUserGroupDlg");
        }
        return true;
    }

    /**
     * permet de supprimer un Groupe/projet
     *
     * @param nodeUserGroup
     */
    public void deleteProject(NodeUserGroup nodeUserGroup) {
        UserHelper userHelper = new UserHelper();
        if (!userHelper.deleteProjectGroup(connect.getPoolConnexion(), nodeUserGroup.getIdGroup())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur :", nodeUserGroup.getGroupName()));
            return;
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Suppression :", nodeUserGroup.getGroupName()));
        getAllProject();
        listAllGroupUser();
        getGroupsOfUser();
    }

    /**
     * permet de récupérer tous les projets pour la gestion
     */
    private void getAllProject() {
        UserHelper userHelper = new UserHelper();
        nodeAllProject = userHelper.getAllProject(connect.getPoolConnexion());
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    //////// fin des nouvelles fontions ////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////       
    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }


    public boolean isLogged() {
        return isLogged;
    }

    public void setIsLogged(boolean isLogged) {
        this.isLogged = isLogged;
    }

    public String getPwdAdded1() {
        return pwdAdded1;
    }

    public void setPwdAdded1(String pwdAdded1) {
        this.pwdAdded1 = pwdAdded1;
    }

    public String getPwdAdded2() {
        return pwdAdded2;
    }

    public void setPwdAdded2(String pwdAdded2) {
        this.pwdAdded2 = pwdAdded2;
    }

    public String getPwdAdded3() {
        return pwdAdded3;
    }

    public void setPwdAdded3(String pwdAdded3) {
        this.pwdAdded3 = pwdAdded3;
    }

    public String getPseudoAdded() {
        return pseudoAdded;
    }

    public void setPseudoAdded(String pseudoAdded) {
        this.pseudoAdded = pseudoAdded;
    }

    public int getRoleAdded() {
        return roleAdded;
    }

    public void setRoleAdded(int roleAdded) {
        this.roleAdded = roleAdded;
    }

    public int getIdEdit() {
        return idEdit;
    }

    public void setIdEdit(int idEdit) {
        this.idEdit = idEdit;
    }

    public LanguageBean getLangueBean() {
        return languageBean;
    }

    public void setLangueBean(LanguageBean langueBean) {
        this.languageBean = langueBean;
    }

    public String getMailAdded() {
        return mailAdded;
    }

    public void setMailAdded(String mailAdded) {
        this.mailAdded = mailAdded;
    }

    public String getLangSourceEdit() {
        return langSourceEdit;
    }

    public void setLangSourceEdit(String langSourceEdit) {
        this.langSourceEdit = langSourceEdit;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getVersionOfOpentheso() {
        if (connect.getPoolConnexion() != null) {
            versionOfOpentheso = new BaseDeDoneesHelper().getVersionOfOpentheso(connect.getPoolConnexion());
        }
        return versionOfOpentheso;
    }

    public void setVersionOfOpentheso(String versionOfOpentheso) {
        this.versionOfOpentheso = versionOfOpentheso;
    }

    public NodeUser getUserEdit() {
        return userEdit;
    }

    public void setUserEdit(NodeUser userEdit) {
        this.userEdit = userEdit;
    }

    public String getPseudoEdit() {
        return pseudoEdit;
    }

    public void setPseudoEdit(String pseudoEdit) {
        this.pseudoEdit = pseudoEdit;
    }

    public boolean isAlertmail() {
        return alertmail;
    }

    public void setAlertmail(boolean alertmail) {
        this.alertmail = alertmail;
    }

    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(String selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public Map<String, String> getListeGroupsOfUser() {
        return listeGroupsOfUser;
    }

    /*
    public void setListeGroupsOfUser(Map<String, String> listeGroupsOfUser) {
        this.listeGroupsOfUser = listeGroupsOfUser;
    }
     */
    public ArrayList<NodeUserRole> getListeUser() {
        return listeUsersOfGroup;
    }

    public void setListeUser(ArrayList<NodeUserRole> listeUser) {
        this.listeUsersOfGroup = listeUser;
    }

    public Map<String, String> getListeThesoOfGroup() {
        return listeThesoOfGroup;
    }

    public void setListeThesoOfGroup(Map<String, String> listeThesoOfGroup) {
        this.listeThesoOfGroup = listeThesoOfGroup;
    }

    public NodeUserRoleGroup getNodeUserRoleOnThisGroup() {
        return nodeUserRoleOnThisGroup;
    }

    public void setNodeUserRoleOnThisGroup(NodeUserRoleGroup nodeUserRoleOnThisGroup) {
        this.nodeUserRoleOnThisGroup = nodeUserRoleOnThisGroup;
    }

    public String getGroupAdded() {
        return groupAdded;
    }

    public void setGroupAdded(String groupAdded) {
        this.groupAdded = groupAdded;
    }

    public NodeUserRoleGroup getNodeUserRoleOnThisGroupEdit() {
        return nodeUserRoleOnThisGroupEdit;
    }

    public void setNodeUserRoleOnThisGroupEdit(NodeUserRoleGroup nodeUserRoleOnThisGroupEdit) {
        this.nodeUserRoleOnThisGroupEdit = nodeUserRoleOnThisGroupEdit;
    }

    public ArrayList<NodeUserRole> getListeUserSuperAdmin() {
        return listeUserSuperAdmin;
    }

    public void setListeUserSuperAdmin(ArrayList<NodeUserRole> listeUserSuperAdmin) {
        this.listeUserSuperAdmin = listeUserSuperAdmin;
    }

    public boolean isVueListUser() {
        return vueListUser;
    }

    public boolean isVueListTheso() {
        return vueListTheso;
    }

    public boolean isVueListSuperAdmin() {
        return vueListSuperAdmin;
    }

    public boolean isVueManageProject() {
        return vueManageProject;
    }

    public void setVueManageProject(boolean vueManageProject) {
        this.vueManageProject = vueManageProject;
    }

    public NodeUserRoleGroup getNodeUserRoleSuperAdmin() {
        return nodeUserRoleSuperAdmin;
    }

    public void setNodeUserRoleSuperAdmin(NodeUserRoleGroup nodeUserRoleSuperAdmin) {
        this.nodeUserRoleSuperAdmin = nodeUserRoleSuperAdmin;
    }

    public NodeUserRoleGroup getNodeUserRoleSuperAdminForEdit() {
        return nodeUserRoleSuperAdminForEdit;
    }

    public void setNodeUserRoleSuperAdminForEdit(NodeUserRoleGroup nodeUserRoleSuperAdminForEdit) {
        this.nodeUserRoleSuperAdminForEdit = nodeUserRoleSuperAdminForEdit;
    }

    public ArrayList<NodeUserRoleGroup> getNodeUserRoleGroups() {
        return nodeUserRoleGroups;
    }

    public void setAuthorizedRoles(ArrayList<Entry<String, String>> authorizedRoles) {
        this.authorizedRoles = authorizedRoles;
    }

    public ArrayList<Entry<String, String>> getAuthorizedRoles() {
        return this.authorizedRoles;
    }

    public String getSelectedGroupName() {
        UserHelper userHelper = new UserHelper();
        if (selectedGroup != null) {
            if (!selectedGroup.isEmpty()) {
                selectedGroupName = userHelper.getGroupName(connect.getPoolConnexion(), Integer.parseInt(selectedGroup));
            } else {
                selectedGroupName = selectedGroup;
            }
        }
        return selectedGroupName;
    }

    public boolean isVueMoveThesoIntoGroupes() {
        return vueMoveThesoIntoGroupes;
    }

    public void setVueMoveThesoIntoGroupes(boolean vueMoveThesoIntoGroupes) {
        this.vueMoveThesoIntoGroupes = vueMoveThesoIntoGroupes;
    }

    public ArrayList<NodeUserGroupThesaurus> getListeAllGroupTheso() {
        return listeAllGroupTheso;
    }

    public void setListeAllGroupTheso(ArrayList<NodeUserGroupThesaurus> listeAllGroupTheso) {
        this.listeAllGroupTheso = listeAllGroupTheso;
    }

    public NodeUserGroupThesaurus getNodeUserGroupThesaurusSelected() {
        return nodeUserGroupThesaurusSelected;
    }

    public void setNodeUserGroupThesaurusSelected(NodeUserGroupThesaurus nodeUserGroupThesaurusSelected) {
        this.nodeUserGroupThesaurusSelected = nodeUserGroupThesaurusSelected;
    }

    public ArrayList<NodeUserGroupUser> getListeAllGroupUser() {
        return listeAllGroupUser;
    }

    public void setListeAllGroupUser(ArrayList<NodeUserGroupUser> listeAllGroupUser) {
        this.listeAllGroupUser = listeAllGroupUser;
    }

    public boolean isVueMoveUserIntoGroupes() {
        return vueMoveUserIntoGroupes;
    }

    public void setVueMoveUserIntoGroupes(boolean vueMoveUserIntoGroupes) {
        this.vueMoveUserIntoGroupes = vueMoveUserIntoGroupes;
    }

    public Map<String, String> getListeOfUsersWithoutSuperadmin() {
        return listeOfUsersWithoutSuperadmin;
    }

    public void setListeOfUsersWithoutSuperadmin(Map<String, String> listeOfUsersWithoutSuperadmin) {
        this.listeOfUsersWithoutSuperadmin = listeOfUsersWithoutSuperadmin;
    }

    public int getUserSelected() {
        return userSelected;
    }

    public void setUserSelected(int userSelected) {
        this.userSelected = userSelected;
    }

    public String getPassword5() {
        return password5;
    }

    public void setPassword5(String password5) {
        this.password5 = password5;
    }

    public boolean isEditPassed() {
        return editPassed;
    }

    public ArrayList<NodeUserGroup> getNodeAllProject() {
        return nodeAllProject;
    }

    public void setNodeAllProject(ArrayList<NodeUserGroup> nodeAllProject) {
        this.nodeAllProject = nodeAllProject;
    }

    public NodeUserGroup getNodeUserGroupEdit() {
        return nodeUserGroupEdit;
    }

    public void setNodeUserGroupEdit(NodeUserGroup nodeUserGroupEdit) {
        this.nodeUserGroupEdit = nodeUserGroupEdit;
    }


}
