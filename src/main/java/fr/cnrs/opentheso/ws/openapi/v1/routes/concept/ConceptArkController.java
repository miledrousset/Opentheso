package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;



@Slf4j
@RestController
@RequestMapping("/concept/ark:")
@CrossOrigin(methods = { RequestMethod.GET })
public class ConceptArkController {

    @Autowired
    private Connect connect;


    @GetMapping(value = "/{naan}/{ark}", produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "${getConceptByArk.summary}$",
            description = "${getConceptByArk.description}$",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getConceptByArk.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getConceptByArk(
            @Parameter(name = "naan", description = "${getConceptByArk.naan.description}$", required = true, example = "66666") @PathVariable("naan") String naan,
            @Parameter(name = "ark", description = "${getConceptByArk.idArk.description}$", required = true, example = "lkp6ure1g7b6") @PathVariable("ark") String idArk,
            @RequestHeader(value = "accept", required = false) String acceptHeader) {

        var datas = new RestRDFHelper().exportConcept(connect.getPoolConnexion(), naan + "/" + idArk, HeaderHelper.removeCharset(acceptHeader));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Objects.requireNonNullElseGet(datas, () -> emptyMessage(APPLICATION_JSON_UTF_8)));
    }


    @GetMapping(value = "/{naan}/{ark}/childs", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${getIdArkOfConceptNT.summary}$",
            description = "${getIdArkOfConceptNT.description}$",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getIdArkOfConceptNT.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${getIdArkOfConceptNT.400.description}$"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getIdArkOfConceptNT(
                @Parameter(name = "naan", description = "${getIdArkOfConceptNT.naan.description}$", example = "66666") @PathVariable("naan") String naan,
                @Parameter(name = "ark", description = "${getIdArkOfConceptNT.arkId.description}$", example = "lkhsq27fw3z6") @PathVariable("ark") String arkLocalId) {

        var arkId = naan + "/" + arkLocalId;
        var datas = new RestRDFHelper().getChildsArkId(connect.getPoolConnexion(), arkId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Objects.requireNonNullElseGet(datas, () -> emptyMessage(APPLICATION_JSON_UTF_8)));
    }


    @GetMapping(value = "/{naan}/{ark}/prefLabel/{lang}", produces = APPLICATION_JSON_UTF_8)
    @Operation(
            summary = "${getPrefLabelFromArk.summary}$",
            description = "${getPrefLabelFromArk.description}$",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getPrefLabelFromArk.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            }
    )
    public ResponseEntity<Object> getPrefLabelFromArk(
            @Parameter(name = "naan", description = "${getPrefLabelFromArk.naan.description}$", required = true, example = "66666") @PathVariable("naan") String naan,
            @Parameter(name = "ark", description = "${getPrefLabelFromArk.idArk.description}$", required = true, example = "lkp6ure1g7b6") @PathVariable("ark") String idArk,
            @Parameter(name = "lang", description = "${getPrefLabelFromArk.lang.description}$", required = true, example = "fr") @PathVariable("lang") String lang) {

        String datas = new RestRDFHelper().getPrefLabelFromArk(connect.getPoolConnexion(), naan, idArk, lang);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Objects.requireNonNullElseGet(datas, () -> emptyMessage(APPLICATION_JSON_UTF_8)));
    }


}
