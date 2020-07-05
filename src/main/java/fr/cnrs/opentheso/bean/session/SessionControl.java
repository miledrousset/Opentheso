package fr.cnrs.opentheso.bean.session;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import javax.inject.Inject;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;


@Named(value = "sessionControl")
@RequestScoped
public class SessionControl {

    @Inject
    private CurrentUser currentUser;
    
    public void isTimeout() {
        if (currentUser.getNodeUser() != null) {
            currentUser.disconnect();
        }
    }
    
}
