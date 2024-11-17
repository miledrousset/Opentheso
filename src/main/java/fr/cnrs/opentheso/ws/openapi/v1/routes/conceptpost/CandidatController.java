package fr.cnrs.opentheso.ws.openapi.v1.routes.conceptpost;

import fr.cnrs.opentheso.repositories.CandidateHelper;

import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;



@Slf4j
@RestController
@RequestMapping("/openapi/v1/candidate")
@CrossOrigin(methods = { RequestMethod.POST })
@Tag(name = "Candidat", description = "Ajouter des candidats")
public class CandidatController {

    @Autowired
    private CandidateHelper candidateHelper;

    @Autowired
    private ApiKeyHelper apiKeyHelper;


    /**
     * Route qui permet d'ajouter un candidat à partir d'un JSON
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Permet d'ajouter un candidat",
            description = "Permet d'ajouter un candidat à partir du JSON donné",
            tags = {"Concept Write"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON représentant le candidat à ajouter"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Allo", content = { @Content(mediaType = MediaType.APPLICATION_JSON)}),
                    @ApiResponse(responseCode = "400", description = "400"),
                    @ApiResponse(responseCode = "404", description = "404"),
                    @ApiResponse(responseCode = "503", description = "503")
            },
            security = { @SecurityRequirement(name = "API-KEY") }
    )
    public ResponseEntity addCandidate(@RequestHeader(value = "API-KEY") String apiKey, @RequestBody Candidate candidate) {

        var keyState = apiKeyHelper.checkApiKey(apiKey);

        if (keyState != ApiKeyState.VALID){
            return ResponseEntity.badRequest().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(apiKeyHelper.errorResponse(keyState));
        }

        var userId = apiKeyHelper.getIdUser(apiKey);

        if (!candidateHelper.saveCandidat(candidate, userId)){
            return ResponseEntity.badRequest().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body("");
        }

        return ResponseEntity.ok().body("");
    }

}
