package fr.cnrs.opentheso.models.nodes;

import lombok.Data;


@Data
public class NodeCorpus {

    private String corpusName;
    private String uriCount;
    private String uriLink;
    private boolean isOnlyUriLink;
    private boolean active;
    private boolean omekaS;
    private int count = -1;

}
