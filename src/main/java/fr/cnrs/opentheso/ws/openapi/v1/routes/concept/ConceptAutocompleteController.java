package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.getAutocompleteDatas;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;

@Path("/concept/{idTheso}/autocomplete")
public class ConceptAutocompleteController {

    @Path("/{input}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${searchAutocomplete.summary}$",
            description = "${searchAutocomplete.description}$",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${searchAutocomplete.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$")
            })
    public Response searchAutocomplete(@Parameter(name = "idThesaurus", required = true, description = "${searchAutocomplete.idThesaurus.description}$") @PathParam("idTheso") String idTheso,
                                       @Parameter(name = "input", required = true, description = "${searchAutocomplete.input.description}$") @PathParam("input") String input,
                                       @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${searchAutocomplete.lang.description}$") @QueryParam("lang") String lang,
                                       @Parameter(name = "group", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${searchAutocomplete.group.description}$") @QueryParam("group") String groupsString,
                                       @Parameter(name = "full", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), required = false, description = "${searchAutocomplete.full.description}$") @QueryParam("full") String fullString) {
        if (lang == null) {
            lang = "";
        }
        String[] groups = groupsString != null ? groupsString.split(",") : null;
        boolean full = fullString != null && fullString.equalsIgnoreCase("true");
        String format = APPLICATION_JSON_UTF_8;
        String datas;

        datas = getAutocompleteDatas(idTheso, lang, groups, input, full);

        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.response(Response.Status.OK, emptyMessage(format), format);
        } else {
            return ResponseHelper.response(Response.Status.OK, datas, format);
        }

    }

}
