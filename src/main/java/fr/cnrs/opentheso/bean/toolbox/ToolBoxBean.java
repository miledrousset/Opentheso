package fr.cnrs.opentheso.bean.toolbox;

import fr.cnrs.opentheso.bean.toolbox.atelier.AtelierThesBean;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewEditionBean;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author miledrousset
 */
@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "toolBoxBean")
public class ToolBoxBean implements Serializable {

    private final ViewEditionBean viewEditionBean;
    private final AtelierThesBean atelierThesBean;

    private boolean isEditionActive, isAtelierActive, isServiceActive, isStatisticActive;
    private String editionColor, atelierColor, serviceColor, statisticColor;

    
    public void reset() {
        isEditionActive = true;
        isStatisticActive = false;
        isAtelierActive = false;
        isServiceActive = false;
        
        // activation de la couleur pour edition
        resetColor();
        editionColor = "white";
        viewEditionBean.init();
    }

    public void setIsEditionActive(boolean isEditionActive) {
        this.isEditionActive = isEditionActive;
        isStatisticActive = false;
        isAtelierActive = false;
        isServiceActive = false;
        resetColor();
        editionColor = "white";          
    }

    public boolean isIsStatisticActive() {
        return isStatisticActive;
    }

    public void setIsStatisticActive(boolean isStatisticActive) {
        this.isStatisticActive = isStatisticActive;
        isEditionActive = false;
        isAtelierActive = false;
        isServiceActive = false;
        resetColor();
        statisticColor = "white";          
    }

    public boolean isIsAtelierActive() {
        return isAtelierActive;
    }

    public void setIsAtelierActive(boolean isAtelierActive) {
        this.isAtelierActive = isAtelierActive;
        isEditionActive = false;
        isStatisticActive = false;
        isServiceActive = false;
        resetColor();
        atelierColor = "white";
        atelierThesBean.init();
    }

    public boolean isIsServiceActive() {
        return isServiceActive;
    }

    public void setIsServiceActive(boolean isServiceActive) {
        this.isServiceActive = isServiceActive;
        isAtelierActive = false;
        isEditionActive = false;
        isStatisticActive = false;
        resetColor();
        serviceColor = "white";
        atelierThesBean.init();        
    }
    
    private void resetColor(){
        editionColor = "#B3DDC4";
        atelierColor = "#B3DDC4";
        serviceColor = "#B3DDC4";        
        statisticColor = "#B3DDC4";
    }
}
