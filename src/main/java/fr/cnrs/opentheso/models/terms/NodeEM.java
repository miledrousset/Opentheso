package fr.cnrs.opentheso.models.terms;

import lombok.Data;
import java.io.Serializable;
import java.sql.Date;


@Data
public class NodeEM implements Serializable {
    
    private String lexicalValue;
    private Date created;
    private Date modified;
    private String source;
    private String status;
    private boolean hiden;
    private String lang;
    private String idUser;
    private String action;
    private String oldValue; // pour permettre la modification en évitant de copier les données qui n'ont pas changées
    private boolean oldHiden; // pour permettre la modification

}
