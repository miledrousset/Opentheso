package fr.cnrs.opentheso.models.concept;

import fr.cnrs.opentheso.models.nodes.DcElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeMetaData {

    private String title;
    private String source;
    private String creator;
    private List<DcElement> dcElementsList;

}
