/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.skosapi;

/**
 *
 * @author Quincy
 */
public class SKOSMatch {
    
    String value;
    int property;

    public SKOSMatch(String value, int property) {
        this.value = value;
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public int getProperty() {
        return property;
    }
    
    
    
}
