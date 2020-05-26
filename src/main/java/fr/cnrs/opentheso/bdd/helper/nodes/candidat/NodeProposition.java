/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.cnrs.opentheso.bdd.helper.nodes.candidat;

import java.util.Date;

/**
 *
 * @author miled.rousset
 */
public class NodeProposition {

    private int id_user;
    private String user;
    private String note;
    private Date created;
    private Date modified;
    private String idConceptParent;
    private String labelConceptParent;
    private String idGroup;
    private String labelGroup;
    
               
    public NodeProposition() {
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getIdConceptParent() {
        return idConceptParent;
    }

    public void setIdConceptParent(String idConceptParent) {
        this.idConceptParent = idConceptParent;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getLabelConceptParent() {
        return labelConceptParent;
    }

    public void setLabelConceptParent(String labelConceptParent) {
        this.labelConceptParent = labelConceptParent;
    }

    public String getLabelGroup() {
        return labelGroup;
    }

    public void setLabelGroup(String labelGroup) {
        this.labelGroup = labelGroup;
    }
  
}
