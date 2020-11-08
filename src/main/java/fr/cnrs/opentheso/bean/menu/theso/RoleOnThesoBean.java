/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.menu.theso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.AccessThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.menu.users.ManagerGroupsUsers;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Miled Rousset
 */
@Named(value = "roleOnTheso")
@SessionScoped

public class RoleOnThesoBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private CurrentUser currentUser;
    @Inject
    private ManagerGroupsUsers managerGroupsUsers;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private SelectedTheso selectedTheso;

    //liste des thesaurus public suivant les droits de l'utilisateur
    private Map<String, String> listTheso;
    //liste des thesaurus For export or management avec droits admin pour l'utilisateur en cours
    private Map<String, String> listThesoAsAdmin;    
    
    // la liste des thésaurus autorisés pour l'utilisateur où il est au minimun manager pour pouvoir les modifier
    private ArrayList<NodeIdValue> nodeListTheso;
    
    // la liste des thésaurus autorisés pour l'utilisateur où il est admin pour avoir le droit de les exporter
    private ArrayList<NodeIdValue> nodeListThesoAsAdmin;    

    //thesaurus à gérer
    private Thesaurus thesoInfos;

    private boolean isSuperAdmin = false;
    private boolean isAdminOnThisTheso = false;
    private boolean isManagerOnThisTheso = false;
    private boolean isContributorOnThisTheso = false;

    private List<String> authorizedTheso;
    
    private List<String> authorizedThesoAsAdmin;
    
    private NodePreference nodePreference;

    private NodeUserRoleGroup nodeUserRoleGroup;

    public RoleOnThesoBean() {

    }

    @PostConstruct
    public void init() {
//        showListTheso();
    }

    //// restructuration de la classe le 05/04/2018 par Miled Rousset//////    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////// Nouvelles fonctions //////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////       
    /**
     * permet de récupérer les préférences pour le thésaurus sélectionné s'il y
     * en a pas, on les initialises par les valeurs par defaut
     *
     * #MR
     */
    public void initNodePref() {
        if (selectedTheso.getCurrentIdTheso() == null) {
            return;
        }
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        if (connect.getPoolConnexion() != null) {
            nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
            if (nodePreference == null) { // cas où il n'y a pas de préférence pour ce thésaurus, il faut les créer 
                preferencesHelper.initPreferences(connect.getPoolConnexion(),
                        selectedTheso.getCurrentIdTheso(), connect.getWorkLanguage());
                nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
            }
            return;
        }
        nodePreference = null;
    }

    /**
     * met à jour les préférences après une modification
     */
    public void updatePreferences() {
        if (!new PreferencesHelper().updateAllPreferenceUser(connect.getPoolConnexion(), nodePreference, selectedTheso.getCurrentIdTheso())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
            return;
        }
        initNodePref();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("currentUser.info6")));
    }

    /**
     * permet d'initialiser la liste des thésaurus suivant les droits
     */
    public void showListTheso() {
        if (currentUser.getNodeUser() == null) {
            setPublicThesos();
        } else {
            setOwnerThesos();
        }
    }

    /**
     * fonction pour sortir une liste (sous forme de hashMap ) de thesaurus
     * correspondant à l'utilisateur connecté permet de charger les thésaurus
     * autorisés pour l'utilisateur en cours on récupère les id puis les
     * tradcutions (ceci permet de récupérer les thésaurus non traduits) #MR
     */
    private void setOwnerThesos() {
        if (currentUser.getNodeUser() == null) {
            this.listTheso = new HashMap();
            return;
        }
        authorizedTheso = new ArrayList<>();
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        UserHelper currentUserHelper = new UserHelper();
        if (currentUser.getNodeUser().isIsSuperAdmin()) {
            boolean withPrivateTheso = true;
            authorizedTheso = thesaurusHelper.getAllIdOfThesaurus(connect.getPoolConnexion(), withPrivateTheso);
            authorizedThesoAsAdmin = thesaurusHelper.getAllIdOfThesaurus(connect.getPoolConnexion(), withPrivateTheso);

        } else {
            authorizedTheso = currentUserHelper.getThesaurusOfUser(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
            authorizedThesoAsAdmin = currentUserHelper.getThesaurusOfUserAsAdmin(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());         
        }
        addAuthorizedThesoToHM();
        initAuthorizedThesoAsAdmin();
        
        // permet de définir le role de l'utilisateur sur le group
        if (authorizedTheso.isEmpty()) {
            setUserRoleGroup();
        } else {
            setUserRoleOnThisTheso();
        }
    }

    
    // on ajoute les thésaurus où l'utilisateur a le droit admin dessus
    private void initAuthorizedThesoAsAdmin() {
        if (authorizedThesoAsAdmin == null) {
            return;
        }
        nodeListThesoAsAdmin = new ArrayList<>();
        HashMap<String, String> authorizedThesoAsAdminHM = new LinkedHashMap();
        
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        String title;
        String lang = connect.getWorkLanguage().toLowerCase();

        // ajout de code qui permet de charger une liste de thésaurus avec le nom en utilisant la langue préférée dans les préférences du thésaurus.
        // pour éviter d'afficher des Id quand on a un mélange des thésaurus avec des langues sources différentes.
        String preferredIdLangOfTheso;
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        for (String idTheso1 : authorizedThesoAsAdmin) {
            preferredIdLangOfTheso = preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), idTheso1);
            if (preferredIdLangOfTheso == null) {
                preferredIdLangOfTheso = lang;
            }

            title = thesaurusHelper.getTitleOfThesaurus(connect.getPoolConnexion(), idTheso1, preferredIdLangOfTheso);
            if (title == null) {
                authorizedThesoAsAdminHM.put("" + "(" + idTheso1 + ")", idTheso1);
            } else {
                authorizedThesoAsAdminHM.put(title + " (" + idTheso1 + ")", idTheso1);
            }
            // nouvel objet pour récupérer la liste des thésaurus autorisée pour l'utilisateur en cours
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idTheso1);
            nodeIdValue.setValue(title);
            nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(connect.getPoolConnexion(), idTheso1));
            nodeListThesoAsAdmin.add(nodeIdValue);
            
        }
               
        this.listThesoAsAdmin = authorizedThesoAsAdminHM;
    }    
            
            
    // on ajoute les titres + id, sinon l'identifiant du thésauurus
    private void addAuthorizedThesoToHM() {
        if (authorizedTheso == null) {
            return;
        }
        nodeListTheso = new ArrayList<>();
        HashMap<String, String> authorizedThesoHM = new LinkedHashMap();
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        String title;
        String lang = connect.getWorkLanguage().toLowerCase();

        // ajout de code qui permet de charger une liste de thésaurus avec le nom en utilisant la langue préférée dans les préférences du thésaurus.
        // pour éviter d'afficher des Id quand on a un mélange des thésaurus avec des langues sources différentes.
        String preferredIdLangOfTheso;
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        for (String idTheso1 : authorizedTheso) {
            preferredIdLangOfTheso = preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), idTheso1);
            if (preferredIdLangOfTheso == null) {
                preferredIdLangOfTheso = lang;
            }

            title = thesaurusHelper.getTitleOfThesaurus(connect.getPoolConnexion(), idTheso1, preferredIdLangOfTheso);
            if (title == null) {
                authorizedThesoHM.put("" + "(" + idTheso1 + ")", idTheso1);
            } else {
                authorizedThesoHM.put(title + " (" + idTheso1 + ")", idTheso1);
            }
            // nouvel objet pour récupérer la liste des thésaurus autorisée pour l'utilisateur en cours
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idTheso1);
            nodeIdValue.setValue(title);
            nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(connect.getPoolConnexion(), idTheso1));
            nodeListTheso.add(nodeIdValue);
            
        }

                
        this.listTheso = authorizedThesoHM;
    }

    /**
     * fonction pour récupérer la liste (sous forme de hashMap ) de thesaurus
     * tous les thésaurus sauf privés #MR
     *
     */
    private void setPublicThesos() {
        boolean withPrivateTheso = false;
        /*    AccessThesaurusHelper ath = new AccessThesaurusHelper();
        this.listTheso = ath.getListThesaurus(connect.getPoolConnexion(),connect.getWorkLanguage());*/
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        authorizedTheso = thesaurusHelper.getAllIdOfThesaurus(connect.getPoolConnexion(), withPrivateTheso);
        addAuthorizedThesoToHM();
    }

    /**
     * permet de savoir si l'utilisateur a les droits sur ce thésaurus
     *
     * @return
     */
    public boolean isIsHaveWriteToCurrentThesaurus() {
        if (currentUser.getNodeUser() == null) {
            return false;
        }
        if (selectedTheso.getCurrentIdTheso() == null) {
            return false;
        }
        if (currentUser.getNodeUser().isIsSuperAdmin()) {
            return true;
        }
        return authorizedTheso.contains(selectedTheso.getCurrentIdTheso());
    }

    /**
     * Permet de définir le role d'un utilisateur sur le thésaurus en cours le
     * groupe du thésaurus est trouvé automatiquement, si l'utilisateur est
     * SuperAdmin, pas besoin du groupe
     *
     * #MR
     */
    private void setUserRoleOnThisTheso() {
        if (currentUser.getNodeUser() == null) {
            nodeUserRoleGroup = null;
            return;
        }
        if (selectedTheso.getCurrentIdTheso() == null) {
            nodeUserRoleGroup = null;
            return;
        }
        initRoles();
        UserHelper currentUserHelper = new UserHelper();

        if (currentUser.getNodeUser().isIsSuperAdmin()) {
            nodeUserRoleGroup = managerGroupsUsers.getUserRoleOnThisGroup(-1); // cas de superadmin, on a accès à tous les groupes
        } else {
            int idGroup = currentUserHelper.getGroupOfThisTheso(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
            nodeUserRoleGroup = managerGroupsUsers.getUserRoleOnThisGroup(idGroup);
        }
        if (nodeUserRoleGroup == null) {
            return;
        }

        if (nodeUserRoleGroup.getIdRole() == 1) {
            setIsSuperAdmin(true);
        }
        if (nodeUserRoleGroup.getIdRole() == 2) {
            setIsAdminOnThisTheso(true);
        }
        if (nodeUserRoleGroup.getIdRole() == 3) {
            setIsManagerOnThisTheso(true);
        }
        if (nodeUserRoleGroup.getIdRole() == 4) {
            setIsContributorOnThisTheso(true);
        }
    }

    /**
     * permet de récuperer le role de l'utilisateur sur le group applelé en cas
     * où le group n'a aucun thésaurus pour que l'utilisateur puisse créer des
     * thésaurus et gérer les utilisateur pour le group il faut être Admin
     */
    private void setUserRoleGroup() {
        UserHelper currentUserHelper = new UserHelper();
        ArrayList<NodeUserRoleGroup> nodeUserRoleGroups = currentUserHelper.getUserRoleGroup(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
        for (NodeUserRoleGroup nodeUserRoleGroup1 : nodeUserRoleGroups) {
            if (nodeUserRoleGroup1.isIsAdmin()) {
                setIsAdminOnThisTheso(true);
            }
        }
    }

    private void initRoles() {
        setIsSuperAdmin(false);
        setIsAdminOnThisTheso(false);
        setIsManagerOnThisTheso(false);
        setIsContributorOnThisTheso(false);
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    //////// fin des nouvelles fontions ////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////          
    /**
     * Fonction accessAThesaurus fonction pour afficher un Thesaurus avec tous
     * ses champs, dans la variable privée thesaurus #JM
     *
     * @param idTheso
     */
    public void accessAThesaurus(String idTheso) {
        AccessThesaurusHelper ath = new AccessThesaurusHelper();
        this.thesoInfos = ath.getAThesaurus(connect.getPoolConnexion(), idTheso, connect.getWorkLanguage());
    }

    /**
     * fonction addVisbility #JM fonction pour update ou insert d'une valeur
     * dans visibility dans la table thesaurus via la classe
     * accessThesaurusHelper
     */
    /*   public void addVisibility(){
        boolean visible=this.thesoInfos.isVisibility();
        String id=theso.getEditTheso().getId_thesaurus();
        AccessThesaurusHelper ath=new AccessThesaurusHelper();
        int ret=ath.insertVisibility(this.connect.getPoolConnexion(),visible,id);
        System.out.println("valeur retour insertVibility ="+ret);
        theso.setEditTheso(new Thesaurus());
    }*/
    public void supprVisibility(String id) {
        AccessThesaurusHelper ath = new AccessThesaurusHelper();

        int ret = ath.supprVisibility(this.connect.getPoolConnexion(), id);
        System.out.println("valeur retour supprVibility =" + ret);
    }

    /**
     * Fonction cleanSession #JM Fonction pour remettre les valeurs par défauts
     * à ce bean et que l'on rappelle bien une nouvelle fois la base de données
     * Provoque une redirection via deco.xhtml, pour être en cohérence avec
     * l'implémentation précédente (c'était la façon de se déconnecté depuis
     * l'index dans les versions précédentes)
     *
     * @throws IOException
     */
    public void cleanSession() throws IOException {
        this.thesoInfos = null;
        setPublicThesos();
        currentUser.setNodeUser(null);
        FacesContext.getCurrentInstance().getExternalContext().redirect("deco.xhtml");
    }

    /**
     * Fonction getAccessThesaurus, #JM Si il n'y pas de valeur de thesaurus
     * dans la variable accessThesaurus alors c'est qu'on doit créer un
     * thésaurus vide
     *
     * @return
     */
    public Thesaurus getAccessThesaurus() {
        if (thesoInfos == null) {
            thesoInfos = new Thesaurus();
        }
        return thesoInfos;
    }

    /**
     * fonction setAccessThesaurus #JM fonction pour attribuer une valeur à au
     * thesaurus en accès, la varaible est accessThesaurus si on passe en
     * paramètre de ce setter une valeur à null, le setter créer un nouveau
     * thesaurus
     *
     * @param thesaurus
     */
    public void setAccessThesaurus(Thesaurus thesaurus) {
        if (thesaurus == null) {
            this.thesoInfos = new Thesaurus();
        } else {
            this.thesoInfos = thesaurus;
        }
    }

    /**
     * changeVisibility #jm fonction pour passer de visibilité privée à publique
     */
    public void changeVisibility() {
        boolean changeVisibility = this.thesoInfos.isPrivateTheso();
        this.thesoInfos.setPrivateTheso(!(changeVisibility));
        //       this.addVisibility();
        System.out.println("visbility change " + this.thesoInfos.isPrivateTheso());
    }

    public boolean isIsAdminOnThisTheso() {
        return isAdminOnThisTheso;
    }

    public void setIsAdminOnThisTheso(boolean isAdminOnThisTheso) {
        this.isAdminOnThisTheso = isAdminOnThisTheso;
    }

    public boolean isIsManagerOnThisTheso() {
        return isManagerOnThisTheso;
    }

    public void setIsManagerOnThisTheso(boolean isManagerOnThisTheso) {
        this.isManagerOnThisTheso = isManagerOnThisTheso;
    }

    public boolean isIsContributorOnThisTheso() {
        return isContributorOnThisTheso;
    }

    public void setIsContributorOnThisTheso(boolean isContributorOnThisTheso) {
        this.isContributorOnThisTheso = isContributorOnThisTheso;
    }

    public boolean isIsSuperAdmin() {
        return isSuperAdmin;
    }

    public void setIsSuperAdmin(boolean isSuperAdmin) {
        this.isSuperAdmin = isSuperAdmin;
    }

    public Map<String, String> getListTheso() {
        return listTheso;
    }

    public void setListTheso(HashMap<String, String> listTheso) {
        this.listTheso = listTheso;
    }

    public Map<String, String> getListThesoAsAdmin() {
        return listThesoAsAdmin;
    }

    public void setListThesoAsAdmin(Map<String, String> listThesoAsAdmin) {
        this.listThesoAsAdmin = listThesoAsAdmin;
    }

    public List<String> getAuthorizedTheso() {
        return authorizedTheso;
    }

    public void setAuthorizedTheso(List<String> authorizedTheso) {
        this.authorizedTheso = authorizedTheso;
    }

    public List<String> getAuthorizedThesoAsAdmin() {
        return authorizedThesoAsAdmin;
    }

    public void setAuthorizedThesoAsAdmin(List<String> authorizedThesoAsAdmin) {
        this.authorizedThesoAsAdmin = authorizedThesoAsAdmin;
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public NodeUserRoleGroup getNodeUserRoleGroup() {
        return nodeUserRoleGroup;
    }

    public void setNodeUserRoleGroup(NodeUserRoleGroup nodeUserRoleGroup) {
        this.nodeUserRoleGroup = nodeUserRoleGroup;
    }

    public LanguageBean getLangueBean() {
        return languageBean;
    }

    public void setLangueBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    public ArrayList<NodeIdValue> getNodeListTheso() {
        return nodeListTheso;
    }

    public void setNodeListTheso(ArrayList<NodeIdValue> nodeListTheso) {
        this.nodeListTheso = nodeListTheso;
    }

    public ArrayList<NodeIdValue> getNodeListThesoAsAdmin() {
        return nodeListThesoAsAdmin;
    }

    public void setNodeListThesoAsAdmin(ArrayList<NodeIdValue> nodeListThesoAsAdmin) {
        this.nodeListThesoAsAdmin = nodeListThesoAsAdmin;
    }

}
