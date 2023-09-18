package fr.cnrs.opentheso.bdd.helper.nodes.concept;

import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCustomRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeRT;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.entites.Gps;
import lombok.Data;

import java.util.List;


@Data
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
    private List<Gps> nodeGps;
    
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
        if(nodeGps != null) nodeGps.clear();
        if(replacedBy != null) replacedBy.clear();    
        if(replaces != null) replaces.clear();    
        if(nodeExternalResources != null) nodeExternalResources.clear(); 
        if(nodeCustomRelations != null) nodeCustomRelations.clear(); 
        if(dcElements != null) dcElements.clear();
    }
    
}
