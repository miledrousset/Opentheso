package fr.cnrs.opentheso.models.relations;

import fr.cnrs.opentheso.models.concept.NodeUri;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeHieraRelation {

    private NodeUri uri = new NodeUri();
    private String role = "";
}
