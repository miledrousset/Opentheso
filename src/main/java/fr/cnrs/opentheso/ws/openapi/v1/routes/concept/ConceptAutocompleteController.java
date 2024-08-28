package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;


@Slf4j
@RestController
@RequestMapping("/concept/{idTheso}/autocomplete")
@CrossOrigin(methods = { RequestMethod.GET })
public class ConceptAutocompleteController {

    @Autowired
    private Connect connect;


    @GetMapping(value = "/{input}", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${searchAutocomplete.summary}$",
            description = "${searchAutocomplete.description}$",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${searchAutocomplete.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "404", description = "${searchAutocomplete.404.description}$")
            })
    public ResponseEntity<Object> searchAutocomplete(@Parameter(name = "idThesaurus", required = true, description = "${searchAutocomplete.idThesaurus.description}$") @PathVariable("idTheso") String idTheso,
                                       @Parameter(name = "input", required = true, description = "${searchAutocomplete.input.description}$") @PathVariable("input") String input,
                                       @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${searchAutocomplete.lang.description}$") @RequestParam(value = "lang", required = false) String lang,
                                       @Parameter(name = "group", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${searchAutocomplete.group.description}$") @RequestParam(value = "group", required = false) String groupsString,
                                       @Parameter(name = "full", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), required = false, description = "${searchAutocomplete.full.description}$") @RequestParam(value = "full", required = false) String fullString) {

        var groups = groupsString != null ? groupsString.split(",") : null;
        var full = fullString != null && fullString.equalsIgnoreCase("true");
        var datas = new RestRDFHelper().findAutocompleteConcepts(connect.getPoolConnexion(), idTheso, lang, groups, input, full);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }

    @GetMapping("/{input}/full")
    public ResponseEntity<Object> searchAutocompleteV2(@PathVariable("idTheso") String idTheso,
                                               @PathVariable("input") String input,
                                               @RequestParam("lang") String lang,
                                               @RequestParam("group") String idGroup) throws JsonProcessingException {

        var concepts = new SearchHelper().searchConceptWSV2(connect.getPoolConnexion(), input, lang, idGroup, idTheso);
        var jsonString = new ObjectMapper().writeValueAsString(concepts);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonString);
    }

    @GetMapping("/{idThesaurus}/{idLang}")
    public ResponseEntity<Object> getGroupsByThesaurus(@PathVariable("idThesaurus") String idThesaurus,
                                               @PathVariable("idLang") String idLang) throws JsonProcessingException {

        var groups = new GroupHelper().getListRootConceptGroup(connect.getPoolConnexion(), idThesaurus, idLang, true);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new ObjectMapper().writeValueAsString(groups));
    }
}
