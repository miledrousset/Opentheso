package fr.cnrs.opentheso.models.imports.rdf4j.helper;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.HierarchicalRelationship;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.*;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.status.NodeStatus;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTerm;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bean.candidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.candidat.dto.VoteDto;
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
    public ImagesHelper imagesHelper = new ImagesHelper();
    public List<VoteDto> votes = new ArrayList<>();
    public List<MessageDto> messages = new ArrayList<>();

    public ArrayList<NodeImage> nodeImages = new ArrayList<>();
    public Term term = new Term();

}
