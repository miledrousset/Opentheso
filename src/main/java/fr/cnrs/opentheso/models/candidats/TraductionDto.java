package fr.cnrs.opentheso.models.candidats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
public class TraductionDto implements Serializable {
    
    private String langue;
    private String traduction;
    private String codePays;
}
