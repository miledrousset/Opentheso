package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesaurusBean;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.GroupTypeService;
import fr.cnrs.opentheso.services.NoteService;
import fr.cnrs.opentheso.services.RelationGroupService;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import jakarta.faces.model.SelectItem;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;


@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "addGroupBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AddGroupBean implements Serializable {
    
    private final LeftBodySetting leftBodySetting;
    private final RoleOnThesaurusBean roleOnThesaurusBean;
    private final TreeGroups treeGroups;
    private final RelationGroupService relationGroupService;
    private final GroupService groupService;
    private final NoteService noteService;
    private final GroupTypeService groupTypeService;

    private List<SelectItem> listGroupType;
    private String selectedGroupType, titleGroup, notation, definition;
    private List<NodeNote> notes, scopeNotes, changeNotes, definitions, editorialNotes, examples, historyNotes;


    public void init() {
        titleGroup = "";
        notation = "";
        definition = "";
        selectedGroupType = null;
        listGroupType = groupTypeService.getAllGroupType();
        if (!listGroupType.isEmpty()) {
            selectedGroupType = listGroupType.get(0).getLabel();
        }
    }
    
    public void addGroup(String idTheso, String idLang, int idUser) {

        if (roleOnThesaurusBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            MessageUtils.showErrorMessage("Le thésaurus n'a pas de préférences !");
            return;
        }

        NodeGroup nodeGroup = new NodeGroup();
        if (titleGroup.isEmpty()) {
            MessageUtils.showErrorMessage("Le label est obligatoire !");
            return;
        }

        nodeGroup.setLexicalValue(titleGroup);
        nodeGroup.setIdLang(idLang);
        nodeGroup.getConceptGroup().setIdThesaurus(idTheso);
        nodeGroup.getConceptGroup().setNotation(notation);

        if (selectedGroupType == null || selectedGroupType.isEmpty()) {
            selectedGroupType = "C";
        }
        nodeGroup.getConceptGroup().setIdTypeCode(selectedGroupType);

        if(notation == null || notation.isEmpty()){
        } else {
            if (groupService.isNotationExist(notation, idTheso)) {
                MessageUtils.showErrorMessage("La notation existe déjà !");
                return;
            }
        }

        String idGroup = groupService.addGroup(nodeGroup, idUser);
        if (idGroup == null) {
            MessageUtils.showErrorMessage("Erreur interne");
            return;
        }
        if(roleOnThesaurusBean.getNodePreference().isUseArkLocal()) {
            generateArkGroup(idGroup, titleGroup, idTheso);
        }

        // ajout de la définition s'il elle est renseignée
        if(StringUtils.isNotEmpty(definition)) {
            noteService.addNote(idGroup, idLang, idTheso, definition, "definition", "",  idUser);
        }


        treeGroups.addNewGroupToTree(idGroup, idTheso, idLang);

        MessageUtils.showInformationMessage(titleGroup + " a été ajouté avec succès");

        PrimeFaces.current().executeScript("PF('addGroup').hide();");
        leftBodySetting.setIndex("2");
        PrimeFaces.current().ajax().update("containerIndex");
    }
    
    public void generateArkGroup(String idGroup, String groupLabel, String idTheso) {

        if(StringUtils.isEmpty(idGroup) || StringUtils.isEmpty(idTheso)) {
            MessageUtils.showErrorMessage("Pas de groupe séléctionné !!");
            return;
        }

        if(!groupService.addIdArkGroup(idTheso, idGroup, groupLabel)) {
            MessageUtils.showErrorMessage("La génération de Ark a échoué !!");
            return;
        }
        MessageUtils.showInformationMessage("La génération de Ark a réussi !!");
    }
    
    public void addSubGroup(String idGroupFather, String idTheso, String idLang, int idUser) {


        if (roleOnThesaurusBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            MessageUtils.showErrorMessage("le thésaurus n'a pas de préférences !");
            return;
        }
        if(idGroupFather == null) {
            MessageUtils.showErrorMessage("Id groupe Parent null ");
            return;
        }
        if (titleGroup.isEmpty()) {
            MessageUtils.showErrorMessage("Le label est obligatoire");
            return;
        }
        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setLexicalValue(titleGroup);
        nodeGroup.setIdLang(idLang);
        nodeGroup.getConceptGroup().setIdThesaurus(idTheso);
        nodeGroup.getConceptGroup().setNotation(notation);

        if (selectedGroupType == null || selectedGroupType.isEmpty()) {
            selectedGroupType = "C";
        }
        nodeGroup.getConceptGroup().setIdTypeCode(selectedGroupType);

        String idSubGroup = groupService.addGroup(nodeGroup, idUser);
        if (idSubGroup == null) {
            MessageUtils.showErrorMessage("Erreur interne");
            return;
        }

        relationGroupService.addSubGroup(idGroupFather, idSubGroup, idTheso);
        treeGroups.addNewSubGroupToTree(treeGroups.getSelectedNode(), idSubGroup, idTheso, idLang);

        MessageUtils.showInformationMessage(titleGroup + " a été ajouté avec succès");

        PrimeFaces.current().executeScript("PF('addSubGroup').hide();");
        PrimeFaces.current().ajax().update("formLeftTab:tabGroups:treeGroups");
    }
}
