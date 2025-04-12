package fr.cnrs.opentheso.bean.toolbox.statistique;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ConceptStatisticData {
    
    private String idConcept;
    private String label;
    private String type;
    private String dateCreation;
    private String dateModification;
    private String utilisateur;

}
