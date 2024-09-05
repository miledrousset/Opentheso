package fr.cnrs.opentheso.models.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConceptGroup {

    private String idgroup;
    private String idthesaurus;
    private String idARk;
    private String idHandle;    
    private String idtypecode;
    private String idparentgroup;
    private String notation;
    private String idconcept; 
    private int id;
    private Date created;
    private Date modified;
    
}
