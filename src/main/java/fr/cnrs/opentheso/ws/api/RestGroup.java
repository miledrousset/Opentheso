package fr.cnrs.opentheso.ws.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group")
@CrossOrigin(methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.PUT })
@Tag(name = "Group", description = "Contient les actions en lien avec les groupes.")
public class RestGroup {

    private final RestRDFHelper restRDFHelper;

    /*
     * recherche par Id Ark
     * Partie pour la négociation de contenu 
     * concernant les URI de type ARK avec header 
     * curl -L --header "Accept: application/rdf+xml »
     * curl -L --header "Accept: text/turtle »
     * curl -L --header "Accept: application/json »
     * curl -L --header "Accept: application/ld+json »
     */
    //  pour produire du RDF-SKOS
    @GetMapping(value = "/{naan}/{idArk}", produces = "application/rdf+xml;charset=UTF-8")
    public ResponseEntity<Object> getSkosFromArk__(@PathVariable("naan") String naan,
                                           @PathVariable("idArk") String arkId) {

        var datas = restRDFHelper.exportGroup(naan + "/" + arkId, "application/rdf+xml");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(datas);
    }    

    // pour produire du RDF-SKOS
    @GetMapping(value = "/{naan}/{idArk}.rdf", produces = "application/rdf+xml;charset=UTF-8")
    public ResponseEntity<Object> getSkosFromArk(@PathVariable("naan") String naan,
                                         @PathVariable("idArk") String arkId) {

        var datas = restRDFHelper.exportGroup(naan + "/" + arkId, "application/rdf+xml");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(datas);
    }
    
    // pour produire du Json
    @GetMapping(value = "/{naan}/{idArk}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Object> getJsonFromArk__(@PathVariable("naan") String naan,
                                           @PathVariable("idArk") String arkId) {

        var datas = restRDFHelper.exportGroup(naan + "/" + arkId, "application/json");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }    
    
    //pour produire du Json
    @GetMapping(value = "/{naan}/{idArk}.json", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Object> getJsonFromArk(@PathVariable("naan") String naan,
                                         @PathVariable("idArk") String arkId) {

        var datas = restRDFHelper.exportGroup(naan + "/" + arkId, "application/json");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }
    
    // pour produire du Json
    @GetMapping(value = "/{naan}/{idArk}", produces = "application/ld+json;charset=UTF-8")
    public ResponseEntity<Object> getJsonldFromArk__(@PathVariable("naan") String naan,
                                             @PathVariable("idArk") String arkId) {

        var datas = restRDFHelper.exportGroup(naan + "/" + arkId, "application/ld+json");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }
    
    //pour produire du Json
    @GetMapping(value = "/{naan}/{idArk}.jsonld", produces = "application/ld+json;charset=UTF-8")
    public ResponseEntity<Object> getJsonldFromArk(@PathVariable("naan") String naan,
                                           @PathVariable("idArk") String arkId) {

        var datas = restRDFHelper.exportGroup(naan + "/" + arkId, "application/ld+json");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(datas);
    }    
    
    //pour produire du Turtle
    @GetMapping(value = "/{naan}/{idArk}", produces = "text/turtle;charset=UTF-8")
    public ResponseEntity<Object> getTurtleFromArk__(@PathVariable("naan") String naan,
                                             @PathVariable("idArk") String arkId) {

        var datas = restRDFHelper.exportGroup(naan + "/" + arkId, "text/turtle");
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(datas);
    }    
    
    //pour produire du Turtle
    @GetMapping(value = "/{naan}/{idArk}.ttl", produces = "text/turtle;charset=UTF-8")
    public ResponseEntity<Object> getTurtleFromArk(@PathVariable("naan") String naan,
                                           @PathVariable("idArk") String arkId) {

        var datas = restRDFHelper.exportGroup(naan + "/" + arkId, "text/turtle");
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(datas);
    }

    /**
     * Permet d'exporter un group par Identifiant interne en précisant le thésaurus
     * curl -L --header "Accept: application/rdf+xml" "http://localhost:8083/opentheso/api/group/?id=5&theso=TH_1"
     */
    @GetMapping(produces = "application/rdf+xml;charset=UTF-8")
    public ResponseEntity<Object> searchJsonLd(@RequestParam("theso") String idTheso,
                                       @RequestParam("id") String idGroup) {

        var datas = restRDFHelper.exportGroup(idTheso, idGroup, "application/rdf+xml");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(datas);
    }
}
