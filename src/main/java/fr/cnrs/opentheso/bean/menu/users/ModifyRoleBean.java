/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRole;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
    
    // pour gérer les droits limités sur un ou plusieurs thésaurus
    private boolean limitOnTheso;    
    private ArrayList<NodeIdValue> listThesoOfProject;
    private List<String> selectedThesos; 
    private NodeUserRole nodeUserRole;

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
        limitOnTheso = false;
        listThesoOfProject = null;
        selectedThesos = null;        
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
        
        limitOnTheso = false;
        listThesoOfProject = null;
        selectedThesos = null;        
    }
    
    /**
     * permet de selectionner l'utilisateur qui a des droits limités 
     * informations nécessaires pour sa modification
     *
     * @param selectedNodeUserRole
     * @param selectedProject
     */
    public void selectUserWithLimitedRole(NodeUserRole selectedNodeUserRole, String selectedProject) {
        nodeUserRole = selectedNodeUserRole;
        this.selectedProject = selectedProject;
        limitOnTheso = true;
        UserHelper userHelper = new UserHelper();
        selectedThesos = new ArrayList<>();
        ArrayList<NodeUserRole> nodeUserRoles = userHelper.getListRoleByThesoLimited(connect.getPoolConnexion(), Integer.parseInt(selectedProject), nodeUserRole.getIdUser());
        for (NodeUserRole nodeUserRole1 : nodeUserRoles) {
            selectedThesos.add(nodeUserRole1.getIdTheso());
        }
        toogleLimitTheso();
    }    
    
    /**
     * met à jour les rôles de l'utilisateur sur les thésaurus du projet
     */
    public void updateUserRoleLimitedForSelectedUser () {
        FacesMessage msg;
        
        if(nodeUserRole == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        UserHelper userHelper = new UserHelper();

        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            if(!userHelper.deleteAllUserRoleOnTheso(connect.getPoolConnexion(), nodeUserRole.getIdUser(), Integer.parseInt(selectedProject))){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la modification des rôles !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;                    
            }
            if(!userHelper.addUserRoleOnTheso(connect.getPoolConnexion(), 
                    nodeUserRole.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject), selectedThesos)){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la modification des rôles !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;                
            }
        } else {
            if(!userHelper.updateUserRoleOnGroup(
                    connect.getPoolConnexion(),
                    nodeSelectedUser.getIdUser(),
                    Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject))) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;             
            }
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }       
    
    /**
     * permet de supprimer le rôle de l'utilisateur sur ce thésaurus du projet
     */
    public void removeUserRoleOnTheso () {
        FacesMessage msg;
        
        if(nodeUserRole == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de rôle sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        UserHelper userHelper = new UserHelper();
        if(!userHelper.deleteUserRoleOnTheso(connect.getPoolConnexion(),
                nodeUserRole.getIdUser(),
                nodeUserRole.getIdRole(),
                Integer.parseInt(selectedProject),
                nodeUserRole.getIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de suppression du rôle de l'utilisateur pour ce thésaurus !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été supprimé !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListLimitedRoleUsers();
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
   
    public void toogleLimitTheso(){
        if(!limitOnTheso) return;
        /// récupérer la liste des thésaurus d'un projet
        int idProject = -1;
        try {
            idProject = Integer.parseInt(selectedProject);
        } catch (Exception e) {
            return;
        }
        if(idProject == -1) return;
        UserHelper userHelper = new UserHelper();
        listThesoOfProject = userHelper.getThesaurusOfProject(connect.getPoolConnexion(), idProject, connect.getWorkLanguage());
    }  
    
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

        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            if(!userHelper.addUserRoleOnTheso(connect.getPoolConnexion(), 
                    nodeSelectedUser.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject), selectedThesos)){
                return;
            }
        } else {
            if(!userHelper.updateUserRoleOnGroup(
                    connect.getPoolConnexion(),
                    nodeSelectedUser.getIdUser(),
                    Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject))) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;             
            }
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
     * permet d'ajouter un utilisateur existant au projet
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

    public boolean isLimitOnTheso() {
        return limitOnTheso;
    }

    public void setLimitOnTheso(boolean limitOnTheso) {
        this.limitOnTheso = limitOnTheso;
    }

    public ArrayList<NodeIdValue> getListThesoOfProject() {
        return listThesoOfProject;
    }

    public void setListThesoOfProject(ArrayList<NodeIdValue> listThesoOfProject) {
        this.listThesoOfProject = listThesoOfProject;
    }

    public List<String> getSelectedThesos() {
        return selectedThesos;
    }

    public void setSelectedThesos(List<String> selectedThesos) {
        this.selectedThesos = selectedThesos;
    }

    public NodeUserRole getNodeUserRole() {
        return nodeUserRole;
    }

    public void setNodeUserRole(NodeUserRole nodeUserRole) {
        this.nodeUserRole = nodeUserRole;
    }


    
}
