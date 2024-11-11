package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;


import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;


@Slf4j
@RestController
@RequestMapping("/openapi/v1/concept/handle/{handle}/{idHandle}")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Concept", description = "Contient toutes les actions disponibles sur les concepts.")
public class ConceptHandleController {

    @Autowired
    private RestRDFHelper restRDFHelper;


    @GetMapping(produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "${getConceptByHandle.summary}$",
            description = "${getConceptByHandle.description}$",
            tags = {"Concept"},
            responses = {
                @ApiResponse(responseCode = "200",
                    description = "${getConceptByHandle.200.description}$", content = {
                    @Content(mediaType = APPLICATION_JSON_UTF_8),
                    @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                    @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                    @Content(mediaType = APPLICATION_RDF_UTF_8)
                }),
                    @ApiResponse(responseCode = "404", description = "Aucun concept n'existe avec cet ID dans le thesaurus choisi"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object>  getConceptByHandle(@RequestParam(name = "handle") String handle,
                                             @RequestParam(name = "idHandle") String idHandle,
                                             @RequestHeader(value = "accept", required = false) String acceptHeader) {

        var datas = restRDFHelper.exportConceptHdl(handle + "/" + idHandle, acceptHeader);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(acceptHeader)).body(datas);
    }    

}
