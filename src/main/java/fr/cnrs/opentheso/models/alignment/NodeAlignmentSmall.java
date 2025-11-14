package fr.cnrs.opentheso.models.alignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeAlignmentSmall {

    private String uri_target;
    private int alignement_id_type;
    private String source;    

}
