package fr.cnrs.opentheso.bean.condidat.dto;


public class DomaineDto {
    
    private int id;
    
    private String name;

    public DomaineDto() {
    }

    public DomaineDto(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
