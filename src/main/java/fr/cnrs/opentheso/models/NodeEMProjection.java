package fr.cnrs.opentheso.models;

import java.util.Date;

public interface NodeEMProjection {

    String getLexicalValue();
    Date getCreated();
    Date getModified();
    String getSource();
    String getStatus();
    boolean isHiden();
}
