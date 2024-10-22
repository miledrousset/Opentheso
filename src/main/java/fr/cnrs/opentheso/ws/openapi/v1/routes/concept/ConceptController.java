package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;

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
    private Connect connect;

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
    public ResponseEntity<Object> searchJsonForWidgetArk(
         /*   @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "ID Ark des concepts recherchés, séparé par des virgules", example = "66666/lkp6ure1g7b6,66666/lkubqlukv7i5") @QueryParam("q") String q,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "Code de la langue dans laquelle on recherche", example = "fr") @QueryParam("lang") String lang,
            @Parameter(name = "full", required = false, description = "= full, si l'on souhaite aussi  récupérer les traduction ('altLabels')") @PathVariable("full") String full
            */
            @Parameter(name = "arks", description = "ID du thesaurus dans lequel récupérer le concept", required = true) @RequestParam("arks") String arks,
            @Parameter(name = "lang", description = "Langue du concept à récupérer", required = true) @RequestParam("lang") String lang,
            @Parameter(name = "full", description = "Langue du concept à récupérer") @RequestParam("full") boolean fullbool
            ) {


        String[] idArks = arks.split(",");
        if (idArks.length == 0) {
         //   return ResponseHelper.response(Response.Status.BAD_REQUEST, "No term specified", APPLICATION_JSON_UTF_8);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(emptyMessage(APPLICATION_JSON_UTF_8));
        }
        if (lang == null) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(emptyMessage(APPLICATION_JSON_UTF_8));

        }
        String full= null;
        if(fullbool)
            full = "full";

        //var full = fullString != null && fullString.equalsIgnoreCase("true");
        if(StringUtils.isEmpty(full)){

        }
    //   var fullFormat = full ? "full" : null;*/
        var datas = restRDFHelper.findDatasForWidgetByArk(connect.getPoolConnexion(), lang, idArks, full);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    //    return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
    }
}
