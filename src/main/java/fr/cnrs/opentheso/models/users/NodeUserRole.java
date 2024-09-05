package fr.cnrs.opentheso.models.users;

import lombok.Data;


@Data
public class NodeUserRole {

    private int idUser;
    private String userName;
    private boolean isActive;
    private int idRole;
    private String roleName;
    private String idTheso;
    private String thesoName;
}
