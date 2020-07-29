/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@Named(value = "processCandidateBean")
@SessionScoped
public class ProcessCandidateBean implements Serializable {

    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private CandidatBean candidatBean;

    private CandidatDto selectedCandidate;
    private String adminMessage;

    public ProcessCandidateBean() {
    }

    public void reset(CandidatDto candidatSelected) {
        selectedCandidate = candidatSelected;
    }


    public void insertCandidat(int idUser) {
        if(selectedCandidate == null) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        CandidatService candidatService = new CandidatService();

        if(!candidatService.insertCandidate(connect, selectedCandidate, adminMessage, idUser)) {
            printErreur("Erreur d'insertion");
            return;
        }
        printMessage("Canditat inséré avec succès");
        reset(null);
        candidatBean.getAllCandidatsByThesoAndLangue();
        try { 
            candidatBean.setIsListCandidatsActivate(true);
        } catch (IOException ex) {
            Logger.getLogger(ProcessCandidateBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
   //         pf.ajax().update("candidatForm:listTraductionForm");
            pf.ajax().update("candidatForm");
        } 
    }
    
    public void rejectCandidat(int idUser){
        if(selectedCandidate == null) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        CandidatService candidatService = new CandidatService();

        if(!candidatService.rejectCandidate(connect, selectedCandidate, adminMessage, idUser)) {
            printErreur("Erreur d'insertion");
            return;
        }
        printMessage("Canditat inséré avec succès");
        reset(null);
        candidatBean.getAllCandidatsByThesoAndLangue();
        try { 
            candidatBean.setIsListCandidatsActivate(true);
        } catch (IOException ex) {
            Logger.getLogger(ProcessCandidateBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
   //         pf.ajax().update("candidatForm:listTraductionForm");
            pf.ajax().update("candidatForm");
        }         
    }

    private void printErreur(String message) {
        FacesMessage msg;
        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    private void printMessage(String message) {
        FacesMessage msg;
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    

    public CandidatDto getSelectedCandidate() {
        return selectedCandidate;
    }

    public void setSelectedCandidate(CandidatDto selectedCandidate) {
        this.selectedCandidate = selectedCandidate;
    }

    public String getAdminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }

}
