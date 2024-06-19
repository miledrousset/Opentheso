package fr.cnrs.opentheso.ws.openapi.v1.routes.conceptpost;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.CandidateHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;
import fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;


@Path("/candidate")
public class addCandidateController {
    /**
     * Route qui permet d'ajouter un candidat à partir d'un JSON
     * @param headers
     * @param candidate
     * @return
     */
    @Path("/add")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "${addCandidate.summary}$",
            description = "${addCandidate.description}$",
            tags = {"Concept Write"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON représentant le candidat à ajouter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Candidate.class),
                            examples = @ExampleObject(
                                    name = "${addCandidate.schemaTitle}$",
                                    value = "{ \"title\": \"Sample Title\", \"definition\": \"Sample Description\", \"thesoId\": \"th2\", \"source\": \"Sample Source\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Allo", content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON)
                    }),
                    @ApiResponse(responseCode = "400", description = "400"),
                    @ApiResponse(responseCode = "404", description = "404"),
                    @ApiResponse(responseCode = "503", description = "503")
            },
            security = {
                    @SecurityRequirement(name = "API-KEY")
            }
    )
    public Response addCandidate(@Context HttpHeaders headers, String candidate) {
        CandidateHelper candidateHelper = new CandidateHelper();    // Pour la fonction saveCandidat
        ApiKeyHelper apiKeyHelper = new ApiKeyHelper();             // Pour l'état de l'APIkey, certaines réponses HTTP et retrouver l'utilisateur propriétaire de la clé
        UserHelper userHelper = new UserHelper();                   // Pour retrouver le rôle de l'utilisateur sur un théso
        String apiKey = headers.getHeaderString("API-KEY");
        ApiKeyState keyState = apiKeyHelper.checkApiKey(apiKey);
        ObjectMapper objectMapper = new ObjectMapper();



        if (keyState != ApiKeyState.VALID){return apiKeyHelper.errorResponse(keyState);}
        try (HikariDataSource ds = connect()) {
            int userId = apiKeyHelper.getIdUser(apiKey);
            if (ds == null) {
                return ResponseHelper.response(Response.Status.NOT_FOUND, null, CustomMediaType.APPLICATION_JSON_UTF_8);
            }

            int roleId = userHelper.getRoleOnThisTheso(ds, userId, userHelper.getUserGroupId(userId, "th2").orElse(0), "th2" );

            if (roleId == -1) {
                return ResponseHelper.createStatusResponse(Response.Status.FORBIDDEN, "Unauthorized");
            } else {
                Map<String, Object> successResponse = new HashMap<>();
                try {
                    boolean saveStatus = candidateHelper.saveCandidat(candidate, userId);
                    if (!saveStatus){return ResponseHelper.createStatusResponse(Response.Status.BAD_REQUEST, "Bad JSON format.");}
                    JsonNode candidateJson = objectMapper.readTree(candidate);
                    successResponse.put("candidate", candidateJson);
                } catch (IOException e) {
                    throw new RuntimeException("Error parsing candidate JSON", e);
                }
                return ResponseHelper.createJsonResponse(Response.Status.OK, successResponse);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    public class Candidate {

        @Schema(description = "Title of the candidate", example = "Sample Title", required = true)
        public String title;

        @Schema(description = "Description or definition of the candidate", example = "Sample Description", required = true)
        public String description;

        @Schema(description = "Thesaurus ID", example = "th123", required = true)
        public String thesoId;

        @Schema(description = "Source of the candidate", example = "Sample Source", required = false)
        public String source;
    }
}
