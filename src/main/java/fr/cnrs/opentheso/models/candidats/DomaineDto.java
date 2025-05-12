package fr.cnrs.opentheso.models.candidats;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class DomaineDto {
    
    private String id;
    private String name;
    
}
