package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.models.languages.Languages_iso639;
import fr.cnrs.opentheso.models.thesaurus.Thesaurus;
import fr.cnrs.opentheso.repositories.LanguageHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.models.users.NodeUserGroup;

import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "newThesoBean")
@SessionScoped
public class NewThesoBean implements Serializable {

    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private RoleOnThesoBean roleOnThesoBean;
    @Autowired @Lazy private ViewEditionBean viewEditionBean;

    @Autowired
    private SelectedTheso selectedTheso;

    @Autowired
    private PreferencesHelper preferencesHelper;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private LanguageHelper languageHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    private String title;
    private ArrayList<Languages_iso639> allLangs;
    private String selectedLang;

    private ArrayList<NodeUserGroup> nodeProjects;
    private String selectedProject;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(allLangs!= null){
            allLangs.clear();
            allLangs = null;
        }
        if(nodeProjects!= null){
            nodeProjects.clear();
            nodeProjects = null;
        }
        title = null;
        selectedLang = null;        
        selectedProject = null;
    }      
    
    /**
     * Creates a new instance of DeleteThesoBean
     */
    public NewThesoBean() {
    }

    public void init() {
        allLangs = languageHelper.getAllLanguages();
        selectedLang = null;
        selectedProject = "";
        title = "";
        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeProjects = userHelper.getAllProject();
        } else {
            nodeProjects = userHelper.getProjectsOfUserAsAdmin(currentUser.getNodeUser().getIdUser());
            for (NodeUserGroup nodeUserProject : nodeProjects) {
                selectedProject = "" + nodeUserProject.getIdGroup();
            }
        }
    }

    /**
     * Permet de supprimer un thésaurus
     */
    public void addNewTheso() {
        String idNewTheso = null;
        FacesMessage msg;

        if (title == null || title.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "le label est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (selectedLang == null || selectedLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "la langue est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        int idProject = -1;
        try {
            if (!StringUtils.isEmpty(selectedProject)) {
                idProject = Integer.parseInt(selectedProject);
            }
        } catch (NumberFormatException e) {
        }

        // création du thésaurus
        idNewTheso = thesaurusHelper.addThesaurusRollBack();
        if(idNewTheso == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la création !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        Thesaurus thesaurus = new Thesaurus();
        thesaurus.setCreator(currentUser.getNodeUser().getName());
        thesaurus.setContributor(currentUser.getNodeUser().getName());
        thesaurus.setId_thesaurus(idNewTheso);
        thesaurus.setTitle(title);
        thesaurus.setLanguage(selectedLang);
        if (!thesaurusHelper.addThesaurusTraductionRollBack(thesaurus)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la création !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        // ajouter le thésaurus dans le group de l'utilisateur
        if (idProject != -1) { // si le groupeUser = - 1, c'est le cas d'un SuperAdmin, alors on n'intègre pas le thésaurus dans un groupUser
            if (!userHelper.addThesoToGroup(idNewTheso, idProject)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la création !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }


        // écriture des préférences en utilisant le thésaurus en cours pour duppliquer les infos
        NodePreference nodePreference = roleOnThesoBean.getNodePreference();
        if (nodePreference == null) {
            preferencesHelper.initPreferences(idNewTheso, selectedLang);
        } else {
            nodePreference.setPreferredName(title);
            nodePreference.setSourceLang(selectedLang);
            preferencesHelper.addPreference(nodePreference, idNewTheso);
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "thesaurus ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init();
        roleOnThesoBean.showListTheso(currentUser, selectedTheso);
        viewEditionBean.init();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formMenu:idListTheso");
            pf.ajax().update("toolBoxForm");
            pf.ajax().update("toolBoxForm:listThesoForm");
            pf.ajax().update("messageIndex");           
        }  
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Languages_iso639> getAllLangs() {
        return allLangs;
    }

    public void setAllLangs(ArrayList<Languages_iso639> allLangs) {
        this.allLangs = allLangs;
    }

    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public ArrayList<NodeUserGroup> getNodeProjects() {
        return nodeProjects;
    }

    public void setNodeProjects(ArrayList<NodeUserGroup> nodeProjects) {
        this.nodeProjects = nodeProjects;
    }

    public String getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(String selectedProject) {
        this.selectedProject = selectedProject;
    }

}
