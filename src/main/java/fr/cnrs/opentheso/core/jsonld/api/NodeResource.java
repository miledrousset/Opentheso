/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.jsonld.api;

import java.util.ArrayList;

/**
 *
 * @author miled.rousset
 */
public class NodeResource {
    
    /**
     *  "http://www.w3.org/2004/02/skos/core#narrower": [
        {
          "@id": "../concept#132"
        },
        {
          "@id": "../concept#131"
        }
      ]
     */
    private String nameSpace;
    private ArrayList<Attribute> attributes;

    public NodeResource() {
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }
    
    
}
