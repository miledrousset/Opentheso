package fr.cnrs.opentheso.models.imports;

import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.relations.HierarchicalRelationship;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.bdd.helper.*;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.nodes.NodeGps;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.status.NodeStatus;
import fr.cnrs.opentheso.models.terms.NodeTerm;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.models.candidats.MessageDto;
import fr.cnrs.opentheso.models.candidats.VoteDto;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;

import java.util.ArrayList;
import java.util.List;

public class AddConceptsStruct {

    public Concept concept;
    public ConceptHelper conceptHelper = new ConceptHelper();
    public String conceptStatus = "";
    public SKOSResource conceptResource;
    public NodeStatus status;
    public String collectionToAdd;
    // pour intégrer les coordonnées GPS
    public List<NodeGps> nodeGps = new ArrayList<>();
    public GpsHelper gpsHelper = new GpsHelper();
    //ajout des termes et traductions
    public NodeTerm nodeTerm = new NodeTerm();
    public ArrayList<NodeTermTraduction> nodeTermTraductionList = new ArrayList<>();
    //Enregister les synonymes et traductions
    public ArrayList<NodeEM> nodeEMList = new ArrayList<>();
    // ajout des notes
    public ArrayList<NodeNote> nodeNotes = new ArrayList<>();
    //ajout des relations
    public ArrayList<HierarchicalRelationship> hierarchicalRelationships = new ArrayList<>();
    // ajout des relations Groups
    public ArrayList<String> idGrps = new ArrayList<>();

    /// objects pour les concepts dépréciés
    //concepts à utiliser pour un concept déprécié
    public ArrayList <NodeIdValue> replacedBy = new ArrayList<>();

    // les concepts dépréciés qui sont reliés à ce concept
    public ArrayList <NodeIdValue> replaces = new ArrayList<>();

    // ajout des alignements
    public ArrayList<NodeAlignment> nodeAlignments = new ArrayList<>();
    public TermHelper termHelper = new TermHelper();
    public NoteHelper noteHelper = new NoteHelper();
    public boolean isTopConcept = false;
    public AlignmentHelper alignmentHelper = new AlignmentHelper();
    public List<VoteDto> votes = new ArrayList<>();
    public List<MessageDto> messages = new ArrayList<>();

    public ArrayList<NodeImage> nodeImages = new ArrayList<>();
    public Term term = new Term();

}
