package fr.cnrs.opentheso.models.users;

import lombok.Data;
import java.io.Serializable;


@Data
public class NodeUserResource implements Serializable {

    private String mail;
    private String username;

}
