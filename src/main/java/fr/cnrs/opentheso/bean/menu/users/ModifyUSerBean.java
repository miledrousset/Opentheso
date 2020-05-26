/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.tools.MD5Password;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "modifyUSerBean")
@SessionScoped
public class ModifyUSerBean implements Serializable {
    @Inject private Connect connect;
    @Inject private MyProjectBean myProjectBean;
    
    private NodeUser nodeUser;
    private String passWord1;
    private String passWord2;    
            
    public ModifyUSerBean() {
    }
    
    
    /**
     * permet de selectionner l'utilisateur dans la liste avec toutes les
     * informations nécessaires pour sa modification
     *
     * @param idUser
     */
    public void selectUser(int idUser) {
        UserHelper userHelper = new UserHelper();
        nodeUser = userHelper.getUser(connect.getPoolConnexion(), idUser);
        passWord1 = null;
        passWord2 = null;
    }   
    
    
    public void updateUser(){
        FacesMessage msg;
        
        if(nodeUser== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }           
        UserHelper userHelper = new UserHelper();
        if(!userHelper.updateUser(
                connect.getPoolConnexion(),
                nodeUser.getIdUser(),
                nodeUser.isIsActive(),
                nodeUser.isIsAlertMail())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de changement !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Utilisateur changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selectUser(nodeUser.getIdUser());
        myProjectBean.setLists();
    }      
    
    public void updatePassword(){
        FacesMessage msg;
        UserHelper userHelper = new UserHelper();
        if(passWord1 == null || passWord1.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un mot de passe est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        if(passWord2 == null || passWord2.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un mot de passe est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }   
        if(!passWord1.equals(passWord2)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Mot de passe non identique !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        if(!userHelper.updatePwd(
                connect.getPoolConnexion(),
                nodeUser.getIdUser(),
                MD5Password.getEncodedPassword(passWord2))){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de changement de passe !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Mot de passe changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selectUser(nodeUser.getIdUser());        
    }    

    public NodeUser getNodeUser() {
        return nodeUser;
    }

    public void setNodeUser(NodeUser nodeUser) {
        this.nodeUser = nodeUser;
    }

    public String getPassWord1() {
        return passWord1;
    }

    public void setPassWord1(String passWord1) {
        this.passWord1 = passWord1;
    }

    public String getPassWord2() {
        return passWord2;
    }

    public void setPassWord2(String passWord2) {
        this.passWord2 = passWord2;
    }
    
    
    
}
