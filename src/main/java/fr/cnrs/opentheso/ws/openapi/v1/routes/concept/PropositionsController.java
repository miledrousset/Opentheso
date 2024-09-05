package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cnrs.opentheso.bdd.helper.PropositionApiHelper;
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
@RequestMapping("/concepts/propositions")
@CrossOrigin(methods = { RequestMethod.POST })
@Tag(name = "Proposition", description = "Ajouter une proposition")
public class PropositionsController {

    @Autowired
    private Connect connect;


    @PostMapping(consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<Object> createProposition(@RequestHeader(value = "API-KEY") String apiKey,
                                            @RequestBody String propositionJson) throws JsonProcessingException {

        var apiKeyHelper = new ApiKeyHelper();
        var keyState = apiKeyHelper.checkApiKey(connect.getPoolConnexion(), apiKey);

        if (keyState != ApiKeyState.VALID) {
            return apiKeyHelper.errorResponse(keyState);
        }

        var proposition = new ObjectMapper().readValue(propositionJson, PropositionFromApi.class);
        var userId = apiKeyHelper.getIdUser(connect.getPoolConnexion(), apiKey);

        new PropositionApiHelper().createProposition(connect.getPoolConnexion(), proposition, userId);
        return ResponseEntity.status(201).build();
    }
}
