package fr.cnrs.opentheso.models.notes;

import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;

import java.io.Serializable;
import java.sql.Date;


@Data
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

    public String getLexicalValue() {
        // Si le texte est HTML-encodé, on le "déséchappe"
        return StringEscapeUtils.unescapeHtml4(lexicalValue);
    }

    public void setLexicalValue(String texte) {
        this.lexicalValue = texte;
    }
}
