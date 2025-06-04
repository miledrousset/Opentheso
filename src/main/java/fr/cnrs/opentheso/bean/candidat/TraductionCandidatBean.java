package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.models.candidats.TraductionDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.services.TermService;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.primefaces.PrimeFaces;


@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "traductionCandidatBean")
public class TraductionCandidatBean implements Serializable {

    private final CandidatBean candidatBean;
    private final LanguageBean languageBean;
    private final SelectedTheso selectedTheso;
    private final TermService termService;

    private String langage, traduction, langageOld, traductionOld, newLangage, newTraduction;
    private List<NodeLangTheso> nodeLanguesFiltered;


    public void init(TraductionDto traductionDto) {
        langage = traductionDto.getLangue();
        traduction = traductionDto.getTraduction();

        langageOld = langage;
        traductionOld = traduction;
    }

    public void init() {
        newLangage = "";
        newTraduction = "";
        initLanguages();
    }

    private void initLanguages() {

        nodeLanguesFiltered = new ArrayList<>();
        nodeLanguesFiltered.addAll(selectedTheso.getNodeLangs());

        // les langues à ignorer
        List<String> languesToRemove = new ArrayList<>();
        languesToRemove.add(candidatBean.getCandidatSelected().getLang());
        for (TraductionDto traductionDto : candidatBean.getCandidatSelected().getTraductions()) {
            languesToRemove.add(traductionDto.getLangue());
        }

        for (NodeLangTheso nodeLang : selectedTheso.getNodeLangs()) {
            if (languesToRemove.contains(nodeLang.getCode())) {
                nodeLanguesFiltered.remove(nodeLang);
            }
        }
    }

    public void deleteTraduction() {

        termService.deleteTerm(candidatBean.getCandidatSelected().getIdThesaurus(), candidatBean.getCandidatSelected().getIdTerm(), langage);

        loadTraductionList();
        MessageUtils.showInformationMessage(languageBean.getMsg("candidat.traduction.msg2"));
        PrimeFaces.current().ajax().update("containerIndex");
    }

    public void updateTraduction() {

        termService.updateIntitule(traduction, candidatBean.getCandidatSelected().getIdTerm(),
                candidatBean.getCandidatSelected().getIdThesaurus(), langage);

        loadTraductionList();
        MessageUtils.showInformationMessage(languageBean.getMsg("candidat.traduction.msg3"));
        PrimeFaces.current().ajax().update("containerIndex");
    }

    public void addTraductionCandidat() {

        if (termService.isTermExistIgnoreCase(newTraduction, candidatBean.getCandidatSelected().getIdThesaurus(), newLangage)) {
            MessageUtils.showErrorMessage("Un label existe dans le thésaurus pour : "
                    + candidatBean.getCandidatSelected().getIdConcepte() + "#" + newTraduction + "(" + langage + ")");
            return;
        }

        Term term = new Term();
        term.setStatus("D");
        term.setSource("Candidat");
        term.setLang(newLangage);
        term.setLexicalValue(newTraduction);
        term.setIdThesaurus(candidatBean.getCandidatSelected().getIdThesaurus());
        term.setContributor(candidatBean.getCandidatSelected().getUserId());
        term.setCreator(candidatBean.getCandidatSelected().getUserId());
        term.setIdTerm(candidatBean.getCandidatSelected().getIdTerm());
        termService.addNewTerme(term);

        loadTraductionList();
        MessageUtils.showInformationMessage(languageBean.getMsg("candidat.traduction.msg1"));
    }

    private void loadTraductionList() {

        var traductions = termService.getTraductionsOfConcept(candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus(), candidatBean.getCandidatSelected().getLang());
        var traductionsMapped = traductions.stream()
                .map(element -> TraductionDto.builder()
                        .langue(element.getLang())
                        .traduction(element.getLexicalValue())
                        .codePays(element.getCodePays())
                        .build())
                .toList();
        candidatBean.getCandidatSelected().setTraductions(traductionsMapped);
        PrimeFaces.current().ajax().update("containerIndex");
    }
}
