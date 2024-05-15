package fr.cnrs.opentheso.bean.alignment;

import lombok.Data;
import java.io.Serializable;
import java.util.Objects;


@Data
public class AlignementElement implements Serializable {

    private int codeColor;
    private int idAlignment;
    private String idConceptOrig;
    private String labelConceptOrig;
    private String targetUri;
    private String typeAlignement;
    private String labelConceptCible;
    private String thesaurus_target;
    private String conceptTarget;
    private int alignement_id_type;
    private boolean valide;
    private int idSource;
    private String definitionLocal;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlignementElement myObject = (AlignementElement) o;
        return Objects.equals(idConceptOrig, myObject.idConceptOrig) &&
                Objects.equals(labelConceptOrig, myObject.labelConceptOrig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idConceptOrig, labelConceptOrig);
    }
        
}
