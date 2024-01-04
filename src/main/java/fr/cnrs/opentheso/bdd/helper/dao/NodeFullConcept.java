package fr.cnrs.opentheso.bdd.helper.dao;

import java.util.List;
import lombok.Data;
/**
 *
 * @author miledrousset
 */
@Data
public class NodeFullConcept {
    // uri 
    private String uri;
    
    // type de resource (ConceptScheme, Concept, Collection, ThesaurusArray, FoafImage)
    private int resourceType;
    
    // concept infos
    private String identifier;

    // peut être Ark, Handle ... suivant le choix pour le thésaurus
    private String permanentId;

    private String notation;

    // (DEP=seprecated, CA=Candidate)
    private int resourceStatus;

    // dates
    private String created;
    private String modified;
    // users
    private String creatorName;
    private List<String> contributorName;       

    // le label dans la langue en cours
    private ConceptLabel prefLabel;
    // les altLabels dans la langue en cours
    private List<ConceptLabel> altLabels;
    // les hiddenLabel dans la langue en cours
    private List<ConceptLabel> hiddenLabels;    
    
    
    // labels
    private List<ConceptLabel> prefLabelsTraduction;
    private List<ConceptLabel> altLabelTraduction;
    private List<ConceptLabel> hiddenLabelTraduction;    

    // relations
    private List <ConceptRelation> narrowers;
    private List <ConceptRelation> broaders;
    private List <ConceptRelation> relateds;    

    // notes
    private List<ConceptNote> notes;
    private List<ConceptNote> definitions;
    private List<ConceptNote> examples;
    private List<ConceptNote> editorialNotes;
    private List<ConceptNote> changeNotes;
    private List<ConceptNote> scopeNotes;
    private List<ConceptNote> historyNotes;

    // alignements
    private List<String> exactMatchs;
    private List<String> closeMatchs;
    private List<String> broadMatchs;
    private List<String> relatedMatchs;
    private List<String> narrowMatchs;

    // coordonnées GPS
    private List<ResourceGPS> gps;

    // images
    private List<ConceptImage> images;
    
    // membres, les collections dont le concept est membre
    private List<ConceptIdLabel> membres;
    
    //concepts à utiliser pour un concept déprécié
    private List <ConceptIdLabel> replacedBy;
    
    // les concepts dépréciés qui sont reliés à ce concept
    private List <ConceptIdLabel> replaces;    
    
    // les Facettes dont le concept en fait partie
    private List <ConceptIdLabel> facets;    
    
    // liste des Qualificatifs
    private List <ConceptCustomRelation> nodeCustomRelations;    
    
    
}
