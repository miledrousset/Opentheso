package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.UserGroupThesaurus;
import fr.cnrs.opentheso.models.concept.NodeMetaData;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.utils.MessageUtils;
import fr.cnrs.opentheso.ws.ark.ArkHelper2;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.TreeNode;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class EditThesaurusService {

    private final GroupService groupService;
    private final ThesaurusService thesaurusService;
    private final PreferenceService preferenceService;


    public void addNewThesaurus(String title, String selectedLang, String selectedProject, String userName) {

        int idProject = -1;
        try {
            if (!StringUtils.isEmpty(selectedProject)) {
                idProject = Integer.parseInt(selectedProject);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalide project id {}", selectedProject);
        }

        // création du thésaurus
        var idNewThesaurus = thesaurusService.addThesaurusRollBack();
        if(idNewThesaurus == null) {
            MessageUtils.showErrorMessage("Erreur pendant la création");
            return;
        }

        var thesaurus = new fr.cnrs.opentheso.models.thesaurus.Thesaurus();
        thesaurus.setCreator(userName);
        thesaurus.setContributor(userName);
        thesaurus.setId_thesaurus(idNewThesaurus);
        thesaurus.setTitle(title);
        thesaurus.setLanguage(selectedLang);
        thesaurusService.addThesaurusTraductionRollBack(thesaurus);

        // ajouter le thésaurus dans le group de l'utilisateur
        if (idProject != -1) { // si le groupeUser = - 1, c'est le cas d'un SuperAdmin, alors on n'intègre pas le thésaurus dans un groupUser
            var userGroupThesaurus = UserGroupThesaurus.builder().idThesaurus(idNewThesaurus).idGroup(idProject).build();
            groupService.saveUserGroupThesaurus(userGroupThesaurus);
        }

        // écriture des préférences en utilisant le thésaurus en cours pour duppliquer les infos
        preferenceService.initPreferences(idNewThesaurus, selectedLang);
    }

    public String generateArkIdForThesaurus(String idThesaurus) {

        log.debug("Regénération d'un identifiant Ark pour le thésaurus id {}", idThesaurus);
        var preferences = preferenceService.getThesaurusPreferences(idThesaurus);
        if (preferences == null) {
            log.error("Erreur: Veuillez paramétrer les préférences pour ce thésaurus !!");
            return null;
        }

        var nodeThesaurus = thesaurusService.getNodeThesaurus(idThesaurus);
        if (preferences.isUseArk()) {
            ArkHelper2 arkHelper2 = new ArkHelper2(preferences);
            if (!arkHelper2.login()) {
                log.error("Erreur de connexion !!");
                return null;
            }

            if (!preferences.isUseArk()) {
                log.error("Erreur: Veuillez activer Ark dans les préférences !!");
                return null;
            }
            var nodeMetaData = new NodeMetaData();
            nodeMetaData.setDcElementsList(new ArrayList<>());
            nodeMetaData.setTitle(nodeThesaurus.getIdThesaurus());
            nodeMetaData.setSource(preferences.getPreferredName());
            nodeMetaData.setCreator("");
            var privateUri = "?idt=" + idThesaurus;
            if (StringUtils.isEmpty(nodeThesaurus.getIdArk())) {
                if (!arkHelper2.addArk(privateUri, nodeMetaData)) {
                    log.error(arkHelper2.getMessage() + "  idThesaurus = " + nodeThesaurus.getIdThesaurus());
                    log.error("La création Ark a échoué ici : " + nodeThesaurus.getIdThesaurus());
                    return null;
                }
                if (thesaurusService.updateIdArkOfThesaurus(idThesaurus, arkHelper2.getIdArk())) {
                    return null;
                }
                return nodeThesaurus.getIdArk();
            }
            return arkHelper2.getIdArk();
        }
        if (preferences.isUseArkLocal()) {
            String idArk = nodeThesaurus.getIdArk();
            if (StringUtils.isEmpty(idArk)) {
                idArk = preferences.getNaanArkLocal() + "/" + preferences.getPrefixArkLocal() + idArk;
                if (thesaurusService.updateIdArkOfThesaurus(idThesaurus, idArk)) {
                    return null;
                }
            }

            return StringUtils.isEmpty(idArk) ? "" : idArk;
        }
        return null;
    }

    public void updateCollectionsStatus(TreeNode<NodeGroup> element, boolean newStatus) {
        for (TreeNode<NodeGroup> group : element.getChildren()) {
            groupService.setGroupVisibility(group.getData().getConceptGroup().getIdGroup(),
                    group.getData().getConceptGroup().getIdThesaurus(), newStatus);
            group.getData().setGroupPrivate(newStatus);

            if (CollectionUtils.isNotEmpty(element.getChildren())) {
                for (TreeNode<NodeGroup> tmp : element.getChildren()) {
                    updateCollectionsStatus(tmp, newStatus);
                }
            }
        }
    }

    public TreeNode<NodeGroup> getTreeNode(List<TreeNode<NodeGroup>> nodes, NodeGroup group) {

        TreeNode<NodeGroup> tmp = null;
        for (TreeNode<NodeGroup> node : nodes) {
            if (node.getData().getConceptGroup().getIdGroup().equals(group.getConceptGroup().getIdGroup())) {
                return node;
            }
            if (CollectionUtils.isNotEmpty(node.getChildren())) {
                tmp = getTreeNode(node.getChildren(), group);
            }
        }
        return tmp;
    }
}
