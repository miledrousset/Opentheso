package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.models.nodes.NodeIdValue;
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


}
