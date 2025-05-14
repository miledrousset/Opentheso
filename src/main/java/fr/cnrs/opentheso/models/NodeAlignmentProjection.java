package fr.cnrs.opentheso.models;

import java.sql.Date;


public interface NodeAlignmentProjection {
    int getId();
    Date getCreated();
    Date getModified();
    int getAuthor();
    String getThesaurus_target();
    String getConcept_target();
    String getUri_target();
    int getAlignement_id_type();
    String getInternal_id_thesaurus();
    String getInternal_id_concept();
    int getId_alignement_source();
    String getLabel();
    String getLabel_skos();
    boolean getUrl_available();
}
