package fr.cnrs.opentheso.models;

import java.util.Date;


public interface SkosConceptProjection {
    String getUri();
    String getType();
    String getLocal_uri();
    String getIdentifier();
    String getArk_id();
    String getPrefLab();
    String getAltLab();
    String getAltLab_hiden();
    String getDefinition();
    String getExample();
    String getEditorialnote();
    String getChangenote();
    String getSecopenote();
    String getNote();
    String getHistorynote();
    String getNotation();
    String getNarrower();
    String getBroader();
    String getRelated();
    String getExactMatch();
    String getCloseMatch();
    String getBroadMatch();
    String getRelatedMatch();
    String getNarrowMatch();
    String getGpsData();
    String getMembre();
    Date getCreated();
    Date getModified();
    String getImg();
    String getCreator();
    String getContributor();
    String getReplaces();
    String getReplaced_by();
    String getFacets();
    String getExternalResources();
}