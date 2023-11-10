package fr.cnrs.opentheso.bdd.helper.nodes.userpermissions;

import java.util.List;
import lombok.Data;

/**
 *
 * @author miledrousset
 */

@Data
public class NodeProjectThesoRole {
    private int idProject;
    private String projectName;
    
    List<NodeThesoRole> nodeThesoRoles;
}
