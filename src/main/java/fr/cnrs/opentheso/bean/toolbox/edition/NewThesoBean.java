package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.entites.UserGroupThesaurus;
import fr.cnrs.opentheso.models.languages.Languages_iso639;
import fr.cnrs.opentheso.models.thesaurus.Thesaurus;
import fr.cnrs.opentheso.repositories.LanguageHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository2;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "newThesoBean")
@SessionScoped
public class NewThesoBean implements Serializable {

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RoleOnThesoBean roleOnThesoBean;

    @Autowired
    private ViewEditionBean viewEditionBean;

    @Autowired
    private UserGroupLabelRepository2 userGroupLabelRepository;

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

    @Autowired
    private UserGroupThesaurusRepository userGroupThesaurusRepository;

    private String title;
    private ArrayList<Languages_iso639> allLangs;
    private String selectedLang;

    private List<UserGroupLabel> nodeProjects;
    private String selectedProject;


    public void init() {
        allLangs = languageHelper.getAllLanguages();
        selectedLang = null;
        selectedProject = "";
        title = "";
        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeProjects = userGroupLabelRepository.findAll();
        } else {
            nodeProjects = userGroupLabelRepository.findProjectsByRole(currentUser.getNodeUser().getIdUser(), 2);
            for (UserGroupLabel userGroupLabel : nodeProjects) {
                selectedProject = "" + userGroupLabel.getId();
            }
        }
    }

    /**
     * Permet de supprimer un thésaurus
     */
    public void addNewTheso() {
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
        var idNewTheso = thesaurusHelper.addThesaurusRollBack();
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
            var userGroupThesaurus = UserGroupThesaurus.builder().idThesaurus(idNewTheso).idGroup(idProject).build();
            userGroupThesaurusRepository.save(userGroupThesaurus);
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

}
