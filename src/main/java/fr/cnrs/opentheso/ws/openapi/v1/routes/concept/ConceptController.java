package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.QueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;

/**
 * @author julie
 */
@Slf4j
@RestController
@RequestMapping("/openapi/v1/concept/ark/fullpath/search")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Concept", description = "Contient toutes les actions disponibles sur les concepts.")
public class ConceptController {

    @Autowired
    private RestRDFHelper restRDFHelper;


    @GetMapping(produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "Permet d'obtenir le chemin complet d'un concept à partir de son ID Ark",
            description = "Ancienne version : `/api/searchwidgetbyark?q={idArks}&lang={lang}`<br/>Permet d'obtenir le chemin complet d'un concept représenté dans un fichier JSON",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier contenant le résultat de la recherche", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            })
    public ResponseEntity searchJsonForWidgetArk(
            @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "ID Ark des concepts recherchés, séparé par des virgules", example = "66666/lkp6ure1g7b6,66666/lkubqlukv7i5") @QueryParam("q") String q,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "Code de la langue dans laquelle on recherche", example = "fr") @QueryParam("lang") String lang,
            @Parameter(name = "full", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), allowEmptyValue = true, description = "`true` si l'on souhaite aussi récupérer les traductions (`altLabels`), `false` sinon", example = "false") @QueryParam("full") Boolean full) {

        // Si 'full' est absent, il aura la valeur par défaut false
        boolean fullOption = (full != null) ? full : false;
        var fullFormat = fullOption ? "full" : null;

        String[] idArks = q.split(",");
        JsonArrayBuilder datas = restRDFHelper.findDatasForWidgetByArk(lang, idArks, fullFormat);

        return ResponseEntity.ok(datas.build());
    }
}
