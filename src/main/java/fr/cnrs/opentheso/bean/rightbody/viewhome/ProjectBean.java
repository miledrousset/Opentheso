package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.entites.LanguageIso639;
import fr.cnrs.opentheso.repositories.LanguageRepository;
import fr.cnrs.opentheso.repositories.StatisticHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.entites.ProjectDescription;
import fr.cnrs.opentheso.repositories.ProjectDescriptionRepository;

import jakarta.inject.Inject;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Value;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;


@Data
@SessionScoped
@Named(value = "projectBean")
public class ProjectBean implements Serializable {

    private SelectedTheso selectedTheso;
    private LanguageBean languageBean;
    private LanguageRepository languageRepository;
    private StatisticHelper statisticHelper;
    private ProjectDescriptionRepository projectDescriptionRepository;

    private String workLanguage, description, langCode, langCodeSelected, projectIdSelected;
    private boolean projectDescription, editingHomePage, isButtonEnable;
    private ProjectDescription projectDescriptionSelected;
    private List<NodeIdValue> listeThesoOfProject;
    private List<LanguageIso639> allLangs, selectedLangs;


    @Inject
    public ProjectBean(@Value("${settings.workLanguage:fr}") String workLanguage,
                       SelectedTheso selectedTheso,
                       LanguageBean languageBean,
                       StatisticHelper statisticHelper,
                       LanguageRepository languageRepository,
                       ProjectDescriptionRepository projectDescriptionRepository) {

        this.workLanguage = workLanguage;
        this.languageBean = languageBean;
        this.selectedTheso = selectedTheso;
        this.statisticHelper = statisticHelper;
        this.languageRepository = languageRepository;
        this.projectDescriptionRepository = projectDescriptionRepository;
    }


    public void initProject(String projectIdSelected, CurrentUser currentUser) {

        this.projectIdSelected = projectIdSelected;
        selectedLangs = languageRepository.findLanguagesByProject(projectIdSelected);
        projectDescription = CollectionUtils.isNotEmpty(selectedLangs);

        projectDescriptionSelected = projectDescriptionRepository.findByIdGroupAndLang(projectIdSelected, getLang()).get();

        if (ObjectUtils.isEmpty(projectDescriptionSelected)) {
            if (CollectionUtils.isNotEmpty(selectedLangs)) {
                projectDescriptionSelected = projectDescriptionRepository.findByIdGroupAndLang(projectIdSelected,
                        selectedLangs.get(0).getIso6391()).get();

                if (ObjectUtils.isEmpty(projectDescriptionSelected)) {
                    projectDescriptionSelected = new ProjectDescription();
                    projectDescriptionSelected.setLang(getLang());
                    projectDescriptionSelected.setIdGroup(projectIdSelected);
                    projectDescriptionSelected.setLang(getLang());
                }
            } else {
                projectDescriptionSelected = new ProjectDescription();
                projectDescriptionSelected.setLang(getLang());
                projectDescriptionSelected.setIdGroup(projectIdSelected);
                projectDescriptionSelected.setLang(getLang());
            }
        }

        if (ObjectUtils.isNotEmpty(projectDescriptionSelected)) {
            description = projectDescriptionSelected.getDescription();
            langCodeSelected = projectDescriptionSelected.getLang();
        }
        listeThesoOfProject = currentUser.getUserPermissions().getListThesos();

        for (NodeIdValue element : listeThesoOfProject) {
            try {
                element.setNbrConcepts(statisticHelper.getNbCpt(element.getId()));
            } catch(Exception ex) {
                element.setNbrConcepts(0);
            }
        }
    }

    /**
     * pour effacer toutes les données des variables
     */
    public void reset(){
        projectDescriptionSelected = null;
    }

    public void init() {
        editingHomePage = false;
        isButtonEnable = true;
    }

    public void back() {
        projectDescription = true;
        editingHomePage = false;
        isButtonEnable = true;
    }

    public void setEditPage() {
        if (CollectionUtils.isEmpty(allLangs)) {
            allLangs = languageRepository.findAll();
        }

        projectDescriptionSelected = projectDescriptionRepository.findByIdGroupAndLang(projectIdSelected, langCodeSelected).get();
        if (!ObjectUtils.isEmpty(projectDescriptionSelected)) {
            description = projectDescriptionSelected.getDescription();
            langCode = projectDescriptionSelected.getLang();
        } else {
            projectDescriptionSelected = new ProjectDescription();
            projectDescriptionSelected.setLang(getLang());
            projectDescriptionSelected.setIdGroup(projectIdSelected);
            langCode = getLang();
        }

        projectDescription = false;
        editingHomePage = true;
        isButtonEnable = false;
    }

    public void updateHomePage() {

        if (StringUtils.isEmpty(description)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Veuillez saisir une description !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (description.equalsIgnoreCase(projectDescriptionSelected.getDescription())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Veuillez proposer une présentation différente de l'ancienne !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        projectDescriptionSelected.setDescription(description);
        projectDescriptionSelected.setLang(langCode);

        projectDescriptionRepository.save(projectDescriptionSelected);

        langCodeSelected = projectDescriptionSelected.getLang();

        selectedLangs = languageRepository.findLanguagesByProject(projectDescriptionSelected.getIdGroup());
        projectDescription = CollectionUtils.isNotEmpty(selectedLangs);
        editingHomePage = false;
        isButtonEnable = true;

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Description ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
    }

    public void deleteDescription() {

        projectDescriptionRepository.delete(projectDescriptionSelected);

        selectedLangs = languageRepository.findLanguagesByProject(projectDescriptionSelected.getIdGroup());

        if (CollectionUtils.isNotEmpty(selectedLangs)) {
            projectDescription = true;
            projectDescriptionSelected = projectDescriptionRepository
                    .findByIdGroupAndLang(projectDescriptionSelected.getIdGroup(), selectedLangs.get(0).getIso6391()).get();

            projectDescription = true;
            langCodeSelected = selectedLangs.get(0).getIso6391();
            description = projectDescriptionSelected.getDescription();
        } else {
            projectDescription = false;
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Description supprimée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public Boolean isListThesoVisible() {
        return CollectionUtils.isNotEmpty(listeThesoOfProject);
    }

    private String getLang() {
        String lang = languageBean.getIdLangue().toLowerCase();
        if (StringUtils.isEmpty(lang)) {
            lang = workLanguage;
        }
        return lang;
    }

    public String getLabel(NodeIdValue nodeIdValue) {
        return nodeIdValue.getValue () + " (" + nodeIdValue.getNbrConcepts() + " concepts)";
    }

    public void changeLangListener() {
        projectDescriptionSelected = projectDescriptionRepository.findByIdGroupAndLang(selectedTheso.getProjectIdSelected(), langCode).get();
        if (ObjectUtils.isEmpty(projectDescriptionSelected)) {
            projectDescriptionSelected = new ProjectDescription();
            projectDescriptionSelected.setLang(langCode);
            projectDescriptionSelected.setIdGroup(selectedTheso.getProjectIdSelected());
        }
        description = projectDescriptionSelected.getDescription();
    }

    public void changeProjectDescription() {
        projectDescriptionSelected = projectDescriptionRepository.findByIdGroupAndLang(selectedTheso.getProjectIdSelected(), langCodeSelected).get();
    }

    public String getDrapeauImgLocal(String codePays) {
        if (StringUtils.isEmpty(codePays)) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/noflag.png";
        }

        var pays = allLangs.stream().filter(element -> codePays.equalsIgnoreCase(element.getIso6391())).findFirst();
        if (pays.isPresent()) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/" + pays.get().getCodePays() + ".png";
        } else {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/noflag.png";
        }
    }

    public boolean isDescriptionVisible() {
        return ObjectUtils.isNotEmpty(projectDescriptionSelected)
                && StringUtils.isEmpty(projectDescriptionSelected.getDescription());
    }
}
