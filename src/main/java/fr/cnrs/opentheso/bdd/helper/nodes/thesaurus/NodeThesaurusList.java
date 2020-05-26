/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes.thesaurus;

/**
 *
 * @author miled.rousset
 */
public class NodeThesaurusList {

    private String idThesaurus;
    private String idLang;
    private String title;

    public NodeThesaurusList() {
        super();
    }

    public NodeThesaurusList(String idThesaurus) {
        super();
        this.idThesaurus = idThesaurus;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public String getIdLang() {
        return idLang;
    }

    public void setIdLang(String idLang) {
        this.idLang = idLang;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
