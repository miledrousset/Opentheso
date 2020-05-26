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
public class NodePath {
    private String idConcept;
    private String title;
    private boolean isStartOfPath;

    
    public NodePath() {
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isIsStartOfPath() {
        return isStartOfPath;
    }

    public void setIsStartOfPath(boolean isStartOfPath) {
        this.isStartOfPath = isStartOfPath;
    }

    
}