/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment;

/**
 *
 * @author antonio.perez
 */
public class GpsPreferences {
    private String id_thesaurus;
    private int id_user;
    private boolean gps_integrertraduction;
    private boolean gps_reemplacertraduction ;
    private boolean gps_alignementautomatique;
    private int id_alignement_source;

    public String getId_thesaurus() {
        return id_thesaurus;
    }

    public void setId_thesaurus(String id_thesaurus) {
        this.id_thesaurus = id_thesaurus;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public boolean isGps_integrertraduction() {
        return gps_integrertraduction;
    }

    public void setGps_integrertraduction(boolean gps_integrertraduction) {
        this.gps_integrertraduction = gps_integrertraduction;
    }

    public boolean isGps_reemplacertraduction() {
        return gps_reemplacertraduction;
    }

    public void setGps_reemplacertraduction(boolean gps_reemplacertraduction) {
        this.gps_reemplacertraduction = gps_reemplacertraduction;
    }

    public boolean isGps_alignementautomatique() {
        return gps_alignementautomatique;
    }

    public void setGps_alignementautomatique(boolean gps_alignementautomatique) {
        this.gps_alignementautomatique = gps_alignementautomatique;
    }

    public int getId_alignement_source() {
        return id_alignement_source;
    }

    public void setId_alignement_source(int id_alignement_source) {
        this.id_alignement_source = id_alignement_source;
    }



    
}
