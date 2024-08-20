package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_role_group")
public class UserRoleGroup {

    @Id
    @Column(name = "id_user")
    private int idUser;

    @Id
    @Column(name = "id_role")
    private int idRole;

    @Id
    @Column(name = "id_group")
    private int idGroup;

}
