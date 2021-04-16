/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "modifyGroupBean")
@javax.enterprise.context.SessionScoped

public class ModifyGroupBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private TreeGroups treeGroups;   
    @Inject private GroupView groupView;
    @Inject private SelectedTheso selectedTheso;
    @Inject private CurrentUser currentUser;
    
    private String selectedGroupType;
    private String idGroup;
    private String titleGroup;    
    private String notation;    
    private List<SelectItem> listGroupType;    

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        selectedGroupType = null;
        titleGroup = null;      
        titleGroup = null;    
        notation = null;         
        if(listGroupType!= null){
            listGroupType.clear();
            listGroupType = null;
        }    
    }
    
    public ModifyGroupBean() {
    }

    public void init() {
        idGroup = groupView.getNodeGroup().getConceptGroup().getIdgroup();
        titleGroup = groupView.getNodeGroup().getLexicalValue();
        notation = groupView.getNodeGroup().getConceptGroup().getNotation();
        selectedGroupType = groupView.getNodeGroup().getConceptGroup().getIdtypecode();
        
        listGroupType = new GroupHelper().getAllGroupType(connect.getPoolConnexion());
        if (!listGroupType.isEmpty()) {
            selectedGroupType = listGroupType.get(0).getLabel();
        }        
    }
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Group !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    
    
    /**
     * Modification du label du gourpe
     *
     */
    public void renameGroup() {
        FacesMessage msg;

        if (titleGroup.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, " ", " Le label ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
       
        GroupHelper groupHelper = new GroupHelper();
        if (groupHelper.isDomainExist(connect.getPoolConnexion(),
                titleGroup,
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, " ", " un group existe déjà avec ce nom !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }        
        
        if(!groupHelper.renameGroup(
                connect.getPoolConnexion(),
                titleGroup,
                selectedTheso.getCurrentLang(),
                idGroup,
                selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Erreur lors de la modification du label !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(),idGroup, groupView.getNodeGroup().getIdLang());
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " Label modifié avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formRightTab:viewTabGroup:idGroupLable");
        }        
        
        if (treeGroups.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            
            // sinon, on modifie le label
            if (((TreeNodeData) treeGroups.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    groupView.getNodeGroup().getConceptGroup().getIdgroup())) {
                ((TreeNodeData) treeGroups.getSelectedNode().getData()).setName(titleGroup);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("formLeftTab:tabGroups:treeGroups");
                }
            }
        }
    }
    
    public void updateNotation(){
        FacesMessage msg;

        if (notation == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Notation ne doit pas être null !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
       
        GroupHelper groupHelper = new GroupHelper();
        
        if (groupHelper.isNotationExist(
                connect.getPoolConnexion(),
                notation,
                selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " La notation existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }           
        
        if (!groupHelper.setNotationOfGroup(connect.getPoolConnexion(),
                notation,
                idGroup,
                selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Erreur pendant la modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }        

        groupView.getGroup(selectedTheso.getCurrentIdTheso(),idGroup, groupView.getNodeGroup().getIdLang());
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " Notation modifiée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formRightTab:viewTabGroup:idGroupNotation");
        }
        
        if (treeGroups.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            
            // sinon, on modifie le label
            if (((TreeNodeData) treeGroups.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    groupView.getNodeGroup().getConceptGroup().getIdgroup())) {
                ((TreeNodeData) treeGroups.getSelectedNode().getData()).setNotation(notation);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("formLeftTab:tabGroups:treeGroups");
                }
            }
        }        
    }
    
    public void updateGroupType(){
        FacesMessage msg;

        if (selectedGroupType == null || selectedGroupType.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Le Type de groupe ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
       
        GroupHelper groupHelper = new GroupHelper();
        if (!groupHelper.updateTypeGroup(
                connect.getPoolConnexion(),
                selectedGroupType,
                selectedTheso.getCurrentIdTheso(),
                idGroup)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Erreur pendant la modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }        

        groupView.getGroup(selectedTheso.getCurrentIdTheso(),idGroup, groupView.getNodeGroup().getIdLang());
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " Notation modifiée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formRightTab:viewTabGroup:idGroupType");
        }         
    }


    public String getSelectedGroupType() {
        return selectedGroupType;
    }

    public void setSelectedGroupType(String selectedGroupType) {
        this.selectedGroupType = selectedGroupType;
    }

    public String getTitleGroup() {
        return titleGroup;
    }

    public void setTitleGroup(String titleGroup) {
        this.titleGroup = titleGroup;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public List<SelectItem> getListGroupType() {
        return listGroupType;
    }

    public void setListGroupType(List<SelectItem> listGroupType) {
        this.listGroupType = listGroupType;
    }


}
