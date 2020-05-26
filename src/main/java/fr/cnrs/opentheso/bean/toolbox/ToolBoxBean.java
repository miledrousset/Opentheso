/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

    @Inject private ViewEditionBean viewEditionBean;
    
    private boolean isEditionActive;
    private boolean isStatisticActive;

    
    private String editionColor;
    private String statisticColor;
    
    public ToolBoxBean() {
    }

    public void reset() {
        isEditionActive = true;
        isStatisticActive = false;
        
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
        resetColor();
        editionColor = "white";          
    }

    public boolean isIsStatisticActive() {
        return isStatisticActive;
    }

    public void setIsStatisticActive(boolean isStatisticActive) {
        this.isStatisticActive = isStatisticActive;
        isEditionActive = false;
        resetColor();
        statisticColor = "white";          
    }

   
    
    private void resetColor(){
        editionColor = "#B3DDC4";
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
  
    
}
