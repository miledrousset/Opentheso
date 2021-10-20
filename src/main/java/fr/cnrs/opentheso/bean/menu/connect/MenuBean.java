package fr.cnrs.opentheso.bean.menu.connect;

import fr.cnrs.opentheso.bean.profile.MyAccountBean;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import fr.cnrs.opentheso.bean.profile.SuperAdminBean;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;


@Named(value = "menuBean")
@SessionScoped
public class MenuBean implements Serializable {

    @Inject 
    private SuperAdminBean superAdminBean;

    @Inject 
    private MyAccountBean myAccountBean;

    @Inject 
    private MyProjectBean myProjectBean;
    
    
    // MENU Profile
    public void redirectToUsersPage() throws IOException {
        superAdminBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/usersV1.xhtml");
    }

    public void redirectToProjetsPage() throws IOException {
        superAdminBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/projectsV1.xhtml");
    }

    public void redirectToThesorusPage() throws IOException {
        superAdminBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/thesaurusV1.xhtml");
    }
    
    public void redirectToMyProfilePage() throws IOException {
        myAccountBean.reset();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/myAccountV1.xhtml");
    }
    
    public void redirectToMesProjectsPage() throws IOException {
        myProjectBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/projectsV1.xhtml");
    }
    
    // MENU Paramètres
    public void redirectToIdetifiantPage() throws IOException {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/setting/identifierV1.xhtml");
    }
    
}
