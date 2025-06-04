package fr.cnrs.opentheso.models.relations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeCustomRelation {

    private String targetConcept;
    private String targetLabel;
    private String relation;
    private String relationLabel;
    private boolean reciprocal;
    
}
