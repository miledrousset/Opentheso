package fr.cnrs.opentheso.bdd.helper.nodes.concept;

import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCustomRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeRT;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import java.util.List;

public class NodeConcept {

    //BT termes génériques
    private ArrayList <NodeBT> nodeBT;

    //pour gérer le concept
    private Concept concept;

    // pour gérer le terme et ses valeurs 
    private Term term;

    //NT pour les termes spécifiques
    private ArrayList <NodeNT> nodeNT;

    //RT related term
    private ArrayList <NodeRT> nodeRT;

    //EM ou USE synonymes ou employé pour
    private ArrayList<NodeEM> nodeEM;

    // notes gestion de toutes les notes
    private ArrayList<NodeNote> nodeNotesTerm;
    
        // notes gestion de toutes les notes
    private ArrayList<NodeNote> nodeNotesConcept;

    //images (permet de gérer plusieurs images
    private ArrayList<NodeImage> nodeimages;
    
    //Ressources externes
    private ArrayList<NodeImage> nodeExternalResources;
    

    //pour la liste des domaines du Concept
    private ArrayList<NodeGroup> nodeConceptGroup;
    
    //les traductions ddu Term
    private ArrayList <NodeTermTraduction> nodeTermTraductions;
    
    //les alignements
    private ArrayList <NodeAlignment> nodeAlignments;
    
    //coordonnées GPS
    private NodeGps nodeGps;
    
    //concepts à utiliser pour un concept déprécié
    private ArrayList <NodeIdValue> replacedBy;
    
    // les concepts dépréciés qui sont reliés à ce concept
    private ArrayList <NodeIdValue> replaces;    
    
    // liste des Qualificatifs
    private ArrayList <NodeCustomRelation> nodeCustomRelations;

    // Dublin Core
    private ArrayList<DcElement> dcElements;
    
    public NodeConcept() {
        nodeNotesTerm = new ArrayList<>();
        nodeNotesConcept = new ArrayList<>();
    }

    public ArrayList<NodeBT> getNodeBT() {
        return nodeBT;
    }

    public void setNodeBT(ArrayList<NodeBT> nodeBT) {
        this.nodeBT = nodeBT;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public ArrayList<NodeNT> getNodeNT() {
        return nodeNT;
    }

    public void setNodeNT(ArrayList<NodeNT> nodeNT) {
        this.nodeNT = nodeNT;
    }

    public ArrayList<NodeRT> getNodeRT() {
        return nodeRT;
    }

    public void setNodeRT(ArrayList<NodeRT> nodeRT) {
        this.nodeRT = nodeRT;
    }

    public ArrayList<NodeEM> getNodeEM() {
        return nodeEM;
    }

    public void setNodeEM(ArrayList<NodeEM> nodeEM) {
        this.nodeEM = nodeEM;
    }

    public ArrayList<NodeNote> getNodeNotesTerm() {
        return nodeNotesTerm;
    }

    public void setNodeNotesTerm(ArrayList<NodeNote> nodeNotesTerm) {
        this.nodeNotesTerm = nodeNotesTerm;
    }

    public ArrayList<NodeNote> getNodeNotesConcept() {
        return nodeNotesConcept;
    }

    public void setNodeNotesConcept(ArrayList<NodeNote> nodeNotesConcept) {
        this.nodeNotesConcept = nodeNotesConcept;
    }

    public ArrayList<NodeImage> getNodeimages() {
        return nodeimages;
    }

    public void setNodeimages(ArrayList<NodeImage> nodeimages) {
        this.nodeimages = nodeimages;
    }

    public ArrayList<NodeGroup> getNodeConceptGroup() {
        return nodeConceptGroup;
    }

    public void setNodeConceptGroup(ArrayList<NodeGroup> nodeConceptGroup) {
        this.nodeConceptGroup = nodeConceptGroup;
    }

    public ArrayList<NodeTermTraduction> getNodeTermTraductions() {
        return nodeTermTraductions;
    }

    public void setNodeTermTraductions(ArrayList<NodeTermTraduction> nodeTermTraductions) {
        this.nodeTermTraductions = nodeTermTraductions;
    }

    public ArrayList<NodeAlignment> getNodeAlignments() {
        return nodeAlignments;
    }

    public void setNodeAlignments(ArrayList<NodeAlignment> nodeAlignments) {
        this.nodeAlignments = nodeAlignments;
    }

    public NodeGps getNodeGps() {
        return nodeGps;
    }

    public void setNodeGps(NodeGps nodeGps) {
        this.nodeGps = nodeGps;
    }

    public ArrayList<NodeIdValue> getReplacedBy() {
        return replacedBy;
    }

    public void setReplacedBy(ArrayList<NodeIdValue> replacedBy) {
        this.replacedBy = replacedBy;
    }

    public ArrayList<NodeIdValue> getReplaces() {
        return replaces;
    }

    public void setReplaces(ArrayList<NodeIdValue> replaces) {
        this.replaces = replaces;
    }

    public ArrayList<NodeImage> getNodeExternalResources() {
        return nodeExternalResources;
    }

    public void setNodeExternalResources(ArrayList<NodeImage> nodeExternalResources) {
        this.nodeExternalResources = nodeExternalResources;
    }

    public ArrayList<NodeCustomRelation> getNodeCustomRelations() {
        return nodeCustomRelations;
    }

    public void setNodeCustomRelations(ArrayList<NodeCustomRelation> nodeCustomRelations) {
        this.nodeCustomRelations = nodeCustomRelations;
    }

    public ArrayList<DcElement> getDcElements() {
        return dcElements;
    }

    public void setDcElements(ArrayList<DcElement> dcElements) {
        this.dcElements = dcElements;
    }

    public void clear(){
        if(nodeBT != null) nodeBT.clear();
        concept = null;
        term = null;
        if(nodeNT != null) nodeNT.clear();
        if(nodeRT != null) nodeRT.clear();
        if(nodeEM != null) nodeEM.clear();
        if(nodeNotesTerm != null) nodeNotesTerm.clear();
        if(nodeNotesConcept != null) nodeNotesConcept.clear();
        if(nodeimages != null) nodeimages.clear();
        if(nodeConceptGroup != null) nodeConceptGroup.clear();
        if(nodeTermTraductions != null) nodeTermTraductions.clear();
        if(nodeAlignments != null) nodeAlignments.clear();
        nodeGps = null;
        if(replacedBy != null) replacedBy.clear();    
        if(replaces != null) replaces.clear();    
        if(nodeExternalResources != null) nodeExternalResources.clear(); 
        if(nodeCustomRelations != null) nodeCustomRelations.clear(); 
        if(dcElements != null) dcElements.clear();
    }
    
}
