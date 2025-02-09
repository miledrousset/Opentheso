/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.models.candidats.NodeTabVote;
import fr.cnrs.opentheso.models.candidats.NodeVote;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.candidats.CandidatDao;
import fr.cnrs.opentheso.models.candidats.CandidatDto;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;

import java.io.Serializable;
import java.util.ArrayList;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "showVoteNote")
@SessionScoped
public class showVoteNote implements Serializable {
    
    @Autowired
    private SelectedTheso selectedTheso;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidatDao candidatDao;

    @Autowired
    private NoteHelper noteHelper;

    private String userName, candidat;
    private ArrayList<NodeTabVote> nodeTabVotes;

    
    /**
     * permet de préparer la vue pour les votes sur les notes pour un candidat
     * 
     * @param selectedCandidate
     */
    public void prepareVoteNote(CandidatDto selectedCandidate){

        String idCandidate = selectedCandidate.getIdConcepte();
        nodeTabVotes = new ArrayList<>();

        userName = selectedCandidate.getCreatedBy();
        candidat = selectedCandidate.getNomPref();

        ArrayList<NodeVote> nodeVotes = candidatDao.getAllVoteNotes(idCandidate, selectedTheso.getCurrentIdTheso());
        
        for (NodeVote nodeVote : nodeVotes) {
            NodeTabVote nodeTabVote = new NodeTabVote();
            nodeTabVote.setIdUser(nodeVote.getIdUser());
            nodeTabVote.setUserName(userRepository.findById(nodeVote.getIdUser()).get().getUsername());

            var nodeNote = noteHelper.getNoteByIdNote(Integer.parseInt(nodeVote.getIdNote()));
            nodeTabVote.setTypeNote(nodeNote.getNoteTypeCode());
            nodeTabVote.setNoteValue(nodeNote.getLexicalValue());
            nodeTabVotes.add(nodeTabVote);
        }
    }
}
