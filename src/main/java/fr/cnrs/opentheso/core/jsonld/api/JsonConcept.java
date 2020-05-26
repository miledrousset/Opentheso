/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.jsonld.api;

import java.util.ArrayList;

/**
 * Cette classe regroupe toutes les inforamtions concernant un Concept
 * 
 * @author miled.rousset
 */
public class JsonConcept {
    
    /** La valeur d'origine en skos : 
    *    <skos:Concept rdf:about="/concept#13370">
    *    "@id": "../concept#13370",
    */ 
    private String id; // id = "../concept#13370"
    
    /**
     *  "@type": "http://www.w3.org/2004/02/skos/core#Concept"
     */
    private String nameSpace; // nameSpace = "http://www.w3.org/2004/02/skos/core#Concept"
    

    
    /**
     * "http://purl.org/dc/terms/created": "2007-02-08",
       "http://purl.org/dc/terms/modified": "2014-04-11"
       NodeElement contient les élements ci dessus
     */

    private ArrayList<NodeElement> nodeElement;
    

    
    /**
     * NodeAttribute permet de ranger les informations ci-dessous
     * "http://www.w3.org/2004/02/skos/core#prefLabel": [
        {
          "@language": "en",
          "@value": "art"
        },
        {
          "@language": "fr",
          "@value": "art"
        }
      ]
     */
    private ArrayList<NodeAttribute> nodeAttribute;

    

    
    /**
     * NodeResource permet de ranger les inforamtions de type :
     *       "http://www.w3.org/2004/02/skos/core#narrower": [
        {
          "@id": "../concept#132"
        },
        {
          "@id": "../concept#131"
        }
      ]
     */
    private ArrayList<NodeResource> nodeResource;

    public JsonConcept() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public ArrayList<NodeElement> getNodeElement() {
        return nodeElement;
    }

    public void setNodeElement(ArrayList<NodeElement> nodeElement) {
        this.nodeElement = nodeElement;
    }

    public ArrayList<NodeAttribute> getNodeAttribute() {
        return nodeAttribute;
    }

    public void setNodeAttribute(ArrayList<NodeAttribute> nodeAttribute) {
        this.nodeAttribute = nodeAttribute;
    }

    public ArrayList<NodeResource> getNodeResource() {
        return nodeResource;
    }

    public void setNodeResource(ArrayList<NodeResource> nodeResource) {
        this.nodeResource = nodeResource;
    }
}
