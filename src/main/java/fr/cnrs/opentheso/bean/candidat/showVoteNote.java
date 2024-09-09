/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.candidats.NodeTabVote;
import fr.cnrs.opentheso.models.candidats.NodeVote;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.repositories.candidats.CandidatDao;
import fr.cnrs.opentheso.models.candidats.CandidatDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "showVoteNote")
@SessionScoped
public class showVoteNote implements Serializable {

    @Autowired @Lazy private Connect connect;
    @Autowired @Lazy private SelectedTheso selectedTheso;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private CandidatDao candidatDao;

    @Autowired
    private NoteHelper noteHelper;

    private String userName;
    private String candidat;
    private ArrayList<NodeTabVote> nodeTabVotes;
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){

    }
    
    public showVoteNote() {
    }

    public void reset() {

    }
    
    /**
     * permet de préparer la vue pour les votes sur les notes pour un candidat
     * 
     * @param selectedCandidate
     */
    public void prepareVoteNote(CandidatDto selectedCandidate){
        String idCandidate = selectedCandidate.getIdConcepte();
        nodeTabVotes = new ArrayList<>();

        NodeNote nodeNote;

        userName = selectedCandidate.getCreatedBy();
        candidat = selectedCandidate.getNomPref();

        ArrayList<NodeVote> nodeVotes = candidatDao.getAllVoteNotes(connect.getPoolConnexion(), idCandidate, selectedTheso.getCurrentIdTheso());
        
        for (NodeVote nodeVote : nodeVotes) {
            NodeTabVote nodeTabVote = new NodeTabVote();
            nodeTabVote.setIdUser(nodeVote.getIdUser());
            nodeTabVote.setUserName(userHelper.getNameUser(connect.getPoolConnexion(), nodeVote.getIdUser()));
            
            /// pour récupérer les notes
            try {
                nodeNote = noteHelper.getNoteByIdNote(connect.getPoolConnexion(), Integer.parseInt(nodeVote.getIdNote()));
                nodeTabVote.setTypeNote(nodeNote.getNoteTypeCode());
                nodeTabVote.setNoteValue(nodeNote.getLexicalValue());
            } catch (Exception e) {
            }
            nodeTabVotes.add(nodeTabVote);
        }
    }
}
