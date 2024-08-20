package fr.cnrs.opentheso.bean.toolbox;

import fr.cnrs.opentheso.bean.toolbox.atelier.AtelierThesBean;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewEditionBean;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 *
 * @author miledrousset
 */
@Getter
@Named(value = "toolBoxBean")
@SessionScoped
public class ToolBoxBean implements Serializable {
    @Autowired @Lazy private ViewEditionBean viewEditionBean;
    @Autowired @Lazy private AtelierThesBean atelierThesBean;

    private boolean isEditionActive;
    private boolean isAtelierActive;
    private boolean isServiceActive;    
    private boolean isStatisticActive;

    private String editionColor;
    private String atelierColor;
    private String serviceColor;
    private String statisticColor;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        editionColor = null;
        atelierColor = null;
        serviceColor = null;
        statisticColor = null;
    }      
    
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

    public void setEditionColor(String editionColor) {
        this.editionColor = editionColor;
    }

    public void setStatisticColor(String statisticColor) {
        this.statisticColor = statisticColor;
    }

    public void setAtelierColor(String atelierColor) {
        this.atelierColor = atelierColor;
    }

    public void setServiceColor(String serviceColor) {
        this.serviceColor = serviceColor;
    }
    
    
    
}
