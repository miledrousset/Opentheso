package fr.cnrs.opentheso.entites;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationGroupId {

    private String idGroup1;
    private String idThesaurus;
    private String relation;
    private String idGroup2;
}
