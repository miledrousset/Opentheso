package fr.cnrs.opentheso.models.search;

import lombok.Data;

@Data
public class NodeSearchMini {

    private String idConcept;
    private String idTerm;
    private String prefLabel;
    private String altLabelValue;
    private String conceptType;
    private boolean isConcept;
    private boolean isAltLabel;
    private boolean isGroup;
    private boolean isFacet;
    private boolean isDeprecated;

}
