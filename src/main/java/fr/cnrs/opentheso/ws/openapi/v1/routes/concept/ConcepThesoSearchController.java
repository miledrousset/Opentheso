package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;
import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;


@Slf4j
@RestController
@RequestMapping("/openapi/v1/concept/{idTheso}/search")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Concept", description = "Contient toutes les actions disponibles sur les concepts.")
public class ConcepThesoSearchController {

    @Autowired
    private Connect connect;

    @Autowired
    private GroupHelper groupHelper;

    @Autowired
    private RestRDFHelper restRDFHelper;


    @GetMapping(produces = {APPLICATION_RDF_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(summary = "Permet de rechercher un concept en filtrant par theso et par langue",
            description = "Ancienne version : `/api/search?q={input}&theso={idTheso}`\n<br>Effectue une recherche du concept à l'aide d'une valeur avec possibilité de filtrer par thesaurus et langue.<br>Il est possible de sélectionner le format souhaité parmis :\n<br>- RDF\n<br>- JSON-LD\n<br>- JSON\n<br>- TURTLE",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier contenent le résultat de la recherche", content = {
                            @Content(mediaType = APPLICATION_RDF_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            })
    public ResponseEntity<Object> search(@Parameter(name = "idTheso", description = "ID du thesaurus dans lequel chercher", required = true) @PathVariable("idTheso") String idTheso,
                           @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "L'entrée correspondant au terme recherché") @RequestParam("q") String q,
                           @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), description = "Langue dans laquelle on recherche") @RequestParam(value = "lang", required = false) String lang,
                           @Parameter(name = "group", in = ParameterIn.QUERY, schema = @Schema(type = "string"), description = "Groupe dans lequel on effectue la recherche") @RequestParam(value = "group", required = false) String group,
                           @Parameter(name = "match", in = ParameterIn.QUERY, schema = @Schema(type = "string", allowableValues = {"exact", "exactone"}), description = "-`exact` limitera la recherche aux termes exacts.\n<br>-`exactone` limitera la recherche aux termes exacts mais en envoyant une seule réponse.") @RequestParam(value = "match", required = false) String match,
                           @RequestHeader(value = "accept", required = false) String format) {

        return searchFilter(idTheso, format, q, lang, group, match, null);
    }


    @GetMapping(value = "/notation", produces = {APPLICATION_RDF_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(summary = "Permet de rechercher un concept par notation",
            description = "Ancienne version : `/api/search?theso=<idTheso>&q=notation:<input>`\\n\\nEffectue une recherche par notation avec possibilité de filtrer par langue ou groupe.",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier contenent le résultat de la recherche", content = {
                            @Content(mediaType = APPLICATION_RDF_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            })
    public ResponseEntity<Object> searchNotation(@Parameter(name = "idTheso", description = "ID du thesaurus dans lequel chercher", required = true) @PathVariable("idTheso") String idTheso,
                                   @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "L'entrée correspondant au terme recherché") @RequestParam("q") String q,
                                   @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), description = "Langue dans laquelle on recherche") @RequestParam(value = "lang", required = false) String lang,
                                   @Parameter(name = "group", in = ParameterIn.QUERY, schema = @Schema(type = "string"), description = "Groupe dans lequel on effectue la recherche") @RequestParam(value = "group", required = false) String group,
                                   @Parameter(name = "match", in = ParameterIn.QUERY, schema = @Schema(type = "string", allowableValues = {"exact", "exactone"}), description = "-`exact` limitera la recherche aux termes exacts.\n<br>-`exactone` limitera la recherche aux termes exacts mais en envoyant une seule réponse.") @RequestParam(value = "match", required = false) String match,
                                                 @RequestHeader(value = "accept", required = false) String format) {
        return searchFilter(idTheso, format, q, lang, group, match, "notation:");
    }

    private ResponseEntity searchFilter(String idTheso, String format, String q, String lang, String groupsString, String match, String filter) {

        String datas;
        String[] groups = null;

        if (lang == null) {
            lang = "";
        }

        if (groupsString != null) {
            groups = groupsString.split(",");
        }

        if (match != null && !match.equalsIgnoreCase("exact") && !match.equalsIgnoreCase("exactone")) {
            match = null;
        }

        datas = getDatas(connect.getPoolConnexion(), idTheso, lang, groups, q, format, filter, match);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(format)).body(datas);
    }


    @GetMapping(value = "/fullpath", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${searchJsonForWidget.summary}$",
            description = "${searchJsonForWidget.description}$",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier contenent le résultat de la recherche", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            })
    public ResponseEntity<Object> searchJsonForWidget(
            @Parameter(name = "idTheso", description = "ID du thesaurus dans lequel chercher", required = true, example = "th3") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${searchJsonForWidget.q.description}$", example = "Lyon") @RequestParam(value = "q", required = false) String q,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "Langue dans laquelle on recherche", example = "fr") @RequestParam(value = "lang", required = false) String lang,
            @Parameter(name = "group", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${searchJsonForWidget.group.description}$") @RequestParam(value = "group", required = false) String groupStrings,
            @Parameter(name = "arkgroup", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${searchJsonForWidget.arkgroup.description}$") @RequestParam(value = "arkgroup", required = false) String arkgroupStrings,
            @Parameter(name = "full", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), required = false, description = "${searchJsonForWidget.full.description}$") @RequestParam(value = "full", required = false) String fullString,
            @Parameter(name = "exactMatch", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), required = false, description = "${searchJsonForWidget.exactMatch.description}$") @RequestParam(value = "exactMatch", required = false) String exactMatchString
    ) {
        boolean full = fullString != null && fullString.equalsIgnoreCase("true");
        String[] groups = null;
        if (groupStrings != null) {
            groups = groupStrings.split(",");
        }
        boolean exactMatch = exactMatchString != null && exactMatchString.equalsIgnoreCase("true");

        if (arkgroupStrings != null) {
            groups = getIdGroupFromArk(arkgroupStrings.split(","), idTheso);
        }

        var fullFormat = full ? "full" : null;
        var datas = restRDFHelper.findDatasForWidget(connect.getPoolConnexion(), idTheso, lang, groups, q, HeaderHelper.removeCharset(fullFormat), exactMatch);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(Objects.requireNonNullElseGet(datas, () -> emptyMessage(APPLICATION_JSON_UTF_8)));
    }

    private String[] getIdGroupFromArk(String[] arkGroups, String idTheso) {
        String[] groups = new String[arkGroups.length];
        int i = 0;
        for (String arkGroup : arkGroups) {
            groups[i] = groupHelper.getIdGroupFromArkId(connect.getPoolConnexion(), arkGroup, idTheso);
            i++;
        }
        return groups;
    }

    private String getDatas(HikariDataSource ds,
                                   String idTheso, String idLang,
                                   String [] groups,
                                   String value,
                                   String format, String filter,
                                   String match) {

        format = HeaderHelper.removeCharset(format);
        if (filter != null && filter.equalsIgnoreCase("notation:")) {
            return restRDFHelper.findNotation(ds, idTheso, value, format);
        }
        return restRDFHelper.findConcepts(ds, idTheso, idLang, groups, value, format, match);
    }
}

