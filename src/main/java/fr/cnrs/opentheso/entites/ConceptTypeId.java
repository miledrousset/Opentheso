package fr.cnrs.opentheso.entites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptTypeId implements Serializable {

    private String code;
    private String idThesaurus;

}
