package fr.cnrs.opentheso.services.candidats;

import fr.cnrs.opentheso.models.terms.Term;

import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.repositories.candidats.TermeDao;
import fr.cnrs.opentheso.models.candidats.TraductionDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import java.io.IOException;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;

@Named(value = "traductionService")
@SessionScoped
public class TraductionService implements Serializable {

    @Autowired @Lazy
    private CandidatBean candidatBean;
    @Autowired @Lazy
    private LanguageBean languageBean;
    @Autowired @Lazy
    private SelectedTheso selectedTheso;

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
        TermeDao termeDao = new TermeDao();
        try {
            termeDao.deleteTermByIdTermAndLang(candidatBean.getConnect().getPoolConnexion(),
                    candidatBean.getCandidatSelected().getIdTerm(),
                    langage,
                    candidatBean.getCandidatSelected().getIdThesaurus());
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
            candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg2"));
        } catch (SQLException | IOException ex) {
            Logger.getLogger(TraductionService.class.getName()).log(Level.SEVERE, null, ex);
            candidatBean.showMessage(FacesMessage.SEVERITY_INFO, ex.getMessage());
        }

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex");
    }

    /**
     * permet de modifier une traduction #MR
     */
    public void updateTraduction() {
        TermeDao termeDao = new TermeDao();
        try {
            termeDao.updateIntitule(candidatBean.getConnect().getPoolConnexion(),
                    traduction,
                    candidatBean.getCandidatSelected().getIdTerm(),
                    candidatBean.getCandidatSelected().getIdThesaurus(),
                    langage);
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
            candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg3"));
        } catch (SQLException | IOException ex) {
            Logger.getLogger(TraductionService.class.getName()).log(Level.SEVERE, null, ex);
            candidatBean.showMessage(FacesMessage.SEVERITY_INFO, ex.getMessage());
        }

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex");
    }

    public String getLangage() {
        return langage;
    }

    public void setLangage(String langage) {
        this.langage = langage;
    }

    public String getTraduction() {
        return traduction;
    }

    public void setTraduction(String traduction) {
        this.traduction = traduction;
    }

    public String getLangageOld() {
        return langageOld;
    }

    public void setLangageOld(String langageOld) {
        this.langageOld = langageOld;
    }

    public String getTraductionOld() {
        return traductionOld;
    }

    public void setTraductionOld(String traductionOld) {
        this.traductionOld = traductionOld;
    }

    public String getNewLangage() {
        return newLangage;
    }

    public void setNewLangage(String newLangage) {
        this.newLangage = newLangage;
    }

    public String getNewTraduction() {
        return newTraduction;
    }

    public void setNewTraduction(String newTraduction) {
        this.newTraduction = newTraduction;
    }

    public ArrayList<NodeLangTheso> getNodeLangsFiltered() {
        return nodeLangsFiltered;
    }

    public void setNodeLangsFiltered(ArrayList<NodeLangTheso> nodeLangsFiltered) {
        this.nodeLangsFiltered = nodeLangsFiltered;
    }

    public String getMessages() {
        return messages.toString();
    }
}
