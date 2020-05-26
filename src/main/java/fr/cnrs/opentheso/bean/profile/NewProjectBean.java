/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "newProjectBean")
@SessionScoped
public class NewProjectBean implements Serializable {
    @Inject private Connect connect;
    @Inject private MyProjectBean myProjectBean;
    @Inject private CurrentUser currentUser;
 
    private String projectName;
    private ArrayList<NodeUserGroup> listeProjectOfUser;
            
    public NewProjectBean() {
    }
    
    
    /**
     * permet d'initialiser les variables 
     *
     */
    public void init() {
        projectName = null;
        
        UserHelper userHelper = new UserHelper();
        if (currentUser.getNodeUser().isIsSuperAdmin()) {// l'utilisateur est superAdmin
            listeProjectOfUser = userHelper.getAllProject(connect.getPoolConnexion());
            return;
        }
        listeProjectOfUser = userHelper.getProjectsOfUser(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());        
    }   
    
    public void addNewProject(){
        FacesMessage msg;
        
        if(projectName== null || projectName.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un label est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }           
        UserHelper userHelper = new UserHelper();
        if(!userHelper.createUserGroup(
                connect.getPoolConnexion(),
                projectName)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Projet créé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.init();
    }      

    public void updateProject(NodeUserGroup nodeUserGroup){
        FacesMessage msg;
        
        if(nodeUserGroup== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de projet sélectioné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }   
        if(nodeUserGroup.getGroupName().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le label est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        UserHelper userHelper = new UserHelper();
        if(!userHelper.updateProject(
                connect.getPoolConnexion(),
                nodeUserGroup.getGroupName(),
                nodeUserGroup.getIdGroup())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de modification !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Projet modifié avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.init();
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            //    pf.ajax().update("messageIndex");
            pf.ajax().update("profileForm:myProjectForm");
            pf.ajax().update("profileForm:modifyProjectForm");
        }
    }      
    
    
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public ArrayList<NodeUserGroup> getListeProjectOfUser() {
        return listeProjectOfUser;
    }

    public void setListeProjectOfUser(ArrayList<NodeUserGroup> listeProjectOfUser) {
        this.listeProjectOfUser = listeProjectOfUser;
    }
    

    
    
}
