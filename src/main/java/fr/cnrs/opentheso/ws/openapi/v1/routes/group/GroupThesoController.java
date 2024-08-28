package fr.cnrs.opentheso.ws.openapi.v1.routes.group;

import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupTraductions;
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
public class GroupThesoController {

    @Autowired
    private Connect connect;


    @GetMapping(produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${getAllGroupsFromTheso.summary}$",
            description = "${getAllGroupsFromTheso.description}$",
            tags = {"Group"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getAllGroupsFromTheso.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)}),
                @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public ResponseEntity<Object>  getAllGroupsFromTheso(@Parameter(name = "idTheso", description = "${getAllGroupsFromTheso.idTheso.description}$", schema = @Schema(type = "string")) @PathVariable("idTheso") String idTheso) {

        GroupHelper groupHelper = new GroupHelper();
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
            summary = "${getGroupFromIdThesoIdGroup.summary}$",
            description = "${getGroupFromIdThesoIdGroup.description}$",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getGroupFromIdThesoIdGroup.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "${responses.group.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            })
    public ResponseEntity<Object> getGroupFromIdThesoIdGroup(
            @Parameter(name = "idTheso", required = true, description = "${getGroupFromIdThesoIdGroup.idTheso.description}$") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idGroup", required = true, description = "${getGroupFromIdThesoIdGroup.idGroup.description}$") @PathVariable("idGroup") String idGroup,
            @RequestHeader(value = "accept", required = false) String acceptHeader) {


        var datas = new RestRDFHelper().exportGroup(connect.getPoolConnexion(), idTheso, idGroup, removeCharset(acceptHeader));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(acceptHeader)).body(datas);
    }

    @GetMapping(value = "/{idGroup}/subgroup", produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(
            summary = "${getSubGroupsFromTheso.summary}$",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getSubGroupsFromTheso.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "${responses.group.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            })
    public ResponseEntity<Object> getSubGroupFromIdThesoIdGroup(
            @Parameter(name = "idTheso", required = true, description = "${getSubGroupsFromTheso.idTheso.description}$") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idGroup", required = true, description = "${getGroupFromIdThesoIdGroup.idGroup.description}$") @PathVariable("idGroup") String idGroup) {

        GroupHelper groupHelper = new GroupHelper();
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
            summary = "${getAllBranchOfGroup.summary}$",
            description = "${getAllBranchOfGroup.description}$",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getAllBranchOfGroup.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "404", description = "${responses.group.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            }
    )
    public ResponseEntity<Object> getAllBranchOfGroup(
            @Parameter(name = "idTheso", required = true, description = "${getAllBranchOfGroup.idTheso.description}$") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "idGroups", required = true, description = "${getAllBranchOfGroup.idGroups.description}$", example = "g1,g2,g3") @RequestParam("idGroups") String idGroups,
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
            summary = "${getAllBranchOfGroupAsTree.summary}$",
            description = "${getAllBranchOfGroupAsTree.description}$",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getAllBranchOfGroupAsTree.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "404", description = "${responses.group.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            }
    )
    public ResponseEntity<Object> getAllBranchOfGroupAsTree(
            @Parameter(name = "idTheso", required = true, description = "${getAllBranchOfGroupAsTree.idTheso.description}$") @PathVariable("idTheso") String idTheso,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${searchAutocomplete.lang.description}$") @RequestParam("lang") String lang,
            @Parameter(name = "idGroups", required = true, description = "${getAllBranchOfGroupAsTree.idGroups.description}$", example = "g1,g2,g3") @RequestParam("idGroups") String idGroups,
            @RequestHeader(value = "accept", required = false) String acceptHeader) {

        String[] groups = idGroups.split(",");
        if (groups.length == 0) {
            return ResponseEntity.badRequest().contentType(MediaType.parseMediaType(acceptHeader)).body("No group id");
        }

        var datas = new RestRDFHelper().brancheOfGroupAsTree(connect.getPoolConnexion(), groups, idTheso, lang);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(acceptHeader)).body(datas);
    }    
    
}
