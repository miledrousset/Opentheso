package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Concept", description = "Contient les opérations en lien avec les identifiants ARK. Un identifiant ark est formatté de la manière suivante : ark:/{naan}/{ark}.")
public class ConceptArkController {

    @Autowired
    private Connect connect;


    @GetMapping(value = "/{naan}/{ark}", produces = {APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "Permet de récupérer les informations d'un concept à partir de son ID Ark",
            description = "Ancienne version : `/api/{naan}/{idArk}.{format}`<br/>Permet de  récupérer les informations d'un concept à partir de son ID Ark dans les formats JSON, JSON-LD, Turtle ou RDF/XML",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier `JSON` contenant les informations du concept", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "Aucun concept n'existe avec cet ID dans le thesaurus choisi"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getConceptByArk(
            @Parameter(name = "naan", description = "Identifiant naan de l'organisme", required = true, example = "66666") @PathVariable("naan") String naan,
            @Parameter(name = "ark", description = "Identifiant Ark du concept", required = true, example = "lkp6ure1g7b6") @PathVariable("ark") String idArk,
            @RequestHeader(value = "accept", required = false) String acceptHeader) {

        var datas = new RestRDFHelper().exportConcept(connect.getPoolConnexion(), naan + "/" + idArk, HeaderHelper.removeCharset(acceptHeader));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Objects.requireNonNullElseGet(datas, () -> emptyMessage(APPLICATION_JSON_UTF_8)));
    }


    @GetMapping(value = "/{naan}/{ark}/childs", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "Permet de récupérer les ID Ark fils pour un concept donné.",
            description = "Ancienne version : `/api/ark/allchilds?ark={naan}/{idArk}`<br/>Retourne un fichier `JSON` contenant les ID Ark des fils du concept ayant l'ID Ark renseigné.",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier JSON contenant les ID Ark des fils", content = {@Content(mediaType = APPLICATION_JSON_UTF_8)}),
                    @ApiResponse(responseCode = "400", description = "Requête incorrecte, vérifiez que vos avez spécifié l'ID Ark"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object> getIdArkOfConceptNT(
                @Parameter(name = "naan", description = "Identifiant naan, correspondant à la première partie de l'ID Ark", example = "66666") @PathVariable("naan") String naan,
                @Parameter(name = "ark", description = "ID Ark du concept pour lequel on veut trouver les fils", example = "lkhsq27fw3z6") @PathVariable("ark") String arkLocalId) {

        var arkId = naan + "/" + arkLocalId;
        var datas = new RestRDFHelper().getChildsArkId(connect.getPoolConnexion(), arkId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Objects.requireNonNullElseGet(datas, () -> emptyMessage(APPLICATION_JSON_UTF_8)));
    }


    @GetMapping(value = "/{naan}/{ark}/prefLabel/{lang}", produces = APPLICATION_JSON_UTF_8)
    @Operation(
            summary = "Permet de récupérer un prefLabel dans une langue donnée à partir d'un ID Ark",
            description = "Ancienne version : `/api/preflabel.{langue}/{naan}/{idArk}.json`<br/>Permet de récupérer un `prefLabel` dans une langue donnée à partir d'un ID Ark",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fichier JSON contenant le `prefLabel`", content = { @Content(mediaType = APPLICATION_JSON_UTF_8)}),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "404", description = "Aucun concept n'existe avec cet ID dans le thesaurus choisi"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            }
    )
    public ResponseEntity<Object> getPrefLabelFromArk(
            @Parameter(name = "naan", description = "Identifiant NAAN de l'organisme", required = true, example = "66666") @PathVariable("naan") String naan,
            @Parameter(name = "ark", description = "Identifiant Ark du concept", required = true, example = "lkp6ure1g7b6") @PathVariable("ark") String idArk,
            @Parameter(name = "lang", description = "Langue du `prefLabel`", required = true, example = "fr") @PathVariable("lang") String lang) {

        String datas = new RestRDFHelper().getPrefLabelFromArk(connect.getPoolConnexion(), naan, idArk, lang);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Objects.requireNonNullElseGet(datas, () -> emptyMessage(APPLICATION_JSON_UTF_8)));
    }


}
