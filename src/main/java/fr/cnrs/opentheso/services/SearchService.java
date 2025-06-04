package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.repositories.SearchRepository;
import fr.cnrs.opentheso.utils.StringUtils;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Data
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;

    public List<NodeIdValue> searchAutoCompletionForRelationIdValue(String value, String idLang, String idTheso) {

        var processedValue = StringUtils.unaccentLowerString(StringUtils.convertString(value));

        List<NodeIdValue> results = new ArrayList<>();
        // Preferred terms
        var preferred = searchRepository.searchPreferredLabels(processedValue, idLang, idTheso);
        for (Object[] row : preferred) {
            results.add(NodeIdValue.builder()
                    .id((String) row[0])
                    .value((String) row[1])
                    .build());
        }

        // Non-preferred terms
        var nonPreferred = searchRepository.searchAltLabels(processedValue, idLang, idTheso);
        for (Object[] row : nonPreferred) {
            results.add(NodeIdValue.builder()
                    .id((String) row[0])
                    .value((String) row[1])
                    .build());
        }

        return results;
    }

    public List<NodeSearchMini> searchAutoCompletionForRelation(String value, String idLang, String idThesaurus, boolean includeDeprecated) {

        // Nettoyage de la requête utilisateur
        value = StringUtils.convertString(value);
        value = StringUtils.unaccentLowerString(value);

        // Initialisation de la liste des résultats
        List<NodeSearchMini> results = new ArrayList<>();

        // Appel à la méthode nativeQuery pour les termes préférés
        List<Object[]> preferred = includeDeprecated
                ? searchRepository.searchPreferredLabels(value, idLang, idThesaurus)
                : searchRepository.searchPreferredLabelsWithoutDeprecated(value, idLang, idThesaurus);

        for (Object[] row : preferred) {
            NodeSearchMini mini = new NodeSearchMini();
            mini.setIdConcept((String) row[0]);
            mini.setPrefLabel((String) row[1]);
            mini.setAltLabel(false);
            results.add(mini);
        }

        // Appel à la méthode nativeQuery pour les termes non-préférés (synonymes)
        List<Object[]> alt = includeDeprecated
                ? searchRepository.searchAltLabels(value, idLang, idThesaurus)
                : searchRepository.searchAltLabelsWithoutDeprecated(value, idLang, idThesaurus);

        for (Object[] row : alt) {
            NodeSearchMini mini = new NodeSearchMini();
            mini.setIdConcept((String) row[0]);
            mini.setAltLabelValue(((String) row[1]).split(" ->")[0]);
            mini.setPrefLabel(((String) row[1]).split(" ->")[1]);
            mini.setAltLabel(true);
            results.add(mini);
        }

        return results;
    }

    public List<NodeSearchMini> searchAutoCompletionForCustomRelation(String value, String idLang, String idTheso) {
        String cleanedValue = StringUtils.unaccentLowerString(StringUtils.convertString(value));

        List<NodeSearchMini> result = new ArrayList<>();

        List<Object[]> preferredResults = searchRepository.searchPreferredCustomRelations(cleanedValue, idLang, idTheso);
        for (Object[] row : preferredResults) {
            result.add(NodeSearchMini.builder()
                    .idConcept((String) row[0])
                    .prefLabel((String) row[1])
                    .conceptType((String) row[2])
                    .altLabel(false)
                    .build());
        }

        List<Object[]> altResults = searchRepository.searchAltCustomRelations(cleanedValue, idLang, idTheso);
        for (Object[] row : altResults) {
            result.add(NodeSearchMini.builder()
                    .idConcept((String) row[0])
                    .altLabelValue((String) row[1])
                    .prefLabel(row[1].toString().split("->")[1].trim()) // récupération du PT
                    .conceptType((String) row[2])
                    .altLabel(true)
                    .build());
        }

        return result;
    }

}
