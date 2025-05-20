package fr.cnrs.opentheso.models;

import java.util.Date;

public interface CandidateProjection {
    String getIdConcept();
    Date getCreated();
    Date getModified();
    Integer getIdUser();
    Integer getIdUserAdmin();
    String getMessage();
}
