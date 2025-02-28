package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.removeCharset;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.getMediaType;


@Slf4j
@RestController
@RequestMapping("/openapi/v1/concept/{idTheso}")
@CrossOrigin(methods = {RequestMethod.GET})
@Tag(name = "Concept", description = "Contient toutes les actions disponibles sur les concepts.")
public class ConceptThesoController {

    @Autowired
    private D3jsHelper d3jsHelper;

    @Autowired
    private RestRDFHelper restRDFHelper;

    private static final String JSON_FORMAT = "application/json";
    private static final String JSON_FORMAT_LONG = JSON_FORMAT + ";charset=UTF-8";

    @GetMapping(value = "/{idConcept}", produces = {APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "Récupère un concept d'après son ID et le  récupérer dans un format spécifié",
            description = "Ancienne version : `/api/<idThesaurus>.<idConcept>.<format>`<br/>Permet de  récupérer un concept dans un thesaurus donné d'après son ID en spécifiant l'un des formats possibles :<br>- JSON<br>- JSON-LD<br>- RDF",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Skos décrivant le concept ayant l'ID correspondant", content = {
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "404", description = "Aucun concept n'existe avec cet ID dans le thesaurus choisi"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getSkosFromidConcept(@Parameter(name = "idTheso", description = "ID du thesaurus dans lequel récupérer le concept.", required = true) @PathVariable("idTheso") String idThesaurus,
                                                       @Parameter(name = "idConcept", description = "Identifiant du concept à récupérer.", required = true) @PathVariable("idConcept") String idConcept,
                                                       @RequestHeader(value = "accept", required = false) String acceptHeader) {

        // code qui permet de déctecter si le header vient d'un navigateur wab ou d'un client REST
        List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader);
        if (mediaTypes.size() > 1) {
            acceptHeader = "application/json";
        }
        var datas = restRDFHelper.exportConceptFromId(idConcept, idThesaurus, removeCharset(acceptHeader));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(acceptHeader)).body(datas);
    }

    @GetMapping(value = "/{idConcept}/labels", produces = {APPLICATION_JSON_UTF_8})
    @Operation(
            summary = "Récupère les labels d'un concept",
            description = "Ancienne version : `/api/{idTheso}.{idConcept}.labels`<br/>Permet de  récupérer les labels d'un concept d'un thesaurus donné d'après son ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JSON contenant les labels du concept ayant l'ID correspondant", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "Aucun concept n'existe avec cet ID dans le thesaurus choisi"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            },
            tags = {"Concept"}
    )
    public ResponseEntity<Object> getJsonFromIdConceptWithLabels(
            @Parameter(name = "idTheso", description = "ID du thesaurus à récupérer", required = true) @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idConcept", description = "ID du concept à récupérer", required = true) @PathVariable("idConcept") String idConcept,
            @Parameter(name = "lang", description = "Langue du concept à  récupérer") @RequestParam(value = "lang", required = false, defaultValue = "fr") String lang) {

        var datas = restRDFHelper.getInfosOfConcept(idTheso, idConcept, lang);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);

    }


    @GetMapping(value = "/{idConcept}/graph/", produces = APPLICATION_JSON_LD_UTF_8)
    @Operation(
            summary = "Permet d'obtenir les données pour l'affichage du graph D3js en partant d'un concept",
            description = "Ancienne version : `/api/graph?theso=<idTheso>&id=<idConcept>&lang=<lang>`<br/>s dans un format permettant l'affichage du graph D3js",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Données pour l'affichage du graph D3js", content = {
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "Aucun concept n'existe avec cet ID dans le thesaurus choisi")
            }
    )
    public ResponseEntity<Object> getDatasForGraph(
            @Parameter(name = "idTheso", description = "ID du thesaurus dans lequel récupérer le concept.", required = true) @PathVariable("idTheso") String idThesaurus,
            @Parameter(name = "idConcept", description = "ID du concept à récupérer", required = true) @PathVariable("idConcept") String idConcept,
            @Parameter(name = "lang", description = "Langue du concept à récupérer", required = true) @RequestParam("lang") String lang,
            @Parameter(name = "limit", example = "true", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), required = false, description = "pour limiter ou non le nombre de concepts à récupérer")
            @RequestParam(value = "limit", required = false, defaultValue = "false") Boolean limit) {

        var datas = d3jsHelper.findDatasForGraph__(idConcept, idThesaurus, lang, limit);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }


    @GetMapping(value = "/thesoGraph", produces = APPLICATION_JSON_LD_UTF_8)
    @Operation(
            summary = "Permet d'obtenir les données pour l'affichage du graph D3js pour tout le thésaurus",
            description = "Ancienne version : `/api/graph?theso=<idTheso>&id=<idConcept>&lang=<lang>`<br/>Données dans un format permettant l'affichage du graph D3js",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Données pour l'affichage du graph D3js", content = {
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "Aucun concept n'existe avec cet ID dans le thesaurus choisi")
            }
    )
    public ResponseEntity<Object> getDatasForGraphForThisTheso(
            @Parameter(name = "idTheso", description = "ID du thesaurus dans lequel récupérer le concept", required = true) @PathVariable("idTheso") String idThesaurus,
            @Parameter(name = "lang", description = "Langue du concept à récupérer", required = true) @RequestParam("lang") String lang,
            @Parameter(name = "limit", example = "true", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), required = false, description = "pour limiter ou non le nombre de concepts à récupérer")
            @RequestParam(value = "limit", required = false, defaultValue = "true") Boolean limit)
    {

        var datas = d3jsHelper.findDatasForGraph__(null, idThesaurus, lang, limit);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }


    @GetMapping(value = "/{idConcept}/expansion", produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_RDF_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(
            summary = "Récupère une branche d'expansion d'un concept",
            description = "Ancienne version : `/api/expansion/concept?theso=<idTheso>&id=<idConcept>&way=<top|down>`<br/>Permet de récupérer une branche d'expansion d'un concept d'un thésaurus donné d'après son ID. Soit en partant d'un concept pour trouver la racine, soit pour récupérer toute une branche à partir de la racine",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier contenant la branche du concept", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8)}),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "404", description = "Aucun concept n'existe avec cet ID dans le thesaurus choisi"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            },
            tags = {"Concept"}
    )
    public ResponseEntity<Object> getBrancheOfConcepts(
            @Parameter(name = "idTheso", description = "ID du thésaurus dans lequel récupérer le concept", required = true, example = "th3") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idConcept", description = "ID du concept à récupérer", required = true, example = "3") @PathVariable("idConcept") String idConcept,
            @Parameter(name = "way", description = "Sens de l'expansion, `top` si l'on veut trouver la racine, `down` si l'on veut récupèrer toute la branche à partir de la racine", required = true, schema = @Schema(type = "string", allowableValues = {"top", "down"})) @RequestParam("way") String way,
            @RequestHeader(value = "accept", required = false) String acceptHeader,
            @RequestParam(value = "format", required = false) String format
    ) {

        if (StringUtils.isNotEmpty(format)) {
            switch (format) {
                case "rdf": {
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(CustomMediaType.APPLICATION_RDF_UTF_8))
                            .body(getBranchOfConcepts(idConcept, idTheso, way, CustomMediaType.APPLICATION_RDF));
                }
                case "jsonld":
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(CustomMediaType.APPLICATION_JSON_LD_UTF_8))
                            .body(getBranchOfConcepts(idConcept, idTheso, way, CustomMediaType.APPLICATION_JSON_LD));
                case "turtle":
                    return ResponseEntity.ok()
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(getBranchOfConcepts(idConcept, idTheso, way, CustomMediaType.APPLICATION_TURTLE));
                default:
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(getBranchOfConcepts(idConcept, idTheso, way, JSON_FORMAT));
            }
        } else {
            var datas = getBranchOfConcepts(idConcept, idTheso, way, removeCharset(acceptHeader));
            return ResponseEntity.ok()
                    .contentType(getMediaType(acceptHeader)).body(datas);
        }
    }

    private String getBranchOfConcepts(String idConcept, String idTheso, String way, String format) {
        if (way.equalsIgnoreCase("top")) {
            return restRDFHelper.brancheOfConceptsTop(idConcept, idTheso, format);
        } else {
            // sens de récupération des concepts vers le bas
            return restRDFHelper.brancheOfConceptsDown(idConcept, idTheso, format);
        }
    }


    @GetMapping(value = "/{idConcept}/narrower/{lang}", produces = APPLICATION_JSON_UTF_8)
    @Operation(
            summary = "Permet de  récupérer la liste des termes spécifiques NT", //langueBean.getMsg("rest.getListNT"),//"
            description = "Ancienne version : `/api/narrower?theso=<idTheso>&id=<idConcept>&lang=<lang>`<br/>Permet de  récupérer la liste des termes spécifiques NT d'un concept d'un thesaurus donné d'après son ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des termes spécifiques NT", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)}),
                    @ApiResponse(responseCode = "404", description = "Aucun concept n'existe avec cet ID dans le thesaurus choisi"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            },
            tags = {"Concept"}
    )

    public ResponseEntity<Object> getNarrower(
            @Parameter(name = "idTheso", description = "ID du thesaurus dans lequel récupérer le concept", required = true, example = "th3") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idConcept", description = "ID du concept à récupérer", required = true, example = "3") @PathVariable("idConcept") String idConcept,
            @Parameter(name = "lang", description = "Langue du concept à  récupérer", required = true, example = "fr") @PathVariable("lang") String lang) {

        var datas = restRDFHelper.getNarrower(idTheso, idConcept, lang);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }


    @GetMapping(value = "/fromdate/{date}", produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_RDF_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(
            summary = "Permet de  récupérer la liste des concepts modifiés depuis une date donnée",
            description = "Ancienne version : `/api/getchangesfrom?theso=<idTheso>&date=<date>&format=<format>`<br/>Permet de  récupérer la liste des concepts modifiés depuis une date donnée",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des concepts modifiés depuis une date donnée", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8)
                    }),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            },
            tags = {"Concept"}
    )
    public ResponseEntity<Object> getConceptsFromDate(
            @Parameter(name = "idTheso", description = "ID du thesaurus dans lequel récupérer les concepts.", required = true, example = "th3") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "date", description = "Date de la dernière modification des concepts à récupérer à format YYYY-MM-DD", required = true, schema = @Schema(type = "string", format = "date"), example = "2014-07-21") @PathVariable("date") String date,
            @RequestHeader(value = "accept", required = false) String format) {

        var datas = restRDFHelper.getIdConceptFromDate(idTheso, date, removeCharset(format));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(format)).body(datas);
    }


    @GetMapping(value = "/ontome/{cidocClass}", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "Récupère tous les concepts du thésaurus qui ont une relation `exactMatch` avec les classes Cidoc",
            description = "Ancienne version : `/api/ontome/linkedConcept?theso=<idTheso>&class=<cidocClass>`<br>\\n\\nOpentheso permet de relier des classes Cidoc-CRM via la plateforme Ontome, le lien se fait grâce à une relation réciproque construite de cette manière :\\nDans Opentheso, si un concept de haut niveau correspond à une classe du Cidoc-CRM, on peut alors ajouter un alignement `exactMach` entre la classe Cidoc-CRM et le concept<br>\\nExemple :<br>\\nLe concept « Lieu géographique » (Geographical place) a un `exactMatch` avec « https://ontome.net/class/363 ».<br>Cette fonctionnalité récupérer tous les concepts du thésaurus qui ont une relation `exactMatch` avec les classes Cidoc",
            tags = {"Concept", "Ontome"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier JSON contenant les concepts", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            })
    public ResponseEntity<Object> getAllLinkedConceptsWithOntome(
            @Parameter(name = "idTheso", description = "Thésaurus dans lequel les concepts sont", required = true, example = "th3") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "cidocClass", description = "Classe Cidoc", required = true, example = "364") @PathVariable("cidocClass") String cidocClass
    ) {
        String datas;
        if (cidocClass == null || cidocClass.isEmpty()) {
            datas = restRDFHelper.getAllLinkedConceptsWithOntome__(idTheso);
        } else {
            datas = restRDFHelper.getLinkedConceptWithOntome__(idTheso, cidocClass);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }

}
