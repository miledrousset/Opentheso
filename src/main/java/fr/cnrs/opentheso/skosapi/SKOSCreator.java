/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.skosapi;

/**
 *
 * @author Quincy
 * 
 */
public class SKOSCreator {
    
    String creator;
    int property;

    public SKOSCreator(String creator, int property) {
        this.creator = creator;
        this.property = property;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getProperty() {
        return property;
    }

    public void setProperty(int property) {
        this.property = property;
    }
    
    
    
}
