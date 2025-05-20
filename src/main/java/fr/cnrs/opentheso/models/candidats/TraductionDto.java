package fr.cnrs.opentheso.models.candidats;

import lombok.Data;
import java.io.Serializable;


@Data
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
    
}
