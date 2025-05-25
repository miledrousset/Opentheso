package fr.cnrs.opentheso.models.statistiques;

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
