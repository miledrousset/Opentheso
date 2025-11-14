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
public class PermutedId implements Serializable {

    private int ord;
    private String idConcept;
    private String idGroup;
    private String idThesaurus;
    private String idLang;
    private String lexicalValue;
    private boolean isPreferredTerm;

}
