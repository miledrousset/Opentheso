package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dto.CandidatDetailsDto;
import fr.cnrs.opentheso.bean.condidat.dto.CorpusDto;
import fr.cnrs.opentheso.bean.condidat.dto.TraductionDto;
import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
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

    private List<CandidatDto> candidatList;
    
    private String message;
    
    private CandidatDetailsDto candidatSelected;
    

    public CandidatPool() {
        
        candidatSelected = new CandidatDetailsDto();
        
        isListCandidatsActivate = true;
        isNewCandidatActivate = false;

        candidatList = new ArrayList<>();
        
        candidatList = getAllCandidats();
        
    }
    
    public List<CandidatDto> getAllCandidats() {
        
        List<CandidatDto> temps = new ArrayList<>();
        CandidatDto candidatDto = new CandidatDto();
        candidatDto.setCreationDate(new Date());
        candidatDto.setNbrDemande(4);
        candidatDto.setStatut("En cours");
        candidatDto.setNbrParticipant(66);
        candidatDto.setNomPref("Firas GABSI");
        temps.add(candidatDto);
        
        candidatDto.setNomPref("BOB TODO");
        temps.add(candidatDto);
        
        candidatDto.setNomPref("Julien");
        temps.add(candidatDto);
        
        return temps;
    }

    public List<CandidatDto> getCandidatList() {
        return candidatList;
    }

    public void setCandidatList(List<CandidatDto> candidatList) {
        this.candidatList = candidatList;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CandidatDetailsDto getCandidatSelected() {
        return candidatSelected;
    }

    public void setCandidatSelected(CandidatDetailsDto candidatSelected) {
        this.candidatSelected = candidatSelected;
    }
    
}
