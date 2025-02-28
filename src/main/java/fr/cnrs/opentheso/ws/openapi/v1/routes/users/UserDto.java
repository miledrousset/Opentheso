package fr.cnrs.opentheso.ws.openapi.v1.routes.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends AuthentificationDto implements Serializable {

    private String mail;
    private boolean superAdmin;
    private boolean alertMail;
    private boolean active;

    private String idThesaurus;
    private Integer idRole;
    private String idProject;

}
