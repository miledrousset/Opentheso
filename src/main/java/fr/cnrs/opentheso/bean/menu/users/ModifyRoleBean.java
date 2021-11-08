/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "modifyRoleBean")
@SessionScoped
public class ModifyRoleBean implements Serializable {
    @Inject private Connect connect;
    @Inject private MyProjectBean myProjectBean;
    
    private NodeUser nodeSelectedUser;
    private String selectedProject;
    private String roleOfSelectedUser;

    // pour l'ajout d'un utilisateur existant au projet
    private NodeUser selectedUser;

    // liste des (rôle -> projet) qui existent déjà pour l'utilisateur     
    ArrayList<NodeUserRoleGroup> allMyRoleProject;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(allMyRoleProject!= null){
            allMyRoleProject.clear();
            allMyRoleProject = null;
        }
        nodeSelectedUser = null;
        selectedProject = null;
        roleOfSelectedUser = null;
        selectedUser = null;        
    }   
    
    public ModifyRoleBean() {
    }

    public void setSelectedProject(String selectedProject) {
        this.selectedProject = selectedProject;
    }
    
    
    /**
     * permet de selectionner l'utilisateur dans la liste avec toutes les
     * informations nécessaires pour sa modification
     *
     * @param idUser
     * @param roleOfSelectedUser
     * @param selectedProject
     */
    public void selectUser(int idUser, int roleOfSelectedUser, String selectedProject) {
        UserHelper userHelper = new UserHelper();
        nodeSelectedUser = userHelper.getUser(connect.getPoolConnexion(), idUser);
        this.selectedProject = selectedProject;

        this.roleOfSelectedUser = "" + roleOfSelectedUser;
        initAllMyRoleProject();
    }
    
    
    private void initAllMyRoleProject(){
        UserHelper userHelper = new UserHelper();
        allMyRoleProject = userHelper.getUserRoleGroup(connect.getPoolConnexion(), nodeSelectedUser.getIdUser());
    }
    
 /*   private void initAuthorizedProject(){
        UserHelper userHelper = new UserHelper();
        myAuthorizedProjects = userHelper.getMyAuthorizedProjects(connect.getPoolConnexion(), nodeUserSelected.getIdUser());
    }
   */ 
   
  
    
    /**
     * met à jour le nouveau rôle de l'utilisateur sur le projet
     */
    public void updateRoleForSelectedUser () {
        FacesMessage msg;
        
        if(nodeSelectedUser== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        
        
        UserHelper userHelper = new UserHelper();
        if(!userHelper.updateUserRoleOnGroup(
                connect.getPoolConnexion(),
                nodeSelectedUser.getIdUser(),
                Integer.parseInt(roleOfSelectedUser),
                Integer.parseInt(selectedProject))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }
   
    /**
     * permet de supprimer l'utilisateur du projet
     */
    public void removeUserFromProject () {
        FacesMessage msg;
        
        if(nodeSelectedUser== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        
        UserHelper userHelper = new UserHelper();
        if(!userHelper.deleteRoleOnGroup(
                connect.getPoolConnexion(),
                nodeSelectedUser.getIdUser(),
                Integer.parseInt(selectedProject))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de suppression de l'utilisateur du projet !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'utilisateur a été supprimé du projet !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }    
    
    /**
     * permet de'ajouter un utilisateur existant au projet
     */
    public void addUserToProject () {
        FacesMessage msg;
        
        if(selectedUser == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        
        UserHelper userHelper = new UserHelper();
        if(!userHelper.addUserRoleOnGroup(
                connect.getPoolConnexion(),
                selectedUser.getIdUser(),
                Integer.parseInt(roleOfSelectedUser),
                Integer.parseInt(selectedProject))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'utilisateur a été ajouté avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
            
        }
    }      
    
    
    public ArrayList<NodeUser> autoCompleteUser(String userName) {
        UserHelper userHelper = new UserHelper();
        ArrayList <NodeUser> nodeUsers = userHelper.searchUser(connect.getPoolConnexion(), userName);
        return nodeUsers;
    }    
    

    public NodeUser getNodeUser() {
        return nodeSelectedUser;
    }

    public void setNodeUser(NodeUser nodeUser) {
        this.nodeSelectedUser = nodeUser;
    }

    public String getRoleOfSelectedUser() {
        return roleOfSelectedUser;
    }

    public void setRoleOfSelectedUser(String roleOfSelectedUser) {
        this.roleOfSelectedUser = roleOfSelectedUser;
    }

    public ArrayList<NodeUserRoleGroup> getAllMyRoleProject() {
        return allMyRoleProject;
    }

    public void setAllMyRoleProject(ArrayList<NodeUserRoleGroup> allMyRoleProject) {
        this.allMyRoleProject = allMyRoleProject;
    }

    public NodeUser getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(NodeUser selectedUser) {
        this.selectedUser = selectedUser;
    }


    
}
