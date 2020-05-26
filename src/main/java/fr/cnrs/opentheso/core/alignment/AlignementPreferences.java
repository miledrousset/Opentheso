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
public class AlignementPreferences {

    private String id_Theso;
    private String id_concetp_depart;
    private int id_user;
    private String id_concept_tratees;

    public String getId_Theso() {
        return id_Theso;
    }

    public void setId_Theso(String id_Theso) {
        this.id_Theso = id_Theso;
    }

    public String getId_concetp_depart() {
        return id_concetp_depart;
    }

    public void setId_concetp_depart(String id_concetp_depart) {
        this.id_concetp_depart = id_concetp_depart;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getId_concept_tratees() {
        return id_concept_tratees;
    }

    public void setId_concept_tratees(String id_concept_tratees) {
        this.id_concept_tratees = id_concept_tratees;
    }

}
