package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;


@Named(value = "discussionService")
@SessionScoped
public class DiscussionService implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private CandidatPool candidatPool;

    private List<String> participants;
    
    
    public DiscussionService () {
        participants = new ArrayList<>();
        participants.add("Firas GABSI");
        participants.add("Firas GABSI");
        participants.add("Firas GABSI");
    }
    
    public void sendMessage () {
        
        MessageDto messageDto = new MessageDto();
        messageDto.setDate(new Date().toString());
        messageDto.setMine(true);
        messageDto.setNom("Firas");
        messageDto.setMsg(candidatPool.getMessage());
        
        candidatPool.getCandidatSelected().getMessages().add(messageDto);
        candidatPool.setMessage("");
        
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Message envoyé !", null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

}
