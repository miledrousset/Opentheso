package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.dto.HistoryValue;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.HistoryService;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;


@Data
@SessionScoped
@Named(value = "historyBean")
public class HistoryBean implements Serializable {

    private final ConceptView conceptBean;
    private final SelectedTheso selectedTheso;
    private final HistoryService historyService;

    private List<HistoryValue> historyLabels, historySynonyms, historyRelations, historyNotes;


    public void reset() {

        var idThesaurus = selectedTheso.getCurrentIdTheso();
        var idConcept = conceptBean.getNodeConcept().getConcept().getIdConcept();
        var idTerm = conceptBean.getNodeConcept().getTerm().getIdTerm();

        historyLabels = historyService.getTermHistories(idTerm, idThesaurus);
        historySynonyms = historyService.getSynonymHistories(idTerm, idThesaurus);
        historyRelations = historyService.getRelationHistories(idTerm, idThesaurus);
        historyNotes = historyService.getNoteHistories(idConcept, idTerm, idThesaurus);
    }
}
