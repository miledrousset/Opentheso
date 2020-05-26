/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.privatesdatas.tables;

/**
 *
 * @author antonio.perez
 */
public class Preferences {
    int id_pref;
    String id_thesaurus;
    String source_Lang;
    int nb_alert_cdt;
    boolean alert_cdt;

    public int getId_pref() {
        return id_pref;
    }

    public void setId_pref(int id_pref) {
        this.id_pref = id_pref;
    }

    public String getId_thesaurus() {
        return id_thesaurus;
    }

    public void setId_thesaurus(String id_thesaurus) {
        this.id_thesaurus = id_thesaurus;
    }

    public String getSource_Lang() {
        return source_Lang;
    }

    public void setSource_Lang(String source_Lang) {
        this.source_Lang = source_Lang;
    }

    public int getNb_alert_cdt() {
        return nb_alert_cdt;
    }

    public void setNb_alert_cdt(int nb_alert_cdt) {
        this.nb_alert_cdt = nb_alert_cdt;
    }

    public boolean isAlert_cdt() {
        return alert_cdt;
    }

    public void setAlert_cdt(boolean alert_cdt) {
        this.alert_cdt = alert_cdt;
    }
    
}
