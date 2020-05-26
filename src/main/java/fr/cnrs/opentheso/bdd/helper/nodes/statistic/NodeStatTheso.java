package fr.cnrs.opentheso.bdd.helper.nodes.statistic;

import java.io.Serializable;

public class NodeStatTheso implements Serializable {
    private String group;
    private int nbDescripteur;
    private int nbNonDescripteur;
    private int nbNoTrad;
    private int nbNotes;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getNbDescripteur() {
        return nbDescripteur;
    }

    public void setNbDescripteur(int nbDescripteur) {
        this.nbDescripteur = nbDescripteur;
    }

    public int getNbNonDescripteur() {
        return nbNonDescripteur;
    }

    public void setNbNonDescripteur(int nbNonDescripteur) {
        this.nbNonDescripteur = nbNonDescripteur;
    }

    public int getNbNoTrad() {
        return nbNoTrad;
    }

    public void setNbNoTrad(int nbNoTrad) {
        this.nbNoTrad = nbNoTrad;
    }

    public int getNbNotes() {
        return nbNotes;
    }

    public void setNbNotes(int nbNotes) {
        this.nbNotes = nbNotes;
    }
}
