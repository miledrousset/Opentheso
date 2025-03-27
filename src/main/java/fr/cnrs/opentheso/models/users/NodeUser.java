package fr.cnrs.opentheso.models.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
