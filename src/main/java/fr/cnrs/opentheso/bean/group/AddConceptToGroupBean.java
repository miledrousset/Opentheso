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
@Named(value = "addConceptToGroupBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AddConceptToGroupBean implements Serializable {

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

    /**
     * permet de retourner la liste des groupes / collections contenus dans le
     * thésaurus
     */
    public List<NodeAutoCompletion> getAutoCompletCollection(String value) {
        selectedNodeAutoCompletionGroup = new NodeAutoCompletion();
        List<NodeAutoCompletion> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && conceptView.getSelectedLang() != null) {
            liste = groupService.getAutoCompletionGroup(selectedTheso.getCurrentIdTheso(), conceptView.getSelectedLang(), value);
        }
        return liste;
    }

    /**
     * permet d'ajouter le concept à une collection ou groupe
     */
    public void addConceptToGroup(int idUser) {

        if (selectedNodeAutoCompletionGroup == null || StringUtils.isEmpty(selectedNodeAutoCompletionGroup.getIdGroup())) {
           MessageUtils.showErrorMessage("Aucune sélection !!");
            return;
        }

        // addConceptToGroup
        if (!groupService.addConceptGroupConcept(selectedNodeAutoCompletionGroup.getIdGroup(),
                conceptView.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso())) {
            MessageUtils.showErrorMessage("Erreur de bases de données !!");
            return;
        }

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptView.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptView.getConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(), currentUser);

        MessageUtils.showInformationMessage("Le concept a été ajouté à la collection");
        PrimeFaces.current().executeScript("PF('addConceptToGroup').hide();");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }
}
