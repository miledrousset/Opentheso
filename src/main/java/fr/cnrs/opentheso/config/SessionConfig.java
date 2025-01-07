package fr.cnrs.opentheso.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SessionConfig {

    @Value("${server.servlet.session.timeout}")
    private String sessionTimeout;

    public int getSessionTimeoutInMilliseconds() {
        // Convertit en millisecondes (IdleMonitor utilise des millisecondes)
        return (int) (parseTimeout(sessionTimeout) * 1000);
    }

    private long parseTimeout(String timeout) {
        // Gère différentes notations de durée (par ex. 1m, 60s, etc.)
        if (timeout.endsWith("m")) {
            return Long.parseLong(timeout.replace("m", "")) * 60;
        } else if (timeout.endsWith("s")) {
            return Long.parseLong(timeout.replace("s", ""));
        } else {
            return Long.parseLong(timeout); // valeur brute en secondes
        }
    }
}
