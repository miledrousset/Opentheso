package fr.cnrs.opentheso.bean.menu.theso;

import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.models.userpermissions.NodeProjectThesoRole;
import fr.cnrs.opentheso.models.userpermissions.NodeThesoRole;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.ThesaurusService;
import fr.cnrs.opentheso.services.UserRoleGroupService;
import fr.cnrs.opentheso.services.UserService;
import fr.cnrs.opentheso.services.statistiques.StatistiqueService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Value;
import lombok.Data;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.io.Serializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "roleOnThesaurus")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RoleOnThesaurusBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private final UserService userService;
    private final LanguageBean languageBean;
    private final SelectedTheso selectedTheso;
    private final ConceptService conceptService;
    private final ThesaurusService thesaurusService;
    private final PreferenceService preferenceService;
    private final StatistiqueService statistiqueService;
    private final UserRoleGroupService userRoleGroupService;

    private List<ThesaurusModel> listThesaurus;
    private Map<String, String> listThesaurusAsAdmin;
    private List<NodeIdValue> nodeListThesaurus, nodeListThesaurusAsAdmin, nodeListThesaurusAsAdminFiltered;
    private boolean isSuperAdmin, isAdminOnThisThesaurus,isManagerOnThisThesaurus, isContributorOnThisThesaurus, isNoRole;
    private List<String> selectedThesaurusForSearch, authorizedThesaurus, authorizedThesaurusAsAdmin;
    private Preferences nodePreference;
    private NodeUserRoleGroup nodeUserRoleGroup;
    private TreeNode<NodeIdValue> root;


    public void initNodePref(SelectedTheso selectedTheso) {
        if (selectedTheso.getCurrentIdTheso() == null) {
            return;
        }

        nodePreference = preferenceService.getThesaurusPreferences(selectedTheso.getCurrentIdTheso());
        if (nodePreference == null) { // cas où il n'y a pas de préférence pour ce thésaurus, il faut les créer
            preferenceService.initPreferences(selectedTheso.getCurrentIdTheso(), workLanguage);
            nodePreference = preferenceService.getThesaurusPreferences(selectedTheso.getCurrentIdTheso());
        }
    }

    public void initNodePref(String idTheso) {
        if (idTheso == null) {
            return;
        }

        nodePreference = preferenceService.getThesaurusPreferences(idTheso);
        if (nodePreference == null) { // cas où il n'y a pas de préférence pour ce thésaurus, il faut les créer
            preferenceService.initPreferences(idTheso, workLanguage);
            nodePreference = preferenceService.getThesaurusPreferences(idTheso);
        }
    }

    /**
     * permet d'initialiser la liste des thésaurus suivant les droits
     */
    public void showListThesaurus(CurrentUser currentUser, String selectedIdThesaurus) {
        if (currentUser.getNodeUser() == null) {
            setPublicThesaurus(currentUser, selectedIdThesaurus);
        } else {
            setOwnerThesaurus(currentUser, selectedIdThesaurus);
        }
    }

    /**
     * fonction pour sortir une liste (sous forme de hashMap ) de thesaurus
     * correspondant à l'utilisateur connecté permet de charger les thésaurus
     * autorisés pour l'utilisateur en cours on récupère les id puis les
     * tradcutions (ceci permet de récupérer les thésaurus non traduits) #MR
     */
    private void setOwnerThesaurus(CurrentUser currentUser, String selectedIdThesaurus) {
        if (currentUser.getNodeUser() == null) {
            this.listThesaurus = new ArrayList<>();
            setUserRoleOnThisThesaurus(currentUser, selectedIdThesaurus);
            return;
        }

        currentUser.initUserPermissions();
        
        authorizedThesaurus = new ArrayList<>();
        if (currentUser.getNodeUser().isSuperAdmin()) {
            boolean withPrivateTheso = true;
            authorizedThesaurus = thesaurusService.getAllIdOfThesaurus(withPrivateTheso);
            authorizedThesaurusAsAdmin = thesaurusService.getAllIdOfThesaurus(withPrivateTheso);

        } else {
            authorizedThesaurus = userService.getThesaurusOfUser(currentUser.getNodeUser().getIdUser());
            // récupération de la liste des thésaurus pour les utilisateurs qui n'ont pas des droits sur un projet, mais uniquement sur des thésaurus du projet
            List<String> listThesoTemp = userService.getAllThesaurusByUsers(currentUser.getNodeUser().getIdUser()).stream()
                    .map(element -> element.getThesaurus().getIdThesaurus())
                    .toList();
            for (String idThesoTemp : listThesoTemp) {
                if(!authorizedThesaurus.contains(idThesoTemp)) {
                    authorizedThesaurus.add(idThesoTemp);
                }
            }

            authorizedThesaurusAsAdmin = userService.getThesaurusOfUserAsAdmin(currentUser.getNodeUser().getIdUser());
            
            // récupération de la liste des thésaurus pour les utilisateurs avec les droits admin, mais qui n'ont pas des droits sur un projet, mais uniquement sur des thésaurus du projet
            listThesoTemp = userService.getListThesaurusLimitedRoleByUserAsAdmin(currentUser.getNodeUser().getIdUser());
            for (String idThesoTemp : listThesoTemp) {
                if(!authorizedThesaurusAsAdmin.contains(idThesoTemp)) {
                    authorizedThesaurusAsAdmin.add(idThesoTemp);
                }
            }
            
        }
        addAuthorizedThesoToHM();
        initAuthorizedThesaurusAsAdmin(currentUser);
        // permet de définir le role de l'utilisateur sur le group
        if (authorizedThesaurus.isEmpty()) {
            setUserRoleGroup(currentUser);
        } else {
            setUserRoleOnThisThesaurus(currentUser, selectedIdThesaurus);
        }
    }

    /**
     * Permet de vérifier après une connexion, si le thésaurus actuel est dans la liste des thésaurus authorisés pour modification
     * sinon, on nettoie l'interface et le thésaurus. 
     */
    public void redirectAndCleanThesaurus(SelectedTheso selectedTheso) throws IOException {
        if(!authorizedThesaurus.contains(selectedTheso.getCurrentIdTheso())){
            selectedTheso.setCurrentIdTheso(null);
            selectedTheso.setSelectedIdTheso(null);
            selectedTheso.setCurrentLang(null);
            selectedTheso.setSelectedLang(null);
            selectedTheso.setSelectedTheso();
        }    
    }
    
    // on ajoute les thésaurus où l'utilisateur a le droit admin dessus
    private void initAuthorizedThesaurusAsAdmin(CurrentUser currentUser) {
        if (authorizedThesaurusAsAdmin == null) {
            return;
        }
        nodeListThesaurusAsAdmin = new ArrayList<>();
        HashMap<String, String> authorizedThesaurusAsAdminHM = new LinkedHashMap<>();
        
        // si c'est superAdmin, on prend tous les thésaurus
        if(currentUser.getNodeUser().isSuperAdmin()){
            for (NodeIdValue listThesaurus : currentUser.getUserPermissions().getListThesaurus()) {
                var thesaurus = thesaurusService.getThesaurusById(listThesaurus.getId());
                NodeIdValue nodeIdValue = new NodeIdValue();
                nodeIdValue.setId(listThesaurus.getId());
                nodeIdValue.setValue(listThesaurus.getValue());
                nodeIdValue.setStatus(thesaurus.getIsPrivate());
                nodeIdValue.setCreationDate(thesaurus.getCreated());
                nodeListThesaurusAsAdmin.add(nodeIdValue);
            }
        } else { // sinon, on prend les thésaurus où l'utilisateur a un role Admin 
            for (NodeProjectThesoRole nodeProjectsWithThesaurusRole : currentUser.getUserPermissions().getNodeProjectsWithThesosRoles()) {
                for (NodeThesoRole nodeThesoRole : nodeProjectsWithThesaurusRole.getNodeThesoRoles()) {
                    if(nodeThesoRole.getIdRole() == 2 || nodeThesoRole.getIdRole() == 1) {
                        var thesaurus = thesaurusService.getThesaurusById(nodeThesoRole.getIdTheso());
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(nodeThesoRole.getIdTheso());
                        nodeIdValue.setValue(nodeThesoRole.getThesoName());
                        nodeIdValue.setStatus(thesaurus.getIsPrivate());
                        nodeIdValue.setCreationDate(thesaurus.getCreated());
                        nodeListThesaurusAsAdmin.add(nodeIdValue);
                    }
                }
            }
        }

        listThesaurusAsAdmin = authorizedThesaurusAsAdminHM;
        nodeListThesaurusAsAdmin = nodeListThesaurusAsAdmin.stream()
                .sorted(Comparator.comparing(NodeIdValue::getCreationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }    
            
            
    // on ajoute les titres + id, sinon l'identifiant du thésauurus
    public void addAuthorizedThesoToHM() {
        if (authorizedThesaurus == null) {
            return;
        }

        nodeListThesaurus = new ArrayList<>();

        // ajout de code qui permet de charger une liste de thésaurus avec le nom en utilisant la langue préférée dans les préférences du thésaurus.
        // pour éviter d'afficher des Id quand on a un mélange des thésaurus avec des langues sources différentes.
        
        listThesaurus = new ArrayList<>();
        
        for (String idTheso1 : authorizedThesaurus) {
            String preferredIdLangOfTheso = preferenceService.getWorkLanguageOfThesaurus(idTheso1);
            if (StringUtils.isEmpty(preferredIdLangOfTheso)) {
                preferredIdLangOfTheso = workLanguage.toLowerCase();
            }
            
            ThesaurusModel thesoModel = new ThesaurusModel();
            thesoModel.setId(idTheso1);

            String title = thesaurusService.getTitleOfThesaurus(idTheso1, preferredIdLangOfTheso);
            if (StringUtils.isEmpty(title)) {
                thesoModel.setNom("(" + idTheso1 + ")");
            } else {
                thesoModel.setNom(title + " (" + idTheso1 + ")");
            }

            thesoModel.setDefaultLang(preferenceService.getWorkLanguageOfThesaurus(idTheso1));
            
            listThesaurus.add(thesoModel);

            var thesaurus = thesaurusService.getThesaurusById(idTheso1);

            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idTheso1);
            nodeIdValue.setValue(title);
            nodeIdValue.setStatus(thesaurus.getIsPrivate());
            nodeListThesaurus.add(nodeIdValue);
        }
        
        selectedThesaurusForSearch = new ArrayList<>();
        for (ThesaurusModel thesoModel : listThesaurus) {
            selectedThesaurusForSearch.add(thesoModel.getId());
        }
    }

    @Data
    public static class ThesaurusModel implements Serializable{
        private String id;
        private String nom;
        private String defaultLang;
    }    

    /**
     * fonction pour récupérer la liste (sous forme de hashMap ) de thesaurus
     * tous les thésaurus sauf privés #MR
     *
     */
    public void setPublicThesaurus(CurrentUser currentUser, String selectedIdThesaurus) {
        currentUser.initAllTheso();
        authorizedThesaurus = thesaurusService.getAllIdOfThesaurus(false);
        addAuthorizedThesoToHM();
        setUserRoleOnThisThesaurus(currentUser, selectedIdThesaurus);
    }

    /**
     * Permet de définir le role d'un utilisateur sur le thésaurus en cours le
     * groupe du thésaurus est trouvé automatiquement, si l'utilisateur est
     * SuperAdmin, pas besoin du groupe
     * #MR
     */
    public void setUserRoleOnThisThesaurus(CurrentUser currentUser, String selectedIdThesaurus) {

        isSuperAdmin = false;
        isAdminOnThisThesaurus = false;
        isManagerOnThisThesaurus = false;
        isContributorOnThisThesaurus = false;

        if (ObjectUtils.isEmpty(currentUser.getNodeUser())) {
            nodeUserRoleGroup = null;
            return;
        }

        if (ObjectUtils.isEmpty(selectedIdThesaurus)) {
            nodeUserRoleGroup = null;
            isAdminOnThisThesaurus = false;
            return;
        }

        int idGroup = userService.getGroupOfThisThesaurus(selectedIdThesaurus);
        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeUserRoleGroup = getUserRoleOnThisGroup(-1, currentUser); // cas de superadmin, on a accès à tous les groupes
            setRole();
        } else {
            nodeUserRoleGroup = getUserRoleOnThisGroup(idGroup, currentUser);
        }

        if (ObjectUtils.isNotEmpty(nodeUserRoleGroup)) {
            setRole();
        } else {
            var user = userService.getById(currentUser.getNodeUser().getIdUser());
            var group = userRoleGroupService.getUserGroupLabelRepository(idGroup);
            var thesaurus = thesaurusService.getThesaurusById(selectedIdThesaurus);
            var tmp = userRoleGroupService.getRole(user, group, thesaurus);
            if(ObjectUtils.isNotEmpty(tmp)) {
                nodeUserRoleGroup = NodeUserRoleGroup.builder().idRole(tmp.getRole().getId()).build();
                setRole();
            } else
                isAdminOnThisThesaurus = false;
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
                isAdminOnThisThesaurus = true;
                break;
            case 3:
                isManagerOnThisThesaurus = true;
                break;
            case 4:
                isContributorOnThisThesaurus = true;
                break;
        }
    }

    /**
     * retourne le role de l'utilisateur sur le group séléctionné
     */
    private NodeUserRoleGroup getUserRoleOnThisGroup(int idGroup, CurrentUser currentUser) {
        if (currentUser.getNodeUser().isSuperAdmin()) {
            var role = userRoleGroupService.getRoleById(1);
            return NodeUserRoleGroup.builder().idRole(role.getId()).roleName(role.getName()).build();
        }
        if (idGroup == -1) {
            return null;
        }
        return userService.getUserRoleOnThisGroup(currentUser.getNodeUser().getIdUser(), idGroup);
    }    
    
    /**
     * permet de récuperer le role de l'utilisateur sur le group applelé en cas
     * où le group n'a aucun thésaurus pour que l'utilisateur puisse créer des
     * thésaurus et gérer les utilisateur pour le group il faut être Admin
     */
    private void setUserRoleGroup(CurrentUser currentUser) {
        var nodeUserRoleGroups = userRoleGroupService.getRoleProjectByUser(currentUser.getNodeUser().getIdUser());
        for (NodeUserRoleGroup nodeUserRoleGroup1 : nodeUserRoleGroups) {
            if (nodeUserRoleGroup1.isAdmin()) {
                isAdminOnThisThesaurus = true;
                break;
            }
        }
    }

    /**
     * permet de savoir si le thésaurus en cours n'est plus dans la liste des thésaurus autorisés
     * alors on nettoie initialise et on nettoie l'écran
     */
    public void setAndClearThesoInAuthorizedList(SelectedTheso selectedTheso) throws IOException{
        // vérification si le thésaurus supprimé est en cours de consultation, alors il faut nettoyer l'écran
        if(!authorizedThesaurus.contains(selectedTheso.getCurrentIdTheso())) {
            selectedTheso.setSelectedIdTheso(null);
            selectedTheso.setSelectedLang(null);
            selectedTheso.setSelectedTheso();
        } else
            selectedTheso.redirectToTheso();
    }
    
    public boolean alignementVisible(CurrentUser currentUser) {
        return currentUser.getNodeUser() != null && (isManagerOnThisThesaurus || isAdminOnThisThesaurus || currentUser.getNodeUser().isSuperAdmin());
    }

    public boolean isPropositionAuthorized() {

        var preference = preferenceService.getThesaurusPreferences(selectedTheso.getCurrentIdTheso());
        return preference != null && preference.isSuggestion();
    }
}
