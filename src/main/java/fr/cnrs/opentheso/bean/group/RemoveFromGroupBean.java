/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 *
 * @author miledrousset
 */
@SessionScoped
@Named(value = "removeFromGroupBean")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RemoveFromGroupBean implements Serializable {

    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private ConceptView conceptView;

    private ArrayList <NodeGroup> nodeGroups;

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
        GroupHelper groupHelper = new GroupHelper();
        
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        
        if (!groupHelper.deleteRelationConceptGroupConcept(
                connect.getPoolConnexion(),
                idGroup,
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de bases de données !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");          
            }            
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a bien été enlevé de la collection");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());
        init();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");    
            pf.ajax().update("conceptForm:listeConceptGroupe");          
        }
    }

    public ArrayList<NodeGroup> getNodeGroups() {
        return nodeGroups;
    }

    public void setNodeGroups(ArrayList<NodeGroup> nodeGroups) {
        this.nodeGroups = nodeGroups;
    }


   
}
