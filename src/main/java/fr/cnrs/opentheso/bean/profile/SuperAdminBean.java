/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRole;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
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
    
    public SuperAdminBean() {
    }

    public void init() {
        allUsers = null;
        listAllUsers();        
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

    public ArrayList<NodeUser> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(ArrayList<NodeUser> allUsers) {
        this.allUsers = allUsers;
    }


    

    
}
