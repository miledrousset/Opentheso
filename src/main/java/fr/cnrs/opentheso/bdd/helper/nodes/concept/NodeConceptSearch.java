package fr.cnrs.opentheso.bdd.helper.nodes.concept;

import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeRT;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;

public class NodeConceptSearch {
    private String idConcept;
    private String prefLabel;
    private ArrayList <NodeTermTraduction> nodeTermTraductions;
    private ArrayList <NodeBT> nodeBT;
    private ArrayList <NodeNT> nodeNT;
    private ArrayList <NodeRT> nodeRT;
    private ArrayList<NodeEM> nodeEM;
    private ArrayList<NodeGroup> nodeConceptGroup;    

//    private ArrayList<NodeNote> nodeNotesTerm;
//    private ArrayList<NodeNote> nodeNotesConcept;

    public void clear(){
        idConcept = null;
        prefLabel = null;
        if(nodeTermTraductions != null)
            nodeTermTraductions.clear();
        if(nodeBT != null)
            nodeBT.clear();        
        if(nodeNT != null)
            nodeNT.clear();    
        if(nodeRT != null)
            nodeRT.clear(); 
        if(nodeEM != null)
            nodeEM.clear(); 
        if(nodeConceptGroup != null)
            nodeConceptGroup.clear();
    }
    
    public NodeConceptSearch() {
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public ArrayList<NodeTermTraduction> getNodeTermTraductions() {
        return nodeTermTraductions;
    }

    public void setNodeTermTraductions(ArrayList<NodeTermTraduction> nodeTermTraductions) {
        this.nodeTermTraductions = nodeTermTraductions;
    }

    public ArrayList<NodeBT> getNodeBT() {
        return nodeBT;
    }

    public void setNodeBT(ArrayList<NodeBT> nodeBT) {
        this.nodeBT = nodeBT;
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

    public ArrayList<NodeGroup> getNodeConceptGroup() {
        return nodeConceptGroup;
    }

    public void setNodeConceptGroup(ArrayList<NodeGroup> nodeConceptGroup) {
        this.nodeConceptGroup = nodeConceptGroup;
    }

    
}
