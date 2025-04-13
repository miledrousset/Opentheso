package fr.cnrs.opentheso.models;

import java.time.LocalDateTime;


public interface TermHistoriqueProjection {
    String getLexicalValue();
    String getNotetypecode();
    String getLang();
    String getRole();
    String getAction();
    LocalDateTime getModified();
    String getUsername();
    String getActionPerformed();
}

