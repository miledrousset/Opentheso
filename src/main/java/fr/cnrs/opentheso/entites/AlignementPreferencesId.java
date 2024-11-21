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
public class AlignementPreferencesId implements Serializable {

    private String idThesaurus;
    private Integer idUser;
    private String idConceptDepart;
    private Integer idAlignementSource;
}
