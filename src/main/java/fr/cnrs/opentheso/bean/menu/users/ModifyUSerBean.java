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
import fr.cnrs.opentheso.bean.profile.SuperAdminBean;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "modifyUSerBean")
@SessionScoped
public class ModifyUSerBean implements Serializable {
    @Inject private Connect connect;
    @Inject private MyProjectBean myProjectBean;
    @Inject private SuperAdminBean superAdminBean;
    
    private NodeUser nodeUser;
    private String passWord1;
    private String passWord2;    
           
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        nodeUser = null;
        passWord1 = null;
        passWord2 = null;
    }       
    
    public ModifyUSerBean() {
    }
    
    
    /**
     * Permet de selectionner l'utilisateur dans la liste avec toutes les
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

    /**
     *
     * @param idUser
     */
    public void setUserStringId(String idUser){
        int id = -1;
        FacesMessage msg;
        try {
            id = Integer.parseInt(idUser);
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }            
        UserHelper userHelper = new UserHelper();
        nodeUser = userHelper.getUser(connect.getPoolConnexion(), id);        
    }

    /**
     * Permet de supprimer un utilisateur
     */
    public void deleteUser() {
        UserHelper userHelper = new UserHelper();
        FacesMessage msg;
        if(nodeUser.getIdUser() == -1) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        
        if(!userHelper.deleteUser(connect.getPoolConnexion(), nodeUser.getIdUser())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de suppression de l'utilisateur !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'utilisateur a bien été supprimé !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        superAdminBean.init();
    }
    
    
    public void updateUser(){
        
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        
        if(nodeUser== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }           
        UserHelper userHelper = new UserHelper();
        if(!userHelper.updateUser(
                connect.getPoolConnexion(),
                nodeUser.getIdUser(),
                nodeUser.getName(),
                nodeUser.getMail(),
                nodeUser.isActive(),
                nodeUser.isAlertMail())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de changement !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Utilisateur changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selectUser(nodeUser.getIdUser());
        myProjectBean.setLists();
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }      
    
    
    public void updateUser2(){
        
        FacesMessage msg;
        
        if(nodeUser== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }           
        UserHelper userHelper = new UserHelper();
        nodeUser.setName(nodeUser.getName().trim());
        if(!userHelper.updateUser(
                connect.getPoolConnexion(),
                nodeUser.getIdUser(),
                nodeUser.getName(),
                nodeUser.getMail(),
                nodeUser.isActive(),
                nodeUser.isAlertMail())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de changement !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Utilisateur changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        superAdminBean.init();
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
