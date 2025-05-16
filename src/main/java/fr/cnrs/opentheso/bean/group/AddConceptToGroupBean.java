package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.concept.NodeAutoCompletion;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.services.GroupService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@Data
@SessionScoped
@Named(value = "addConceptToGroupBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AddConceptToGroupBean implements Serializable {

    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private ConceptView conceptView;
    @Autowired @Lazy private CurrentUser currentUser;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private ConceptDcTermRepository conceptDcTermRepository;

    private NodeAutoCompletion selectedNodeAutoCompletionGroup;


    public void init() {
        selectedNodeAutoCompletionGroup = null;
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
        if (selectedTheso.getCurrentIdTheso() != null && conceptView.getSelectedLang() != null) {
            liste = groupService.getAutoCompletionGroup(selectedTheso.getCurrentIdTheso(), conceptView.getSelectedLang(), value);
        }
        return liste;
    }

    /**
     * permet d'ajouter le concept à une collection ou groupe
     */
    public void addConceptToGroup(int idUser) {

        // selectedAtt.getIdConcept() est le terme TG à ajouter
        // terme.getIdC() est le terme séléctionné dans l'arbre
        // terme.getIdTheso() est l'id du thésaurus

        if (selectedNodeAutoCompletionGroup == null || selectedNodeAutoCompletionGroup.getIdGroup() == null
                || selectedNodeAutoCompletionGroup.getIdGroup().equals("")) {
            var msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Aucune sélection !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        // addConceptToGroup
        if (!groupService.addConceptGroupConcept(selectedNodeAutoCompletionGroup.getIdGroup(),
                conceptView.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso())) {
            var msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de bases de données !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptHelper.updateDateOfConcept(selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptView.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptView.getConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(), currentUser);

        var msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a été ajouté à la collection");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('addConceptToGroup').hide();");

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }
}
