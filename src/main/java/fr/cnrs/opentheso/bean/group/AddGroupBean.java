/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "addGroupBean")
@javax.enterprise.context.SessionScoped

public class AddGroupBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    @Inject
    private TreeGroups treeGroups;    
    
    private String selectedGroupType;
    private String titleGroup;    
    private String notation;    
    private List<SelectItem> listGroupType;    
    
    public AddGroupBean() {

    }

    public void init() {
        titleGroup = "";
        notation = "";
        selectedGroupType = null;
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
     * Création d'un domaine/colection avec mise à jour dans l'arbre
     *
     * @param idTheso
     * @param idLang
     * @param idUser
     */
    public void addGroup(
            String idTheso,
            String idLang,
            int idUser) {

        if (roleOnThesoBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        NodeGroup nodeGroup = new NodeGroup();
        if (titleGroup.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    languageBean.getMsg("error") + " :", languageBean.getMsg("theso.error7")));
            return;
        }

        nodeGroup.setLexicalValue(titleGroup);
        nodeGroup.setIdLang(idLang);
        nodeGroup.getConceptGroup().setIdthesaurus(idTheso);
        nodeGroup.getConceptGroup().setNotation(notation);

        if (selectedGroupType == null || selectedGroupType.isEmpty()) {
            selectedGroupType = "C";
        }
        nodeGroup.getConceptGroup().setIdtypecode(selectedGroupType);

        GroupHelper groupHelper = new GroupHelper();
        groupHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        String idGroup = groupHelper.addGroup(connect.getPoolConnexion(),
                nodeGroup,
                idUser);
        if (idGroup == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :",
                            titleGroup + " " + languageBean.getMsg("group.errorCreate")));
            return;
        }
        treeGroups.addNewGroupToTree(idGroup, idTheso, idLang);

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("info") + " :",
                titleGroup + " " + languageBean.getMsg("theso.info1.2")));

        PrimeFaces.current().executeScript("PF('addGroup').hide();");
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
    //        pf.ajax().update("formLeftTab:tabTree:tree");
            pf.ajax().update("formLeftTab:tabGroups:treeGroups");
        }
    }

    
    /**
     * permet d'ajouter un sous groupe avec un type défini, le groupe père doit
     * exister.Le sous-groupe prend le même type que le père On ajoute aussi la
     * notation
     *
     * @param idGroupFather
     * @param idTheso
     * @param idLang
     * @param idUser
     */
    public void addSubGroup(
            String idGroupFather,
            String idTheso,
            String idLang,
            int idUser) {
        // typeDom = "";
        //si on a bien selectioner un group
        //  String idGroup = tree.getSelectedTerme().getIdC();
        
        
        if (roleOnThesoBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }        
        if(idGroupFather == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                            "Id groupe Parent null "));
            return;
        } 
        if (titleGroup.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    languageBean.getMsg("error") + " :", "Un label est obligatoire"));
            return;
        }
        NodeGroup nodeGroup = new NodeGroup();        
        nodeGroup.setLexicalValue(titleGroup);
        nodeGroup.setIdLang(idLang);
        nodeGroup.getConceptGroup().setIdthesaurus(idTheso);
        nodeGroup.getConceptGroup().setNotation(notation);

        if (selectedGroupType == null || selectedGroupType.isEmpty()) {
            selectedGroupType = "C";
        }
        nodeGroup.getConceptGroup().setIdtypecode(selectedGroupType);

        GroupHelper groupHelper = new GroupHelper();
        groupHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        String idSubGroup = groupHelper.addGroup(connect.getPoolConnexion(),
                nodeGroup,
                idUser);
        if (idSubGroup == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :",
                            titleGroup + " " + languageBean.getMsg("group.errorCreate")));
            return;
        }        

        if (!groupHelper.addSubGroup(connect.getPoolConnexion(), idGroupFather, idSubGroup, idTheso)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :",
                            titleGroup + " " + languageBean.getMsg("group.errorCreate")));
            return;
        }
        treeGroups.addNewSubGroupToTree(treeGroups.getSelectedNode(), idSubGroup, idTheso, idLang);        
        
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("info") + " :",
                titleGroup + " " + languageBean.getMsg("theso.info1.2")));

        PrimeFaces.current().executeScript("PF('addSubGroup').hide();");
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
    //        pf.ajax().update("formLeftTab:tabTree:tree");
            pf.ajax().update("formLeftTab:tabGroups:treeGroups");
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
