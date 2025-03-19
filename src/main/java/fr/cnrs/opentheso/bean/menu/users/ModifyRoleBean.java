package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.entites.UserRoleOnlyOn;
import fr.cnrs.opentheso.repositories.RoleRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository2;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.users.NodeUserRole;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;
import fr.cnrs.opentheso.repositories.UserRoleOnlyOnRepository;
import fr.cnrs.opentheso.services.ThesaurusService;
import fr.cnrs.opentheso.services.users.UserRoleGroupService;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.primefaces.PrimeFaces;


@Data
@SessionScoped
@NoArgsConstructor
@Named(value = "modifyRoleBean")
public class ModifyRoleBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private MyProjectBean myProjectBean;
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private UserGroupLabelRepository2 userGroupLabelRepository2;
    private UserRoleGroupRepository userRoleGroupRepository;
    private UserRoleOnlyOnRepository userRoleOnlyOnRepository;

    private ThesaurusService thesaurusService;
    private UserRoleGroupService userRoleGroupService;
    
    private User nodeSelectedUser, selectedUser;
    private String selectedProject, roleOfSelectedUser;

    // liste des (rôle -> projet) qui existent déjà pour l'utilisateur     
    private List<NodeUserRoleGroup> allMyRoleProject;
    
    // pour gérer les droits limités sur un ou plusieurs thésaurus
    private boolean limitOnTheso;    
    private List<NodeIdValue> listThesoOfProject, myAuthorizedRolesLimited;
    private List<String> selectedThesos; 
    private NodeUserRole selectedNodeUserRole;
    private List<NodeUserRole> listeLimitedThesoRoleForUser; // la liste des roles / thesos de l'utilisateur et du groupe avec des droits limités


    @Inject
    public ModifyRoleBean(MyProjectBean myProjectBean, RoleRepository roleRepository, UserRepository userRepository,
                          UserGroupLabelRepository2 userGroupLabelRepository2, UserRoleGroupRepository userRoleGroupRepository,
                          UserRoleOnlyOnRepository userRoleOnlyOnRepository,
                          ThesaurusService thesaurusService, UserRoleGroupService userRoleGroupService) {

        this.myProjectBean = myProjectBean;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userGroupLabelRepository2 = userGroupLabelRepository2;
        this.userRoleGroupRepository = userRoleGroupRepository;
        this.userRoleOnlyOnRepository = userRoleOnlyOnRepository;
        this.thesaurusService = thesaurusService;
        this.userRoleGroupService = userRoleGroupService;
    }

    /**
     * permet de selectionner l'utilisateur dans la liste avec toutes les informations nécessaires pour sa modification
     */
    public void selectUser(int idUser, int roleOfSelectedUser, String selectedProject) {
        nodeSelectedUser = userRepository.findById(idUser).get();
        this.selectedProject = selectedProject;

        this.roleOfSelectedUser = "" + roleOfSelectedUser;
        initAllMyRoleProject();
        
        limitOnTheso = false;
        listThesoOfProject = null;
        selectedThesos = null;    
        myAuthorizedRolesLimited = null;
    }
    
    /**
     * permet de selectionner l'utilisateur qui a des droits limités 
     * informations nécessaires pour sa modification
     */
    public void selectUserWithLimitedRole(NodeUserRole selectedNodeUserRole, String selectedProject) {
        this.selectedNodeUserRole = selectedNodeUserRole;
        this.selectedProject = selectedProject;
        limitOnTheso = true;
        myAuthorizedRolesLimited = null;
        selectedThesos = new ArrayList<>();

        List<NodeUserRole> nodeUserRoles = userRoleOnlyOnRepository.getListRoleByThesoLimited(Integer.parseInt(selectedProject), selectedNodeUserRole.getIdUser());
        for (NodeUserRole nodeUserRole1 : nodeUserRoles) {
            selectedThesos.add(nodeUserRole1.getIdTheso());
        }
        toogleLimitTheso();
        setLimitedRoleForThisUserByGroup();
    }    
    
    /**
     * permet de récupérer la liste des rôles pour l'utilisateur sur les thésaurus du projet
     */
    private void setLimitedRoleForThisUserByGroup(){

        if (StringUtils.isEmpty(selectedProject)) {
            return;
        }

        var idGroup = Integer.parseInt(selectedProject);

        if (ObjectUtils.isNotEmpty(selectedNodeUserRole)) {
            listeLimitedThesoRoleForUser = userRoleOnlyOnRepository.getListRoleByThesoLimited(idGroup, selectedNodeUserRole.getIdUser());
        } else if (ObjectUtils.isEmpty(selectedNodeUserRole) && ObjectUtils.isNotEmpty(listeLimitedThesoRoleForUser)) {
            listeLimitedThesoRoleForUser.clear(); //cas où on supprime l'utilisateur en cour
        }

        List<String> idThesosTemp = listeLimitedThesoRoleForUser.stream().map(element -> element.getIdTheso()).toList();
        List<NodeIdValue> allThesoOfProject = thesaurusService.getThesaurusOfProject(idGroup, workLanguage);

        listeLimitedThesoRoleForUser.addAll(allThesoOfProject.stream()
                .filter(element -> !idThesosTemp.contains(element.getId()))
                .map(element -> NodeUserRole.builder()
                        .idTheso(element.getId())
                        .thesoName(element.getValue())
                        .idRole(-1)
                        .roleName("")
                        .build())
                .toList());

        //myAuthorizedRolesLimited = List.of(NodeIdValue.builder().id("-1").value("").build());
        myAuthorizedRolesLimited = myProjectBean.getMyAuthorizedRoles();
    }

    /**
     * met à jour les rôles de l'utilisateur sur les thésaurus du projet ou 
     * si on redonne les droits sur le projet entier
     */
    public void updateLimitedRoleOnThesosForUser () {
        
        if(CollectionUtils.isEmpty(listeLimitedThesoRoleForUser))  {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun utilisateur sélectionné !!!");
            return;              
        }
        
        // suppression de tous les rôles
        var user = userRepository.findById(selectedNodeUserRole.getIdUser()).get();
        var userGroupLabel = userGroupLabelRepository2.findById(Integer.parseInt(selectedProject)).get();
        userRoleOnlyOnRepository.deleteByUserAndGroup(user, userGroupLabel);
        
        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            // ajout des rôles pour l'utilisateur sur les thésaurus
            for (NodeUserRole nodeThesoRole : listeLimitedThesoRoleForUser) {
                if(nodeThesoRole.getIdRole() != -1) {
                    var role = roleRepository.findById(nodeThesoRole.getIdRole()).get();
                    var thesaurus = thesaurusService.getThesaurusById(nodeThesoRole.getIdTheso());
                    userRoleOnlyOnRepository.save(UserRoleOnlyOn.builder()
                            .user(user)
                            .role(role)
                            .group(userGroupLabel)
                            .theso(thesaurus)
                            .build());
                }
            }
            myProjectBean.setSelectedIndex("2");
        } else {
            var roleSelected = StringUtils.isEmpty(roleOfSelectedUser) ? myProjectBean.getMyAuthorizedRoles().get(0).getId() : roleOfSelectedUser;
            userRoleGroupService.addUserRoleOnGroup(user.getId(), Integer.parseInt(roleSelected), Integer.parseInt(selectedProject));
            myProjectBean.setSelectedIndex("1");
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Le rôle a été changé avec succès !!!");
        myProjectBean.resetListUsers();
    }
    
    /**
     * met à jour les rôles de l'utilisateur sur les thésaurus du projet
     */
    public void updateUserRoleLimitedForSelectedUser() {
        
        if(selectedNodeUserRole == null) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun utilisateur sélectionné !!!");
            return;              
        }

        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            var user = userRepository.findById(selectedNodeUserRole.getIdUser()).get();
            var userGroupLabel = userGroupLabelRepository2.findById(Integer.parseInt(selectedProject)).get();

            userRoleOnlyOnRepository.deleteByUserAndGroup(user, userGroupLabel);

            userRoleGroupService.addUserRoleOnTheso(selectedNodeUserRole.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject), selectedThesos);
        } else {
            userRoleGroupService.updateUserRoleOnGroup(selectedNodeUserRole.getIdUser(),
                    Integer.parseInt(roleOfSelectedUser), Integer.parseInt(selectedProject));
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Le rôle a été changé avec succès !!!");
        myProjectBean.resetListUsers();
    }       
    
    /**
     * permet de supprimer le rôle de l'utilisateur sur ce thésaurus du projet
     */
    public void removeUserRoleOnTheso () {

        if(ObjectUtils.isEmpty(selectedNodeUserRole)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun rôle sélectionné !!!");
            return;              
        }

        var user = userRepository.findById(selectedNodeUserRole.getIdUser()).get();
        var role = roleRepository.findById(selectedNodeUserRole.getIdRole()).get();
        var group = userGroupLabelRepository2.findById(Integer.parseInt(selectedProject)).get();
        var thesaurus = thesaurusService.getThesaurusById(selectedNodeUserRole.getIdTheso());
        userRoleOnlyOnRepository.deleteByUserAndGroupAndRoleAndTheso(user, group, role, thesaurus);

        showMessage(FacesMessage.SEVERITY_INFO, "Le rôle a été supprimé !!!");
        myProjectBean.resetListLimitedRoleUsers();
    }      
    
    private void initAllMyRoleProject(){
        allMyRoleProject = userRoleGroupRepository.getUserRoleGroup(nodeSelectedUser.getId());
    }
   
    public void toogleLimitTheso(){
        if(!limitOnTheso) return;
        try {
            listThesoOfProject = thesaurusService.getThesaurusOfProject(Integer.parseInt(selectedProject), workLanguage);
        } catch (Exception e) { }
    }  
    
    /**
     * met à jour le nouveau rôle de l'utilisateur sur le projet
     */
    public void updateRoleForSelectedUser () {

        if(ObjectUtils.isEmpty(nodeSelectedUser)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun utilisateur sélectionné !!!");
            return;
        }

        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            userRoleGroupService.addUserRoleOnTheso(nodeSelectedUser.getId(), Integer.parseInt(roleOfSelectedUser), Integer.parseInt(selectedProject), selectedThesos);
            myProjectBean.setSelectedIndex("2");
        } else {
            userRoleGroupService.updateUserRoleOnGroup(nodeSelectedUser.getId(), Integer.parseInt(roleOfSelectedUser), Integer.parseInt(selectedProject));
            myProjectBean.setSelectedIndex("1");
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Le rôle a été changé avec succès !!!");
        myProjectBean.resetListUsers();
    }

    public void removeUserFromProject () {
        
        if(ObjectUtils.isEmpty(nodeSelectedUser)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun utilisateur sélectionné !!!");
            return;              
        }

        var user = userRepository.findById(nodeSelectedUser.getId()).get();
        var userGroupLabel = userGroupLabelRepository2.findById(Integer.parseInt(selectedProject)).get();
        userRoleGroupRepository.deleteByUserAndGroup(user, userGroupLabel);

        showMessage(FacesMessage.SEVERITY_INFO, "L'utilisateur a été supprimé du projet !!!");
        myProjectBean.resetListUsers();
    }    

    public void addUserToProject () {
        
        if(ObjectUtils.isEmpty(selectedUser)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun utilisateur sélectionné !!!");
            return;              
        }

        userRoleGroupService.addUserRoleOnGroup(selectedUser.getId(), Integer.parseInt(roleOfSelectedUser), Integer.parseInt(selectedProject));

        showMessage(FacesMessage.SEVERITY_INFO, "L'utilisateur a été ajouté avec succès !!!");
        myProjectBean.resetListUsers();

        PrimeFaces.current().ajax().update("containerIndex");
    }      

    public List<User> autoCompleteUser(String userName) {
        return userRepository.findAllByUsernameLike("%" + userName + "%s");
    }

    private void showMessage(FacesMessage.Severity type, String message) {
        var msg = new FacesMessage(type, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }
}
