package fr.cnrs.opentheso.bdd.helper.nodes.term;

import java.io.Serializable;

public class NodeTermTraduction implements Serializable {

    private String lang;
    private String nomLang;
    private String lexicalValue;
    private String codePays;
    
    public NodeTermTraduction() {
        
    }

    public NodeTermTraduction(String lang, String lexicalValue) {
        this.lang = lang;
        this.lexicalValue = lexicalValue;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLexicalValue() {
        return lexicalValue;
    }

    public void setLexicalValue(String lexicalValue) {
        this.lexicalValue = lexicalValue;
    }

    public String getNomLang() {
        return nomLang;
    }

    public void setNomLang(String nomLang) {
        this.nomLang = nomLang;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

}
