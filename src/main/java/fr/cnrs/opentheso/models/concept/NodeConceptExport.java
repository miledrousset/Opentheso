package fr.cnrs.opentheso.models.concept;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.models.alignment.NodeAlignmentSmall;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.nodes.NodeGps;
import fr.cnrs.opentheso.models.relations.NodeHieraRelation;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.status.NodeStatus;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.models.candidats.MessageDto;
import fr.cnrs.opentheso.models.candidats.VoteDto;


@Data
public class NodeConceptExport {
    
    // set relations BT
    private final List<String> relationsBT = new ArrayList<>();

    // set relations NT
    private final List<String> relationsNT = new ArrayList<>();

    // set relations RT
    private final List<String> relationsRT = new ArrayList<>();

    //pour gérer le concept
    private Concept concept;

    private NodeStatus nodeStatus = new NodeStatus();
    
    //BT termes génériques
    private List <NodeHieraRelation> nodeListOfBT;

    //NT pour les termes spécifiques
    private List <NodeHieraRelation> nodeListOfNT;

    //RT related term
    private List <NodeHieraRelation> nodeListIdsOfRT;

    //EM ou USE synonymes ou employé pour
    private List<NodeEM> nodeEM;

    //pour la liste des domaines du Concept
    private List<NodeUri> nodeListIdsOfConceptGroup;
    
    //les traductions ddu Term
    private List <NodeTermTraduction> nodeTermTraductions;
    
    private List <NodeNote> nodeNoteTerm;
    
    private List <NodeNote> nodeNoteConcept;
    
    private List <NodeNote> nodeNotes;    
    
    private List <NodeAlignmentSmall> nodeAlignmentsList;

    private List<MessageDto> messages = new ArrayList<>();

    private List<VoteDto> votes = new ArrayList<>();

    private List<NodeGps> nodeGps;
    
    //concepts à utiliser pour un concept déprécié
    private List <NodeHieraRelation> replacedBy;
    
    // les concepts dépréciés qui sont reliés à ce concept
    private List <NodeHieraRelation> replaces;       
    
    //images (permet de gérer plusieurs images
    private List<NodeImage> nodeImages;    
    
    private List<String> listFacetsOfConcept;
    

    public List<String> getRelationsBT() {
        relationsBT.add("BT");
        relationsBT.add("BTG");
        relationsBT.add("BTP");
        relationsBT.add("BTI");
        return relationsBT;
    }

    public List<String> getRelationsNT() {
        relationsNT.add("NT");
        relationsNT.add("NTG");
        relationsNT.add("NTP");
        relationsNT.add("NTI");        
        return relationsNT;
    }

    public List<String> getRelationsRT() {
        relationsRT.add("RT");
        return relationsRT;
    }
    
}
