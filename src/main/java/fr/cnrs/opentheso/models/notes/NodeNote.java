package fr.cnrs.opentheso.models.notes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.sql.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeNote implements Serializable {
    
    private int idNote;
    // For Concept : note, scopeNote
    // For Term : definition; editorialNote; historyNote, changeNote, example; 
    private String noteTypeCode;
    
    private String noteSource;
    
    private String idTerm;
    private String idConcept;
    private String lang;
    private String lexicalValue;
    private Date created;
    private Date modified;
    private int idUser;
    private String user;
    private boolean voted;
    private String identifier;
    
}
