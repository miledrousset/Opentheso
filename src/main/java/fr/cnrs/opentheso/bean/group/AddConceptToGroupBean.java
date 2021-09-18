/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAutoCompletion;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "addConceptToGroupBean")
@javax.enterprise.context.SessionScoped

public class AddConceptToGroupBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private ConceptView conceptView;
    
    private NodeAutoCompletion selectedNodeAutoCompletionGroup;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        selectedNodeAutoCompletionGroup = null;
    }
    
    public AddConceptToGroupBean() {

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
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            return new GroupHelper().getAutoCompletionGroup(
                    connect.getPoolConnexion(),
                    selectedTheso.getCurrentIdTheso(),
                    conceptView.getSelectedLang(),
                    value);
        }
        return new ArrayList<>();
    }

    /**
     * permet d'ajouter le concept à une collection ou groupe
     */
    public void addConceptToGroup() {

        // selectedAtt.getIdConcept() est le terme TG à ajouter
        // terme.getIdC() est le terme séléctionné dans l'arbre
        // terme.getIdTheso() est l'id du thésaurus
        FacesMessage msg;
        if (selectedNodeAutoCompletionGroup == null || selectedNodeAutoCompletionGroup.getIdGroup().equals("")) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "pas de sélection !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        // addConceptToGroup
        GroupHelper groupHelper = new GroupHelper();
        if (!groupHelper.addConceptGroupConcept(connect.getPoolConnexion(),
                selectedNodeAutoCompletionGroup.getIdGroup(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de bases de données !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept());

        conceptView.getConcept(selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a été ajouté à la collection");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('addConceptToGroup').hide();");

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab:viewTabConcept:idConceptGroupRow");
        }
    }
    
    public NodeAutoCompletion getSelectedNodeAutoCompletionGroup() {
        return selectedNodeAutoCompletionGroup;
    }

    public void setSelectedNodeAutoCompletionGroup(NodeAutoCompletion selectedNodeAutoCompletionGroup) {
        this.selectedNodeAutoCompletionGroup = selectedNodeAutoCompletionGroup;
    }
}
