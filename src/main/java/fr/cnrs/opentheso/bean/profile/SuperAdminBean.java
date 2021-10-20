/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroupThesaurus;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroupUser;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PreDestroy;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "superAdminBean")
@SessionScoped
public class SuperAdminBean implements Serializable {

    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private CurrentUser currentUser;
    @Inject private SelectedTheso selectedTheso;
    
    private ArrayList<NodeUser> allUsers;// la liste de tous les utilisateurs  
    private ArrayList<NodeUserGroupUser> nodeUserGroupUsers; // liste des utilisateurs + projets + roles
    
    private ArrayList<NodeUserGroup> allProjects;
    private ArrayList<NodeUserGroupThesaurus> allThesoProject;     
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(allUsers!= null){
            allUsers.clear();
            allUsers = null;
        }
        if(allProjects!= null){
            allProjects.clear();
            allProjects = null;
        }
        if(allThesoProject!= null){
            allThesoProject.clear();
            allThesoProject = null;
        }    
        if(nodeUserGroupUsers!= null){
            nodeUserGroupUsers.clear();
            nodeUserGroupUsers = null;
        }         
        
    }    
    
    public SuperAdminBean() {
    }

    public void init() {
        allUsers = null;
        allProjects = null;
        allThesoProject = null;
        nodeUserGroupUsers = null;
        listAllUsers();
        listAllUsersProjectRole();
        listAllProjects();
        listAllThesaurus();
    }
    
    /**
     * permet de récupérer la liste de tous les utilisateurs (Pour SuperAdmin)
     */
    private void listAllUsers(){
        UserHelper userHelper = new UserHelper();

        if (currentUser.getNodeUser().isIsSuperAdmin()) {// l'utilisateur est superAdmin
            allUsers = userHelper.getAllUsers(connect.getPoolConnexion());
        } 
    }    
    
    /**
     * permet de récupérer la liste de tous les utilisateurs avec les roles pour chaque projet
     */
    private void listAllUsersProjectRole(){
        UserHelper userHelper = new UserHelper();
        String idLang = connect.getWorkLanguage();
        if(selectedTheso.getCurrentLang() != null)
            idLang = selectedTheso.getCurrentLang();
        
        if (currentUser.getNodeUser().isIsSuperAdmin()) {// l'utilisateur est superAdmin
            nodeUserGroupUsers = userHelper.getAllGroupUser(connect.getPoolConnexion(), idLang);    
        } 
    }      
    
    /**
     * permet de retourner la liste de tous les projets
     */
    private void listAllProjects(){
        UserHelper userHelper = new UserHelper();
        allProjects = userHelper.getAllProject(
                connect.getPoolConnexion());    
    }
    
    private void listAllThesaurus(){
        allThesoProject = new ArrayList<>();
        UserHelper userHelper = new UserHelper();
        String idLang = connect.getWorkLanguage();
        if(selectedTheso.getCurrentLang() != null)
            idLang = selectedTheso.getCurrentLang();
        ArrayList<NodeUserGroupThesaurus> allThesoWithProject = userHelper.getAllGroupTheso(connect.getPoolConnexion(), idLang);
        ArrayList<NodeUserGroupThesaurus> allThesoWithoutProject = userHelper.getAllThesoWithoutGroup(connect.getPoolConnexion(), idLang);
        allThesoProject.addAll(allThesoWithProject);
        allThesoProject.addAll(allThesoWithoutProject);
    }
    

    public ArrayList<NodeUser> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(ArrayList<NodeUser> allUsers) {
        this.allUsers = allUsers;
    }

    public ArrayList<NodeUserGroup> getAllProjects() {
        return allProjects;
    }

    public void setAllProjects(ArrayList<NodeUserGroup> allProjects) {
        this.allProjects = allProjects;
    }

    public ArrayList<NodeUserGroupThesaurus> getAllThesoProject() {
        return allThesoProject;
    }

    public void setAllThesoProject(ArrayList<NodeUserGroupThesaurus> allThesoProject) {
        this.allThesoProject = allThesoProject;
    }

    public ArrayList<NodeUserGroupUser> getNodeUserGroupUsers() {
        return nodeUserGroupUsers;
    }

    public void setNodeUserGroupUsers(ArrayList<NodeUserGroupUser> nodeUserGroupUsers) {
        this.nodeUserGroupUsers = nodeUserGroupUsers;
    }



    
}
