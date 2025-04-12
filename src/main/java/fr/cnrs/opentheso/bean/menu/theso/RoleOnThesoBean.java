package fr.cnrs.opentheso.bean.menu.theso;

import fr.cnrs.opentheso.repositories.ConceptRepository;
import fr.cnrs.opentheso.repositories.ConceptStatusRepository;
import fr.cnrs.opentheso.repositories.RoleRepository;
import fr.cnrs.opentheso.repositories.ThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;
import fr.cnrs.opentheso.repositories.UserRoleOnlyOnRepository;
import fr.cnrs.opentheso.models.thesaurus.Thesaurus;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.models.userpermissions.NodeProjectThesoRole;
import fr.cnrs.opentheso.models.userpermissions.NodeThesoRole;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Value;
import org.primefaces.PrimeFaces;
import lombok.Data;

import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;

import java.io.Serializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Data
@SessionScoped
@Named(value = "roleOnTheso")
public class RoleOnThesoBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;
    private UserRoleOnlyOnRepository userRoleOnlyOnRepository;
    private UserRepository userRepository;
    private LanguageBean languageBean;
    private UserRoleGroupRepository userRoleGroupRepository;
    private UserHelper userHelper;
    private ThesaurusHelper thesaurusHelper;
    private PreferencesHelper preferencesHelper;
    private RoleRepository roleRepository;
    private ThesaurusRepository thesaurusRepository;
    private UserGroupLabelRepository userGroupLabelRepository;
    private ConceptRepository conceptRepository;
    private ConceptStatusRepository conceptStatusRepository;

    private List<ThesoModel> listTheso;
    private Map<String, String> listThesoAsAdmin;
    private List<NodeIdValue> nodeListTheso, nodeListThesoAsAdmin,nodeListThesoAsAdminFiltered;
    private Thesaurus thesoInfos;
    private boolean isSuperAdmin, isAdminOnThisTheso,isManagerOnThisTheso, isContributorOnThisTheso, isNoRole;
    private List<String> selectedThesoForSearch, authorizedTheso, authorizedThesoAsAdmin;
    private NodePreference nodePreference;
    private NodeUserRoleGroup nodeUserRoleGroup;

    private TreeNode<NodeIdValue> root;


    @Inject
    public RoleOnThesoBean(@Value("${settings.workLanguage:fr}")String workLanguage,
                           RoleRepository roleRepository,
                           UserRoleGroupRepository userRoleGroupRepository,
                           UserRoleOnlyOnRepository userRoleOnlyOnRepository,
                           UserRepository userRepository,
                           LanguageBean languageBean,
                           UserHelper userHelper,
                           ThesaurusHelper thesaurusHelper,
                           PreferencesHelper preferencesHelper,
                           ThesaurusRepository thesaurusRepository,
                           UserGroupLabelRepository userGroupLabelRepository,
                           ConceptRepository conceptRepository) {

        this.workLanguage = workLanguage;
        this.languageBean = languageBean;
        this.userHelper = userHelper;
        this.userRoleOnlyOnRepository = userRoleOnlyOnRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.thesaurusHelper = thesaurusHelper;
        this.conceptRepository = conceptRepository;
        this.thesaurusRepository = thesaurusRepository;
        this.preferencesHelper = preferencesHelper;
        this.userRoleGroupRepository = userRoleGroupRepository;
        this.userGroupLabelRepository = userGroupLabelRepository;
    }

    public void initNodePref(SelectedTheso selectedTheso) {
        if (selectedTheso.getCurrentIdTheso() == null) {
            return;
        }

        nodePreference = preferencesHelper.getThesaurusPreferences(selectedTheso.getCurrentIdTheso());
        if (nodePreference == null) { // cas où il n'y a pas de préférence pour ce thésaurus, il faut les créer
            preferencesHelper.initPreferences(selectedTheso.getCurrentIdTheso(), workLanguage);
            nodePreference = preferencesHelper.getThesaurusPreferences(selectedTheso.getCurrentIdTheso());
        }
    }

    public void initNodePref(String idTheso) {
        if (idTheso == null) {
            return;
        }

        nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) { // cas où il n'y a pas de préférence pour ce thésaurus, il faut les créer
            preferencesHelper.initPreferences(idTheso, workLanguage);
            nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        }
    }

    /**
     * permet d'initialiser la liste des thésaurus suivant les droits
     */
    public void showListTheso(CurrentUser currentUser, SelectedTheso selectedTheso) {
        if (currentUser.getNodeUser() == null) {
            setPublicThesos(currentUser, selectedTheso);
        } else {
            setOwnerThesos(currentUser, selectedTheso);
        }
    }

    /**
     * fonction pour sortir une liste (sous forme de hashMap ) de thesaurus
     * correspondant à l'utilisateur connecté permet de charger les thésaurus
     * autorisés pour l'utilisateur en cours on récupère les id puis les
     * tradcutions (ceci permet de récupérer les thésaurus non traduits) #MR
     */
    private void setOwnerThesos(CurrentUser currentUser, SelectedTheso selectedTheso) {
        if (currentUser.getNodeUser() == null) {
            this.listTheso = new ArrayList();
            setUserRoleOnThisTheso(currentUser, selectedTheso);
            return;
        }
        
        // nouvelle méthode de gestion des droitds
        currentUser.initUserPermissions();
        
        authorizedTheso = new ArrayList<>();
        if (currentUser.getNodeUser().isSuperAdmin()) {
            boolean withPrivateTheso = true;
            authorizedTheso = thesaurusHelper.getAllIdOfThesaurus(withPrivateTheso);
            authorizedThesoAsAdmin = thesaurusHelper.getAllIdOfThesaurus(withPrivateTheso);

        } else {
            authorizedTheso = userHelper.getThesaurusOfUser(currentUser.getNodeUser().getIdUser());

            var user = userRepository.findById(currentUser.getNodeUser().getIdUser());
            // récupération de la liste des thésaurus pour les utilisateurs qui n'ont pas des droits sur un projet, mais uniquement sur des thésaurus du projet
            List<String> listThesoTemp = userRoleOnlyOnRepository.findAllByUserOrderByTheso(user.get()).stream()
                    .map(element -> element.getTheso().getIdThesaurus())
                    .toList();
            for (String idThesoTemp : listThesoTemp) {
                if(!authorizedTheso.contains(idThesoTemp)) {
                    authorizedTheso.add(idThesoTemp);
                }
            }

            authorizedThesoAsAdmin = userHelper.getThesaurusOfUserAsAdmin(currentUser.getNodeUser().getIdUser());
            
            // récupération de la liste des thésaurus pour les utilisateurs avec les droits admin, mais qui n'ont pas des droits sur un projet, mais uniquement sur des thésaurus du projet
            listThesoTemp = userHelper.getListThesoLimitedRoleByUserAsAdmin(currentUser.getNodeUser().getIdUser());
            for (String idThesoTemp : listThesoTemp) {
                if(!authorizedThesoAsAdmin.contains(idThesoTemp)) {
                    authorizedThesoAsAdmin.add(idThesoTemp);
                }
            }
            
        }
        addAuthorizedThesoToHM();
        initAuthorizedThesoAsAdmin(currentUser);
        // permet de définir le role de l'utilisateur sur le group
        if (authorizedTheso.isEmpty()) {
            setUserRoleGroup(currentUser);
        } else {
            setUserRoleOnThisTheso(currentUser, selectedTheso);
        }
    }

    /**
     * Permet de vérifier après une connexion, si le thésaurus actuel est dans la liste des thésaurus authorisés pour modification
     * sinon, on nettoie l'interface et le thésaurus. 
     */
    public void redirectAndCleanTheso(SelectedTheso selectedTheso){
        if(!authorizedTheso.contains(selectedTheso.getCurrentIdTheso())){
            selectedTheso.setCurrentIdTheso(null);
            selectedTheso.setSelectedIdTheso(null);
            selectedTheso.setCurrentLang(null);
            selectedTheso.setSelectedLang(null);
            try {
                selectedTheso.setSelectedTheso();
            } catch (Exception e) {
            }
        }    
    }
    
    // on ajoute les thésaurus où l'utilisateur a le droit admin dessus
    private void initAuthorizedThesoAsAdmin(CurrentUser currentUser) {
        if (authorizedThesoAsAdmin == null) {
            return;
        }
        if(nodeListThesoAsAdmin == null)
            nodeListThesoAsAdmin = new ArrayList<>();
        else
            nodeListThesoAsAdmin.clear();
        HashMap<String, String> authorizedThesoAsAdminHM = new LinkedHashMap();
        
        // si c'est superAdmin, on prend tous les thésaurus
        if(currentUser.getNodeUser().isSuperAdmin()){
            for (NodeIdValue listTheso1 : currentUser.getUserPermissions().getListThesos()) {
                NodeIdValue nodeIdValue = new NodeIdValue();
                nodeIdValue.setId(listTheso1.getId());
                nodeIdValue.setValue(listTheso1.getValue());
                nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(listTheso1.getId()));
                nodeListThesoAsAdmin.add(nodeIdValue);
            }
        } else { // sinon, on prend les thésaurus où l'utilisateur a un role Admin 
            for (NodeProjectThesoRole nodeProjectsWithThesosRole : currentUser.getUserPermissions().getNodeProjectsWithThesosRoles()) {
                for (NodeThesoRole nodeThesoRole : nodeProjectsWithThesosRole.getNodeThesoRoles()) {
                    if(nodeThesoRole.getIdRole() == 2 || nodeThesoRole.getIdRole() == 1) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(nodeThesoRole.getIdTheso());
                        nodeIdValue.setValue(nodeThesoRole.getThesoName());
                        nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(nodeThesoRole.getIdTheso()));
                        nodeListThesoAsAdmin.add(nodeIdValue);
                    }
                }
            }
        }

        this.listThesoAsAdmin = authorizedThesoAsAdminHM;
    }    
            
            
    // on ajoute les titres + id, sinon l'identifiant du thésauurus
    public void addAuthorizedThesoToHM() {
        if (authorizedTheso == null) {
            return;
        }

        nodeListTheso = new ArrayList<>();

        // ajout de code qui permet de charger une liste de thésaurus avec le nom en utilisant la langue préférée dans les préférences du thésaurus.
        // pour éviter d'afficher des Id quand on a un mélange des thésaurus avec des langues sources différentes.
        
        listTheso = new ArrayList<>();
        
        for (String idTheso1 : authorizedTheso) {
            String preferredIdLangOfTheso = preferencesHelper.getWorkLanguageOfTheso(idTheso1);
            if (StringUtils.isEmpty(preferredIdLangOfTheso)) {
                preferredIdLangOfTheso = workLanguage.toLowerCase();
            }
            
            ThesoModel thesoModel = new ThesoModel();
            thesoModel.setId(idTheso1);

            String title = thesaurusHelper.getTitleOfThesaurus(idTheso1, preferredIdLangOfTheso);
            if (StringUtils.isEmpty(title)) {
                thesoModel.setNom("(" + idTheso1 + ")");
            } else {
                thesoModel.setNom(title + " (" + idTheso1 + ")");
            }

            thesoModel.setDefaultLang(preferencesHelper.getWorkLanguageOfTheso(idTheso1));
            
            listTheso.add(thesoModel);
            
            // nouvel objet pour récupérer la liste des thésaurus autorisée pour l'utilisateur en cours
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idTheso1);
            nodeIdValue.setValue(title);
            nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(idTheso1));
            nodeListTheso.add(nodeIdValue);
        }
        
        selectedThesoForSearch = new ArrayList<>();
        for (ThesoModel thesoModel : listTheso) {
            selectedThesoForSearch.add(thesoModel.getId());
        }
    }

    @Data
    public class ThesoModel implements Serializable{
        private String id;
        private String nom;
        private String defaultLang;
    }    

    /**
     * fonction pour récupérer la liste (sous forme de hashMap ) de thesaurus
     * tous les thésaurus sauf privés #MR
     *
     */
    public void setPublicThesos(CurrentUser currentUser, SelectedTheso selectedTheso) {
        currentUser.initAllTheso();
        authorizedTheso = thesaurusHelper.getAllIdOfThesaurus(false);
        addAuthorizedThesoToHM();
        setUserRoleOnThisTheso(currentUser, selectedTheso);
    }

    /**
     * Permet de définir le role d'un utilisateur sur le thésaurus en cours le
     * groupe du thésaurus est trouvé automatiquement, si l'utilisateur est
     * SuperAdmin, pas besoin du groupe
     *
     * #MR
     */
    public void setUserRoleOnThisTheso(CurrentUser currentUser, SelectedTheso selectedTheso) {

        isSuperAdmin = false;
        isAdminOnThisTheso = false;
        isManagerOnThisTheso = false;
        isContributorOnThisTheso = false;

        if (ObjectUtils.isEmpty(currentUser.getNodeUser())) {
            nodeUserRoleGroup = null;
            return;
        }

        if (ObjectUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            nodeUserRoleGroup = null;
            isAdminOnThisTheso = false;
            return;
        }
        int idGroup = userHelper.getGroupOfThisTheso(selectedTheso.getCurrentIdTheso());
        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeUserRoleGroup = getUserRoleOnThisGroup(-1, currentUser); // cas de superadmin, on a accès à tous les groupes
            setRole();
        } else {
            nodeUserRoleGroup = getUserRoleOnThisGroup(idGroup, currentUser);
        }

        if (ObjectUtils.isNotEmpty(nodeUserRoleGroup)) {
            setRole();
        } else {
            var user = userRepository.findById(currentUser.getNodeUser().getIdUser()).get();
            var group = userGroupLabelRepository.findById(idGroup).get();
            var thesaurus = thesaurusRepository.findById(selectedTheso.getCurrentIdTheso()).get();
            var tmp = userRoleOnlyOnRepository.findByUserAndGroupAndTheso(user, group, thesaurus);
            if(ObjectUtils.isNotEmpty(tmp)) {
                nodeUserRoleGroup = NodeUserRoleGroup.builder().idRole(tmp.getRole().getId()).build();
                setRole();
            } else
                isAdminOnThisTheso = false;
        }
    }

    private void setRole() {

        switch(nodeUserRoleGroup.getIdRole()) {
            case -1:
                isNoRole = true;
                break;
            case 1:
                isSuperAdmin = true;
                break;
            case 2:
                isAdminOnThisTheso = true;
                break;
            case 3:
                isManagerOnThisTheso = true;
                break;
            case 4:
                isContributorOnThisTheso = true;
                break;
        }
    }

    /**
     * retourne le role de l'utilisateur sur le group séléctionné
     *
     * #MR
     *
     * @param idGroup
     * @return
     */
    private NodeUserRoleGroup getUserRoleOnThisGroup(int idGroup, CurrentUser currentUser) {
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            var role = roleRepository.findById(1).get();
            return NodeUserRoleGroup.builder().idRole(role.getId()).roleName(role.getName()).build();
        }
        if (idGroup == -1) {
            return null;
        }
        return userHelper.getUserRoleOnThisGroup(currentUser.getNodeUser().getIdUser(), idGroup);
    }    
    
    /**
     * permet de récuperer le role de l'utilisateur sur le group applelé en cas
     * où le group n'a aucun thésaurus pour que l'utilisateur puisse créer des
     * thésaurus et gérer les utilisateur pour le group il faut être Admin
     */
    private void setUserRoleGroup(CurrentUser currentUser) {
        var nodeUserRoleGroups = userRoleGroupRepository.getUserRoleGroup(currentUser.getNodeUser().getIdUser());
        for (NodeUserRoleGroup nodeUserRoleGroup1 : nodeUserRoleGroups) {
            if (nodeUserRoleGroup1.isAdmin()) {
                isAdminOnThisTheso = true;
            }
        }
    }

    /**
     * permet de savoir si le thésaurus en cours n'est plus dans la liste des thésaurus autorisés
     * alors on nettoie initialise et on nettoie l'écran
     */
    public void setAndClearThesoInAuthorizedList(SelectedTheso selectedTheso) throws IOException{
        // vérification si le thésaurus supprimé est en cours de consultation, alors il faut nettoyer l'écran
        if(!authorizedTheso.contains(selectedTheso.getCurrentIdTheso())) {
            selectedTheso.setSelectedIdTheso(null);
            selectedTheso.setSelectedLang(null);
            selectedTheso.setSelectedTheso();
        } else
            selectedTheso.redirectToTheso();
    }
    
    public void showInfosOfTheso(String idTheso) {

        var conceptsCount = conceptStatusRepository.countValidConceptsByThesaurus(idTheso);

        var conceptsList = conceptRepository.findAllByThesaurusIdThesaurusAndStatus(idTheso, "CA");
        var candidatesCount = CollectionUtils.isNotEmpty(conceptsList) ? conceptsList.size() : 0;

        var deprecatedConceptsList = conceptRepository.findAllByThesaurusIdThesaurusAndStatus(idTheso, "DEP");
        var deprecatedCount = CollectionUtils.isNotEmpty(deprecatedConceptsList) ? deprecatedConceptsList.size() : 0;

        var message = new FacesMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("info"),
                languageBean.getMsg("candidat.total_concepts") + " = " + conceptsCount + "\n" 
                + languageBean.getMsg("candidat.titre") + " = " + candidatesCount + "\n"
                + languageBean.getMsg("search.deprecated") + " = " + deprecatedCount);
        
        PrimeFaces.current().dialog().showMessageDynamic(message);
    }

    public void accessAThesaurus(String idTheso) {
        this.thesoInfos = thesaurusRepository.getThesaurusByIdAndLang(idTheso, workLanguage)
                .orElseThrow(() -> new RuntimeException("Thesaurus not found: " + idTheso));
    }
    
    public boolean alignementVisible(CurrentUser currentUser) {
        return currentUser.getNodeUser() != null && (isManagerOnThisTheso || isAdminOnThisTheso || currentUser.getNodeUser().isSuperAdmin());
    }
}
