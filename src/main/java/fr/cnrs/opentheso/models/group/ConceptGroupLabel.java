package fr.cnrs.opentheso.models.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConceptGroupLabel {

    private String lexicalValue;
    private String created;
    private String modified;
    private String lang;
    private String idthesaurus;
    private String idgroup;

}
