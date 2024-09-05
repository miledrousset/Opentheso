package fr.cnrs.opentheso.ws.openapi.v1.routes.group;


import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.models.group.NodeGroupTraductions;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
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
@RequestMapping("/group/{idTheso}")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Group", description = "4. Contient les actions en lien avec les groupes.")
public class GroupThesoController {

    @Autowired
    private Connect connect;

    @Autowired
    private GroupHelper groupHelper;


    @GetMapping(produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "Récupère toutes les collections et sous collections d'un thésaurus",
            description = "Ancienne version : `/api/info/list?theso=<idTheso>&group=all`<br/>Permet de récupérer toutes les collections et sous collections d'un thésaurus au format JSON",
            tags = {"Group"},
            responses = { @ApiResponse(responseCode = "200", description = "Fichier JSON contenant les collections d'un thésaurus", content = { @Content(mediaType = APPLICATION_JSON_UTF_8)}), @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")})
    public ResponseEntity<Object>  getAllGroupsFromTheso(@Parameter(name = "idTheso", description = "Thésaurus pour lequel on veut récupérer les groupes", schema = @Schema(type = "string")) @PathVariable("idTheso") String idTheso) {

        ArrayList<NodeGroupTraductions> nodeGroupTraductions;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        List<String> listIdGroupOfTheso = groupHelper.getListIdOfGroup(connect.getPoolConnexion(), idTheso);

        for (String idGroup : listIdGroupOfTheso) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("idGroup", idGroup);
            JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

            nodeGroupTraductions = groupHelper.getAllGroupTraduction(connect.getPoolConnexion(), idGroup, idTheso);
            for (NodeGroupTraductions nodeGroupTraduction : nodeGroupTraductions) {
                JsonObjectBuilder jobLang = Json.createObjectBuilder();
                jobLang.add("lang", nodeGroupTraduction.getIdLang());
                jobLang.add("title", nodeGroupTraduction.getTitle());
                jsonArrayBuilderLang.add(jobLang.build());
            }
            if (!nodeGroupTraductions.isEmpty()) {
                job.add("labels", jsonArrayBuilderLang.build());
            }
            jsonArrayBuilder.add(job.build());
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonArrayBuilder.build().toString());
    }


    @GetMapping(value = "/{idGroup}", produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(
            summary = "Permet de récupérer les informations d'un groupe à partir de son identifiant interne",
            description = "Ancienne version : `/api/info/list?theso={idTheso}&group={idGroup}`<br/>Recherche les informations d'un groupe à partir de son identifiant",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Information du groupe", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "Groupe non trouvé"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getGroupFromIdThesoIdGroup(
            @Parameter(name = "idTheso", required = true, description = "Identifiant du thesaurus") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idGroup", required = true, description = "Identifiant interne du groupe") @PathVariable("idGroup") String idGroup,
            @RequestHeader(value = "accept", required = false) String acceptHeader) {


        var datas = new RestRDFHelper().exportGroup(connect.getPoolConnexion(), idTheso, idGroup, removeCharset(acceptHeader));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(acceptHeader)).body(datas);
    }

    @GetMapping(value = "/{idGroup}/subgroup", produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(
            summary = "Récupère les sous-collections d'une collection dans un thésaurus",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier JSON contenant les sous collections d'un thésaurus", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "Groupe non trouvé"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getSubGroupFromIdThesoIdGroup(
            @Parameter(name = "idTheso", required = true, description = "Thésaurus pour lequel on veut récupérer les collections") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idGroup", required = true, description = "Identifiant interne du groupe") @PathVariable("idGroup") String idGroup) {

        ArrayList<NodeGroupTraductions> nodeGroupTraductions;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        List<String> listIdSubGroupOfTheso = groupHelper.getListGroupChildIdOfGroup(connect.getPoolConnexion(), idGroup, idTheso);

        for (String idSubGroup : listIdSubGroupOfTheso) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("idGroup", idSubGroup);
            JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

            nodeGroupTraductions = groupHelper.getAllGroupTraduction(connect.getPoolConnexion(), idSubGroup, idTheso);
            for (NodeGroupTraductions nodeGroupTraduction : nodeGroupTraductions) {
                JsonObjectBuilder jobLang = Json.createObjectBuilder();
                jobLang.add("lang", nodeGroupTraduction.getIdLang());
                jobLang.add("title", nodeGroupTraduction.getTitle());
                jsonArrayBuilderLang.add(jobLang.build());
            }
            if (!nodeGroupTraductions.isEmpty()) {
                job.add("labels", jsonArrayBuilderLang.build());
            }
            jsonArrayBuilder.add(job.build());
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonArrayBuilder.build().toString());
    }


    @GetMapping(value = "/branch", produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(
            summary = "Permet de récupérer toute une branche de groupes à partir des identifiants internes",
            description = "Ancienne version : `/api/all/group?id={idGroups}&theso={idTheso}&format={format}`<br/>Récupère une branche d'un groupe à partir de son identifiant",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Information de la branche du groupe", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "404", description = "Groupe non trouvé"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            }
    )
    public ResponseEntity<Object> getAllBranchOfGroup(
            @Parameter(name = "idTheso", required = true, description = "Identifiant du thésaurus") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idGroups", required = true, description = "Identifiants internes des groupes séparés par une virgule", example = "g1,g2,g3") @RequestParam("idGroups") String idGroups,
            @RequestHeader(value = "accept", required = false) String acceptHeader) {

        String[] groups = idGroups.split(",");
        if (groups.length == 0) {
            return ResponseEntity.badRequest().contentType(MediaType.parseMediaType(acceptHeader)).body("No group id");
        }
        
        var datas = new RestRDFHelper().brancheOfGroup(connect.getPoolConnexion(), groups, idTheso, removeCharset(acceptHeader));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(acceptHeader)).body(datas);
    }


    @GetMapping(value = "/branchtree", produces = APPLICATION_JSON_UTF_8)
    @Operation(
            summary = "Permet de récupérer toute une branche de groupes avec le chemin complet vers la racine",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Information de la branche du groupe", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "404", description = "Groupe non trouvé"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            }
    )
    public ResponseEntity<Object> getAllBranchOfGroupAsTree(
            @Parameter(name = "idTheso", required = true, description = "Identifiant du thésaurus") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "Langue dans laquelle chercher la saisie de l'utilisateur") @RequestParam("lang") String lang,
            @Parameter(name = "idGroups", required = true, description = "Identifiants internes des groupes séparés par une virgule", example = "g1,g2,g3") @RequestParam("idGroups") String idGroups,
            @RequestHeader(value = "accept", required = false) String acceptHeader) {

        String[] groups = idGroups.split(",");
        if (groups.length == 0) {
            return ResponseEntity.badRequest().contentType(MediaType.parseMediaType(acceptHeader)).body("No group id");
        }

        var datas = new RestRDFHelper().brancheOfGroupAsTree(connect.getPoolConnexion(), groups, idTheso, lang);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(acceptHeader)).body(datas);
    }    
    
}
