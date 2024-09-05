package fr.cnrs.opentheso.models.relations;

import lombok.Data;

@Data
public class NodeRelation {

    private String idConcept1;
    private String relation;
    private String idConcept2;

}
