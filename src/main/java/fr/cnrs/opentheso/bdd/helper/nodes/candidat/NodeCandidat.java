package fr.cnrs.opentheso.bdd.helper.nodes.candidat;

import java.io.Serializable;
import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;

public class NodeCandidat implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private ArrayList <NodeUser> nodesUser;
    private ArrayList <NodeTraductionCandidat> nodeTraductions; 

    public ArrayList<NodeUser> getNodesUser() {
        return nodesUser;
    }

    public void setNodesUser(ArrayList<NodeUser> nodesUser) {
        this.nodesUser = nodesUser;
    }

    public ArrayList<NodeTraductionCandidat> getNodeTraductions() {
        return nodeTraductions;
    }

    public void setNodeTraductions(ArrayList<NodeTraductionCandidat> nodeTraductions) {
        this.nodeTraductions = nodeTraductions;
    }
    
    
}
