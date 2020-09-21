package fr.cnrs.opentheso.bean.condidat.enumeration;

public enum VoteType {

    CANDIDAT("CA"),
    NOTE("NT");

    private String label;

    VoteType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
