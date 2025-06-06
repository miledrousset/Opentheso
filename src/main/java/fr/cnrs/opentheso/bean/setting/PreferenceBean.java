package fr.cnrs.opentheso.bean.setting;

import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesaurusBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.services.HomePageService;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.ThesaurusService;
import fr.cnrs.opentheso.utils.MessageUtils;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.io.Serializable;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
@SessionScoped
@RequiredArgsConstructor
@Named(value = "preferenceBean")
public class PreferenceBean implements Serializable {

    private final RoleOnThesaurusBean roleOnThesoBean;
    private final SelectedTheso selectedTheso;
    private final PreferenceService preferenceService;
    private final HomePageService homePageService;
    private final ThesaurusService thesaurusService;

    private String uriType;
    private Preferences preferences;
    private List<NodeLangTheso> languagesOfThesaurus;


    public void init() {

        if (selectedTheso.getCurrentIdTheso() == null) {
            return;
        }

        preferences = preferenceService.getThesaurusPreferences(selectedTheso.getCurrentIdTheso());
        if (this.preferences == null) {
            log.error("Aucun paramètre n'est trouvé pour le thésaurus id {}", selectedTheso.getCurrentIdTheso());
            return;
        }

        languagesOfThesaurus = thesaurusService.getAllUsedLanguagesOfThesaurusNode(selectedTheso.getCurrentIdTheso(),
                preferences.getSourceLang());

        uriType = "uri";
        if (this.preferences.isOriginalUriIsHandle()) {
            uriType = "handle";
        } else if (this.preferences.isOriginalUriIsArk()) {
            uriType = "ark";
        } else if (this.preferences.isOriginalUriIsDoi()) {
            uriType = "doi";
        }
    }

    public void updateSelectedServer(String selectedServer){

        var nodePreference = roleOnThesoBean.getNodePreference();
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
        }
    }
    
    public String getGoogleAnalytics() {
        return homePageService.getCodeGoogleAnalytics();
    }

    public void savePreference() {

        if (uriType == null) {
            return;
        }

        preferences.setOriginalUriIsArk(uriType.equalsIgnoreCase("ark"));
        preferences.setOriginalUriIsHandle(uriType.equalsIgnoreCase("handle"));
        preferenceService.updateAllPreferenceUser(preferences);

        MessageUtils.showInformationMessage("Préférences enregistrées avec succès");
    }

}
