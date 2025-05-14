package fr.cnrs.opentheso.models.alignment;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class NodeAlignmentType {

    private int id;
    private String label;
    private String isocode;
    private String labelSkos;

}
