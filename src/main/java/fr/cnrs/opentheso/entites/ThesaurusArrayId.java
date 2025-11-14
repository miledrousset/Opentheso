package fr.cnrs.opentheso.entites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThesaurusArrayId {

    private String idThesaurus;
    private String idConceptParent;
    private String idFacet;

}
