package fr.cnrs.opentheso.models.userpermissions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author miledrousset
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeThesoRole {

    private int idRole;
    private String idTheso;
    private String thesoName;
    private String roleName;
     
}
