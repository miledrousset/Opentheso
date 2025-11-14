package fr.cnrs.opentheso.models.nodes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeCorpus {

    private String corpusName;
    private String uriCount;
    private String uriLink;
    private boolean isOnlyUriLink;
    private boolean active;
    private boolean omekaS;
    private int count = -1;

}
