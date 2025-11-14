package fr.cnrs.opentheso.models;

import java.util.Date;

public interface CandidateSearchProjection {

    String getLang();
    String getIdTerm();
    String getLexicalValue();
    String getIdConcept();
    String getIdThesaurus();
    Date getCreated();
    String getUsername();
    Integer getContributor();
}
