package fr.cnrs.opentheso.models.facets;

import fr.cnrs.opentheso.models.concept.NodeUri;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeFacet {
    
    private String idFacet;
    private String idThesaurus;
    private String lexicalValue;
    private String created;
    private String modified;
    private String lang;
    private String idConceptParent;
    private NodeTermTraduction nodeTraduction;
    private NodeUri nodeUri;
    
}
