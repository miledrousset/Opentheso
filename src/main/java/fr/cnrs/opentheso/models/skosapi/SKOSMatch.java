package fr.cnrs.opentheso.models.skosapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SKOSMatch {
    
    private String value;
    private int property;
    
}
