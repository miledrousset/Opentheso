package fr.cnrs.opentheso.bean.setting;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "settingAndToolsBean")
public class SettingAndToolsBean implements Serializable {

    private final PreferenceBean preferenceBean;

    private boolean isPreferenceActive, isIdentifierActive, isCorpusActive, isMaintenanceActive;
    private String preferenceColor, identifierColor, corpusColor, maintenanceColor;


    public void reset() {
        isPreferenceActive = true;
        isMaintenanceActive = false;
        isIdentifierActive = false;
        isCorpusActive = false;
        
        // activation de la couleur pour edition
        resetColor();
        preferenceColor = "white";
        preferenceBean.init();
    }

    public void setIsPreferenceActive(boolean isPreferenceActive) {
        this.isPreferenceActive = isPreferenceActive;
        isMaintenanceActive = false;
        isIdentifierActive = false;
        isCorpusActive = false;
        resetColor();
        preferenceColor = "white";         
    }

    public void setIsMaintenanceActive(boolean isMaintenanceActive) {
        this.isMaintenanceActive = isMaintenanceActive;
        isPreferenceActive = false;
        isIdentifierActive = false;  
        isCorpusActive = false;
        resetColor();
        maintenanceColor = "white";   
    }

    public void setIsIdentifierActive(boolean isIdentifierActive) {
        this.isIdentifierActive = isIdentifierActive;
        isPreferenceActive = false;
        isMaintenanceActive = false;
        isCorpusActive = false;
        resetColor();
        identifierColor = "white";        
    }

    public void setIsCorpusActive(boolean isCorpusActive) {
        this.isCorpusActive = isCorpusActive;
        isPreferenceActive = false;
        isMaintenanceActive = false;
        isIdentifierActive = false;
        resetColor();
        corpusColor = "white";          
    }

    private void resetColor(){
        preferenceColor = "#B3DDC4";
        maintenanceColor = "#B3DDC4";
        identifierColor = "#B3DDC4";   
        corpusColor = "#B3DDC4";
    }
}
