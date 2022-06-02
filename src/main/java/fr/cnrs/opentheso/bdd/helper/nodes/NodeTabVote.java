package fr.cnrs.opentheso.bdd.helper.nodes;

import java.io.Serializable;

public class NodeTabVote implements Serializable {

    private int idUser;
    private String userName;
    
    private String typeNote;
    private String noteValue;
    
    public NodeTabVote() {
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTypeNote() {
        return typeNote;
    }

    public void setTypeNote(String typeNote) {
        this.typeNote = typeNote;
    }

    public String getNoteValue() {
        return noteValue;
    }

    public void setNoteValue(String noteValue) {
        this.noteValue = noteValue;
    }



}
