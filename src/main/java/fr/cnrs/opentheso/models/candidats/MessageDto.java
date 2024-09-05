package fr.cnrs.opentheso.models.candidats;

import java.io.Serializable;


public class MessageDto implements Serializable {

    private int idUser;
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

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }
}
