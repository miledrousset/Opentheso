package fr.cnrs.opentheso.bdd.helper.nodes;

import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;

public class NodeFacet {
    
    private String idFacet;
    private String idThesaurus;
    private String lexicalValue;
    private String created;
    private String modified;
    private String lang;
    private String idConceptParent;

    private NodeTermTraduction nodeTraduction;
    private NodeUri nodeUri;
    
    
    public String getIdFacet() {
        return idFacet;
    }

    public void setIdFacet(String idFacet) {
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

    public NodeUri getNodeUri() {
        return nodeUri;
    }

    public void setNodeUri(NodeUri nodeUri) {
        this.nodeUri = nodeUri;
    }

    public NodeTermTraduction getNodeTraduction() {
        return nodeTraduction;
    }

    public void setNodeTraduction(NodeTermTraduction nodeTraduction) {
        this.nodeTraduction = nodeTraduction;
    }
    
    
}
