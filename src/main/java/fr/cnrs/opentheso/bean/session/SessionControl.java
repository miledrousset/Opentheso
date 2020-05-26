/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.session;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author miledrousset
 */
@Named(value = "sessionControl")
@RequestScoped
public class SessionControl {

    /**
     * Creates a new instance of SessionControl
     */
    public SessionControl() {
    }
    
    public void onIdle() {
        try {
         /*   FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "No activity.", "What are you doing over there?"));*/
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/index.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(SessionControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
    public void onActive() {
    /*    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                                        "Welcome Back", "Well, that's a long coffee break!"));*/
    }    
    
}
