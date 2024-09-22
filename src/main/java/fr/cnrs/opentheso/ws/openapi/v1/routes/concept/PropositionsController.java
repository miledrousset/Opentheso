package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import fr.cnrs.opentheso.repositories.PropositionApiHelper;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.models.propositions.PropositionFromApi;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;

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
@RequestMapping("/openapi/v1/concepts/propositions")
@CrossOrigin(methods = { RequestMethod.POST })
@Tag(name = "Proposition", description = "Ajouter une proposition")
public class PropositionsController {

    @Autowired
    private Connect connect;

    @Autowired
    private ApiKeyHelper apiKeyHelper;

    @Autowired
    private PropositionApiHelper propositionApiHelper;


    @PostMapping(consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<Object> createProposition(@RequestHeader(value = "API-KEY") String apiKey,
                                                    @RequestBody PropositionFromApi proposition) {

        var keyState = apiKeyHelper.checkApiKey(connect.getPoolConnexion(), apiKey);

        if (keyState != ApiKeyState.VALID) {
            return apiKeyHelper.errorResponse(keyState);
        }

        var userId = apiKeyHelper.getIdUser(connect.getPoolConnexion(), apiKey);

        propositionApiHelper.createProposition(connect.getPoolConnexion(), proposition, userId);
        return ResponseEntity.status(201).build();
    }
}
