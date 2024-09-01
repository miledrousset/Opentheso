package fr.cnrs.opentheso.bdd.datas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConceptGroupLabel {

    private String lexicalvalue;
    private String created;
    private String modified;
    private String lang;
    private String idthesaurus;
    private String idgroup;

}
