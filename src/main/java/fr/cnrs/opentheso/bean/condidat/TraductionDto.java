package fr.cnrs.opentheso.bean.condidat;

import java.io.Serializable;


public class TraductionDto implements Serializable {
    
    private String langue;
    
    private String traduction;

    public TraductionDto() {
        langue = "";
        traduction = "";
    }

    public TraductionDto(String langue, String traduction) {
        this.langue = langue;
        this.traduction = traduction;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getTraduction() {
        return traduction;
    }

    public void setTraduction(String traduction) {
        this.traduction = traduction;
    }
    
}
