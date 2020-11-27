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
public class conceptReplacedby {
    String id_concept1;
    String id_concept2;
    String id_thesaurus;
    Date modified;
    int id_user;

    public String getId_concept1() {
        return id_concept1;
    }

    public void setId_concept1(String id_concept1) {
        this.id_concept1 = id_concept1;
    }

    public String getId_concept2() {
        return id_concept2;
    }

    public void setId_concept2(String id_concept2) {
        this.id_concept2 = id_concept2;
    }

    public String getId_thesaurus() {
        return id_thesaurus;
    }

    public void setId_thesaurus(String id_thesaurus) {
        this.id_thesaurus = id_thesaurus;
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
