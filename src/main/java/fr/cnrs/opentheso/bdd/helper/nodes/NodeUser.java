package fr.cnrs.opentheso.bdd.helper.nodes;

import lombok.Data;
import java.io.Serializable;


@Data
public class NodeUser implements Serializable {

    private int idUser;
    private String name;
    private String mail;
    private boolean active;
    private boolean alertMail;
    private boolean superAdmin;
    private boolean passtomodify;

}
