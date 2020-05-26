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
public class ConceptGroupLabel {

    private String lexicalvalue;
    private String created;
    private String modified;
    private String lang;
    private String idthesaurus;
    private String idgroup;

    public ConceptGroupLabel() {
    }

    public String getLexicalvalue() {
        return lexicalvalue;
    }

    public void setLexicalvalue(String lexicalvalue) {
        this.lexicalvalue = lexicalvalue;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
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

    public void setIdgroup(String idgroup) {
        this.idgroup = idgroup;
    }

}
