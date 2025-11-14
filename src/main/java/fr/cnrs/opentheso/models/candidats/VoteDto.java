package fr.cnrs.opentheso.models.candidats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteDto {

    private String idConcept;
    private String idThesaurus;
    private String idNote;
    private int idUser;
    private String typeVote;

}
