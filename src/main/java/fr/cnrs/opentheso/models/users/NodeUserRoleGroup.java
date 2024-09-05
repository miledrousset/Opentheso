package fr.cnrs.opentheso.models.users;

import lombok.Data;


@Data
public class NodeUserRoleGroup {

    private int idRole;
    private String roleName;
    private int idGroup;
    private String groupName;
    private boolean isAdmin;
    private boolean isManager;
    private boolean isContributor;

}
