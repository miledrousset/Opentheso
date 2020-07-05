package fr.cnrs.opentheso.bean.session;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ResourceBundle;


@Named(value = "sessionControl")
@SessionScoped
public class SessionControl implements Serializable {

    @Inject
    private CurrentUser currentUser;

    private final int DEFAULT_TIMEOUT_IN_MIN = 10;


    public void isTimeout() {
        if (currentUser.getNodeUser() != null) {
            currentUser.disconnect();
        }
    }


    public int getTimeout() {
        int minNbr;
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
            minNbr = Integer.parseInt(bundlePref.getString("timeout_nbr_minute"));
        } catch(Exception e) {
            minNbr = DEFAULT_TIMEOUT_IN_MIN;
        }
        return (minNbr * 60 * 1000);
    }
    
}
