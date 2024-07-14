package fr.cnrs.opentheso.bdd.helper.nodes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NodeElement {
    private String id;
    private String value;
    private String lang;
}
