package fr.cnrs.opentheso.models.userpermissions;

import java.util.List;
import lombok.Data;


@Data
public class NodeProjectThesoRole {

    private int idProject;
    private String projectName;
    private List<NodeThesoRole> nodeThesoRoles;
}
