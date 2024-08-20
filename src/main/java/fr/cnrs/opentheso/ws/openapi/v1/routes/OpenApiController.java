/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.openapi.v1.routes;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.ws.openapi.helper.*;
import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import fr.cnrs.opentheso.ws.openapi.v1.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.sql.SQLException;
import java.util.*;

import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;

/**
 * REST Web Service
 *
 * @author Julien LINGET
 */

@Path("/")
public class OpenApiController extends BaseOpenApiResource {

    @Context
    ServletConfig config;

    @Context
    Application app;

    @Path("/{lang}/openapi.{type:json|yaml}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    @Operation(hidden = true)

    public Response getOpenApi(@Context HttpHeaders headers,
            @Context UriInfo uriInfo,
            @PathParam("type") String type,
            @PathParam("lang") String lang,
            @QueryParam("scheme") String scheme) {

       Map<String, String> types = new HashMap<>();
       types.put("json", CustomMediaType.APPLICATION_JSON_UTF_8);
       types.put("yaml", "application/yaml;charset=utf-8");

        LangHelper helper = new LangHelper();
         List<String> languages = helper.availableLang();

         if (!languages.contains(lang.toLowerCase())) {
             return Response
                     .status(Response.Status.NOT_FOUND)
                     .entity("The lang " + lang + " is not available")
                     .type(types.get(type))
                     .header("Access-Control-Allow-Origin", "*")
                     .build();
         }

         ResourceBundle bundle = ResourceBundle.getBundle("language.openapi", new Locale(lang));

        try {
            /*Response openapi = super.getOpenApi(headers, config, app, uriInfo, type);

            String jsonOAS = (String) openapi.getEntity();
            jsonOAS = helper.translate(jsonOAS, bundle);

            jsonOAS = jsonOAS.replace("${BASE_SERVER}$", changeURL(uriInfo, scheme));
            Logger.getLogger(OpenApiConfig.class.getName()).log(Level.SEVERE, changeURL(uriInfo, scheme));

            return Response.status(Response.Status.OK)
                    .entity(jsonOAS)
                    .type(types.get(type))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();*/
        } catch (Exception e) {
            Logger.getLogger(OpenApiConfig.class.getName()).log(Level.SEVERE, e.getMessage());
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error")
                .type(CustomMediaType.APPLICATION_JSON_UTF_8)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @Path("/ping")
    @GET
    @Produces({CustomMediaType.APPLICATION_JSON_UTF_8})
    @Operation(summary = "${testWS.summary}$",
            description = "${testWS.description}$",
            responses = {
                @ApiResponse(responseCode = "200", description = "${testWS.200.description}$", content = {
            @Content(mediaType = CustomMediaType.APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "400", description = "${responses.400.description}$")
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



    @Path("/Auth")
    @GET
    @Produces(CustomMediaType.APPLICATION_JSON_UTF_8)
    @Operation(summary = "${testAuth.summary}$",
            description = "${testAuth.description}$",
            tags = {"Test"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${testAuth.200.description}$", content = {
                    @Content(mediaType = CustomMediaType.APPLICATION_JSON_UTF_8)
                }),
                @ApiResponse(responseCode = "401", description = "${testAuth.401.description}$"),
                @ApiResponse(responseCode = "403", description = "${testAuth.403.description}$"),
                @ApiResponse(responseCode = "500", description = "${responses.500.description}$"),
                @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            },
            security = {
                @SecurityRequirement(name = "API-KEY")
            }
    )
    public Response testAuth(@Context HttpHeaders headers)  {
        DataHelper dataHelper = new DataHelper();
        UserHelper userHelper = new UserHelper();
        String apiKey = headers.getHeaderString("API-KEY");
        ApiKeyHelper helper = new ApiKeyHelper();
        ApiKeyState keyState = helper.checkApiKey(apiKey);
        if (keyState != ApiKeyState.VALID){
            return errorResponse(keyState);
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("valid", true);
        builder.add("key", apiKey);
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(null)
                        .type(CustomMediaType.APPLICATION_JSON_UTF_8)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
            }

            int roleId = userHelper.getRoleOnThisTheso(ds, helper.getIdUser(apiKey), userHelper.getUserGroupId(helper.getIdUser(apiKey), "th2").orElse(0), "th2" );
            builder.add("Roles", roleId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Response.status(Response.Status.OK)
                .entity(builder.build().toString())
                .type(CustomMediaType.APPLICATION_JSON_UTF_8)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    private String changeURL(UriInfo uriInfo, String scheme) {
        if (scheme == null) return uriInfo.getBaseUri().toString();
        String urlWithoutScheme = uriInfo.getBaseUri().toString().split("://")[1];
        return scheme + "://" + urlWithoutScheme;
    }


    public Response errorResponse(ApiKeyState state) {
        Response.Status code = null;
        String msg = null;
        switch (state) {
            case EMPTY:
                code = Response.Status.UNAUTHORIZED;
                msg = "No API key given";
                break;
            case DATABASE_UNAVAILABLE:
                code = Response.Status.SERVICE_UNAVAILABLE;
                msg = "Database unavailable";
                break;
            case INVALID:
                code = Response.Status.FORBIDDEN;
                msg = "API key is invalid";
                break;
            case SQL_ERROR:
                code = Response.Status.INTERNAL_SERVER_ERROR;
                msg = "Server internal error";
                break;
            case EXPIRED:
                code = Response.Status.UNAUTHORIZED;
                msg = "API key is expired";
                break;
        }

        return Response.status(code)
                .entity(msg)
                .type(CustomMediaType.APPLICATION_JSON_UTF_8)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }




}
