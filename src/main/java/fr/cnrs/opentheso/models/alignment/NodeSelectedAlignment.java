package fr.cnrs.opentheso.models.alignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeSelectedAlignment {

    private int idAlignmentSource;
    private String sourceLabel;
    private String sourceDescription;
    private boolean isSelected;
    
}
