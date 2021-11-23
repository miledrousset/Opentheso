package fr.cnrs.opentheso.bean.menu.connect;

import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.menu.users.NewUSerBean;
import fr.cnrs.opentheso.bean.profile.MyAccountBean;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import fr.cnrs.opentheso.bean.profile.SuperAdminBean;
import fr.cnrs.opentheso.bean.setting.CorpusBean;
import fr.cnrs.opentheso.bean.setting.PreferenceBean;
import fr.cnrs.opentheso.bean.toolbox.atelier.AtelierThesBean;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewEditionBean;
import fr.cnrs.opentheso.bean.toolbox.statistique.StatistiqueBean;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Named(value = "menuBean")
@SessionScoped
public class MenuBean implements Serializable {

    @Inject 
    private SuperAdminBean superAdminBean;

    @Inject 
    private MyAccountBean myAccountBean;

    @Inject 
    private MyProjectBean myProjectBean;

    @Inject 
    private CorpusBean corpusBean;

    @Inject 
    private PreferenceBean preferenceBean;
    
    @Inject
    private ViewEditionBean viewEditionBean;
    
    @Inject
    private AtelierThesBean atelierThesBean;
    
    @Inject
    private StatistiqueBean statistiqueBean;
    
    @Inject
    private NewUSerBean newUSerBean;
    
    @Inject 
    private SelectedTheso selectedTheso;
    
    @Inject 
    private CandidatBean candidatBean;
    
    @Inject 
    private CurrentUser currentUser;
    
    
    
    public boolean checkIfUserIsConnected() throws IOException {
        if (currentUser.getNodeUser() == null) {
            redirectToThesaurus();
            return false;
        }
        return true;
    }
    
    // LOGIN Page
    public void redirectToThesaurus() throws IOException {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/index.xhtml");
    }
    
    // LOGIN Page
    public void redirectToCandidatPage() throws IOException {
        candidatBean.initCandidatModule();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/candidat/condidat.xhtml");
    }
    
    // MENU Profile
    public void redirectToUsersPage() throws IOException {
        superAdminBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/users.xhtml");
    }

    public void redirectToProjetsPage() throws IOException {
        superAdminBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/projects.xhtml");
    }

    public void redirectToThesorusPage() throws IOException {
        superAdminBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/thesaurus.xhtml");
    }
    
    public void redirectToMyProfilePage() throws IOException {
        myAccountBean.reset();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/myAccount.xhtml");
    }
    
    public void redirectToMesProjectsPage() throws IOException {
        myProjectBean.init();
        newUSerBean.clear();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/myProject.xhtml");
    }
    
    // MENU Paramètres
    public void redirectToIdetifiantPage() throws IOException {
        preferenceBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/setting/identifier.xhtml");
    }
    
    public void redirectToPreferencePage() throws IOException {
        preferenceBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/setting/preference.xhtml");
    }
    
    public void redirectToCorpusPage() throws IOException {
        corpusBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/setting/corpus.xhtml");
    }
    
    ///Boite à outils
    public void redirectToEditionPage() throws IOException {
        viewEditionBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/toolbox/edition.xhtml");
    }
    
    public void redirectToAtelierPage() throws IOException {
        atelierThesBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/toolbox/atelier.xhtml");
    }
    
    public void redirectToMaintenancePage() throws IOException {
        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "", 
                    "Vous devez choisir un Thesorus avant !"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
            return;
        }
        atelierThesBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/toolbox/service.xhtml");
    }
    
    public void redirectToStatistiquePage() throws IOException {
        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "", 
                    "Vous devez choisir un Thesorus avant !"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
            return;
        }
        statistiqueBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/toolbox/statistic.xhtml");
    }

    // LOGIN Page
    public void redirectToLoginPage() throws IOException {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/login.xhtml");
    }
    
}
