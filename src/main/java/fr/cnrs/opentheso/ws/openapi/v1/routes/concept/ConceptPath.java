package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.RestRDFHelper;
import fr.cnrs.opentheso.ws.Rest_new;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static fr.cnrs.opentheso.ws.openapi.helper.ConceptHelper.directFetchConcept;
import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import fr.cnrs.opentheso.ws.openapi.helper.MessageHelper;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.QueryParam;

/**
 * @author julie
 */
@Path("/concept")
public class ConceptPath {

    @Path("/handle/{handle}/{idHandle}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "getConceptByHandle.summary",
            description = "getConceptByHandle.description",
            tags = {"Concept"},
            responses = {
                @ApiResponse(responseCode = "200", description = "getConceptByHandle.200.responses", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8),
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
            @Content(mediaType = APPLICATION_RDF_UTF_8)
        }),
                @ApiResponse(responseCode = "404", description = "responses.concept.404.description"),
                @ApiResponse(responseCode = "503", description = "responses.503.description")
            })
    public Response getConceptByHandle(
            @Parameter(name = "handle", description = "getConceptByHandle.handle.description", required = true) @PathParam("handle") String handle,
            @Parameter(name = "idHandle", description = "getConceptByHandle.idHandle.description", required = true) @PathParam("idHandle") String idHandle,
            @Context HttpHeaders headers
    ) {
        String format = HeaderHelper.getContentTypeFromHeader(headers);
        return directFetchConcept(handle + "/" + idHandle, format);
    }

    @Path("/ark/fullpath/search")
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
            @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "searchJsonForWidgetArk.q.description", example = "66666/lkp6ure1g7b6,66666/lkubqlukv7i5") @QueryParam("q") String q,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "searchJsonForWidgetArk.lang.description", example = "fr") @QueryParam("lang") String lang,
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
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", APPLICATION_JSON_UTF_8);
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.findDatasForWidgetByArk(ds, lang, idArks, fullFormat);
            if (datas == null) {
                return ResponseHelper.response(Response.Status.OK, emptyMessage(APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
            }
        }

        return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
    }

    @Path("/redirect/ark:/{naan}/{idArk}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "getUriFromArk.summary",
            description = "getUriFromArk.description",
            tags = {"Concept", "Ark"},
            responses = {
                @ApiResponse(responseCode = "307", description = "getUriFromArk.200.description"),
                @ApiResponse(responseCode = "400", description = "responses.400.description"),
                @ApiResponse(responseCode = "404", description = "getUriFromArk.404.description", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "500", description = "responses.500.description")
            })
    public Response getUriFromArk(
            @Parameter(name = "naan", in = ParameterIn.PATH, schema = @Schema(type = "string"), description = "getUriFromArk.naan.description") @PathParam("naan") String naan,
            @Parameter(name = "idArk", in = ParameterIn.PATH, schema = @Schema(type = "string"), description = "getUriFromArk.idArk.description") @PathParam("idArk") String idArk
    ) {
        String webUrl;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, MessageHelper.errorMessage("No database connection", APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
            }

            RestRDFHelper restRDFHelper = new RestRDFHelper();
            webUrl = restRDFHelper.getUrlFromIdArk(ds, naan, idArk);
            if (webUrl == null) {
                return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, MessageHelper.errorMessage("Ark ID not found", APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
            }
            URI uri = new URI(webUrl);
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(ConceptPath.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ResponseHelper.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, MessageHelper.errorMessage("Internal server error", APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
    }

}
