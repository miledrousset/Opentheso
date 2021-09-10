/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.alignment;

import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentType;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "alignmentManualBean")
@SessionScoped
public class AlignmentManualBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptView;
    @Inject private SelectedTheso selectedTheso;
    @Inject private CurrentUser currentUser;     
    
    private ArrayList<NodeAlignmentType> nodeAlignmentTypes;
    
    // pour l'aligenement manuel
    private String manualAlignmentSource;
    private String manualAlignmentUri;
    private int manualAlignmentType;
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeAlignmentTypes!= null){
            nodeAlignmentTypes.clear();
            nodeAlignmentTypes = null;
        }        
        manualAlignmentSource = null;
        manualAlignmentUri = null;
    }
    
    public AlignmentManualBean() {
    }

    public void reset() {
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        nodeAlignmentTypes = alignmentHelper.getAlignmentsType(connect.getPoolConnexion());
        manualAlignmentSource = "";
        manualAlignmentUri = "";
        manualAlignmentType = -1;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    
    
    public void deleteAlignment(NodeAlignment nodeAlignment) {
        
        if(nodeAlignment == null) return;
        
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        FacesMessage msg;

        if(!alignmentHelper.deleteAlignment(connect.getPoolConnexion(),
                nodeAlignment.getId_alignement(),
                selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "alignement supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            //    pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab:viewTabConcept:idConceptAlignment");
            pf.ajax().update("containerIndex:formRightTab:viewTabConcept:deleteAlignmentForm");
        }

    }

    public void updateAlignement(NodeAlignment nodeAlignment){
        if(nodeAlignment == null) return;
        
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        FacesMessage msg;

        if(!alignmentHelper.updateAlignment(connect.getPoolConnexion(),
                nodeAlignment.getId_alignement(),
                nodeAlignment.getConcept_target(),
                nodeAlignment.getThesaurus_target(),
                nodeAlignment.getUri_target(),
                nodeAlignment.getAlignement_id_type(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeAlignment.getId_source())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de mofication !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "alignement modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            //    pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab:viewTabConcept:idConceptAlignment");
            pf.ajax().update("containerIndex:formRightTab:viewTabConcept:deleteAlignmentForm");
        }
    }
    
    public void addManualAlignement(){
        FacesMessage msg;
        if(manualAlignmentSource == null || manualAlignmentSource.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Veuillez saisir une valeur !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }
        if(manualAlignmentUri == null || manualAlignmentUri.isEmpty()){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Veuillez saisir une valeur  !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        } 
        
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        if(!alignmentHelper.addNewAlignment(
                connect.getPoolConnexion(),
                currentUser.getNodeUser().getIdUser(),
                "",
                manualAlignmentSource,
                manualAlignmentUri,
                manualAlignmentType,
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                0)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de mofication !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "alignement ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            //    pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab:viewTabConcept:idConceptAlignment");
            pf.ajax().update("containerIndex:formRightTab:viewTabConcept:addManualAlignmentForm");
        }
    }    
    
    

    public ArrayList<NodeAlignmentType> getNodeAlignmentTypes() {
        return nodeAlignmentTypes;
    }

    public void setNodeAlignmentTypes(ArrayList<NodeAlignmentType> nodeAlignmentTypes) {
        this.nodeAlignmentTypes = nodeAlignmentTypes;
    }

    public String getManualAlignmentSource() {
        return manualAlignmentSource;
    }

    public void setManualAlignmentSource(String manualAlignmentSource) {
        this.manualAlignmentSource = manualAlignmentSource;
    }

    public String getManualAlignmentUri() {
        return manualAlignmentUri;
    }

    public void setManualAlignmentUri(String manualAlignmentUri) {
        this.manualAlignmentUri = manualAlignmentUri;
    }

    public int getManualAlignmentType() {
        return manualAlignmentType;
    }

    public void setManualAlignmentType(int manualAlignmentType) {
        this.manualAlignmentType = manualAlignmentType;
    }

}
