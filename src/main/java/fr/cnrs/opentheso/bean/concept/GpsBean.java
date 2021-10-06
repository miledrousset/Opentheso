/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GpsHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "gpsBean")
//@javax.enterprise.context.RequestScoped
//// on ne peut pas relancer plusieurs actions avec cette déclaration

@javax.enterprise.context.SessionScoped
public class GpsBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptView;
    @Inject private SelectedTheso selectedTheso;    

    private NodeGps nodeGps;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        nodeGps = null;
    }   
    
    public GpsBean() {
    }

    public void reset() {
        nodeGps = conceptView.getNodeConcept().getNodeGps();
        if(nodeGps == null) {
            nodeGps = new NodeGps();
        }
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour modifier Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de mettre à jour les coordonnées GPS
     */
    public void updateCoordinateGps() {

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();;

        GpsHelper gpsHelper = new GpsHelper();
        
        if(nodeGps == null) return;
        
        if(!gpsHelper.insertCoordonees(
                connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeGps.getLatitude(),
                nodeGps.getLongitude())){
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", " l'insertion des coordonnées GPS a échouée!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
       
        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Coordonnées GPS modifiés avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formLeftTab");
            pf.ajax().update("containerIndex:formRightTab");
        }
        reset();
    }



    /**
     * permet de supprimer les coordonnées GPS d'un concept
     */
    public void deleteCoordinateGps() {

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        GpsHelper gpsHelper = new GpsHelper();
        
        if(nodeGps == null) return;
        
        if(!gpsHelper.deleteGpsCoordinate(
                connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso())){
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", " la suppression des coordonnées GPS a échouée!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
       
        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "suppression des coordonnées GPS réussie");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formLeftTab");
            pf.ajax().update("containerIndex:formRightTab");
        }
        reset();
    }
    
    
    
    public NodeGps getNodeGps() {
        if(nodeGps == null) {
            nodeGps = new NodeGps();
        }
        return nodeGps;
    }

    public void setNodeGps(NodeGps nodeGps) {
        this.nodeGps = nodeGps;
    }


    
    
}
