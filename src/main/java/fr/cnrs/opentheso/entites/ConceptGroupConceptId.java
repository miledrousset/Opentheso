package fr.cnrs.opentheso.entites;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ConceptGroupConceptId implements Serializable {

    private String idGroup;
    private String idThesaurus;
    private String idConcept;

}
