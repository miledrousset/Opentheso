package fr.cnrs.opentheso.models.concept;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeConceptType {

    private String code;
    private String labelFr;
    private String labelEn;
    private boolean reciprocal;
    private boolean permanent;
    
}
