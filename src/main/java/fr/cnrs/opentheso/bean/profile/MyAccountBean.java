/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bdd.tools.MD5Password;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
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
@Named(value = "myAccountBean")
@SessionScoped
public class MyAccountBean implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private CurrentUser currentUser;

    private NodeUser nodeUser;
    private String passWord1;
    private String passWord2;
    // liste des (rôle -> projet) qui existent déjà pour l'utilisateur     
    ArrayList<NodeUserRoleGroup> allMyRoleProject;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (allMyRoleProject != null) {
            allMyRoleProject.clear();
            allMyRoleProject = null;
        }
        nodeUser = null;
        passWord1 = null;
        passWord2 = null;
    }

    public MyAccountBean() {
    }

    public void reset() {
        nodeUser = currentUser.getNodeUser();
        passWord1 = null;
        passWord2 = null;
        initAllMyRoleProject();
    }

    private void initAllMyRoleProject() {
        UserHelper userHelper = new UserHelper();
        allMyRoleProject = userHelper.getUserRoleGroup(connect.getPoolConnexion(), nodeUser.getIdUser());
    }

    public void updatePseudo() {
        
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (nodeUser.getName() == null || nodeUser.getName().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Le pseudo est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        UserHelper userHelper = new UserHelper();
        if (!userHelper.updatePseudo(
                connect.getPoolConnexion(),
                nodeUser.getIdUser(),
                nodeUser.getName())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de changement de pseudo !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Pseudo changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    public void updateAlertEmail() {
        FacesMessage msg;

        UserHelper userHelper = new UserHelper();
        if (!userHelper.setAlertMailForUser(
                connect.getPoolConnexion(),
                nodeUser.getIdUser(),
                nodeUser.isAlertMail())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant le changement d'alertes !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Alerte changée avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();
    }

    public void updateEmail() {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (nodeUser.getMail() == null || nodeUser.getMail().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un Email est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        UserHelper userHelper = new UserHelper();
        if (!userHelper.updateMail(
                connect.getPoolConnexion(),
                nodeUser.getIdUser(),
                nodeUser.getMail())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de changement d'Email !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Email changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    public void updatePassword() {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        
        if (passWord1 == null || passWord1.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un mot de passe est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (passWord2 == null || passWord2.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un mot de passe est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (!passWord1.equals(passWord2)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Mot de passe non identique !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (!new UserHelper().updatePwd(connect.getPoolConnexion(), nodeUser.getIdUser(),
                MD5Password.getEncodedPassword(passWord2))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de changement de passe !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Mot de passe changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    public Connect getConnect() {
        return connect;
    }

    public void setConnect(Connect connect) {
        this.connect = connect;
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
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

    public ArrayList<NodeUserRoleGroup> getAllMyRoleProject() {
        return allMyRoleProject;
    }

    public void setAllMyRoleProject(ArrayList<NodeUserRoleGroup> allMyRoleProject) {
        this.allMyRoleProject = allMyRoleProject;
    }
}
