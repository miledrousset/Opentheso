package fr.cnrs.opentheso.bean.toolbox;

import fr.cnrs.opentheso.bean.toolbox.edition.ViewEditionBean;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "toolBoxBean")
@SessionScoped
public class ToolBoxBean implements Serializable {

    @Inject 
    private ViewEditionBean viewEditionBean;
    
    private boolean isEditionActive;
    private boolean isAtelierActive;
    private boolean isStatisticActive;

    private String editionColor;
    private String atelierColor;
    private String statisticColor;

    public void reset() {
        isEditionActive = true;
        isStatisticActive = false;
        isAtelierActive = false;
        
        // activation de la couleur pour edition
        resetColor();
        editionColor = "white";
        viewEditionBean.init();
      
    }
    
    public boolean isIsEditionActive() {
        return isEditionActive;
    }

    public void setIsEditionActive(boolean isEditionActive) {
        this.isEditionActive = isEditionActive;
        isStatisticActive = false;
        isAtelierActive = false;
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
        resetColor();
        atelierColor = "white";    
    }
    
    private void resetColor(){
        editionColor = "#B3DDC4";
        atelierColor = "#B3DDC4";
        statisticColor = "#B3DDC4";
    }

    public String getEditionColor() {
        return editionColor;
    }

    public void setEditionColor(String editionColor) {
        this.editionColor = editionColor;
    }

    public String getStatisticColor() {
        return statisticColor;
    }

    public void setStatisticColor(String statisticColor) {
        this.statisticColor = statisticColor;
    }

    public String getAtelierColor() {
        return atelierColor;
    }

    public void setAtelierColor(String atelierColor) {
        this.atelierColor = atelierColor;
    }
    
}
