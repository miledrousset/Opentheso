package fr.cnrs.opentheso.bdd.helper.nodes;

/**
 *
 * @author miledrousset
 */
public class NodeCustomRelation {
    private String targetConcept;
    private String targetLabel;
    private String relation;
    
    private String relationLabel;
    
    private boolean onway;

    public String getTargetConcept() {
        return targetConcept;
    }

    public void setTargetConcept(String targetConcept) {
        this.targetConcept = targetConcept;
    }

    public String getTargetLabel() {
        return targetLabel;
    }

    public void setTargetLabel(String targetLabel) {
        this.targetLabel = targetLabel;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getRelationLabel() {
        return relationLabel;
    }

    public void setRelationLabel(String relationLabel) {
        this.relationLabel = relationLabel;
    }


    public boolean isOnway() {
        return onway;
    }

    public void setOnway(boolean onway) {
        this.onway = onway;
    }
    
}
