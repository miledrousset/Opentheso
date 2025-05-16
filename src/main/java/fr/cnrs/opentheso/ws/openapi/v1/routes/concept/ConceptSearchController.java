package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.services.GroupService;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/openapi/v1/concept/search")
@CrossOrigin(methods = { RequestMethod.GET })
@Tag(name = "Concept", description = "Contient toutes les actions disponibles sur les concepts.")
public class ConceptSearchController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private SearchHelper searchHelper;


    @GetMapping("/{idThesaurus}/{input}")
    public ResponseEntity<Object> searchAutocompleteV2(@PathVariable("input") String input,
                                               @PathVariable("idThesaurus") String idThesaurus,
                                               @RequestParam("lang") String lang,
                                               @RequestParam(value = "group", required = false) String idGroup) {

        var concepts = searchHelper.searchConceptWSV2(input, lang, idGroup, idThesaurus);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(concepts);
    }


    @GetMapping("/groups/{idThesaurus}/{idLang}")
    public ResponseEntity<Object> getGroupsByThesaurus(@PathVariable("idThesaurus") String idThesaurus,
                                         @PathVariable("idLang") String idLang) {

        var groups = groupService.getListRootConceptGroup(idThesaurus, idLang, true, false);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(groups);
    }
}
