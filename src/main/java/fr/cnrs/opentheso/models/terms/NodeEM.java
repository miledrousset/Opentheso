package fr.cnrs.opentheso.models.terms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    public NodeEM(String lexicalValue, Date created, Date modified, String source, String status, boolean hiden, String lang) {
        this.lexicalValue = lexicalValue;
        this.created = created;
        this.modified = modified;
        this.source = source;
        this.status = status;
        this.hiden = hiden;
        this.lang = lang;
    }
}
