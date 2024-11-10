package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.users.NodeUserRole;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.models.userpermissions.NodeThesoRole;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;


@Data
@Named(value = "modifyRoleBean")
@SessionScoped
public class ModifyRoleBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    @Autowired @Lazy private MyProjectBean myProjectBean;

    @Autowired
    private UserHelper userHelper;
    
    private NodeUser nodeSelectedUser;
    private String selectedProject;
    private String roleOfSelectedUser;

    // pour l'ajout d'un utilisateur existant au projet
    private NodeUser selectedUser;

    // liste des (rôle -> projet) qui existent déjà pour l'utilisateur     
    ArrayList<NodeUserRoleGroup> allMyRoleProject;
    
    // pour gérer les droits limités sur un ou plusieurs thésaurus
    private boolean limitOnTheso;    
    private ArrayList<NodeIdValue> listThesoOfProject;
    private List<String> selectedThesos; 
    private NodeUserRole selectedNodeUserRole;
    private ArrayList<NodeThesoRole> listeLimitedThesoRoleForUser; // la liste des roles / thesos de l'utilisateur et du groupe avec des droits limités       
    private ArrayList<NodeIdValue> myAuthorizedRolesLimited;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(allMyRoleProject!= null){
            allMyRoleProject.clear();
            allMyRoleProject = null;
        }
        nodeSelectedUser = null;
        selectedProject = null;
        roleOfSelectedUser = null;
        selectedUser = null;      
        limitOnTheso = false;
        listThesoOfProject = null;
        selectedThesos = null;    
        myAuthorizedRolesLimited = null;
    }   
    
    public ModifyRoleBean() {
    }

    public void setSelectedProject(String selectedProject) {
        this.selectedProject = selectedProject;
    }
    
    
    /**
     * permet de selectionner l'utilisateur dans la liste avec toutes les
     * informations nécessaires pour sa modification
     *
     * @param idUser
     * @param roleOfSelectedUser
     * @param selectedProject
     */
    public void selectUser(int idUser, int roleOfSelectedUser, String selectedProject) {
        nodeSelectedUser = userHelper.getUser(idUser);
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
     *
     * @param selectedNodeUserRole
     * @param selectedProject
     */
    public void selectUserWithLimitedRole(NodeUserRole selectedNodeUserRole, String selectedProject) {
        this.selectedNodeUserRole = selectedNodeUserRole;
        this.selectedProject = selectedProject;
        limitOnTheso = true;
        myAuthorizedRolesLimited = null;
        selectedThesos = new ArrayList<>();
        ArrayList<NodeUserRole> nodeUserRoles = userHelper.getListRoleByThesoLimited(Integer.parseInt(selectedProject), selectedNodeUserRole.getIdUser());
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
        if (selectedProject == null || selectedProject.isEmpty()) {
            return;
        }

        int idGroup = Integer.parseInt(selectedProject);
        if (selectedNodeUserRole != null) {
            listeLimitedThesoRoleForUser = userHelper.getAllRolesThesosByUserGroupLimited(
                    idGroup, selectedNodeUserRole.getIdUser());
        } else {
            if (listeLimitedThesoRoleForUser != null) {
                listeLimitedThesoRoleForUser.clear(); //cas où on supprime l'utilisateur en cours
            }
        }
        ArrayList<String> idThesosTemp = new ArrayList<>();
        for (NodeThesoRole nodeThesoRole : listeLimitedThesoRoleForUser) {
            idThesosTemp.add(nodeThesoRole.getIdTheso());
        }

        ArrayList<NodeIdValue> allThesoOfProject = userHelper.getThesaurusOfProject(idGroup, workLanguage);
        for (NodeIdValue nodeIdValue : allThesoOfProject) {
            if(!idThesosTemp.contains(nodeIdValue.getId())){
                NodeThesoRole nodeThesoRole = new NodeThesoRole();
                nodeThesoRole.setIdTheso(nodeIdValue.getId());
                nodeThesoRole.setThesoName(nodeIdValue.getValue());
                nodeThesoRole.setIdRole(-1);
                nodeThesoRole.setRoleName("");
                
                listeLimitedThesoRoleForUser.add(nodeThesoRole);
            }    
        }
        
        myAuthorizedRolesLimited = myProjectBean.getMyAuthorizedRoles();
        NodeIdValue nodeIdValue = new NodeIdValue();
        nodeIdValue.setId("-1");
        nodeIdValue.setValue("");
        myAuthorizedRolesLimited.add(0,nodeIdValue);
    }      
    
    public void setSelectedRoleLimitedForTheso(){
    }

    /**
     * met à jour les rôles de l'utilisateur sur les thésaurus du projet ou 
     * si on redonne les droits sur le projet entier
     */
    public void updateLimitedRoleOnThesosForUser () {
        FacesMessage msg;
        
        if(listeLimitedThesoRoleForUser == null || listeLimitedThesoRoleForUser.isEmpty())  {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        
        // suppression de tous les rôles
        if(!userHelper.deleteAllUserRoleOnTheso(selectedNodeUserRole.getIdUser(), Integer.parseInt(selectedProject))){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;                   
        }
        
        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            // ajout des rôles pour l'utilisateur sur les thésaurus
            for (NodeThesoRole nodeThesoRole : listeLimitedThesoRoleForUser) {
                if(nodeThesoRole.getIdRole() != -1) {
                    if(!userHelper.addUserRoleOnThisTheso(
                            selectedNodeUserRole.getIdUser(), nodeThesoRole.getIdRole(),
                            Integer.parseInt(selectedProject), nodeThesoRole.getIdTheso())){
                        return;
                    }
                }
            }
            myProjectBean.setSelectedIndex("2");
        } else {
            if(!userHelper.addUserRoleOnGroup(nodeSelectedUser.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject))) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;             
            }
            myProjectBean.setSelectedIndex("1");
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }
    
    /**
     * met à jour les rôles de l'utilisateur sur les thésaurus du projet
     */
    public void updateUserRoleLimitedForSelectedUser () {
        FacesMessage msg;
        
        if(selectedNodeUserRole == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            if(!userHelper.deleteAllUserRoleOnTheso(selectedNodeUserRole.getIdUser(), Integer.parseInt(selectedProject))){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la modification des rôles !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;                    
            }
            if(!userHelper.addUserRoleOnTheso(
                    selectedNodeUserRole.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject), selectedThesos)){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la modification des rôles !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;                
            }
        } else {
            if(!userHelper.updateUserRoleOnGroup(nodeSelectedUser.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject))) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;             
            }
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }       
    
    /**
     * permet de supprimer le rôle de l'utilisateur sur ce thésaurus du projet
     */
    public void removeUserRoleOnTheso () {
        FacesMessage msg;
        
        if(selectedNodeUserRole == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de rôle sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        if(!userHelper.deleteUserRoleOnTheso(
                selectedNodeUserRole.getIdUser(),
                selectedNodeUserRole.getIdRole(),
                Integer.parseInt(selectedProject),
                selectedNodeUserRole.getIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de suppression du rôle de l'utilisateur pour ce thésaurus !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été supprimé !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListLimitedRoleUsers();
    }      
    
    private void initAllMyRoleProject(){
        allMyRoleProject = userHelper.getUserRoleGroup(nodeSelectedUser.getIdUser());
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
        listThesoOfProject = userHelper.getThesaurusOfProject(idProject, workLanguage);
    }  
    
    /**
     * met à jour le nouveau rôle de l'utilisateur sur le projet
     */
    public void updateRoleForSelectedUser () {
        FacesMessage msg;
        
        if(nodeSelectedUser== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            if(!userHelper.addUserRoleOnTheso(
                    nodeSelectedUser.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject), selectedThesos)){
                return;
            }
            myProjectBean.setSelectedIndex("2");
        } else {
            if(!userHelper.updateUserRoleOnGroup(nodeSelectedUser.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject))) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;             
            }
            myProjectBean.setSelectedIndex("1");
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }
    
      
   
    /**
     * permet de supprimer l'utilisateur du projet
     */
    public void removeUserFromProject () {
        FacesMessage msg;
        
        if(nodeSelectedUser== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        if(!userHelper.deleteRoleOnGroup(nodeSelectedUser.getIdUser(), Integer.parseInt(selectedProject))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de suppression de l'utilisateur du projet !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'utilisateur a été supprimé du projet !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }    
    
    /**
     * permet d'ajouter un utilisateur existant au projet
     */
    public void addUserToProject () {
        FacesMessage msg;
        
        if(selectedUser == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        if(!userHelper.addUserRoleOnGroup(selectedUser.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                Integer.parseInt(selectedProject))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'utilisateur a été ajouté avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
            
        }
    }      

    public ArrayList<NodeUser> autoCompleteUser(String userName) {
        ArrayList <NodeUser> nodeUsers = userHelper.searchUser(userName);
        return nodeUsers;
    }
}
