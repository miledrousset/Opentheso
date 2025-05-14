package fr.cnrs.opentheso.bean.setting;

import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.services.HomePageService;
import fr.cnrs.opentheso.services.PreferenceService;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.RequiredArgsConstructor;
import org.primefaces.PrimeFaces;



@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "preferenceBean")
public class PreferenceBean implements Serializable {

    private final RoleOnThesoBean roleOnThesoBean;
    private final SelectedTheso selectedTheso;
    private final ThesaurusHelper thesaurusHelper;
    private final PreferenceService preferenceService;
    private final HomePageService homePageService;

    private String uriType;
    private Preferences nodePreference;
    private List<NodeLangTheso> languagesOfTheso;



    public void init() {
        if (selectedTheso.getCurrentIdTheso() == null) {
            return;
        }
        nodePreference = roleOnThesoBean.getNodePreference();
        // les langues du thésaurus
        languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(selectedTheso.getCurrentIdTheso(), nodePreference.getSourceLang());

        uriType = "uri";
        if (nodePreference.isOriginalUriIsHandle()) {
            uriType = "handle";
        } else if (nodePreference.isOriginalUriIsArk()) {
            uriType = "ark";
        } else if (nodePreference.isOriginalUriIsDoi()) {
            uriType = "doi";
        }
    }

    public void updateSelectedServer(String selectedServer){
       
        switch (selectedServer) {
            case "ark":
                nodePreference.setUseArkLocal(false);
                nodePreference.setUseHandle(false);
                preferenceService.setUseArk(selectedTheso.getCurrentIdTheso(), nodePreference.isUseArk());
                break;
            case "arklocal":
                nodePreference.setUseArk(false);
                nodePreference.setUseHandle(false);
                preferenceService.setUseArkLocal(selectedTheso.getCurrentIdTheso(), nodePreference.isUseArkLocal());
                break;
            case "handle":
                nodePreference.setUseArk(false);
                nodePreference.setUseArkLocal(false);
                preferenceService.setUseHandle(selectedTheso.getCurrentIdTheso(), nodePreference.isUseHandle());
                break;                
            default:
                break;
        }
    }
    
    public String getGoogleAnalytics() {
        return homePageService.getCodeGoogleAnalytics();
    }

    public void savePreference() {

        setUriType();
        
        if (!preferenceService.updateAllPreferenceUser(nodePreference)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "", "Erreur d'enregistrement des préférences !!!"));
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "", "Préférences enregistrées avec succès !!!"));
        init();

        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
    }

    private void setUriType() {
        if (uriType == null) {
            return;
        }

        nodePreference.setOriginalUriIsArk(false);
        nodePreference.setOriginalUriIsHandle(false);

        if (uriType.equalsIgnoreCase("ark")) {
            nodePreference.setOriginalUriIsArk(true);
        } else if (uriType.equalsIgnoreCase("handle")) {
            nodePreference.setOriginalUriIsHandle(true);
        }
    }

}
