package fr.cnrs.opentheso.bean.toolbox.atelier;

import lombok.Data;

@Data
public class ConceptResultNode {
    
    private String idOrigine;
    private String prefLabelOrigine;
    private String prefLabelConcept;
    private String altLabelConcept;
    private String idConcept;
    private String uriArk;
    private String definition;
    private String termGenerique;

}
