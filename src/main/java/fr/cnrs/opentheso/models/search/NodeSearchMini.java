package fr.cnrs.opentheso.models.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
