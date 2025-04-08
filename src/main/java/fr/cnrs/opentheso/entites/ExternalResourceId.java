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
public class ExternalResourceId implements Serializable {

    private String idConcept;
    private String idThesaurus;
    private String externalUri;

}
