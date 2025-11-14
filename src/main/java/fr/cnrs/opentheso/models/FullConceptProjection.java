package fr.cnrs.opentheso.models;

import java.util.Date;

public interface FullConceptProjection {
    String getUri();
    String getResourcetype();
    String getLocaluri();
    String getIdentifier();
    String getPermalinkid();
    String getPreflabel();
    String getAltlabel();
    String getHidenlabel();
    String getPreflabelTrad();
    String getAltlabelTrad();
    String getHiddenlabelTrad();
    String getDefinition();
    String getExample();
    String getEditorialnote();
    String getChangenote();
    String getScopenote();
    String getNote();
    String getHistorynote();
    String getNotation();
    String getNarrower();
    String getBroader();
    String getRelated();
    String getExactmatch();
    String getClosematch();
    String getBroadmatch();
    String getRelatedmatch();
    String getNarrowmatch();
    String getGpsdata();
    String getMembre();
    Date getCreated();
    Date getModified();
    String getImages();
    String getCreator();
    String getContributor();
    String getReplaces();
    String getReplacedBy();
    String getFacets();
    String getExternalresources();
    String getConcepttype();
}

