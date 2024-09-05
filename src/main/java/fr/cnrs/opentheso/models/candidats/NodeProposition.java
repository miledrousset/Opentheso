package fr.cnrs.opentheso.models.candidats;

import lombok.Data;
import java.util.Date;


@Data
public class NodeProposition {

    private int idUser;
    private String user;
    private String note;
    private Date created;
    private Date modified;
    private String idConceptParent;
    private String labelConceptParent;
    private String idGroup;
    private String labelGroup;
  
}
