package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.entites.UserRoleGroup;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.enterprise.context.SessionScoped;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 * Bean pour la gestion des actions de compte utilisateur telles que la mise à jour des informations de profil et des clés API.
 * Cette classe est à portée de session et gère les paramètres du compte utilisateur.
 *
 * @author miledrousset
 */
@Slf4j
@Data
@SessionScoped
@Named(value = "myAccountBean")
public class MyAccountBean implements Serializable {

    private final CurrentUser currentUser;
    private final ApiKeyHelper apiKeyHelper;
    private final UserRepository userRepository;
    private final UserRoleGroupRepository userRoleGroupRepository;

    private User user;
    private NodeUser nodeUser;
    private String passWord1, passWord2, displayedKey;
    // liste des (rôle -> projet) qui existent déjà pour l'utilisateur
    private List<NodeUserRoleGroup> allMyRoleProject;


    public MyAccountBean(CurrentUser currentUser,
                         ApiKeyHelper apiKeyHelper,
                         UserRepository userRepository,
                         UserRoleGroupRepository userRoleGroupRepository) {

        this.currentUser = currentUser;
        this.apiKeyHelper = apiKeyHelper;
        this.userRepository = userRepository;
        this.userRoleGroupRepository = userRoleGroupRepository;
    }

    public void loadDataPage(){

        user = userRepository.findById(currentUser.getNodeUser().getIdUser()).get();

        nodeUser = NodeUser.builder()
                .idUser(user.getId())
                .name(user.getUsername())
                .mail(user.getMail())
                .active(user.getActive())
                .alertMail(user.getAlertMail())
                .superAdmin(user.getIsSuperAdmin())
                .passToModify(user.getPassToModify())
                .apiKey(user.getApiKey())
                .keyNeverExpire(user.getKeyNeverExpire())
                .apiKeyExpireDate(user.getKeyExpiresAt())
                .isServiceAccount(user.getIsServiceAccount())
                .keyDescription(user.getKeyDescription())
                .build();

        displayedKey = StringUtils.isEmpty(nodeUser.getApiKey()) ? null : new String(new char[64]).replace("\0", "*");
        passWord1 = null;
        passWord2 = null;
    }

    public void updateKey() {
        displayedKey = apiKeyHelper.generateApiKey("ot_", 64);
        nodeUser.setApiKey(displayedKey);

        if (apiKeyHelper.saveApiKey(MD5Password.getEncodedPassword(displayedKey), nodeUser.getIdUser())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "La clé a bien été enregistrée.");
        } else {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de sauvegarde de la clé.");
        }

    }

    private NodeUserRoleGroup toNodeUserRoleGroup(UserRoleGroup userRoleGroup) {

        return NodeUserRoleGroup.builder()
                .idGroup(userRoleGroup.getGroup().getId())
                .groupName(userRoleGroup.getGroup().getLabel())
                .idRole(userRoleGroup.getRole().getId())
                .roleName(userRoleGroup.getRole().getName())
                .isAdmin(userRoleGroup.getRole().getId() == 2)
                .isContributor(userRoleGroup.getRole().getId() == 4)
                .isManager(userRoleGroup.getRole().getId() == 3)
                .build();
    }

    public void updatePseudo() {

        if (StringUtils.isEmpty(nodeUser.getName())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le pseudo est obligatoire !!!");
            return;
        }

        user.setUsername(nodeUser.getName());
        userRepository.save(user);
        showMessage(FacesMessage.SEVERITY_INFO, "Pseudo changé avec succès !!!");

        PrimeFaces.current().ajax().update("containerIndex");
    }

    public void updateAlertEmail() {

        user.setAlertMail(nodeUser.isAlertMail());
        userRepository.save(user);
        showMessage(FacesMessage.SEVERITY_INFO, "Alerte changée avec succès !!!");
    }

    public void updateEmail() {

        if (StringUtils.isEmpty(nodeUser.getMail())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Un Email est obligatoire !!!");
            return;
        }

        user.setMail(nodeUser.getMail());
        userRepository.save(user);
        showMessage(FacesMessage.SEVERITY_INFO, "Email changé avec succès !!!");

        PrimeFaces.current().ajax().update("containerIndex");
    }

    public void updatePassword() {

        if (StringUtils.isEmpty(passWord1)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Un mot de passe est obligatoire !!!");
            return;
        }

        if (StringUtils.isEmpty(passWord2)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Un mot de passe est obligatoire !!!");
            return;
        }

        if (!passWord1.equals(passWord2)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Mot de passe non identique !!!");
            return;
        }

        user.setPassword(MD5Password.getEncodedPassword(passWord2));
        userRepository.save(user);

        showMessage(FacesMessage.SEVERITY_INFO, "Mot de passe changé avec succès !!!");

        PrimeFaces.current().ajax().update("containerIndex");
    }

    public boolean isKeyExpired() {
        if (ObjectUtils.isEmpty(nodeUser.getApiKeyExpireDate())) return false;
        return LocalDate.now().isAfter(nodeUser.getApiKeyExpireDate());
    }

    private void showMessage(FacesMessage.Severity severity, String message) {
        var msg = new FacesMessage(severity, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }
}

