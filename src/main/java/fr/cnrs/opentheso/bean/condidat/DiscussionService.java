package fr.cnrs.opentheso.bean.condidat;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.condidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.utils.EmailUtils;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;


@Named(value = "discussionService")
@SessionScoped
public class DiscussionService implements Serializable {

    @Inject
    private CandidatBean candidatBean;
    
    @Inject
    private LanguageBean langueBean;

    @Inject
    private Connect connect;

    private String email;

    public List<String> getParticipantsInConversation() {
        if (candidatBean != null && candidatBean.getCandidatSelected() != null) {
            return new MessageDao().getParticipantsByCandidat(
                    connect.getPoolConnexion(), candidatBean.getCandidatSelected().getIdConcepte(),
                    candidatBean.getCandidatSelected().getIdThesaurus());
        } else {
            return new ArrayList<>();
        }
    }

    public void sendMessage() throws SQLException {
        
        if (StringUtils.isEmpty(candidatBean.getMessage())) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg1"));
            return;
        }
        
        MessageDto messageDto = new MessageDto();
        messageDto.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        messageDto.setNom(candidatBean.getCurrentUser().getUsername().toUpperCase());
        messageDto.setMsg(candidatBean.getMessage());
        
        HikariDataSource connection = connect.getPoolConnexion();
        
        MessageDao messageDao = new MessageDao();
        messageDao.addNewMessage(connection, 
                candidatBean.getMessage(), 
                candidatBean.getCurrentUser().getNodeUser().getIdUser()+"", 
                candidatBean.getCandidatSelected().getIdConcepte(), 
                candidatBean.getCandidatSelected().getIdThesaurus());

        candidatBean.getCandidatSelected().setMessages(messageDao.getAllMessagesByCandidat(
                connection, 
                candidatBean.getCandidatSelected().getIdConcepte(), 
                candidatBean.getCandidatSelected().getIdThesaurus(), 
                candidatBean.getCurrentUser().getNodeUser().getIdUser()));
      
        candidatBean.setMessage("");
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, langueBean.getMsg("candidat.send_message.msg2"));
        
        connection.close();
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

}
