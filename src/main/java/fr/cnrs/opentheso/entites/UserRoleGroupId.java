package fr.cnrs.opentheso.entites;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleGroupId implements Serializable {

    private Integer user;
    private Integer role;
    private Integer group;

}
