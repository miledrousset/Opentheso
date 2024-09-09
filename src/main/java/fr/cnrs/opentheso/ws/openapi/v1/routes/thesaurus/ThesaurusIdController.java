package fr.cnrs.opentheso.ws.openapi.v1.routes.thesaurus;

import fr.cnrs.opentheso.repositories.ConceptHelper;

import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Thesaurus", description = "Contient toutes les actions en liens avec les thesaurus.")
public class ThesaurusIdController {

    @Autowired
    private Connect connect;

    @Autowired
    private TermHelper termHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private RestRDFHelper restRDFHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;


    @GetMapping(produces = {APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "Permet de  récupérer un thesaurus entier",
            description = "Ancienne version : `/api/all/theso?id=<idTheso>&format=<format>`\\n\\nRécupère entièrement le thesaurus dont l'ID est spécifié.\\n**Attention : Si le thesaurus est gros, l'opération peut prendre beaucoup de temps**",
            tags = {"Thesaurus"},
            responses = {
                @ApiResponse(
                        responseCode = "200", description = "Renvoie un fichier contenant le thesaurus",
                        content = {
                                @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                                @Content(mediaType = APPLICATION_JSON_UTF_8),
                                @Content(mediaType = APPLICATION_RDF_UTF_8)
                        }),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur"),
                @ApiResponse(responseCode = "404", description = "Thésaurus non trouvé")
            })
    public ResponseEntity<Object> getThesoFromId(@Parameter(name = "thesaurusId", description = "Identifiant du thesaurus à récupérer", required = true) @PathVariable("thesaurusId") String thesaurusId,
                                         @RequestHeader(value = "accept", required = false) String format) {

        var datas = restRDFHelper.getTheso(connect.getPoolConnexion(), thesaurusId, HeaderHelper.removeCharset(format));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }


    @GetMapping(value = "/topconcept", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "Permet de récupérer les Top termes du thesaurus spécifié dans les langues disponibles",
            description = "Ancienne version : `/api/info/list?topconcept=all&theso=<idTheso>&lang=<lang>`<br/>Renvoie un fichier contenant les tops terms du thésaurus spécifié.<br/>Si aucune langue n'est spécifié, la réponse contiendra toutes les langues.",
            tags = {"Thesaurus"},
            responses = { @ApiResponse(responseCode = "200", description = "Renvoie un fichier contenant la liste des Top Concepts du thesaurus.", content = { @Content(mediaType = APPLICATION_JSON_UTF_8)}),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getThesoGroupsFromId(@Parameter(name = "thesaurusId", description = "Identifiant du thesaurus à récupérer", required = true) @PathVariable("thesaurusId") String thesaurusId) {

        var listIdTopConceptOfTheso = conceptHelper.getAllTopTermOfThesaurus(connect.getPoolConnexion(), thesaurusId);

        ArrayList<NodeTermTraduction> nodeTermTraductions;

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String idConcept : listIdTopConceptOfTheso) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("idConcept", idConcept);
            JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

            nodeTermTraductions = termHelper.getAllTraductionsOfConcept(connect.getPoolConnexion(), idConcept, thesaurusId);
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
    @Operation(summary = "Récupère la dernière date de modification d'un thesaurus",
            description = "Ancienne version : `/api/info/lastupdate?theso=<idTheso>`<br/>Permet de connaitre la date de la dernière modification d'un thesaurus",
            tags = {"Thesaurus"},
            responses = {
                @ApiResponse(responseCode = "200", description = "Renvoie la date de la dernière modification du thésaurus au format YYYY-MM-DD", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur"),
                @ApiResponse(responseCode = "404", description = "Thésaurus non trouvé")
            })
    public ResponseEntity<Object> getInfoLastUpdate(@Parameter(name = "thesaurusId", description = "Identifiant du thesaurus à récupérer.", required = true) @PathVariable("thesaurusId") String thesaurusId) {

        var date = conceptHelper.getLastModification(connect.getPoolConnexion(), thesaurusId);
        var datas = "{\"lastUpdate\":\"" + date.toString() + "\"}";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }


    @GetMapping(value = "/flatlist", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "Permet de  récupérer un thesaurus entier sous forme d'une liste JSON plate",
            description = "Ancienne version : `/api/jsonlist/theso?id=<idTheso>&lang=<lang>`<br/>Récupère entièrement le thesaurus dont l'ID est spécifié.<br/>**Attention : Si le thesaurus est gros, l'opération peut prendre beaucoup de temps**",
            tags = {"Thesaurus"},
            responses = {
                @ApiResponse(responseCode = "200", description = "Renvoie un fichier contenant le thesaurus", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur"),
                @ApiResponse(responseCode = "404", description = "Thésaurus non trouvé")
            })
    public ResponseEntity<Object> getThesoFromIdFlat(@Parameter(name = "thesaurusId", description = "Identifiant du thesaurus à récupérer", required = true) @PathVariable("thesaurusId") String thesaurusId,
            @Parameter(name = "lang", description = "Langue des termes à  récupérer.", required = true) @RequestParam(value = "lang", required = false, defaultValue = "fr") String lang) {

        var datas = restRDFHelper.getThesoIdValue(connect.getPoolConnexion(), thesaurusId, lang);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }

    @GetMapping(value = "/listlang", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "Récupère la liste des langues d'un thesaurus",
            description = "Ancienne version : `/api/info/listLang?theso=<idTheso>`<br/>Permet de connaitre la liste des langues d'un thesaurus",
            tags = {"Thesaurus"},
            responses = {
                @ApiResponse(responseCode = "200", description = "Renvoie la liste des langues d'un thésaurus au format Json", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur"),
                @ApiResponse(responseCode = "404", description = "Thésaurus non trouvé")
            })
    public ResponseEntity<Object> getListLang(@Parameter(name = "thesaurusId", description = "Identifiant du thesaurus", required = true) @PathVariable("thesaurusId") String thesaurusId) {

        ArrayList<String> listLangOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurus(connect.getPoolConnexion(), thesaurusId);
        JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();
        for (String idLang : listLangOfTheso) {
            JsonObjectBuilder jobLang = Json.createObjectBuilder();
            jobLang.add("lang", idLang);
            jsonArrayBuilderLang.add(jobLang.build());
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonArrayBuilderLang.build().toString());
    }

}
