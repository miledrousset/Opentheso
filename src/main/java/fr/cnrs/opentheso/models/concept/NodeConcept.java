package fr.cnrs.opentheso.models.concept;

import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.relations.NodeCustomRelation;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.models.terms.NodeNT;
import fr.cnrs.opentheso.models.terms.NodeRT;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.entites.Gps;
import lombok.Data;



@Data
public class NodeConcept {

    //BT termes génériques
    private List<NodeBT> nodeBT;

    //pour gérer le concept
    private Concept concept;

    // pour gérer le terme et ses valeurs 
    private Term term;

    //NT pour les termes spécifiques
    private List<NodeNT> nodeNT;

    //RT related term
    private List<NodeRT> nodeRT;

    //EM ou USE synonymes ou employé pour
    private List<NodeEM> nodeEM;

    // notes gestion de toutes les notes
    private List<NodeNote> nodeNotesTerm;
    
        // notes gestion de toutes les notes
    private List<NodeNote> nodeNotesConcept;
    
    // notes gestion de toutes les notes
    private List<NodeNote> nodeNotes;    

    //images (permet de gérer plusieurs images
    private List<NodeImage> nodeimages;
    
    //Ressources externes
    private List<NodeImage> nodeExternalResources;
    

    //pour la liste des domaines du Concept
    private List<NodeGroup> nodeConceptGroup;
    
    //les traductions ddu Term
    private List<NodeTermTraduction> nodeTermTraductions;
    
    //les alignements
    private List<NodeAlignment> nodeAlignments;
    
    //coordonnées GPS
    private List<Gps> nodeGps;
    
    //concepts à utiliser pour un concept déprécié
    private List<NodeIdValue> replacedBy;
    
    // les concepts dépréciés qui sont reliés à ce concept
    private List<NodeIdValue> replaces;    
    
    // liste des Qualificatifs
    private List<NodeCustomRelation> nodeCustomRelations;

    // Dublin Core
    private List<DcElement> dcElements;
    
    public NodeConcept() {
        nodeNotesTerm = new ArrayList<>();
        nodeNotesConcept = new ArrayList<>();
    }

    public void clear(){
        concept = null;
        term = null;
        nodeBT = new ArrayList<>();
        nodeNT = new ArrayList<>();
        nodeRT = new ArrayList<>();
        nodeEM = new ArrayList<>();
        nodeNotesTerm = new ArrayList<>();
        nodeNotesConcept = new ArrayList<>();
        nodeNotes = new ArrayList<>();
        nodeimages = new ArrayList<>();
        nodeConceptGroup = new ArrayList<>();
        nodeTermTraductions = new ArrayList<>();
        nodeAlignments = new ArrayList<>();
        nodeGps = new ArrayList<>();
        replacedBy = new ArrayList<>();
        replaces = new ArrayList<>();
        nodeExternalResources = new ArrayList<>();
        nodeCustomRelations = new ArrayList<>();
        dcElements = new ArrayList<>();
    }
    
}
