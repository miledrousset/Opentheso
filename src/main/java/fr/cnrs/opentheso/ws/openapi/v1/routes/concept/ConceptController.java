package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;

/**
 * @author julie
 */
@Slf4j
@RestController
@RequestMapping("/openapi/v1/concept/ark/fullpath/search")
@CrossOrigin(methods = { RequestMethod.GET })
public class ConceptController {

    @Autowired
    private Connect connect;

    @GetMapping(produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${searchJsonForWidgetArk.summary}$",
            description = "${searchJsonForWidgetArk.description}$",
            tags = {"Concept", "Ark"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${searchJsonForWidgetArk.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response searchJsonForWidgetArk(
            @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${searchJsonForWidgetArk.q.description}$", example = "66666/lkp6ure1g7b6,66666/lkubqlukv7i5") @QueryParam("q") String q,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${searchJsonForWidgetArk.lang.description}$", example = "fr") @QueryParam("lang") String lang,
            @Parameter(name = "full", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), description = "${searchJsonForWidgetArk.full.description}$") @QueryParam("full") String fullString
    ) {
        String[] idArks = q.split(",");
        if (idArks.length == 0) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "No term specified", APPLICATION_JSON_UTF_8);
        }
        if (lang == null) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "No lang specified", APPLICATION_JSON_UTF_8);
        }

        var full = fullString != null && fullString.equalsIgnoreCase("true");
        var fullFormat = full ? "full" : null;
        var datas = new RestRDFHelper().findDatasForWidgetByArk(connect.getPoolConnexion(), lang, idArks, fullFormat);
        return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
    }
}
