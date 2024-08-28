package fr.cnrs.opentheso.ws.openapi.v1.routes;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;


@Slf4j
@RestController
@RequestMapping("/api/redirect")
@CrossOrigin(methods = { RequestMethod.GET })
public class RedirectController {

    @Autowired
    private Connect connect;

    @GetMapping(value = "/ark:/{naan}/{idArk}", produces = APPLICATION_JSON_UTF_8)
    @Operation(summary = "${getUriFromArk.summary}$",
            description = "${getUriFromArk.description}$",
            tags = {"Ark"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getUriFromArk.200.description}$"),
                @ApiResponse(responseCode = "307", description = "${getUriFromArk.307.description}$"),
                @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                @ApiResponse(responseCode = "404", description = "${getUriFromArk.404.description}$", content = { @Content(mediaType = APPLICATION_JSON_UTF_8) }),
                @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public ResponseEntity<Object> getUriFromArk(@PathVariable("naan") String naan, @PathVariable("idArk") String arkId) throws URISyntaxException {

        var webUrl = new RestRDFHelper().getUrlFromIdArk(connect.getPoolConnexion(), naan, arkId);
        return ResponseEntity.status(307).location(new URI(webUrl)).build();
    }
    
}
