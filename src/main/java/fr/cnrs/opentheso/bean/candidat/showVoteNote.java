package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.models.candidats.enumeration.VoteType;
import fr.cnrs.opentheso.repositories.CandidatVoteRepository;
import fr.cnrs.opentheso.models.candidats.NodeTabVote;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.models.candidats.CandidatDto;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.services.NoteService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "showVoteNote")
public class showVoteNote implements Serializable {

    private final SelectedTheso selectedTheso;
    private final UserRepository userRepository;
    private final CandidatVoteRepository candidatVoteRepository;
    private final NoteService noteService;

    private String userName, candidat;
    private List<NodeTabVote> nodeTabVotes;

    
    /**
     * permet de préparer la vue pour les votes sur les notes pour un candidat
     * 
     * @param selectedCandidate
     */
    public void prepareVoteNote(CandidatDto selectedCandidate){

        nodeTabVotes = new ArrayList<>();

        userName = selectedCandidate.getCreatedBy();
        candidat = selectedCandidate.getNomPref();

        var nodeVotes = candidatVoteRepository.findAllByIdConceptAndIdThesaurusAndTypeVote(selectedCandidate.getIdConcepte(),
                selectedTheso.getCurrentIdTheso(), VoteType.NOTE.getLabel());

        if (CollectionUtils.isNotEmpty(nodeVotes)) {
            nodeTabVotes = nodeVotes.stream().map(element -> {

                var nodeNote = noteService.getNoteByIdNote(Integer.parseInt(element.getIdNote()));
                return NodeTabVote.builder()
                        .idUser(element.getIdUser())
                        .userName(userRepository.findById(element.getIdUser()).get().getUsername())
                        .noteValue(nodeNote.getLexicalValue())
                        .typeNote(nodeNote.getNoteTypeCode())
                        .build();
            }).toList();
        }
    }
}
