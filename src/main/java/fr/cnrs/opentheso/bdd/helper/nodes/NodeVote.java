package fr.cnrs.opentheso.bdd.helper.nodes;

import java.io.Serializable;

public class NodeVote implements Serializable {

    private int idUser;
    private String idNote;

    public NodeVote() {
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getIdNote() {
        return idNote;
    }

    public void setIdNote(String idNote) {
        this.idNote = idNote;
    }


}
