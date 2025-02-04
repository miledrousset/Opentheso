package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import fr.cnrs.opentheso.bean.profile.SuperAdminBean;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.time.LocalDate;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "modifyUserBean")
@SessionScoped
public class ModifyUserBean implements Serializable {

    @Autowired @Lazy private MyProjectBean myProjectBean;
    @Autowired @Lazy private SuperAdminBean superAdminBean;

    @Autowired
    private UserHelper userHelper;
    
    private NodeUser nodeUser;
    private String passWord1;
    private String passWord2;;
    private boolean hasKey;
    private LocalDate apiKeyExpireDate;
    private boolean keyNeverExpire;

    public void toggleHasKey(){
    }

    public void toggleKeyNeverExpire(){
    }

    /**
     * Permet de selectionner l'utilisateur dans la liste avec toutes les
     * informations nécessaires pour sa modification
     *
     * @param idUser
     */
    public void selectUser(int idUser) {
        nodeUser = userHelper.getUser(idUser);
        setUserStringId(""+idUser);
        passWord1 = null;
        passWord2 = null;
    }

    public boolean hasKey(){
        if (nodeUser==null){return false;}
        if (nodeUser.isKeyNeverExpire()){
            return true;
        }
        if (nodeUser.getApiKeyExpireDate()!=null){
            return true;
        }


        return false;
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
        } catch (NumberFormatException e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        nodeUser = userHelper.getUser(id);
        hasKey = hasKey();
        apiKeyExpireDate = nodeUser.getApiKeyExpireDate();
        keyNeverExpire = nodeUser.isKeyNeverExpire();
    }

    /**
     * Permet de supprimer un utilisateur
     */
    public void deleteUser() {
        FacesMessage msg;
        if(nodeUser.getIdUser() == -1) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        
        if(!userHelper.deleteUser(nodeUser.getIdUser())){
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

        if(!userHelper.updateUser(nodeUser.getIdUser(), nodeUser.getName(), nodeUser.getMail(), nodeUser.isActive(), nodeUser.isAlertMail())){
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

        nodeUser.setName(nodeUser.getName().trim());
        if(!userHelper.updateUser(nodeUser.getIdUser(), nodeUser.getName(), nodeUser.getMail(), nodeUser.isActive(), nodeUser.isAlertMail())){
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

        if(!userHelper.updatePwd(nodeUser.getIdUser(), MD5Password.getEncodedPassword(passWord2))){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de changement de passe !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Mot de passe changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selectUser(nodeUser.getIdUser());        
    }

    /**
     * Permet de sauvegarder les informations concernant la clé  d'API
     */
    public void updateApiKey() {
        Boolean keyNeverExpireValue = nodeUser.isKeyNeverExpire();
        LocalDate apiKeyExpireDateValue = null;

        if (hasKey) {
            if (keyNeverExpireValue) {
                keyNeverExpireValue = true;
                apiKeyExpireDate = null;
            } else {
                keyNeverExpireValue = false;
                apiKeyExpireDateValue = apiKeyExpireDate;
            }
        } else {
            keyNeverExpireValue = false;
            apiKeyExpireDate = null;
        }

        // Mise à jour de l'utilisateur dans la base de données
        FacesMessage msg;
        if(userHelper.updateApiKeyInfos(nodeUser.getIdUser(), keyNeverExpireValue, apiKeyExpireDateValue)){
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Clé mise à jour avec succès !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de mise à jour !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
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

    public boolean isHasKey() {
        return hasKey;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    public LocalDate getApiKeyExpireDate() {
        return apiKeyExpireDate;
    }

    public void setApiKeyExpireDate(LocalDate apiKeyExpireDate) {
        this.apiKeyExpireDate = apiKeyExpireDate;
    }

    public boolean isKeyNeverExpire() {
        return keyNeverExpire;
    }

    public void setKeyNeverExpire(boolean keyNeverExpire) {
        this.keyNeverExpire = keyNeverExpire;
    }


}
