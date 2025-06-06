package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.concept.NodeAutoCompletion;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;


@Data
@SessionScoped
@Named(value = "addConceptAndChildToGroupBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AddConceptAndChildToGroupBean implements Serializable {

    private final SelectedTheso selectedTheso;
    private final ConceptView conceptView;
    private final CurrentUser currentUser;
    private final GroupService groupService;
    private final ConceptService conceptService;
    private final ConceptDcTermRepository conceptDcTermRepository;

    private NodeAutoCompletion selectedNodeAutoCompletionGroup;


    public void init() {
        selectedNodeAutoCompletionGroup = null;
    }


    public List<NodeAutoCompletion> getAutoCompletCollection(String value) {
        selectedNodeAutoCompletionGroup = new NodeAutoCompletion();
        List<NodeAutoCompletion> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            liste = groupService.getAutoCompletionGroup(selectedTheso.getCurrentIdTheso(),
                    conceptView.getSelectedLang(), value);
        }
        return liste;
    }

    /**
     * permet d'ajouter le concept à une collection ou groupe
     */
    public void addConceptAndChildToGroup(int idUser) {

        if (selectedNodeAutoCompletionGroup == null || "".equals(selectedNodeAutoCompletionGroup.getIdGroup())) {
            MessageUtils.showErrorMessage("Aucune sélection !!");
            return;
        }

        var allId  = conceptService.getIdsOfBranch(conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        if( (allId == null) || (allId.isEmpty())) return;

        // addConceptToGroup
        for (String idConcept : allId) {
            if (!groupService.addConceptGroupConcept(selectedNodeAutoCompletionGroup.getIdGroup(), idConcept,
                    selectedTheso.getCurrentIdTheso())) {
                MessageUtils.showErrorMessage("Erreur lors de l'ajout du concept à la collection !!");
                return;
            }
        }

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptView.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptView.getConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(), currentUser);

        MessageUtils.showInformationMessage("La branche a bien été ajoutée à la collection");
        PrimeFaces.current().executeScript("PF('addConceptAndChildToGroup').hide();");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }
}
