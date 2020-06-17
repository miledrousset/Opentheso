package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dto.CorpusDto;
import fr.cnrs.opentheso.bean.condidat.dto.TraductionDto;
import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
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
    private List<CorpusDto> corpusList;
    private List<MessageDto> messagesList;
    
    private String message;
    

    public CandidatPool() {
        isListCandidatsActivate = true;
        isNewCandidatActivate = false;

        corpusList = new ArrayList<>();
        traductionList = new ArrayList<>();
        messagesList = new ArrayList<>();
        
        messagesList.add(new MessageDto("Toto", "DATE DU JOUR", "Coucou from me", false));
        messagesList.add(new MessageDto("Toto", "DATE DU JOUR", "Coucou from me", false));
        messagesList.add(new MessageDto("Firas", "DATE DU JOUR", "Coucou from me", true));
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

    public List<CorpusDto> getCorpusList() {
        return corpusList;
    }

    public void setCorpusList(List<CorpusDto> corpusList) {
        this.corpusList = corpusList;
    }

    public List<MessageDto> getMessagesList() {
        return messagesList;
    }

    public void setMessagesList(List<MessageDto> messagesList) {
        this.messagesList = messagesList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
