package fr.cnrs.opentheso.models.nodes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeTree {

    private String idConcept;
    private String preferredTerm;
    private String idParent;
    private List<NodeTree> childrens = new ArrayList<>();


    public NodeTree(String idConcept, String preferredTerm) {
        this.idConcept = idConcept;
        this.preferredTerm = preferredTerm;
    }
}
