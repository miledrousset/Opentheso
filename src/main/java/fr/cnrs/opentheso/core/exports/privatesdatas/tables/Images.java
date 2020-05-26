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
public class Images {
    String id_concept;
    String id_thesaururs;
    String image_name;
    String image_copyright;
    int id_user;

    public String getId_concept() {
        return id_concept;
    }

    public void setId_concept(String id_concept) {
        this.id_concept = id_concept;
    }

    public String getId_thesaururs() {
        return id_thesaururs;
    }

    public void setId_thesaururs(String id_thesaururs) {
        this.id_thesaururs = id_thesaururs;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getImage_copyright() {
        return image_copyright;
    }

    public void setImage_copyright(String image_copyright) {
        this.image_copyright = image_copyright;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
    
}
