/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.cnrs.opentheso.bdd.helper.nodes.notes;

import java.io.Serializable;
import java.sql.Date;

/**
 *
 * @author miled.rousset
 */
public class NodeNote implements Serializable {
    
    private int id_note;
    
    // For Concept : note, scopeNote
    // For Term : definition; editorialNote; historyNote, changeNote, example; 
    private String notetypecode;
    
    private String id_term;
    private String id_concept;
    private String lang;
    private String lexicalvalue;
    private Date created;
    private Date modified;
    private int idUser;
    private String user;
    private boolean voted;

    public NodeNote() {
    }

    public int getId_note() {
        return id_note;
    }

    public void setId_note(int id_note) {
        this.id_note = id_note;
    }

    public String getNotetypecode() {
        return notetypecode;
    }

    public void setNotetypecode(String notetypecode) {
        this.notetypecode = notetypecode;
    }

    public String getId_term() {
        return id_term;
    }

    public void setId_term(String id_term) {
        this.id_term = id_term;
    }

    public String getId_concept() {
        return id_concept;
    }

    public void setId_concept(String id_concept) {
        this.id_concept = id_concept;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLexicalvalue() {
        return lexicalvalue;
    }

    public void setLexicalvalue(String lexicalvalue) {
        this.lexicalvalue = lexicalvalue;
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

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }
}
