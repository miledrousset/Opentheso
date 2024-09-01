/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.mail;

import com.sun.mail.smtp.SMTPTransport;
import java.io.Serializable;
import java.util.Properties;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "mailBean")
@RequestScoped
public class MailBean implements Serializable {

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


    public boolean sendMail(String sendTo, String subject, String message) {
        Properties props = getPrefMail();
        if (ObjectUtils.isEmpty(props)) {
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_FATAL, "", "Absence des préférences pour le serveur Mail");
            FacesContext.getCurrentInstance().addMessage(null, fm);
            return false;
        }        
        
        try { 
            var session = Session.getInstance(props);
            var msg = new MimeMessage(session);
           
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
	        msg.addHeader("format", "flowed");
            msg.setFrom(new InternetAddress(props.getProperty("mailFrom")));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(sendTo));
            msg.setSubject(subject);
            msg.setContent(message, "text/html; charset=utf-8");

            var transport = (SMTPTransport) session.getTransport(props.getProperty("transportMail"));
            transport.connect();
            transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
            transport.close();
            
            var fm = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Email envoyé avec succès");
            FacesContext.getCurrentInstance().addMessage(null, fm);
            return true;
        } catch (MessagingException e) {
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_FATAL, "", e.toString());
            FacesContext.getCurrentInstance().addMessage(null, fm);
        }
        return false;
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

}
