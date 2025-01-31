package fr.cnrs.opentheso.models.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeUserGroupUser {

    private int idUser;
    private String userName;
    private int idGroup;
    private String groupName;
    private int idRole;
    private String roleName;

}
