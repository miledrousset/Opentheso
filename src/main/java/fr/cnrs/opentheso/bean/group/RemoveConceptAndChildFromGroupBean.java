package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.primefaces.PrimeFaces;


@Data
@Slf4j
@SessionScoped
@RequiredArgsConstructor
@Named(value = "removeConceptAndChildFromGroupBean")
public class RemoveConceptAndChildFromGroupBean implements Serializable {

    private final CurrentUser currentUser;
    private final SelectedTheso selectedTheso;
    private final ConceptView conceptView;
    private final TreeGroups treeGroups;
    private final GroupView groupView;
    private final ConceptHelper conceptHelper;
    private final GroupService groupService;
    private final ConceptService conceptService;

    private List<NodeGroup> nodeGroups;


    public void init() {
        nodeGroups = conceptView.getNodeConcept().getNodeConceptGroup();
    }


    public void removeConceptAndChildFromGroup(String idGroup) {

        log.info("Début de la suppression de tous les concepts rattachés au group id {}", idGroup);
        var allId  = conceptService.getIdsOfBranch(conceptView.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());

        if(CollectionUtils.isEmpty(allId)) {
            log.error("Aucun concepts n'est trouvé");
            return;
        }

        for (String idConcept : allId) {
            groupService.deleteRelationConceptGroupConcept(idGroup, idConcept, selectedTheso.getCurrentIdTheso());
        }

        MessageUtils.showInformationMessage("La branche a bien été enlevée de la collection");
        conceptView.getConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(), currentUser);

        log.info("Initialisation de l'interface");
        init();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        PrimeFaces.current().ajax().update("conceptForm:listeConceptGroupeToDelete");
    }

    public void deleteGroup(String idGroup){

        groupService.deleteConceptGroupRollBack(idGroup, selectedTheso.getCurrentIdTheso());
        if(!groupService.removeAllConceptsFromThisGroup(idGroup, selectedTheso.getCurrentIdTheso())){
            MessageUtils.showErrorMessage("Erreur lors de la suppression de l'appartenance des concepts à la collection !!");
        }

        MessageUtils.showInformationMessage("La collection a bien été supprimée");

        if (treeGroups.getSelectedNode() != null) {
            var parent = treeGroups.getSelectedNode().getParent();
            if (parent != null) {
                parent.getChildren().remove(treeGroups.getSelectedNode());
                PrimeFaces.current().ajax().update("containerIndex:formLeftTab:tabGroups:treeGroups");
            }
        }
        groupView.init();
    }
}
