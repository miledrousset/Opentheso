package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.repositories.ConceptFacetRepository;
import fr.cnrs.opentheso.repositories.FacetHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Slf4j
@Service
@AllArgsConstructor
public class FacetService {

    private final ConceptFacetRepository facetRepository;
    private final FacetHelper facetHelper;


    public void deleteFacets(String idThesaurus, String idConcept) {

        log.info("Suppression de tous les facette rattachées au concept id {} dans le thésaurus id {}", idConcept, idThesaurus);
        var listFacets = facetHelper.getAllIdFacetsOfConcept(idConcept, idThesaurus);
        for (String idFacet : listFacets) {
            log.info("Suppression de la facet id {}", idFacet);
            facetHelper.deleteFacet(idFacet, idThesaurus);
        }
    }

    public void deleteConceptFromFacets(String idThesaurus, String idConcept) {

        log.info("Suppression de la facet id {} dans le thésaurus id {}", idConcept, idThesaurus);
        facetRepository.deleteAllByIdConceptAndIdThesaurus(idConcept, idThesaurus);
    }
}
