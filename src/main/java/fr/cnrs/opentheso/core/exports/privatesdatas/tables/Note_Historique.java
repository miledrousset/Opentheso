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
public class Note_Historique {
    int id_note;
    String  notetypecode;
    String  id_thesaurus;
    String  id_term;
    String  id_concept;
    String  lang;
    String  lexicalvalue;
    Date modified;
    int id_user;

    public int getId_note() {
        return id_note;
    }

    public void setId_note(int id_note) {
        this.id_note = id_note;
    }

    public String getNotetypecode() {
        return notetypecode;
    }

    public void setNotetypecode(String notetypecode) {
        this.notetypecode = notetypecode;
    }

    public String getId_thesaurus() {
        return id_thesaurus;
    }

    public void setId_thesaurus(String id_thesaurus) {
        this.id_thesaurus = id_thesaurus;
    }

    public String getId_term() {
        return id_term;
    }

    public void setId_term(String id_term) {
        this.id_term = id_term;
    }

    public String getId_concept() {
        return id_concept;
    }

    public void setId_concept(String id_concept) {
        this.id_concept = id_concept;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
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

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
    
    
}
