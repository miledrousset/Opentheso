package fr.cnrs.opentheso.ws.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/theso")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Thesaurus", description = "Contient toutes les actions en liens avec les thesaurus.")
public class SelectTheso {

    // Cette fonction permet de se diriger vers le bon thésaurus en passant par son nom VIA REST ceci permet de gérer
    // les noms de domaines et filtrer les thésaurus dans un parc important
    @GetMapping(value = "{theso}", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<Object> getThesoUri(@PathVariable("theso") String idTheso,

    // @Context UriInfo uriInfo
       HttpServletRequest request
    ) throws URISyntaxException {

        // Récupération de l'URL de la requête
        String requestUrl = request.getRequestURL().toString();
        String newUrl = requestUrl.replace("/api/theso/" +idTheso, "/") + "?idt=" + idTheso;
        return ResponseEntity.status(307).location(new URI(newUrl)).build();
    }
}
