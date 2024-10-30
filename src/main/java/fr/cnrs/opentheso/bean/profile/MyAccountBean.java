/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;

/**
 * Bean pour la gestion des actions de compte utilisateur telles que la mise à jour des informations de profil et des clés API.
 * Cette classe est à portée de session et gère les paramètres du compte utilisateur.
 *
 * @author miledrousset
 */
@Named(value = "myAccountBean")
@SessionScoped
public class MyAccountBean implements Serializable {

    @Autowired @Lazy
    private Connect connect;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private ApiKeyHelper apiKeyHelper;

    private NodeUser nodeUser;
    private String passWord1;
    private String passWord2;
    private String displayedKey;
    private LocalDate keyExpireDate;
    private Boolean isKeyExpired ;

    // liste des (rôle -> projet) qui existent déjà pour l'utilisateur
    ArrayList<NodeUserRoleGroup> allMyRoleProject;

    public MyAccountBean() {
    }

    /**
     * Nettoie les ressources avant la destruction.
     */
    @PreDestroy
    public void destroy() {
        clear();
    }

    /**
     * Réinitialise les informations de profil de l'utilisateur.
     */
    public void reset(){
        currentUser.reGetUser();
        nodeUser = currentUser.getNodeUser();
        displayedKey=nodeUser.getApiKey() == null ? null : new String(new char[64]).replace("\0", "*");
        passWord1 = null;
        passWord2 = null;
        initAllMyRoleProject();
        keyExpireDate = nodeUser.getApiKeyExpireDate();
        isKeyExpired = userHelper.isApiKeyExpired(nodeUser);

    }

    /**
     * Met à jour la clé API de l'utilisateur.
     *
     * @throws SQLException si une erreur d'accès à la base de données se produit.
     */
    public void updateKey() throws SQLException {
        displayedKey = apiKeyHelper.generateApiKey("ot_", 64);
        FacesMessage msg;
        nodeUser.setApiKey(displayedKey);
        if(apiKeyHelper.saveApiKey(connect.openConnexionPool(), MD5Password.getEncodedPassword(displayedKey), nodeUser.getIdUser())){
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La clé a bien été enregistrée.");
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de sauvegarde de la clé.");
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);

    }

    /**
     * Efface toutes les données utilisateur de la session.
     */
    public void clear() {
        if (allMyRoleProject != null) {
            allMyRoleProject.clear();
            allMyRoleProject = null;
        }
        nodeUser = null;
        passWord1 = null;
        passWord2 = null;
        displayedKey=null;
    }
    /**
     * Initialise la liste des rôles et projets pour l'utilisateur.
     */
    private void initAllMyRoleProject() {
        allMyRoleProject = userHelper.getUserRoleGroup(connect.openConnexionPool(), nodeUser.getIdUser());
    }

    public void updatePseudo() {

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (nodeUser.getName() == null || nodeUser.getName().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Le pseudo est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (!userHelper.updatePseudo(connect.openConnexionPool(), nodeUser.getIdUser(), nodeUser.getName())) {
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

    /**
     * Met à jour la préférence d'alerte email de l'utilisateur.
     */
    public void updateAlertEmail() {
        FacesMessage msg;
        if (!userHelper.setAlertMailForUser(
                connect.openConnexionPool(),
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

    /**
     * Met à jour l'adresse email de l'utilisateur.
     */
    public void updateEmail() {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (nodeUser.getMail() == null || nodeUser.getMail().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un Email est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (!userHelper.updateMail(
                connect.openConnexionPool(),
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

    /**
     * Met à jour le mot de passe de l'utilisateur.
     */
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

        if (!userHelper.updatePwd(connect.openConnexionPool(), nodeUser.getIdUser(),
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






// Getters et Setters

    public ApiKeyHelper getApiKeyHelper() {
        return apiKeyHelper;
    }

    public void setApiKeyHelper(ApiKeyHelper apiKeyHelper) {
        this.apiKeyHelper = apiKeyHelper;
    }

    public Boolean getKeyExpired() {
        return isKeyExpired;
    }

    public void setKeyExpired(Boolean keyExpired) {
        isKeyExpired = keyExpired;
    }

    public LocalDate getKeyExpireDate() {
        return keyExpireDate;
    }

    public void setKeyExpireDate(LocalDate keyExpireDate) {
        this.keyExpireDate = keyExpireDate;
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

    public String getDisplayedKey() {
        return displayedKey;
    }

    public void setDisplayedKey(String displayedKey) {
        this.displayedKey = displayedKey;
    }

    public ArrayList<NodeUserRoleGroup> getAllMyRoleProject() {
        return allMyRoleProject;
    }

    public void setAllMyRoleProject(ArrayList<NodeUserRoleGroup> allMyRoleProject) {
        this.allMyRoleProject = allMyRoleProject;
    }

    public boolean isKeyExpired() {
        return isKeyExpired;
    }

    public void setKeyExpired(boolean keyExpired) {
        isKeyExpired = keyExpired;
    }
}

