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
public class Non_Preferred_Term {
    String  id_term;
    String  lexical_value;
    String  lang;
    String  id_thesaurus;
    Date created;
    Date modified;
    String  source;
    String  status;
    boolean hiden;

    public String getId_term() {
        return id_term;
    }

    public void setId_term(String id_term) {
        this.id_term = id_term;
    }

    public String getLexical_value() {
        return lexical_value;
    }

    public void setLexical_value(String lexical_value) {
        this.lexical_value = lexical_value;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getId_thesaurus() {
        return id_thesaurus;
    }

    public void setId_thesaurus(String id_thesaurus) {
        this.id_thesaurus = id_thesaurus;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isHiden() {
        return hiden;
    }

    public void setHiden(boolean hiden) {
        this.hiden = hiden;
    }
    
}
