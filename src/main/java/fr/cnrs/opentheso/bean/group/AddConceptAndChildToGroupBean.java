package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.concept.NodeAutoCompletion;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.GroupHelper;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PreDestroy;
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
@Named(value = "addConceptAndChildToGroupBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AddConceptAndChildToGroupBean implements Serializable {

    @Autowired @Lazy
    private SelectedTheso selectedTheso;
    @Autowired @Lazy
    private ConceptView conceptView;
    @Autowired @Lazy
    private CurrentUser currentUser;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private GroupHelper groupHelper;

    private NodeAutoCompletion selectedNodeAutoCompletionGroup;

    @PreDestroy
    public void destroy(){
        clear();
    }
    public void clear(){
        selectedNodeAutoCompletionGroup = null;
    }

    public AddConceptAndChildToGroupBean() {
    }

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
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            liste = groupHelper.getAutoCompletionGroup(selectedTheso.getCurrentIdTheso(),
                    conceptView.getSelectedLang(), value);
        }
        return liste;
    }

    /**
     * permet d'ajouter le concept à une collection ou groupe
     */
    public void addConceptAndChildToGroup(int idUser) {

        // selectedAtt.getIdConcept() est le terme TG à ajouter
        // terme.getIdC() est le terme séléctionné dans l'arbre
        // terme.getIdTheso() est l'id du thésaurus
        FacesMessage msg;
        if (selectedNodeAutoCompletionGroup == null || selectedNodeAutoCompletionGroup.getIdGroup().equals("")) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "pas de sélection !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ArrayList<String> allId  = conceptHelper.getIdsOfBranch(conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        if( (allId == null) || (allId.isEmpty())) return;

        // addConceptToGroup
        for (String idConcept : allId) {
            if (!groupHelper.addConceptGroupConcept(
                    selectedNodeAutoCompletionGroup.getIdGroup(),
                    idConcept,
                    selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Erreur lors de l'ajout du concept à la collection !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }

        conceptHelper.updateDateOfConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(), idUser);

        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptView.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////

        conceptView.getConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(), currentUser);

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "La branche a bien été ajoutée à la collection");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('addConceptAndChildToGroup').hide();");

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public NodeAutoCompletion getSelectedNodeAutoCompletionGroup() {
        return selectedNodeAutoCompletionGroup;
    }

    public void setSelectedNodeAutoCompletionGroup(NodeAutoCompletion selectedNodeAutoCompletionGroup) {
        this.selectedNodeAutoCompletionGroup = selectedNodeAutoCompletionGroup;
    }
}
