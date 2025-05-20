package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.entites.UserGroupThesaurus;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.userpermissions.NodeThesoRole;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.users.NodeUserComplet;
import fr.cnrs.opentheso.models.users.NodeUserRole;
import fr.cnrs.opentheso.models.users.NodeUserRoleGroup;
import fr.cnrs.opentheso.repositories.RoleRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;
import fr.cnrs.opentheso.repositories.UserRoleOnlyOnRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ThesaurusService thesaurusService;
    private final UserGroupThesaurusRepository userGroupThesaurusRepository;
    private final UserRoleOnlyOnRepository userRoleOnlyOnRepository;
    private final RoleRepository roleRepository;
    private final UserGroupLabelRepository userGroupLabelRepository;
    private final PreferenceService preferenceService;
    private final UserRoleGroupRepository userRoleGroupRepository;


    public List<NodeUser> searchUserByCriteria(String mail, String username) {
        log.info("Recherche des utilisateurs avec mail contenant '{}' et nom d'utilisateur contenant '{}'", mail, username);

        // Sécurité minimale contre null
        var safeMail = StringUtils.defaultString(mail);
        var safeUsername = StringUtils.defaultString(username);
        var users = userRepository.searchByMailAndUsername(safeMail, safeUsername);

        log.info("Nombre d'utilisateurs trouvés : {}", users.size());
        return users.stream().map(user -> {
            NodeUser nodeUser = new NodeUser();
            nodeUser.setIdUser(user.getId());
            nodeUser.setName(user.getUsername());
            nodeUser.setMail(user.getMail());
            nodeUser.setActive(user.getActive());
            nodeUser.setAlertMail(user.getAlertMail());
            nodeUser.setPassToModify(user.getPassToModify());
            nodeUser.setSuperAdmin(user.getIsSuperAdmin());
            nodeUser.setApiKey(user.getApiKey());
            nodeUser.setKeyNeverExpire(user.getKeyNeverExpire());
            nodeUser.setApiKeyExpireDate(user.getKeyExpiresAt());
            nodeUser.setServiceAccount(user.getIsServiceAccount());
            nodeUser.setKeyDescription(user.getKeyDescription());
            return nodeUser;
        }).toList();
    }

    public NodeUser getUser(int idUser) {
        log.info("Recherche des informations de l'utilisateur avec l'ID : {}", idUser);

        return userRepository.findById(idUser)
                .map(user -> {
                    NodeUser nodeUser = new NodeUser();
                    nodeUser.setIdUser(user.getId());
                    nodeUser.setName(user.getUsername());
                    nodeUser.setActive(user.getActive());
                    nodeUser.setMail(user.getMail());
                    nodeUser.setPassToModify(user.getPassToModify());
                    nodeUser.setAlertMail(user.getAlertMail());
                    nodeUser.setSuperAdmin(user.getIsSuperAdmin());
                    nodeUser.setApiKey(user.getApiKey());
                    nodeUser.setKeyNeverExpire(user.getKeyNeverExpire());
                    nodeUser.setApiKeyExpireDate(user.getKeyExpiresAt());
                    nodeUser.setServiceAccount(user.getIsServiceAccount());
                    nodeUser.setKeyDescription(user.getKeyDescription());
                    log.info("Utilisateur trouvé : {}", user.getUsername());
                    return nodeUser;
                })
                .orElseGet(() -> {
                    log.warn("Aucun utilisateur trouvé avec l'ID : {}", idUser);
                    return null;
                });
    }

    public NodeUserComplet getUserByLoginAndPassword(String login, String password) {

        log.info("Tentative de connexion pour l'utilisateur : {}", login);
        return userRepository.findByUsernameAndPassword(login, password)
                .map(user -> {
                    NodeUserComplet nodeUser = new NodeUserComplet();
                    nodeUser.setIdUser(user.getId());
                    nodeUser.setName(user.getUsername());
                    nodeUser.setActive(user.getActive());
                    nodeUser.setMail(user.getMail());
                    nodeUser.setAlertMail(user.getAlertMail());
                    nodeUser.setPassToModify(user.getPassToModify());
                    nodeUser.setSuperAdmin(user.getIsSuperAdmin());
                    nodeUser.setApiKey(user.getApiKey());
                    nodeUser.setKeyNeverExpire(user.getKeyNeverExpire());
                    nodeUser.setApiKeyExpireDate(user.getKeyExpiresAt());
                    nodeUser.setServiceAccount(user.getIsServiceAccount());
                    nodeUser.setKeyDescription(user.getKeyDescription());
                    log.info("Connexion réussie pour l'utilisateur : {}", login);
                    return nodeUser;
                })
                .orElseGet(() -> {
                    log.warn("Échec de la connexion : utilisateur introuvable ou mot de passe incorrect");
                    return null;
                });
    }

    public List<String> getListThesaurusLimitedRoleByUserAsAdmin(int idUser) {

        log.info("Rechercher les thésaurus ayant des utilisateurs avec des rôles limités");
        var user = userRepository.getById(idUser);
        var role = roleRepository.findById(2);
        var result = userRoleOnlyOnRepository.findAllByUserAndRoleOrderByThesaurus(user, role.get());
        if (CollectionUtils.isEmpty(result)) {
            log.info("Aucun thésaurus n'est trouvé !");
            return List.of();
        }

        log.info("{} thésaurus trouvé pour l'utilisateur id {}", result.size(), idUser);
        return result.stream().map(element -> element.getThesaurus().getIdThesaurus()).toList();
    }

    public User getById(int id) {

        return userRepository.getById(id);
    }

    public int getGroupOfThisThesaurus(String idThesaurus) {

        log.info("Recherche du group id associé au thésaurus id {}", idThesaurus);
        var thesaurus = thesaurusService.getThesaurusById(idThesaurus);
        var groupThesaurus = userGroupThesaurusRepository.findAllByThesaurus(thesaurus);
        return groupThesaurus.map(UserGroupThesaurus::getIdGroup).orElse(-1);
    }

    public Map<String, String> getGroupsOfUser(int userId) {
        log.info("Recherche des groupes pour l'utilisateur avec ID {}", userId);
        List<UserGroupLabel> groupLabels = userGroupLabelRepository.findAllGroupsOfUser(userId);

        if (groupLabels.isEmpty()) {
            log.warn("Aucun groupe trouvé pour l'utilisateur ID {}", userId);
        } else {
            log.info("{} groupe(s) trouvé(s) pour l'utilisateur ID {}", groupLabels.size(), userId);
        }

        return groupLabels.stream()
                .collect(Collectors.toMap(
                        g -> String.valueOf(g.getId()),
                        UserGroupLabel::getLabel,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    public List<UserGroupLabel> getProjectOfUser(int userId) {
        log.info("Recherche des projets pour l'utilisateur avec ID {}", userId);
        List<UserGroupLabel> labels = userGroupLabelRepository.findAllGroupsOfUser(userId);
        log.info("{} projet(s) trouvé(s)", labels.size());
        return labels;
    }

    public List<NodeIdValue> getThesaurusOfProject(int groupId, String lang) {
        log.info("Chargement des thésaurus pour le projet ID {}, langue = {}", groupId, lang);

        var groups = userGroupThesaurusRepository.findAllByIdGroup(groupId);

        List<NodeIdValue> thesaurusList = new ArrayList<>();
        for (UserGroupThesaurus group : groups) {
            String effectiveLang = Optional.ofNullable(preferenceService.getWorkLanguageOfThesaurus(group.getIdThesaurus())).orElse(lang);
            String title = thesaurusService.getTitleOfThesaurus(group.getIdThesaurus(), effectiveLang);

            NodeIdValue node = new NodeIdValue();
            node.setId(group.getIdThesaurus());
            node.setValue(title);
            node.setStatus(thesaurusService.getThesaurusById(group.getIdThesaurus()).getIsPrivate());
            thesaurusList.add(node);
        }

        log.info("{} thésaurus chargé(s) pour le projet ID {}", thesaurusList.size(), groupId);
        return thesaurusList;
    }

    public Optional<Integer> getUserGroupId(int userId, String thesoId) {
        log.info("Recherche de l'identifiant du groupe de l'utilisateur ID {} pour le thésaurus '{}'", userId, thesoId);
        try {
            return userRoleGroupRepository.findGroupIdByUserIdAndThesaurus(userId, thesoId);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du groupe pour l'utilisateur ID {} et thésaurus '{}'", userId, thesoId, e);
            return Optional.empty();
        }
    }

    public List<NodeIdValue> getThesaurusOfProject(int idProject, String idLang, boolean isPrivate) {
        log.info("Recherche des thésaurus du projet ID={}, visibilité={}, langue={}", idProject, isPrivate, idLang);

        List<String> thesaurusIds = userGroupThesaurusRepository.findThesaurusIdsByGroupAndVisibility(idProject, isPrivate);

        return thesaurusIds.stream().map(idTheso -> {
            String workingLang = preferenceService.getWorkLanguageOfThesaurus(idTheso);
            if (StringUtils.isEmpty(workingLang)) {
                workingLang = idLang;
            }

            String label = thesaurusService.getTitleOfThesaurus(idTheso, workingLang);
            boolean isThesoPrivate = thesaurusService.getThesaurusById(idTheso).getIsPrivate();

            NodeIdValue node = new NodeIdValue();
            node.setId(idTheso);
            node.setValue(label);
            node.setStatus(isThesoPrivate);
            return node;
        }).toList();
    }

    public int getRoleOnThisThesaurus(int idUser, int idGroup, String idTheso) {
        log.info("Recherche du rôle pour l'utilisateur ID={} sur le thésaurus '{}' et le groupe ID={}", idUser, idTheso, idGroup);

        return userRoleOnlyOnRepository.findRoleFromOnlyOn(idUser, idGroup, idTheso)
                .or(() -> userRoleOnlyOnRepository.findRoleFromGroup(idUser, idGroup, idTheso))
                .orElse(-1);
    }

    public NodeUserRoleGroup getUserRoleOnThisGroup(int idUser, int idGroup) {
        log.info("Recherche du rôle de l'utilisateur ID={} dans le groupe ID={}", idUser, idGroup);

        return userRoleGroupRepository.findUserRoleOnThisGroup(idUser, idGroup)
                .map(role -> {
                    switch (role.getIdRole()) {
                        case 2 -> role.setAdmin(true);
                        case 3 -> role.setManager(true);
                        case 4 -> role.setContributor(true);
                    }
                    return role;
                })
                .orElse(null);
    }

    public List<NodeUserRole> getUsersRolesByGroup(int idGroup, int idRole) {
        log.info("Chargement des utilisateurs du groupe ID={} avec rôle >= {}", idGroup, idRole);
        List<NodeUserRole> users = userRoleGroupRepository.findUsersByGroupAndRole(idGroup, idRole);
        log.info("{} utilisateur(s) récupéré(s) pour le groupe ID={}", users.size(), idGroup);
        return users;
    }

    public List<NodeUserRole> getAllUsersRolesLimitedByTheso(int idGroup) {
        log.info("Chargement des utilisateurs avec rôles limités par thésaurus dans le groupe ID={}", idGroup);

        var users = userRoleOnlyOnRepository.findAllUsersWithLimitedRoleByGroup(idGroup);

        for (NodeUserRole user : users) {
            String idLang = preferenceService.getWorkLanguageOfThesaurus(user.getIdTheso());
            user.setThesoName(thesaurusService.getTitleOfThesaurus(user.getIdTheso(), idLang));
        }

        log.info("{} utilisateur(s) trouvé(s) avec des rôles limités par thésaurus dans le groupe ID={}", users.size(), idGroup);
        return users;
    }

    public List<String> getThesaurusOfUser(int idUser) {
        log.info("Recherche des thésaurus pour l'utilisateur ID={}", idUser);

        List<String> thesaurusIds = userGroupThesaurusRepository.findThesaurusIdsByUserId(idUser);

        log.info("{} thésaurus trouvé(s) pour l'utilisateur ID={}", thesaurusIds.size(), idUser);
        return thesaurusIds;
    }

    public List<NodeThesoRole> getAllRolesThesosByUserGroup(int idGroup, int idUser) {

        log.info("Recherche des roles/thesaurus pour l'utilisateur id {} et un group id {}", idUser, idGroup);
        ArrayList<NodeThesoRole> nodeThesoRoles = new ArrayList<>();
        var listThesaurus = getIdThesaurusOfProject(idGroup);
        for (String idThesaurus : listThesaurus) {
            var idRole = getRoleOnThisThesaurus(idUser, idGroup, idThesaurus);
            if (idRole != -1) {
                NodeThesoRole nodeThesoRole = new NodeThesoRole();
                nodeThesoRole.setIdRole(idRole);
                nodeThesoRole.setRoleName(getRoleName(idRole));
                nodeThesoRole.setIdTheso(idThesaurus);
                var idLang = preferenceService.getWorkLanguageOfThesaurus(idThesaurus);
                nodeThesoRole.setThesoName(thesaurusService.getTitleOfThesaurus(idThesaurus, idLang));
                nodeThesoRoles.add(nodeThesoRole);
            }
        }
        return nodeThesoRoles;
    }

    private List<String> getIdThesaurusOfProject(int idProject) {
        log.info("Chargement des thésaurus pour le projet ID={}", idProject);
        List<String> thesaurusIds = userGroupThesaurusRepository.findThesaurusIdsByGroupId(idProject);
        log.info("{} thésaurus trouvés pour le projet ID={}", thesaurusIds.size(), idProject);
        return thesaurusIds;
    }

    public String getRoleName(int idRole) {
        switch (idRole) {
            case 1:
                return "superAdmin";
            case 2:
                return "admin";
            case 3:
                return "manager";
            case 4:
                return "contributor";
            default:
                return "";
        }
    }

    public List<String> getThesaurusOfUserAsAdmin(int idUser) {
        log.info("Recherche des thésaurus où l'utilisateur ID={} est administrateur...", idUser);

        List<String> thesaurusIds = userGroupThesaurusRepository.findThesaurusIdsWhereUserIsAdmin(idUser);
        if (thesaurusIds.isEmpty()) {
            log.info("Aucun thésaurus trouvé pour l'utilisateur ID={} avec un rôle admin.", idUser);
        } else {
            log.info("{} thésaurus trouvés pour l'utilisateur ID={} avec un rôle admin.", thesaurusIds.size(), idUser);
        }

        return thesaurusIds;
    }
}
