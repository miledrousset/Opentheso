package fr.cnrs.opentheso.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collections;



@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Opentheso ", version = "v1"),
        security = @SecurityRequirement(name = "apiKey")
)
@SecurityScheme(
        name = "apiKey",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "API-KEY"
)
public class OpenApiConfig {

    @Bean
    public OpenApiCustomiser openApiCustomiser(HttpServletRequest request) {
        return openApi -> {
            var scheme = request.getScheme();  // Récupère "http" ou "https"
            var serverUrl = scheme + "://" + request.getServerName() + ":" + request.getServerPort();
            var server = new Server().url(serverUrl);
            openApi.setServers(Collections.singletonList(server));
        };
    }
}
