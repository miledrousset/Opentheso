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
public class Concept_Group_Label_Historique {
    int idgrouplabel;
    String lexicalvalue;
    Date modified;
    String lang;
    String idthesaurus;
    String idgroup;
    int id_user;

    public int getIdgrouplabel() {
        return idgrouplabel;
    }

    public void setIdgrouplabel(int idgrouplabel) {
        this.idgrouplabel = idgrouplabel;
    }

    public String getLexicalvalue() {
        return lexicalvalue;
    }

    public void setLexicalvalue(String lexicalvalue) {
        this.lexicalvalue = lexicalvalue;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getIdthesaurus() {
        return idthesaurus;
    }

    public void setIdthesaurus(String idthesaurus) {
        this.idthesaurus = idthesaurus;
    }

    public String getIdgroup() {
        return idgroup;
    }

    public void setIdgroup(String idfroup) {
        this.idgroup = idfroup;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
    
}
