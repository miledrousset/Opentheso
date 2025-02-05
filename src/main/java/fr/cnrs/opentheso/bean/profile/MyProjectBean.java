package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.users.NodeUserRole;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;


@Data
@Named(value = "myProjectBean")
@SessionScoped
public class MyProjectBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private final CurrentUser currentUser;
    private final UserHelper userHelper;

    private List<NodeIdValue> listeThesoOfProject, myAuthorizedRoles;
    private Map<String, String> listeGroupsOfUser;
    private List<NodeUserRole> listeUserLimitedRole, listeUser; // la liste des utilisateur du groupe avec des droits limités
    private NodeUserRoleGroup nodeUserRoleOnThisGroup, nodeUserRoleSuperAdmin, myRoleOnThisProject;
    private String selectedProject, selectedProjectName, selectedIndex;


    public MyProjectBean(CurrentUser currentUser, UserHelper userHelper) {
        this.currentUser = currentUser;
        this.userHelper = userHelper;
    }

    public void init() {
        listeThesoOfProject = null;
        listeGroupsOfUser = null;        
        selectedProject = null;
        selectedProjectName = null;
        myRoleOnThisProject = null;
        myAuthorizedRoles = null;
        selectedIndex = "0";
      
        getGroupsOfUser();
        getListThesoByGroup();
    }
    
    private void initMyAuthorizedRoleOnThisGroup(){
        if(selectedProject.isEmpty()) return;
        myRoleOnThisProject = userHelper.getUserRoleOnThisGroup(currentUser.getNodeUser().getIdUser(),
                Integer.parseInt(selectedProject));
    }    
    
    /**
     * permet de récupérer la liste des roles autorisés pour un utilisateur
     * c'est la liste des roles qu'il aura le droit d'attribuer aux nouveaux utilisateurs
     */
    private void initAuthorizedRoles() {
        int idRoleFrom = 4;
        if (currentUser.getNodeUser().isSuperAdmin()) {
            idRoleFrom = 1; // l'utilisateur est SuperAdmin
        } else {
            if (myRoleOnThisProject == null) {
                return;
            }
            if (myRoleOnThisProject.isAdmin()) {
                idRoleFrom = 2; // l'utilisateur est Admin            
            }
            if (myRoleOnThisProject.isManager()) {
                idRoleFrom = 3; // l'utilisateur est Manager            
            }
            if (myRoleOnThisProject.isContributor()) {
                idRoleFrom = 4; // l'utilisateur est Contributeur / user       
            }
        }
        myAuthorizedRoles = userHelper.getMyAuthorizedRoles(idRoleFrom);
    }     

    /**
     * permet de récupérer la liste des groupes/projets d'un utilisateur #MR
     */
    private void getGroupsOfUser() {
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            listeGroupsOfUser = userHelper.getAllGroups();
            return;
        }
        listeGroupsOfUser = userHelper.getGroupsOfUser(currentUser.getNodeUser().getIdUser());
    }    
    
    public void setLists() {
        listeUser = null;
        listeUserLimitedRole = null;
        listeThesoOfProject = null;
        nodeUserRoleOnThisGroup = null;
        nodeUserRoleSuperAdmin = null;
        getListThesoByGroup();
        listUsersByGroup();
        listUsersLimitedRoleByGroup();
        initMyAuthorizedRoleOnThisGroup();
        initAuthorizedRoles();        
    }    

    /**
     * appel après la modifcation d'un rôle pour un utilisateur
     */
    public void resetListUsers(){
        listUsersByGroup();
    }
    
    /**
     * appel après la modifcation d'un rôle limité pour un utilisateur
     */
    public void resetListLimitedRoleUsers(){
        listUsersLimitedRoleByGroup(); 
    }    
    
   
    
    /**
     * retourne la liste des thésaurus par groupe
     */
   
    private void getListThesoByGroup(){
        if (selectedProject == null || selectedProject.isEmpty()) {
            return;
        }
        
        int idGroup = Integer.parseInt(selectedProject);
        listeThesoOfProject = userHelper.getThesaurusOfProject(idGroup, workLanguage);
    } 
    
    /**
     * permet de récupérer la liste des utilisateurs suivants les options choisies
     */
    private void listUsersByGroup(){
        if (selectedProject == null || selectedProject.isEmpty()) {
            return;
        }

        int idGroup = Integer.parseInt(selectedProject);
        setUserRoleOnThisGroup();
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            listeUser = userHelper.getUsersRolesByGroup(
                    idGroup, nodeUserRoleSuperAdmin.getIdRole());
        } else {
            if (nodeUserRoleOnThisGroup != null) {
                listeUser = userHelper.getUsersRolesByGroup(
                        idGroup, nodeUserRoleOnThisGroup.getIdRole());
            } else {
                if (listeUser != null) {
                    listeUser.clear(); //cas où on supprime l'utilisateur en cours
                }
            }
        }
        listUsersLimitedRoleByGroup();
    }
    
     
    
    /**
     * permet de récupérer la liste des utilisateurs et les rôles sur les thésaurus du projet
     */
    private void listUsersLimitedRoleByGroup(){
        if (selectedProject == null || selectedProject.isEmpty()) {
            return;
        }

        int idGroup = Integer.parseInt(selectedProject);
        listeUserLimitedRole = userHelper.getAllUsersRolesLimitedByTheso(idGroup);
    }    
    
    /**
     * setting du role de l'utilisateur sur le group séléctionné
     *
     * #MR
     *
     * @return
     */
    private void setUserRoleOnThisGroup() {
        if (selectedProject == null) {
            return;
        }
        if (selectedProject.isEmpty()) {
            return;
        }
        int idGroup = Integer.parseInt(selectedProject);
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            nodeUserRoleSuperAdmin = userHelper.getUserRoleForSuperAdmin();
            return;
        }
        nodeUserRoleOnThisGroup = userHelper.getUserRoleOnThisGroup(currentUser.getNodeUser().getIdUser(), idGroup);
    } 
    
    
    /**
     * permet de savoir si l'utilisateur est Admin sur ce Groupe / SuperAdmin
     *
     * @return
     */
    public boolean isAdminOnThisGroup() {
        if (currentUser.getNodeUser().isSuperAdmin()) {
            return true;
        }
        if (selectedProject == null) {
            return false;
        }
        if (selectedProject.isEmpty()) {
            return false;
        }
        int idGroup = Integer.parseInt(selectedProject);
        return userHelper.isAdminOnThisGroup(
                currentUser.getNodeUser().getIdUser(), idGroup);
    }
    
    public String getSelectedProjectName() {
        if(selectedProject != null) 
            if(!selectedProject.isEmpty())
                selectedProjectName = userHelper.getGroupName(Integer.parseInt(selectedProject));
            else
                selectedProjectName = selectedProject;
        return selectedProjectName;
    }
}
