package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.dto.HistoryValue;
import fr.cnrs.opentheso.models.TermHistoriqueProjection;
import fr.cnrs.opentheso.repositories.HistoriqueRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class HistoryService {

    private final HistoriqueRepository historiqueRepository;


    public List<HistoryValue> getTermHistories(String idTerm, String idThesaurus) {
        var termHistories = historiqueRepository.findTermHistories(idTerm, idThesaurus);
        return historyValueMap(termHistories);
    }

    public List<HistoryValue> getSynonymHistories(String idTerm, String idThesaurus) {
        var termHistories = historiqueRepository.findSynonymHistories(idTerm, idThesaurus);
        return historyValueMap(termHistories);
    }

    public List<HistoryValue> getRelationHistories(String idTerm, String idThesaurus) {
        var termHistories = historiqueRepository.findgetRelationsHistories(idTerm, idThesaurus);
        return historyValueMap(termHistories);
    }

    public List<HistoryValue> getNoteHistories(String idConcept, String idTerm, String idThesaurus) {
        var termHistories = historiqueRepository.getNotesHistories(idConcept, idTerm, idThesaurus);
        return historyValueMap(termHistories);
    }

    private List<HistoryValue> historyValueMap(List<TermHistoriqueProjection> termHistories) {
        return CollectionUtils.isEmpty(termHistories)
                ? List.of()
                : termHistories.stream()
                .map(element -> HistoryValue.builder()
                        .value(element.getLexicalValue())
                        .lang(element.getLang())
                        .noteType(element.getNotetypecode())
                        .role(element.getRole())
                        .action(StringUtils.isEmpty(element.getAction()) ? element.getActionPerformed() :  element.getAction())
                        .date(Date.from(element.getModified().atZone(ZoneId.systemDefault()).toInstant()))
                        .user(element.getUsername())
                        .build())
                .toList();
    }
}
