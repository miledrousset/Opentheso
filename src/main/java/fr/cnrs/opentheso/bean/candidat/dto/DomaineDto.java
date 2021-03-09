package fr.cnrs.opentheso.bean.candidat.dto;


public class DomaineDto {
    
    private String id;
    
    private String name;

    public DomaineDto() {
    }

    public DomaineDto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
