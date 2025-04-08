package fr.cnrs.opentheso.bean.forgetpassword;

import fr.cnrs.opentheso.utils.ToolsHelper;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.bean.mail.MailBean;

import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;


@Data
@RequestScoped
@Named(value = "forgetPassBean")
public class ForgetPassBean implements Serializable {

    private MailBean mailBean;
    private UserRepository userRepository;
    private String sendTo;


    public ForgetPassBean(MailBean mailBean, UserRepository userRepository) {

        this.mailBean = mailBean;
        this.userRepository = userRepository;
    }

    public void sendMail() {

        if (StringUtils.isEmpty(sendTo)) {
            printMessage("Veuillez-saisir une adresse mail");
            return;
        }

        var user = userRepository.findByMail(sendTo);
        if (user.isPresent()) {

            var password = ToolsHelper.getNewId(10, false, false);
            var passwordMD5 = MD5Password.getEncodedPassword(password);
            var message = "Veuillez-trouver ci-joint vos coordonnées pour vous connecter à opentheso : " + "\n"
                    + "Votre pseudo : " + user.get().getUsername() + "\n votre passe : " + password;

            if (!mailBean.sendMail(sendTo, "Mot de passe oublié",  message)) {
                printMessage("Erreur d'envoie de mail, veuillez contacter l'administrateur");
                return;
            }

            user.get().setPassword(passwordMD5);
            userRepository.save(user.get());
        } else {
            printMessage("L'utilisateur n'existe pas");
        }
    }

    private void printMessage(String message) {
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_FATAL, "", message);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }
}
