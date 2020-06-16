package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;

import javax.inject.Inject;
import javax.inject.Named;


@Named(value = "candidatPool")
@SessionScoped
public class CandidatPool implements Serializable {

    @Inject
    private Connect connect;

    private boolean isListCandidatsActivate;
    private boolean isNewCandidatActivate;

    private List<CandidatDto> candidatPool;

    private List<TraductionDto> traductionList;
    

    public CandidatPool() {

        isListCandidatsActivate = true;
        isNewCandidatActivate = false;

        traductionList = new ArrayList<>();
        TraductionDto dto = new TraductionDto();
        dto.setLangue("Lan");
        dto.setTraduction("Trad");
        traductionList.add(dto);  

    }

    public List<TraductionDto> getTraductionList() {
        return traductionList;
    }

    public void setTraductionList(List<TraductionDto> traductionList) {
        this.traductionList = traductionList;
    }

    public List<CandidatDto> getCandidatPool() {
        return candidatPool;
    }

    public void setCandidatPool(List<CandidatDto> candidatPool) {
        this.candidatPool = candidatPool;
    }

    public boolean isIsListCandidatsActivate() {
        return isListCandidatsActivate;
    }

    public void setIsListCandidatsActivate(boolean isListCandidatsActivate) {
        this.isListCandidatsActivate = isListCandidatsActivate;
        isNewCandidatActivate = false;
    }

    public boolean isIsNewCandidatActivate() {
        return isNewCandidatActivate;
    }

    public void setIsNewCandidatActivate(boolean isNewCandidatActivate) {
        this.isNewCandidatActivate = isNewCandidatActivate;
        isListCandidatsActivate = false;
    }
 
}
