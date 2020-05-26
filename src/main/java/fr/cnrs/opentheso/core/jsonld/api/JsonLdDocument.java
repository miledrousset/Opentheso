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
public class JsonLdDocument {
   // private String node
    
    private ArrayList<JsonConcept> jsonConcept;

    public JsonLdDocument() {
    }

    public ArrayList<JsonConcept> getJsonConcept() {
        return jsonConcept;
    }

    public void setJsonConcept(ArrayList<JsonConcept> jsonConcept) {
        this.jsonConcept = jsonConcept;
    }
    
}
