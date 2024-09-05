package fr.cnrs.opentheso.models.concept;

import lombok.Data;


@Data
public class NodeConceptType {

    private String code;
    private String labelFr;
    private String labelEn;
    private boolean reciprocal;
    private boolean permanent;
    
}
