package fr.cnrs.opentheso.models.group;

import lombok.Data;
import java.util.ArrayList;
import java.util.Date;


@Data
public class NodeGroupLabel {

    private String idGroup;
    private String idArk;
    private String idHandle;
    private String idDoi;
    private String notation;
    private String idThesaurus;
    private Date created;
    private Date modified;
    private ArrayList<NodeGroupTraductions> nodeGroupTraductionses = new ArrayList<>();

}
