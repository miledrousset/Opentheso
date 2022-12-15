/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.cnrs.opentheso.bdd.helper.nodes;

import java.io.Serializable;
import java.sql.Date;

/**
 *
 * @author miled.rousset
 */
public class NodeEM implements Serializable {
    
    private String lexical_value;
    private Date created;
    private Date  modified;
    private String source;
    private String status;
    private boolean hiden;
    private String lang;
    private String idUser;
    private String action;
    
    private String oldValue; // pour permettre la modification en évitant de copier les données qui n'ont pas changées
    private boolean oldHiden; // pour permettre la modification    

    public NodeEM() {
    }

    public String getLexical_value() {
        return lexical_value;
    }

    public void setLexical_value(String lexical_value) {
        this.lexical_value = lexical_value;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isHiden() {
        return hiden;
    }

    public void setHiden(boolean hiden) {
        this.hiden = hiden;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public boolean isOldHiden() {
        return oldHiden;
    }

    public void setOldHiden(boolean oldHiden) {
        this.oldHiden = oldHiden;
    }



   
}
