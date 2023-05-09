package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

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

}
