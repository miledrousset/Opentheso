package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.models.candidats.CandidatDto;
import fr.cnrs.opentheso.bean.mail.MailBean;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.candidats.CandidatService;
import fr.cnrs.opentheso.services.exports.csv.CsvWriteHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Slf4j
@Data
@Named(value = "processCandidateBean")
@SessionScoped
public class ProcessCandidateBean implements Serializable {

    
    @Autowired @Lazy private CandidatBean candidatBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private MailBean mailBean;  
    @Autowired @Lazy private CurrentUser currentUser;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private CandidatService candidatService;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private CsvWriteHelper csvWriteHelper;

    @Autowired
    private UserHelper userHelper;
    
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
    
    public void action(){
    }

    public void reset(CandidatDto candidatSelected) {
        this.selectedCandidate = candidatSelected;
        adminMessage = null;
    }

    public StreamedContent exportProcessedCandidates(List<CandidatDto> candidatDtos) {

        byte[] datas;
        
        datas = csvWriteHelper.writeProcessedCandidates(candidatDtos, ';');

        if (datas == null) {
            return null;
        }

        PrimeFaces.current().executeScript("PF('waitDialog').hide();");

        try ( ByteArrayInputStream input = new ByteArrayInputStream(datas)) {
            return DefaultStreamedContent.builder()
                    .contentType("text/csv")
                    .name(selectedTheso.getThesoName() + "_candidats" + ".csv")
                    .stream(() -> input)
                    .build();
        } catch (IOException ex) {
        }
        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
        return new DefaultStreamedContent();

    }
    
    
    public void insertCandidat(int idUser, NodePreference nodePreference) {
        if (selectedCandidate == null) {
            printErreur("Pas de candidat sélectionné");
            return;
        }

        if (!candidatService.insertCandidate(selectedCandidate, adminMessage, idUser)) {
            printErreur("Erreur d'insertion");
            return;
        }
        // envoie de mail au créateur du candidat si l'option mail est activée
        NodeUser nodeUser = userHelper.getUser(selectedCandidate.getCreatedById());

        if(nodeUser != null && nodeUser.isAlertMail())
            sendMailCandidateAccepted(nodeUser.getMail(), selectedCandidate);
        
        generateArk(nodePreference, selectedCandidate);
        
        conceptHelper.updateDateOfConcept(selectedCandidate.getIdThesaurus(),
                selectedCandidate.getIdConcepte(), idUser);

        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                selectedCandidate.getIdConcepte(), selectedCandidate.getIdThesaurus());
        ///////////////             
        
        
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
    
    private void generateArk(NodePreference nodePreference, CandidatDto selectedCandidateTemp){
        if (nodePreference != null) {
            conceptHelper.setNodePreference(nodePreference);

            // création de l'identifiant Handle
            if (nodePreference.isUseHandle()) {
                if (!conceptHelper.generateIdHandle(selectedCandidateTemp.getIdConcepte(), selectedCandidateTemp.getIdThesaurus())) {
                    printErreur("La création Handle a échouée");
                    log.error("La création Handle a échoué");
                }
            }     
            // serveur Ark
            if (nodePreference.isUseArk()) {
                if (!conceptHelper.generateArkId(
                        selectedCandidateTemp.getIdThesaurus(), selectedCandidateTemp.getIdConcepte(),
                        selectedCandidateTemp.getLang())) {
                    log.error("La création Ark a échoué");
                }
            }
            // ark Local
            if (nodePreference.isUseArkLocal()) {
                ArrayList<String> idConcepts = new ArrayList<>();
                idConcepts.add(selectedCandidateTemp.getIdConcepte());
                if (!conceptHelper.generateArkIdLocal(
                        selectedCandidateTemp.getIdThesaurus(),
                        idConcepts)) {
                    printErreur("La création du Ark local a échoué");
                    log.error("La création du Ark local a échoué");
                }
            }                
        }          
    }
    
    public void rejectCandidat(int idUser) {
        if (selectedCandidate == null) {
            printErreur("Pas de candidat sélectionné");
            return;
        }

        if (!candidatService.rejectCandidate(selectedCandidate, adminMessage, idUser)) {
            printErreur("Erreur d'insertion");
            return;
        }

        // envoie de mail au créateur du candidat si l'option mail est activée

        NodeUser nodeUser = userHelper.getUser(selectedCandidate.getCreatedById());
        if(nodeUser.isAlertMail())
            sendMailCandidateRejected(nodeUser.getMail(), selectedCandidate);

        
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

    public void insertListCandidat(int idUser, NodePreference nodePreference) {
        if (candidatBean.getSelectedCandidates() == null || candidatBean.getSelectedCandidates().isEmpty()) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        
        // envoie de mail au créateur du candidat si l'option mail est activée
        NodeUser nodeUser;

        
        for (CandidatDto selectedCandidate1 : candidatBean.getSelectedCandidates()) {
            selectedCandidate1 = candidatBean.getAllInfosOfCandidate(selectedCandidate1);
            if (!candidatService.insertCandidate(selectedCandidate1, adminMessage, idUser)) {
                printErreur("Erreur d'insertion pour le candidat : " + selectedCandidate1.getNomPref() + "(" + selectedCandidate1.getIdConcepte() + ")");
                return;
            }
            conceptHelper.updateDateOfConcept(selectedCandidate1.getIdThesaurus(),
                    selectedCandidate1.getIdConcepte(), idUser);
            
            ///// insert DcTermsData to add contributor
            dcElementHelper.addDcElementConcept(
                    new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                    selectedCandidate1.getIdConcepte(), selectedCandidate1.getIdThesaurus());
            ///////////////              
            
            generateArk(nodePreference, selectedCandidate1);
            nodeUser = userHelper.getUser(selectedCandidate1.getCreatedById());
            if(nodeUser.isAlertMail())
                sendMailCandidateAccepted(nodeUser.getMail(), selectedCandidate1);
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
    
    public void rejectCandidatList(int idUser) {
        if (candidatBean.getSelectedCandidates() == null || candidatBean.getSelectedCandidates().isEmpty()) {
            printErreur("Pas de candidat sélectionné");
            return;
        }

        // envoie de mail au créateur du candidat si l'option mail est activée
        NodeUser nodeUser;
        
        for (CandidatDto selectedCandidate1 : candidatBean.getSelectedCandidates()) {
            selectedCandidate1 = candidatBean.getAllInfosOfCandidate(selectedCandidate1);
            if (!candidatService.rejectCandidate(selectedCandidate1, adminMessage, idUser)) {
                printErreur("Erreur pour le candidat : " + selectedCandidate1.getNomPref() + "(" + selectedCandidate1.getIdConcepte() + ")");
                return;
            }
            nodeUser = userHelper.getUser(selectedCandidate1.getCreatedById());
            if(nodeUser.isAlertMail())
                sendMailCandidateRejected(nodeUser.getMail(), selectedCandidate1);    
            conceptHelper.updateDateOfConcept(selectedCandidate1.getIdThesaurus(),
                    selectedCandidate1.getIdConcepte(), idUser); 
            ///// insert DcTermsData to add contributor
            dcElementHelper.addDcElementConcept(
                    new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                    selectedCandidate1.getIdConcepte(), selectedCandidate1.getIdThesaurus());
            ///////////////               
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
    
    private boolean sendMailCandidateAccepted(String mail, CandidatDto candidat) {
        if(adminMessage == null) adminMessage = ""; 
        try {
            String subject = "[" + selectedTheso.getThesoName() + "] Confirmation de l'acceptation de votre candidat (" + candidat.getNomPref() + ")";
            String contentFile = "<html><body>"
                    + "Cher(e) " + candidat.getCreatedBy() + ", <br/> "
                    + "<p> Votre candidat a été accepté par nos administrateurs, il est désormais intégré au thésaurus " + selectedTheso.getThesoName() + "<br/></p>"
                    + "Nous vous remercions de votre contribution à l'enrichissement du thésaurus <b>" + selectedTheso.getThesoName() + "</b> "
                    + "(concept : <a href=\"" + getPath() + "/?idc=" + candidat.getIdConcepte()
                    + "&idt=" + candidat.getIdThesaurus() + "\">" + candidat.getNomPref() + "</a>). "
                    
                    + "</b></b>"
                    + "Message de l'administrateur : " + "</b>"
                    + adminMessage
                    + "</b></b>"                    
                    
                    + "<br/><br/> Cordialement,<br/>"
                    + "L'équipe " + selectedTheso.getThesoName() + ".<br/> <img src=\"" + getPath() + "/resources/img/icon_opentheso2.png\" height=\"106\"></body></html>";

            if(!sendEmail(mail, subject, contentFile)) {
                printErreur("!! votre propostion n'a pas été envoyée !!");
                return false;
            }
        } catch (IOException ex) {
            printErreur("Erreur detectée pendant l'envoie du mail de notification! \n votre propostion n'a pas été envoyée !");
            return false;
        }
        return true;
    }
    
    private boolean sendMailCandidateRejected(String mail, CandidatDto candidat) {
        if(adminMessage == null) adminMessage = "";
        try {
            String subject = "[" + selectedTheso.getThesoName() + "] Refus de votre candidat (" + candidat.getNomPref() + ")";
            String contentFile = "<html><body>"
                    + "Cher(e) " + candidat.getCreatedBy() + ", <br/> "
                    + "<p> Votre candidat a été refusé par nos administrateurs, il n'a pas été intégré au thésaurus " + selectedTheso.getThesoName() + "<br/></p>"
                    + "Nous vous remercions de votre contribution à l'enrichissement du thésaurus <b>" + selectedTheso.getThesoName() + "</b> "
                    
                    + "</b></b>"
                    + "Message de l'administrateur : " + "</b>"
                    + adminMessage
                    + "</b></b>"
                   
                    + "L'équipe " + selectedTheso.getThesoName() + ".<br/> <img src=\"" + getPath() + "/resources/img/icon_opentheso2.png\" height=\"106\"></body></html>";

            if(!sendEmail(mail, subject, contentFile)) {
                printErreur("!! votre propostion n'a pas été envoyée !!");
                return false;
            }
        } catch (IOException ex) {
            printErreur("Erreur detectée pendant l'envoie du mail de notification! \n votre propostion n'a pas été envoyée !");
            return false;
        }
        return true;
    }    
    
    private boolean sendEmail(String emailDestination, String subject, String contentFile) throws IOException {
        return mailBean.sendMail(emailDestination, subject,  contentFile);    
    }
    
    private String getPath(){
        if(FacesContext.getCurrentInstance() == null) {
            return "";
        }
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        path = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        return path;
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

}
