package fr.cnrs.opentheso.models.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeUserRole {

    private int idUser;
    private String userName;
    private boolean isActive;
    private int idRole;
    private String roleName;
    private String idTheso;
    private String thesoName;


    public NodeUserRole(String idTheso, String roleName, int idRole) {
        this.idTheso = idTheso;
        this.roleName = roleName;
        this.idRole = idRole;
    }

    public NodeUserRole(int idUser, String userName, boolean active, int idRole, String roleName) {
        this.userName = userName;
        this.isActive = active;
        this.idRole = idRole;
        this.roleName = roleName;
        this.idUser = idUser;
    }

    public NodeUserRole(int idUser, String userName, boolean active, int idRole, String roleName, String idThesaurus) {
        this.idTheso = idThesaurus;
        this.userName = userName;
        this.isActive = active;
        this.idRole = idRole;
        this.roleName = roleName;
        this.idUser = idUser;
    }
}
