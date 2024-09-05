package fr.cnrs.opentheso.models.candidats;

import lombok.Data;
import java.io.Serializable;


@Data
public class NodeCandidatValue implements Serializable {

    private String idConcept;
    private String value;
    private int nbProp;
    // etat : v=validé, a=attente,r=refusé,i=inserré 
    private String etat;

}
