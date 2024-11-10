package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.users.NodeUserGroup;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import fr.cnrs.opentheso.models.users.NodeUserGroupUser;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;

/**
 *
 * @author miledrousset
 */
@Named(value = "superAdminBean")
@SessionScoped
public class SuperAdminBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    @Autowired
    private UserHelper userHelper;
    
    @Autowired @Lazy 
    private CurrentUser currentUser;
    
    @Autowired @Lazy 
    private SelectedTheso selectedTheso;
    
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
        allUsers = userHelper.getAllUsers();
    }    
    
    /**
     * permet de récupérer la liste de tous les utilisateurs avec les roles pour chaque projet
     */
    private void listAllUsersProjectRole(){
        String idLang = workLanguage;
        if(selectedTheso.getCurrentLang() != null)
            idLang = selectedTheso.getCurrentLang();
        
        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeUserGroupUsers = userHelper.getAllGroupUser();
            nodeUserGroupUsers.addAll(userHelper.getAllGroupUserWithoutGroup());
            nodeUserGroupUsers.addAll(userHelper.getAllUsersSuperadmin());
        } 
    }      
    
    /**
     * permet de retourner la liste de tous les projets
     */
    private void listAllProjects(){
        allProjects = userHelper.getAllProject();
    }
    
    private void listAllThesaurus(){
        allThesoProject = new ArrayList<>();
        String idLang = workLanguage;
        if(selectedTheso.getCurrentLang() != null)
            idLang = selectedTheso.getCurrentLang();
        ArrayList<NodeUserGroupThesaurus> allThesoWithProject = userHelper.getAllGroupTheso(idLang);
        ArrayList<NodeUserGroupThesaurus> allThesoWithoutProject = userHelper.getAllThesoWithoutGroup(idLang);
        allThesoProject.addAll(allThesoWithProject);
        allThesoProject.addAll(allThesoWithoutProject);
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public SelectedTheso getSelectedTheso() {
        return selectedTheso;
    }

    public void setSelectedTheso(SelectedTheso selectedTheso) {
        this.selectedTheso = selectedTheso;
    }

    public ArrayList<NodeUser> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(ArrayList<NodeUser> allUsers) {
        this.allUsers = allUsers;
    }

    public ArrayList<NodeUserGroupUser> getNodeUserGroupUsers() {
        return nodeUserGroupUsers;
    }

    public void setNodeUserGroupUsers(ArrayList<NodeUserGroupUser> nodeUserGroupUsers) {
        this.nodeUserGroupUsers = nodeUserGroupUsers;
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
