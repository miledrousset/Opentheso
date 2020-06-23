package fr.cnrs.opentheso.bean.condidat.dto;

import java.io.Serializable;


public class MessageDto implements Serializable {
    
    private String nom;
    private String msg;
    private String date;
    private boolean mine;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }
    
    public String getMessagePossition() {
        return mine ? "right" : "left";
    }
    
    public String getMessageColor() {
        return mine ? "#C8EAD6" : "#FAFAFA";
    }
    
}
