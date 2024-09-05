package fr.cnrs.opentheso.models.concept;

import lombok.Data;


@Data
public class NodeCompareTheso {

    private String idConcept;
    private String idArk;
    private String originalPrefLabel;
    private String prefLabel;
    private String altLabel;
    private String definition;
  
}
