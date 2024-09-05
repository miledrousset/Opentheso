package fr.cnrs.opentheso.models.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeStatus {

    private String idConcept;
    private String idStatus;
    private String idUser;
    private String idThesaurus;
    private String date;
    private String message;

}
