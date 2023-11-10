package fr.cnrs.opentheso.bdd.helper.nodes.userpermissions;

import lombok.Data;
/**
 *
 * @author miledrousset
 */
@Data
public class NodeThesoRole {
    
    private String idTheso;
    private String thesoName;
    
    private int idRole;
    private String roleName;
     
}
