package fr.cnrs.opentheso.models.nodes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeImage {

    private int id;
    private String idConcept;
    private String idThesaurus;
    private String imageName;
    private String creator;
    private String copyRight;
    private String uri;
    private String oldUri; // pour la modification d'une Uri

}
