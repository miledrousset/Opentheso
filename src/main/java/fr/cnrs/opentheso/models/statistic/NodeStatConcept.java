package fr.cnrs.opentheso.models.statistic;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;


@Data
public class NodeStatConcept implements Serializable {

    private String value;
    private String idConcept;
    private Date dateCreat;
    private Date dateEdit;
    private String group;
    
}
