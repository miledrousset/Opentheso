package fr.cnrs.opentheso.models;

import java.util.Date;


public interface ConceptGroupProjection {
    String getIdConcept();
    String getLexicalValue();
    Date getCreated();
    Date getModified();
    String getUsername();
}
