package fr.cnrs.opentheso.models.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeUserRoleGroup {

    private int idRole;
    private String roleName;
    private int idGroup;
    private String groupName;
    private boolean isAdmin;
    private boolean isManager;
    private boolean isContributor;

}
