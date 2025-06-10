package fr.cnrs.opentheso.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.keycloak")
public class KeycloakProperties {

    private String realm;
    private String serverUrl;
    private String postLogoutRedirectUri;
    private String clientId;
}
