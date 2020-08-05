package fr.cnrs.opentheso.bdd.helper.nodes.candidat;

import java.io.Serializable;
import java.util.ArrayList;

public class NodeCandidateOld implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String idCandidate;
    private String status;
    private ArrayList <NodeTraductionCandidat> nodeTraductions; 
    private ArrayList<NodeProposition> nodePropositions;

    public String getIdCandidate() {
        return idCandidate;
    }

    public void setIdCandidate(String idCandidate) {
        this.idCandidate = idCandidate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<NodeTraductionCandidat> getNodeTraductions() {
        return nodeTraductions;
    }

    public void setNodeTraductions(ArrayList<NodeTraductionCandidat> nodeTraductions) {
        this.nodeTraductions = nodeTraductions;
    }

    public ArrayList<NodeProposition> getNodePropositions() {
        return nodePropositions;
    }

    public void setNodePropositions(ArrayList<NodeProposition> nodePropositions) {
        this.nodePropositions = nodePropositions;
    }

    
    
}
