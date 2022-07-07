/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import javax.inject.Named;
import java.io.Serializable;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "addArkGroupBean")
@javax.enterprise.context.SessionScoped

public class AddArkGroupBean implements Serializable {
    @Inject private Connect connect;
    @Inject private TreeGroups treeGroups;
    @Inject private RoleOnThesoBean roleOnThesoBean;      
   
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
    }
    
    public AddArkGroupBean() {
    }

    public void init() {
    
    }
    
    /**
     * permet de générer l'identifiant Ark, s'il n'existe pas, il sera créé,
     * sinon, il sera mis à jour.
     * @param idTheso
     */
    public void generateArkGroup(String idTheso) {
        FacesMessage msg;
        if(treeGroups == null || treeGroups.getSelectedNode() == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Pas de groupe séléctionné !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        String idGroup = ((TreeNodeData) treeGroups.getSelectedNode().getData()).getNodeId();
        String labelGroup = ((TreeNodeData) treeGroups.getSelectedNode().getData()).getName();
        GroupHelper groupHelper = new GroupHelper();
        groupHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        
        if(!groupHelper.addIdArkGroup(connect.getPoolConnexion(), idTheso, idGroup, labelGroup)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La génération de Ark a échoué !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", groupHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La génération de Ark a réussi !!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
        
    }

}
