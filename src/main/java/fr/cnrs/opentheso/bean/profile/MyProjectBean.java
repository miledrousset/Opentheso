package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.users.NodeUserRole;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.UserRoleGroupService;
import fr.cnrs.opentheso.services.UserService;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


@Getter
@Setter
@Slf4j
@SessionScoped
@RequiredArgsConstructor
@Named(value = "myProjectBean")
public class MyProjectBean implements Serializable {

    private final UserService userService;
    private final CurrentUser currentUser;
    private final UserRoleGroupService userRoleGroupService;

    private List<NodeIdValue> listeThesaurusOfProject, myAuthorizedRoles;
    private Map<String, String> listeGroupsOfUser;
    private List<NodeUserRole> listeUserLimitedRole, listeUser;
    private NodeUserRoleGroup nodeUserRoleOnThisGroup, nodeUserRoleSuperAdmin, myRoleOnThisProject;
    private String selectedProject, selectedProjectName, selectedIndex, workLanguage;


    public void init() {
        listeThesaurusOfProject = null;
        listeGroupsOfUser = null;        
        selectedProject = null;
        selectedProjectName = null;
        myRoleOnThisProject = null;
        myAuthorizedRoles = null;
        selectedIndex = "0";
      
        log.debug("Recherche de la liste des groupes/projets d'un utilisateur");
        listeGroupsOfUser = currentUser.getNodeUser().isSuperAdmin()
                ? userRoleGroupService.findAllUserRoleGroup().stream().collect(Collectors.toMap(item -> String.valueOf(item.getId()), UserGroupLabel::getLabel))
                : userService.getGroupsOfUser(currentUser.getNodeUser().getIdUser());

        getListThesoByGroup();
    }
    
    private void initMyAuthorizedRoleOnThisGroup(){
        if(selectedProject.isEmpty()) return;
        myRoleOnThisProject = userService.getUserRoleOnThisGroup(currentUser.getNodeUser().getIdUser(),
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
        myAuthorizedRoles = userRoleGroupService.getRolesByIdGreaterThanEqual(idRoleFrom);
    }
    
    public void setLists() {
        listeUser = null;
        listeUserLimitedRole = null;
        listeThesaurusOfProject = null;
        nodeUserRoleOnThisGroup = null;
        nodeUserRoleSuperAdmin = null;
        getListThesoByGroup();
        listUsersByGroup();
        resetListLimitedRoleUsers();
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
        if (selectedProject == null || selectedProject.isEmpty()) {
            return;
        }

        int idGroup = Integer.parseInt(selectedProject);
        listeUserLimitedRole = userService.getAllUsersRolesLimitedByTheso(idGroup);
    }

    
    /**
     * retourne la liste des thésaurus par groupe
     */
   
    private void getListThesoByGroup(){
        if (StringUtils.isEmpty(selectedProject)) {
            return;
        }
        
        var idGroup = Integer.parseInt(selectedProject);
        listeThesaurusOfProject = userService.getThesaurusOfProject(idGroup, workLanguage);
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
            listeUser = userService.getUsersRolesByGroup(idGroup, nodeUserRoleSuperAdmin.getIdRole());
        } else {
            if (nodeUserRoleOnThisGroup != null) {
                listeUser = userService.getUsersRolesByGroup(idGroup, nodeUserRoleOnThisGroup.getIdRole());
            } else {
                if (listeUser != null) {
                    listeUser.clear(); //cas où on supprime l'utilisateur en cours
                }
            }
        }
        resetListLimitedRoleUsers();
    }

    private void setUserRoleOnThisGroup() {

        if (StringUtils.isEmpty(selectedProject)) {
            return;
        }
        int idGroup = Integer.parseInt(selectedProject);
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            var role = userRoleGroupService.getRoleById(1);
            nodeUserRoleSuperAdmin = NodeUserRoleGroup.builder().idRole(role.getId()).roleName(role.getName()).build();
            return;
        }
        nodeUserRoleOnThisGroup = userService.getUserRoleOnThisGroup(currentUser.getNodeUser().getIdUser(), idGroup);
    }
    
    /**
     * permet de savoir si l'utilisateur est Admin sur ce Groupe / SuperAdmin
     */
    public boolean isAdminOnThisGroup() {
        if (currentUser.getNodeUser().isSuperAdmin()) {
            return true;
        }

        if (StringUtils.isEmpty(selectedProject)) {
            return false;
        }

        int idGroup = Integer.parseInt(selectedProject);
        int idUser = currentUser.getNodeUser().getIdUser();
        var userRole = userService.getUserRoleOnThisGroup(idUser, idGroup);
        return userRole != null && userRole.getIdRole() < 3;
    }
    
    public String getSelectedProjectName() {
        if(selectedProject != null)
            if(!selectedProject.isEmpty())
                selectedProjectName = userRoleGroupService.getUserGroupLabelRepository(Integer.parseInt(selectedProject)).getLabel();
            else
                selectedProjectName = selectedProject;
        return selectedProjectName;
    }
}
