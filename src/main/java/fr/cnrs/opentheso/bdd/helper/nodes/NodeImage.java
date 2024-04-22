package fr.cnrs.opentheso.bdd.helper.nodes;

import lombok.Data;

@Data
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
