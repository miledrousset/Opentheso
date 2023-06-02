package fr.cnrs.opentheso.bdd.tools;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.validation.constraints.Email;

/**
 *
 * @author miledrousset
 */
@Named
@RequestScoped
public class CustomValidationView {
    
    private String text;

    @Email(message = "zzzmust be a valid email")
    private String email;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}

