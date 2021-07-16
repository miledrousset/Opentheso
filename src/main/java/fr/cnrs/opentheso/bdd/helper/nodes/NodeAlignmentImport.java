package fr.cnrs.opentheso.bdd.helper.nodes;

import java.util.ArrayList;


/**
 *
 * @author miled.rousset
 */
public class NodeAlignmentImport {
    private String localId;
    private ArrayList<NodeAlignmentSmall> nodeAlignmentSmalls;
    
    public NodeAlignmentImport() {
        nodeAlignmentSmalls = new ArrayList<>();
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public ArrayList<NodeAlignmentSmall> getNodeAlignmentSmalls() {
        return nodeAlignmentSmalls;
    }

    public void setNodeAlignmentSmalls(ArrayList<NodeAlignmentSmall> nodeAlignmentSmalls) {
        this.nodeAlignmentSmalls = nodeAlignmentSmalls;
    }
    
}
