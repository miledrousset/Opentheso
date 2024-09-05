package fr.cnrs.opentheso.models.relations;

import lombok.Data;


@Data
public class NodeCustomRelation {

    private String targetConcept;
    private String targetLabel;
    private String relation;
    private String relationLabel;
    private boolean reciprocal;
    
}
