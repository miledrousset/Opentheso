package fr.cnrs.opentheso.bdd.helper.nodes;

import lombok.Data;


@Data
public class NodeSelectedAlignment {

    private int idAlignmentSource;
    private String sourceLabel;
    private String sourceDescription;
    private boolean isSelected;
    
}
