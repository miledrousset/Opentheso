package fr.cnrs.opentheso.ws.graphql;

import fr.cnrs.opentheso.models.concept.NodeFullConcept;
import fr.cnrs.opentheso.repositories.SearchHelper;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor

public class NodeFullConceptController {
    private final NodeFullConceptService nodeFullConceptService;
    private final SearchHelper searchHelper;

    @QueryMapping
    public NodeFullConcept getNodeFullConcept(
            @Argument String idTheso,
            @Argument String idConcept,
            @Argument String idLang) {
        return nodeFullConceptService.getFullConcept(idTheso, idConcept, idLang, 0, 10);
    }

    @QueryMapping
    public List<NodeFullConcept> searchConcept(
            @Argument String idTheso,
            @Argument String value,
            @Argument String idLang) {
        ArrayList<String> listIds = searchHelper.searchFullTextElasticId(value, idLang, idTheso);

        List<NodeFullConcept> nodeFullConcepts = new ArrayList<>();
        for (String idConcept : listIds) {
            nodeFullConcepts.add(nodeFullConceptService.getFullConcept(idTheso, idConcept, idLang, 0, 10));
        }
        return nodeFullConcepts;
    }
}