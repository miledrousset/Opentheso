/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.privatesdatas;

/**
 *
 * @author antonio.perez
 */
public class LineOfData {
    
    private String colomne;
    private String value;

    public String getColomne() {
        return colomne;
    }

    public void setColomne(String colomne) {
        this.colomne = colomne;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if(value == null){
            this.value = "";
            return;
        }
        if(value.isEmpty()){
            this.value = "";
            return;
        }
        if("null".equals(value)){
            this.value = "";
            return;
        }
        this.value = value.trim();
    }
}
