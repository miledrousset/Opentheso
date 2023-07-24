/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.datas;
import lombok.Data;

/**
 *
 * @author miled.rousset
 */

@Data
public class DcElement {
    private int id;
    protected String name;
    protected String value;
    protected String language;

    public DcElement() {
    }

    
    public DcElement(String name, String value, String language) {
        this.id = -1;
        this.language = language;
        this.name = name;
        this.value = value;
    }
}
