package fr.cnrs.opentheso.models.candidats;

import lombok.Data;
import java.io.Serializable;
import java.util.List;


@Data
public class NodeCandidateOld implements Serializable {

    private String idCandidate;
    private String status;
    private List<NodeTraductionCandidat> nodeTraductions;
    private List<NodeProposition> nodePropositions;

}
