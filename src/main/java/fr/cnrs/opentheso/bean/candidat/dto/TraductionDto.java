package fr.cnrs.opentheso.bean.candidat.dto;

import java.io.Serializable;


public class TraductionDto implements Serializable {
    
    private String langue;
    
    private String traduction;
    private String codePays;

    public TraductionDto() {
        langue = "";
        traduction = "";
        codePays = "";
    }

    public TraductionDto(String langue, String traduction, String codePays) {
        this.langue = langue;
        this.traduction = traduction;
        this.codePays = codePays;
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

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }
    
}
