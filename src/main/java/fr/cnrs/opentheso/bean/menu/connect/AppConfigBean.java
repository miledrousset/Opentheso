package fr.cnrs.opentheso.bean.menu.connect;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Named("appConfigBean") // Nom utilisé dans la vue JSF
@ApplicationScoped
@Component
public class AppConfigBean {

    @Value("${app.security.keycloak-enabled:false}")
    private boolean keycloakEnabled;

    public boolean isKeycloakEnabled() {
        return keycloakEnabled;
    }
}