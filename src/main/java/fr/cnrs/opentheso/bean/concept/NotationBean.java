/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "notationBean")
//@javax.enterprise.context.RequestScoped
//// on ne peut pas relancer plusieurs actions avec cette déclaration

@javax.enterprise.context.SessionScoped
public class NotationBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private ConceptView conceptBean;

    private String notation;

    public NotationBean() {
    }

    public void reset() {
        notation = conceptBean.getNodeConcept().getConcept().getNotation();
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour modifier Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter ou modifier la Notation
     * @param idTheso
     * @param idUser 
     */
    public void updateNotation(
            String idTheso,
            int idUser) {

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        ConceptHelper conceptHelper = new ConceptHelper();
        
        if(!notation.isEmpty()) {
            if(conceptHelper.isNotationExist(connect.getPoolConnexion(),
                    idTheso,
                    notation)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La notation existe déjà dans le thésaurus !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }            
                return;
            }
        }
        
        if(!conceptHelper.updateNotation(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                idTheso,
                notation.trim())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de cohérence de BDD !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }            
            return;
        } 

        conceptBean.getConcept(idTheso,
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "La notation a bien été modifiée");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }
        reset();
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }
    
    
}
