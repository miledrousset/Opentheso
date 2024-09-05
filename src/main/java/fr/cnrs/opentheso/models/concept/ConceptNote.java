
package fr.cnrs.opentheso.models.concept;

import lombok.Data;
/**
 *
 * @author miledrousset
 */

@Data
public class ConceptNote {
    
    private int idNote;
    private String idLang;
    private String label;    
    private String noteSource;
}
