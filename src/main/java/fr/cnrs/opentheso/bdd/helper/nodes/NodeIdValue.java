/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes;

import java.text.Normalizer;

/**
 *
 * @author miled.rousset
 */
public class NodeIdValue implements Comparable{
    private String id;
    private String value;
    private boolean Status;
    private String notation;

    public NodeIdValue() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isStatus() {
        return Status;
    }

    public void setStatus(boolean Status) {
        this.Status = Status;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public String getNotation() {
        return notation;
    }

    @Override
    public int compareTo(Object o) {
        try {
            String str1, str2;
            str1 = Normalizer.normalize(this.value, Normalizer.Form.NFD);
            str1 = str1.replaceAll("[^\\p{ASCII}]", "");
            str2 = Normalizer.normalize(((NodeIdValue)o).value, Normalizer.Form.NFD);
            str2 = str2.replaceAll("[^\\p{ASCII}]", "");
            return str1.toUpperCase().compareTo(str2.toUpperCase());
        } catch (Exception e) {
            return 1;
        }
    }
}
