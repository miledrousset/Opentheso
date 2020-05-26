/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.datas;

/**
 *
 * @author miled.rousset
 */
public class ConceptGroup {
    private String idgroup;
    private String idthesaurus;
    private String idARk;
    private String idHandle;    
    private String idtypecode;
    private String idparentgroup;
    private String notation;
    private String idconcept; 
    private int id;

    public ConceptGroup() {
    }

    public String getIdgroup() {
        return idgroup;
    }

    public void setIdgroup(String idgroup) {
        this.idgroup = idgroup;
    }

    public String getIdthesaurus() {
        return idthesaurus;
    }

    public void setIdthesaurus(String idthesaurus) {
        this.idthesaurus = idthesaurus;
    }

    public String getIdtypecode() {
        return idtypecode;
    }

    
    public void setIdtypecode(String idtypecode) {
        this.idtypecode = idtypecode;
    }

    public String getIdparentgroup() {
        return idparentgroup;
    }

    public void setIdparentgroup(String idparentgroup) {
        this.idparentgroup = idparentgroup;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public String getIdconcept() {
        return idconcept;
    }

    public void setIdconcept(String idconcept) {
        this.idconcept = idconcept;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdARk() {
        return idARk;
    }

    public void setIdARk(String idARk) {
        this.idARk = idARk;
    }

    public String getIdHandle() {
        return idHandle;
    }

    public void setIdHandle(String idHandle) {
        this.idHandle = idHandle;
    }
    
}
