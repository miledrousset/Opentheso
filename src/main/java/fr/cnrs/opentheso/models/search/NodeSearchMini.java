package fr.cnrs.opentheso.models.search;

import lombok.Data;

@Data
public class NodeSearchMini {

    private String idConcept;
    private String idTerm;
    private String prefLabel;
    private String altLabelValue;
    private String conceptType;
    private boolean concept;
    private boolean altLabel;
    private boolean group;
    private boolean facet;
    private boolean deprecated;

}
