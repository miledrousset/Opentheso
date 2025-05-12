package fr.cnrs.opentheso.entites;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Date;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class NonPreferredTermHistoriqueId implements Serializable {

    private String idTerm;
    private String lexicalValue;
    private String lang;
    private String idThesaurus;
    private Date modified;

}
