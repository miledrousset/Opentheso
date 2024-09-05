package fr.cnrs.opentheso.models.candidats;

import lombok.Data;
import java.io.Serializable;


@Data
public class NodeTabVote implements Serializable {

    private int idUser;
    private String userName;
    private String typeNote;
    private String noteValue;

}
