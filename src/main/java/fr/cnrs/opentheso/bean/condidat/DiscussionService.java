package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
    private CandidatBean candidatBean;

    @Inject
    private Connect connect;

    private List<String> participants;
    
    
    public DiscussionService () {
        participants = new ArrayList<>();
        participants.add("Firas GABSI");
        participants.add("Firas GABSI");
        participants.add("Firas GABSI");
    }
    
    public void sendMessage () throws SQLException {
        
        MessageDto messageDto = new MessageDto();
        messageDto.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        messageDto.setNom(candidatBean.getCurrentUser().getUsername().toUpperCase());
        messageDto.setMsg(candidatBean.getMessage());
        
        new CandidatService().addNewMessage(connect, candidatBean.getMessage(), 
                candidatBean.getCurrentUser().getNodeUser().getIdUser()+"", 
                candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus());
        
        candidatBean.getCandidatSelected().setMessages(new CandidatService()
                .getAllMessagesCandidat(connect, 
                        candidatBean.getCandidatSelected().getIdConcepte(), 
                        candidatBean.getCandidatSelected().getIdThesaurus(), 
                        candidatBean.getCurrentUser().getUsername()));
        
        candidatBean.setMessage("");
        
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
