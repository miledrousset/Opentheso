package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bdd.tools.MD5Password;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.connect.MenuBean;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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
@Named(value = "currentUser")
@SessionScoped
public class CurrentUser implements Serializable {

    @Inject private Connect connect;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private ViewEditorHomeBean viewEditorHomeBean;
    @Inject private IndexSetting indexSetting;
    @Inject private MenuBean menuBean;

    private NodeUser nodeUser;
    private String username;
    private String password;
    private boolean ldapEnable = false;
    
    private ArrayList<NodeUserRoleGroup> allAuthorizedProjectAsAdmin;

    @PreDestroy
    public void destroy(){
        /// c'est le premier composant qui se détruit
        clear();
    }  
    public void clear(){
        if(allAuthorizedProjectAsAdmin!= null){
            allAuthorizedProjectAsAdmin.clear();
            allAuthorizedProjectAsAdmin = null;
        }
        nodeUser = null;
        username = null;
        password = null;
    }    
    
    public void setUsername(String name) {
        this.username = name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void disconnect(boolean redirectionEnable) throws IOException {
        FacesMessage facesMessage;
        facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Goodbye", nodeUser.getName());
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        nodeUser = null;
        roleOnThesoBean.showListTheso();
        // tester si le thésaurus en cours est privé, alors après une déconnexion, on devrait plus l'afficher
        roleOnThesoBean.setAndClearThesoInAuthorizedList();
        indexSetting.setIsThesoActive(true);
        PrimeFaces pf = PrimeFaces.current();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
        initHtmlPages();
        
        if (redirectionEnable) {
            menuBean.redirectToThesaurus();
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
     * @throws java.io.IOException
     */
    public void login() throws IOException {
        
        UserHelper userHelper = new UserHelper();
        
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            showErrorMessage("champ vide non autorisé");
            return;
        }

        int idUser = -1;
        if (ldapEnable) {
            if (!new LDAPUtils().authentificationLdapCheck(username, password)) {
                showErrorMessage("User or password LDAP wrong, please try again");
                return;
            }
            idUser = userHelper.getIdUserFromPseudo(connect.getPoolConnexion(), username);
        } else {
            idUser = userHelper.getIdUser(connect.getPoolConnexion(),
                    username, MD5Password.getEncodedPassword(password));
        }

        if (idUser == -1) {
            showErrorMessage("User or password wrong, please try again");
            return;
        }

        // on récupère le compte de l'utilisatreur
        nodeUser = userHelper.getUser(connect.getPoolConnexion(), idUser);
        if (nodeUser == null) {
            showErrorMessage("Incohérence base de données ou utilisateur n'existe pas");
            return;
        }
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome", username);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);

        setInfos();
        indexSetting.setIsThesoActive(true);
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex");
        }        
        FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
    }

    private void showErrorMessage(String msg) {
        // utilisateur ou mot de passe n'existent pas
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error!", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }
    
    private void initHtmlPages(){
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
        return StringUtils.upperCase(userName.charAt(0)+"") + userName.substring(1, userName.length());
    }
    
    /**
     * permet de savoir si l'utilisateur est admin au moins sur un projet
     * pour contôler la partie import et export
     * @return 
     */
    private void initAllAuthorizedProjectAsAdmin(){
        UserHelper userHelper = new UserHelper();
        ArrayList<NodeUserRoleGroup> allAuthorizedProjectAsAdminTemp = userHelper.getUserRoleGroup(connect.getPoolConnexion(), nodeUser.getIdUser());
        if(allAuthorizedProjectAsAdmin == null)
            allAuthorizedProjectAsAdmin = new ArrayList<>();
        else
            allAuthorizedProjectAsAdmin.clear();
        for (NodeUserRoleGroup nodeUserRoleGroup : allAuthorizedProjectAsAdminTemp) {
            if(nodeUserRoleGroup.isIsAdmin())
                allAuthorizedProjectAsAdmin.add(nodeUserRoleGroup);
        }
    }

    public void forgotPassword() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Default user name: BootsFaces");
        FacesContext.getCurrentInstance().addMessage("loginForm:username", msg);
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Default password: rocks!");
        FacesContext.getCurrentInstance().addMessage("loginForm:password", msg);
    }
    
    public boolean isAlertVisible() {
        return ObjectUtils.isNotEmpty(nodeUser) && nodeUser.isIsSuperAdmin() && nodeUser.isIsActive();
    }

    public NodeUser getNodeUser() {
        return nodeUser;
    }

    public void setNodeUser(NodeUser nodeUser) {
        this.nodeUser = nodeUser;
    }

    public ArrayList<NodeUserRoleGroup> getAllAuthorizedProjectAsAdmin() {
        return allAuthorizedProjectAsAdmin;
    }

    public void setAllAuthorizedProjectAsAdmin(ArrayList<NodeUserRoleGroup> allAuthorizedProjectAsAdmin) {
        this.allAuthorizedProjectAsAdmin = allAuthorizedProjectAsAdmin;
    }
    public boolean isLdapEnable() {
        return ldapEnable;
    }

    public void setLdapEnable(boolean ldapEnable) {
        this.ldapEnable = ldapEnable;
    }
}
