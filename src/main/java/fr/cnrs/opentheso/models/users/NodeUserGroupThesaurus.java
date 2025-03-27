package fr.cnrs.opentheso.models.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeUserGroupThesaurus {

    private String idThesaurus;
    private String thesaurusName;
    private int idGroup;
    private String groupName;
    private boolean privateTheso;
}
