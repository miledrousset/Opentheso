package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dto.TraductionDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

@Named(value = "traductionService")
@SessionScoped
public class TraductionService implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private CandidatBean candidatBean;

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


        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Traduction ajoutée avec sucée", null);
        FacesContext.getCurrentInstance().addMessage(null, message);
        
        candidatBean.getCandidatSelected().getTraductions().add(new TraductionDto(newLangage, newTraduction));
        candidatBean.setIsNewCandidatActivate(true);
    }

    public void deleteTraduction() {
        List<TraductionDto> temps = candidatBean.getCandidatSelected().getTraductions();

        for (int i = 0; i < temps.size(); i++) {
            if (traduction != null && traduction.equals(temps.get(i).getTraduction())) {
                temps.remove(i);
            }
        }

        if (candidatBean.getCandidatSelected().getTraductions().size() != temps.size()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Traduction supprimée avec succée !", null);
            FacesContext.getCurrentInstance().addMessage(null, message);

            candidatBean.getCandidatSelected().setTraductions(temps);
            candidatBean.setIsNewCandidatActivate(true);
        }
    }

    public void updateTraduction() {

        List<TraductionDto> temps = candidatBean.getCandidatSelected().getTraductions();

        for (TraductionDto traductionDto : temps) {
            if (traductionOld != null && traductionOld.equals(traductionDto.getTraduction())
                    && langageOld != null && langageOld.equals(traductionDto.getLangue())) {
                traductionDto.setLangue(langage);
                traductionDto.setTraduction(traduction);
            }
        }

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Traduction mise à jour avec succée !", null);
        FacesContext.getCurrentInstance().addMessage(null, message);

        candidatBean.getCandidatSelected().setTraductions(temps);
        candidatBean.setIsNewCandidatActivate(true);
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
