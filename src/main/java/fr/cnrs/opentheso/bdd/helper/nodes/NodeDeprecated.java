package fr.cnrs.opentheso.bdd.helper.nodes;

/**
 *
 * @author miledrousset
 */
public class NodeDeprecated {

    private String deprecatedId;
    private String deprecatedLabel;
    private String replacedById;
    private String replacedByLabel;
    
    public NodeDeprecated() {
    }

    public String getDeprecatedId() {
        return deprecatedId;
    }

    public void setDeprecatedId(String deprecatedId) {
        this.deprecatedId = deprecatedId;
    }

    public String getDeprecatedLabel() {
        return deprecatedLabel;
    }

    public void setDeprecatedLabel(String deprecatedLabel) {
        this.deprecatedLabel = deprecatedLabel;
    }

    public String getReplacedById() {
        return replacedById;
    }

    public void setReplacedById(String replacedById) {
        this.replacedById = replacedById;
    }

    public String getReplacedByLabel() {
        return replacedByLabel;
    }

    public void setReplacedByLabel(String replacedByLabel) {
        this.replacedByLabel = replacedByLabel;
    }
    
}