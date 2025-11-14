package fr.cnrs.opentheso.models.concept;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeUri {

    private String idArk;
    private String idHandle;
    private String idDoi;
    private String idConcept;

}
