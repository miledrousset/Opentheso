package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.ProjectService;
import fr.cnrs.opentheso.services.UserRoleGroupService;
import fr.cnrs.opentheso.services.UserService;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


/**
 *
 * @author miledrousset
 */
@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "newProjectBean")
public class NewProjectBean implements Serializable {

    private final MyProjectBean myProjectBean;
    private final CurrentUser currentUser;
    private final UserRoleGroupService userRoleGroupService;
    private final UserService userService;
    private final ProjectService projectService;

    private String projectName;
    private List<UserGroupLabel> listeProjectOfUser;


    public void init() {
        projectName = null;
        if (currentUser.getNodeUser().isSuperAdmin()) {
            listeProjectOfUser = userRoleGroupService.findAllUserRoleGroup();
            listeProjectOfUser.sort(Comparator.comparing(UserGroupLabel::getLabel, String.CASE_INSENSITIVE_ORDER));
            return;
        }
        listeProjectOfUser = projectService.getProjectByUser(currentUser.getNodeUser().getIdUser(), 2);
    }   
    
    /**
     * Permet de créer un nouveau projet de regroupement de thésaurus,
     * si l'utilisateur est SuperAdmin, le projet n'aura pas d'utilisateur pas défaut,
     * si l'utilisateur est Admin, il aura les droits admin dessus par défaut
     */
    public void addNewProject(){
        
        if(StringUtils.isEmpty(projectName)) {
            MessageUtils.showErrorMessage("Le label est obligatoire");
            return;              
        }

        var userGroup = projectService.getUserGroupLabelByLabel(projectName);
        if(userGroup != null){
            MessageUtils.showErrorMessage("Le label existe déjà !!!");
            return;             
        }

        var userGroupCreated = projectService.saveNewProject(UserGroupLabel.builder().label(projectName).build());
        currentUser.initUserPermissions();
        // on vérifie si l'utilisateur en cours est un Admin 
        if(!currentUser.getNodeUser().isSuperAdmin() && CollectionUtils.isNotEmpty(currentUser.getAllAuthorizedProjectAsAdmin())){
            // on donne le droit admin pour l'utilisateur courant sur ce groupe
            userRoleGroupService.addUserRoleOnGroup(currentUser.getNodeUser().getIdUser(), 2, userGroupCreated.getId());
        }

        MessageUtils.showInformationMessage("Projet créé avec succès !!!");
        myProjectBean.init();
    }      

    public void updateProject(UserGroupLabel userGroupLabel){
        
        if(ObjectUtils.isEmpty(userGroupLabel)) {
            MessageUtils.showErrorMessage("Aucun projet sélectioné !!!");
            return;              
        }

        if(StringUtils.isEmpty(userGroupLabel.getLabel())) {
            MessageUtils.showErrorMessage("Le label est obligatoire !!!");
            return;              
        }

        var userGroup = projectService.getUserGroupLabelByLabel(projectName);
        if(userGroup != null){
            MessageUtils.showErrorMessage("Ce nom de projet existe déjà !!!");
            init();
            return;             
        }

        projectService.saveNewProject(userGroupLabel);
        MessageUtils.showInformationMessage("Projet modifié avec succès !!!");
        myProjectBean.init();
        PrimeFaces.current().ajax().update("containerIndex");
    }      


    public void deleteProject(UserGroupLabel userGroupLabel) {

        projectService.deleteProject(userGroupLabel.getId());
        MessageUtils.showInformationMessage("Suppression OK :" + userGroupLabel.getLabel());

        currentUser.initUserPermissions();
        myProjectBean.init();
        init();
        PrimeFaces.current().ajax().update("containerIndex");
    }
}
