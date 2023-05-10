/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.openapi.v1;

import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;
import fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import fr.cnrs.opentheso.ws.openapi.scanner.io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.HeaderParam;

/**
 * REST Web Service
 *
 * @author Julien LINGET
 */
//general path = /openapi/v1
@Path("/")
public class OpenApiPath extends BaseOpenApiResource {

    @Context
    ServletConfig config;

    @Context
    Application app;

    @Path("/{lang:en|fr}/openapi.{type:json|yaml}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    @Operation(hidden = true)
    public Response getOpenApi(@Context HttpHeaders headers,
            @Context UriInfo uriInfo,
            @PathParam("type") String type,
            @PathParam("lang") String lang) throws Exception {

        ResourceBundle bundle = ResourceBundle.getBundle("language.openapi", new Locale(lang));

        try {
            Response openapi = super.getOpenApi(headers, config, app, uriInfo, type, bundle);
            return openapi;
        } catch (Exception e) {
            Logger.getLogger(OpenApiConfig.class.getName()).log(Level.SEVERE, e.getMessage());
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Path("/ping")
    @GET
    @Produces({CustomMediaType.APPLICATION_JSON_UTF_8})
    @Operation(summary = "testWS.summary",
            description = "testWS.description",
            responses = {
                @ApiResponse(responseCode = "200", description = "testWS.200.description", content = {
            @Content(mediaType = CustomMediaType.APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "400", description = "responses.400.description")
            },
            tags = {"Test"})
    public Response testWS() {
        String message = "{\"message\": \"pongV1\"}";

        return Response
                .status(Response.Status.OK)
                .entity(message)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @Path("/testAuth")
    @GET
    @Produces(CustomMediaType.APPLICATION_JSON_UTF_8)
    @Operation(summary = "testAuth.summary",
            description = "testAuth.description",
            tags = {"Test"},
            responses = {
                @ApiResponse(responseCode = "200", description = "testAuth.200.description"),
                @ApiResponse(responseCode = "403", description = "testAuth.403.description"),
                @ApiResponse(responseCode = "500", description = "responses.500.description"),
                @ApiResponse(responseCode = "503", description = "responses.503.description")
            },
            security = {
                @SecurityRequirement(name = "CLE-API-EXAMPLE")
            }
    )
    public Response testAuth(@Parameter(name = "CLE-API-EXEMPLE", hidden = true) @HeaderParam("CLE-API-EXEMPLE") String cleAPI) {
        ApiKeyHelper helper = new ApiKeyHelper();
        ApiKeyState keyState = helper.checkApiKeyExistance(cleAPI);
        if (keyState != ApiKeyState.VALID) {
            return helper.errorResponse(keyState);
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("valid", true);
        builder.add("key", cleAPI);
        
        Response myResponse = ResponseHelper.response(Response.Status.OK, builder.build().toString(), CustomMediaType.APPLICATION_JSON_UTF_8);
        
        return myResponse;
    }

}
