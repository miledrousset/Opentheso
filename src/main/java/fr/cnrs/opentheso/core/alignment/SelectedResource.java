/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment;

/**
 *
 * @author miled.rousset
 */
public class SelectedResource {
    private String idLang;
    private String gettedValue;
    private boolean selected = true;
    
    private String localValue;
    
    private boolean isEqual;

    public SelectedResource() {
    }

    public String getIdLang() {
        return idLang;
    }

    public void setIdLang(String idLang) {
        this.idLang = idLang;
    }

    public String getGettedValue() {
        return gettedValue;
    }

    public void setGettedValue(String gettedValue) {
        this.gettedValue = gettedValue;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getLocalValue() {
        return localValue;
    }

    public void setLocalValue(String localValue) {
        this.localValue = localValue;
    }

    public boolean isIsEqual() {
        return isEqual;
    }

    public void setIsEqual(boolean isEqual) {
        this.isEqual = isEqual;
    }
    
    
}
