package fr.cnrs.opentheso.bdd.helper.nodes;

public class NodeFacet {
    
    private String idConceptParent;
    private int idFacet;
    private String lexicalValue;
    
    public NodeFacet() {
        
    }

    public int getIdFacet() {
        return idFacet;
    }

    public void setIdFacet(int idFacet) {
        this.idFacet = idFacet;
    }

    public String getLexicalValue() {
        return lexicalValue;
    }

    public void setLexicalValue(String lexicalValue) {
        this.lexicalValue = lexicalValue;
    }

    public String getIdConceptParent() {
        return idConceptParent;
    }

    public void setIdConceptParent(String idConceptParent) {
        this.idConceptParent = idConceptParent;
    }

    
}
