package fr.cnrs.opentheso.ws.openapi.v1.routes.graph;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph.GraphD3jsHelper;
import fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph.IdValuePair;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;


@Slf4j
@RestController
@RequestMapping("/openapi/v1")
@CrossOrigin(methods = { RequestMethod.GET })
public class GraphController {

    @Autowired
    private Connect connect;


    @GetMapping(produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${getDatasForGraphNew.summary}$",
            tags = {"Graph"},
            description = "http://localhost:8080/opentheso2/openapi/v1/graph/getData?lang=fr&idThesoConcept=th3,4&idThesoConcept=th3",
            responses = {
                    @ApiResponse(responseCode = "200", description = "${search.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public ResponseEntity<Object>  getGraph(
            @Parameter(name = "lang", example = "fr", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${getDatasForGraphNew.lang.description}$") @RequestParam("lang") String lang,
            @Parameter(name = "idThesoConcept", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, example = "th3,4" , description = "${getDatasForGraphNew.idTheso.description}$") @RequestParam("idThesoConcept") List<String> idThesoConcepts) {
       
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

        for (IdValuePair idValuePair : idValuePairs) {
            if(StringUtils.isEmpty(idValuePair.getIdConcept())){
                graphD3jsHelper.getGraphByTheso(connect.getPoolConnexion(), idValuePair.getIdTheso(), lang);
            } else {
                graphD3jsHelper.getGraphByConcept(connect.getPoolConnexion(), idValuePair.getIdTheso(), idValuePair.getIdConcept(), lang);
            }
        }

        var datas = graphD3jsHelper.getJsonFromNodeGraphD3js();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }        
}

