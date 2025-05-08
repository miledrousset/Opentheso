package fr.cnrs.opentheso.bean;

import fr.cnrs.opentheso.entites.CandidatMessages;
import fr.cnrs.opentheso.models.users.NodeUser;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.repositories.CandidatMessageRepository;
import fr.cnrs.opentheso.models.candidats.MessageDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.mail.MailBean;
import fr.cnrs.opentheso.utils.EmailUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@SessionScoped
@RequiredArgsConstructor
@Named(value = "discussionCandidatBean")
public class DiscussionCandidatBean implements Serializable {

    private final CandidatBean candidatBean;
    private final LanguageBean languageBean;
    private final MailBean mailBean;
    private final CandidatMessageRepository candidatMessageRepository;

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
        if (candidatBean.getCandidatSelected() != null) {
            var candidatMessages = candidatMessageRepository.findMessagesByConceptAndThesaurus(candidatBean.getCandidatSelected().getIdConcepte(),
                    candidatBean.getCandidatSelected().getIdThesaurus());
            if (CollectionUtils.isNotEmpty(candidatMessages)) {
                nodeUsers = candidatMessages.stream()
                        .map(element -> NodeUser.builder().idUser(element.getIdUser()).build())
                        .toList();
            } else {
                nodeUsers = new ArrayList<>();
            }
        } else {
            nodeUsers = new ArrayList<>();
        }
    }

    public void sendMessage() {
        if (candidatBean.getInitialCandidat() == null) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.send_message.msg7"));
            return;
        }

        if (StringUtils.isEmpty(candidatBean.getMessage())) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.send_message.msg1"));
            return;
        }

        candidatMessageRepository.save(CandidatMessages.builder()
                        .value(candidatBean.getMessage())
                        .idConcept(candidatBean.getCandidatSelected().getIdConcepte())
                        .idThesaurus(candidatBean.getCandidatSelected().getIdThesaurus())
                        .idUser(candidatBean.getCurrentUser().getNodeUser().getIdUser())
                        .date(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()))
                .build());

        sendNotificationMail();

        reloadMessage();

        candidatBean.setMessage("");
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.send_message.msg2"));
    }

    private void sendNotificationMail() {

        // Envoi de mail aux participants à la discussion
        String subject = "Nouveau message module candidat";
        String message = "Vous avez participé à la discussion pour ce candidat "
                + candidatBean.getCandidatSelected().getNomPref() + ", "
                + " id= " + candidatBean.getCandidatSelected().getIdConcepte()
                + ". Sachez qu’un nouveau message a été posté.";
        setListUsersForMail();

        if (CollectionUtils.isNotEmpty(nodeUsers)) {
            nodeUsers.stream()
                    .filter(user -> user != null && Boolean.TRUE.equals(user.isAlertMail())) // Vérifie si l'alerte est activée
                    .forEach(user -> {
                        try {
                            mailBean.sendMail(user.getMail(), subject, message);
                        } catch (Exception e) {
                            System.err.println("Erreur lors de l'envoi du mail à : " + user.getMail());
                            e.printStackTrace();
                        }
                    });
        }
    }

    public void reloadMessage() {
        var candidatMessages = candidatMessageRepository.findMessagesByConceptAndThesaurus(
                candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus());

        if (CollectionUtils.isNotEmpty(candidatMessages)) {
            candidatBean.getCandidatSelected().setMessages(candidatMessages.stream().map(element ->
                            MessageDto.builder()
                                    .msg(element.getValue())
                                    .nom(element.getUsername())
                                    .idUser(element.getIdUser())
                                    .mine(candidatBean.getCurrentUser().getNodeUser().getIdUser() == element.getIdUser())
                                    .date(element.getDate())
                                    .build())
                    .toList());
        } else {
            candidatBean.getCandidatSelected().setMessages(List.of());
        }
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
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.send_message.msg3"));
        } else if (!EmailUtils.isValidEmailAddress(email)) {
            candidatBean.showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.send_message.msg4"));
        } else {
            Properties properties = System.getProperties();
            properties.setProperty("mail.smtp.host", props.getProperty("hostMail"));
            try {
                MimeMessage message = new MimeMessage(Session.getDefaultInstance(properties));
                message.setFrom(new InternetAddress(props.getProperty("mailFrom")));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                message.setSubject("Invitation à une conversation !");
                message.setText("C'est le body du message");
                Transport.send(message);
                candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.send_message.msg5"));
            } catch (MessagingException mex) {
                candidatBean.showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.send_message.msg6"));
            }
        }
    }
}
