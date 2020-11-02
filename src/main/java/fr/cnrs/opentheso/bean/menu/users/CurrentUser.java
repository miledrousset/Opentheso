/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bdd.tools.MD5Password;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import java.io.Serializable;
import java.util.ArrayList;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
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

    private NodeUser nodeUser;
    private String username;
    private String password;
    
    private ArrayList<NodeUserRoleGroup> allAuthorizedProjectAsAdmin;  

    public CurrentUser() {
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

    public void disconnect() {
        FacesMessage facesMessage;
        facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Goodbye", nodeUser.getName());
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        nodeUser = null;

        indexSetting.setIsThesoActive(true);
        PrimeFaces pf = PrimeFaces.current();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex");
            pf.ajax().update("formMenu");
            pf.ajax().update("formRightTab");
            pf.ajax().update("messageIndex");
            pf.ajax().update("formLeftTab");
            pf.ajax().update("homePageForm");
            pf.ajax().update("loginForm");
            pf.ajax().update("formSearch:languageSelect");
            pf.ajax().update("formSearch");

        }
        initHtmlPages();
    }

    public void disconnect(String msg) {

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
     */
    public void login() {
        UserHelper userHelper = new UserHelper();
        FacesMessage facesMessage;
        
        
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            // utilisateur ou mot de passe n'existent pas
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error!", "champ vide non autorisé");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            return;
        }        
        
        int idUser = userHelper.getIdUser(connect.getPoolConnexion(),
                username, MD5Password.getEncodedPassword(password));
        if (idUser == -1) {
            // utilisateur ou mot de passe n'existent pas
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error!", "User or password wrong, please try again");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            return;
        }

        // on récupère le compte de l'utilisatreur 
        nodeUser = userHelper.getUser(connect.getPoolConnexion(), idUser);
        if (nodeUser == null) {
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "incohérence base de données");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            return;
        }
        facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome", username);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        
        PrimeFaces.current().executeScript("PF('login').hide();");

        setInfos();
        indexSetting.setIsThesoActive(true);
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex");
            pf.ajax().update("formMenu");;
            
        }

        
        /*   if (nodeUser.isPasstomodify()) {
            return "changePass.xhtml?faces-redirect=true";// nouvelle pass web pour changer le motpasstemp
        }*/
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
//        addAuthorizedThesoToHM();
/*        listeUser = null;
        listeGroupsOfUser = null;
        nodeUserRoleOnThisGroup = null;
        listeThesoOfGroup = null;
        FacesContext context = FacesContext.getCurrentInstance();
        String version_Opentheso = context.getExternalContext().getInitParameter("version");
        versionOfOpentheso = new BaseDeDoneesHelper().getVersionOfOpentheso(connect.getPoolConnexion());
        new BaseDeDoneesHelper().updateVersionOpentheso(connect.getPoolConnexion(), version_Opentheso);*/

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
            //       getGroupsOfUser();
            //       initVue();
        }
    }
    
    /**
     * permet de savoir si l'utilisateur est admin au moins sur un projet
     * pour contôler la partie import et export
     * @return 
     */
    private void initAllAuthorizedProjectAsAdmin(){
        UserHelper userHelper = new UserHelper();
        ArrayList<NodeUserRoleGroup> allAuthorizedProjectAsAdminTemp = userHelper.getUserRoleGroup(connect.getPoolConnexion(), nodeUser.getIdUser());
        allAuthorizedProjectAsAdmin = new ArrayList<>();
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


}
