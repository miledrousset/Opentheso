package fr.cnrs.opentheso.ws.openapi.v1.routes.group;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.MessageHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.Objects;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;

/**
 * @author julie
 */
@Slf4j
@RestController
@RequestMapping("/group")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Group", description = "Contient les actions en lien avec les groupes.")
public class GroupController {

    @Autowired
    private Connect connect;

    @Autowired
    private RestRDFHelper restRDFHelper;


    @GetMapping(value = "/ark:/{naan}/{ark}", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "Permet de récupérer un ID de groupe associé à un identifiant ARK",
            description = "Ancienne version : `/api/group/{naan}/{idArk}`<br/>Recherche l'identifiant du groupe associé à l'identifiant ARK donné",
            tags = {"Group", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Identifiant du groupe correspondant à l'ID Ark", content = {@Content(mediaType = APPLICATION_JSON_UTF_8)}),
                    @ApiResponse(responseCode = "400", description = "Erreur dans la synthaxe de la requête"),
                    @ApiResponse(responseCode = "503", description = "Pas de connexion au serveur")
            })
    public ResponseEntity<Object>  getGroupIdFromArk(@Parameter(name = "naan", required = true, description = "Identifiant naan") @PathVariable("naan") String naan,
                                                     @Parameter(name = "ark", required = true, description = "ARK id") @PathVariable("ark") String arkId) {

        var datas = restRDFHelper.exportGroup(connect.getPoolConnexion(), naan + "/" + arkId, HeaderHelper.removeCharset(APPLICATION_JSON_UTF_8));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Objects.requireNonNullElseGet(datas, () -> MessageHelper.emptyMessage(APPLICATION_JSON_UTF_8)));
    }

}
