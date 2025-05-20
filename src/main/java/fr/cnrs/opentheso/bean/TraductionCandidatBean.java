package fr.cnrs.opentheso.bean;

import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.models.candidats.TraductionDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.services.TermService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
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
    private List<NodeLangTheso> nodeLangs, nodeLangsFiltered;
    private StringBuilder messages = new StringBuilder();


    public void init(TraductionDto traductionDto) {
        langage = traductionDto.getLangue();
        traduction = traductionDto.getTraduction();

        langageOld = langage;
        traductionOld = traduction;
    }

    public void init() {
        newLangage = "";
        newTraduction = "";
        nodeLangsFiltered = new ArrayList<>();
        initLanguages();
    }

    private void initLanguages() {

        nodeLangs = selectedTheso.getNodeLangs();
        nodeLangsFiltered.addAll(nodeLangs);

        // les langues à ignorer
        List<String> langsToRemove = new ArrayList<>();
        langsToRemove.add(candidatBean.getCandidatSelected().getLang());
        for (TraductionDto traductionDto : candidatBean.getCandidatSelected().getTraductions()) {
            langsToRemove.add(traductionDto.getLangue());
        }
        for (NodeLangTheso nodeLang : nodeLangs) {
            if (langsToRemove.contains(nodeLang.getCode())) {
                nodeLangsFiltered.remove(nodeLang);
            }
        }

    }

    /**
     * permet de supprimer une tradcution #MR
     */
    public void deleteTraduction() {

        termService.deleteTerm(candidatBean.getCandidatSelected().getIdThesaurus(), candidatBean.getCandidatSelected().getIdTerm(), langage);
        candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg2"));

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
    }

    /**
     * permet de modifier une traduction #MR
     */
    public void updateTraduction() {
        termService.updateIntitule(traduction, candidatBean.getCandidatSelected().getIdTerm(),
                candidatBean.getCandidatSelected().getIdThesaurus(), langage);
        candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg3"));

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
    }

    public void addTraductionCandidat() {

        if (termService.isTermExistIgnoreCase(newTraduction, candidatBean.getCandidatSelected().getIdThesaurus(), newLangage)) {
            messages = new StringBuilder();
            messages.append("Un label existe dans le thésaurus pour : ").append(candidatBean.getCandidatSelected().getIdConcepte())
                    .append("#").append(newTraduction).append(" (").append(langage).append(")");
            candidatBean.showMessage(FacesMessage.SEVERITY_ERROR, messages.toString());
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

        candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg1"));

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");

    }
}
