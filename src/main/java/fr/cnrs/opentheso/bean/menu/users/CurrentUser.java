package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.models.userpermissions.NodeProjectThesoRole;
import fr.cnrs.opentheso.models.userpermissions.NodeThesoRole;
import fr.cnrs.opentheso.models.userpermissions.UserPermissions;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.MenuBean;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ProjectBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.search.SearchBean;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;
import fr.cnrs.opentheso.services.connexion.LdapService;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Data
@SessionScoped
@Named(value = "currentUser")
public class CurrentUser implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    @Autowired @Lazy
    private RoleOnThesoBean roleOnThesoBean;
    @Autowired @Lazy
    private ViewEditorHomeBean viewEditorHomeBean;
    @Autowired @Lazy
    private IndexSetting indexSetting;
    @Autowired @Lazy
    private MenuBean menuBean;
    @Autowired @Lazy
    private RightBodySetting rightBodySetting;
    @Autowired @Lazy
    private PropositionBean propositionBean;
    @Autowired @Lazy
    private SearchBean searchBean;
    @Autowired @Lazy
    private LanguageBean languageBean;
    @Autowired @Lazy
    private SelectedTheso selectedTheso;
    @Autowired @Lazy
    private ProjectBean projectBean;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private UserGroupLabelRepository userGroupLabelRepository;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    private NodeUser nodeUser;
    private String username;
    private String password;
    private boolean ldapEnable = false;

    private ArrayList<NodeUserRoleGroup> allAuthorizedProjectAsAdmin;
    
    // nouvel objet pour gérer les permissions
    private UserPermissions userPermissions;


    public void clear() {
        if (allAuthorizedProjectAsAdmin != null) {
            allAuthorizedProjectAsAdmin.clear();
            allAuthorizedProjectAsAdmin = null;
        }
        nodeUser = null;
        username = null;
        password = null;
    }

    public void disconnect() throws IOException {

        if(nodeUser == null) return;

        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("connect.goodbye"), nodeUser.getName());
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);

        nodeUser = null;
        

        // initialisation des permissions
        resetPermissionsAfterLogout();
        
        // tester si le thésaurus en cours est privé, alors après une déconnexion, on devrait plus l'afficher
        roleOnThesoBean.setAndClearThesoInAuthorizedList();
        indexSetting.setIsThesoActive(true);
        rightBodySetting.setIndex("0");

        viewEditorHomeBean.reset();

        selectedTheso.loadProject();
        selectedTheso.setSelectedProject();

        if ("-1".equals(selectedTheso.getProjectIdSelected())) {
            roleOnThesoBean.setPublicThesos(this);
            if(StringUtils.isNotEmpty(selectedTheso.getCurrentIdTheso())){
                if (!thesaurusHelper.isThesoPrivate(selectedTheso.getCurrentIdTheso())) {
                    indexSetting.setSelectedTheso(true);
                } else {
                    selectedTheso.setCurrentIdTheso(null);
                    indexSetting.setSelectedTheso(false);
                }
                indexSetting.setProjectSelected(false);
            }
        } else if (selectedTheso.getProjectsList().stream()
                .filter(element -> element.getId() == Integer.parseInt(selectedTheso.getProjectIdSelected()))
                .findFirst().isEmpty()) {
            selectedTheso.setProjectIdSelected("-1");
            indexSetting.setProjectSelected(false);
            selectedTheso.setSelectedIdTheso(null);
            indexSetting.setSelectedTheso(false);
        } else {
            if (!projectBean.getListeThesoOfProject().isEmpty()) {
                roleOnThesoBean.setAuthorizedTheso(projectBean.getListeThesoOfProject().stream()
                        .map(NodeIdValue::getId)
                        .collect(Collectors.toList()));
            } else {
                roleOnThesoBean.setAuthorizedTheso(Collections.emptyList());
            }
            roleOnThesoBean.addAuthorizedThesoToHM();
            roleOnThesoBean.setUserRoleOnThisTheso(this);

            if (StringUtils.isNotEmpty(selectedTheso.getCurrentIdTheso())
                    && thesaurusHelper.isThesoPrivate(selectedTheso.getCurrentIdTheso())) {
                indexSetting.setSelectedTheso(true);
                indexSetting.setProjectSelected(false);
            }
        }
        
        if (!"index".equals(menuBean.getActivePageName())) {
            menuBean.redirectToThesaurus();
        } else {
            PrimeFaces.current().ajax().update("containerIndex:contentConcept");
            PrimeFaces.current().ajax().update("containerIndex:searchBar");
            PrimeFaces.current().ajax().update("containerIndex:header");
            PrimeFaces.current().ajax().update("menuBar");
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }

    /**
     * Connect l'utilisateur si son compte en récupérant toutes les informations
     * lui concernant
     *
     * le lien de l'index si le compte existe, un message d'erreur sinon init
     * c'est une parametre que viens du "isUserExist" ou return une 1 si on fait
     * le login normal (usuaire, pass), une 2 si on fait le login avec le
     * motpasstemp (et nous sommes dirigées a la page web de changer le
     * motpasstemp) #MR
     *
     * @throws java.io.IOException
     */
    public void login() throws Exception {

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            showErrorMessage("champ vide non autorisé");
            return;
        }

        int idUser;
        if (ldapEnable) {
            if (!ldapService.authentificationLdapCheck(username, password)) {
                showErrorMessage("User or password LDAP wrong, please try again");
                return;
            }
            idUser = userHelper.getIdUserFromPseudo(username);
        } else {
            idUser = userHelper.getIdUser(username, MD5Password.getEncodedPassword(password));
        }

        if (idUser == -1) {
            showErrorMessage("User or password wrong, please try again");
            return;
        }

        // on récupère le compte de l'utilisatreur
        nodeUser = userHelper.getUser(idUser);
        if (nodeUser == null) {
            showErrorMessage("Incohérence base de données ou utilisateur n'existe pas");
            return;
        }

        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("connect.welcome"), username);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);

        if ("index".equals(menuBean.getActivePageName())) {
            menuBean.setNotificationPannelVisible(true);
        }

        setInfos();
        //// Nouvelle gestion des droits pour l'utilisateur avec l'Objet UserPermissions

        
        
        if ("2".equals(rightBodySetting.getIndex())) {
            rightBodySetting.setIndex("0");
        }

        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);

        selectedTheso.loadProject();

        if ("-1".equals(selectedTheso.getProjectIdSelected()) || StringUtils.isEmpty(selectedTheso.getProjectIdSelected())) {
            indexSetting.setProjectSelected(false);
            if(!StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())){
                if (!thesaurusHelper.isThesoPrivate(selectedTheso.getCurrentIdTheso())) {
                    indexSetting.setSelectedTheso(true);
                } else {
                    selectedTheso.setCurrentIdTheso(null);
                    indexSetting.setSelectedTheso(false);
                }
            }
        } else {
            projectBean.initProject(selectedTheso.getProjectIdSelected(), this);

            if (!projectBean.getListeThesoOfProject().isEmpty()) {
                roleOnThesoBean.setAuthorizedTheso(projectBean.getListeThesoOfProject().stream()
                        .map(NodeIdValue::getId)
                        .collect(Collectors.toList()));
            } else {
                roleOnThesoBean.setAuthorizedTheso(Collections.emptyList());
            }
            roleOnThesoBean.addAuthorizedThesoToHM();
            roleOnThesoBean.setUserRoleOnThisTheso(this);
            projectBean.init();
        }

        PrimeFaces.current().executeScript("PF('login').hiden();");
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("idLogin");
            pf.ajax().update("containerIndex:header");
        }
    }
  
    /**
     * initialisation des permissions suivant l'utilisateur connecté 
     */
    public void initUserPermissions(){
        if(nodeUser == null) return;
        if(userPermissions == null){
            userPermissions = new UserPermissions();
        }

        // liste des projets de l'utilisateur
        if (nodeUser.isSuperAdmin()) {
            userPermissions.setListProjects(userGroupLabelRepository.getAllProjects());
        } else {  
            userPermissions.setListProjects(userHelper.getProjectOfUser(nodeUser.getIdUser()));
            setListProjectForUser();
        }
        setAllListThesoOfAllProject();
    }
    
    private void setAllListThesoOfAllProject(){
        // liste des thésaurus de l'utilisateur (tous les droits en partant du contributeur)
        if (nodeUser.isSuperAdmin()) {
            userPermissions.setListThesos(thesaurusHelper.getAllTheso(true));
            userPermissions.setRole(1);
            userPermissions.setRoleName("superAdmin");   
            if(userPermissions.getSelectedProject() != -1) {
                initUserPermissionsForThisProject(userPermissions.getSelectedProject());
            }
        } else {  
            // les projets de l'utilisateurs avec les roles sur les thésaurus par projet
            List<NodeProjectThesoRole> nodeProjectThesoRoles = new ArrayList<>();

            for (UserGroupLabel userGroupLabel : userPermissions.getListProjects()) {
                NodeProjectThesoRole nodeProjectThesoRole = new NodeProjectThesoRole();
                nodeProjectThesoRole.setIdProject(userGroupLabel.getId()); // id du projet
                nodeProjectThesoRole.setProjectName(userGroupLabel.getLabel()); // label du projet

                List<NodeThesoRole> nodeThesoRoles = userHelper.getAllRolesThesosByUserGroup(nodeProjectThesoRole.getIdProject(), nodeUser.getIdUser());

                nodeProjectThesoRole.setNodeThesoRoles(nodeThesoRoles);
                nodeProjectThesoRoles.add(nodeProjectThesoRole);
            }
            userPermissions.setNodeProjectsWithThesosRoles(nodeProjectThesoRoles);
            setListThesoForUser();
        }        
    }
    
    private void setListProjectForUser(){
        if(userPermissions.getSelectedProject() == -1) return;

        for (UserGroupLabel userGroupLabel : userPermissions.getListProjects()) {
            if(userPermissions.getSelectedProject() == userGroupLabel.getId()) return;
        }
        resetUserPermissionsForThisProject();
        resetSelectedTheso();
    }  

    private void resetSelectedTheso(){
        userPermissions.setSelectedTheso(null);
        userPermissions.setSelectedThesoName("");
        userPermissions.setPreferredLangOfSelectedTheso(null);
        userPermissions.setListLangsOfSelectedTheso(null);
        userPermissions.setProjectOfselectedTheso(-1);
        userPermissions.setProjectOfselectedThesoName("");
    }
    
    private void setListThesoForUser(){
        boolean resetTheso = true;
        ArrayList<NodeIdValue> thesos = new ArrayList<>();
        for (NodeProjectThesoRole nodeProjectsWithThesosRole : userPermissions.getNodeProjectsWithThesosRoles()) {
            if(userPermissions.getSelectedProject() == -1) {
                for (NodeThesoRole nodeThesoRole : nodeProjectsWithThesosRole.getNodeThesoRoles()) {
                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId(nodeThesoRole.getIdTheso());
                    nodeIdValue.setValue(nodeThesoRole.getThesoName());
                    if(!StringUtils.isEmpty(userPermissions.getSelectedTheso())) {
                        if(userPermissions.getSelectedTheso().equalsIgnoreCase(nodeThesoRole.getIdTheso())) {
                            resetTheso = false;
                        }
                    }
                    thesos.add(nodeIdValue);
                } 
            }  else {          
                if(userPermissions.getSelectedProject() == nodeProjectsWithThesosRole.getIdProject()) {
                    for (NodeThesoRole nodeThesoRole : nodeProjectsWithThesosRole.getNodeThesoRoles()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(nodeThesoRole.getIdTheso());
                        nodeIdValue.setValue(nodeThesoRole.getThesoName());
                        if(!StringUtils.isEmpty(userPermissions.getSelectedTheso())) {
                            if(userPermissions.getSelectedTheso().equalsIgnoreCase(nodeThesoRole.getIdTheso())) {
                                resetTheso = false;
                            }
                        }
                        thesos.add(nodeIdValue);
                    } 
                }
            }
        }

        userPermissions.setListThesos(thesos);
        if(resetTheso) {
            resetUserPermissionsForThisTheso();
            resetUserPermissionsForThisProject();
        }
        else
            initUserPermissionsForThisTheso(userPermissions.getSelectedTheso());
    }
    
    public void initUserPermissionsForThisTheso(String idTheso){
        if(userPermissions == null){
            userPermissions = new UserPermissions();
        }
        if(StringUtils.isEmpty(idTheso)) {
            resetUserPermissionsForThisTheso();
            return;
        }

        int idProject, idRole; 
        userPermissions.setSelectedTheso(idTheso);
        userPermissions.setPreferredLangOfSelectedTheso(preferencesHelper.getWorkLanguageOfTheso(selectedTheso.getCurrentIdTheso()));
        userPermissions.setSelectedThesoName(thesaurusHelper.getTitleOfThesaurus(idTheso, userPermissions.getPreferredLangOfSelectedTheso()));
        
        
        userPermissions.setListLangsOfSelectedTheso(thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(
                selectedTheso.getCurrentIdTheso(), userPermissions.getPreferredLangOfSelectedTheso()));
        
        idProject = userHelper.getGroupOfThisTheso(selectedTheso.getCurrentIdTheso());
        
        if(nodeUser != null) {
            if(nodeUser.isSuperAdmin()) {
                userPermissions.setRole(1);
                userPermissions.setRoleName("superAdmin");                
            } else {
                idRole = userHelper.getRoleOnThisTheso(nodeUser.getIdUser(), idProject, idTheso);
                userPermissions.setRole(idRole);
                userPermissions.setRoleName(userHelper.getRoleName(idRole));
            }
        }
        
        userPermissions.setProjectOfselectedTheso(idProject);
        userPermissions.setProjectOfselectedThesoName(userHelper.getGroupName(idProject));
    }    
    
    public void initUserPermissionsForThisProject(int idProject){
        if(userPermissions == null){
            userPermissions = new UserPermissions();
        }

        userPermissions.setSelectedProject(idProject);
        userPermissions.setSelectedProjectName(userHelper.getGroupName(idProject));
        userPermissions.setListThesos(userHelper.getThesaurusOfProject(idProject, workLanguage, nodeUser == null));
        if(!StringUtils.isEmpty(userPermissions.getSelectedTheso())){
            for (NodeIdValue nodeIdValue : userPermissions.getListThesos()) {
                if(nodeIdValue.getId().equalsIgnoreCase(userPermissions.getSelectedTheso()))
                    return;
            }
            resetUserPermissionsForThisTheso();
        }
    }
    
    /**
     * remise à zéro des variables pour le projet en cours
     */    
    public void resetUserPermissionsForThisTheso(){
        if(userPermissions == null){
            userPermissions = new UserPermissions();
        }
        userPermissions.setRole(-1);
        userPermissions.setRoleName("");
    }       
    
    /**
     * remise à zéro des variables pour le projet en cours
     */
    public void resetUserPermissionsForThisProject(){
        if(userPermissions == null){
            userPermissions = new UserPermissions();
        }           
        userPermissions.setSelectedProject(-1);
        userPermissions.setSelectedProjectName("");
    }      
    
    /**
     * initialisation des toutes les permissions vers un droit public
     */
    private void resetPermissionsAfterLogout(){
        if(userPermissions == null) return;
        userPermissions.setNodeProjectsWithThesosRoles(null);
        
        resetUserPermissionsForThisTheso();
    }
    
    /**
     * Chargement de tous les projets publics
     */
    public void initAllProject(){
        if(userPermissions == null){
            userPermissions = new UserPermissions();
        }
        userPermissions.setListProjects(userGroupLabelRepository.getProjectsByThesoStatus(false));
        
        // contrôle si le projet actuel est dans la liste, sinon, on initialise le projet sélectionné à -1
        if(userPermissions.getSelectedProject() != -1){
            for (UserGroupLabel listProject : userPermissions.getListProjects()) {
                if(listProject.getId() == userPermissions.getSelectedProject()){
                    return;
                } 
            }
            userPermissions.setSelectedProject(-1);
            userPermissions.setSelectedProjectName("");
        }
        if(userPermissions.getListProjects() == null || userPermissions.getListProjects().isEmpty()) {
            resetUserPermissionsForThisProject();
        }    
    }
    /**
     * Rcharge tous les thésaurus de tous les projets après avoir sélectionné (tous les projets)  
     */
    public void reloadAllThesoOfAllProject(){
        if(nodeUser == null) {
            initAllTheso();
        } else {
            setAllListThesoOfAllProject();      
        }
    }
    
    /**
     * chargement de tous les thésaurus publics
     */
    public void initAllTheso(){
        if(userPermissions == null){
            userPermissions = new UserPermissions();
        }

        userPermissions.setListThesos(thesaurusHelper.getAllTheso(nodeUser != null));

        // contrôle si le thésaurus actuel est dans la liste, sinon, on initialise le thésaurus à null
        if(!StringUtils.isEmpty(userPermissions.getSelectedTheso())){
            for (NodeIdValue nodeIdValue : userPermissions.getListThesos()) {
                if(nodeIdValue.getId().equalsIgnoreCase(userPermissions.getSelectedTheso())){
                    return;
                } 
            }
            userPermissions.setSelectedTheso(null);
            userPermissions.setSelectedProjectName("");
        }         
        if(userPermissions.getListThesos() == null || userPermissions.getListThesos().isEmpty()) {
            resetUserPermissionsForThisTheso();
        }   
    }

    public boolean isHasRoleAsContributor(){
        return ObjectUtils.isNotEmpty(nodeUser) && ObjectUtils.isNotEmpty(userPermissions) &&
                (
                (userPermissions.isContributor()) || (userPermissions.isManager()) || (userPermissions.isAdmin()) || (nodeUser.isSuperAdmin())
                ) ;
    }

    public boolean isHasRoleAsManager(){
        return ObjectUtils.isNotEmpty(nodeUser) && ObjectUtils.isNotEmpty(userPermissions) &&
                (
                (userPermissions.isManager()) || (userPermissions.isAdmin()) || (nodeUser.isSuperAdmin())
                );
    }
    
    public boolean isHasRoleAsAdmin(){
        return ObjectUtils.isNotEmpty(nodeUser) && ObjectUtils.isNotEmpty(userPermissions) && 
                (  
                (userPermissions.isAdmin()) || (nodeUser.isSuperAdmin()) 
                );
    }

    public boolean isHasRoleAsSuperAdmin(){
        return ObjectUtils.isNotEmpty(nodeUser) && ObjectUtils.isNotEmpty(userPermissions) &&
                (
                (nodeUser.isSuperAdmin())
                );
    }


    public boolean isAlertVisible() {
        return ObjectUtils.isNotEmpty(nodeUser) && (nodeUser.isSuperAdmin() || roleOnThesoBean.isAdminOnThisTheso()) && nodeUser.isActive();
    }
    
    private void showErrorMessage(String msg) {
        // utilisateur ou mot de passe n'existent pas
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, "Login Error!", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    /**
     * Permet de mettre à jour toutes les informations concernant un user #MR
     */
    private void setInfos() {
        username = "";
        password = "";
        if (nodeUser == null) {
            return;
        }
        roleOnThesoBean.showListTheso(this);
        initAllAuthorizedProjectAsAdmin();

        /// Permet de vérifier après une connexion, si le thésaurus actuel est dans la liste des thésaurus authorisés pour modification
        // sinon, on nettoie l'interface et le thésaurus. 
        roleOnThesoBean.redirectAndCleanTheso();
    }

    /**
     * permet de remettre à jour les informations d'un utilisateur
     */
    public void reGetUser() {
        if (nodeUser == null) {
            return;
        }
        nodeUser = userHelper.getUser(nodeUser.getIdUser());
    }

    public String formatUserName(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return "";
        }
        return StringUtils.upperCase(userName.charAt(0) + "") + userName.substring(1);
    }

    /**
     * permet de savoir si l'utilisateur est admin au moins sur un projet pour
     * contôler la partie import et export
     */
    private void initAllAuthorizedProjectAsAdmin() {
        ArrayList<NodeUserRoleGroup> allAuthorizedProjectAsAdminTemp = userHelper.getUserRoleGroup(nodeUser.getIdUser());
        if (allAuthorizedProjectAsAdmin == null) {
            allAuthorizedProjectAsAdmin = new ArrayList<>();
        } else {
            allAuthorizedProjectAsAdmin.clear();
        }
        for (NodeUserRoleGroup nodeUserRoleGroup : allAuthorizedProjectAsAdminTemp) {
            if (nodeUserRoleGroup.isAdmin()) {
                allAuthorizedProjectAsAdmin.add(nodeUserRoleGroup);
            }
        }
    }
    
}
