package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
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
import java.util.ArrayList;


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

    private String text, projectIdSelected;
    private boolean projectDescription, editingHomePage;
    private ProjectDescription projectDescriptionSelected;
    private ArrayList<NodeIdValue> listeThesoOfProject;


    public void initProject(String projectIdSelected) {
        this.projectIdSelected = projectIdSelected;
        projectDescriptionSelected = projectDescriptionRepository.getProjectDescription(projectIdSelected, getLang());
        listeThesoOfProject = new UserHelper().getThesaurusOfProject(connect.getPoolConnexion(),
                Integer.parseInt(projectIdSelected), connect.getWorkLanguage());

        for (NodeIdValue element : listeThesoOfProject) {
            try {
                element.setNbrConcepts(new StatisticHelper().getNbCpt(connect.getPoolConnexion(),
                        element.getId()));
            } catch(Exception ex) {
                element.setNbrConcepts(0);
            }
        }
    }

    public void init() {
        projectDescription = true;
        editingHomePage = false;
    }

    public void setEditPage() {
        projectDescription = false;
        editingHomePage = true;
    }

    public void updateHomePage() {

        if (ObjectUtils.isEmpty(projectDescriptionSelected)) {
            projectDescriptionSelected = new ProjectDescription();
            projectDescriptionSelected.setLang(getLang());
            projectDescriptionSelected.setIdGroup(projectIdSelected);
        }

        if (text.equalsIgnoreCase(projectDescriptionSelected.getDescription())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Vérifier la description du projet");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        projectDescriptionSelected.setDescription(text);
        projectDescriptionRepository.saveProjectDescription(projectDescriptionSelected);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Texte ajouté avec succès");
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
}
