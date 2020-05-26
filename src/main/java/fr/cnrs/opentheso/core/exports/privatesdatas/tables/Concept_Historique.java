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
public class Concept_Historique {
    String  id_concept;
    String  id_thesaurus;
    String  id_ark;
    Date modified;
    String  status;
    String  notation;
    boolean top_concept;
    String  id_group;
    int id;
    int id_user;

    public String getId_concept() {
        return id_concept;
    }

    public void setId_concept(String id_concept) {
        this.id_concept = id_concept;
    }

    public String getId_thesaurus() {
        return id_thesaurus;
    }

    public void setId_thesaurus(String id_thesaurus) {
        this.id_thesaurus = id_thesaurus;
    }

    public String getId_ark() {
        return id_ark;
    }

    public void setId_ark(String id_ark) {
        this.id_ark = id_ark;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public boolean isTop_concept() {
        return top_concept;
    }

    public void setTop_concept(boolean top_concept) {
        this.top_concept = top_concept;
    }

    public String getId_group() {
        return id_group;
    }

    public void setId_group(String id_group) {
        this.id_group = id_group;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
    
}
    
