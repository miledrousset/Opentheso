/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.alignment;

import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeSelectedAlignment;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.core.alignment.AlignementSource;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "setAlignmentSourceBean")
@SessionScoped
public class SetAlignmentSourceBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptView;
    @Inject private SelectedTheso selectedTheso;
    @Inject private AlignmentBean alignmentBean;       
    
    private ArrayList<AlignementSource> allAlignementSources;
    private ArrayList<NodeSelectedAlignment> selectedAlignmentsOfTheso;
    
    private ArrayList<NodeSelectedAlignment> nodeSelectedAlignmentsAll;
    
    
    //// pour l'ajout d'une nouvelle source
    private String sourceName;
    private String sourceUri;
    private String sourceIdTheso;
    private String description;
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(allAlignementSources!= null){
            allAlignementSources.clear();
            allAlignementSources = null;
        } 
        if(selectedAlignmentsOfTheso!= null){
            selectedAlignmentsOfTheso.clear();
            selectedAlignmentsOfTheso = null;
        }         
        if(nodeSelectedAlignmentsAll!= null){
            nodeSelectedAlignmentsAll.clear();
            nodeSelectedAlignmentsAll = null;
        }     
        sourceName = null;
        sourceUri = null;
        sourceIdTheso = null;
        description = null;
    }    

    public SetAlignmentSourceBean() {
    }

    public void init() {
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        
        // toutes les sources d'alignements 
        allAlignementSources = alignmentHelper.getAlignementSourceSAdmin(connect.getPoolConnexion());
        nodeSelectedAlignmentsAll = new ArrayList<>();
        

        // la liste des sources séléctionnées pour le thésaurus en cours
        selectedAlignmentsOfTheso = alignmentHelper.getSelectedAlignementOfThisTheso(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        
        
        // intégrer les éléments dans un vecteur global pour la modifiation
        for (AlignementSource allAlignementSource : allAlignementSources) {
            NodeSelectedAlignment nodeSelectedAlignment = new NodeSelectedAlignment();
            nodeSelectedAlignment.setIdAlignmnetSource(allAlignementSource.getId());
            nodeSelectedAlignment.setSourceLabel(allAlignementSource.getSource());
            nodeSelectedAlignment.setSourceDescription(allAlignementSource.getDescription());
            nodeSelectedAlignment.setIsSelected(false);
            nodeSelectedAlignmentsAll.add(nodeSelectedAlignment);
        }        
        

        for (NodeSelectedAlignment selectedAlignmentOfTheso : selectedAlignmentsOfTheso) {
            for (NodeSelectedAlignment nodeSelectedAlignment : nodeSelectedAlignmentsAll) {
                if(nodeSelectedAlignment.getIdAlignmnetSource() == selectedAlignmentOfTheso.getIdAlignmnetSource()) {
                    nodeSelectedAlignment.setIsSelected(true);
                }
            }
        }
        alignmentBean.setViewSetting(true);
        alignmentBean.setViewAddNewSource(false);
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("viewTabConcept");
  //          pf.ajax().update("addAlignmentForm");
        }        
    }
    
    public void initAddSource(){
        
        sourceName = null;
        sourceUri = null;
        sourceIdTheso = null;   
        description = null;
        
        alignmentBean.setViewAddNewSource(true);
        alignmentBean.setViewSetting(false);        
        PrimeFaces pf = PrimeFaces.current();
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    
    
    /**
     * permet d'ajouter une source d'alignement et affecter cette source au thésaurus en cours.
     * @param idUser 
     */
    public void addAlignmentSource(int idUser){
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        
        if(sourceName == null || sourceName.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Le nom de la source est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formRightTab:viewTabConcept:addAlignmentForm:panelAddNewSource");
                pf.ajax().update("messageIndex");
            }                   
            return;  
        }
        if(sourceUri == null || sourceUri.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " L'URL est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formRightTab:viewTabConcept:addAlignmentForm:panelAddNewSource");
                pf.ajax().update("messageIndex");
            }                   
            return;  
        }       
        if(sourceIdTheso == null || sourceIdTheso.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " L'Id. du thésaurus est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formRightTab:viewTabConcept:addAlignmentForm:panelAddNewSource");
                pf.ajax().update("messageIndex");
            }                   
            return;  
        } 
        

        if(sourceUri.lastIndexOf("/") + 1 == sourceUri.length() ) {
            sourceUri = sourceUri.substring(0, sourceUri.length()-1);
        }
        
                
        String requete = sourceUri + "/api/search?q=##value##&lang=##lang##&theso=" + sourceIdTheso;
        
        AlignementSource alignementSource = new AlignementSource();
        alignementSource.setAlignement_format("skos");
        alignementSource.setDescription(description);
//        alignementSource.setId(id);
        alignementSource.setRequete(requete);
        alignementSource.setSource(sourceName);
        alignementSource.setTypeRequete("REST");
        
        
        AlignmentHelper alignementHelper = new AlignmentHelper();
        
        if(!alignementHelper.addNewAlignmentSource(
                connect.getPoolConnexion(),
                alignementSource,
                idUser, selectedTheso.getCurrentIdTheso())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur côté base de données !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            alignementHelper.getMessage();
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", " Source ajoutée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }           
        alignmentBean.setViewAddNewSource(false);
        alignmentBean.setViewSetting(false);  
    }
    
    public void updateSelectedAlignment(){
        if(nodeSelectedAlignmentsAll == null) return;
        
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        
        
        // delete All reselected Sources
        if(!alignmentHelper.deleteAllALignementSourceOfTheso(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant l'ajout de la source au thésaurus !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            alignmentBean.setViewSetting(false);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("panelManagement");
            }                   
            return;  
        }
        
        // Add selected Sources
        for (NodeSelectedAlignment nodeSelectedAlignment : nodeSelectedAlignmentsAll) {
            if(nodeSelectedAlignment.isIsSelected()) {
                if(!alignmentHelper.addSourceAlignementToTheso(
                        connect.getPoolConnexion(),
                        selectedTheso.getCurrentIdTheso(),
                        nodeSelectedAlignment.getIdAlignmnetSource())){
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendnat l'ajout de la source au thésaurus !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    alignmentBean.setViewSetting(false);
                    if (pf.isAjaxRequest()) {
                        pf.ajax().update("panelManagement");
                    }                   
                    return;                       
                }
               
            }
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Sources associées avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        alignmentBean.setViewSetting(false);
        alignmentBean.initAlignmentSources(selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());

        if (pf.isAjaxRequest()) {
            pf.ajax().update("addAlignmentForm");
        }    
    }
    
    public void cancel(){
        alignmentBean.setViewSetting(false);
        alignmentBean.setViewAddNewSource(false);        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
//            pf.ajax().update("addAlignmentForm");
        }   
    }

    public ArrayList<AlignementSource> getAllAlignementSources() {
        return allAlignementSources;
    }

    public void setAllAlignementSources(ArrayList<AlignementSource> allAlignementSources) {
        this.allAlignementSources = allAlignementSources;
    }

    public ArrayList<NodeSelectedAlignment> getSelectedAlignmentsOfTheso() {
        return selectedAlignmentsOfTheso;
    }

    public void setSelectedAlignmentsOfTheso(ArrayList<NodeSelectedAlignment> selectedAlignmentsOfTheso) {
        this.selectedAlignmentsOfTheso = selectedAlignmentsOfTheso;
    }

    public ArrayList<NodeSelectedAlignment> getNodeSelectedAlignmentsAll() {
        return nodeSelectedAlignmentsAll;
    }

    public void setNodeSelectedAlignmentsAll(ArrayList<NodeSelectedAlignment> nodeSelectedAlignmentsAll) {
        this.nodeSelectedAlignmentsAll = nodeSelectedAlignmentsAll;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    public void setSourceUri(String sourceUri) {
        this.sourceUri = sourceUri;
    }

    public String getSourceIdTheso() {
        return sourceIdTheso;
    }

    public void setSourceIdTheso(String sourceIdTheso) {
        this.sourceIdTheso = sourceIdTheso;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



}
