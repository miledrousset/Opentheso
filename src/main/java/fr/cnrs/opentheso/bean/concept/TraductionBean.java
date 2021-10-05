/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "traductionBean")
@SessionScoped
public class TraductionBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;

    private String selectedLang;
    private ArrayList<NodeLangTheso> nodeLangs;
    private ArrayList<NodeLangTheso> nodeLangsFiltered; // uniquement les langues non traduits
    private ArrayList<NodeTermTraduction> nodeTermTraductions;
    private String traductionValue;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeLangs != null){
            nodeLangs.clear();
            nodeLangs = null;
        }
        if(nodeLangsFiltered != null){
            nodeLangsFiltered.clear();
            nodeLangsFiltered = null;
        }
        if(nodeTermTraductions != null){
            nodeTermTraductions.clear();
            nodeTermTraductions = null;
        }        
        selectedLang = null;
        traductionValue = null;
    }      
    
    public TraductionBean() {
    }

    public void reset() {
        nodeLangs = selectedTheso.getNodeLangs();
        if(nodeLangsFiltered == null)
            nodeLangsFiltered = new ArrayList<>();
        else
            nodeLangsFiltered.clear();
        nodeTermTraductions = conceptBean.getNodeConcept().getNodeTermTraductions();
        
        selectedLang = null;
        traductionValue = "";
        setLangWithNoTraduction();
    }

    public void setLangWithNoTraduction() {
        nodeLangs.forEach((nodeLang) -> {
            nodeLangsFiltered.add(nodeLang);
        });
       
        // les langues à ignorer
        ArrayList<String> langsToRemove = new ArrayList<>();
        langsToRemove.add(conceptBean.getSelectedLang());
        for (NodeTermTraduction nodeTermTraduction : conceptBean.getNodeConcept().getNodeTermTraductions()) {
            langsToRemove.add(nodeTermTraduction.getLang());
        }
        for (NodeLangTheso nodeLang : nodeLangs) {
            if(langsToRemove.contains(nodeLang.getCode())) {
                nodeLangsFiltered.remove(nodeLang);
            }
        }
        if(nodeLangsFiltered.isEmpty()) 
            infoNoTraductionToAdd();
    }
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void infoNoTraductionToAdd() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " Le concept est traduit déjà dans toutes les langues du thésaurus !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    

    /**
     * permet d'ajouter une nouvelle traduction au concept
     * @param idUser
     */
    public void addNewTraduction(int idUser) {
        FacesMessage msg;
        if (traductionValue == null || traductionValue.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Une valeur est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (selectedLang == null || selectedLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Pas de langue choisie !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        TermHelper termHelper = new TermHelper();
        if(!termHelper.addTraduction(connect.getPoolConnexion(),
                traductionValue,
                conceptBean.getNodeConcept().getTerm().getId_term(),
                selectedLang, "", "", selectedTheso.getCurrentIdTheso(), idUser)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur d'ajout de traduction !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "traduction ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        reset();
        setLangWithNoTraduction();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formLeftTab");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }
    
    /**
     * permet de modifier une traduction au concept
     * @param nodeTermTraduction
     * @param idUser
     */
    public void updateTraduction(NodeTermTraduction nodeTermTraduction, int idUser) {
        FacesMessage msg;
        if (nodeTermTraduction == null || nodeTermTraduction.getLexicalValue().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " veuillez saisir une valeur !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        TermHelper termHelper = new TermHelper();
        if(!termHelper.updateTraduction(connect.getPoolConnexion(),
                nodeTermTraduction.getLexicalValue(), conceptBean.getNodeConcept().getTerm().getId_term(),
                nodeTermTraduction.getLang(),
                selectedTheso.getCurrentIdTheso(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La modification a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "traduction modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        //    PrimeFaces.current().executeScript("PF('addNote').hide();");
        reset();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }    
    
    /**
     * permet de modifier toutes les traductions du concept
     * multiple corrections
     * @param idUser
     */
    public void updateAllTraduction(int idUser) {
        FacesMessage msg;
        if (nodeTermTraductions == null || nodeTermTraductions.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " veuillez saisir une valeur !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        TermHelper termHelper = new TermHelper();
        
        for (NodeTermTraduction nodeTermTraduction : nodeTermTraductions) {
            if(!termHelper.updateTraduction(connect.getPoolConnexion(),
                    nodeTermTraduction.getLexicalValue(), conceptBean.getNodeConcept().getTerm().getId_term(),
                    nodeTermTraduction.getLang(),
                    selectedTheso.getCurrentIdTheso(), idUser)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La modification a échoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }            
        }
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "traduction modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        //    PrimeFaces.current().executeScript("PF('addNote').hide();");
        reset();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formLeftTab");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }        
    
            
    
    /**
     * permet de supprimer une traduction au concept
     * @param nodeTermTraduction
     * @param idUser
     */
    public void deleteTraduction(NodeTermTraduction nodeTermTraduction, int idUser) {
        FacesMessage msg;
        if (nodeTermTraduction == null || nodeTermTraduction.getLang().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de sélection de tradcution !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        TermHelper termHelper = new TermHelper();
        if(!termHelper.deleteTraductionOfTerm(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getId_term(),
                nodeTermTraduction.getLexicalValue(),
                nodeTermTraduction.getLang(),
                selectedTheso.getCurrentIdTheso(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "traduction supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        //    PrimeFaces.current().executeScript("PF('addNote').hide();");
        reset();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:listTraductions");
        }
    }    

    
    
    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public String getTraductionValue() {
        return traductionValue;
    }

    public void setTraductionValue(String traductionValue) {
        this.traductionValue = traductionValue;
    }

    public ArrayList<NodeLangTheso> getNodeLangsFiltered() {
        return nodeLangsFiltered;
    }

    public void setNodeLangsFiltered(ArrayList<NodeLangTheso> nodeLangsFiltered) {
        this.nodeLangsFiltered = nodeLangsFiltered;
    }

    public ArrayList<NodeTermTraduction> getNodeTermTraductions() {
        return nodeTermTraductions;
    }

    public void setNodeTermTraductions(ArrayList<NodeTermTraduction> nodeTermTraductions) {
        this.nodeTermTraductions = nodeTermTraductions;
    }


}
