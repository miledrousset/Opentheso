package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.entites.Roles;
import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.repositories.RoleRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.services.ThesaurusService;
import fr.cnrs.opentheso.services.UserRoleGroupService;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import fr.cnrs.opentheso.bean.profile.SuperAdminBean;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import liquibase.util.StringUtil;
import org.primefaces.PrimeFaces;


@Data
@SessionScoped
@NoArgsConstructor
@Named(value = "newUserBean")
public class NewUserBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private MyProjectBean myProjectBean;
    private SuperAdminBean superAdminBean;
    private UserGroupLabelRepository userGroupLabelRepository;
    private RoleRepository roleRepository;
    private UserRepository userRepository;

    private ThesaurusService thesaurusService;
    private UserRoleGroupService userRoleGroupService;

    private NodeUser nodeUser;
    private boolean limitOnTheso;
    private String passWord1, passWord2, selectedProject, selectedRole, name;
    private List<UserGroupLabel> nodeAllProjects;
    private List<Roles> nodeAllRoles;
    private List<NodeIdValue> listThesoOfProject;
    private List<String> selectedThesos;
    

    @Inject
    public NewUserBean(MyProjectBean myProjectBean, SuperAdminBean superAdminBean,
                       UserGroupLabelRepository userGroupLabelRepository,
                       RoleRepository roleRepository,
                       UserRepository userRepository,
                       ThesaurusService thesaurusService,
                       UserRoleGroupService userRoleGroupService) {

        this.myProjectBean = myProjectBean;
        this.superAdminBean = superAdminBean;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.thesaurusService = thesaurusService;
        this.userRoleGroupService = userRoleGroupService;
        this.userGroupLabelRepository = userGroupLabelRepository;
    }

    public void init(String selectedProject) {
        nodeUser = new NodeUser();
        passWord1 = null;
        passWord2 = null;
        this.selectedProject = selectedProject;
        selectedRole = null;
        limitOnTheso = false; 
        listThesoOfProject = null;
        selectedThesos = null;
    }   
    
    public void initForSuperAdmin() {
        nodeUser = new NodeUser();
        passWord1 = null;
        passWord2 = null;
        selectedRole = null;
        listThesoOfProject = null;
        selectedThesos = null;
        limitOnTheso = false;

        nodeAllProjects = userGroupLabelRepository.findAll();
        nodeAllProjects.sort(Comparator.comparing(UserGroupLabel::getLabel, String.CASE_INSENSITIVE_ORDER));
        nodeAllRoles = roleRepository.findAll();

        selectedProject = CollectionUtils.isNotEmpty(nodeAllProjects) ? nodeAllProjects.get(0).getId().toString() : null;
    }
    
    public void toogleLimitTheso(){
        if(!limitOnTheso) return;
        /// récupérer la liste des thésaurus d'un projet
        int idProject = -1;
        try {
            idProject = Integer.parseInt(selectedProject);
        } catch (Exception e) {
            return;
        }
        if(idProject == -1) return;
        listThesoOfProject = thesaurusService.getThesaurusOfProject(idProject, workLanguage);
    }
    
    public void addUser(boolean bySuperAdmin){

        if(checkUserDatas()) {
            var userCreated = saveUser();
            saveRole(userCreated);
            showMessage(FacesMessage.SEVERITY_INFO, "Utilisateur créé avec succès !!!");

            if (bySuperAdmin) {
                superAdminBean.init();
            } else {
                myProjectBean.setLists();
            }

            PrimeFaces.current().executeScript("PF('newUserForProject').hide();");
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }

    private User saveUser() {
        var user = User.builder()
                .mail(nodeUser.getMail())
                .username(nodeUser.getName())
                .password(MD5Password.getEncodedPassword(passWord1))
                .isSuperAdmin(nodeUser.isSuperAdmin())
                .alertMail(nodeUser.isAlertMail())
                .isServiceAccount(nodeUser.isServiceAccount())
                .active(true)
                .keyNeverExpire(false)
                .passToModify(false)
                .build();
        var userCreated = userRepository.save(user);
        if(ObjectUtils.isEmpty(userCreated)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant la création de l'utilisateur !!!");
            return null;
        }
        return userCreated;
    }

    private boolean checkUserDatas() {
        if(ObjectUtils.isEmpty(nodeUser)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun utilisateur à ajouter !!!");
            return false;
        }

        if(StringUtil.isEmpty(nodeUser.getName())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le pseudo est obligatoire !!!");
            return false;
        }

        if(StringUtil.isEmpty(passWord1)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le mot de passe est obligatoire !!!");
            return false;
        }

        if(StringUtil.isEmpty(passWord2)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le mot de passe est obligatoire !!!");
            return false;
        }
        if(!passWord1.equals(passWord2)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le mot de passe n'est pas identique !!!");
            return false;
        }

        if(userRepository.findByMail(nodeUser.getMail()).isPresent()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Email existe déjà !!!");
            return false;
        }

        nodeUser.setName(nodeUser.getName().trim());
        if(userRepository.findByMail(nodeUser.getName()).isPresent()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Pseudo existe déjà !!!");
            return false;
        }

        if(StringUtil.isEmpty(selectedRole)) {
            nodeUser.setSuperAdmin(false);
            selectedProject = null;
        } else {
            try {
                nodeUser.setSuperAdmin(Integer.parseInt(selectedRole) == 1);
            } catch (Exception e) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Role non reconnu !!!");
                return false;
            }
        }

        return true;
    }

    private void saveRole(User userCreated) {

        if(StringUtil.isNotEmpty(selectedProject) && StringUtil.isNotEmpty(selectedRole)){
            if (limitOnTheso) {
                userRoleGroupService.addUserRoleOnTheso(userCreated.getId(), Integer.parseInt(selectedRole),
                        Integer.parseInt(selectedProject), selectedThesos);
            } else {
                userRoleGroupService.addUserRoleOnGroup(userCreated.getId(), Integer.parseInt(selectedRole),
                        Integer.parseInt(selectedProject));
            }
        }
    }

    private void showMessage(FacesMessage.Severity type, String message) {
        var msg = new FacesMessage(type, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }
}
