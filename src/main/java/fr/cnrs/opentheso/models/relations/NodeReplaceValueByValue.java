package fr.cnrs.opentheso.models.relations;

import lombok.Data;


@Data
public class NodeReplaceValueByValue {

    private String idConcept;
    private int SKOSProperty;
    private String oldValue;
    private String newValue;
    private String idLang;
}
