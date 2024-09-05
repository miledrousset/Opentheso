package fr.cnrs.opentheso.models.group;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;


@Data
public class NodeGroupSousGroup {

    private List<NodeGroupIdLabel> hierarchyOfGroup = new ArrayList<>();
   
}
