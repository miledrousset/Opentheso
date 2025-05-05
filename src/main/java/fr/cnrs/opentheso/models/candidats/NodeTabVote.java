package fr.cnrs.opentheso.models.candidats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeTabVote implements Serializable {

    private int idUser;
    private String userName;
    private String typeNote;
    private String noteValue;

}
