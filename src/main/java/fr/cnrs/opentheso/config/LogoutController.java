package fr.cnrs.opentheso.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class LogoutController {

    private final KeycloakProperties keycloakProps;

    public LogoutController(KeycloakProperties keycloakProps) {
        this.keycloakProps = keycloakProps;
    }

    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws IOException {
        String logoutUrl = String.format(
                "%s/realms/%s/protocol/openid-connect/logout?client_id=%s&redirect_uri=%s",
                keycloakProps.getServerUrl(),
                keycloakProps.getRealm(),
                keycloakProps.getClientId(),
                keycloakProps.getPostLogoutRedirectUri()
        );
        response.sendRedirect(logoutUrl);
    }
}
