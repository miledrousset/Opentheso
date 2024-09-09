package fr.cnrs.opentheso.models.nodes;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class NodeTree {

    private String idConcept;
    private String preferredTerm;
    private String idParent;
    private List<NodeTree> childrens = new ArrayList<>();

}
