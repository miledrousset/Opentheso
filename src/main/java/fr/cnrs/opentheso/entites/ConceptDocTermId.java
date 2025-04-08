package fr.cnrs.opentheso.entites;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ConceptDocTermId implements Serializable {

    private String idConcept;
    private String idThesaurus;
    private String name;
    private String value;

}
