package fr.cnrs.opentheso.models.users;

import lombok.Data;


@Data
public class NodeUserGroupThesaurus {

    private String idThesaurus;
    private String thesaurusName;
    private int idGroup;
    private String groupName;
    private boolean privateTheso;
}
