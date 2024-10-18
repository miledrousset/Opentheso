package fr.cnrs.opentheso.ws.api;

import java.net.URI;
import java.net.URISyntaxException;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/concept")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Concept", description = "Contient toutes les actions disponibles sur les concepts.")
public class SelectConcept {


    // Cette fonction permet de se replacer sur un concept dans le thésaurus
    @GetMapping(value = "{idTheso}.{idConcept}", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<Object> getConcept(@PathVariable("idTheso") String idTheso,
                                     @PathVariable("idConcept") String idConcept,
                                     HttpServletRequest request) throws URISyntaxException {

        // Extraire la base de l'URL (jusqu'à "api/concept/") et créer la nouvelle URL avec les bons paramètres
        String requestUrl = request.getRequestURL().toString();

        // Construire l'URL de redirection avec le format requis
        String newUrl = requestUrl.replace("/api/concept/" + idTheso + "." + idConcept, "/")
                + "?idc=" + idConcept + "&idt=" + idTheso;
        return ResponseEntity.status(307).location(new URI(newUrl)).build();
    }
}
