package fr.cnrs.opentheso.models.candidats;

import lombok.Data;

@Data
public class VoteDto {

    private String idConcept;
    private String idThesaurus;
    private String idNote;
    private int idUser;
    private String typeVote;

}
