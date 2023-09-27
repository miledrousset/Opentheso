package fr.cnrs.opentheso.bdd.helper.nodes.concept;

import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentSmall;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeHieraRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.status.NodeStatus;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bean.candidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.candidat.dto.VoteDto;

public class NodeConceptExport {
    
    // set relations BT
    private final ArrayList<String> relationsBT = new ArrayList<>();

    // set relations NT
    private final ArrayList<String> relationsNT = new ArrayList<>();    

    // set relations RT
    private final ArrayList<String> relationsRT = new ArrayList<>();     

    //pour gérer le concept
    private Concept concept;

    private NodeStatus nodeStatus = new NodeStatus();
    
    //BT termes génériques
    private ArrayList <NodeHieraRelation> nodeListOfBT;

    //NT pour les termes spécifiques
    private ArrayList <NodeHieraRelation> nodeListOfNT;

    //RT related term
    private ArrayList <NodeHieraRelation> nodeListIdsOfRT;

    //EM ou USE synonymes ou employé pour
    private ArrayList<NodeEM> nodeEM;

    //pour la liste des domaines du Concept
    private ArrayList<NodeUri> nodeListIdsOfConceptGroup;
    
    //les traductions ddu Term
    private ArrayList <NodeTermTraduction> nodeTermTraductions;
    
    private ArrayList <NodeNote> nodeNoteTerm;
    
    private ArrayList <NodeNote> nodeNoteConcept;
    
    private ArrayList <NodeAlignmentSmall> nodeAlignmentsList;

    private List<MessageDto> messages = new ArrayList<>();

    private List<VoteDto> votes = new ArrayList<>();
    //TODO MILTI GPS
    private NodeGps nodeGps;
    
    //concepts à utiliser pour un concept déprécié
    private ArrayList <NodeHieraRelation> replacedBy;
    
    // les concepts dépréciés qui sont reliés à ce concept
    private ArrayList <NodeHieraRelation> replaces;       
    
    //images (permet de gérer plusieurs images
    private ArrayList<NodeImage> nodeImages;    
    
    private List<String> listFacetsOfConcept;

    public NodeConceptExport() {
    }



    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }


    public ArrayList<NodeHieraRelation> getNodeListIdsOfRT() {
        return nodeListIdsOfRT;
    }

    public void setNodeListIdsOfRT(ArrayList<NodeHieraRelation> nodeListIdsOfRT) {
        this.nodeListIdsOfRT = nodeListIdsOfRT;
    }

    public ArrayList<NodeEM> getNodeEM() {
        return nodeEM;
    }

    public void setNodeEM(ArrayList<NodeEM> nodeEM) {
        this.nodeEM = nodeEM;
    }

    public ArrayList<NodeUri> getNodeListIdsOfConceptGroup() {
        return nodeListIdsOfConceptGroup;
    }

    public void setNodeListIdsOfConceptGroup(ArrayList<NodeUri> nodeListIdsOfConceptGroup) {
        this.nodeListIdsOfConceptGroup = nodeListIdsOfConceptGroup;
    }

    public ArrayList<NodeTermTraduction> getNodeTermTraductions() {
        return nodeTermTraductions;
    }

    public void setNodeTermTraductions(ArrayList<NodeTermTraduction> nodeTermTraductions) {
        this.nodeTermTraductions = nodeTermTraductions;
    }

    public ArrayList<NodeNote> getNodeNoteTerm() {
        return nodeNoteTerm;
    }

    public void setNodeNoteTerm(ArrayList<NodeNote> nodeNoteTerm) {
        this.nodeNoteTerm = nodeNoteTerm;
    }

    public ArrayList<NodeNote> getNodeNoteConcept() {
        return nodeNoteConcept;
    }

    public void setNodeNoteConcept(ArrayList<NodeNote> nodeNoteConcept) {
        this.nodeNoteConcept = nodeNoteConcept;
    }

    public ArrayList<NodeAlignmentSmall> getNodeAlignmentsList() {
        return nodeAlignmentsList;
    }

    public void setNodeAlignmentsList(ArrayList<NodeAlignmentSmall> nodeAlignmentsList) {
        this.nodeAlignmentsList = nodeAlignmentsList;
    }



    public NodeGps getNodeGps() {
        return nodeGps;
    }

    public void setNodeGps(NodeGps nodeGps) {
        this.nodeGps = nodeGps;
    }

    public ArrayList<NodeHieraRelation> getNodeListOfBT() {
        return nodeListOfBT;
    }

    public void setNodeListOfBT(ArrayList<NodeHieraRelation> nodeListOfBT) {
        this.nodeListOfBT = nodeListOfBT;
    }

    public ArrayList<NodeHieraRelation> getNodeListOfNT() {
        return nodeListOfNT;
    }

    public void setNodeListOfNT(ArrayList<NodeHieraRelation> nodeListOfNT) {
        this.nodeListOfNT = nodeListOfNT;
    }

    public ArrayList<String> getRelationsBT() {
        relationsBT.add("BT");
        relationsBT.add("BTG");
        relationsBT.add("BTP");
        relationsBT.add("BTI");
        return relationsBT;
    }

    public ArrayList<String> getRelationsNT() {
        relationsNT.add("NT");
        relationsNT.add("NTG");
        relationsNT.add("NTP");
        relationsNT.add("NTI");        
        return relationsNT;
    }

    public ArrayList<String> getRelationsRT() {
        relationsRT.add("RT");
        return relationsRT;
    }    

    public ArrayList<NodeImage> getNodeImages() {
        return nodeImages;
    }

    public void setNodeImages(ArrayList<NodeImage> nodeImages) {
        this.nodeImages = nodeImages;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }

    public List<VoteDto> getVotes() {
        return votes;
    }

    public void setVotes(List<VoteDto> votes) {
        this.votes = votes;
    }

    public ArrayList<NodeHieraRelation> getReplacedBy() {
        return replacedBy;
    }

    public void setReplacedBy(ArrayList<NodeHieraRelation> replacedBy) {
        this.replacedBy = replacedBy;
    }

    public ArrayList<NodeHieraRelation> getReplaces() {
        return replaces;
    }

    public void setReplaces(ArrayList<NodeHieraRelation> replaces) {
        this.replaces = replaces;
    }

    public List<String> getListFacetsOfConcept() {
        return listFacetsOfConcept;
    }

    public void setListFacetsOfConcept(List<String> listFacetsOfConcept) {
        this.listFacetsOfConcept = listFacetsOfConcept;
    }
    
}
