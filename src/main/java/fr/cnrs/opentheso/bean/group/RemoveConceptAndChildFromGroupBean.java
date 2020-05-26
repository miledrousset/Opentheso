/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "removeConceptAndChildFromGroupBean")
@javax.enterprise.context.SessionScoped

public class RemoveConceptAndChildFromGroupBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private ConceptView conceptView;

    private ArrayList <NodeGroup> nodeGroups;
    
    public RemoveConceptAndChildFromGroupBean() {

    }

    public void init() {
        nodeGroups = conceptView.getNodeConcept().getNodeConceptGroup();
    }

    public void removeConceptAndChildFromGroup(String idGroup, int idUser) {
        GroupHelper groupHelper = new GroupHelper();
        
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> allId  = conceptHelper.getIdsOfBranch(
                connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        
        if( (allId == null) || (allId.isEmpty())) return;         
        
        for (String idConcept : allId) {
            if (!groupHelper.deleteRelationConceptGroupConcept(
                    connect.getPoolConnexion(),
                    idGroup,
                    idConcept,
                    selectedTheso.getCurrentIdTheso(),
                    idUser)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Erreur lors de la suppression des concepts de la collection !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");          
                }            
                return;
            }            
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "La branche a bien été enlevée de la collection");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());
        init();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("formRightTab:viewTabConcept:idConceptGroupRow");            
        }
    }

    public ArrayList<NodeGroup> getNodeGroups() {
        return nodeGroups;
    }

    public void setNodeGroups(ArrayList<NodeGroup> nodeGroups) {
        this.nodeGroups = nodeGroups;
    }


   
}
