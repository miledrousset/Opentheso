
package fr.cnrs.opentheso.bdd.helper.nodes;

/**
 *
 * @author miledrousset
 */
public class NodeCompareTheso {
    private String idConcept;
    private String originalPrefLabel, prefLabel, altLabel;
    private String definition;

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

    public String getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(String altLabel) {
        this.altLabel = altLabel;
    }

    public String getOriginalPrefLabel() {
        return originalPrefLabel;
    }

    public void setOriginalPrefLabel(String originalPrefLabel) {
        this.originalPrefLabel = originalPrefLabel;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
    
}
