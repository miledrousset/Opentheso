/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.alignment;

import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
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

    @Inject
    private Connect connect;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private ConceptView conceptView;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private AlignmentBean alignmentBean;       
    
    private ArrayList<AlignementSource> allAlignementSources;
    private ArrayList<NodeSelectedAlignment> selectedAlignmentsOfTheso;
    
    private ArrayList<NodeSelectedAlignment> nodeSelectedAlignmentsAll;

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
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("viewTabConcept");
            pf.ajax().update("addAlignmentForm");
        }        

    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
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
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("addAlignmentForm");
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



}
