package fr.cnrs.opentheso.bean.candidat.enumeration;

public enum NoteEnum {
    
    DEFINITION("definition"),
    NOTE("note");
    
    String name;
    
    NoteEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
