package fr.cnrs.opentheso.ws.openapi.v1.routes.group;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
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

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.removeCharset;

@Path("/group/{idTheso}")
public class GroupThesoController {

    @Path("/{idGroup}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(
            summary = "getGroupFromIdThesoIdGroup.summary",
            description = "getGroupFromIdThesoIdGroup.description",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "getGroupFromIdThesoIdGroup.200.description", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "responses.group.404.description"),
                    @ApiResponse(responseCode = "503", description = "responses.503.description")
            })
    public Response getGroupFromIdThesoIdGroup(
            @Parameter(name = "idTheso", required = true, description = "getGroupFromIdThesoIdGroup.idTheso.description") @PathParam("idTheso") String idTheso,
            @Parameter(name = "idGroup", required = true, description = "getGroupFromIdThesoIdGroup.idGroup.description") @PathParam("idGroup") String idGroup,
            @Context HttpHeaders headers
            ) {
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        HikariDataSource ds = connect();
        String format = HeaderHelper.getContentTypeFromHeader(headers);
        if (ds == null) return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Service unavailable", format);
        String datas = restRDFHelper.exportGroup(ds, idTheso, idGroup, removeCharset(format));
        ds.close();
        if (datas == null) return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "Group not found", format);
        return ResponseHelper.response(Response.Status.OK, datas, format);
    }

    @Path("/{idGroups}/branch")
    @GET
    @Produces({APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(
            summary = "getAllBranchOfGroup.summary",
            description = "getAllBranchOfGroup.description",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "getAllBranchOfGroup.200.description", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "responses.group.404.description"),
                    @ApiResponse(responseCode = "503", description = "responses.503.description")
            }
    )
    public Response getAllBranchOfGroup(
            @Parameter(name = "idTheso", required = true, description = "getAllBranchOfGroup.idTheso.description") @PathParam("idTheso") String idTheso,
            @Parameter(name = "idGroups", required = true, description = "getAllBranchOfGroup.idGroups.description", example = "g1,g2,g3") @PathParam("idGroups") String idGroups,
            @Context HttpHeaders headers
    ) {
        String[] groups = idGroups.split(",");
        String format = HeaderHelper.getContentTypeFromHeader(headers);
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Service unavailable", format);
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.brancheOfGroup(ds, groups, idTheso, removeCharset(format));
        }
        if (datas == null) return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "Group not found", format);
        return ResponseHelper.response(Response.Status.OK, datas, format);
    }
}
