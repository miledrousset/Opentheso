package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import fr.cnrs.opentheso.repositories.PathHelper;
import fr.cnrs.opentheso.ws.api.D3jsHelper;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.getMediaType;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.removeCharset;


@Slf4j
@RestController
@RequestMapping("/openapi/v1/path")
@CrossOrigin(methods = {RequestMethod.GET})
@Tag(name = "Concept", description = "Contient toutes les actions disponibles sur les concepts.")
public class ConceptPathController {

    @Autowired
    private PathHelper pathHelper;

    private static final String JSON_FORMAT = "application/json";
    private static final String JSON_FORMAT_LONG = JSON_FORMAT + ";charset=UTF-8";

    @GetMapping(value = "/{idTheso}/{idConcept}", produces = {APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "Récupère le chemin complet d'un concept jusqu'à la racine",
            description = "Récupère le chemin complet d'un concept jusqu'à la racine. Récupère aussi la poly-hiérarchie",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "404", description = "Aucun concept n'existe avec cet ID dans le thesaurus choisi"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getPathOfConcept(@Parameter(name = "idTheso", description = "ID du thesaurus dans lequel récupérer le concept.", required = true) @PathVariable("idTheso") String idThesaurus,
                                                       @Parameter(name = "idConcept", description = "Identifiant du concept à récupérer.", required = true) @PathVariable("idConcept") String idConcept) {

        var datas = pathHelper.getPathOfConcept(idConcept, idThesaurus);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }
}
