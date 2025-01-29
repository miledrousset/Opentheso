package fr.cnrs.opentheso.ws.openapi.v1.routes;

import fr.cnrs.opentheso.repositories.UserHelper;

import fr.cnrs.opentheso.utils.MD5Password;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;
import fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.Json;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Web Service
 *
 * @author Julien LINGET
 */

@Slf4j
@RestController
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Test Authentification", description = "Permet de vérifier si l'API est fonctionnelle.")
public class OpenApiController {

    @Autowired
    private ApiKeyHelper apiKeyHelper;

    @Autowired
    private UserHelper userHelper;

    @GetMapping(value = "/Auth", produces = CustomMediaType.APPLICATION_JSON_UTF_8)
    @Operation(
            summary = "Vérification du système d'authentification",
            description = "Cette méthode vérifie si le système d'authentification est fonctionnel en utilisant une clé API. " +
                    "Elle renvoie un statut indiquant si la clé API est valide ou non, ainsi que les rôles associés à l'utilisateur.",
            tags = {"Test"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "La clé API est valide et les rôles de l'utilisateur sont renvoyés.",
                            content = @Content(mediaType = CustomMediaType.APPLICATION_JSON_UTF_8,
                                    examples = @ExampleObject(value = "{\"valid\":true,\"key\":\"CLE-API-EXEMPLE\",\"Roles\":[\"admin\"]}"))),
                    @ApiResponse(responseCode = "401", description = "Aucune clé API fournie ou clé API expirée",
                            content = @Content(mediaType = CustomMediaType.APPLICATION_JSON_UTF_8,
                                    examples = @ExampleObject(value = "No API key given"))),
                    @ApiResponse(responseCode = "403", description = "Clé API invalide",
                            content = @Content(mediaType = CustomMediaType.APPLICATION_JSON_UTF_8,
                                    examples = @ExampleObject(value = "API key is invalid"))),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur",
                            content = @Content(mediaType = CustomMediaType.APPLICATION_JSON_UTF_8,
                                    examples = @ExampleObject(value = "Server internal error"))),
                    @ApiResponse(responseCode = "503", description = "Service non disponible (base de données non accessible)",
                            content = @Content(mediaType = CustomMediaType.APPLICATION_JSON_UTF_8,
                                    examples = @ExampleObject(value = "Database unavailable")))
            },
            security = @SecurityRequirement(name = "API-KEY")
    )
    public ResponseEntity<Object> testAuth(@RequestHeader(value = "API-KEY") String apiKey)  {

        var keyState = apiKeyHelper.checkApiKey(MD5Password.getEncodedPassword(apiKey));
        if (keyState != ApiKeyState.VALID) {
            return errorResponse(keyState);
        }

        var builder = Json.createObjectBuilder();
        builder.add("valid", true);
        builder.add("key", apiKey);

        var userId = apiKeyHelper.getIdUser(apiKey);
        var userGroupId = userHelper.getUserGroupId(userId, apiKey);
        var roleId = userHelper.getRoleOnThisTheso(userId, userGroupId.orElse(0), "th2");
        builder.add("Roles", roleId);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(builder.build().toString());
    }

    public ResponseEntity<Object> errorResponse(ApiKeyState state) {
        int code = 0;
        String msg = switch (state) {
            case EMPTY -> {
                code = Response.Status.UNAUTHORIZED.getStatusCode();
                yield "No API key given";
            }
            case DATABASE_UNAVAILABLE -> {
                code = Response.Status.SERVICE_UNAVAILABLE.getStatusCode();
                yield "Database unavailable";
            }
            case INVALID -> {
                code = Response.Status.FORBIDDEN.getStatusCode();
                yield "API key is invalid";
            }
            case SQL_ERROR -> {
                code = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
                yield "Server internal error";
            }
            case EXPIRED -> {
                code = Response.Status.UNAUTHORIZED.getStatusCode();
                yield "API key is expired";
            }
            default -> null;
        };

        return ResponseEntity.status(code).contentType(MediaType.APPLICATION_JSON).body(msg);
    }
}
