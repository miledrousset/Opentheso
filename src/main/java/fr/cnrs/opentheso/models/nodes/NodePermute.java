package fr.cnrs.opentheso.models.nodes;

import lombok.Data;
import java.io.Serializable;


@Data
public class NodePermute implements Serializable {

    private String firstColumn;
    private String searchedValue;
    private String lastColumn;
    private String idThesaurus;
    private String idConcept;
    private String idGroup;
    private String idLang;
    private int indexOfValue;
    private boolean isPreferredTerm;

}
