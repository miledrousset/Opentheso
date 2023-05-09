package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Objects;

import static fr.cnrs.opentheso.ws.openapi.helper.ConceptHelper.directFetchConcept;
import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;

@Path("/concept/ark")
public class ConceptInfoFromArkPath {


    @Path("/{naan}/{idArk}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "getConceptByArk.summary",
            description = "getConceptByArk.description",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "getConceptByArk.200.description", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "responses.concept.404.description"),
                    @ApiResponse(responseCode = "503", description = "responses.503.description")
            })
    public Response getConceptByArk(
            @Parameter(name = "naan", description = "getConceptByArk.naan.description", required = true) @PathParam("naan") String naan,
            @Parameter(name = "idArk", description = "getConceptByArk.idArk.description", required = true) @PathParam("idArk") String idArk,
            @Context HttpHeaders headers
    ) {
        String format = HeaderHelper.getContentTypeFromHeader(headers);
        return directFetchConcept(naan + "/" + idArk, format);
    }

    @Path("/{arkId}/childs")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "getIdArkOfConceptNT.summary",
            description = "getIdArkOfConceptNT.description",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "getIdArkOfConceptNT.200.description", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "getIdArkOfConceptNT.400.description"),
                    @ApiResponse(responseCode = "503", description = "responses.503.description")
            })
    public Response getIdArkOfConceptNT(@Parameter(name = "arkId", description = "getIdArkOfConceptNT.arkId.description") @PathParam("arkId") String arkId) {

        if (StringUtils.isEmpty(arkId)) {
            return ResponseHelper.errorResponse(Response.Status.BAD_REQUEST, "Missing Ark ID", APPLICATION_JSON_UTF_8);
        }

        String datas;
        RestRDFHelper restRDFHelper = new RestRDFHelper();

        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "No server connection", APPLICATION_JSON_UTF_8);
            }
            datas = restRDFHelper.getChildsArkId(ds, arkId);
        }

        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.response(Response.Status.OK, emptyMessage(APPLICATION_JSON_UTF_8), datas);

        }
        return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
    }

    @Path("/fullpath/search")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "searchJsonForWidgetArk.summary",
            description = "searchJsonForWidgetArk.description",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "searchJsonForWidgetArk.200.description", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "responses.400.description"),
                    @ApiResponse(responseCode = "500", description = "responses.500.description")
            })
    public Response searchJsonForWidgetArk(
            @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "searchJsonForWidgetArk.q.description") @QueryParam("q") String q,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "searchJsonForWidgetArk.lang.description") @QueryParam("lang") String lang,
            @Parameter(name = "full", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), description = "searchJsonForWidgetArk.full.description") @QueryParam("full") String fullString
    ) {
        String[] idArks = q.split(",");
        if (idArks.length == 0) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "No term specified", APPLICATION_JSON_UTF_8);
        }
        if (lang == null) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "No lang specified", APPLICATION_JSON_UTF_8);
        }
        boolean full = fullString != null && fullString.equalsIgnoreCase("true");

        String fullFormat = full ? "full" : null;

        String datas;
        try(HikariDataSource ds = connect()) {
            if (ds == null) return ResponseHelper.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", APPLICATION_JSON_UTF_8);
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.findDatasForWidgetByArk(ds, lang, idArks, fullFormat);
            if (datas == null) return ResponseHelper.response(Response.Status.OK, emptyMessage(APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
        }

        return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
    }

    @Path("/{naan}/{idArk}/prefLabel/{lang}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(
            summary = "getPrefLabelFromArk.summary",
            description = "getPrefLabelFromArk.description",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "getPrefLabelFromArk.200.description", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "responses.400.description"),
                    @ApiResponse(responseCode = "404", description = "responses.concept.404.description"),
                    @ApiResponse(responseCode = "503", description = "responses.503.description")
            }
    )
    public Response getPrefLabelFromArk(
            @Parameter(name = "naan", description = "getPrefLabelFromArk.naan.description", required = true) @PathParam("naan") String naan,
            @Parameter(name = "idArk", description = "getPrefLabelFromArk.idArk.description", required = true) @PathParam("idArk") String idArk,
            @Parameter(name = "lang", description = "getPrefLabelFromArk.lang.description", required = true) @PathParam("lang") String lang
    ) {
        HikariDataSource ds = connect();
        if (ds == null) {
            return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "No server connection", APPLICATION_JSON_UTF_8);
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.getPrefLabelFromArk(ds, naan, idArk, lang);
        ds.close();
        return ResponseHelper.response(Response.Status.OK, Objects.requireNonNullElseGet(datas, () -> emptyMessage(APPLICATION_JSON_UTF_8)), APPLICATION_JSON_UTF_8);
    }
}
