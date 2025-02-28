package fr.cnrs.opentheso.ws.openapi.v1.routes.users;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthentificationDto implements Serializable {

    @NotNull
    private String login;

    @NotNull
    private String password;
}
