package fr.cnrs.opentheso.models.relations;

import fr.cnrs.opentheso.models.concept.NodeUri;
import lombok.Data;


@Data
public class NodeHieraRelation {

    private NodeUri uri = new NodeUri();
    private String role = "";
}
