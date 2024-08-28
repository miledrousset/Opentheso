package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.D3jsHelper;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;

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

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.removeCharset;


@Slf4j
@RestController
@RequestMapping("/concept/{idTheso}")
@CrossOrigin(methods = { RequestMethod.GET })
public class ConceptThesoController {

    @Autowired
    private Connect connect;


    @GetMapping(value = "/{idConcept}", produces = {APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "${getSkosFromidConcept.summary}$",
            description = "${getSkosFromidConcept.description}$",
            tags = {"Concept"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getSkosFromidConcept.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
            @Content(mediaType = APPLICATION_JSON_UTF_8),
            @Content(mediaType = APPLICATION_RDF_UTF_8)
        }),
                @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getSkosFromidConcept(@Parameter(name = "idTheso", description = "${getSkosFromidConcept.idTheso.description}$", required = true) @PathVariable("idTheso") String idThesaurus,
                                         @Parameter(name = "idConcept", description = "${getSkosFromidConcept.idConcept.description}$", required = true) @PathVariable("idConcept") String idConcept,
                                         @RequestHeader(value = "accept", required = false) String acceptHeader) {

        var datas = new RestRDFHelper().exportConceptFromId(connect.getPoolConnexion(), idConcept, idThesaurus, removeCharset(acceptHeader));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(acceptHeader)).body(datas);
    }

    @GetMapping(value = "/{idConcept}/labels", produces = {APPLICATION_JSON_UTF_8})
    @Operation(
            summary = "${getJsonFromIdConceptWithLabels.summary}$",
            description = "${getJsonFromIdConceptWithLabels.description}$",
            responses = {
                @ApiResponse(responseCode = "200", description = "${getJsonFromIdConceptWithLabels.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            },
            tags = {"Concept"}
    )
    public ResponseEntity<Object> getJsonFromIdConceptWithLabels(
            @Parameter(name = "idTheso", description = "${getJsonFromIdConceptWithLabels.idTheso.description}$", required = true) @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idConcept", description = "${getJsonFromIdConceptWithLabels.idConcept.description}$", required = true) @PathVariable("idConcept") String idConcept,
            @Parameter(name = "lang", description = "${getJsonFromIdConceptWithLabels.lang.description}$") @RequestParam(value = "lang", required = false, defaultValue = "fr") String lang) {

        var datas = new RestRDFHelper().getInfosOfConcept(connect.getPoolConnexion(), idTheso, idConcept, lang);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);

    }


    @GetMapping(value = "/{idConcept}/graph/", produces = APPLICATION_JSON_LD_UTF_8)
    @Operation(
            summary = "${getDatasForGraph.summary}$",
            description = "${getDatasForGraph.description}$",
            tags = {"Concept"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getDatasForGraph.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8)
        }),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$")
            }
    )
    public ResponseEntity<Object> getDatasForGraph(
            @Parameter(name = "idTheso", description = "${getDatasForGraph.idTheso.description}$", required = true) @PathVariable("idTheso") String idThesaurus,
            @Parameter(name = "idArk", description = "${getDatasForGraph.idArk.description}$", required = true) @PathVariable("idConcept") String idConcept,
            @Parameter(name = "lang", description = "${getDatasForGraph.lang.description}$", required = true) @RequestParam("lang") String lang) {

        var datas = new D3jsHelper().findDatasForGraph__(connect.getPoolConnexion(), idConcept, idThesaurus, lang);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }


    @GetMapping(value = "/thesoGraph", produces = APPLICATION_JSON_LD_UTF_8)
    @Operation(
            summary = "${getDatasForGraphByTheso.summary}$",
            description = "${getDatasForGraph.description}$",
            tags = {"Concept"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getDatasForGraph.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8)
        }),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$")
            }
    )
    public ResponseEntity<Object> getDatasForGraphForThisTheso(
            @Parameter(name = "idTheso", description = "${getDatasForGraph.idTheso.description}$", required = true) @PathVariable("idTheso") String idThesaurus,
            @Parameter(name = "lang", description = "${getDatasForGraph.lang.description}$", required = true) @RequestParam("lang") String lang) {

        var datas = new D3jsHelper().findDatasForGraph__(connect.getPoolConnexion(), null, idThesaurus, lang);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }    


    @GetMapping(value = "/{idConcept}/expansion", produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_RDF_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(
            summary = "${getBrancheOfConcepts.summary}$",
            description = "${getBrancheOfConcepts.description}$",
            responses = {
                @ApiResponse(responseCode = "200", description = "${getBrancheOfConcepts.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8),
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
            @Content(mediaType = APPLICATION_RDF_UTF_8),
            @Content(mediaType = APPLICATION_TURTLE_UTF_8)}),
                @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            },
            tags = {"Concept"}
    )
    public ResponseEntity<Object> getBrancheOfConcepts(
            @Parameter(name = "idTheso", description = "${getBrancheOfConcepts.idTheso.description}$", required = true, example = "th3") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idConcept", description = "${getBrancheOfConcepts.idConcept.description}$", required = true, example = "3") @PathVariable("idConcept") String idConcept,
            @Parameter(name = "way", description = "${getBrancheOfConcepts.way.description}$", required = true, schema = @Schema(type = "string", allowableValues = {"top", "down"})) @RequestParam("way") String way,
            @RequestHeader(value = "accept", required = false) String acceptHeader) {
        
        var datas = getBranchOfConcepts(idConcept, idTheso, way, removeCharset(acceptHeader));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(acceptHeader)).body(datas);
    }

    private String getBranchOfConcepts(String idConcept, String idTheso, String way, String format) {
        if (way.equalsIgnoreCase("top")) {
            return new RestRDFHelper().brancheOfConceptsTop(connect.getPoolConnexion(), idConcept, idTheso, format);
        } else {
            // sens de récupération des concepts vers le bas
            return new RestRDFHelper().brancheOfConceptsDown(connect.getPoolConnexion(), idConcept, idTheso, format);
        }
    }


    @GetMapping(value = "/{idConcept}/narrower/{lang}", produces = APPLICATION_JSON_UTF_8)
    @Operation(
            summary = "${getNarrower.summary}$",
            description = "${getNarrower.description}$",
            responses = {
                @ApiResponse(responseCode = "200", description = "${getNarrower.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)}),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            },
            tags = {"Concept"}
    )
    public ResponseEntity<Object> getNarrower(
            @Parameter(name = "idTheso", description = "${getNarrower.idTheso.description}$", required = true, example = "th3") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idConcept", description = "${getNarrower.idConcept.description}$", required = true, example = "3") @PathVariable("idConcept") String idConcept,
            @Parameter(name = "lang", description = "${getNarrower.lang.description}$", required = true, example = "fr") @PathVariable("lang") String lang) {

        var datas = new RestRDFHelper().getNarrower(connect.getPoolConnexion(), idTheso, idConcept, lang);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }


    @GetMapping(value = "/fromdate/{date}", produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_RDF_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(
            summary = "${getConceptsFromDate.summary}$",
            description = "${getConceptsFromDate.description}$",
            responses = {
                @ApiResponse(responseCode = "200", description = "${getConceptsFromDate.200.description}$", content = {
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
            @Parameter(name = "idTheso", description = "${getConceptsFromDate.idTheso.description}$", required = true, example = "th3") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "date", description = "${getConceptsFromDate.date.description}$", required = true, schema = @Schema(type = "string", format = "date"), example = "2014-07-21") @PathVariable("date") String date,
            @RequestHeader(value = "accept", required = false) String format) {

        var datas = new RestRDFHelper().getIdConceptFromDate(connect.getPoolConnexion(), idTheso, date, removeCharset(format));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(format)).body(datas);
    }


    @GetMapping(value = "/ontome/{cidocClass}", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${getAllLinkedConceptsWithOntome.summary}$",
            description = "${getAllLinkedConceptsWithOntome.description}$",
            tags = {"Concept", "Ontome"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getAllLinkedConceptsWithOntome.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            })
    public ResponseEntity<Object> getAllLinkedConceptsWithOntome(
            @Parameter(name = "idTheso", description = "${getAllLinkedConceptsWithOntome.idTheso.description}$", required = true, example = "th3") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "cidocClass", description = "${getAllLinkedConceptsWithOntome.cidocClass.description}$", required = true, example = "364") @PathVariable("cidocClass") String cidocClass
    ) {
        String datas;
        if (cidocClass == null || cidocClass.isEmpty()) {
            datas = new RestRDFHelper().getAllLinkedConceptsWithOntome__(connect.getPoolConnexion(), idTheso);
        } else {
            datas = new RestRDFHelper().getLinkedConceptWithOntome__(connect.getPoolConnexion(), idTheso, cidocClass);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }

}
