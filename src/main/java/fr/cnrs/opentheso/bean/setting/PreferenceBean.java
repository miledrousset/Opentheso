package fr.cnrs.opentheso.bean.setting;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.services.HomePageService;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.ThesaurusService;
import fr.cnrs.opentheso.utils.MessageUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.io.Serializable;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "preferenceBean")
public class PreferenceBean implements Serializable {

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

        switch (selectedServer) {
            case "ark":
                preferences.setUseArk(preferences.isUseArk());
                preferences.setUseArkLocal(false);
                preferences.setUseHandle(false);
                break;
            case "arklocal":
                preferences.setUseArk(false);
                preferences.setUseArkLocal(preferences.isUseArkLocal());
                preferences.setUseHandle(false);
                break;
            case "handle":
                preferences.setUseArk(false);
                preferences.setUseArkLocal(false);
                preferences.setUseHandle(preferences.isUseHandle());
        }
        preferenceService.setIdentifierFlags(selectedTheso.getCurrentIdTheso(), preferences.isUseArk(), preferences.isUseArkLocal(), preferences.isUseHandle());
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
