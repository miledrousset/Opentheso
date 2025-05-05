package fr.cnrs.opentheso.entites;

import lombok.Setter;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PropositionId implements Serializable {

    private Integer idUser;
    private String idConcept;
    private String idThesaurus;

}
