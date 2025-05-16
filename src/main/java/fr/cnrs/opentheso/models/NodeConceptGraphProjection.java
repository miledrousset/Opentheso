package fr.cnrs.opentheso.models;


public interface NodeConceptGraphProjection {
    String getIdconcept();
    String getIdconcept2();
    String getLocalUri();
    String getStatus();
    String getLabel();
    String getAltlabel();
    String getDefinition();
    Boolean getHavechildren();
    String getImage();
}
