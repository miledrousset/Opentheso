package fr.cnrs.opentheso.models.concept;

import lombok.Data;

@Data
public class ConceptCustomRelation {

    private String targetConcept;
    private String targetLabel;
    private String relation;
    private String relationLabel;
    private boolean reciprocal;
    
}
