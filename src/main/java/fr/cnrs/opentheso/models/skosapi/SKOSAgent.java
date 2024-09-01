package fr.cnrs.opentheso.models.skosapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Miled
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SKOSAgent {
    
    private String agent;
    private int property;

}
