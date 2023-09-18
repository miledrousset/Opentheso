package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bdd.tools.MD5Password;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.connect.MenuBean;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ProjectBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.search.SearchBean;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import javax.annotation.PreDestroy;

import fr.cnrs.opentheso.utils.LDAPUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


/**
 *
 * @author miledrousset
 */
@SessionScoped
@Named(value = "currentUser")
public class CurrentUser implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    @Inject
    private ViewEditorHomeBean viewEditorHomeBean;
    @Inject
    private IndexSetting indexSetting;
    @Inject
    private MenuBean menuBean;
    @Inject
    private RightBodySetting rightBodySetting;
    @Inject
    private PropositionBean propositionBean;
    @Inject
    private SearchBean searchBean;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private ProjectBean projectBean;

    private NodeUser nodeUser;
    private String username;
    private String password;
    private boolean ldapEnable = false;

    private ArrayList<NodeUserRoleGroup> allAuthorizedProjectAsAdmin;

    @PreDestroy
    public void destroy() {
        /// c'est le premier composant qui se détruit
        clear();
    }

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
        roleOnThesoBean.showListTheso();

        // tester si le thésaurus en cours est privé, alors après une déconnexion, on devrait plus l'afficher
        roleOnThesoBean.setAndClearThesoInAuthorizedList();
        indexSetting.setIsThesoActive(true);
        rightBodySetting.setIndex("0");

        initHtmlPages();

        selectedTheso.loadProject();
        selectedTheso.setSelectedProject();

        if ("-1".equals(selectedTheso.getProjectIdSelected())) {
            roleOnThesoBean.setPublicThesos();
            if (!new ThesaurusHelper().isThesoPrivate(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso())) {
                indexSetting.setSelectedTheso(true);
            } else {
                selectedTheso.setCurrentIdTheso(null);
                indexSetting.setSelectedTheso(false);
            }
            indexSetting.setProjectSelected(false);
        } else if (selectedTheso.getProjectsList().stream()
                .filter(element -> element.getId() == Integer.parseInt(selectedTheso.getProjectIdSelected()))
                .findFirst().isEmpty()) {
            selectedTheso.setProjectIdSelected("-1");
            indexSetting.setProjectSelected(false);
            selectedTheso.setSelectedIdTheso(null);
            indexSetting.setSelectedTheso(false);
        } else {
            if (StringUtils.isNotEmpty(selectedTheso.getCurrentIdTheso())) {
                if (!new ThesaurusHelper().isThesoPrivate(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso())) {
                    indexSetting.setSelectedTheso(true);
                    indexSetting.setProjectSelected(false);
                } else {
                    selectedTheso.setCurrentIdTheso(null);
                    indexSetting.setSelectedTheso(false);
                    indexSetting.setProjectSelected(true);
                }
            } else {
                indexSetting.setSelectedTheso(false);
                indexSetting.setProjectSelected(false);
            }
        }

        if (propositionBean.isPropositionVisibleControle()) {
            PrimeFaces.current().executeScript("disparaitre();");
            propositionBean.setPropositionVisibleControle(false);
            searchBean.setBarVisisble(false);
            searchBean.setSearchResultVisible(false);
            searchBean.setSearchVisibleControle(false);
        }
        
        if (!"index".equals(menuBean.getActivePageName())) {
            menuBean.redirectToThesaurus();
        } else {
            PrimeFaces pf = PrimeFaces.current();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:contentConcept");
                pf.ajax().update("containerIndex:searchBar");
                pf.ajax().update("containerIndex:header");
                pf.ajax().update("menuBar");
                pf.ajax().update("messageIndex");
            }
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

        int idUser = -1;
        if (ldapEnable) {
            if (!new LDAPUtils().authentificationLdapCheck(username, password)) {
                showErrorMessage("User or password LDAP wrong, please try again");
                return;
            }
            idUser = new UserHelper().getIdUserFromPseudo(connect.getPoolConnexion(), username);
        } else {
            idUser = new UserHelper().getIdUser(connect.getPoolConnexion(), username, MD5Password.getEncodedPassword(password));
        }

        if (idUser == -1) {
            showErrorMessage("User or password wrong, please try again");
            return;
        }

        // on récupère le compte de l'utilisatreur
        nodeUser = new UserHelper().getUser(connect.getPoolConnexion(), idUser);
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
        
        if ("2".equals(rightBodySetting.getIndex())) {
            rightBodySetting.setIndex("0");
        }

        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);

        selectedTheso.loadProject();
        //selectedTheso.setSelectedProject();
        if ("-1".equals(selectedTheso.getProjectIdSelected())) {
            roleOnThesoBean.setOwnerThesos();
            indexSetting.setProjectSelected(false);
            if (!new ThesaurusHelper().isThesoPrivate(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso())) {
                indexSetting.setSelectedTheso(true);
            } else {
                selectedTheso.setCurrentIdTheso(null);
                indexSetting.setSelectedTheso(false);
            }
        } else {
            //indexSetting.setProjectSelected(true);
            projectBean.initProject(selectedTheso.getProjectIdSelected());

            if (!projectBean.getListeThesoOfProject().isEmpty()) {
                roleOnThesoBean.setAuthorizedTheso(projectBean.getListeThesoOfProject().stream()
                        .map(NodeIdValue::getId)
                        .collect(Collectors.toList()));
            } else {
                roleOnThesoBean.setAuthorizedTheso(Collections.emptyList());
            }
            roleOnThesoBean.addAuthorizedThesoToHM();
            roleOnThesoBean.setUserRoleOnThisTheso();
            projectBean.init();
        }

        if (StringUtils.isNotEmpty(selectedTheso.getCurrentIdTheso())) {
            if (!new ThesaurusHelper().isThesoPrivate(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso())) {
                indexSetting.setSelectedTheso(true);
                indexSetting.setProjectSelected(false);
            } else {
                selectedTheso.setCurrentIdTheso(null);
                indexSetting.setSelectedTheso(false);
                indexSetting.setProjectSelected(true);
            }
        } else {
            indexSetting.setSelectedTheso(false);
            indexSetting.setProjectSelected(false);
        }

        PrimeFaces.current().executeScript("PF('login').hiden();");
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("idLogin");
            pf.ajax().update("containerIndex:header");
        }
    }

    private void showErrorMessage(String msg) {
        // utilisateur ou mot de passe n'existent pas
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, "Login Error!", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    private void initHtmlPages() {
        viewEditorHomeBean.reset();
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
        roleOnThesoBean.showListTheso();
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
        if (connect.getPoolConnexion() != null) {
            nodeUser = new UserHelper().getUser(connect.getPoolConnexion(), nodeUser.getIdUser());
        }
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
     *
     * @return
     */
    private void initAllAuthorizedProjectAsAdmin() {
        UserHelper userHelper = new UserHelper();
        ArrayList<NodeUserRoleGroup> allAuthorizedProjectAsAdminTemp = userHelper.getUserRoleGroup(connect.getPoolConnexion(), nodeUser.getIdUser());
        if (allAuthorizedProjectAsAdmin == null) {
            allAuthorizedProjectAsAdmin = new ArrayList<>();
        } else {
            allAuthorizedProjectAsAdmin.clear();
        }
        for (NodeUserRoleGroup nodeUserRoleGroup : allAuthorizedProjectAsAdminTemp) {
            if (nodeUserRoleGroup.isIsAdmin()) {
                allAuthorizedProjectAsAdmin.add(nodeUserRoleGroup);
            }
        }
    }

    public void forgotPassword() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Default user name: BootsFaces");
        FacesContext.getCurrentInstance().addMessage("loginForm:username", msg);
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Default password: rocks!");
        FacesContext.getCurrentInstance().addMessage("loginForm:password", msg);
    }

    public boolean isAlertVisible() {
        return ObjectUtils.isNotEmpty(nodeUser) && (nodeUser.isSuperAdmin() || roleOnThesoBean.isAdminOnThisTheso()) && nodeUser.isActive();
    }

    public boolean isCanModify() {
        return ObjectUtils.isNotEmpty(nodeUser) && (roleOnThesoBean.isManagerOnThisTheso() || nodeUser.isSuperAdmin()
                || roleOnThesoBean.isAdminOnThisTheso());
    }

    public Connect getConnect() {
        return connect;
    }

    public void setConnect(Connect connect) {
        this.connect = connect;
    }

    public RoleOnThesoBean getRoleOnThesoBean() {
        return roleOnThesoBean;
    }

    public void setRoleOnThesoBean(RoleOnThesoBean roleOnThesoBean) {
        this.roleOnThesoBean = roleOnThesoBean;
    }

    public ViewEditorHomeBean getViewEditorHomeBean() {
        return viewEditorHomeBean;
    }

    public void setViewEditorHomeBean(ViewEditorHomeBean viewEditorHomeBean) {
        this.viewEditorHomeBean = viewEditorHomeBean;
    }

    public IndexSetting getIndexSetting() {
        return indexSetting;
    }

    public void setIndexSetting(IndexSetting indexSetting) {
        this.indexSetting = indexSetting;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    public RightBodySetting getRightBodySetting() {
        return rightBodySetting;
    }

    public void setRightBodySetting(RightBodySetting rightBodySetting) {
        this.rightBodySetting = rightBodySetting;
    }

    public PropositionBean getPropositionBean() {
        return propositionBean;
    }

    public void setPropositionBean(PropositionBean propositionBean) {
        this.propositionBean = propositionBean;
    }

    public SearchBean getSearchBean() {
        return searchBean;
    }

    public void setSearchBean(SearchBean searchBean) {
        this.searchBean = searchBean;
    }

    public SelectedTheso getSelectedTheso() {
        return selectedTheso;
    }

    public void setSelectedTheso(SelectedTheso selectedTheso) {
        this.selectedTheso = selectedTheso;
    }

    public NodeUser getNodeUser() {
        return nodeUser;
    }

    public void setNodeUser(NodeUser nodeUser) {
        this.nodeUser = nodeUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLdapEnable() {
        return ldapEnable;
    }

    public void setLdapEnable(boolean ldapEnable) {
        this.ldapEnable = ldapEnable;
    }

    public ArrayList<NodeUserRoleGroup> getAllAuthorizedProjectAsAdmin() {
        return allAuthorizedProjectAsAdmin;
    }

    public void setAllAuthorizedProjectAsAdmin(ArrayList<NodeUserRoleGroup> allAuthorizedProjectAsAdmin) {
        this.allAuthorizedProjectAsAdmin = allAuthorizedProjectAsAdmin;
    }
}
