package fr.cnrs.opentheso.bean.menu.connect;

import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.bean.graph.DataGraphView;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.menu.users.NewUserBean;
import fr.cnrs.opentheso.bean.profile.MyAccountBean;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import fr.cnrs.opentheso.bean.profile.SuperAdminBean;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.search.SearchBean;
import fr.cnrs.opentheso.bean.setting.CorpusBean;
import fr.cnrs.opentheso.bean.setting.PreferenceBean;
import fr.cnrs.opentheso.bean.toolbox.atelier.AtelierThesBean;
import fr.cnrs.opentheso.bean.toolbox.edition.FlagBean;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewEditionBean;
import fr.cnrs.opentheso.bean.toolbox.statistique.StatistiqueBean;
import java.io.IOException;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Named(value = "menuBean")
@SessionScoped
public class MenuBean implements Serializable {

    @Autowired @Lazy 
    private SearchBean searchBean;

    @Autowired @Lazy 
    private SuperAdminBean superAdminBean;

    @Autowired @Lazy 
    private MyAccountBean myAccountBean;

    @Autowired @Lazy 
    private MyProjectBean myProjectBean;

    @Autowired @Lazy 
    private CorpusBean corpusBean;

    @Autowired @Lazy 
    private PreferenceBean preferenceBean;
    
    @Autowired @Lazy
    private ViewEditionBean viewEditionBean;

    @Autowired @Lazy
    private FlagBean flagBean;
    
    @Autowired @Lazy
    private AtelierThesBean atelierThesBean;
    
    @Autowired @Lazy
    private StatistiqueBean statistiqueBean;
    
    @Autowired @Lazy
    private NewUserBean newUserBean;
    
    @Autowired @Lazy 
    private SelectedTheso selectedTheso;
    
    @Autowired @Lazy 
    private CandidatBean candidatBean;
    
    @Autowired @Lazy 
    private CurrentUser currentUser;
    
    @Autowired @Lazy
    private PropositionBean propositionBean;
    @Autowired @Lazy
    private DataGraphView dataGraphView;    
    
    private boolean notificationPannelVisible;
    
    private String activePageName = "index";
    
    
    
    public boolean checkIfUserIsConnected() throws IOException {
        if (currentUser.getNodeUser() == null) {
            redirectToThesaurus();
            return false;
        }
        return true;
    }
    
    // LOGIN Page
    public void redirectToThesaurus() throws IOException {
        activePageName = "index";
        notificationPannelVisible = true;
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/index.xhtml");
    }
    
    // LOGIN Page
    public void redirectToCandidatPage() throws IOException {
        activePageName = "candidat";
        notificationPannelVisible = false;
        candidatBean.initCandidatModule();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/candidat/candidat.xhtml");
    }
    
    // LOGIN Page
    public void redirectToGraphPage() throws IOException {
        activePageName = "graph";
        notificationPannelVisible = false;
        dataGraphView.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/graphview/graph.xhtml");
    }    
    
    // MENU Profile
    public void redirectToUsersPage() throws IOException {
        activePageName = "users";
        notificationPannelVisible = false;
        superAdminBean.init();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/users.xhtml");
    }

    public void redirectToProjetsPage() throws IOException {
        activePageName = "Projects";
        notificationPannelVisible = false;
        superAdminBean.init();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/projects.xhtml");
    }

    public void redirectToThesorusPage() throws IOException {
        activePageName = "thesorus";
        notificationPannelVisible = false;
        superAdminBean.init();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/thesaurus.xhtml");
    }
    
    public void redirectToMyProfilePage() throws IOException {
        activePageName = "myAccount";
        notificationPannelVisible = false;
        myAccountBean.reset();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/myAccount.xhtml");
    }
    
    public void redirectToMesProjectsPage() throws IOException {
        activePageName = "myProject";
        notificationPannelVisible = false;
        myProjectBean.init();
        newUserBean.clear();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/profile/myProject.xhtml");
    }
    
    // MENU Paramètres
    public void redirectToIdetifiantPage() throws IOException {
        activePageName = "identifier";
        notificationPannelVisible = false;
        preferenceBean.init();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/setting/identifier.xhtml");
    }
    
    public void redirectToPreferencePage() throws IOException {
        activePageName = "preference";
        notificationPannelVisible = false;
        preferenceBean.init();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/setting/preference.xhtml");
    }
    
    public void redirectToCorpusPage() throws IOException {
        activePageName = "corpus";
        notificationPannelVisible = false;
        corpusBean.init();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/setting/corpus.xhtml");
    }
    
    ///Boite à outils
    public void redirectToEditionPage() throws IOException {
        activePageName = "edition";
        notificationPannelVisible = false;
        viewEditionBean.init();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/toolbox/edition.xhtml");
    }

    public void redirectToFlagPage() throws IOException {
        activePageName = "flag";
        notificationPannelVisible = false;
        flagBean.init();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/toolbox/flag.xhtml");
    }
    
    public void redirectToAtelierPage() throws IOException {
        activePageName = "atelier";
        atelierThesBean.init();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/toolbox/atelier.xhtml");
    }
    
    public void redirectToMaintenancePage() throws IOException {
        activePageName = "service";
        notificationPannelVisible = false;
        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "", 
                    "Vous devez choisir un Thesorus avant !"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
            return;
        }
        atelierThesBean.init();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/toolbox/service.xhtml");
    }
    
    public void redirectToStatistiquePage() throws IOException {
        activePageName = "statistic";
        notificationPannelVisible = false;
        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "", 
                    "Vous devez choisir un Thesorus avant !"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
            return;
        }
        statistiqueBean.init();
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/toolbox/statistic.xhtml");
    }

    // LOGIN Page
    public void redirectToLoginPage() throws IOException {
        activePageName = "login";
        notificationPannelVisible = false;
        propositionBean.searchNewPropositions();
        propositionBean.setRubriqueVisible(false);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + "/login.xhtml");
    }

    public boolean isNotificationPannelVisible() {
        return notificationPannelVisible;
    }

    public void setNotificationPannelVisible(boolean notificationPannelVisible) {
        this.notificationPannelVisible = notificationPannelVisible;
    }

    public String getActivePageName() {
        return activePageName;
    }
    
}
