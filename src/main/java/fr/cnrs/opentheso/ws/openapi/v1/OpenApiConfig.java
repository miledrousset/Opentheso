package fr.cnrs.opentheso.ws.openapi.v1;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.ext.Provider;

/**
 * @author julie
 */
@Provider
@OpenAPIDefinition(
        info = @Info(
                title = "Opentheso2",
                description = "${definition.description}$",
                version = "v1"
        ),
        tags = {
                @Tag(name = "Test", description = "${definition.tags.test.description}$"),
                @Tag(name = "Concept", description = "${definition.tags.concept.description}$"),
                @Tag(name = "Thesaurus", description = "${definition.tags.thesaurus.description}$"),
                @Tag(name = "Group", description = "${definition.tags.group.description}$"),
                @Tag(name = "Ark", description = "${definition.tags.ark.description}$"),
                @Tag(name = "Ontome", description = "${definition.tags.ontome.description}$")
        },
        servers = {
            @Server(url = "${BASE_SERVER}$")
        }
)
@SecurityScheme(name = "CLE-API-EXEMPLE", paramName = "CLE-API-EXEMPLE", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
public interface OpenApiConfig {

}
