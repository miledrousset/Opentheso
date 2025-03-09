package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import fr.cnrs.opentheso.bean.profile.SuperAdminBean;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.time.LocalDate;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Data
@SessionScoped
@NoArgsConstructor
@Named(value = "modifyUserBean")
public class ModifyUserBean implements Serializable {

    private MyProjectBean myProjectBean;
    private SuperAdminBean superAdminBean;
    private UserRepository userRepository;
    
    private User nodeUser;
    private String passWord1, passWord2;
    private boolean hasKey;
    private LocalDate apiKeyExpireDate;


    @Inject
    public ModifyUserBean(MyProjectBean myProjectBean, SuperAdminBean superAdminBean, UserRepository userRepository) {
        this.myProjectBean = myProjectBean;
        this.superAdminBean = superAdminBean;
        this.userRepository = userRepository;
    }
    
    /**
     * Permet de selectionner l'utilisateur dans la liste avec toutes les informations nécessaires pour sa modification
     */
    public void selectUser(int idUser) {
        nodeUser = userRepository.findById(idUser).get();
        setUserStringId(""+idUser);
        passWord1 = null;
        passWord2 = null;
    }

    public boolean hasKey(){

        return ObjectUtils.isNotEmpty(nodeUser)
                && (nodeUser.getKeyNeverExpire() || ObjectUtils.isNotEmpty(nodeUser.getKeyNeverExpire()));
    }

    public void setUserStringId(String idUser){
        try {
            nodeUser = userRepository.findById(Integer.parseInt(idUser)).get();
            hasKey = hasKey();
            apiKeyExpireDate = nodeUser.getKeyExpiresAt();
        } catch (NumberFormatException e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun utilisateur sélectionné !!!");
        }

    }

    public void deleteUser() {

        if(nodeUser.getId() == -1) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun utilisateur sélectionné !!!");
            return;
        }

        userRepository.delete(nodeUser);
        showMessage(FacesMessage.SEVERITY_INFO, "L'utilisateur a bien été supprimé !!!");
        superAdminBean.init();
    }

    public void updateUser(){
        
        if(ObjectUtils.isEmpty(nodeUser)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun utilisateur sélectionné !!!");
            return;              
        }

        userRepository.save(nodeUser);
        showMessage(FacesMessage.SEVERITY_INFO, "Utilisateur changé avec succès !!!");

        selectUser(nodeUser.getId());
        myProjectBean.setLists();

        PrimeFaces.current().ajax().update("containerIndex");
    }      

    public void updateUser2(){
        
        if(ObjectUtils.isEmpty(nodeUser)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun utilisateur sélectionné !!!");
            return;              
        }

        nodeUser.setUsername(nodeUser.getUsername().trim());

        userRepository.save(nodeUser);
        showMessage(FacesMessage.SEVERITY_INFO,  "Utilisateur changé avec succès !!!");
        superAdminBean.init();
    }     
    
    public void updatePassword(){

        if(StringUtils.isEmpty(passWord1)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Un mot de passe est obligatoire !!!");
            return;              
        }

        if(StringUtils.isEmpty(passWord2)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Un mot de passe est obligatoire !!!");
            return;              
        }

        if(!passWord1.equals(passWord2)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Mot de passe non identique !!!");
            return;              
        }

        nodeUser.setPassword(MD5Password.getEncodedPassword(passWord2));
        userRepository.save(nodeUser);
        showMessage(FacesMessage.SEVERITY_INFO, "Mot de passe changé avec succès !!!");
        selectUser(nodeUser.getId());
    }

    public void updateApiKey() {

        var keyNeverExpireValue = nodeUser.getKeyNeverExpire();
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

        nodeUser.setKeyNeverExpire(keyNeverExpireValue);
        nodeUser.setKeyExpiresAt(apiKeyExpireDateValue);
        userRepository.save(nodeUser);
        showMessage(FacesMessage.SEVERITY_INFO, "Clé mise à jour avec succès !!!");
    }

    private void showMessage(FacesMessage.Severity type, String message) {
        var msg = new FacesMessage(type, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }
}
