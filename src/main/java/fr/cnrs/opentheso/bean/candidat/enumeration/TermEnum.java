package fr.cnrs.opentheso.bean.candidat.enumeration;

public enum TermEnum {

    TERME_GENERIQUE("BT"),
    TERME_SPECIFIQUE("NT"),
    TERME_ASSOCIE("RT");

    private String label;

    TermEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
