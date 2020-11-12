package fr.cnrs.opentheso.bean.condidat;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.concept.SynonymBean;
import fr.cnrs.opentheso.bean.condidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.utils.EmailUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.*;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Named(value = "discussionService")
@SessionScoped
public class DiscussionService implements Serializable {

    @Inject
    private CandidatBean candidatBean;
    
    @Inject
    private LanguageBean langueBean;

    @Inject
    private Connect connect;

    @Inject
    private LanguageBean languageBean;

    private String email;
    
    private List<String> participants;


    public List<String> getParticipantsInConversation() {
        
        participants = new MessageDao().getParticipantsByCandidat(
                connect.getPoolConnexion(), 
                candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus());
        
        if (CollectionUtils.isEmpty(participants)) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.send_message.msg8"));
            return new ArrayList<>();
        }

        PrimeFaces.current().ajax().update("candidatForm");
        PrimeFaces.current().executeScript("PF('participantsList').show();");

        return participants;
    }

    public void sendMessage() {

        if (candidatBean.getInitialCandidat() == null) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.send_message.msg7"));
            return;
        }
        
        if (StringUtils.isEmpty(candidatBean.getMessage())) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg1"));
            return;
        }
        
        MessageDto messageDto = new MessageDto();
        messageDto.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        messageDto.setNom(candidatBean.getCurrentUser().getUsername().toUpperCase());
        messageDto.setMsg(candidatBean.getMessage());
        
        MessageDao messageDao = new MessageDao();
        messageDao.addNewMessage(connect.getPoolConnexion(), 
                candidatBean.getMessage(), 
                candidatBean.getCurrentUser().getNodeUser().getIdUser(), 
                candidatBean.getCandidatSelected().getIdConcepte(), 
                candidatBean.getCandidatSelected().getIdThesaurus());

        reloadMessage();
      
        candidatBean.setMessage("");
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, langueBean.getMsg("candidat.send_message.msg2"));
    }
    
    public void reloadMessage(){
        MessageDao messageDao = new MessageDao();
        candidatBean.getCandidatSelected().setMessages(messageDao.getAllMessagesByCandidat(
                connect.getPoolConnexion(), 
                candidatBean.getCandidatSelected().getIdConcepte(), 
                candidatBean.getCandidatSelected().getIdThesaurus(), 
                candidatBean.getCurrentUser().getNodeUser().getIdUser()));   
    }

    public void sendInvitation() {

        if (StringUtils.isEmpty(email)) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg3"));
        } else if (!EmailUtils.isValidEmailAddress(email)) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg4"));
        } else {

            String from = "opentheso@mom.fr";
            String host = "smtp.mom.fr";

            Properties properties = System.getProperties();
            properties.setProperty("mail.smtp.host", host);

            Session session = Session.getDefaultInstance(properties);

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                message.setSubject("Invitation à une conversation !");
                message.setText("C'est le body du message");
                // Send message
                Transport.send(message);
                candidatBean.showMessage(FacesMessage.SEVERITY_INFO, langueBean.getMsg("candidat.send_message.msg5"));
            } catch (MessagingException mex) {
                candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg6"));
            }
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

}
