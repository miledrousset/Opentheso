/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
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
@Named(value = "moveThesoToProjectBean")
@SessionScoped
public class MoveThesoToProjectBean implements Serializable {
    @Inject private Connect connect;
    @Inject private MyProjectBean myProjectBean;
    @Inject private CurrentUser currentUser;
    
    private NodeIdValue selectedThesoToMove;
    private String currentProject;
    private NodeUserGroup newProject;
    
            
    public MoveThesoToProjectBean() {
    }
    
    
    /**
     * permet d'initialiser les variables 
     *
     * @param selectedThesoToMove
     * @param currentProject
     */
    public void setTheso(NodeIdValue selectedThesoToMove, String currentProject) {
        this.selectedThesoToMove = selectedThesoToMove;
        this.currentProject = currentProject;
    }   
    
    public ArrayList<NodeUserGroup> autoCompleteProject(String projectName) {
        UserHelper userHelper = new UserHelper();
        ArrayList<NodeUserGroup> nodeProjects = null;
        if(currentUser.getNodeUser().isIsSuperAdmin()) {
            nodeProjects = userHelper.searchAllProject(
                    connect.getPoolConnexion(),
                    projectName);            
        } else {
            nodeProjects = userHelper.searchMyProject(
                    connect.getPoolConnexion(),
                    currentUser.getNodeUser().getIdUser(),
                    projectName);
        }
        return nodeProjects;
    }        

    public void moveThesoToProject(){
        FacesMessage msg;
        
        if(newProject== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de projet sélectioné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }   

        UserHelper userHelper = new UserHelper();
        if(!userHelper.moveThesoToGroup(
                connect.getPoolConnexion(),
                selectedThesoToMove.getId(),
                Integer.parseInt(currentProject),
                newProject.getIdGroup() )){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de déplacement !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }
               
             

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Projet déplacé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.init();
/*        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            //    pf.ajax().update("messageIndex");
            pf.ajax().update("profileForm:myProjectForm");
            pf.ajax().update("profileForm:modifyProjectForm");
        }*/
    }      

    public NodeUserGroup getNewProject() {
        return newProject;
    }

    public void setNewProject(NodeUserGroup newProject) {
        this.newProject = newProject;
    }



    public NodeIdValue getSelectedThesoToMove() {
        return selectedThesoToMove;
    }

    public void setSelectedThesoToMove(NodeIdValue selectedThesoToMove) {
        this.selectedThesoToMove = selectedThesoToMove;
    }
    

    
    
}
