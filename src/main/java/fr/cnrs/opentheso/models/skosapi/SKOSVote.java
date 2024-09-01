package fr.cnrs.opentheso.models.skosapi;

import lombok.Data;


@Data
public class SKOSVote {

    private String idConcept;
    private String idThesaurus;
    private String idNote;
    private String valueNote;
    private int idUser;
    private String typeVote;

}
