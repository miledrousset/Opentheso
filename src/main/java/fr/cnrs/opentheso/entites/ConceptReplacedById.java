package fr.cnrs.opentheso.entites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptReplacedById {

    private String idConcept1;
    private String idConcept2;
    private String idThesaurus;
}

