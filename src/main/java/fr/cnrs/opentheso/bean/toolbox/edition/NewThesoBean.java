package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.entites.LanguageIso639;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.entites.UserGroupThesaurus;
import fr.cnrs.opentheso.models.thesaurus.Thesaurus;
import fr.cnrs.opentheso.repositories.LanguageRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.ThesaurusService;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "newThesoBean")
public class NewThesoBean implements Serializable {

    private final CurrentUser currentUser;
    private final RoleOnThesoBean roleOnThesoBean;
    private final ViewEditionBean viewEditionBean;
    private final UserGroupLabelRepository userGroupLabelRepository;
    private final SelectedTheso selectedTheso;
    private final PreferenceService preferenceService;
    private final LanguageRepository languageRepository;
    private final UserGroupThesaurusRepository userGroupThesaurusRepository;
    private final ThesaurusService thesaurusService;

    private List<LanguageIso639> allLangs;
    private List<UserGroupLabel> nodeProjects;
    private String title, selectedLang, selectedProject;


    public void init() {
        allLangs = languageRepository.findAll();
        selectedLang = null;
        selectedProject = "";
        title = "";
        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeProjects = userGroupLabelRepository.findAll();
            nodeProjects.sort(Comparator.comparing(UserGroupLabel::getLabel, String.CASE_INSENSITIVE_ORDER));
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
        var idNewTheso = thesaurusService.addThesaurusRollBack();
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
        if (!thesaurusService.addThesaurusTraductionRollBack(thesaurus)) {
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
        var nodePreference = roleOnThesoBean.getNodePreference();
        if (nodePreference == null) {
            preferenceService.initPreferences(idNewTheso, selectedLang);
        } else {
            nodePreference.setPreferredName(title);
            nodePreference.setSourceLang(selectedLang);
            preferenceService.addPreference(nodePreference, idNewTheso);
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
