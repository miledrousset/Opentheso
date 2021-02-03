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
public class NodeUri {

    private String idConcept;
    private String idArk;
    private String idHandle;
    private String idDoi;

    public NodeUri() {
    }

    public String getIdArk() {
        return idArk;
    }

    public void setIdArk(String idArk) {
        this.idArk = idArk;
    }


    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getIdHandle() {
        return idHandle;
    }

    public void setIdHandle(String idHandle) {
        this.idHandle = idHandle;
    }

    public String getIdDoi() {
        return idDoi;
    }

    public void setIdDoi(String idDoi) {
        this.idDoi = idDoi;
    }
    
}
