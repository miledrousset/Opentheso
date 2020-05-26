/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.privatesdatas.tables;

import java.util.Date;

/**
 *
 * @author antonio.perez
 */
public class Concept_Group_Historique {
  String idgroup;
  String id_ark;
  String idthesaurus;
  String idtypecode;
  String idparentgroup;
  String notation;
  String idconcept;
  int id; 
  Date modified;
  int id_user;

    public String getIdgroup() {
        return idgroup;
    }

    public void setIdgroup(String idgroup) {
        this.idgroup = idgroup;
    }

    public String getId_ark() {
        return id_ark;
    }

    public void setId_ark(String id_ark) {
        this.id_ark = id_ark;
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

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
  
}
