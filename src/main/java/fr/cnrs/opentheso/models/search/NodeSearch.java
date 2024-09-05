package fr.cnrs.opentheso.models.search;

import lombok.Data;


@Data
public class NodeSearch {

    private String lexicalValue;
    private String idConcept;
    private String idTerm;
    private String idGroup;
    private String typeGroup;
    private String groupLabel;
    private String idLang;
    private String idThesaurus;
    private boolean topConcept;
    private boolean preferredLabel;

}
