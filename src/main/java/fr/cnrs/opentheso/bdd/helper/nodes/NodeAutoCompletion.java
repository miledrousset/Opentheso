package fr.cnrs.opentheso.bdd.helper.nodes;

import java.io.Serializable;

public class NodeAutoCompletion implements Serializable {
    
    private String idConcept;
    private String idTerm;
    private String prefLabel ="";
    private String altLabel;    
    private String groupLexicalValue;
    private String definition;
    private String idGroup;
    private boolean isAltLabel;
    
    // Url pour l'imagette
    private String url;
    
    private String idArk;
    private String idHandle;
    

    public NodeAutoCompletion() {
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String getGroupLexicalValue() {
        return groupLexicalValue;
    }

    public void setGroupLexicalValue(String GroupLexicalValue) {
        this.groupLexicalValue = GroupLexicalValue;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public boolean isIsAltLabel() {
        return isAltLabel;
    }

    public void setIsAltLabel(boolean isAltLabel) {
        this.isAltLabel = isAltLabel;
    }

    public String getIdArk() {
        return idArk;
    }

    public void setIdArk(String idArk) {
        this.idArk = idArk;
    }

    public String getIdHandle() {
        return idHandle;
    }

    public void setIdHandle(String idHandle) {
        this.idHandle = idHandle;
    }

    public String getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(String altLabel) {
        this.altLabel = altLabel;
    }

    public String getIdTerm() {
        return idTerm;
    }

    public void setIdTerm(String idTerm) {
        this.idTerm = idTerm;
    }

    
    
    @Override
    public String toString() {
        return prefLabel;
    }


}
