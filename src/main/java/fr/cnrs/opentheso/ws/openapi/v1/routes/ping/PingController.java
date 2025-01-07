package fr.cnrs.opentheso.ws.openapi.v1.routes.ping;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;


@Slf4j
@RestController
@RequestMapping("/openapi/v1/ping")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Ping", description = "")
public class PingController {
    @GetMapping(produces = TEXT_PLAIN)
    @Operation(summary = "Permet de savoir si le Webservices fonctionne",
            tags = {"Ping"},
            description = "Permet de savoir si le Webservices fonctionne, doit retourner pong",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier contenant le résultat de la recherche", content = {
                            @Content(mediaType = TEXT_PLAIN),
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            })
    public ResponseEntity<Object> ping() {
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("pong");
    }        
}

