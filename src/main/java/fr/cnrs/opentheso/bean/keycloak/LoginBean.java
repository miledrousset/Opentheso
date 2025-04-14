package fr.cnrs.opentheso.bean.keycloak;

import fr.cnrs.opentheso.config.KeycloakConfigProperties;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Named("loginBean")
@RequestScoped
public class LoginBean implements Serializable {

    @Inject
    private KeycloakConfigProperties keycloakConfig;

    public String getKeycloakUrl() {
        String baseUrl = keycloakConfig.getBaseUrl();
        String clientId = keycloakConfig.getClientId();
        String redirectUri = keycloakConfig.getRedirectUri();

        return baseUrl +
                "/protocol/openid-connect/auth?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
    }
}
