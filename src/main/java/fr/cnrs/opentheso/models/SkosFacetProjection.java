package fr.cnrs.opentheso.models;

import java.util.Date;

public interface SkosFacetProjection {

    String getId_facet();
    String getLexicalvalue();
    String getLang();
    String getUri_value();
    String getDefinition();
    String getExample();
    String getEditorialnote();
    String getChangenote();
    String getSecopenote();
    String getNote();
    String getHistorynote();
    Date getCreated();
    Date getModified();
    String getId_concept_parent();
}