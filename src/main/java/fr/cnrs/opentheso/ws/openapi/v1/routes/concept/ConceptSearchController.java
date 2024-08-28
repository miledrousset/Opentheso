package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
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
@RequestMapping("/concept/search")
@CrossOrigin(methods = { RequestMethod.GET })
public class ConceptSearchController {

    @Autowired
    private Connect connect;


    @GetMapping("/{idThesaurus}/{input}")
    public ResponseEntity<Object> searchAutocompleteV2(@PathVariable("input") String input,
                                               @PathVariable("idThesaurus") String idThesaurus,
                                               @RequestParam("lang") String lang,
                                               @RequestParam("group") String idGroup) throws JsonProcessingException {

        var concepts = new SearchHelper().searchConceptWSV2(connect.getPoolConnexion(), input, lang, idGroup, idThesaurus);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new ObjectMapper().writeValueAsString(concepts));
    }


    @GetMapping("/groups/{idThesaurus}/{idLang}")
    public ResponseEntity<Object> getGroupsByThesaurus(@PathVariable("idThesaurus") String idThesaurus,
                                         @PathVariable("idLang") String idLang) throws JsonProcessingException {

        var groups = new GroupHelper().getListRootConceptGroup(connect.getPoolConnexion(), idThesaurus, idLang, true);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new ObjectMapper().writeValueAsString(groups));
    }
}
