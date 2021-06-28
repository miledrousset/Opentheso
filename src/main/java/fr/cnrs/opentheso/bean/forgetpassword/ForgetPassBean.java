/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.forgetpassword;

import com.sun.mail.smtp.SMTPTransport;
import fr.cnrs.opentheso.bdd.helper.ToolsHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.tools.MD5Password;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.Serializable;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author miledrousset
 */
@Named(value = "forgetPassBean")
@RequestScoped
public class ForgetPassBean implements Serializable {

    @Inject
    private Connect connect;

    private String mail;
    public ForgetPassBean() {
    }

    private Properties getPrefMail() {
        Properties props;
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
            props = new Properties();
            props.setProperty("mail.transport.protocol", bundlePref.getString("protocolMail"));
            props.setProperty("mail.smtp.host", bundlePref.getString("hostMail"));
            props.setProperty("mail.smtp.port", bundlePref.getString("portMail"));
            props.setProperty("mail.smtp.auth", bundlePref.getString("authMail"));
            props.setProperty("mailFrom", bundlePref.getString("mailFrom"));  
            props.setProperty("transportMail", bundlePref.getString("transportMail"));             
            
            return props;
        } catch (Exception e) {
        }
        return null;
    }

    public void sendMail() {
        if (mail == null || mail.isEmpty()) {
            printMessage("Veuillez-saisir une adresse mail");
            return;
        }

        Properties props = getPrefMail();
        if (props == null) {
            printMessage("Absence des préférences pour le serveur Mail");
            return;
        }

        UserHelper userHelper = new UserHelper();
        if (userHelper.isUserMailExist(connect.getPoolConnexion(), mail)) {
            ToolsHelper toolsHelper = new ToolsHelper();
            String password = toolsHelper.getNewId(10);
            password = MD5Password.getEncodedPassword(password);
            String pseudo = userHelper.getNameUser(connect.getPoolConnexion(), mail);
            int idUser = userHelper.getIdUserFromMail(connect.getPoolConnexion(), mail);
            if (idUser == -1) {
                printMessage("Absence des préférences pour le serveur Mail");
                return;
            }
            if (!sendMail__(mail, password, pseudo, props)) {
                printMessage("Erreur d'envoie de mail, veuillez contacter l'administrateur");
                return;
            }
            if (!userHelper.updatePwd(connect.getPoolConnexion(), idUser, password)) {
                printMessage("Erreur base de données");
            }
        } else {
            printMessage("L'utilisateur n'existe pas");
        }
    }

    private void printMessage(String message) {
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_FATAL, "", message);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }

    private boolean sendMail__(String email, String pass, String pseudo, Properties props) {
        try {
            Session session = Session.getInstance(props);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(props.getProperty("mailFrom")));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            msg.setSubject("Mot de passe oublié"); /// mot.titlePass

            msg.setText("Veuillez-trouver ci-joint vos coordonnées pour vous connecter à opentheso : " + "\n"
                    + "Votre pseudo : " + pseudo + "\n votre passe : " + pass);

            SMTPTransport transport = (SMTPTransport) session.getTransport(props.getProperty("transportMail"));
            transport.connect();
            transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
            transport.close();
            
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Email envoyé avec succès");
            FacesContext.getCurrentInstance().addMessage(null, fm);            
            
            return true;
        } catch (Exception e) {
            printMessage(e.toString());
        }
        return false;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

}
