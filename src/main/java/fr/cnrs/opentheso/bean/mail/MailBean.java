/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.mail;

import com.sun.mail.smtp.SMTPTransport;
import java.io.Serializable;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author miledrousset
 */
@Named(value = "mailBean")
@RequestScoped
public class MailBean implements Serializable {

    public MailBean() {
    }

    private Properties getPrefMail() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", bundlePref.getString("protocolMail"));
            props.setProperty("mail.smtp.host", bundlePref.getString("hostMail"));
            props.setProperty("mail.smtp.port", bundlePref.getString("portMail"));
            props.setProperty("mail.smtp.auth", bundlePref.getString("authMail"));
            props.setProperty("mailFrom", bundlePref.getString("mailFrom"));  
            props.setProperty("transportMail", bundlePref.getString("transportMail"));
            return props;
        } catch (Exception e) {
            return null;
        }
    }

    private void printMessage(String message) {
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_FATAL, "", message);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }

    public boolean sendMail(String sendTo, String subject, String message) {
        Properties props = getPrefMail();
        if (props == null) {
            printMessage("Absence des préférences pour le serveur Mail");
            return false;
        }        
        
        try { 
            Session session = Session.getInstance(props);
            Message msg = new MimeMessage(session);
           
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
	        msg.addHeader("format", "flowed");
            
            msg.setFrom(new InternetAddress(props.getProperty("mailFrom")));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(sendTo));
            msg.setSubject(subject);
            msg.setContent(message, "text/html; charset=utf-8");

            SMTPTransport transport = (SMTPTransport) session.getTransport(props.getProperty("transportMail"));
            transport.connect();
            transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
            transport.close();
            
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Email envoyé avec succès");
            FacesContext.getCurrentInstance().addMessage(null, fm);            
            
            return true;
        } catch (MessagingException e) {
            printMessage(e.toString());
        }
        return false;
    }

}
