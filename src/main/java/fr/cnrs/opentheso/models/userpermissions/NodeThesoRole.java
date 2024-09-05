package fr.cnrs.opentheso.models.userpermissions;

import lombok.Data;
/**
 *
 * @author miledrousset
 */
@Data
public class NodeThesoRole {

    private int idRole;
    private String idTheso;
    private String thesoName;
    private String roleName;
     
}
