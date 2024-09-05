package fr.cnrs.opentheso.models.alignment;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;


@Data
public class NodeAlignmentImport {

    private String localId;
    private List<NodeAlignmentSmall> nodeAlignmentSmalls = new ArrayList<>();
    
}
