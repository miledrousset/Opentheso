package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.bdd.datas.Languages_iso639;
import fr.cnrs.opentheso.bdd.helper.LanguageHelper;
import fr.cnrs.opentheso.bdd.helper.StatisticHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.entites.ProjectDescription;
import fr.cnrs.opentheso.repositories.GpsRepository;
import fr.cnrs.opentheso.repositories.ProjectDescriptionRepository;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;


@Data
@SessionScoped
@Named(value = "projectBean")
public class ProjectBean implements Serializable {

    @Inject private Connect connect;
    @Inject private ConceptView conceptView;
    @Inject private SelectedTheso selectedTheso;
    @Inject private GpsRepository gpsRepository;
    @Inject private LanguageBean languageBean;
    @Inject private ProjectDescriptionRepository projectDescriptionRepository;

    private String description, langCode, langCodeSelected;
    private boolean projectDescription, editingHomePage, isButtonEnable;
    private ProjectDescription projectDescriptionSelected;
    private List<NodeIdValue> listeThesoOfProject;
    private List<Languages_iso639> allLangs;


    public void initProject(String projectIdSelected, boolean isPrivate) {
        if(StringUtils.isEmpty(projectIdSelected)) return;
        projectDescriptionSelected = projectDescriptionRepository.getProjectDescription(projectIdSelected, getLang());
        if (ObjectUtils.isEmpty(projectDescriptionSelected)) {
            projectDescriptionSelected = new ProjectDescription();
            projectDescriptionSelected.setLang(getLang());
            projectDescriptionSelected.setIdGroup(projectIdSelected);
        }
        description = projectDescriptionSelected.getDescription();
        listeThesoOfProject = new UserHelper().getThesaurusOfProject(connect.getPoolConnexion(),
                Integer.parseInt(projectIdSelected), connect.getWorkLanguage(), isPrivate);

        for (NodeIdValue element : listeThesoOfProject) {
            try {
                element.setNbrConcepts(new StatisticHelper().getNbCpt(connect.getPoolConnexion(),
                        element.getId()));
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
        projectDescription = true;
        editingHomePage = false;
        isButtonEnable = true;
    }

    public void setEditPage() {
        if (CollectionUtils.isEmpty(allLangs)) {
            allLangs = new LanguageHelper().getAllLanguages(connect.getPoolConnexion());
        }
        if (StringUtils.isEmpty(langCode)) {
            langCode = getLang();
        }
        projectDescription = false;
        editingHomePage = true;
        isButtonEnable = false;
    }

    public void updateHomePage() {

        if (description.equalsIgnoreCase(projectDescriptionSelected.getDescription())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Veuillez proposer une présentation différente de l'ancienne !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        projectDescriptionSelected.setDescription(description);
        projectDescriptionRepository.saveProjectDescription(projectDescriptionSelected);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Description ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        init();

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
    }

    public Boolean isListThesoVisible() {
        return CollectionUtils.isNotEmpty(listeThesoOfProject);
    }

    private String getLang() {
        String lang = languageBean.getIdLangue().toLowerCase();
        if (StringUtils.isEmpty(lang)) {
            lang = connect.getWorkLanguage();
        }
        return lang;
    }

    public String getLabel(NodeIdValue nodeIdValue) {
        return nodeIdValue.getValue () + " (" + nodeIdValue.getNbrConcepts() + " concepts)";
    }

    public void changeLangListener() {
        projectDescriptionSelected = projectDescriptionRepository.getProjectDescription(selectedTheso.getProjectIdSelected(), langCode);
        if (ObjectUtils.isEmpty(projectDescriptionSelected)) {
            projectDescriptionSelected = new ProjectDescription();
            projectDescriptionSelected.setLang(langCode);
            projectDescriptionSelected.setIdGroup(selectedTheso.getProjectIdSelected());
        }
        description = projectDescriptionSelected.getDescription();
    }

    public void changeProjectDescription() {
        projectDescriptionSelected = projectDescriptionRepository.getProjectDescription(selectedTheso.getProjectIdSelected(), langCodeSelected);
    }

    public String getDrapeauImgLocal(String codePays) {
        if (StringUtils.isEmpty(codePays)) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/noflag.png";
        }

        var pays = allLangs.stream().filter(element -> codePays.equalsIgnoreCase(element.getId_iso639_1())).findFirst();
        if (pays.isPresent()) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/" + pays.get().getCodePays() + ".png";
        } else {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/noflag.png";
        }
    }
}
