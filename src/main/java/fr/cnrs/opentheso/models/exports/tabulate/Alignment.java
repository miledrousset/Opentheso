/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.models.exports.tabulate;

/**
 *
 * @author miled.rousset
 */
public class Alignment {
    
    //exactMatch or closeMatch
    private String type;
   
    private String uri;

    public Alignment() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    
}
