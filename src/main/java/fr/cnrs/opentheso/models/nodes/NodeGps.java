package fr.cnrs.opentheso.models.nodes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeGps {

    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private int position;
    
}
