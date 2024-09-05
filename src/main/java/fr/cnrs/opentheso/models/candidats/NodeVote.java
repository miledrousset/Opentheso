package fr.cnrs.opentheso.models.candidats;

import lombok.Data;
import java.io.Serializable;


@Data
public class NodeVote implements Serializable {

    private int idUser;
    private String idNote;

}
