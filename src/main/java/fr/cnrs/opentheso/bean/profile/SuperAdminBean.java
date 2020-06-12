/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroupThesaurus;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
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
    
    private ArrayList<NodeUserGroup> allProjects;
    private ArrayList<NodeUserGroupThesaurus> allThesoProject;     
    
    public SuperAdminBean() {
    }

    public void init() {
        allUsers = null;
        allProjects = null;
        allThesoProject = null;
        listAllUsers();
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



    
}
