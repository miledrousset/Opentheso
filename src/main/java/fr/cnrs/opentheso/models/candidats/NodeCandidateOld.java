package fr.cnrs.opentheso.models.candidats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeCandidateOld implements Serializable {

    private String idCandidate;
    private String status;
    private List<NodeTraductionCandidat> nodeTraductions;
    private List<NodeProposition> nodePropositions;

}
