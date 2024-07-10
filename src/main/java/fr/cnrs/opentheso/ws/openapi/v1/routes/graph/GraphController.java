package fr.cnrs.opentheso.ws.openapi.v1.routes.graph;

import com.zaxxer.hikari.HikariDataSource;
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

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.*;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;
import fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph.GraphD3jsHelper;
import fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph.IdValuePair;
import java.util.ArrayList;
import java.util.List;

@Path("/graph/getData")
public class GraphController {

    @Path("/")
    @GET
    @Produces({APPLICATION_RDF_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(summary = "${search.summary}$",
            description = "${search.description}$",
            tags = {"Graph"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${search.200.description}$", content = {
                            @Content(mediaType = APPLICATION_RDF_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response getGraph(
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${search.q.description}$") @QueryParam("lang") String lang,
            @Parameter(name = "idThesoConcept", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${search.q.description}$") @QueryParam("idThesoConcept") List<String> idThesoConcepts,
                           @Context HttpHeaders headers) {
        
        if(StringUtils.isEmpty(lang)) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "La langue est requise", APPLICATION_JSON);            
        }
        if(idThesoConcepts == null || idThesoConcepts.isEmpty()) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "Manque les infos du thésaurus et des concepts", APPLICATION_JSON);            
        }        
       
        List<IdValuePair> idValuePairs = new ArrayList<>();
        
        for (String idThesoConcept : idThesoConcepts) {
            String[] elements = idThesoConcept.split(",");
            
            if(elements.length == 2){
                IdValuePair idValuePair = new IdValuePair();
                idValuePair.setIdTheso(elements[0]);
                idValuePair.setIdConcept(elements[1]);
                idValuePairs.add(idValuePair);
            }
            if(elements.length == 1){
                IdValuePair idValuePair = new IdValuePair();
                idValuePair.setIdTheso(elements[0]);
                idValuePairs.add(idValuePair);
            }            
        }
        
        GraphD3jsHelper graphD3jsHelper = new GraphD3jsHelper();
        graphD3jsHelper.initGraph();        
        
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", APPLICATION_JSON_UTF_8);
            }

            for (IdValuePair idValuePair : idValuePairs) {
                if(StringUtils.isEmpty(idValuePair.getIdConcept())){
                    graphD3jsHelper.getGraphByTheso(ds, idValuePair.getIdTheso(), lang);
                } else {
                    graphD3jsHelper.getGraphByConcept(ds, idValuePair.getIdTheso(), idValuePair.getIdConcept(), lang);
                }
            }            
        }
        datas = graphD3jsHelper.getJsonFromNodeGraphD3js();
        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.response(Response.Status.OK, emptyMessage(APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
        } 
        return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);        
    }        
}

