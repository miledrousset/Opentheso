package fr.cnrs.opentheso.bean.forgetpassword;

import fr.cnrs.opentheso.services.UserService;
import fr.cnrs.opentheso.utils.MessageUtils;
import fr.cnrs.opentheso.utils.ToolsHelper;
import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.services.MailService;

import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;


@Data
@RequestScoped
@Named(value = "forgetPassBean")
public class ForgetPassBean implements Serializable {

    private final UserService userService;
    private final MailService mailBean;

    private String sendTo;


    public void sendMail() {

        if (StringUtils.isEmpty(sendTo)) {
            MessageUtils.showErrorMessage("Veuillez-saisir une adresse mail");
            return;
        }

        var user = userService.getUserByMail(sendTo);
        if (user != null) {
            var password = ToolsHelper.getNewId(10, false, false);
            var passwordMD5 = MD5Password.getEncodedPassword(password);
            var message = "Veuillez-trouver ci-joint vos coordonnées pour vous connecter à Opentheso : " + "\n"
                    + "Votre pseudo : " + user.getUsername() + "\n votre passe : " + password;

            if (!mailBean.sendMail(sendTo, "Mot de passe oublié",  message)) {
                MessageUtils.showErrorMessage("Erreur pendant l'envoie de mail, veuillez contacter l'administrateur");
                return;
            }

            user.setPassword(passwordMD5);
            userService.saveUser(user);
        } else {
            MessageUtils.showErrorMessage("L'utilisateur n'existe pas");
        }
    }
}
