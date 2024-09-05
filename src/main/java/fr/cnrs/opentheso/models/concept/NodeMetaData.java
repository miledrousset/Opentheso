package fr.cnrs.opentheso.models.concept;

import fr.cnrs.opentheso.models.nodes.DcElement;
import lombok.Data;

import java.util.List;


@Data
public class NodeMetaData {

    private String title;
    private String source;
    private String creator;
    private List<DcElement> dcElementsList;

}
