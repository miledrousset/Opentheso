package fr.cnrs.opentheso.models.concept;

import lombok.Data;
import java.io.Serializable;


@Data
public class NodeAutoCompletion implements Serializable {
    
    private String idConcept;
    private String idTerm;
    private String prefLabel ="";
    private String altLabelValue;
    private String groupLexicalValue;
    private String definition;
    private String idGroup;
    private boolean altLabel;
    
    // Url pour l'imagette
    private String url;
    private String idArk;
    private String idHandle;


    @Override
    public String toString() {
        return prefLabel;
    }

}
