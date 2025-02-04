package fr.cnrs.opentheso.bean.forgetpassword;

import fr.cnrs.opentheso.repositories.ToolsHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.bean.mail.MailBean;

import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;


@Named(value = "forgetPassBean")
@RequestScoped
public class ForgetPassBean implements Serializable {

    @Autowired
    private MailBean mailBean;

    @Autowired
    private ToolsHelper toolsHelper;

    @Autowired
    private UserHelper userHelper;

    private String sendTo;


    public void sendMail() {
        if (sendTo == null || sendTo.isEmpty()) {
            printMessage("Veuillez-saisir une adresse mail");
            return;
        }

        if (userHelper.isUserMailExist(sendTo)) {
            String password = toolsHelper.getNewId(10, false, false);
            String passwordMD5 = MD5Password.getEncodedPassword(password);
            String pseudo = userHelper.getNameUser(sendTo);
            int idUser = userHelper.getIdUserFromMail(sendTo);
            if (idUser == -1) {
                printMessage("Absence des préférences pour le serveur Mail");
                return;
            }
            String subject = "Mot de passe oublié";
            String message = "Veuillez-trouver ci-joint vos coordonnées pour vous connecter à opentheso : " + "\n"
                    + "Votre pseudo : " + pseudo + "\n votre passe : " + password;            
            
            
            if (!mailBean.sendMail(sendTo, subject,  message)) {
                printMessage("Erreur d'envoie de mail, veuillez contacter l'administrateur");
                return;
            }
            if (!userHelper.updatePwd(idUser, passwordMD5)) {
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

    public String getMail() {
        return sendTo;
    }

    public void setMail(String mail) {
        this.sendTo = mail;
    }

}
