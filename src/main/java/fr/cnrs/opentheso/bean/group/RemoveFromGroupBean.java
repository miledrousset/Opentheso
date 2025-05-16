package fr.cnrs.opentheso.bean.group;


import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.services.GroupService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
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

/**
 *
 * @author miledrousset
 */
@Data
@SessionScoped
@Named(value = "removeFromGroupBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RemoveFromGroupBean implements Serializable {

    
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private ConceptView conceptView;
    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private GroupService groupService;

    private List<NodeGroup> nodeGroups;

    @PreDestroy
    public void destroy(){
        clear();
    }
    public void clear(){
        if(nodeGroups!= null){
            nodeGroups.clear();
            nodeGroups = null;
        }
    }

    public RemoveFromGroupBean() {
    }

    public void init() {
        nodeGroups = conceptView.getNodeConcept().getNodeConceptGroup();
    }

    public void removeConceptFromGroup(String idGroup, int idUser) {

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        groupService.deleteRelationConceptGroupConcept(idGroup, conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a bien été enlevé de la collection");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        conceptView.getConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang(), currentUser);
        init();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:listeConceptGroupe");
        }
    }
}
