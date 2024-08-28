package fr.cnrs.opentheso.ws.openapi.v1.routes.thesaurus;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
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


@Slf4j
@RestController
@RequestMapping("/thesaurus/{thesaurusId}")
@CrossOrigin(methods = { RequestMethod.GET })
public class ThesaurusIdController {

    @Autowired
    private Connect connect;


    @GetMapping(produces = {APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "${getThesoFromId.summary}$",
            description = "${getThesoFromId.description}$",
            tags = {"Thesaurus"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getThesoFromId.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
            @Content(mediaType = APPLICATION_JSON_UTF_8),
            @Content(mediaType = APPLICATION_RDF_UTF_8)
        }),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur"),
                @ApiResponse(responseCode = "404", description = "${responses.theso.404.description}$")
            })
    public ResponseEntity<Object> getThesoFromId(@Parameter(name = "thesaurusId", description = "${getThesoFromId.thesaurusId.description}$", required = true) @PathVariable("thesaurusId") String thesaurusId,
                                         @RequestHeader(value = "accept", required = false) String format) {

        var datas = new RestRDFHelper().getTheso(connect.getPoolConnexion(), thesaurusId, HeaderHelper.removeCharset(format));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }


    @GetMapping(value = "/topconcept", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${getThesoGroupsFromId.summary}$",
            description = "${getThesoGroupsFromId.description}$",
            tags = {"Thesaurus"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getThesoGroupsFromId.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getThesoGroupsFromId(
            @Parameter(name = "thesaurusId", description = "${getThesoGroupsFromId.thesaurusId.description}$", required = true) @PathVariable("thesaurusId") String thesaurusId) {

        var listIdTopConceptOfTheso = new ConceptHelper().getAllTopTermOfThesaurus(connect.getPoolConnexion(), thesaurusId);

        ArrayList<NodeTermTraduction> nodeTermTraductions;

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String idConcept : listIdTopConceptOfTheso) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("idConcept", idConcept);
            JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

            nodeTermTraductions = new TermHelper().getAllTraductionsOfConcept(connect.getPoolConnexion(), idConcept, thesaurusId);
            for (NodeTermTraduction nodeTermTraduction : nodeTermTraductions) {
                JsonObjectBuilder jobLang = Json.createObjectBuilder();
                jobLang.add("lang", nodeTermTraduction.getLang());
                jobLang.add("title", nodeTermTraduction.getLexicalValue());
                jsonArrayBuilderLang.add(jobLang.build());
            }
            if (!nodeTermTraductions.isEmpty()) {
                job.add("labels", jsonArrayBuilderLang.build());
            }
            jsonArrayBuilder.add(job.build());
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonArrayBuilder.build().toString());
    }


    @GetMapping(value = "/lastupdate", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${getInfoLastUpdate.summary}$",
            description = "${getInfoLastUpdate.description}$",
            tags = {"Thesaurus"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getInfoLastUpdate.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur"),
                @ApiResponse(responseCode = "404", description = "${responses.theso.404.description}$")
            })
    public ResponseEntity<Object> getInfoLastUpdate(@Parameter(name = "thesaurusId", description = "${getInfoLastUpdate.thesaurusId.description}$", required = true) @PathVariable("thesaurusId") String thesaurusId) {

        var date = new ConceptHelper().getLastModification(connect.getPoolConnexion(), thesaurusId);
        var datas = "{\"lastUpdate\":\"" + date.toString() + "\"}";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }


    @GetMapping(value = "/flatlist", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${getThesoFromIdFlat.summary}$",
            description = "${getThesoFromIdFlat.description}$",
            tags = {"Thesaurus"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getThesoFromIdFlat.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur"),
                @ApiResponse(responseCode = "404", description = "${responses.theso.404.description}$")
            })
    public ResponseEntity<Object> getThesoFromIdFlat(@Parameter(name = "thesaurusId", description = "${getThesoFromIdFlat.thesaurusId.description}$", required = true) @PathVariable("thesaurusId") String thesaurusId,
            @Parameter(name = "lang", description = "${getThesoFromIdFlat.lang.description}$", required = true) @RequestParam(value = "lang", required = false, defaultValue = "fr") String lang) {

        var datas = new RestRDFHelper().getThesoIdValue(connect.getPoolConnexion(), thesaurusId, lang);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }

    @GetMapping(value = "/listlang", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${getListLang.summary}$",
            description = "${getListLang.description}$",
            tags = {"Thesaurus"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getListLang.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur"),
                @ApiResponse(responseCode = "404", description = "${responses.theso.404.description}$")
            })
    public ResponseEntity<Object> getListLang(@Parameter(name = "thesaurusId", description = "${getListLang.thesaurusId.description}$", required = true) @PathVariable("thesaurusId") String thesaurusId) {

        ArrayList<String> listLangOfTheso = new ThesaurusHelper().getAllUsedLanguagesOfThesaurus(connect.getPoolConnexion(), thesaurusId);
        JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();
        for (String idLang : listLangOfTheso) {
            JsonObjectBuilder jobLang = Json.createObjectBuilder();
            jobLang.add("lang", idLang);
            jsonArrayBuilderLang.add(jobLang.build());
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonArrayBuilderLang.build().toString());
    }

}
