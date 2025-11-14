package fr.cnrs.opentheso.entites;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HierarchicalRelationshipId implements Serializable {

    private String idConcept1;
    private String idConcept2;
    private String idThesaurus;
    private String role;

}
