package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository2;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.users.UserRoleGroupService;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


/**
 *
 * @author miledrousset
 */
@Data
@SessionScoped
@NoArgsConstructor
@Named(value = "newProjectBean")
public class NewProjectBean implements Serializable {

    private MyProjectBean myProjectBean;
    private CurrentUser currentUser;
    private UserGroupThesaurusRepository userGroupThesaurusRepository;
    private UserGroupLabelRepository2 userGroupLabelRepository;
    private UserRoleGroupService userRoleGroupService;
 
    private String projectName;
    private List<UserGroupLabel> listeProjectOfUser;


    @Inject
    public NewProjectBean(MyProjectBean myProjectBean, CurrentUser currentUser,
                          UserGroupThesaurusRepository userGroupThesaurusRepository,
                          UserGroupLabelRepository2 userGroupLabelRepository,
                          UserRoleGroupService userRoleGroupService) {

        this.myProjectBean = myProjectBean;
        this.currentUser = currentUser;
        this.userGroupThesaurusRepository = userGroupThesaurusRepository;
        this.userGroupLabelRepository = userGroupLabelRepository;
        this.userRoleGroupService = userRoleGroupService;
    }

    public void init() {
        projectName = null;
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            listeProjectOfUser = userGroupLabelRepository.findAll();
            return;
        }
        listeProjectOfUser = userGroupLabelRepository.findProjectsByRole(currentUser.getNodeUser().getIdUser(), 2);
    }   
    
    /**
     * Permet de créer un nouveau projet de regroupement de thésaurus,
     * si l'utilisateur est SuperAdmin, le projet n'aura pas d'utilisateur pas défaut,
     * si l'utilisateur est Admin, il aura les droits admin dessus par défaut
     */
    public void addNewProject(){
        
        if(StringUtils.isEmpty(projectName)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le label est obligatoire !!!");
            return;              
        }

        var userGroup = userGroupLabelRepository.findByLabelLike(projectName);
        if(userGroup.isPresent()){
            showMessage(FacesMessage.SEVERITY_ERROR, "Le label existe déjà !!!");
            return;             
        }

        var userGroupCreated = userGroupLabelRepository.save(UserGroupLabel.builder().label(projectName).build());

        currentUser.initUserPermissions();
        // on vérifie si l'utilisateur en cours est un Admin 
        if(!currentUser.getNodeUser().isSuperAdmin() && CollectionUtils.isNotEmpty(currentUser.getAllAuthorizedProjectAsAdmin())){
            // on donne le droit admin pour l'utilisateur courant sur ce groupe
            userRoleGroupService.addUserRoleOnGroup(currentUser.getNodeUser().getIdUser(), 2, userGroupCreated.getId());
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Projet créé avec succès !!!");
        myProjectBean.init();
    }      

    public void updateProject(UserGroupLabel userGroupLabel){
        
        if(ObjectUtils.isEmpty(userGroupLabel)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun projet sélectioné !!!");
            return;              
        }

        if(StringUtils.isEmpty(userGroupLabel.getLabel())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le label est obligatoire !!!");
            return;              
        }

        var groupLabel = userGroupLabelRepository.findByLabelLike(userGroupLabel.getLabel());
        if(groupLabel.isPresent()){
            showMessage(FacesMessage.SEVERITY_ERROR, "Ce nom de projet existe déjà !!!");
            init();
            return;             
        }

        userGroupLabelRepository.save(userGroupLabel);
        showMessage(FacesMessage.SEVERITY_INFO, "Projet modifié avec succès !!!");
        myProjectBean.init();
        PrimeFaces.current().ajax().update("containerIndex");
    }      


    public void deleteProject(UserGroupLabel userGroupLabel) {

        userRoleGroupService.deleteRoleByIdGroup(userGroupLabel.getId());
        userGroupThesaurusRepository.deleteByIdGroup(userGroupLabel.getId());
        userGroupLabelRepository.deleteById(userGroupLabel.getId());

        showMessage(FacesMessage.SEVERITY_INFO, "Suppression OK :" + userGroupLabel.getLabel());

        currentUser.initUserPermissions();
        myProjectBean.init();
        init();

        PrimeFaces.current().ajax().update("containerIndex");
    }

    private void showMessage(FacesMessage.Severity type, String message) {
        var msg = new FacesMessage(type, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }
}
