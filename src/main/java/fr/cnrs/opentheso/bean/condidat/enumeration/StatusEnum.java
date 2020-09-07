package fr.cnrs.opentheso.bean.condidat.enumeration;

public enum StatusEnum {

    WAITING(1),
    INSERTED(2),
    REJECTED(3);

    private int value;

    private StatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
