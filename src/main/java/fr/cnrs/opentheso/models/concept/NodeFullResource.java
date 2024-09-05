package fr.cnrs.opentheso.models.concept;

import fr.cnrs.opentheso.models.nodes.DcElement;
import java.util.List;
import lombok.Data;


@Data
public class NodeFullResource {
    
    // type de resource (ConceptScheme, Concept, Collection, ThesaurusArray, FoafImage)
    private String resourceType;
    
    // Uri de la resource
    private String Uri;
    
    // peut être Ark, Handle ... suivant le choix pour le thésaurus
    private String permanentId;
    
    private String notation;
    
    // (DEP=seprecated, CA=Candidate)
    private String resourceStatus;

    // labels (prefLabel, altLabel, HiddenLabel
    private List<ResourceLabel> labels;

    // DcElements (identifier, creator, contributor, Date, externalResources
    private List<DcElement> dcElements;
    
    // relations list (membres, replaces, replaced_by, 
    private List <ResourceRelation> relations;

    // notes
    private List<ResourceNote> notes;

    // alignements
    private List<ResourceAlignment> alignments;

    // coordonnées GPS long et lat (Point, Plyline, Polygone)
    private List<ResourceGPS> gps;
    
}
