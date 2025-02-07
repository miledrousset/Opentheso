package fr.cnrs.opentheso.models.users;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;


@Data
public class NodeUser implements Serializable {

    private int idUser;
    private String name;
    private String mail;
    private boolean active;
    private boolean alertMail;
    private boolean superAdmin;
    private boolean passToModify;
    private String apiKey;
    private boolean keyNeverExpire;
    private LocalDate apiKeyExpireDate;
    private boolean isServiceAccount;
    private String keyDescription;

}
