package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository2;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.users.UserRoleGroupService;import jakarta.inject.Named;

import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "newProjectBean")
@SessionScoped
public class NewProjectBean implements Serializable {
    
    @Autowired
    private MyProjectBean myProjectBean;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private UserGroupLabelRepository2 userGroupLabelRepository;

    @Autowired
    private UserRoleGroupService userRoleGroupService;
 
    private String projectName;
    private List<UserGroupLabel> listeProjectOfUser;

    
    /**
     * permet d'initialiser les variables 
     *
     */
    public void init() {
        projectName = null;
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            listeProjectOfUser = userGroupLabelRepository.findAll();
            return;
        }
        listeProjectOfUser = userGroupLabelRepository.findProjectsByRole(currentUser.getNodeUser().getIdUser(), 2);
    }   
    
    /**
     * permet de créer un nouveau projet de regroupement de thésaurus, 
     * si l'utilisateur est SuperAdmin, le projet n'aura pas d'utilisateur pas défaut,
     * si l'utilisateur est Admin, il aura les droits admin dessus par défaut
     */
    public void addNewProject(){
        FacesMessage msg;
        
        if(projectName== null || projectName.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un label est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        
        if(userHelper.isUserGroupExist(projectName)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Ce nom de projet existe déjà !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }        
        if(!userHelper.addNewProject(projectName)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }
        currentUser.initUserPermissions();
        // on vérifie si l'utilisateur en cours est un Admin 
        if(!currentUser.getNodeUser().isSuperAdmin()){
            if(currentUser.getAllAuthorizedProjectAsAdmin() != null && !currentUser.getAllAuthorizedProjectAsAdmin().isEmpty()) {
                // on donne le droit admin pour l'utilisateur courant sur ce groupe
                int projectId = userHelper.getThisProjectId(projectName);
                if(projectId == -1) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur interne BDD !!!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    return;   
                }
                userRoleGroupService.addUserRoleOnGroup(currentUser.getNodeUser().getIdUser(), 2, projectId);
            }
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Projet créé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.init();
    }      

    public void updateProject(UserGroupLabel userGroupLabel){
        FacesMessage msg;
        
        if(userGroupLabel== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de projet sélectioné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }   
        if(userGroupLabel.getLabel().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le label est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        
        if(userHelper.isUserGroupExist(userGroupLabel.getLabel())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Ce nom de projet existe déjà !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            init();
            return;             
        }          
        if(!userHelper.updateProject(userGroupLabel.getLabel(), userGroupLabel.getId())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de modification !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Projet modifié avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.init();
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }     
    }      


    public void deleteProject(UserGroupLabel userGroupLabel) {
        FacesMessage msg;

        if (!userHelper.deleteProjectGroup(userGroupLabel.getId())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur :", userGroupLabel.getLabel()));
            return;
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Suppression OK :", userGroupLabel.getLabel()));
        currentUser.initUserPermissions();
        myProjectBean.init();
        init();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }        
    }
}
