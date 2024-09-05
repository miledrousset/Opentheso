package fr.cnrs.opentheso.models.candidats;

import fr.cnrs.opentheso.models.terms.NodeElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeConceptSearch implements Serializable {

    private String idConcept;
    private String idTerm;
    private String status;
    private List<NodeElement> terms;
    private List<NodeElement> collections;
    private List<NodeElement> synonymes;
    private List<NodeElement> notes;
    private List<NodeElement> definitions;

}
