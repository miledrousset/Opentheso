package fr.cnrs.opentheso.bdd.helper.nodes;

public class NodeFacet {
    
    private int idFacet;
    private String idThesaurus;
    private String lexicalValue;
    private String created;
    private String modified;
    private String lang;
    private String idConceptParent;

    public int getIdFacet() {
        return idFacet;
    }

    public void setIdFacet(int idFacet) {
        this.idFacet = idFacet;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public String getLexicalValue() {
        return lexicalValue;
    }

    public void setLexicalValue(String lexicalValue) {
        this.lexicalValue = lexicalValue;
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

    public String getIdConceptParent() {
        return idConceptParent;
    }

    public void setIdConceptParent(String idConceptParent) {
        this.idConceptParent = idConceptParent;
    }
    
    
}
