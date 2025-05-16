package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.entites.LanguageIso639;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.thesaurus.Thesaurus;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.bean.menu.connect.MenuBean;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.repositories.ConceptGroupRepository;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.LanguageRepository;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.ThesaurusRepository;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.PreferenceService;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;


@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "editThesoBean")
public class EditThesoBean implements Serializable {

    private final CurrentUser currentUser;
    private final RoleOnThesoBean roleOnThesoBean;
    private final MenuBean menuBean;
    private final ThesaurusMetadataAdd thesaurusMetadataAdd;
    private final ConceptGroupRepository conceptGroupRepository;
    private final SelectedTheso selectedTheso;
    private final PreferenceService preferenceService;
    private final ThesaurusRepository thesaurusRepository;
    private final LanguageRepository languageRepository;
    private final ThesaurusHelper thesaurusHelper;
    private final ConceptHelper conceptHelper;
    private final GroupService groupService;

    private List<LanguageIso639> allLangs;
    private List<NodeLangTheso> languagesOfTheso;
    private TreeNode<NodeGroup> groupRoot;
    private NodeLangTheso langSelected;
    private NodeIdValue nodeIdValueOfTheso;
    private boolean isPrivateTheso;
    private int activeTabIndex;
    private String title, selectedLang, preferredLang, arkIdOfTheso, newIdOfTheso;


    public void init(String idTheso) {

        activeTabIndex = 0;
        nodeIdValueOfTheso = new NodeIdValue();
        nodeIdValueOfTheso.setId(idTheso);
        arkIdOfTheso = thesaurusHelper.getIdArkOfThesaurus(idTheso);
        init();
        try {
            menuBean.redirectToEditionPage();
        } catch (IOException ex) {
            Logger.getLogger(EditThesoBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        /// initialisation des métadonnées pour le thésaurus 
        thesaurusMetadataAdd.init(nodeIdValueOfTheso.getId());        
    }    
    
    public void init(NodeIdValue nodeIdValueOfTheso) {
        this.nodeIdValueOfTheso = nodeIdValueOfTheso;
        activeTabIndex = 0;
        init();
        /// initialisation des métadonnées pour le thésaurus 
        thesaurusMetadataAdd.init(nodeIdValueOfTheso.getId());        
    }

    private void init() {

        activeTabIndex = 0;
        isPrivateTheso = thesaurusHelper.isThesoPrivate(nodeIdValueOfTheso.getId());

        var nodePreference = preferenceService.getThesaurusPreferences(nodeIdValueOfTheso.getId());
        preferredLang = nodePreference.getSourceLang();
        allLangs = languageRepository.findAll();
        languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(nodeIdValueOfTheso.getId(), preferredLang);
        selectedLang = null;
        langSelected = new NodeLangTheso();
        title = "";

        loadGroupsTree();
    }

    private void loadGroupsTree() {
        groupRoot = new DefaultTreeNode<>(new NodeGroup(), null);
        var elements = groupService.getListRootConceptGroup(nodeIdValueOfTheso.getId(), preferredLang, false, false);
        setGroupTree(groupRoot, elements);
    }

    private void setGroupTree(TreeNode<NodeGroup> elementTree, List<NodeGroup> elements) {
        for (NodeGroup nodeGroup : elements) {
            var fils = new DefaultTreeNode(nodeGroup, elementTree);
            var tmp = groupService.getListChildsOfGroup(nodeGroup.getConceptGroup().getIdGroup(),
                    nodeIdValueOfTheso.getId(), preferredLang, false);

            if (CollectionUtils.isNotEmpty(tmp)) {
                setGroupTree(fils, tmp);
            }
        }
    }

    public void updateCollectionStatus(NodeGroup group) {

        if(ObjectUtils.isEmpty(group) || ObjectUtils.isEmpty(group.getConceptGroup())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur : Groupe invalide");
            return;
        }

        var isPrivate = group.isGroupPrivate();
        conceptGroupRepository.updateVisibility(group.getConceptGroup().getIdGroup(), group.getConceptGroup().getIdThesaurus(), isPrivate);

        if (group.isHaveChildren()) {
            var nodes = getTreeNode(groupRoot.getChildren(), group);
            updateCollectionsStatus(nodes, isPrivate);
        }

        var message = String.format("La collection %s est maintenant %s", group.getLexicalValue(), isPrivate ? "privée" : "publique");
        showMessage(FacesMessage.SEVERITY_INFO, message);
    }

    private void updateCollectionsStatus(TreeNode<NodeGroup> element, boolean newStatus) {
        for (TreeNode<NodeGroup> group : element.getChildren()) {
            conceptGroupRepository.updateVisibility(group.getData().getConceptGroup().getIdGroup(),
                    group.getData().getConceptGroup().getIdThesaurus(), newStatus);
            group.getData().setGroupPrivate(newStatus);

            if (CollectionUtils.isNotEmpty(element.getChildren())) {
                for (TreeNode<NodeGroup> tmp : element.getChildren()) {
                    updateCollectionsStatus(tmp, newStatus);
                }
            }
        }
    }

    private TreeNode<NodeGroup> getTreeNode(List<TreeNode<NodeGroup>> nodes, NodeGroup group) {

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

    public void reset() {
        selectedLang = null;
        title = "";
        preferredLang = null;
        newIdOfTheso = "";
    }

    public void generateArkId(){
        arkIdOfTheso = conceptHelper.generateArkIdForTheso(nodeIdValueOfTheso.getId());
        if(arkIdOfTheso == null){ arkIdOfTheso = ""; }
    }

    public void modifyIdOfThesaurus(String oldId, String newId) {

        if(!thesaurusHelper.changeIdOfThesaurus(oldId, newId)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de changement d'identifiant !!!");
            return;
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Le changement d'identifiant a réussi, veuillez recharger les thésaurus");
    }

    public void changeSourceLang(){

        if (ObjectUtils.isEmpty(nodeIdValueOfTheso) || StringUtils.isEmpty(nodeIdValueOfTheso.getId())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Pas de thésaurus sélectionné !!!");
            return;
        }

        if (StringUtils.isEmpty(preferredLang)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "La langue source est obligatoire !!!");
            return;
        }
        
        if (!preferenceService.setWorkLanguageOfThesaurus(preferredLang, nodeIdValueOfTheso.getId())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant la modification de la langue source !!!");
            return;
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Langue source modifiée avec succès");
        init(nodeIdValueOfTheso);
    }
    
    /**
     * permet de changer le status du thésaurus entre public et privé
     */
    public void changeStatus() {

        if (thesaurusRepository.updateVisibility(nodeIdValueOfTheso.getId(), isPrivateTheso) == 0) {
            showMessage(FacesMessage.SEVERITY_ERROR, "La modification a échoué !!!");
            return;
        }

        if (isPrivateTheso) {
            showMessage(FacesMessage.SEVERITY_INFO, "Le thésaurus est maintenant privé");
        } else {
            showMessage(FacesMessage.SEVERITY_INFO, "Le thésaurus est maintenant public");
        }

        init(nodeIdValueOfTheso);
        roleOnThesoBean.showListTheso(currentUser, selectedTheso);
        PrimeFaces.current().ajax().update("toolBoxForm:idLangToModify");
    }

    /**
     * Permet de supprimer un thésaurus
     */
    public void addNewLang() {

        if (ObjectUtils.isEmpty(nodeIdValueOfTheso) || StringUtils.isEmpty(nodeIdValueOfTheso.getId())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Pas de thésaurus sélectionné !!!");
            return;
        }

        if (StringUtils.isEmpty(title)) {
            showMessage(FacesMessage.SEVERITY_WARN, "Le label est obligatoire !!!");
            return;
        }

        if (StringUtils.isEmpty(selectedLang)) {
            showMessage(FacesMessage.SEVERITY_WARN, "La langue est obligatoire !!!");
            return;
        }

        Thesaurus thesaurus = new Thesaurus();
        thesaurus.setCreator(currentUser.getNodeUser().getName());
        thesaurus.setContributor(currentUser.getNodeUser().getName());
        thesaurus.setId_thesaurus(nodeIdValueOfTheso.getId());
        thesaurus.setTitle(title);
        thesaurus.setLanguage(selectedLang);
        if (!thesaurusHelper.addThesaurusTraductionRollBack(thesaurus)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant l'ajout de la langue !!!");
            return;
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Langue ajoutée avec succès");
        init(nodeIdValueOfTheso);
    }
    
    public void updateLang(NodeLangTheso nodeLangThesoSelected){

        if (ObjectUtils.isEmpty(nodeIdValueOfTheso) || StringUtils.isEmpty(nodeIdValueOfTheso.getId())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Pas de thésaurus sélectionné !!!");
            return;
        }

        if (ObjectUtils.isEmpty(nodeLangThesoSelected) || StringUtils.isEmpty(nodeLangThesoSelected.getValue())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Le label est obligatoire !!!");
            return;
        }

        Thesaurus thesaurus = new Thesaurus();
        thesaurus.setCreator(currentUser.getNodeUser().getName());
        thesaurus.setContributor(currentUser.getNodeUser().getName());
        thesaurus.setId_thesaurus(nodeIdValueOfTheso.getId());
        thesaurus.setTitle(langSelected.getLabelTheso());
        thesaurus.setLanguage(nodeLangThesoSelected.getCode());

        if (!thesaurusHelper.UpdateThesaurus(thesaurus)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant la modification !!!");
            return;
        }

        roleOnThesoBean.showListTheso(currentUser, selectedTheso);
        showMessage(FacesMessage.SEVERITY_INFO, "Langue modifiée avec succès");

        String sourceLang = preferenceService.getWorkLanguageOfThesaurus(nodeIdValueOfTheso.getId());
        languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(nodeIdValueOfTheso.getId(), sourceLang);

        PrimeFaces.current().ajax().update("containerIndex:listLangThes");
    }
    
    public void deleteLangFromTheso(String idLang){

        if (ObjectUtils.isEmpty(nodeIdValueOfTheso) || StringUtils.isEmpty(nodeIdValueOfTheso.getId())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Pas de thésaurus sélectionné !!!");
            return;
        }

        if (StringUtils.isEmpty(idLang)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Pas de langue sélectionnée !!!");
            return;
        }

        if (!thesaurusHelper.deleteThesaurusTraduction(nodeIdValueOfTheso.getId(), idLang)){
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant la suppression de la langue !!!");
            return;
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Langue supprimée avec succès");
        init(nodeIdValueOfTheso);
    }

    public void setLangSelected(NodeLangTheso langSelected) {
        if(langSelected == null) 
            langSelected = new NodeLangTheso();
        this.langSelected = langSelected;
    }

    private void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }

}
