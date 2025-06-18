package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import fr.cnrs.opentheso.models.concept.NodeAutoCompletion;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.GroupTypeService;
import fr.cnrs.opentheso.services.RelationGroupService;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.model.SelectItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "modifyGroupBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ModifyGroupBean implements Serializable {

    private final TreeGroups treeGroups;
    private final GroupView groupView;
    private final CurrentUser currentUser;
    private final GroupService groupService;
    private final SelectedTheso selectedTheso;
    private final GroupTypeService groupTypeService;
    private final RelationGroupService relationGroupService;

    private boolean moveToRoot = false;
    private List<SelectItem> listGroupType;
    private String selectedGroupType, idGroup, titleGroup, notation;
    private NodeAutoCompletion selectedNodeAutoCompletionGroup;


    public void init() {
        moveToRoot = false;
        idGroup = groupView.getNodeGroup().getConceptGroup().getIdGroup();
        titleGroup = groupView.getNodeGroup().getLexicalValue();
        notation = groupView.getNodeGroup().getConceptGroup().getNotation();
        selectedGroupType = groupView.getNodeGroup().getConceptGroup().getIdTypeCode();

        listGroupType = groupTypeService.getAllGroupType();
    }

    public void moveGroupTo(){
        if(StringUtils.isEmpty(idGroup)){
            MessageUtils.showErrorMessage("Pas de sélection !");
            return;
        }

        String idParent = relationGroupService.getIdFather(idGroup, selectedTheso.getCurrentIdTheso());

        if(isMoveToRoot()) {
            if(!StringUtils.isEmpty(idParent)) {
                relationGroupService.removeGroupFromGroup(idGroup, idParent, selectedTheso.getCurrentIdTheso());
            } else {
                MessageUtils.showErrorMessage("Déplacement à la même place !");
                return;
            }
        } else {

            if(selectedNodeAutoCompletionGroup == null || StringUtils.isEmpty(selectedNodeAutoCompletionGroup.getIdGroup())){
                MessageUtils.showErrorMessage("Pas de sélection !");
                return;
            }

            if(selectedNodeAutoCompletionGroup.getIdGroup().equalsIgnoreCase(idGroup)) {
                MessageUtils.showErrorMessage("Déplacement impossible !");
                return;
            }

            /// contrôle si le groupe est à déplacer dans la même hiérarchie, c'est interdit
            if(groupService.isMoveToDescending(idGroup, selectedNodeAutoCompletionGroup.getIdGroup(), selectedTheso.getCurrentIdTheso())){
                MessageUtils.showErrorMessage(" Déplacement impossible !");
                return;
            }


            if(!StringUtils.isEmpty(idParent)) {
                if(selectedNodeAutoCompletionGroup.getIdGroup().equalsIgnoreCase(idParent)) {
                    MessageUtils.showErrorMessage("Déplacement à la même place !");
                    return;
                }

                relationGroupService.removeGroupFromGroup(idGroup, idParent, selectedTheso.getCurrentIdTheso());
            }
            relationGroupService.addSubGroup(selectedNodeAutoCompletionGroup.getIdGroup(), idGroup, selectedTheso.getCurrentIdTheso());
        }

        selectedTheso.reloadGroups();
        MessageUtils.showInformationMessage("Déplacement réussi !");
    }

    /**
     * permet de retourner la liste des groupes / collections contenus dans le thésaurus
     */
    public List<NodeAutoCompletion> getAutoCompletCollection(String value) {
        selectedNodeAutoCompletionGroup = new NodeAutoCompletion();
        List<NodeAutoCompletion> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            liste = groupService.getAutoCompletionGroup(selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang(), value);
        }
        return liste;
    }

    /**
     * Modification du label du gourpe
     *
     */
    public void renameGroup() {

        if (titleGroup.isEmpty()) {
            MessageUtils.showErrorMessage("Le label ne doit pas être vide !");
            return;
        }

        if (groupService.isDomainExist(titleGroup, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang())) {
            MessageUtils.showErrorMessage("Un group existe déjà avec ce nom !");
            return;
        }
        if(groupService.isHaveTraduction(idGroup, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang())){
            if (groupService.renameGroup(titleGroup, selectedTheso.getCurrentLang(), idGroup, selectedTheso.getCurrentIdTheso(),
                    currentUser.getNodeUser().getIdUser())) {
                MessageUtils.showErrorMessage("Erreur lors de la modification du label !");
                return;
            }
        } else {
            groupService.addGroupTraduction(idGroup, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang(), titleGroup);
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(), idGroup, groupView.getNodeGroup().getIdLang());

        MessageUtils.showInformationMessage("Libellé modifié avec succès !");

        if (treeGroups.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre

            // sinon, on modifie le label
            if (((TreeNodeData) treeGroups.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    groupView.getNodeGroup().getConceptGroup().getIdGroup())) {
                ((TreeNodeData) treeGroups.getSelectedNode().getData()).setName(titleGroup);
            }
            PrimeFaces.current().ajax().update("containerIndex:formLeftTab:tabTree:treeGroups");
        }

        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    public void updateNotation() {

        if (notation == null) {
            MessageUtils.showErrorMessage(" Notation ne doit pas être null !");
            return;
        }

        if (groupService.isNotationExist(notation, selectedTheso.getCurrentIdTheso())) {
            MessageUtils.showErrorMessage(" La notation existe déjà !");
            return;
        }

        if (!groupService.setNotationOfGroup(notation, idGroup, selectedTheso.getCurrentIdTheso())) {
            MessageUtils.showErrorMessage(" Erreur pendant la modification !");
            return;
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(), idGroup, groupView.getNodeGroup().getIdLang());

        MessageUtils.showInformationMessage("Notation modifiée avec succès !");

        if (treeGroups.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre

            // sinon, on modifie le label
            if (((TreeNodeData) treeGroups.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    groupView.getNodeGroup().getConceptGroup().getIdGroup())) {
                ((TreeNodeData) treeGroups.getSelectedNode().getData()).setNotation(notation);
            }
        }

        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        PrimeFaces.current().ajax().update("containerIndex:formLeftTab:treeGroups");
    }

    public void updateGroupType() {

        if (selectedGroupType == null || selectedGroupType.isEmpty()) {
            MessageUtils.showErrorMessage(" Le Type de groupe ne doit pas être vide !");
            return;
        }

        if (!groupTypeService.updateTypeGroup(selectedGroupType, selectedTheso.getCurrentIdTheso(), idGroup)) {
            MessageUtils.showErrorMessage(" Erreur pendant la modification !");
            return;
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(), idGroup, groupView.getNodeGroup().getIdLang());

        MessageUtils.showInformationMessage("Groupe modifié avec succès !");
        PrimeFaces.current().ajax().update("containerIndex:formLeftTab:treeGroups");
    }
}
