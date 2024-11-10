package fr.cnrs.opentheso.services.candidats;

import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.repositories.candidats.MessageCandidatHelper;
import fr.cnrs.opentheso.models.candidats.MessageDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.mail.MailBean;

import fr.cnrs.opentheso.utils.EmailUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Data
@Named(value = "discussionService")
@SessionScoped
public class DiscussionService implements Serializable {

    @Autowired @Lazy
    private CandidatBean candidatBean;

    @Autowired @Lazy
    private LanguageBean langueBean;

    

    @Autowired @Lazy
    private LanguageBean languageBean;

    @Autowired
    private MessageCandidatHelper messageCandidatHelper;

    @Autowired @Lazy
    private MailBean mailBean;

    @Value("${smpt.protocol}")
    private String protocolMail;

    @Value("${smpt.hostname}")
    private String hostname;

    @Value("${smpt.portNumber}")
    private String portNumber;

    @Value("${smpt.authorization}")
    private String authorization;

    @Value("${smpt.mailFrom}")
    private String mailFrom;

    @Value("${smpt.transportMail}")
    private String transportMail;

    private String email;
    private List<NodeUser> nodeUsers;


    public void clear() {
        if (nodeUsers != null) {
            nodeUsers.clear();
            nodeUsers = null;
        }
        email = null;
    }

    public void getParticipantsInConversation() {
        setListUsersForMail();

        if (CollectionUtils.isEmpty(nodeUsers)) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.send_message.msg8"));
        } else {
            PrimeFaces.current().executeScript("PF('participantsList').show();");
        }
    }
    
    private void setListUsersForMail(){
        nodeUsers = messageCandidatHelper.getParticipantsByCandidat(candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus());        
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

        messageCandidatHelper.addNewMessage(
                candidatBean.getMessage(),
                candidatBean.getCurrentUser().getNodeUser().getIdUser(),
                candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus());

        sendNotificationMail();

        reloadMessage();

        candidatBean.setMessage("");
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, langueBean.getMsg("candidat.send_message.msg2"));
    }

    private void sendNotificationMail() {

        // Envoi de mail aux participants à la discussion
        String subject = "Nouveau message module candidat";
        String message = "Vous avez participé à la discussion pour ce candidat "
                + candidatBean.getCandidatSelected().getNomPref() + ", "
                + " id= " + candidatBean.getCandidatSelected().getIdConcepte()
                + ". Sachez qu’un nouveau message a été posté.";

        // Exécution asynchrone de la méthode setListUsersForMail
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            setListUsersForMail();
            if(CollectionUtils.isNotEmpty(nodeUsers)) {
                nodeUsers.stream()
                        .filter(NodeUser::isAlertMail)
                        .forEach(user -> mailBean.sendMail(user.getMail(), subject,  message));
            }
        });
        executorService.shutdown();

    }
     

    public void reloadMessage() {
        candidatBean.getCandidatSelected().setMessages(messageCandidatHelper.getAllMessagesByCandidat(
                candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus(),
                candidatBean.getCurrentUser().getNodeUser().getIdUser()));
    }

    private Properties getPrefMail() {
        var props = new Properties();
        props.setProperty("mail.transport.protocol", protocolMail);
        props.setProperty("mail.smtp.host", hostname);
        props.setProperty("mail.smtp.port", portNumber);
        props.setProperty("mail.smtp.auth", authorization);
        props.setProperty("mailFrom", mailFrom);
        props.setProperty("transportMail", transportMail);
        return props;
    }

    public void sendInvitation() {
        Properties props = getPrefMail();

        if (StringUtils.isEmpty(email)) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg3"));
        } else if (!EmailUtils.isValidEmailAddress(email)) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, langueBean.getMsg("candidat.send_message.msg4"));
        } else {
            Properties properties = System.getProperties();
            properties.setProperty("mail.smtp.host", props.getProperty("hostMail"));

            Session session = Session.getDefaultInstance(properties);

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(props.getProperty("mailFrom")));
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
}
