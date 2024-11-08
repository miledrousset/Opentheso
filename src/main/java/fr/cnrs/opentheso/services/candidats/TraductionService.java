package fr.cnrs.opentheso.services.candidats;

import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.repositories.candidats.TermeDao;
import fr.cnrs.opentheso.models.candidats.TraductionDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.primefaces.PrimeFaces;


@Data
@Named(value = "traductionService")
@SessionScoped
public class TraductionService implements Serializable {

    @Autowired
    private CandidatBean candidatBean;

    @Autowired
    private LanguageBean languageBean;

    @Autowired
    private SelectedTheso selectedTheso;

    @Autowired
    private TermHelper termHelper;

    @Autowired
    private TermeDao termeDao;

    private String langage;
    private String traduction;

    private String langageOld;
    private String traductionOld;

    private String newLangage;
    private String newTraduction;

    private ArrayList<NodeLangTheso> nodeLangs;
    private ArrayList<NodeLangTheso> nodeLangsFiltered; // uniquement les langues non traduits    

    private StringBuilder messages;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeLangs != null) {
            nodeLangs.clear();
            nodeLangs = null;
        }
        if (nodeLangsFiltered != null) {
            nodeLangsFiltered.clear();
            nodeLangsFiltered = null;
        }
        langage = null;
        traduction = null;
        langageOld = null;
        traductionOld = null;
        newLangage = null;
        newTraduction = null;
        messages = null;
    }

    public TraductionService() {
        langage = null;
        traduction = null;
        messages = new StringBuilder();
    }

    /**
     * set pour la modification d'une tradcution
     *
     * @param traductionDto
     */
    public void init(TraductionDto traductionDto) {
        langage = traductionDto.getLangue();
        traduction = traductionDto.getTraduction();

        langageOld = langage;
        traductionOld = traduction;
    }

    /**
     * set pour une nouvelle traduction
     */
    public void init() {
        newLangage = "";
        newTraduction = "";
        nodeLangsFiltered = new ArrayList<>();
        initLanguages();
    }

    private void initLanguages() {
        nodeLangs = selectedTheso.getNodeLangs();

        nodeLangs.forEach((nodeLang) -> {
            nodeLangsFiltered.add(nodeLang);
        });

        // les langues à ignorer
        ArrayList<String> langsToRemove = new ArrayList<>();
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
        termeDao.deleteTermByIdTermAndLang(candidatBean.getCandidatSelected().getIdTerm(), langage,
                candidatBean.getCandidatSelected().getIdThesaurus());
        candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg2"));

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex");
    }

    /**
     * permet de modifier une traduction #MR
     */
    public void updateTraduction() {
        termeDao.updateIntitule(traduction, candidatBean.getCandidatSelected().getIdTerm(),
                candidatBean.getCandidatSelected().getIdThesaurus(), langage);
        candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg3"));

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex");
    }

    public void addTraductionCandidat() {
        if (termHelper.isTermExistIgnoreCase(newTraduction, candidatBean.getCandidatSelected().getIdThesaurus(), newLangage)) {
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

        termeDao.addNewTerme(term);
        candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg1"));

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex");

    }
}
