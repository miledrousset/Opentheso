/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.skosapi;

/**
 *
 * @author Miled
 * 
 */
public class SKOSAgent {
    
    String agent;
    int property;

    public SKOSAgent(String agent, int property) {
        this.agent = agent;
        this.property = property;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public int getProperty() {
        return property;
    }

    public void setProperty(int property) {
        this.property = property;
    }
    
    
    
}
