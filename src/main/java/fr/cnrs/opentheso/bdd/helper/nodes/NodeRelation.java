/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes;

/**
 *
 * @author miled.rousset
 */
public class NodeRelation {

    private String idConcept1;
    private String relation;
    private String idConcept2;
    
    public NodeRelation() {
    }

    public String getIdConcept1() {
        return idConcept1;
    }

    public void setIdConcept1(String idConcept1) {
        this.idConcept1 = idConcept1;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getIdConcept2() {
        return idConcept2;
    }

    public void setIdConcept2(String idConcept2) {
        this.idConcept2 = idConcept2;
    }
    
    
    
}
