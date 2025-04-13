package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.dto.HistoryValue;
import fr.cnrs.opentheso.models.TermHistoriqueProjection;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.repositories.HistoriqueRepository;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import lombok.Data;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;


@Data
@SessionScoped
@Named(value = "historyBean")
public class HistoryBean implements Serializable {

    private ConceptView conceptBean;
    private SelectedTheso selectedTheso;
    private HistoriqueRepository termHistoriqueRepository;

    private List<HistoryValue> historyLabels, historySynonyms, historyRelations, historyNotes;


    @Inject
    public HistoryBean(ConceptView conceptBean, SelectedTheso selectedTheso, HistoriqueRepository termHistoriqueRepository) {

        this.conceptBean = conceptBean;
        this.selectedTheso = selectedTheso;
        this.termHistoriqueRepository = termHistoriqueRepository;
    }

    public void reset() {

        historyLabels = getTermHistories();
        historySynonyms =getSynonymHistories();
        historyRelations = getRelationHistories();
        historyNotes = getNoteHistories();
    }

    private List<HistoryValue> getTermHistories() {
        var termHistories = termHistoriqueRepository.findTermHistories(conceptBean.getNodeConcept().getTerm().getIdTerm(),
                selectedTheso.getCurrentIdTheso());
        return historyValueMap(termHistories);
    }

    private List<HistoryValue> getSynonymHistories() {
        var termHistories = termHistoriqueRepository.findSynonymHistories(conceptBean.getNodeConcept().getTerm().getIdTerm(),
                selectedTheso.getCurrentIdTheso());
        return historyValueMap(termHistories);
    }

    private List<HistoryValue> getRelationHistories() {
        var termHistories = termHistoriqueRepository.findgetRelationsHistories(conceptBean.getNodeConcept().getTerm().getIdTerm(),
                selectedTheso.getCurrentIdTheso());
        return historyValueMap(termHistories);
    }

    private List<HistoryValue> getNoteHistories() {
        var termHistories = termHistoriqueRepository.getNotesHistories(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getNodeConcept().getTerm().getIdTerm(),
                selectedTheso.getCurrentIdTheso());
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

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour images !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

}
