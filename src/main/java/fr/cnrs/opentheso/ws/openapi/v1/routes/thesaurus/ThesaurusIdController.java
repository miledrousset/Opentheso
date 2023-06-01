package fr.cnrs.opentheso.ws.openapi.v1.routes.thesaurus;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.ws.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import fr.cnrs.opentheso.ws.openapi.helper.MessageHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;

@Path("/thesaurus/{thesaurusId}")
public class ThesaurusIdController {

    @Path("/")
    @GET
    @Produces({APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "${getThesoFromId.summary}$",
            description = "${getThesoFromId.description}$",
            tags = {"Thesaurus"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getThesoFromId.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$"),
                    @ApiResponse(responseCode = "404", description = "${responses.theso.404.description}$")
            })
    public Response getThesoFromId(@Parameter(name = "thesaurusId", description = "${getThesoFromId.thesaurusId.description}$", required = true) @PathParam("thesaurusId") String thesaurusId,
                                   @Context HttpHeaders headers) {
        String format = HeaderHelper.getContentTypeFromHeader(headers);
        String datas;

        try (HikariDataSource ds = connect()) {

            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Service unavailable", format);
            }

            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.getTheso(ds, thesaurusId, format);
        }

        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "The given thesaurus ID does not exist", format);
        } else {
            return ResponseHelper.response(Response.Status.OK, datas, format);
        }
    }

    @Path("/topterms")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${getThesoGroupsFromId.summary}$",
            description = "${getThesoGroupsFromId.description}$",
            tags = {"Thesaurus"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getThesoGroupsFromId.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            })
    public Response getThesoGroupsFromId(@Parameter(name = "thesaurusId", description = "${getThesoGroupsFromId.thesaurusId.description}$", required = true) @PathParam("thesaurusId") String thesaurusId) {
        ConceptHelper conceptHelper = new ConceptHelper();
        TermHelper termHelper = new TermHelper();
        String datasJson;

        try (HikariDataSource ds = connect()) {

            List<String> listIdTopConceptOfTheso = conceptHelper.getAllTopTermOfThesaurus(ds, thesaurusId);

            ArrayList<NodeTermTraduction> nodeTermTraductions;

            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            for (String idConcept : listIdTopConceptOfTheso) {
                JsonObjectBuilder job = Json.createObjectBuilder();
                job.add("idConcept", idConcept);
                JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

                nodeTermTraductions = termHelper.getAllTraductionsOfConcept(ds, idConcept, thesaurusId);
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
            datasJson = jsonArrayBuilder.build().toString();

        }

        if (datasJson != null) {
            return Response.status(Response.Status.OK)
                    .entity(datasJson)
                    .type(APPLICATION_JSON_UTF_8)
                    .build();
        } else {
            return null;
        }
    }


    @Path("/topterms/{lang}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(
            summary = "${getTopterms.summary}$",
            description = "${getTopterms.description}$",
            tags = {"Thesaurus"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "${responses.theso.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            }
    )
    public Response getTopterms (
            @Parameter(name = "thesaurusId", description = "${getTopterms.thesaurusId.description}$", required = true) @PathParam("thesaurusId") String thesaurusId,
            @Parameter(name = "lang", description = "${getTopterms.lang.description}$", required = true) @PathParam("lang") String lang
    ) {
        String datas;
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        try (HikariDataSource ds = connect()) {
            datas = restRDFHelper.getTopTerms(ds, thesaurusId, lang);
        }
        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "The given thesaurus ID does not exist", APPLICATION_JSON_UTF_8);
        } else {
            return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
        }
    }

    @Path("/lastupdate")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${getInfoLastUpdate.summary}$",
    description = "${getInfoLastUpdate.description}$",
    tags = {"Thesaurus"},
    responses = {
            @ApiResponse(responseCode = "200", description = "${getInfoLastUpdate.200.description}$", content = {
                    @Content(mediaType = APPLICATION_JSON_UTF_8)
            }),
            @ApiResponse(responseCode = "503", description = "${responses.503.description}$"),
            @ApiResponse(responseCode = "404", description = "${responses.theso.404.description}$")
    })
    public Response getInfoLastUpdate(@Parameter(name = "thesaurusId", description = "${getInfoLastUpdate.thesaurusId.description}$", required = true) @PathParam("thesaurusId") String thesaurusId) {
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(MessageHelper.errorMessage("Service unavailable", APPLICATION_JSON_UTF_8)).type(APPLICATION_JSON_UTF_8).build();
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        Date date = conceptHelper.getLastModification(ds, thesaurusId);
        if (date == null) {
            return ResponseHelper.response(Response.Status.OK, MessageHelper.emptyMessage(APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
        }

        String datas = "{\"lastUpdate\":\"" + date.toString() + "\"}";

        ds.close();

        return Response.status(Response.Status.OK).entity(datas).type(APPLICATION_JSON_UTF_8).build();
    }

    @Path("/flatlist")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${getThesoFromIdFlat.summary}$",
            description = "${getThesoFromIdFlat.description}$",
            tags = {"Thesaurus"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getThesoFromIdFlat.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$"),
                    @ApiResponse(responseCode = "404", description = "${responses.theso.404.description}$")
            })
    public Response getThesoFromIdFlat(@Parameter(name = "thesaurusId", description = "${getThesoFromIdFlat.thesaurusId.description}$", required = true) @PathParam("thesaurusId") String thesaurusId,
                                   @Parameter(name = "lang", description = "${getThesoFromIdFlat.lang.description}$", required = true) @QueryParam("lang") String lang,
                                   @Context HttpHeaders headers) {
        String datas;
        if (lang == null) {
            lang = "fr";
        }
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.getThesoIdValue(ds, thesaurusId, lang);
        }

        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, MessageHelper.errorMessage("The given thesaurus ID does not exist", APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
        } else {
            return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
        }
    }

}
