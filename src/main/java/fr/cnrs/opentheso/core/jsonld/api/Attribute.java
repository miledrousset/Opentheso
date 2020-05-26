/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.jsonld.api;

/**
 *
 * @author miled.rousset
 */
public class Attribute{
    
    /**
     * id = @language
     * value = en
     * 
     *  {
          "@language": "en",
          "@value": "art"
        }
     */

    private String id;
    private String value;

    public Attribute() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
}
