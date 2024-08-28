package fr.cnrs.opentheso.ws.openapi.v1.routes.conceptpost;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cnrs.opentheso.bdd.helper.CandidateHelper;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.ws.rs.core.MediaType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/concept/candidate")
@CrossOrigin(methods = { RequestMethod.POST })
public class addCandidateController {

    @Autowired
    private Connect connect;

    /**
     * Route qui permet d'ajouter un candidat à partir d'un JSON
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
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
                    @ApiResponse(responseCode = "200", description = "Allo", content = { @Content(mediaType = MediaType.APPLICATION_JSON)}),
                    @ApiResponse(responseCode = "400", description = "400"),
                    @ApiResponse(responseCode = "404", description = "404"),
                    @ApiResponse(responseCode = "503", description = "503")
            },
            security = { @SecurityRequirement(name = "API-KEY") }
    )
    public ResponseEntity<Object> addCandidate(@RequestHeader(value = "API-KEY") String apiKey,
                                       String candidate) throws JsonProcessingException, SQLException {

        var apiKeyHelper = new ApiKeyHelper();
        var keyState = apiKeyHelper.checkApiKey(connect.getPoolConnexion(), apiKey);

        if (keyState != ApiKeyState.VALID){
            return ResponseEntity.badRequest().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(apiKeyHelper.errorResponse(keyState));
        }

        var userId = apiKeyHelper.getIdUser(connect.getPoolConnexion(), apiKey);
        JsonNode candidateJson = new ObjectMapper().readTree(candidate);

        Map<String, Object> successResponse = new HashMap<>();
        if (!new CandidateHelper().saveCandidat(connect.getPoolConnexion(), candidate, userId)){
            return ResponseEntity.badRequest().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body("");
        }
        successResponse.put("candidate", candidateJson);
        return ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(successResponse);
    }

    public class Candidate {

        @Schema(description = "Title of the candidate", example = "Sample Title", requiredMode = Schema.RequiredMode.REQUIRED)
        public Element terme;

        @Schema(description = "Thesaurus ID", example = "th123", requiredMode = Schema.RequiredMode.REQUIRED)
        public String thesoId;

        @Schema(description = "collection ID", example = "g123", requiredMode = Schema.RequiredMode.REQUIRED)
        public String collectionId;

        @Schema(description = "Description or definition of the candidate", example = "Sample Description", requiredMode = Schema.RequiredMode.REQUIRED)
        public Element definition;

        @Schema(description = "Note of the candidate", example = "Sample note", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public Element note;

        @Schema(description = "Source of the candidate", example = "Sample Source", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public String source;

        @Schema(description = "lang", example = "fr", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public List<Element> synonymes;

        @Schema(description = "comment", example = "Simple Comment", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public String comment;
    }

    @Data
    public class Element {
        private String value;
        private String lang;
    }
}
