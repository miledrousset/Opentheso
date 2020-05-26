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
public class NodeElement {
    
    
    /**
     * permet de ranger les données de ce type
     * 
     * "http://purl.org/dc/terms/created": "2007-02-08",
       "http://purl.org/dc/terms/modified": "2014-04-11"
       NodeElement contient les élements ci dessus
     */
    private String nameSpace;
    private String value;

    public NodeElement() {
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
}
