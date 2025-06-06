package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 *
 * @author miledrousset
 */
@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "removeFromGroupBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RemoveFromGroupBean implements Serializable {

    private final SelectedTheso selectedTheso;
    private final ConceptView conceptView;
    private final CurrentUser currentUser;
    private final GroupService groupService;

    private List<NodeGroup> nodeGroups;


    public void init() {
        nodeGroups = conceptView.getNodeConcept().getNodeConceptGroup();
    }

    public void removeConceptFromGroup(String idGroup, int idUser) {

        groupService.deleteRelationConceptGroupConcept(idGroup, conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        MessageUtils.showInformationMessage("Le concept a bien été enlevé de la collection");

        conceptView.getConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(), currentUser);
        init();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        PrimeFaces.current().ajax().update("conceptForm:listeConceptGroupe");
    }
}
