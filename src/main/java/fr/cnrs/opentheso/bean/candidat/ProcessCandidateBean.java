package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.bean.candidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@Named(value = "processCandidateBean")
@SessionScoped
public class ProcessCandidateBean implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private CandidatBean candidatBean;

    private CandidatDto selectedCandidate;
    private String adminMessage;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        selectedCandidate = null;
        adminMessage = null;
    }

    public ProcessCandidateBean() {
    }

    public void reset(CandidatDto candidatSelected) {
        this.selectedCandidate = candidatSelected;
    }

    public void insertCandidat(int idUser) {
        if (selectedCandidate == null) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        CandidatService candidatService = new CandidatService();

        if (!candidatService.insertCandidate(connect, selectedCandidate, adminMessage, idUser)) {
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

        candidatBean.initCandidatModule();

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex:tabViewCandidat");
    }

    public void rejectCandidat(int idUser) {
        if (selectedCandidate == null) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        CandidatService candidatService = new CandidatService();

        if (!candidatService.rejectCandidate(connect, selectedCandidate, adminMessage, idUser)) {
            printErreur("Erreur d'insertion");
            return;
        }
        printMessage("Candidat(s) rejeté(s) avec succès");
        reset(null);
        candidatBean.getAllCandidatsByThesoAndLangue();
        try {
            candidatBean.setIsListCandidatsActivate(true);
        } catch (IOException ex) {
            Logger.getLogger(ProcessCandidateBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        candidatBean.initCandidatModule();

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex:tabViewCandidat");
    }

    public void insertListCandidat(int idUser) {
        if (candidatBean.getSelectedCandidates() == null || candidatBean.getSelectedCandidates().isEmpty()) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        CandidatService candidatService = new CandidatService();

        for (CandidatDto selectedCandidate1 : candidatBean.getSelectedCandidates()) {
            if (!candidatService.insertCandidate(connect, selectedCandidate1, adminMessage, idUser)) {
                printErreur("Erreur d'insertion pour le candidat : " + selectedCandidate1.getNomPref() + "(" + selectedCandidate1.getIdConcepte() + ")");
                return;
            }
        }

        printMessage("Canditats insérés avec succès");
        reset(null);
        candidatBean.initCandidatModule();
        candidatBean.getAllCandidatsByThesoAndLangue();
        try {
            candidatBean.setIsListCandidatsActivate(true);
        } catch (IOException ex) {
            Logger.getLogger(ProcessCandidateBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void rejectCandidatList(int idUser) {
        if (candidatBean.getSelectedCandidates() == null || candidatBean.getSelectedCandidates().isEmpty()) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        CandidatService candidatService = new CandidatService();

        for (CandidatDto selectedCandidate1 : candidatBean.getSelectedCandidates()) {
            if (!candidatService.rejectCandidate(connect, selectedCandidate1, adminMessage, idUser)) {
                printErreur("Erreur pour le candidat : " + selectedCandidate1.getNomPref() + "(" + selectedCandidate1.getIdConcepte() + ")");
                return;
            }
        }

        printMessage("Candidats insérés avec succès");
        reset(null);
        candidatBean.initCandidatModule();
        candidatBean.getAllCandidatsByThesoAndLangue();
        try {
            candidatBean.setIsListCandidatsActivate(true);
        } catch (IOException ex) {
            Logger.getLogger(ProcessCandidateBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printErreur(String message) {
        FacesMessage msg;
        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }

    private void printMessage(String message) {
        FacesMessage msg;
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
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
