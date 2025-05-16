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
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 *
 * @author miledrousset
 */
@Data
@SessionScoped
@Named(value = "modifyGroupBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ModifyGroupBean implements Serializable {

    @Autowired @Lazy
    private TreeGroups treeGroups;
    @Autowired @Lazy
    private GroupView groupView;
    @Autowired @Lazy
    private SelectedTheso selectedTheso;
    @Autowired @Lazy
    private CurrentUser currentUser;

    @Autowired
    private GroupTypeService groupTypeService;

    @Autowired
    private GroupService groupService;

    private String selectedGroupType;
    private String idGroup;
    private String titleGroup;
    private String notation;
    private List<SelectItem> listGroupType;

    private NodeAutoCompletion selectedNodeAutoCompletionGroup;
    private boolean moveToRoot = false;
    @Autowired
    private RelationGroupService relationGroupService;


    public void init() {
        moveToRoot = false;
        idGroup = groupView.getNodeGroup().getConceptGroup().getIdGroup();
        titleGroup = groupView.getNodeGroup().getLexicalValue();
        notation = groupView.getNodeGroup().getConceptGroup().getNotation();
        selectedGroupType = groupView.getNodeGroup().getConceptGroup().getIdTypeCode();

        listGroupType = groupTypeService.getAllGroupType();
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Group !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }


    public void moveGroupTo(){
        if(StringUtils.isEmpty(idGroup)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        String idParent = relationGroupService.getIdFather(idGroup, selectedTheso.getCurrentIdTheso());

        if(isMoveToRoot()) {
            if(!StringUtils.isEmpty(idParent)) {
                relationGroupService.removeGroupFromGroup(idGroup, idParent, selectedTheso.getCurrentIdTheso());
            } else {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Déplacement à la même place !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        } else {

            if(selectedNodeAutoCompletionGroup == null || StringUtils.isEmpty(selectedNodeAutoCompletionGroup.getIdGroup())){
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Pas de sélection !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }

            if(selectedNodeAutoCompletionGroup.getIdGroup().equalsIgnoreCase(idGroup)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Déplacement impossible !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }

            /// contrôle si le groupe est à déplacer dans la même hiérarchie, c'est interdit
            if(groupService.isMoveToDescending(idGroup, selectedNodeAutoCompletionGroup.getIdGroup(), selectedTheso.getCurrentIdTheso())){
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Déplacement impossible !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }


            if(!StringUtils.isEmpty(idParent)) {
                if(selectedNodeAutoCompletionGroup.getIdGroup().equalsIgnoreCase(idParent)) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Déplacement à la même place !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    return;
                }

                relationGroupService.removeGroupFromGroup(idGroup, idParent, selectedTheso.getCurrentIdTheso());
            }
            relationGroupService.addSubGroup(selectedNodeAutoCompletionGroup.getIdGroup(), idGroup, selectedTheso.getCurrentIdTheso());
        }

        selectedTheso.reloadGroups();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", " Déplacement réussi !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de retourner la liste des groupes / collections contenus dans le
     * thésaurus
     *
     * @param value
     * @return
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
        FacesMessage msg;

        PrimeFaces pf = PrimeFaces.current();

        if (titleGroup.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, " ", " Le label ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (groupService.isDomainExist(titleGroup, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, " ", " un group existe déjà avec ce nom !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        if(groupService.isHaveTraduction(idGroup, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang())){
            if (groupService.renameGroup(titleGroup, selectedTheso.getCurrentLang(), idGroup, selectedTheso.getCurrentIdTheso(),
                    currentUser.getNodeUser().getIdUser())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Erreur lors de la modification du label !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }
        } else {
            groupService.addGroupTraduction(idGroup, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang(), titleGroup);
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(), idGroup, groupView.getNodeGroup().getIdLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " Libellé modifié avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (treeGroups.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre

            // sinon, on modifie le label
            if (((TreeNodeData) treeGroups.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    groupView.getNodeGroup().getConceptGroup().getIdGroup())) {
                ((TreeNodeData) treeGroups.getSelectedNode().getData()).setName(titleGroup);
            }
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formLeftTab:tabTree:treeGroups");
            }
        }

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public void updateNotation() {

        PrimeFaces pf = PrimeFaces.current();
        FacesMessage msg;

        if (notation == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Notation ne doit pas être null !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (groupService.isNotationExist(notation, selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " La notation existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (!groupService.setNotationOfGroup(notation, idGroup, selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Erreur pendant la modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);

            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(), idGroup, groupView.getNodeGroup().getIdLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " Notation modifiée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (treeGroups.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre

            // sinon, on modifie le label
            if (((TreeNodeData) treeGroups.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    groupView.getNodeGroup().getConceptGroup().getIdGroup())) {
                ((TreeNodeData) treeGroups.getSelectedNode().getData()).setNotation(notation);
            }
        }

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("containerIndex:formLeftTab:treeGroups");
        }
    }

    public void updateGroupType() {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (selectedGroupType == null || selectedGroupType.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Le Type de groupe ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (!groupTypeService.updateTypeGroup(selectedGroupType, selectedTheso.getCurrentIdTheso(), idGroup)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Erreur pendant la modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(), idGroup, groupView.getNodeGroup().getIdLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " groupe modifié avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("containerIndex:formLeftTab:treeGroups");
        }
    }
}
