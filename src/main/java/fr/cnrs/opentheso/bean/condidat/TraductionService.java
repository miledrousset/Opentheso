package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dto.TraductionDto;
import fr.cnrs.opentheso.bean.condidat.enumeration.LanguageEnum;
import fr.cnrs.opentheso.bean.language.LanguageBean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;

@Named(value = "traductionService")
@SessionScoped
public class TraductionService implements Serializable {

    @Inject
    private CandidatBean candidatBean;

    @Inject
    private LanguageBean languageBean;

    private String langage;
    private String traduction;

    private String langageOld;
    private String traductionOld;

    private String newLangage;
    private String newTraduction;
    

    public TraductionService() {
        langage = null;
        traduction = null;
    }

    public void init(TraductionDto traductionDto) {
        langage = traductionDto.getLangue();
        traduction = traductionDto.getTraduction();

        langageOld = langage;
        traductionOld = traduction;
    }
    
    public void init() {
        newLangage = "";
        newTraduction = "";
    }

    public void addTraductionCandidat() {
        for (TraductionDto traduction : candidatBean.getCandidatSelected().getTraductions()) {
            if (traduction.getLangue().equals(LanguageEnum.valueOf(newLangage).getLanguage())
                && traduction.getTraduction().equals(newTraduction)) {
                return;
            }
        }

        TraductionDto traduction = new TraductionDto();
        traduction.setLangue(LanguageEnum.valueOf(newLangage).getLanguage());
        traduction.setTraduction(newTraduction);
        candidatBean.getCandidatSelected().getTraductions().add(traduction);

        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg1"));
    }

    public void deleteTraduction() {

        List<TraductionDto> temps =  candidatBean.getCandidatSelected().getTraductions();
        for (int i = 0; i < temps.size(); i++) {
            if (temps.get(i).getTraduction().equalsIgnoreCase(traductionOld) && 
                    temps.get(i).getLangue().equalsIgnoreCase(langageOld)) {
                temps.remove(i);
            }
        }
        candidatBean.getCandidatSelected().setTraductions(temps);

        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg2"));
    }

    public void updateTraduction() {
        for (TraductionDto traductionDto : candidatBean.getCandidatSelected().getTraductions()) {
            if (traductionDto.getTraduction().equalsIgnoreCase(traductionOld) && 
                    traductionDto.getLangue().equalsIgnoreCase(langageOld)) {
                traductionDto.setLangue(langage);
                traductionDto.setTraduction(traduction);
            }
        }

        candidatBean.showMessage(FacesMessage.SEVERITY_INFO,  languageBean.getMsg("candidat.traduction.msg3"));
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

}
