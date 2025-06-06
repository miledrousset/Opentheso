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

    private boolean altLabel;
    private boolean deprecated;
    private boolean group;
    private boolean facet;
    private boolean concept;

    public NodeSearchMini(String idGroup) {
        this.idConcept = idGroup;
        this.idTerm = "";
        this.altLabelValue = "";
        this.prefLabel = "";
        this.group = false;
    }

    public NodeSearchMini(String idFacet, String lexicalValue) {
        this.idConcept = idFacet;
        this.idTerm = "";
        this.altLabelValue = "";
        this.prefLabel = lexicalValue;
        this.facet = false;
    }

    public NodeSearchMini(String idConcept, String idTerm, String lexicalValue, String status) {
        this.idConcept = idConcept;
        this.idTerm = idTerm;
        this.altLabelValue = "";
        this.prefLabel = lexicalValue;
        this.facet = false;
        this.deprecated = "DEP".equalsIgnoreCase(status);
    }

}
