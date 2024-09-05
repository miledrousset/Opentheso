package fr.cnrs.opentheso.models.terms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Term {

    private String idTerm;
    private String idConcept;
    private String lexicalValue;
    private String lang;
    private String idThesaurus;
    private Date created;
    private Date modified;
    private String source;
    private String status;
    private boolean preferred;
    private boolean hidden;
    private String idUser;
    private String codePays;    
    private int contributor;
    private int creator;

}
