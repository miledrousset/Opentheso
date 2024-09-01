package fr.cnrs.opentheso.models.skosapi;

import lombok.Data;


@Data
public class SKOSStatus {

    private String idConcept;
    private String idStatus;
    private String idUser;
    private String idThesaurus;
    private String date;
    private String message;

}
