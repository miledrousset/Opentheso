package fr.cnrs.opentheso.models.users;

import lombok.Data;

/**
 * Permet de gérer les utilisateurs avec les groupes pour les superAdmin
 * @author miled.rousset
 */
@Data
public class NodeUserGroupUser {

    private String idUser;
    private String userName;
    private int idGroup;
    private String groupName;
    private int idRole;
    private String roleName;
}
