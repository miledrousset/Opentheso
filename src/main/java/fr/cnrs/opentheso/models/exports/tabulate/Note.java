/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.models.exports.tabulate;

/**
 *
 * @author miled.rousset
 */
public class Note {
    
    //definition;editorialNote;historyNote;scopeNote
    private String note;
    private String lang;

    public Note() {
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
    
}
