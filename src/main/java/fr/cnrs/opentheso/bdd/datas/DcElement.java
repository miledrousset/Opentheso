/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.datas;

/**
 *
 * @author miled.rousset
 */

public class DcElement {
    public static final String TITLE = "title";
    public static final String CREATOR = "creator";
    public static final String CONTRIBUTOR = "contributor";    
    public static final String SUBJECT = "subject";    

    protected String name;
    protected String value;
    protected String language;

    public DcElement() {
    }

    
    public DcElement(String name, String value, String language) {
        this.language = language;
        this.name = name;
        this.value = value;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
