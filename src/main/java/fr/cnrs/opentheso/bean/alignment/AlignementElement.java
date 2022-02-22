package fr.cnrs.opentheso.bean.alignment;

public class AlignementElement {

    private int idAlignment;
    private String idConceptOrig;
    private String labelConceptOrig;
    private String targetUri;
    private String typeAlignement;
    private String labelConceptCible;
    private String thesaurus_target;
    private String conceptTarget;
    private int alignement_id_type;
    private int idSource;

    
    public String getConceptTarget() {
        return conceptTarget;
    }

    public void setConceptTarget(String conceptTarget) {
        this.conceptTarget = conceptTarget;
    }

    public String getThesaurus_target() {
        return thesaurus_target;
    }

    public void setThesaurus_target(String thesaurus_target) {
        this.thesaurus_target = thesaurus_target;
    }

    public int getAlignement_id_type() {
        return alignement_id_type;
    }

    public void setAlignement_id_type(int alignement_id_type) {
        this.alignement_id_type = alignement_id_type;
    }

    public int getIdSource() {
        return idSource;
    }

    public void setIdSource(int idSource) {
        this.idSource = idSource;
    }

    public int getIdAlignment() {
        return idAlignment;
    }

    public void setIdAlignment(int idAlignment) {
        this.idAlignment = idAlignment;
    }

    public String getIdConceptOrig() {
        return idConceptOrig;
    }

    public void setIdConceptOrig(String idConceptOrig) {
        this.idConceptOrig = idConceptOrig;
    }

    public String getLabelConceptOrig() {
        return labelConceptOrig;
    }

    public void setLabelConceptOrig(String labelConceptOrig) {
        this.labelConceptOrig = labelConceptOrig;
    }

    public String getTypeAlignement() {
        return typeAlignement;
    }

    public void setTypeAlignement(String typeAlignement) {
        this.typeAlignement = typeAlignement;
    }

    public String getLabelConceptCible() {
        return labelConceptCible;
    }

    public void setLabelConceptCible(String labelConceptCible) {
        this.labelConceptCible = labelConceptCible;
    }

    public String getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }
        
}
