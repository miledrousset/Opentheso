package fr.cnrs.opentheso.ws.openapi.v1.routes.thesaurus;

import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;

/**
 *
 * @author julie
 */
@Slf4j
@RestController
@RequestMapping("/thesaurus")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Thesaurus", description = "Contient toutes les actions en liens avec les thesaurus.")
public class ThesaurusController {

    @Autowired
    private Connect connect;


    @GetMapping(produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "Permet de récupérer tous les thesaurus publiques",
            description = "Ancienne version : `/api/info/list?theso=all`<br/>Récupère une liste de tous les thésaurus publiques ainsi que les langues dans lesquelles ils sont disponibles.",
            tags = {"Thesaurus"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste contenant les ID des thesaurus publiques.", content = { @Content(mediaType = APPLICATION_JSON_UTF_8) }),
                    @ApiResponse(responseCode = "404", description = "Thésaurus non trouvé")
            }
    )
    public ResponseEntity<Object>  getListAllPublicTheso() {

        var listPublicTheso = new ThesaurusHelper().getAllIdOfThesaurus(connect.getPoolConnexion(), false);

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (String idTheso : listPublicTheso) {
            var job = Json.createObjectBuilder();
            job.add("idTheso", idTheso);
            var jsonArrayBuilderLang = Json.createArrayBuilder();

            var nodeThesaurus = new ThesaurusHelper().getNodeThesaurus(connect.getPoolConnexion(), idTheso);
            for (Thesaurus thesaurus : nodeThesaurus.getListThesaurusTraduction()) {
                var jobLang = Json.createObjectBuilder();
                jobLang.add("lang", thesaurus.getLanguage());
                jobLang.add("title", thesaurus.getTitle());
                jsonArrayBuilderLang.add(jobLang.build());
            }
            if (!nodeThesaurus.getListThesaurusTraduction().isEmpty()) {
                job.add("labels", jsonArrayBuilderLang.build());
            }

            jsonArrayBuilder.add(job.build());
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonArrayBuilder.build().toString());
    }
}
